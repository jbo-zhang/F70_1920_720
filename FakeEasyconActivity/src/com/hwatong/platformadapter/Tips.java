package com.hwatong.platformadapter;
import utils.L;
/**
 * 定制提示
 * @date 2017-11-28
 * @author caochao
 */
public class Tips {
	private final static String thiz = Tips.class.getSimpleName();
	/**
	 * 是否使用定制提示
	 */
	private static boolean isCustomTip = false ;
	/**
	 * 定制提示
	 */
	private static String customTip = null ;
	
	public static boolean isCustomTipUse() {
		L.d(thiz , "定制提示是否开启"+isCustomTip);
		return isCustomTip;
	}
	
	public static void setCustomTipUse(boolean isCustomTip) {
		
		Tips.isCustomTip = isCustomTip;
	}
	
	public static String getCustomTip() {
		L.d(thiz , "定制提示"+customTip);
		return customTip;
	}
	
	public static void setCustomTip(String customTip) {
		
		Tips.customTip = customTip;
	}
}
