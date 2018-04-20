package com.hwatong.platformadapter;

import android.content.Intent;
import android.util.Log;

/**
 * 定制提示
 * @date 2017-11-28
 * @author caochao
 */
public class Tips {
	private final static String TAG = "Tips";
	/**
	 * 是否使用定制提示
	 */
	private static boolean isCustomTip = false ;
	/**
	 * 定制提示
	 */
	private static String customTip = null ;
	
	public static boolean isCustomTipUse() {
		Log.d(TAG , "定制提示是否开启"+isCustomTip);
		return isCustomTip;
	}
	
	public static void setCustomTipUse(boolean isCustomTip) {
		
		Tips.isCustomTip = isCustomTip;
	}
	
	public static String getCustomTip() {
		Log.d(TAG , "定制提示"+customTip);
		return customTip;
	}
	
	public static void setCustomTip(String customTip) {
		
		Tips.customTip = customTip;
	}
}
