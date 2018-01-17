package com.hwatong.projectmode.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Log;

/** 
 * 系统工具类 
 * Created by zhuwentao on 2016-07-18. 
 */  
public class SystemUtil {  
  
	private static final String thiz = SystemUtil.class.getSimpleName();
	
	
    /** 
     * 获取当前手机系统语言。 
     * 
     * @return 返回当前系统语言。例如：当前设置的是“中文-中国”，则返回“zh-CN” 
     */  
    public static String getSystemLanguage() {  
        return Locale.getDefault().getLanguage();  
    }  
  
    /** 
     * 获取当前系统上的语言列表(Locale列表) 
     * 
     * @return  语言列表 
     */  
    public static Locale[] getSystemLanguageList() {  
        return Locale.getAvailableLocales();  
    }  
  
    /** 
     * 获取当前手机系统版本号 
     * 
     * @return  系统版本号 
     */  
    public static String getSystemVersion() {  
        return android.os.Build.VERSION.RELEASE;  
    }  
    
    /**
	 * 获取软件版本内容
	 */
    public static String getSoftwareVersion() {
		return android.os.Build.ID;
	}
    
    /**
     * 获取ARM版本号
     * @return
     */
    public static String getArmVersion () {
    	return SystemProperties.get("ro.build.lct_internal_version", "");
    }
    
  
    /** 
     * 获取手机型号 
     * 
     * @return  手机型号 
     */  
    public static String getSystemModel() {  
        return android.os.Build.MODEL;  
    }  
  
    /** 
     * 获取手机厂商 
     * 
     * @return  手机厂商 
     */  
    public static String getDeviceBrand() {  
        return android.os.Build.BRAND;  
    }  
  
    /** 
     * 获取手机IMEI(需要“android.permission.READ_PHONE_STATE”权限) 
     * 
     * @return  手机IMEI 
     */  
    public static String getIMEI(Context ctx) {  
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Activity.TELEPHONY_SERVICE);  
        if (tm != null) {  
            return tm.getDeviceId();  
        }  
        return null;  
    }  
    
    
    private final static String VERSION_URI = "/sys/devices/platform/imx-i2c.1/i2c-1/1-0019/version";
    
    /**
	 * 获取MCU版本内容
	 * @return
	 */
	public static String getMcuVersionInfo() {
        try {
            FileReader fr = new FileReader(VERSION_URI);
            BufferedReader br = new BufferedReader(fr);
            String tmp = br.readLine();
            L.d(thiz, "version: " + tmp);
            br.close();
            fr.close();
            return tmp;
//            while (tmp != null) {
//                String[] strs = tmp.split(",");
//                Log.i("ljwtest:", strs[0] + "," + strs[1] + "," + str);
//                if (strs.length == 2 && strs[0].equals(str))
//                    return strs[1];
//                tmp = br.readLine();
//            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i("TAG", "file not found");
        } catch (IOException e) {
            e.printStackTrace();
        }
		return "";
	}
    
    
    
    
    /** 
     * 获得SD卡总大小 
     *  
     * @return 
     */  
    public static String getSDTotalSize(Context context) {  
        File path = Environment.getExternalStorageDirectory();  
        StatFs stat = new StatFs(path.getPath());  
        long blockSize = stat.getBlockSize();  
        long totalBlocks = stat.getBlockCount();  
        return Formatter.formatFileSize(context, blockSize * totalBlocks);  
    }  
  
    /** 
     * 获得sd卡剩余容量，即可用大小 
     *  
     * @return 
     */  
    public static String getSDAvailableSize(Context context) {  
        File path = Environment.getExternalStorageDirectory();  
        StatFs stat = new StatFs(path.getPath());  
        long blockSize = stat.getBlockSize();  
        long availableBlocks = stat.getAvailableBlocks();  
        return Formatter.formatFileSize(context, blockSize * availableBlocks);  
    }  
  
    /** 
     * 获得机身内存总大小 
     *  
     * @return 
     */  
    public static String getRomTotalSize(Context context) {  
        File path = Environment.getDataDirectory();  
        StatFs stat = new StatFs(path.getPath());  
        long blockSize = stat.getBlockSize();  
        long totalBlocks = stat.getBlockCount();  
        return Formatter.formatFileSize(context, blockSize * totalBlocks);  
    }  
  
    /** 
     * 获得机身可用内存 
     *  
     * @return 
     */  
    public static String getRomAvailableSize(Context context) {  
        File path = Environment.getDataDirectory();  
        StatFs stat = new StatFs(path.getPath());  
        long blockSize = stat.getBlockSize();  
        long availableBlocks = stat.getAvailableBlocks();  
        return Formatter.formatFileSize(context, blockSize * availableBlocks);  
    }  
    
    
    public static String getTotalRam(Context context){//GB
        String path = "/proc/meminfo";
        String firstLine = null;
        String totalRamStr = "";
        int totalRam = 0 ;
        try{
            FileReader fileReader = new FileReader(path);
            BufferedReader br = new BufferedReader(fileReader,8192);
            firstLine = br.readLine().split("\\s+")[1];
            br.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        if(firstLine != null){
        	totalRamStr = Formatter.formatFileSize(context, Long.valueOf(firstLine) * 1024);
        	
          //  totalRam = (int)Math.ceil((new Float(Float.valueOf(firstLine) / (1024 * 1024)).doubleValue()));
        }

        //return totalRam + "GB";//返回1GB/2GB/3GB/4GB
        return totalRamStr;
    }
    
    
}  
