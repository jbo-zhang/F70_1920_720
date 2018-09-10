package com.hwatong.settings.wallpaper;

import android.content.Context;
import android.content.SharedPreferences;

public class SpTools {
	
	private static final String CONFIG = "com.hwatong.wallpaper.config";
	
	public static void putInt (Context context , String key , int value){
		SharedPreferences sp = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE);
		sp.edit().putInt(key, value).commit();
	}
	public static int getInt (Context context , String key , int defaultValue){
		SharedPreferences sp = context.getSharedPreferences(CONFIG, Context.MODE_PRIVATE);
		return sp.getInt(key, defaultValue);
	}
	
}
