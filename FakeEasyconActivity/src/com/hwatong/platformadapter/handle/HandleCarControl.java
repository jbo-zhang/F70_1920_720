package com.hwatong.platformadapter.handle;

import org.json.JSONException;
import org.json.JSONObject;

import utils.L;
import android.canbus.ICanbusService;
import android.content.Context;
import android.os.RemoteException;

import com.hwatong.platformadapter.Tips;

/**
 * 
 * @author caochao
 * 
 */
public class HandleCarControl {

	private static final String thiz = HandleCarControl.class.getSimpleName();
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
		L.d(thiz, "HandleCarControl init");
		if (mHandCarControl == null) {
			mHandCarControl = new HandleCarControl(context);
		}
		mCanbusService = canbusService;

		return mHandCarControl;
	}

	public boolean handleCarControlScence(JSONObject result) {
		L.d(thiz, result.toString());

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
			L.d(thiz, "operation:" + operation);
		} catch (JSONException e) {
		}
		try {
			device = result.getString("device");
			L.d(thiz, "device:" + device);
		} catch (JSONException e) {
		}
		try {
			mode = result.getString("mode");
			L.d(thiz, "mode:" + mode);
		} catch (JSONException e) {
		}
		try {
			temperature = result.getString("temperature");
			L.d(thiz, "temperature:" + temperature);
		} catch (JSONException e) {
		}
		try {
			fan_speed = result.getString("fan_speed");
			L.d(thiz, "fan_speed:" + fan_speed);
		} catch (JSONException e) {
		}
		try {
			airflow_direction = result.getString("airflow_direction");
			L.d(thiz, "airflow_direction:" + airflow_direction);
		} catch (JSONException e) {
		}
		try {
			airflow_direction = result.getString("airflow_direction");
			L.d(thiz, "airflow_direction:" + airflow_direction);
		} catch (JSONException e) {
		}
		try {
			rawText = result.getString("rawText");
			L.d(thiz, "rawText:" + rawText);
		} catch (JSONException e) {
		}
		try {
			name = result.getString("name");
			L.d(thiz, "name:" + name);
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
				L.d(thiz, "CanbusService is null");
				return false;
			}
			mCanbusService.writeASRRequest(i, j);
			return true ;
		} catch (RemoteException e) {
			L.d(thiz, "CanbusService RemoteException");
			e.printStackTrace();
			return false ;
		}
	}
}
