package com.hwatong.platformadapter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.canbus.CarStatus;
import android.canbus.GpsStatus;
import android.canbus.ICanbusService;
import android.canbus.ICarStatusListener;
import android.canbus.ISystemStatusListener;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.IAudioFocusDispatcher;
import android.media.IAudioService;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.hwatong.platformadapter.handle.HandleAirControl;
import com.hwatong.platformadapter.handle.HandleAppControl;
import com.hwatong.platformadapter.handle.HandleCarControl;
import com.hwatong.platformadapter.handle.HandleCmdControl;
import com.hwatong.platformadapter.handle.HandleMusicControl;
import com.hwatong.platformadapter.handle.HandlerBtPhoneControl;
import com.hwatong.platformadapter.handle.HandlerRadioControl;
import com.hwatong.platformadapter.thirdparty.ResultListener;
import com.hwatong.platformadapter.thirdparty.ThirdSpeechService;
import com.hwatong.platformadapter.utils.L;
import com.iflytek.platform.PlatformClientListener;
import com.iflytek.platform.type.PlatformCode;
import com.iflytek.platformservice.PlatformService;

public class PlatformAdapterClient implements PlatformClientListener {
	private static final String TAG = "PlatformAdapterClient";
    private static final boolean DBG = true;

    private static final int SEARCH_MUSIC = 0;
    private static final int SEARCH_RADIO = 1;

    private final Context mContext;
    private final AudioManager mAudioManager;
    private final IAudioService mAudioService;
    private int currentMicType = -1;
    private final ICanbusService mCanbusService;
    private String[] mAirConditionTips = null;

    private ServiceList mServiceList;

    private boolean mRecording;
    
    
    private String locationJsonFormat = "{'name':'%s','address':'%s','city':'%s','longitude':'%f','latitude':'%f'}";
    private String locationJson = null;
    
    
    private LocationManager locationManager;
    private String locationProvider; 
    private int acc_status = 1;
    
    
    private boolean thirdUsingMic = false;
    

    /**
     * 本应用的的application启动时会执行，应该也就是开机的时候
     * @param context
     */
    public PlatformAdapterClient(Context context) {
        L.d(TAG, "PlatformAdapterClient()");

        this.mContext = context;

		mContext.startService(new Intent(context, ThirdSpeechService.class));

        mServiceList = new ServiceList(mContext, mBtCallback);

        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mAudioService = IAudioService.Stub.asInterface(ServiceManager.getService(Context.AUDIO_SERVICE));

        mAirConditionTips = context.getResources().getStringArray(
                R.array.open_aircondition_tips);

        mCanbusService = ICanbusService.Stub.asInterface(ServiceManager.getService("canbus"));

        if (mCanbusService != null) {
            try {
                mCanbusService.addCarStatusListener(new ICarStatusListener.Stub() {
                    @Override
                    public void onReceived(CarStatus carStatus) throws RemoteException {
//                    	if(carStatus == null) {
//                    		return;
//                    	}
//                    	
//                    	int status1 = carStatus.getStatus1();
//                    	
//                    	if(status1 != 0 && status1 != 2) {
//                    		mHandler.post(new Runnable() {
//                    			@Override
//                    			public void run() {
//                    				notifySystemStateChange();
//                    			}
//                    		});
//                    	}
                    	
//                    	if(acc_status != carStatus.getStatus1()) {
//                    		mHandler.post(new Runnable() {
//                    			@Override
//                    			public void run() {
//                    				notifySystemStateChange();
//                    			}
//                    		});
//                    		acc_status = carStatus.getStatus1();
//                    	}
                    }
                });
                
                mCanbusService.addSystemStatusListener("lock", new ISystemStatusListener.Stub() {
        			@Override
        			public void onReceived(String value) throws RemoteException {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                notifySystemStateChange();
                            }
                        });
        			}
        		});
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.hwatong.voice.SPEECH_OFF");
        filter.addAction("com.hwatong.voice.SPEECH_ON");
        filter.addAction("com.hwatong.voice.SPEECH_BUTTON");
        
        // 增加获取导航提供的位置
        filter.addAction("com.shx.shxmap.TO_CTRL_ADRESS_INFO");
        
