package com.hwatong.platformadapter;

import java.io.FileOutputStream;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import android.canbus.CarStatus;
import android.canbus.ICanbusService;
import android.canbus.ICarStatusListener;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.util.Log;
import com.hwatong.btphone.CallStatus;
import com.hwatong.platformadapter.handle.HandleAirControl;
import com.hwatong.platformadapter.handle.HandleAppControl;
import com.hwatong.platformadapter.handle.HandleCarControl;
import com.hwatong.platformadapter.handle.HandleCmdControl;
import com.hwatong.platformadapter.handle.HandleMusicControl;
import com.hwatong.platformadapter.handle.HandlerBtPhoneControl;
import com.hwatong.platformadapter.handle.HandlerRadioControl;
import com.hwatong.platformadapter.thirdparty.ResultListener;
import com.hwatong.platformadapter.thirdparty.ThirdSpeechService;
import com.iflytek.platform.PlatformClientListener;
import com.iflytek.platform.type.PlatformCode;
import com.iflytek.platformservice.PlatformService;

public class PlatformAdapterClient implements PlatformClientListener {
    private static final String TAG = "Voice";
    private static final boolean DBG = true;
    private final Context mContext;
    private final AudioManager mAudioManager;
    private final IAudioService mAudioService;
    private int currentMicType = -1;
    private final ICanbusService mCanbusService;
    private String[] mAirConditionTips = null;
    public static final int SEARCH_MUSIC = 0;
    public static final int SEARCH_RADIO = 1;

    private ServiceList mServiceList;

    private boolean mRecording;

