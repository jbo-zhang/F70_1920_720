package com.hwatong.f70.main;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.DisplayMetrics;

public class Utils {
	
	public static final int PROGRAMER_MODE_ON = 1;
	public static final int PROGRAMER_MODE_OFF = 0;
	public static void startApp(Context context, String action) {
		Intent intent = new Intent();
		intent.setAction(action);
		intent.addCategory("android.intent.category.DEFAULT");
		if (intent != null) {
			try {
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			} catch (Exception e) {
			}
		}
	}
	
	public static void startApp(Context context, String packageName, String activityName) {
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName(packageName,activityName);
        intent.setComponent(componentName);
        try {
			context.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void setProgramMode(Context context, int mode) {
		SharedPreferences pref = context.getSharedPreferences("Programer_Mode", Context.MODE_PRIVATE);
		pref.edit().putInt("mode", mode).apply();
	}
	
	public static int getProgramMode(Context context) {
		SharedPreferences pref = context.getSharedPreferences("Programer_Mode", Context.MODE_PRIVATE);
		return pref.getInt("mode", -1);
	}
	
    /**
     *
     *
     * @param packName
     * @return
     */
    public static boolean isRunning(Context context, String packName) {

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(100);
        boolean isAppRunning = false;
        for (ActivityManager.RunningTaskInfo info : list) {
            if (info.topActivity.getPackageName().equals(packName) || info.baseActivity.getPackageName().equals(packName)) {
                isAppRunning = true;
                break;
            }
        }
        LogUtils.d(packName + "is running ? " + isAppRunning);
        return isAppRunning;
    }
    
    /**
     *
     * @param context
     * @return
     */
    public static boolean isAppIsInBackground(Context context, String packageName) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.CUPCAKE) {
//            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
//            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
//                //Ç°Ì¨³ÌÐò
//                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
//                    for (String activeProcess : processInfo.pkgList) {
//                        if (activeProcess.equals(packageName)) {
//                            isInBackground = false;
//                        }
//                    }
//                }
//            }
//        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(packageName)) {
                isInBackground = false;
            }
//        }

        return isInBackground;
    }
    
    /**
     *
     */
    public static boolean isActivityBackGround(Context context, String packageName, String activityName) {
    	boolean isBackGround = true;
    	
    	ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
		ComponentName componentInfo = taskInfo.get(0).topActivity;
		if (componentInfo.getPackageName().equals(packageName)) {
			if (componentInfo.getClassName().equals(activityName)) {
				isBackGround = false;
			}
		}
    	return isBackGround;
    }
    
    /**
     * ÅÐ¶Ï·Ö±æÂÊ
     */
    public static boolean isLowDesityMachine(Activity activity) {
    	DisplayMetrics metrics = new DisplayMetrics();
    	activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
    	
    	return metrics.widthPixels == 1280;
    	
    }
}