        mContext.registerReceiver(mSpeechSwitchReceiver, filter);
        
    }

	public void notifySystemStateChange() {
		L.d(TAG, "notifySystemStateChange!");
		if (PlatformService.platformCallback == null) {
			Log.e(TAG, "PlatformService.platformCallback == null");
			return;
		}

		try {
			
			// 1、打电话状态 关闭录音
            com.hwatong.btphone.IService service = mServiceList.getBtPhoneService();
            if (service != null) {
                com.hwatong.btphone.CallStatus callStatus = service.getCallStatus();
                L.d(TAG, "callStatus : " + callStatus);
                if (callStatus != null && !callStatus.status.equals(com.hwatong.btphone.CallStatus.PHONE_CALL_NONE)) {
		            if (DBG) Log.d(TAG, "!PHONE_CALL_NONE notifySystemStateChange SPEECHOFF");
	                changeDinoseMode(2);
    			    PlatformService.platformCallback.systemStateChange(PlatformCode.STATE_SPEECHOFF);
                    return;
                }
            }

            
            // 2、未点击同意状态 关闭录音
		    if (!mCanbusService.isUserConfirmed()) {
		        if (DBG) Log.d(TAG, "!isUserConfirmed notifySystemStateChange SPEECHOFF");
			    PlatformService.platformCallback.systemStateChange(PlatformCode.STATE_SPEECHOFF);
			    return;
			}
			
		    
		    
		    // 3、锁屏状态 关闭录音
		    String lockStatus = mCanbusService.getSystemStatus("lock");
		    L.d(TAG, "lockStatus : " + lockStatus);
			if (/*"locked".equals(value) ||*/ "mute_locked".equals(lockStatus)) {
		        if (DBG) Log.d(TAG, "mute_locked notifySystemStateChange SPEECHOFF");
			    PlatformService.platformCallback.systemStateChange(PlatformCode.STATE_SPEECHOFF);
			    return;
			}

			
			// 4、倒车状态 关闭录音
			CarStatus carStatus = mCanbusService.getLastCarStatus(mContext.getPackageName());
			L.d(TAG, "carStatus: " + carStatus);
            if (carStatus.getStatus1() == 0 ||  // ACC OFF
                carStatus.getStatus2() == 1 ||  // Back Gear ON
                carStatus.getStatus4() == 1) {  // RVC ON
		        if (DBG) Log.d(TAG, "back car notifySystemStateChange SPEECHOFF");
			    PlatformService.platformCallback.systemStateChange(PlatformCode.STATE_SPEECHOFF);
                return;
            }

            
            // 5、第三方使用状态 关闭录音
            if(thirdUsingMic) {
            	if (DBG) Log.d(TAG, "thirdUsingMic notifySystemStateChange SPEECHOFF");
            	PlatformService.platformCallback.systemStateChange(PlatformCode.STATE_SPEECHOFF);
            	return;
            }
            
            
            
            // 6、非打电话，已点击同意，非锁屏，非倒车，非第三方使用状态 开启录音。
			if ("unlocked".equals(lockStatus)) {
		        if (DBG) Log.d(TAG, "notifySystemStateChange SPEECHON");
				PlatformService.platformCallback.systemStateChange(PlatformCode.STATE_SPEECHON);
                changeDinoseMode(3);
			}

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    /**
     * 客户主动回调的方法
     */
    private void notifyAudioFocusChange(int focusChange) {
        L.d(TAG, "notifyAudioFocusChange(): " + focusChange);

        if (PlatformService.platformCallback == null) {
            L.d(TAG, "PlatformService.platformCallback == null");
            return;
        }
    }

    private void reportSearchPlayListResult(int type, String result) {
        if (DBG)
            L.d(TAG, "reportSearchPlayListResult(): " + type + ", " + result);

        if (PlatformService.platformCallback == null) {
            L.d(TAG, "PlatformService.platformCallback == null");
            return;
        }

        try {
            PlatformService.platformCallback.onSearchPlayListResult(type, result);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 	语音助理通知系统平台执行来电时的电话操作
		参数:
		state - 状态名称 参数说明：
		0 CALL_STATE_IDLE 挂断电话
		2 CALL_STATE_OFFHOOK接听电话
		返回:
		int 返回状态
		SUCCESS 操作成功
		FAILED 操作失败
     */
    @Override
    public int changePhoneState(int state) {
        L.d(TAG, "changePhoneState(): " + state);
        return PlatformCode.FAILED;
    }

    @Override
    public int onRequestAudioFocus(int streamType, int nDuration) {
        L.d(TAG, "onRequestAudioFocus(): streamType " + streamType
                + ", nDuration " + nDuration);
        PowerManager pm = (PowerManager) mContext
                .getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();
        L.d(TAG, "isScreenOn:" + isScreenOn);
        if (!isScreenOn) {
            PowerManager.WakeLock wakeLock = pm.newWakeLock(
                PowerManager.SCREEN_DIM_WAKE_LOCK, "TAG");
            wakeLock.acquire();
        }
        nDuration = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT;
        
        
        return mRequestAudioFocusRunnable.requestAudioFocus(streamType,
                nDuration);
    }

    private int doRequestAudioFocus(int streamType, int nDuration) {
        L.d(TAG, "doRequestAudioFocus(): streamType " + streamType
                + ", nDuration " + nDuration);

        int audioFocusResult = AudioManager.AUDIOFOCUS_REQUEST_FAILED;
        try {
            audioFocusResult = mAudioService.requestAudioFocus(streamType,
                nDuration, mICallBack, mAudioFocusDispatcher,
                new String(this.toString()), mContext.getPackageName());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        
        //add++ 
        mResultListener.syncStatusBar(true);
        

        if (audioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
        /** 获得音频焦点 通知导航 */
            mContext.sendBroadcast(new Intent("com.iflytek.startoperation"));
            mHasFocus = true;

            if (!mIsMuted) {
                mAudioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
                mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
                mIsMuted = true;
            }
        }
        return audioFocusResult;
    }

    @Override
    public void onAbandonAudioFocus() {
        L.d(TAG, "onAbandonAudioFocus()");

        mAbandonAudioFocusRunnable.abandonAudioFocus();
    }

    private void doAbandonAudioFocus() {
        L.d(TAG, "doAbandonAudioFocus()");

        if (mHasFocus) {
            mHasFocus = false;

        if (mIsMuted) {
            mAudioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            mIsMuted = false;
        }

        
        //add++ 取消同步状态栏定时器
        mResultListener.syncStatusBar(false);
        
        try {
            mAudioService.abandonAudioFocus(mAudioFocusDispatcher,
                new String(this.toString()));
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        /** 失去音频焦点 通知导航 */
        mContext.sendBroadcast(new Intent("com.iflytek.endoperation"));
        if (DBG)
            L.d(TAG, "sendBroadcast : " + "com.iflytek.endoperation");
            mHandler.removeCallbacks(mStopSpeechRecord);
            mHandler.postDelayed(mStopSpeechRecord, 1000);
        }
    }
    /*
     * private AudioManager.OnAudioFocusChangeListener mAfChangeListener = new
     * AudioManager.OnAudioFocusChangeListener() { public void
     * onAudioFocusChange(int focusChange) {
     * notifyAudioFocusChange(focusChange); } };
     */
    private final Object mLock = new Object();
    @Override
    public String onDoAction(String actionJson) {
        L.d(TAG, "onDoAction(): " + actionJson);
        JSONObject resultJson = new JSONObject();
        if (actionJson == null) {
            try {
                resultJson.put("status", "fail");
                resultJson.put("message", "抱歉，没有可处理的操作");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return resultJson.toString();
        } else {
            try {
                JSONObject action = new JSONObject(actionJson);

                if ("call".equals(action.getString("action"))) {
                    if (action.getString("param1") != null) {
                        final String number = action.getString("param1");
                        L.d(TAG, "call number = " + number);
                        synchronized (mLock) {
                            if (mServiceList.getBtPhoneService() != null) {
                                try {
                                    mServiceList.getBtPhoneService().phoneDial(number);
                                   
                                    startDialActivity();
                                    
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        resultJson.put("status", "success");
                        return resultJson.toString();
                    }
                }  else if ("startspeechrecord".equals(action.getString("action"))) {
                    if (mResultListener != null) {
                        mResultListener.onState(ThirdSpeechService.START_RECORDER);
                    }
                    synchronized (mLock) {
                        mRecording = true;
                    }

                    changeDinoseMode(1);

                    resultJson.put("status", "success");
                    return resultJson.toString();
                } else if ("stopspeechrecord".equals(action.getString("action"))) {
                    if (mResultListener != null) {
                        mResultListener.onState(ThirdSpeechService.STOP_RECORDER);
                    }
                    synchronized (mLock) {
                        mRecording = false;
                    }
                    resultJson.put("status", "success");
                    return resultJson.toString();
                } else if ("startwakerecord".equals(action.getString("action"))) {
                    changeDinoseMode(3);
                    resultJson.put("status", "success");
                    return resultJson.toString();
                } else if ("stopwakerecord".equals(action.getString("action"))) {
                    resultJson.put("status", "success");
                    return resultJson.toString();
                }
            } catch (JSONException e) {
                L.d(TAG, "Fail to do action:" + e.getMessage());
            }
        }
        try {
            resultJson.put("status", "fail");
            resultJson.put("message", "抱歉，无法处理此操作");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return resultJson.toString();
    }
    @Override
    public String onGetCarNumbersInfo() {
        L.d(TAG, "onGetCarNumbersInfo()");
        // 获取车辆信息，暂时只是一个測試數據 。
        // 语音助理 的：违章查询业务依赖此信息
        // 包含三個信息：carNumber车牌号，carCode车架号，carDriveNo发动机号
//        String carInfo = "{'carNumber':'粤YM5610','carCode':'116238','carDriveNo':'123446'}";
        String carInfo = "{'carNumber':'','carCode':'','carDriveNo':''}";
        return carInfo;
    }

    @Override
    public String onGetLocation() {
        L.d(TAG, "onGetLocation()");
        // 获取当前位置 这是只是模拟了一个位置 。实际的位置 需要客户实现
        // 语音助理 的：今天的天气、到上海的航班、附近的美食、附近的酒店，是依赖这个位置信息的
        // "{'name':'科大讯飞信息科技股份有限公司','address':'黄山路616','city':'合肥市','longitude':'117.143269','latitude':'31.834399'}";
        
        L.d(TAG,"location result : " +  locationJson);
        L.d(TAG,"location result : return null");
        
        return null;
    }

    @Override
    public int onGetState(int state) {
        L.d(TAG, "onGetState(): " + state);
        if (state == PlatformCode.STATE_BLUETOOTH_PHONE) {
        // 返回蓝牙电话状态
            synchronized (mLock) {
                try {
                    com.hwatong.btphone.IService mBtPhoneService = mServiceList.getBtPhoneService();
                    if (mBtPhoneService != null && mBtPhoneService.isHfpConnected()) {
                        L.d(TAG, "return STATE_BLUETOOTH_PHONE: " + "STATE_OK");
                        return PlatformCode.STATE_OK;
                    } else {
                        L.d(TAG, "return STATE_BLUETOOTH_PHONE: " + "STATE_NO");
                        return PlatformCode.STATE_NO;
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        } else if (state == PlatformCode.STATE_SENDSMS) {
            // 返回短信功能是否可用
            return PlatformCode.STATE_NO;
        } else {
            // 不存在此种状态请求
            return PlatformCode.FAILED;
        }
        return PlatformCode.FAILED;
    }

    @Override
    public String onNLPResult(String arg0) {
        L.d(TAG, "onNLPResult(): " + arg0);
        boolean handled = parseResult(arg0);
        JSONObject resultJson = new JSONObject();
        try {
            if (handled) {
                if (Tips.isCustomTipUse()) {
                    if (Tips.getCustomTip() != null) {
                        resultJson.put("status", "fail");
                        resultJson.put("message", Tips.getCustomTip());
                    }
                } else {
                    resultJson.put("status", "success");
                }
            } else {
                if (Tips.isCustomTipUse()) {
                    if (Tips.getCustomTip() != null) {
                        resultJson.put("status", "fail");
                        resultJson.put("message", Tips.getCustomTip());
                    }
                } else {
                    resultJson.put("status", "fail");
                    resultJson.put("message", "抱歉，无法处理此操作");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Tips.setCustomTipUse(false);
        return resultJson.toString();
    }

    @Override
    public boolean onSearchPlayList(String arg0) {
        L.d(TAG, "onSearchPlayList(): " + arg0);
        try {
            JSONObject action = new JSONObject(arg0);
            String focus = action.optString("focus");
            if ("music".equals(focus)) {
                // 不要进行耗时操作，有需要的话请使用handler。
                Message message = new Message();
                message.what = SEARCH_MUSIC;
                message.obj = arg0;
                // mHandler.sendMessage(message);
                return true;
            } else if ("radio".equals(focus)) {
                // 不要进行耗时操作，有需要的话请使用handler。
                Message message = new Message();
                message.what = SEARCH_RADIO;
                message.obj = arg0;
                // mHandler.sendMessage(message);
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    @Override
    public void onServiceUnbind() {
        L.d(TAG, "onServiceUnbind()");
        // 助理因为异常，导致和平台适配器服务断开，这里可以做重置处理
        if (mHasFocus) {
            onAbandonAudioFocus();
        }
    }

    private final IAudioFocusDispatcher mAudioFocusDispatcher = new AudioFocusDispatcher();

    private final class AudioFocusDispatcher extends IAudioFocusDispatcher.Stub implements Runnable {
        private int focusChange;
        private boolean done;

        @Override
        public void dispatchAudioFocusChange(int focusChange, String id) {
            if (Looper.myLooper() == mHandler.getLooper()) {
                notifyAudioFocusChange(focusChange);
                return;
            }

            synchronized (this) {
                done = false;

                this.focusChange = focusChange;

                if (mHandler.post(this)) {
                    while (!done) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        @Override
        public void run() {
            notifyAudioFocusChange(focusChange);
            synchronized (this) {
                done = true;
                notifyAll();
            }
        }
    }

    private final IBinder mICallBack = new Binder();

    private final RequestAudioFocusRunnable mRequestAudioFocusRunnable = new RequestAudioFocusRunnable();

    private final class RequestAudioFocusRunnable implements Runnable {
        int streamType;
        int durationHint;
        int audioFocusResult;

        boolean done;

        public int requestAudioFocus(int streamType, int durationHint) {
            if (Looper.myLooper() == mHandler.getLooper()) {
                return doRequestAudioFocus(streamType, durationHint);
            }

            synchronized (this) {
                this.streamType = streamType;
                this.durationHint = durationHint;
                this.audioFocusResult = AudioManager.AUDIOFOCUS_REQUEST_FAILED;

                done = false;

                if (mHandler.post(this)) {
                    while (!done) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return this.audioFocusResult;
            }
        }

        @Override
        public void run() {
            this.audioFocusResult = doRequestAudioFocus(streamType, durationHint);

            synchronized (this) {
                done = true;
                notifyAll();
            }
        }
    }

    private final AbandonAudioFocusRunnable mAbandonAudioFocusRunnable = new AbandonAudioFocusRunnable();

    private final class AbandonAudioFocusRunnable implements Runnable {
        boolean done;
        public void abandonAudioFocus() {
            if (Looper.myLooper() == mHandler.getLooper()) {
                doAbandonAudioFocus();
                return;
            }
            synchronized (this) {
                done = false;
                if (mHandler.post(this)) {
                    while (!done) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        @Override
        public void run() {
            doAbandonAudioFocus();
            synchronized (this) {
                done = true;
                notifyAll();
            }
        }
    }

    private final DoActionRunnable mStopSpeechRecord = new DoActionRunnable(
            "stopspeechrecord");

    private final class DoActionRunnable implements Runnable {
        final String action;

        public DoActionRunnable(String action) {
            this.action = action;
        }
        @Override
        public void run() {
        }
    }

    private final BroadcastReceiver mSpeechSwitchReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            L.d(TAG, "onReceive(): " + action);

			if ("com.hwatong.voice.SPEECH_OFF".equals(action)) {
				
				notifySystemStateChange();
				
			} else if ("com.hwatong.voice.SPEECH_ON".equals(action)) {
				
				notifySystemStateChange();

            } else if ("com.hwatong.voice.SPEECH_BUTTON".equals(action)) {
                mHandler.removeCallbacks(mStopSpeechRecord);
                Intent launchIntent = new Intent();
                launchIntent.setComponent(new ComponentName(
                    "com.iflytek.cutefly.speechclient",
                    "com.iflytek.autofly.SpeechClientService"));
                launchIntent.putExtra("fromservice", "CanbusService");
                mContext.startService(launchIntent);

            } else if("com.shx.shxmap.TO_CTRL_ADRESS_INFO".equals(action)) {
            	
            	double latitude = 0;
            	double longitude = 0;
            	
            	try {
                	if(mCanbusService != null) {
                		GpsStatus lastGpsStatus = mCanbusService.getLastGpsStatus(mContext.getPackageName());
                		L.d(TAG, "lastGpsStatus : " + lastGpsStatus);
                		if(lastGpsStatus != null) {
                			latitude = lastGpsStatus.getLatitude();
                			longitude = lastGpsStatus.getLongitude();
                			Log.d("Voice_roll", "Client Gps latitude : " + latitude + " longitude : " + longitude);
                		}
                	} 
        		} catch (RemoteException e) {
        			e.printStackTrace();
        		}
            	
            	
//            	 Intent intent2 = new Intent("com.shx.shxmap.TO_CTRL_ADRESS_INFO");
//            	 intent2.putExtra("adminname", stradminname);
//            	 intent2.putExtra("roadname", roadname);
//            	 intent2.putExtra("lon", dblon);
//            	 intent2.putExtra("lat", dblat);
            	
            	String adminName = intent.getStringExtra("adminname");
            	String roadName = intent.getStringExtra("roadname");
            	
            	double lat = intent.getDoubleExtra("lat", 0);
            	double lon = intent.getDoubleExtra("lon", 0);
            	
            	Log.d("Voice_roll", "Navi latitude : " + lat + " longitude : " + lon + " admin : " + adminName + " road : " + roadName);
            	
            	//地图传过来有则以地图优先
            	latitude = lat == 0 ? latitude : lat;
            	longitude = lon == 0 ? longitude : lon;
            	
            	
            	locationJson = String.format(locationJsonFormat, roadName, roadName, adminName, longitude, latitude);
            	
            }
        }
    };

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case SEARCH_MUSIC:
                String paramsStr = (String) msg.obj;
                String jsonResult = onGetMusics(paramsStr);
                reportSearchPlayListResult(SEARCH_MUSIC, jsonResult);
                break;
            case SEARCH_RADIO:
                break;
            }
        }
    };

    private boolean mHasFocus = false;
    private boolean mIsMuted;
    /**
     * 语义解析处理 
     * @param arg0
     * @return
     */
    private boolean parseResult(final String arg0) {
    	
    	L.d(TAG, "zhangjinbo: " + arg0);
    	
        int acc = -1;
        if (mCanbusService != null) {
            try {
                acc = mCanbusService.getLastCarStatus(mContext.getPackageName()).getStatus1();
                L.d(TAG, "acc = " + acc);
            } catch (RemoteException e) {
                L.d(TAG, "RemoteException: " + e.toString());
            } catch (Exception e) {
            	L.d(TAG, "Exception: " + e.toString());
            }
        }
        L.d(TAG, "xiaoma:"+ThirdSpeechService.state);
        try {
            JSONObject resultJson = new JSONObject(arg0);
            final String focus = resultJson.getString("focus");
            
            /**
             * 小马注册后 优先处理
             */
            String response ;
            if (mResultListener != null && ThirdSpeechService.state) {
                response = mResultListener.onResult(arg0);
                if(response!=null ){
                    if(response.length()>0){                   
                        Tips.setCustomTipUse(true);
                        Tips.setCustomTip(response);
                        return false ;
                    } else {
                        return true ;
                    }
                    
                }
            }
            
            if ("music".equals(focus)) {
                return HandleMusicControl.getInstance(mContext, mCanbusService,mServiceList).handleMusicScence(resultJson);
                /**
                if(response==null){
                    return HandleMusicControl.getInstance(mContext, mCanbusService,mServiceList).handleMusicScence(resultJson);
                }*/
            } else if ("radio".equals(focus)) {
                return HandlerRadioControl.getInstance(mContext, mCanbusService).handleRadioScence(resultJson);
                /**
                if(response==null){
                    return HandlerRadioControl.getInstance(mContext, mCanbusService).handleRadioScence(resultJson);
                }*/
            } else if ("cmd".equals(focus)) {
                return HandleCmdControl.getInstance(mContext, mCanbusService,mAudioManager, mServiceList).handleCmdScence(resultJson);
                /**
                if(response==null){  
                    return HandleCmdControl.getInstance(mContext, mCanbusService,mAudioManager, mServiceList).handleCmdScence(resultJson);
                }*/
            } else if ("app".equals(focus)) {
                /**
                 * 过滤空调语义
                 */
                for (int i = 0; i < mAirConditionTips.length; i++) {
                    if (resultJson.getString("rawText").contains(mAirConditionTips[i])) {
                        if (acc != 2) {
                            accNotOn();
                            Tips.setCustomTipUse(true);
                            Tips.setCustomTip("请在点火开关位于打开时使用");
                            return false;
                        }
                        boolean flag = HandleAirControl.getInstance(mContext, mCanbusService).handleAppIsAirControlScence(resultJson, mAirConditionTips[i]);
                        Tips.setCustomTipUse(true);
                        if (flag) {
                            Tips.setCustomTip("操作成功");
                        } else {
                            Tips.setCustomTip("操作失败");
                        }
                        return flag;
                    }
                }
                /**
                if (mResultListener != null && ThirdSpeechService.state) {
                    String response = mResultListener.onResult(arg0);
                    if(response!=null){
                        Tips.setCustomTipUse(true);
                        Tips.setCustomTip(response);
                        return false ;
                    } else {
                        return true ;
                    }
                }*/
                return HandleAppControl.getInstance(mContext, mCanbusService,mServiceList).handleAppScence(resultJson);
            } else if ("carControl".equals(focus)) {
                boolean flag = HandleCarControl.getInstance(mContext, mCanbusService).handleCarControlScence(resultJson);
                Tips.setCustomTipUse(true);
                if (flag) {
                    Tips.setCustomTip("操作成功");
                } else {
                    Tips.setCustomTip("操作失败");
                }
                return flag;
            } else if ("airControl".equals(focus)) {
                if (acc != 2) {
                    accNotOn();
                    Tips.setCustomTipUse(true);
                    Tips.setCustomTip("请在点火开关位于打开时使用");
                    return false;
                }
                boolean flag = HandleAirControl.getInstance(mContext, mCanbusService).handleAirControlScence(resultJson);
                if (flag && HandleAirControl.isOpenAirControlView) {
                    Intent intent = new Intent();
                    intent.setClassName("com.hwatong.aircondition","com.hwatong.aircondition.MainActivity");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                    HandleAirControl.isOpenAirControlView = false;
                }
                return flag;
            } else if ("telephone".equals(focus)) {
                boolean flag = HandlerBtPhoneControl.getInstance(mContext,mCanbusService, mServiceList).handleBtPhoneSence(resultJson);
                return flag;
            }/**
            if (mResultListener != null && ThirdSpeechService.state) {
                String response = mResultListener.onResult(arg0);
                if(response!=null){
                    Tips.setCustomTipUse(true);
                    Tips.setCustomTip(response);
                    return false ;
                }   else {
                    return true ;
                }
            }*/
        } catch (JSONException e) {
            L.d(TAG, "Fail to parserResult:" + e.getMessage());
        }
        return false;
    }

    private void changeDinoseMode(int type) {
        // 若客户使用了 讯飞的降噪模块 这里来切换 降噪模块的不同工作模式
        // 1: 降噪模式 2: 通话模式 3: 唤醒模式
        if (currentMicType == -1 || currentMicType != type) {
        	L.d(TAG, "changeDinoseMode type " + type);
        	
            try {
                FileOutputStream os = new FileOutputStream(
                    "/sys/devices/platform/imx-i2c.0/i2c-0/0-0047/mode_func");
                try {
                    os.write(Integer.toString(type, 10).getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            currentMicType = type;
        }
    }

    /*
     * {"song":"忘情水"}<br/> {"artist":"刘德华"}<br/> {"category":"摇滚歌曲"}<br/>
     * {"source":"网络"}<br/>
     */
    public String onGetMusics(String params) {
        try {
            JSONObject action = new JSONObject(params);
            return "{\"focus\":\"music\",\"status\":\"success\",\"result\":[{\"song\":\"忘情水\",\"artist\":\"刘德华\"},"
                + "{\"song\":\"恭喜发财\",\"artist\":\"刘德华\"},"
                + "{\"song\":\"billie jean\",\"artist\":\"迈克杰克逊\",\"category\":\"摇滚\"},"
                + "{\"song\":\"beat it\",\"artist\":\"迈克杰克逊\",\"category\":\"摇滚\"},"
                + "{\"song\":\"we are the world\",\"artist\":\"迈克杰克逊\",\"category\":\"摇滚\"},"
                + "{\"song\":\"beat it\",\"artist\":\"迈克杰克逊\",\"category\":\"摇滚\"},"
                + "{\"song\":\"双节棍\",\"artist\":\"周杰伦\",\"album\":\"Jay\"},"
                + "{\"song\":\"青花瓷\",\"artist\":\"周杰伦\"},"
                + "{\"song\":\"斗牛\",\"artist\":\"周杰伦\"},"
                + "{\"song\":\"七里香\",\"artist\":\"周杰伦\"}" + "]}";

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "{\"focus\":\"music\",\"status\":\"fail\",\"message\":\"获取音乐失败\"}";
    }
    /**
     * acc提示弹框
     */
    private void accNotOn() {
        Intent intent = new Intent("com.hwatong.backservice.BackService");
        intent.putExtra("cmd", "alarm");
        intent.putExtra("type", "noacc");
        intent.putExtra("run", "true");
        mContext.startService(intent);
    }

    private ResultListener mResultListener = null;

    public void setResultListener(ResultListener listener) {
        mResultListener = listener;
    }

	private void onCallStatusChanged() {
		if (DBG) Log.d(TAG, "onCallStatusChanged()");

        com.hwatong.btphone.IService btPhoneService = mServiceList.getBtPhoneService();

        if (btPhoneService != null) {
            try {
                com.hwatong.btphone.CallStatus callStatus = btPhoneService.getCallStatus();
                if (callStatus != null && !com.hwatong.btphone.CallStatus.PHONE_CALL_NONE.equals(callStatus.status)) {
                    mBtHandler.removeMessages(MSG_CALL_HANGUP);

				    notifySystemStateChange();
                } else {
                    mBtHandler.removeMessages(MSG_CALL_HANGUP);
                    mBtHandler.sendEmptyMessageDelayed(MSG_CALL_HANGUP, 1000);
                }
            } catch (RemoteException e) {
            	e.printStackTrace();
            }
        }
	}
	
    private final static int MSG_HFP_CONNECT_CHANGED = 1;
    private final static int MSG_CALL_STATUS_CHANGED = 2;    
    private final static int MSG_CALL_HANGUP = 3;    

    private final Handler mBtHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

            case MSG_HFP_CONNECT_CHANGED:
                break;

            case MSG_CALL_STATUS_CHANGED:
				onCallStatusChanged();
                break;

            case MSG_CALL_HANGUP:
		        notifySystemStateChange();
                break;
            }
        }
    };

    private final com.hwatong.btphone.ICallback mBtCallback = new com.hwatong.btphone.ICallback.Stub() {
        @Override
        public void onHfpConnected() {
            mBtHandler.removeMessages(MSG_HFP_CONNECT_CHANGED);
            mBtHandler.sendEmptyMessageDelayed(MSG_HFP_CONNECT_CHANGED, 200);
        }

        @Override
        public void onHfpDisconnected() {
            mBtHandler.removeMessages(MSG_HFP_CONNECT_CHANGED);
            mBtHandler.sendEmptyMessage(MSG_HFP_CONNECT_CHANGED);
        }

        @Override
        public void onCallStatusChanged() {
            mBtHandler.removeMessages(MSG_CALL_STATUS_CHANGED);
            mBtHandler.sendEmptyMessage(MSG_CALL_STATUS_CHANGED);
        }

        @Override
        public void onRingStart() throws RemoteException {
        }

        @Override
        public void onRingStop() throws RemoteException {
        }

        @Override
        public void onHfpLocal() throws RemoteException {
        }

        @Override
        public void onHfpRemote() throws RemoteException {
        }

        @Override
        public void onPhoneBook(String type, String name, String number) {
        }

        @Override
        public void onCalllog(String type, String name, String number,
                String date) throws RemoteException {
        }

        @Override
        public void onContactsChange() {
        }

        @Override
        public void onCalllogChange(String type) {
        }

        @Override
        public void onAllDownloadDone(int arg0) throws RemoteException {

        }

        @Override
        public void onCalllogDone(String arg0, int arg1) throws RemoteException {

        }

        @Override
        public void onPhoneBookDone(int arg0) throws RemoteException {

        }

        @Override
        public void onSignalBattery() throws RemoteException {

        }
    };
    
    
    
    //语音拨打电话需要直接跳转到拨号界面，这么多判断是因为当dial在前台不要finish，当dial在后台需要finish，当dial在通讯录/通话记录之后需要跳回通讯录/通话记录
    private void startDialActivity() {
    	Intent intent = new Intent();
		intent.setClassName("com.hwatong.btphone.ui", "com.hwatong.btphone.activity.DialActivity");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		
		boolean fromVoice = true;
		boolean voiceFromOutside = false;
		int from = 0;
		
		ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> cn = am.getRunningTasks(5);
		if(cn == null) {
			return;
		}
		
		for(int i = 0; i < cn.size(); i++) {
			RunningTaskInfo runningTaskInfo = cn.get(i);
			if(runningTaskInfo == null) {
				continue;
			}
			
			ComponentName topActivity = runningTaskInfo.topActivity;
			ComponentName baseActivity = runningTaskInfo.baseActivity;
			int numActivities = runningTaskInfo.numActivities;
			
			if(topActivity == null) {
				continue;
			}

			String topName = topActivity.getClassName();
			String baseName = baseActivity.getClassName();
			
			L.d(TAG, "topActivity : " + topName + " numActivities : " + numActivities + " baseActivity: " + baseName);
			
			
			//------------以下判断蓝牙电话应用是不是在前台---------------------
			
			//过滤掉讯飞前台activity
			if(topName.contains("com.iflytek.autofly.activity")) {
				continue;
			
			// 如果前台是拨号界面,再跳一次，不然服务跳会把fromoutside置为true
			} else if("com.hwatong.btphone.activity.DialActivity".equals(topName)) {
				L.d(TAG, "back top!");
				fromVoice = false;
				break;
				
				
			//如果前台是通讯录，分情况，当有3个activity表示通讯录是从通话界面跳过去的，当有2个activity且第一个是通话界面，表示通讯录也是从通话界面跳过去的
			} else if("com.hwatong.btphone.activity.ContactsListActivity".equals(topName)) {
				
				if(numActivities == 3 || ("com.hwatong.btphone.activity.DialActivity".equals(baseName) &&  numActivities == 2)) {
					from = 2;
					L.d(TAG, "back top behind contacts!");
				} 
				break;
				
			//如果前台是通话记录，分情况，当有3个activity表示通话记录是从通话界面跳过去的，当有2个activity且第一个是通话界面，表示通话记录也是从通话界面跳过去的
			} else if("com.hwatong.btphone.activity.CallLogActivity".equals(topName)) {
				
				if(numActivities == 3 || ("com.hwatong.btphone.activity.DialActivity".equals(baseName) &&  numActivities == 2)) {
					from = 1;
					L.d(TAG, "back top behind calllog!");
				} 
				break;
			
			
			//如果前台是蓝牙电话界面，直接判定dial不在前台
			} else if("com.hwatong.btphone.activity.PhoneActivity".equals(topName)) {
				break;
				
				
			//到这里表示非蓝牙电话应用界面，表示从应用外部跳转
			} else {
				voiceFromOutside = true;
				break;
			}
		}
		
		intent.putExtra("from_voice", fromVoice);
		intent.putExtra("voice_from_out_side", voiceFromOutside);
		intent.putExtra("from", from);
		
		try {
			mContext.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			L.d(TAG, "ActivityNotFoundException : " + e.getMessage());
		}
    }
    
    
    public void doSwitchSpeechMic(int state) {
    	L.d(TAG, "doSwitchSpeechMic third switch mic for voice : " + state);
    	
    	thirdUsingMic = (state==0);
    	
    	if(thirdUsingMic) {
    		notifySystemStateChange();
    	} else {
    		new Thread(new Runnable() {
				@Override
				public void run() {
					SystemClock.sleep(2000);
					notifySystemStateChange();
				}
			}).start();
    	}
    }
}
