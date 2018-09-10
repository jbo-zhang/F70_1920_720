package com.hwatong.f70.main;

import android.canbus.ICanbusService;
import android.os.RemoteException;

public class F70CanbusUtils {
	
	private F70CanbusUtils() {
		
	}
	
	private static class CanbusUtils {
		private static final F70CanbusUtils instance = new F70CanbusUtils();
	}
	
	private static final String TAG = "F70CanbusUtils";
//	private static F70CanbusUtils instance;
	
	public static F70CanbusUtils getInstance() {
		return CanbusUtils.instance;
	}
	
	/**
	 *
	 */
	public static void writeCarConfig(ICanbusService iCanbusService, int Type, int action) {
		LogUtils.d("writeCarConfig: type is " + Type + ", action: " + action);
		try {
			if(iCanbusService != null)
				iCanbusService.writeCarConfig(Type, action);
			else
				LogUtils.e(TAG, "iCanbusService is null");
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 *
	 */
	public static void writeRemoteFuntion(ICanbusService iCanbusService, int Type, int action) {
		LogUtils.d("writeRemoteFuntion: type is " + Type + ", action: " + action);
		try {
			if(iCanbusService != null)
				iCanbusService.writeTboxConfig(Type, action);
			else
				LogUtils.e(TAG, "iCanbusService is null");
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

}
