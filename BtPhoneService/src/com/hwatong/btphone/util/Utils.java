package com.hwatong.btphone.util;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class Utils {

	public static String getTopPackageName(Context context) {
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Activity.ACTIVITY_SERVICE);
		final ComponentName cn = activityManager.getRunningTasks(1).get(0).topActivity;
		if (cn != null) {
			Log.d("BtPhone.Utils", "getTopPackageName: "+cn.getPackageName());
			return cn.getPackageName();
		}
		return "";
	}

	public static String getTopActivityName(Context context) {
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Activity.ACTIVITY_SERVICE);
		final ComponentName cn = activityManager.getRunningTasks(1).get(0).topActivity;
		if (cn != null) {
			Log.d("BtPhone.Utils", "getTopActivityName: "+cn.getClassName());
			return cn.getClassName();
		}
		return "";
	}

	public static String formatTime(long ms) {
		if (ms <= 0)	return "00:00";
		if (ms%1000 >= 900) {
			ms += 100;
		}

		int ss = 1000;
		int mi = ss * 60;
		int hh = mi * 60;

		long hour = ms / hh;
		long minute = (ms - hour * hh) / mi;
		long second = (ms - hour * hh - minute * mi) / ss;

		if (hour > 99)	hour = 99;
		String strHour = hour < 10 ? "0" + hour : "" + hour;//小时
		String strMinute = minute < 10 ? "0" + minute : "" + minute;//分钟
		String strSecond = second < 10 ? "0" + second : "" + second;//秒

		if (hour == 0) {
			return strMinute + ":" + strSecond;
		}
		return strHour + ":" + strMinute + ":" + strSecond;
	}

	public static String getComFlg(String name) {
		if (name == null || name.equals(""))	return "";

		String result = getPingYin(name);

		if (!result.equals("")) {
			String first = result.substring(0, 1).toUpperCase();
			if (!first.matches("[A-Z]")) {
				result = "#" + result.substring(1);
			}
		} else {
			result = "#";
		}

		return result;
	}

	public static String getPingYin(String src) {
		if (src == null || src.equals(""))	return "";

		char[] t1 = null;
		t1 = src.toCharArray();
		String[] t2 = new String[t1.length];
		HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
		format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
		format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		format.setVCharType(HanyuPinyinVCharType.WITH_V);
		StringBuilder result = new StringBuilder("");
		int l = t1.length;
		try {
			for (int i = 0; i < l; i++) {

				if (Character.toString(t1[i]).matches("[\\u4E00-\\u9FA5]+")) {
					t2 = PinyinHelper.toHanyuPinyinStringArray(t1[i], format);
					if (t2 != null)	result.append(t2[0]);					
				} else {
					result.append(Character.toString(t1[i]).toLowerCase());
				}
			}
			return result.toString().replaceAll(" ", "");
		} catch (BadHanyuPinyinOutputFormatCombination e1) {
			e1.printStackTrace();
		}
		return result.toString().replaceAll(" ", "");
	}

	public static void showInputMethod(View v) {
		InputMethodManager im = (InputMethodManager) v.getContext( ).getSystemService(Context.INPUT_METHOD_SERVICE);
		im.showSoftInput(v, 0);
	}

	public static void hideInputMethod(View v){
		InputMethodManager imm = ( InputMethodManager) v.getContext( ).getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm.isActive()) {
			imm.hideSoftInputFromWindow( v.getApplicationWindowToken() , 0 );
		}
	}

	public static final Uri CONTENT_URI = Uri.parse("content://car_settings/content");
	public static String getCarSettingValue(Context context, String name) {
		String value = null;
		try {
			String[] select = new String[] { "value" };
			Cursor cursor = context.getContentResolver().query(CONTENT_URI, select,
					"name=?", new String[] { name }, null);
			if (cursor == null)
				return null;
			if (cursor.moveToFirst()) {
				value = cursor.getString(0);
			}
			cursor.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}

    public static String NAME_MODIFY = "BtNameModify";
	public static String AUTO_CONNECT = "BtAutoConnect";
	public static String AUTO_PB = "BtAutoPhoneBook";
	public static String BT_TAB = "BtTab";
	public static void saveBtNameModified(Context ctxt, boolean modify) {
		SharedPreferences pref = ctxt.getSharedPreferences("BtPhone",Context.MODE_PRIVATE);
		pref.edit().putBoolean(NAME_MODIFY, modify).commit();
	}

	public static boolean getBtNameModified(Context ctxt) {
		SharedPreferences pref = ctxt.getSharedPreferences("BtPhone",Context.MODE_PRIVATE);
		return pref.getBoolean(NAME_MODIFY, false);
	}

	public static void saveBtTab(Context ctxt, int tab) {
		SharedPreferences pref = ctxt.getSharedPreferences("BtPhone",Context.MODE_PRIVATE);
		pref.edit().putInt(BT_TAB, tab).commit();
	}

	public static int getBtTab(Context ctxt) {
		SharedPreferences pref = ctxt.getSharedPreferences("BtPhone",Context.MODE_PRIVATE);
		return pref.getInt(BT_TAB, 0);
	}	

}
