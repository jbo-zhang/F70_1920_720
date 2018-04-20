package com.hwatong.platformadapter.thirdparty;

import java.util.ArrayList;
import com.hwatong.platformadapter.PlatformAdapterApp;
import com.iflytek.platform.type.PlatformCode;
import com.iflytek.platformservice.PlatformService;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
/**
 * @date 2017-11-22
 * @author caochao
 */
public class ThirdSpeechService extends Service implements ResultListener{
    
    private static final String TAG = "Voice";
    
	public static boolean state = false ;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG , "ThirdSpeechService is started!");
        PlatformAdapterApp.getPlatformClientInstance().setResultListener(this);
	    return super.onStartCommand(intent, flags, startId);
    }
    private IService.Stub iservice = new IService.Stub() {
		@Override
		public void registCallBack(CallBack callBack) throws RemoteException {
			if(callBack == null){
				Log.d(TAG, "third callback is null");
				return ;
			}
			synchronized(callbacks){				
				for(int i =0 ; i<callbacks.size(); i++){
					if(callBack.asBinder().equals(callbacks.get(i).mCallback.asBinder())){
					    Log.d(TAG, "this callback had registed !");
					    //state = true ;
						return ;
					}
				}
				try {
					CallBackListener l = new CallBackListener(callBack);
					callBack.asBinder().linkToDeath(l, 0);
					callbacks.add(new CallBackListener(callBack));
					state = true ;
					Log.d(TAG, "a new client regist");					
				} catch (Exception e) {
					e.printStackTrace();
					Log.d(TAG, "a new client regist error:"+e.toString()+";"+e.getMessage());					
				}
			}
		}
		@Override
		public void unregistCallBack(CallBack callBack) throws RemoteException {
            		synchronized(callbacks){
                /**
				for(int i =0 ; i<callbacks.size(); i++){
					if(callBack.asBinder().equals(callbacks.get(i).mCallback.asBinder())){
					    callBack.asBinder().unlinkToDeath(callbacks.get(i), 0);
						callbacks.remove(i);
						state = false;
						return ;
					}
				}*/
                state = false;
                for(int i =0 ; i<callbacks.size(); i++){
                    //callBack.asBinder().unlinkToDeath(callbacks.get(i), 0);
                    callbacks.remove(i);
                }
                //callbacks.clear();
                return ;
            }
		}
        @Override
        public void openVoiceHelp() throws RemoteException {
            Log.d(TAG, "third open voice"); 
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.iflytek.cutefly.speechclient", "com.iflytek.autofly.SpeechClientService"));
            intent.putExtra("fromservice", "com.hwatong.platformadapter");
            startService(intent);
        }
        @Override
        public void switchSpeechMic(int state) throws RemoteException {
            Log.d(TAG, "third switch mic for voice:" + state);
            if(state == 0){
                PlatformService.platformCallback.systemStateChange(PlatformCode.STATE_SPEECHOFF);
            } else if(state == 1){
                PlatformService.platformCallback.systemStateChange(PlatformCode.STATE_SPEECHON);
            }
        }
	};
	
	private ArrayList<CallBackListener> callbacks = new ArrayList<CallBackListener>();
	
	private final class CallBackListener implements IBinder.DeathRecipient{
		
        final CallBack mCallback ;
        
		public CallBackListener(CallBack callback) {
			this.mCallback = callback;
		}
		@Override
		public void binderDied() {
			synchronized(callbacks){
				callbacks.remove(this);
			}
			if(mCallback!=null){
				mCallback.asBinder().unlinkToDeath(this, 0);
			}
		}
	}
	/**
	 * 分发语义给第三方
	 * @return 
	 */
	private String doAction(String result){
        Log.d(TAG, "third voice daAction:"+result);
		for(int i = 0 ; i<callbacks.size() ; i++ ){
			try {
                String s = callbacks.get(i).mCallback.onResult(result);
			    Log.d(TAG, "小马返回:" + s);
				return s ; 
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
        return null;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return iservice;
	}

	@Override
	public String onResult(String result) {
		return doAction(result);
	}
	public static final int START_RECORDER = 1 ;
	public static final int STOP_RECORDER = 0 ;
	@Override
	public void onState(int state) {
        for(int i = 0 ; i<callbacks.size() ; i++ ){
        try {
            callbacks.get(i).mCallback.onStatus(state);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }		
	}


}
