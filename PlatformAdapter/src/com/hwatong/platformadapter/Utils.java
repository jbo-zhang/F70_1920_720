package com.hwatong.platformadapter;

import java.lang.reflect.Method;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class Utils {

    private static boolean USE_MAP_MXNAVI = false;//true:mxnavi;false:shx

    public static String getMapPackage(){
        if(USE_MAP_MXNAVI){
            return "com.mxnavi.mxnavi";
        }else{
            return "com.shx.navi";
        }
    }

	public static String getTopPackageName(Context context) {
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
		final ComponentName cn = activityManager.getRunningTasks(1).get(0).topActivity;
		if (cn != null) {
			Log.d("Platformadapter.Utils", "getTopPackageName: " + cn.getPackageName());
			return cn.getPackageName();
		}
		return "";
	}

	public static String getTopActivityName(Context context) {
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
		final ComponentName cn = activityManager.getRunningTasks(1).get(0).topActivity;
		if (cn != null) {
			return cn.getClassName();
		}
		return "";
	}

	public static boolean openApplication(Context context, String pkgName) {
		Intent intent = context.getPackageManager().getLaunchIntentForPackage(pkgName);
		if (intent != null) {
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			try {
				context.startActivity(intent);
				return true;
			} catch (ActivityNotFoundException e) {
			}
		}

		return false;
	}

	public static void closeApplication(Context context, String packageName) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		try {
			Method method = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage", String.class);
			method.invoke(am, packageName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean launchMatchApp(Context context, String name, String rawText) {
		Log.i("PlatformAdapter", "launchMatchApp-----" + "name:" + name + "rawText:" + rawText);
		if ("地图".equals(name) || "导航".equals(name)) {
			openApplication(context, getMapPackage()/*"com.mxnavi.mxnavi"*/);
			return true;
		} else if ("蓝牙".equals(name)) {
			Intent intent = new Intent();
			if (rawText.contains("蓝牙音乐"))
				intent.setClassName("com.hwatong.btmusic.ui", "com.hwatong.btmusic.ui.BluetoothMusicActivity");
			else
				intent.setClassName("com.hwatong.btphone.ui", "com.hwatong.btphone.ui.MainActivity");
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			try {
				context.startActivity(intent);
				return true;
			} catch (ActivityNotFoundException e) {
			}
		} else if ("电话".equals(name) || "蓝牙".equals(name) || "蓝牙电话".equals(name)) {
			Intent intent = new Intent();
			intent.setClassName("com.hwatong.btphone.ui", "com.hwatong.btphone.ui.MainActivity");
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			try {
				context.startActivity(intent);
				return true;
			} catch (ActivityNotFoundException e) {
			}
		} else if ("媒体".equals(name) || "多媒体".equals(name)) {
			Intent intent = new Intent();
			intent.setClassName("com.hwatong.media.common", "com.hwatong.media.common.MainActivity");
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			try {
				context.startActivity(intent);
				return true;
			} catch (ActivityNotFoundException e) {
			}
		} else if ("收音机".equals(name) || "电台".equals(name) || "广播".equals(name) || "广播电台".equals(name)) {
			openApplication(context, "com.hwatong.radio.ui");
			return true;
		} else if ("u盘".equals(name) || "播放器".equals(name)) {
			openMusic(context);
			return true;
		} else if ("音乐".equals(name)) {
			openMusic(context);
			return true;
		} else if ("视频".equals(name)) {
			openVideo(context);
			return true;
		} else if ("图片".equals(name)) {
			openPicture(context);
			return true;
		} else if ("蓝牙音乐".equals(name)) {
			Intent intent = new Intent();
			intent.setClassName("com.hwatong.btmusic.ui", "com.hwatong.btmusic.ui.BluetoothMusicActivity");
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			try {
				context.startActivity(intent);
				return true;
			} catch (ActivityNotFoundException e) {
			}
		} else if ("手机互联".equals(name) || "亿连".equals(name)) {
			openApplication(context, "net.easyconn");
			return true;
		} else if ("ipod".equals(name)) {
			Intent intent = new Intent();
			intent.setClassName("com.hwatong.ipod.ui", "com.hwatong.ipod.ui.IPodMainActivity");
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			try {
				context.startActivity(intent);
				return true;
			} catch (ActivityNotFoundException e) {
			}
		} else if ("车辆".equals(name) || "车辆信息".equals(name)) {
			Intent intent = new Intent();
			intent.setClassName("com.hwatong.tpms", "com.hwatong.tpms.VehicleInformation");
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			try {
				context.startActivity(intent);
				return true;
			} catch (ActivityNotFoundException e) {
			}
		}/* else if ("胎压".equals(name)) {
			Intent intent = new Intent();
			intent.setClassName("com.hwatong.tpms", "com.hwatong.tpms.TirePressure");
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			try {
				context.startActivity(intent);
				return true;
			} catch (ActivityNotFoundException e) {
			}
		}*/ /*
		 * else if ("外拓设备".equals(name)) { Intent intent = new Intent();
		 * intent.setClassName("com.hwatong.launcher",
		 * "com.hwatong.launcher.ExtensionEquipment");
		 * intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); try {
		 * context.startActivity(intent); return true; }
		 * catch(ActivityNotFoundException e) {} }
		 *//*
			 * else if ("电子书".equals(name)) { openApplication(context,
			 * "com.hwatong.ebookreader"); return true; }
			 */else if ("设置".equals(name)) {
			Intent intent = new Intent();
			intent.setClassName("com.hwatong.settings", "com.hwatong.settings.MainActivity");
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			try {
				context.startActivity(intent);
				return true;
			} catch (ActivityNotFoundException e) {
			}
		}

		return false;
	}

	public static void openMusic(Context context) {
		Intent intent = new Intent("com.hwatong.media.MUSIC_PLAYER");
		if (intent != null) {
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			try {
				context.startActivity(intent);
			} catch (ActivityNotFoundException e) {
			}
		}
	}

	public static void openVideo(Context context) {
		if (!"com.hwatong.usbvideo".equals(getTopPackageName(context))) {
			Intent intent = new Intent("com.hwatong.media.USB_VIDEO");
			if (intent != null) {
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				try {
					context.startActivity(intent);
				} catch (ActivityNotFoundException e) {
				}
			}
		}
		// openApplication(context, "com.hwatong.usbvideo");
	}

	public static void openPicture(Context context) {
		if (!"com.hwatong.usbvideo".equals(getTopPackageName(context))) {
			Intent intent = new Intent("com.hwatong.media.USB_PICTURE");
			if (intent != null) {
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				try {
					context.startActivity(intent);
				} catch (ActivityNotFoundException e) {
				}
			}
		}
	}
	
	public static void closeMusic(Context context) {
		context.sendBroadcast(new Intent("com.hwatong.voice.CLOSE_MUSIC"));
	}

	public static void closeBtMusic(Context context) {
		context.sendBroadcast(new Intent("com.hwatong.voice.CLOSE_BTMUSIC"));
	}

	public static void closeFM(Context context) {
		context.sendBroadcast(new Intent("com.hwatong.voice.CLOSE_FM"));
	}

	public static void closeBT(Context context) {
		context.sendBroadcast(new Intent("com.hwatong.voice.CLOSE_BT"));
	}

	public static void closeMap(Context context) {
        if(USE_MAP_MXNAVI){
		    context.sendBroadcast(new Intent("com.mxnavi.mxnavi.CMD_NAVI_CLOSE_MAP"));
        }else{
            //close shx
            context.sendBroadcast(new Intent("com.shx.shx.SHUTDOWN"));
        }
	}

	public static String getMatchAppPkgName(String name) {
		Log.i("PlatformAdapter", "getMatchAppPkgName-----" + "name:" + name);
		if ("地图".equals(name) || "导航".equals(name)) {
			return getMapPackage()/*"com.mxnavi.mxnavi"*/;
		} else if ("电话".equals(name) || "蓝牙".equals(name) || "蓝牙电话".equals(name)) {
			return "com.hwatong.btphone.ui";
		} else if ("收音机".equals(name) || "电台".equals(name) || "广播".equals(name)) {
			return "com.hwatong.radio.ui";
		} else if ("u盘".equals(name) || "播放器".equals(name)) {
			return "com.hwatong.usbmusic";
		} else if ("音乐".equals(name)) {
			return "com.hwatong.usbmusic";
		} else if ("视频".equals(name)) {
			return "com.hwatong.usbvideo";
		} else if ("图片".equals(name)) {
			return "com.hwatong.usbpicture";
		} else if ("蓝牙音乐".equals(name)) {
			return "com.hwatong.btphone.ui";
		} else if ("手机互联".equals(name) || "亿连".equals(name)) {
			return "net.easyconn";
		} else if ("ipod".equals(name)) {
			return "com.hwatong.ipod.ui";
		} else if ("车辆信息".equals(name)) {
			// return "com.hwatong.tpms";
		} else if ("设置".equals(name)) {
			return "com.hwatong.settings";
		}
		return null;
	}

	public static final Uri CONTENT_URI = Uri.parse("content://car_settings/content");

	public static String getCarSettingsString(ContentResolver cr, String name) {
		String[] select = new String[] { "value" };
		Cursor cursor = cr.query(CONTENT_URI, select, "name=?", new String[] { name }, null);
		if (cursor == null)
			return null;
		String value = null;
		if (cursor.moveToFirst()) {
			value = cursor.getString(0);
		}
		cursor.close();
		return value;
	}

	public static String getCarSettingsString(ContentResolver cr, String name, String defaultValue) {
		String[] select = new String[] { "value" };
		Cursor cursor = cr.query(CONTENT_URI, select, "name=?", new String[] { name }, null);
		String value = defaultValue;
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				value = cursor.getString(0);
			}
			cursor.close();
		}
		return value;
	}
	public static boolean putCarSettingsString(ContentResolver cr, String name, String value, boolean notify) {
		String[] select = new String[] { "value" };
		Uri uri;
		if (notify)
			uri = Uri.parse(CONTENT_URI + "/" + name);
		else
			uri = CONTENT_URI;
		Cursor cursor = cr.query(uri, select, "name=?", new String[] { name }, null);
		if (cursor != null && cursor.moveToFirst()) {
			if (cursor != null)
				cursor.close();
			ContentValues values = new ContentValues();
			values.put("value", value);
			cr.update(CONTENT_URI, values, "name=?", new String[] { name });
		} else {
			if (cursor != null)
				cursor.close();
			ContentValues values = new ContentValues();
			values.put("name", name);
			values.put("value", value);
			cr.insert(CONTENT_URI, values);
		}
		return true;
	}

	public static boolean putCarSettingsString(ContentResolver cr, String name, String value) {
		String[] select = new String[] { "value" };
		Cursor cursor = cr.query(CONTENT_URI, select, "name=?", new String[] { name }, null);
		if (cursor != null && cursor.moveToFirst()) {
			if (cursor != null)
				cursor.close();
			ContentValues values = new ContentValues();
			values.put("value", value);
			cr.update(CONTENT_URI, values, "name=?", new String[] { name });
		} else {
			if (cursor != null)
				cursor.close();
			ContentValues values = new ContentValues();
			values.put("name", name);
			values.put("value", value);
			cr.insert(CONTENT_URI, values);
		}
		return true;
	}

}
