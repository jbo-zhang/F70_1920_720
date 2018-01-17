package com.hwatong.utils;

import android.util.Log;

public class L {
	private static boolean DEBUG = true;
	private static String TAG = "Radio_F70";

	public static void d(String clazz, String info) {
		if(DEBUG) {
			Log.d(TAG, "[" + clazz + "] " + info);
		}
	}
	
}
