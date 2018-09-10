package com.hwatong.f70.bluetooth;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.hwatong.settings.R;

public class BluetoothCommonView {
	
	public static final int DISCONNECTED_TOAST_LAYOUTID = R.layout.f70_disconnect_bluetooth_toast;
	public static final int CONNECTED_TOAST_LAYOUTID = R.layout.f70_connect_bluetooth_toast;
	public static final int DISCONNECTED_SUCCESS_TOAST_LAYOUTID = R.layout.f70_disconnected_success_toast;
	
	/**
	 * tips for disconnecting or disconnecting bluetooth device
	 */
	public static Toast bluetoothConnectedInfoToast(Context context, int layoutId) {
		LayoutInflater inflater = ((Activity) context).getLayoutInflater();
		View layout = inflater.inflate(layoutId,
				(ViewGroup) ((Activity) context)
						.findViewById(R.id.bluetooth_disconn));
		Toast toast = new Toast(context);
		int offset = (int) context.getResources().getDimension(R.dimen.bluetoothConnectedInfoToast_offset);
		toast.setGravity(Gravity.LEFT | Gravity.CENTER, offset, 0);
		toast.setDuration(3000);
		toast.setView(layout);
		return toast;
	}
}
