package com.hwatong.projectmode.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {

	private static Toast mToast;
	
	public static void showToast(Context context, String msg) {
		if (mToast != null) {
			mToast.setText(msg);
			mToast.setDuration(Toast.LENGTH_SHORT);
		} else {
			mToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
		}
		mToast.show();
	}
	
}
