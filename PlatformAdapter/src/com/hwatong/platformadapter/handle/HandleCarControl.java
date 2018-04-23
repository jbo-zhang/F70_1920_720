package com.hwatong.platformadapter.handle;

import org.json.JSONException;
import org.json.JSONObject;

import com.hwatong.platformadapter.Tips;

import android.canbus.ICanbusService;
import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

/**
 * 
 * @author caochao
 * 
 */
public class HandleCarControl {

	private static final String TAG = "Voice";
	/**
	 * 车辆控制
	 */
	private static HandleCarControl mHandCarControl = null;

	private Context mContext;
	private static ICanbusService mCanbusService;
	private int mSkylightStatus = 0; // 天窗状态
	
	public HandleCarControl(Context mContext) {
		super();
		this.mContext = mContext;
	}

	public static HandleCarControl getInstance(Context context, ICanbusService canbusService) {
		Log.d(TAG, "HandleCarControl init");
		if (mHandCarControl == null) {
			mHandCarControl = new HandleCarControl(context);
		}
		mCanbusService = canbusService;

		return mHandCarControl;
	}

	public boolean handleCarControlScence(JSONObject result) {
		Log.d(TAG, result.toString());

		String operation = null;
		String device = null;
		String mode = null;
		String temperature = null;
		String fan_speed = null;
		String airflow_direction = null;
		String rawText = null;
		String name = null;
		try {
			operation = result.getString("operation");
			Log.d(TAG, "operation:" + operation);
		} catch (JSONException e) {
		}
		try {
			device = result.getString("device");
			Log.d(TAG, "device:" + device);
		} catch (JSONException e) {
		}
		try {
			mode = result.getString("mode");
			Log.d(TAG, "mode:" + mode);
		} catch (JSONException e) {
		}
		try {
			temperature = result.getString("temperature");
			Log.d(TAG, "temperature:" + temperature);
		} catch (JSONException e) {
		}
		try {
			fan_speed = result.getString("fan_speed");
			Log.d(TAG, "fan_speed:" + fan_speed);
		} catch (JSONException e) {
		}
		try {
			airflow_direction = result.getString("airflow_direction");
			Log.d(TAG, "airflow_direction:" + airflow_direction);
		} catch (JSONException e) {
		}
		try {
			airflow_direction = result.getString("airflow_direction");
			Log.d(TAG, "airflow_direction:" + airflow_direction);
		} catch (JSONException e) {
		}
		try {
			rawText = result.getString("rawText");
			Log.d(TAG, "rawText:" + rawText);
		} catch (JSONException e) {
		}
		try {
			name = result.getString("name");
			Log.d(TAG, "name:" + name);
		} catch (JSONException e) {
		}

		if (operation != null) {
			if ("OPEN".equals(operation)) {
				if (name != null && "天窗".equals(name)) {
					Tips.setCustomTipUse(true);
					if(sendCmd(0, 2)){
						Tips.setCustomTip("天窗已打开");
					} else {
						Tips.setCustomTip("操作失败");
					}
					return true;
				}
			} else if ("CLOSE".equals(operation)) {
				if (name != null && "天窗".equals(name)) {
					Tips.setCustomTipUse(true);
					if(sendCmd(0, 4)){
						Tips.setCustomTip("天窗已关闭");
					} else {
						Tips.setCustomTip("操作失败");
					}
					return true;
				}
			}
		}
		return false;
	}

	private boolean sendCmd(int i, int j) {
		try {
			if (mCanbusService == null) {
				Log.d(TAG, "CanbusService is null");
				return false;
			}
			mCanbusService.writeASRRequest(i, j);
			return true ;
		} catch (RemoteException e) {
			Log.d(TAG, "CanbusService RemoteException");
			e.printStackTrace();
			return false ;
		}
	}
}