    public PlatformAdapterClient(Context context) {
        Log.d(TAG, "PlatformAdapterClient()");

        this.mContext = context;
        mServiceList = new ServiceList(mContext);
        mAudioManager = (AudioManager) mContext
                .getSystemService(Context.AUDIO_SERVICE);
        mAudioService = IAudioService.Stub.asInterface(ServiceManager
                .getService(Context.AUDIO_SERVICE));
        mAirConditionTips = context.getResources().getStringArray(
                R.array.open_aircondition_tips);
        mCanbusService = ICanbusService.Stub.asInterface(ServiceManager
                .getService("canbus"));
        if (mCanbusService != null) {
        try {
        mCanbusService.addCarStatusListener(new ICarStatusListener.Stub() {
            @Override
            public void onReceived(CarStatus carStatus) throws RemoteException {
                com.hwatong.btphone.IService service = mServiceList
                        .getBtPhoneService();
                boolean isCall = false;
                if (service != null) {
                    CallStatus callStatus = service.getCallStatus();
                    if (callStatus != null
                        && !callStatus.status.equals(CallStatus.PHONE_CALL_NONE)) {
                        isCall = true;
                    }
                }
                if (carStatus.getStatus1() != 0 && carStatus.getStatus2() != 1
                        && mCanbusService.isUserConfirmed() && !isCall && carStatus.getStatus4()!=1) {
                    Log.d(TAG, "voice speech on when client init!!");
                    PlatformService.platformCallback.systemStateChange(PlatformCode.STATE_SPEECHON);
                } else {
                    Log.d(TAG, "voice speech off when client init!");
                    PlatformService.platformCallback.systemStateChange(PlatformCode.STATE_SPEECHOFF);
                }
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
        filter.addAction("com.hwatong.phone.PHONE_STATUS");
        filter.addAction("com.hwatong.system.USER_CONFIRMED");
        filter.addAction("com.hwatong.system.LOCK");
        filter.addAction("com.hwatong.system.UNLOCK");
        mContext.registerReceiver(mSpeechSwitchReceiver, filter);
    }

    /**
     * 客户主动回调的方法
     */
    private void notifyAudioFocusChange(int focusChange) {
        Log.d(TAG, "notifyAudioFocusChange(): " + focusChange);

        if (PlatformService.platformCallback == null) {
            Log.e(TAG, "PlatformService.platformCallback == null");
            return;
        }
    }

    private void reportSearchPlayListResult(int type, String result) {
        if (DBG)
            Log.d(TAG, "reportSearchPlayListResult(): " + type + ", " + result);

        if (PlatformService.platformCallback == null) {
            Log.e(TAG, "PlatformService.platformCallback == null");
            return;
        }

        try {
            PlatformService.platformCallback.onSearchPlayListResult(type, result);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int changePhoneState(int state) {
        Log.d(TAG, "changePhoneState(): " + state);
        return PlatformCode.FAILED;
    }

    @Override
    public int onRequestAudioFocus(int streamType, int nDuration) {
        Log.d(TAG, "onRequestAudioFocus(): streamType " + streamType
                + ", nDuration " + nDuration);
        PowerManager pm = (PowerManager) mContext
                .getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();
        Log.i(TAG, "isScreenOn:" + isScreenOn);
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
        Log.d(TAG, "doRequestAudioFocus(): streamType " + streamType
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
        Log.d(TAG, "onAbandonAudioFocus()");

        mAbandonAudioFocusRunnable.abandonAudioFocus();
    }

    private void doAbandonAudioFocus() {
        Log.d(TAG, "doAbandonAudioFocus()");

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
            Log.d(TAG, "sendBroadcast : " + "com.iflytek.endoperation");
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
        Log.d(TAG, "onDoAction(): " + actionJson);
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
                        Log.d(TAG, "call number = " + number);
                        synchronized (mLock) {
                            if (mServiceList.getBtPhoneService() != null) {
                                try {
                                    mServiceList.getBtPhoneService().phoneDial(number);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        resultJson.put("status", "success");
                        return resultJson.toString();
                    }
                }  else if ("startspeechrecord".equals(action.getString("action"))) {
                    if(mResultListener!=null){
                        mResultListener.onState(ThirdSpeechService.START_RECORDER);
                    }
                    synchronized (mLock) {
                        mRecording = true;
                    }
                    changeDinoseMode(0);
                    resultJson.put("status", "success");
                    return resultJson.toString();
                } else if ("stopspeechrecord".equals(action.getString("action"))) {
                    if(mResultListener!=null){
                        mResultListener.onState(ThirdSpeechService.STOP_RECORDER);
                    }
                    synchronized (mLock) {
                        mRecording = false;
                    }
                    resultJson.put("status", "success");
                    return resultJson.toString();
                } else if ("startwakerecord".equals(action.getString("action"))) {
                    changeDinoseMode(1);
                    resultJson.put("status", "success");
                    try {
                        if(!mCanbusService.isUserConfirmed()){
                            Log.d(TAG, "isUserConfirmed = false ,speech off");
                            PlatformService.platformCallback.systemStateChange(PlatformCode.STATE_SPEECHOFF);
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }                                        
                    return resultJson.toString();
                } else if ("stopwakerecord".equals(action.getString("action"))) {
                    resultJson.put("status", "success");
                    return resultJson.toString();
                }
            } catch (JSONException e) {
                Log.e(TAG, "Fail to do action:" + e.getMessage());
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
        Log.d(TAG, "onGetCarNumbersInfo()");
        // 获取车辆信息，暂时只是一个測試數據 。
        // 语音助理 的：违章查询业务依赖此信息
        // 包含三個信息：carNumber车牌号，carCode车架号，carDriveNo发动机号
        String carInfo = "{'carNumber':'粤YM5610','carCode':'116238','carDriveNo':'123446'}";
        return carInfo;
    }

    @Override
    public String onGetLocation() {
        Log.d(TAG, "onGetLocation()");
        // 获取当前位置 这是只是模拟了一个位置 。实际的位置 需要客户实现
        // 语音助理 的：今天的天气、到上海的航班、附近的美食、附近的酒店，是依赖这个位置信息的
        String location = "{'name':'科大讯飞信息科技股份有限公司','address':'黄山路616','city':'合肥市','longitude':'117.143269','latitude':'31.834399'}";
        return null;
    }

    @Override
    public int onGetState(int state) {
        Log.d(TAG, "onGetState(): " + state);
        if (state == PlatformCode.STATE_BLUETOOTH_PHONE) {
        // 返回蓝牙电话状态
            synchronized (mLock) {
                try {
                    com.hwatong.btphone.IService mBtPhoneService = mServiceList.getBtPhoneService();
                    if (mBtPhoneService != null && mBtPhoneService.isHfpConnected()) {
                        Log.d(TAG, "return STATE_BLUETOOTH_PHONE: " + "STATE_OK");
                        return PlatformCode.STATE_OK;
                    } else {
                        Log.d(TAG, "return STATE_BLUETOOTH_PHONE: " + "STATE_NO");
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
        Log.d(TAG, "onNLPResult(): " + arg0);
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
        Log.d(TAG, "onSearchPlayList(): " + arg0);
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
        Log.d(TAG, "onServiceUnbind()");
        // 助理因为异常，导致和平台适配器服务断开，这里可以做重置处理
        if (mHasFocus) {
            onAbandonAudioFocus();
        }
    }
    private final IAudioFocusDispatcher mAudioFocusDispatcher = new AudioFocusDispatcher();
    private final class AudioFocusDispatcher extends IAudioFocusDispatcher.Stub
            implements Runnable {
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
                return requestAudioFocus(streamType, durationHint);
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
            this.audioFocusResult = doRequestAudioFocus(streamType,
                    durationHint);
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

    private BroadcastReceiver mSpeechSwitchReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive(): " + action);
            if ("com.hwatong.voice.SPEECH_BUTTON".equals(action)) {
                mHandler.removeCallbacks(mStopSpeechRecord);
                Intent launchIntent = new Intent();
                launchIntent.setComponent(new ComponentName(
                    "com.iflytek.cutefly.speechclient",
                    "com.iflytek.autofly.SpeechClientService"));
                launchIntent.putExtra("fromservice", "CanbusService");
                mContext.startService(launchIntent);
            }
            if ("com.hwatong.phone.PHONE_STATUS".equals(action)) {
                String status = intent.getStringExtra("status");
                if ("ring".equals(status)) {
                    try {
                        PlatformService.platformCallback.systemStateChange(PlatformCode.STATE_SPEECHOFF);
                        Log.d(TAG,"PHONE_STATUS voice speech off success");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                if ("release".equals(status)) {
                    try {
                        PlatformService.platformCallback.systemStateChange(PlatformCode.STATE_SPEECHON);
                        setMode(3);
                        Log.d(TAG,"PHONE_STATUS voice speech on success");
                    } catch (RemoteException e) {
                        Log.d(TAG,"PHONE_STATUS voice speech on failed");
                        e.printStackTrace();
                    }
                }
            }
            if ("com.hwatong.system.USER_CONFIRMED".equals(action)) {
                try {
                    PlatformService.platformCallback.systemStateChange(PlatformCode.STATE_SPEECHON);
                    Log.d(TAG,"USER_CONFIRMED voice speech on ");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            if ("com.hwatong.system.LOCK".equals(action)) {
                try {
                    PlatformService.platformCallback.systemStateChange(PlatformCode.STATE_SPEECHOFF);
                    Log.d(TAG, "LOCK voice speech on ");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            if ("com.hwatong.system.UNLOCK".equals(action)) {
                try {
                    PlatformService.platformCallback.systemStateChange(PlatformCode.STATE_SPEECHON);
                    Log.d("VoiceSpeechSwitchReceiver", "UNLOCK voice speech on ");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
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
    	
    	Log.d(TAG, "zhangjinbo: " + arg0);
    	
        int acc = -1;
        if (mCanbusService != null) {
            try {
                acc = mCanbusService.getLastCarStatus(mContext.getPackageName()).getStatus1();
            } catch (RemoteException e) {
                e.printStackTrace();
            }    
        }
        Log.d(TAG, "xiaoma:"+ThirdSpeechService.state);
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
            Log.e(TAG, "Fail to parserResult:" + e.getMessage());
        }
        return false;
    }

    private void changeDinoseMode(int type) {
        // 若客户使用了 讯飞的降噪模块 这里来切换 降噪模块的不同工作模式
        if (type == 0) {
            // 降噪模式
            if (currentMicType != 0) {
                setMode(1);
            }
            currentMicType = 0;
       } else {
            // 唤醒模式
            if (currentMicType != 1) {
                setMode(3);
            }
            currentMicType = 1;
        }
    }
    private void setMode(int mode) {
    	
    	Log.d(TAG, "setMode  mode : === " + mode);
    	
        try {
            FileOutputStream os = new FileOutputStream(
                "/sys/devices/platform/imx-i2c.0/i2c-0/0-0047/mode_func");
            try {
                os.write(Integer.toString(mode, 10).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
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

}
