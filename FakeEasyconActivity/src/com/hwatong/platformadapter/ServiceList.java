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
import android.os.SystemClock;

import com.hwatong.bt.IService;

import com.hwatong.platformadapter.utils.L;

/**
 * 服务类
 * @author caochao
 */
public class ServiceList {
    private static final String thiz = ServiceList.class.getSimpleName(); 
    
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

    private com.hwatong.btphone.ICallback mBtCallback;
    
    /**
     * 构造器生成服务
     * @param mContext
     */
    public ServiceList(Context context, com.hwatong.btphone.ICallback callback) {
        this.mContext = context;
        mBtCallback = callback;

        if (mContext != null) {
            mContext.bindService(new Intent("com.hwatong.btphone.service"), mBtPhoneConnect, Context.BIND_AUTO_CREATE);
            mContext.bindService(new Intent("com.hwatong.bt.service"), mBtConnect, Context.BIND_AUTO_CREATE);
            mContext.bindService(new Intent("com.hwatong.media.MediaScannerService"), mMediaConnection, Context.BIND_AUTO_CREATE);
            mContext.bindService(new Intent("com.hwatong.ipod.service"), mIPodServiceConnection, Context.BIND_AUTO_CREATE);
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
    
    private ServiceConnection mBtPhoneConnect = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            L.d(thiz, "BtPhoneService onServiceConnected !");
            btPhoneService = com.hwatong.btphone.IService.Stub.asInterface(service);

            if (btPhoneService != null && mBtCallback != null) {
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

    private ServiceConnection mMediaConnection = new ServiceConnection() {
        
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            
        }
        
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder service) {
            mediaService = com.hwatong.media.IService.Stub.asInterface(service);
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

}
