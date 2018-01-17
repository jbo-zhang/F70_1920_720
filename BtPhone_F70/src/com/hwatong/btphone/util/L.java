package com.hwatong.btphone.util;

import android.util.Log;

public class L {
	private static boolean DEBUG = true;
	private static String TAG = "btf70";
	
	public static void d(String clazz, String info) {
		if(DEBUG) {
			Log.d(TAG, "[" + clazz + "] " + info);
		}
	}
	
	public static void dRoll(String clazz, String info){
		if(DEBUG) {
			Log.d(TAG + "_roll", "[" + clazz + "] " + info);
		}
	}
	
}
