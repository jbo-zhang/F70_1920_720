package com.hwatong.platformadapter;

import java.io.FileOutputStream;
import java.io.IOException;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.hwatong.bt.IService;
import com.hwatong.btphone.CallStatus;
import com.iflytek.platform.type.PlatformCode;
import com.iflytek.platformservice.PlatformService;

/**
 * 服务类
 * @author caochao
 */
public class ServiceList {
    private static final String TAG = "Voice"; 
    
    private Context mContext ;
    /**
     * 蓝牙
     */
    private com.hwatong.bt.IService btService ;
    /**
     * 蓝牙电话
     */
    private com.hwatong.btphone.IService btPhoneService;
    /**
     * 媒体
     */
    private com.hwatong.media.IService mediaService ;
    /**
     * iPod
     */
    private com.hwatong.ipod.IService IPodService;
    /**
     * Radio
     */
    private com.hwatong.radio.IService radioService;

    
    
    /**
     * 构造器生成服务
     * @param mContext
     */
    public ServiceList(Context context) {
        this.mContext = context;
        if(mContext != null){
            mContext.bindService(new Intent("com.hwatong.btphone.service"), mBtPhoneConnect, Context.BIND_AUTO_CREATE);
            mContext.bindService(new Intent("com.hwatong.bt.service"), mBtConnect, Context.BIND_AUTO_CREATE);
            mContext.bindService(new Intent("com.hwatong.media.MediaScannerService"), mMediaConnection, Context.BIND_AUTO_CREATE);
        }
    }
    
    /**
     * 预留接口
     */
    public void unBindService(){
    }
    
    public com.hwatong.bt.IService getBtService() {
        return btService;
    }

    public com.hwatong.btphone.IService getBtPhoneService() {
        return btPhoneService;
    }

    public com.hwatong.media.IService getMediaService() {
        return mediaService;
    }

    public com.hwatong.ipod.IService getIPodService() {
        
        return IPodService;
    }

    public com.hwatong.radio.IService getRadioService() {
        return radioService;
    }
    private ServiceConnection mMediaConnection = new ServiceConnection() {
        
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            
        }
        
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder service) {
            mediaService = com.hwatong.media.IService.Stub.asInterface(service);
        }
    };
    private ServiceConnection mRadioServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            radioService = com.hwatong.radio.IService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            radioService = null ;
        }
    };
    private ServiceConnection mIPodServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IPodService = com.hwatong.ipod.IService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            IPodService = null;
        }
    };
    
    private ServiceConnection mBtPhoneConnect = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "BtPhoneService onServiceConnected !");
            btPhoneService = com.hwatong.btphone.IService.Stub.asInterface(service);
            if(btPhoneService!=null){
                try {
                    btPhoneService.registerCallback(mBtCallback);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            btPhoneService = null;
        }
    };    
    
    private ServiceConnection mBtConnect = new ServiceConnection(){
        
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            btService = IService.Stub.asInterface(binder);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            
        }
    };
    private final static int MSG_HFP_CONNECT_CHANGED = 1;
    private final static int MSG_CALL_STATUS_CHANGED = 2;    
    private final Handler mBtHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

            case MSG_HFP_CONNECT_CHANGED:
                break;

            case MSG_CALL_STATUS_CHANGED:
                if (btPhoneService != null) {
                try {
                CallStatus callStatus = btPhoneService.getCallStatus();
                if ((callStatus != null)
                        && (!CallStatus.PHONE_CALL_NONE
                                .equals(callStatus.status))) {
                setMode(2);
                Log.d("VoiceSpeechSwitchReceiver",
                        " MSG_CALL_STATUS_CHANGED voice speech off success");
                PlatformService.platformCallback
                        .systemStateChange(PlatformCode.STATE_SPEECHOFF);
                } else {
                setMode(3);
                PlatformService.platformCallback
                        .systemStateChange(PlatformCode.STATE_SPEECHON);
                Log.d("VoiceSpeechSwitchReceiver",
                        " MSG_CALL_STATUS_CHANGED voice speech on success");
                }
                } catch (RemoteException e) {
                e.printStackTrace();
                }
                }
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
    private void setMode(int mode) {
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
}
