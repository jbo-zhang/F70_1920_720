package com.hwatong.platformadapter.handle;

import org.json.JSONException;
import org.json.JSONObject;

import android.canbus.ICanbusService;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.os.SystemClock;

import com.hwatong.platformadapter.ServiceList;
import com.hwatong.platformadapter.Tips;
import com.hwatong.platformadapter.utils.L;

public class HandlerBtPhoneControl {
	private static final String thiz= HandlerBtPhoneControl.class.getSimpleName() ;
	
	private static HandlerBtPhoneControl mHandlerBtPhoneControl = null ;
	
	private Context mContext = null;
	
	private static ICanbusService mCanbusService;
	
	private static ServiceList mServiceList ;
	
	public HandlerBtPhoneControl(Context context) {
		mContext = context ;
	}

	public static HandlerBtPhoneControl getInstance(Context context, ICanbusService canbusService , ServiceList serviceList) {
		L.d(thiz, "HandlerBtPhoneControl init");
		if (mHandlerBtPhoneControl == null) {
			mHandlerBtPhoneControl = new HandlerBtPhoneControl(context);
		}
		mCanbusService = canbusService;
		mServiceList = serviceList ;
		return mHandlerBtPhoneControl;
	}
	com.hwatong.bt.IService mService ;
	public boolean handleBtPhoneSence(JSONObject result){
		String cmd = "";
		try {
			cmd = result.getString("callcmd");
		} catch (JSONException e) {
			e.printStackTrace();
			return false ;
		}
		if(mServiceList!=null){
		    mService = mServiceList.getBtService();
		}
		/**
		 *未接来电
		 */
		if("missed".equals(cmd)){
		    if(!isBtConnected()){
                Tips.setCustomTipUse(true);
                Tips.setCustomTip("蓝牙未连接");
                return false ;		        
		    }
		    mContext.sendBroadcast(new Intent("com.hwatong.bt.TELEPHONE_MISSED"));
		    //add++ 解决闪主界面
            SystemClock.sleep(1000);
		    return true ;
		}
        /**
         *全部来电
         */
        if("records".equals(cmd)){
            if(!isBtConnected()){
                Tips.setCustomTipUse(true);
                Tips.setCustomTip("蓝牙未连接");
                return false ;              
            }
            mContext.sendBroadcast(new Intent("com.hwatong.bt.TELEPHONE_RECORDS"));
            //add++ 解决闪主界面
            SystemClock.sleep(1000);
            return true ;
        }		
		return false ;
	}
	
	private boolean isBtConnected(){
	    if(mService == null){
	        return false;
	    }
	    try {
            if(!mService.getConnectState()){
                return false ;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
	    return true ;
	}
	
}
