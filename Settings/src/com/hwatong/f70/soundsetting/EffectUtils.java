package com.hwatong.f70.soundsetting;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.hwatong.f70.main.F70Application;
import com.hwatong.f70.main.LogUtils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;


public class EffectUtils {	
	public final static String CUSTOMER = "CUSTOMER";
	public final static String POP = "POP";
	public final static String JAZZ = "JAZZ";
	public final static String CLASSICAL = "CLASSICAL";
	public final static String ROCK = "ROCK";
	public final static String HUMAN = "HUMAN";
	public final static String FLAT = "FLAT";
	
	public final static String POP_VALUE = "2";
	public final static String JAZZ_VALUE = "5";
	public final static String CLASSICAL_VALUE = "1";
	public final static String ROCK_VALUE = "3";
	public final static String HUMAN_VALUE = "6";
	public final static String FLAT_VALUE = "4";
	public final static String CUSTOM_VALUE = "7";
	
	public final static int[] POP_EFFECT = {7, -1, 0, 0, 5};
	public final static int[] JAZZ_EFFECT = {4, -2, 2, 2, 2};
	public final static int[] CLASSICAL_EFFECT = {12, 0, 0, 0, 8};
	public final static int[] ROCK_EFFECT = {10, 0, 0, 2, 1};
	public final static int[] HUMAN_EFFECT = {0, 2, 4, 4, 0};
	public final static int[] FLAT_EFFECT = {0, 0, 0, 0, 0};
	
	public static final int BASS_POSITON = 0;
	public static final int MID_POSITON = 1;
	public static final int TREBLE_POSITON = 2;
	public static final int CUSTOMER_DEFALUT_VALUE = 12;
	
//	private static final int[] EFFECT_POP = { -100, 200, 500, 100, -200 };
//	private static final int[] EFFECT_JAZZ = { 400, 200, -200, 200, 500 };
//	private static final int[] EFFECT_CLASSICAL = { 500, 300, -200, 400, 400};
//	private static final int[] EFFECT_ROCK = { 500, 300, -100, 300, 500 };
//	private static final int[] EFFECT_HUMAN = {100, 200, 900, 600, -200};
//	private static final int[] EFFECT_FLAT = { 0, 0, 0, 0, 0 };
	
	private static final int[] EFFECT_POP = { -1, 5, -2 };
	private static final int[] EFFECT_JAZZ = { 4, -2, 5 };
	private static final int[] EFFECT_CLASSICAL = { 5, -2, 4};
	private static final int[] EFFECT_ROCK = { 5, -1, 5 };
	private static final int[] EFFECT_HUMAN = {1, 7, -2};
	private static final int[] EFFECT_FLAT = { 0, 0, 0 };
	
	/**
	 *
	 */
	private final static String EQ_FILE_POSITION = "/sys/bus/i2c/devices/0-0063/";//闄嗚劮纰岃尗鑴楄矾鎴湶
	
    private final static String BAND1_POSITION = "/sys/bus/i2c/devices/0-0063/band1_func";
    private final static String BAND2_POSITION = "/sys/bus/i2c/devices/0-0063/band2_func";
    private final static String BAND3_POSITION = "/sys/bus/i2c/devices/0-0063/band3_func";
    private final static String BAND4_POSITION = "/sys/bus/i2c/devices/0-0063/band4_func";
    private final static String BAND5_POSITION = "/sys/bus/i2c/devices/0-0063/band5_func";
    
	private final static String BALANCE_FUNC = "balance_func";
	private final static String FADER_FUNC = "fader_func";
	private final static String BASS_FUNC = "bass_func";
	private final static String MID_FUNC = "mid_func";
	private final static String TREBLE_FUNC = "treble_func";
	private final static String LOUDNESS_SWITCH_FUNC = "loudness_switch_func";
	private final static String QE_FUNC = "qe_func";
	
	public static String getBassValue() {
		String path = EQ_FILE_POSITION + BASS_FUNC;
		LogUtils.d("bass path: " + path);
		return getFileInfo(path);
	}
	
	public static String getMidValue() {
		String path = EQ_FILE_POSITION + MID_FUNC;
		LogUtils.d("mid path: " + path);
		return getFileInfo(path);
	}
	public static String getTrebleValue() {
		String path = EQ_FILE_POSITION + TREBLE_FUNC;
		LogUtils.d("treble path: " + path);
		return getFileInfo(path);
	}
	public static String getBalanceValue() {
		String path = EQ_FILE_POSITION + BALANCE_FUNC;
		LogUtils.d("balance path: " + path);
		return getFileInfo(path);
	}
	public static String getFaderValue() {
		String path = EQ_FILE_POSITION + FADER_FUNC;
		LogUtils.d("fader path: " + path);
		return getFileInfo(path);
	}
	public static String getLoudnessValue() {
		String path = EQ_FILE_POSITION + LOUDNESS_SWITCH_FUNC;
		LogUtils.d("fader path: " + path);
		return getFileInfo(path);
	}
	
	public static boolean setBassValue(int value) {
		String path = EQ_FILE_POSITION + BASS_FUNC;
		return writeDataToFile(path, String.valueOf(value));
	}
	public static boolean setMidValue(int value) {
		String path = EQ_FILE_POSITION + MID_FUNC;
		return writeDataToFile(path, String.valueOf(value));
	}
	public static boolean setTrebleValue(int value) {
		String path = EQ_FILE_POSITION + TREBLE_FUNC;
		return writeDataToFile(path, String.valueOf(value));
	}
	public static boolean setBalanceValue(int value) {
		String path = EQ_FILE_POSITION + BALANCE_FUNC;
		return writeDataToFile(path, String.valueOf(value));
	}
	public static boolean setFaderValue(int value) {
		String path = EQ_FILE_POSITION + FADER_FUNC;
		return writeDataToFile(path, String.valueOf(value));
	}
	public static boolean setLoudnessValue(int value) {
		String path = EQ_FILE_POSITION + LOUDNESS_SWITCH_FUNC;
		return writeDataToFile(path, String.valueOf(value));
	}
	
	
	public static int[] getEffect(String mode) {
		if(mode.equals(POP))
			return EFFECT_POP;
		else if(mode.equals(JAZZ))
			return EFFECT_JAZZ;
		else if(mode.equals(CLASSICAL))
			return EFFECT_CLASSICAL;
		else if(mode.equals(ROCK))
			return EFFECT_ROCK;
		else if(mode.equals(HUMAN))
			return EFFECT_HUMAN;
		else if(mode.equals(FLAT))
			return EFFECT_FLAT;
		else
			return EFFECT_FLAT;
	}
		
	/**
	 *
	 * @param con
	 * @param a
	 */
	public static void setCustomTreble(int a) {
		SharedPreferences pref = F70Application.getInstance().getSharedPreferences("CUSTOMER", Context.MODE_PRIVATE);
		pref.edit().putInt("treble", a).apply();
	}
	
	public static int getCustomTreble() {
		SharedPreferences pref = F70Application.getInstance().getSharedPreferences("CUSTOMER", Context.MODE_PRIVATE);
		return pref.getInt("treble", 0);
	}
	public static void setCustomMid(int b) {
		SharedPreferences pref = F70Application.getInstance().getSharedPreferences("CUSTOMER", Context.MODE_PRIVATE);
		pref.edit().putInt("mid", b).apply();
	}
	public static int getCustomMid() {
		SharedPreferences pref = F70Application.getInstance().getSharedPreferences("CUSTOMER", Context.MODE_PRIVATE);
		return pref.getInt("mid", 0);
	}
	public static void setCustomBass(int b) {
		SharedPreferences pref = F70Application.getInstance().getSharedPreferences("CUSTOMER", Context.MODE_PRIVATE);
		pref.edit().putInt("bass", b).apply();
	}
	public static int getCustomBass() {
		SharedPreferences pref = F70Application.getInstance().getSharedPreferences("CUSTOMER", Context.MODE_PRIVATE);
		return pref.getInt("bass", 0);
	}
	
	/**
	 *
	 */
	public static void setCurrentBass(int value) {
		SharedPreferences pref = F70Application.getInstance().getSharedPreferences("CURRENT_COMPLETE_EFFECT", Context.MODE_PRIVATE);
		pref.edit().putInt("bass", value).apply();
	}
	public static void setCurrentMid(int value) {
		SharedPreferences pref = F70Application.getInstance().getSharedPreferences("CURRENT_COMPLETE_EFFECT", Context.MODE_PRIVATE);
		pref.edit().putInt("mid", value).apply();
	}
	public static void setCurrentTreble(int value) {
		SharedPreferences pref = F70Application.getInstance().getSharedPreferences("CURRENT_COMPLETE_EFFECT", Context.MODE_PRIVATE);
		pref.edit().putInt("treble", value).apply();
	}
	public static void setCurrentBalance(int value) {
		SharedPreferences pref = F70Application.getInstance().getSharedPreferences("CURRENT_COMPLETE_EFFECT", Context.MODE_PRIVATE);
		pref.edit().putInt("balance", value).apply();
	}
	public static void setCurrentFader(int value) {
		SharedPreferences pref = F70Application.getInstance().getSharedPreferences("CURRENT_COMPLETE_EFFECT", Context.MODE_PRIVATE);
		pref.edit().putInt("fader", value).apply();
	}
	
	public static void setCurrentEqAnother(String value) {
		String typeValue = "";
		if(value.equals(POP))
			typeValue = POP;
		else if(value.equals(JAZZ))
			typeValue = JAZZ;
		else if(value.equals(CLASSICAL))
			typeValue = CLASSICAL;
		else if(value.equals(ROCK))
			typeValue = ROCK;
		else if(value.equals(HUMAN))
			typeValue = HUMAN;
		else if(value.equals(CUSTOMER))
			typeValue = CUSTOMER;
		else
			typeValue = FLAT_VALUE;
		LogUtils.d("setCurrentEqAnother typeValue: " + typeValue);
		SharedPreferences pref = F70Application.getInstance().getSharedPreferences("CURRENT_COMPLETE_EQ", Context.MODE_PRIVATE);
		pref.edit().putString("another_eq", typeValue).apply();		
	}
	
	public static void setCurrentLoudness(int value) {
		SharedPreferences pref = F70Application.getInstance().getSharedPreferences("CURRENT_COMPLETE_EFFECT", Context.MODE_PRIVATE);
		pref.edit().putInt("loudness", value).commit();
	}
	
	public static int getCurrentBass() {
		SharedPreferences pref = F70Application.getInstance().getSharedPreferences("CURRENT_COMPLETE_EFFECT", Context.MODE_PRIVATE);
		return pref.getInt("bass", 0);
	}
	public static int getCurrentMid() {
		SharedPreferences pref = F70Application.getInstance().getSharedPreferences("CURRENT_COMPLETE_EFFECT", Context.MODE_PRIVATE);
		return pref.getInt("mid", 0);
	}
	public static int getCurrentTreble() {
		SharedPreferences pref = F70Application.getInstance().getSharedPreferences("CURRENT_COMPLETE_EFFECT", Context.MODE_PRIVATE);
		return pref.getInt("treble", 0);
	}
	public static int getCurrentBalance() {
		SharedPreferences pref = F70Application.getInstance().getSharedPreferences("CURRENT_COMPLETE_EFFECT", Context.MODE_PRIVATE);
		return pref.getInt("balance", 0);
	}
	public static int getCurrentFader() {
		SharedPreferences pref = F70Application.getInstance().getSharedPreferences("CURRENT_COMPLETE_EFFECT", Context.MODE_PRIVATE);
		return pref.getInt("fader", 0);
	}
	
	public static String getCurrentEqAnother() {
		SharedPreferences pref = F70Application.getInstance().getSharedPreferences("CURRENT_COMPLETE_EQ", Context.MODE_PRIVATE);
		return pref.getString("another_eq", FLAT);		
	}
	
	public static int getCurrentLoudness(Context context) {
		SharedPreferences pref = context.getSharedPreferences("CURRENT_COMPLETE_EFFECT", Context.MODE_PRIVATE);
		return pref.getInt("loudness", 0);	
	}
	
	/**
	 * band5
	 */
	public static void setBand1(Context context, int value) {
		SharedPreferences pref = context.getSharedPreferences("QE_EFFECT", Context.MODE_PRIVATE);
		pref.edit().putInt("band1", value).apply();
	}
	
	public static int getBand1(Context context) {
		SharedPreferences pref = context.getSharedPreferences("QE_EFFECT", Context.MODE_PRIVATE);
		return pref.getInt("band1", CUSTOMER_DEFALUT_VALUE);	
	}
	
	public static void setBand2(Context context, int value) {
		SharedPreferences pref = context.getSharedPreferences("QE_EFFECT", Context.MODE_PRIVATE);
		pref.edit().putInt("band2", value).apply();
	}
	
	public static int getBand2(Context context) {
		SharedPreferences pref = context.getSharedPreferences("QE_EFFECT", Context.MODE_PRIVATE);
		return pref.getInt("band2", CUSTOMER_DEFALUT_VALUE);	
	}
	
	public static void setBand3(Context context, int value) {
		SharedPreferences pref = context.getSharedPreferences("QE_EFFECT", Context.MODE_PRIVATE);
		pref.edit().putInt("band3", value).apply();
	}
	
	public static int getBand3(Context context) {
		SharedPreferences pref = context.getSharedPreferences("QE_EFFECT", Context.MODE_PRIVATE);
		return pref.getInt("band3", CUSTOMER_DEFALUT_VALUE);	
	}
	
	public static void setBand4(Context context, int value) {
		SharedPreferences pref = context.getSharedPreferences("QE_EFFECT", Context.MODE_PRIVATE);
		pref.edit().putInt("band4", value).apply();
	}
	
	public static int getBand4(Context context) {
		SharedPreferences pref = context.getSharedPreferences("QE_EFFECT", Context.MODE_PRIVATE);
		return pref.getInt("band4", CUSTOMER_DEFALUT_VALUE);	
	}
	
	public static void setBand5(Context context, int value) {
		SharedPreferences pref = context.getSharedPreferences("QE_EFFECT", Context.MODE_PRIVATE);
		pref.edit().putInt("band5", value).apply();
	}
	
	public static int getBand5(Context context) {
		SharedPreferences pref = context.getSharedPreferences("QE_EFFECT", Context.MODE_PRIVATE);
		return pref.getInt("band5", CUSTOMER_DEFALUT_VALUE);	
	}
	
	public static void setBand1ToFile(int value) {
		LogUtils.d("setBand1ToFile: " + String.valueOf(value / 2));
		writeDataToFile(BAND1_POSITION, String.valueOf(value / 2));
	}
	public static void setBand2ToFile(int value) {
		writeDataToFile(BAND2_POSITION, String.valueOf(value / 2));
	}
	public static void setBand3ToFile(int value) {
		writeDataToFile(BAND3_POSITION, String.valueOf(value / 2));
	}
	public static void setBand4ToFile(int value) {
		writeDataToFile(BAND4_POSITION, String.valueOf(value / 2));
	}
	public static void setBand5ToFile(int value) {
		writeDataToFile(BAND5_POSITION, String.valueOf(value / 2));
	}
	
	/**
	 * 鑴＄尗鑴拌剻鑴箞鑴ㄦ悅鑴涙嫝鑴㈤檰鎷㈠崲鑴瘺鑴拌劥纰岃劌鑴箞鑴熼叾璺劙椹撮檵
	 */
	public static void setEqAnother(String type) {
		String path = EQ_FILE_POSITION + QE_FUNC;
		String typeValue = "";
		if(type.equals(POP))
			typeValue = POP_VALUE;
		else if(type.equals(JAZZ))
			typeValue = JAZZ_VALUE;
		else if(type.equals(CLASSICAL))
			typeValue = CLASSICAL_VALUE;
		else if(type.equals(ROCK))
			typeValue = ROCK_VALUE;
		else if(type.equals(HUMAN))
			typeValue = HUMAN_VALUE;
		else
			typeValue = FLAT_VALUE;
		writeDataToFile(path, typeValue);
	}
	
	public static void setEqCustomer(Context context) {
		EffectUtils.setBand1ToFile(getBand1(context));
		EffectUtils.setBand2ToFile(getBand2(context));
		EffectUtils.setBand3ToFile(getBand3(context));
		EffectUtils.setBand4ToFile(getBand4(context));
		EffectUtils.setBand5ToFile(getBand5(context));
	}
		
	public static int[] getEqTypeAnother(String type) {
		if(type.equals(POP))
			return POP_EFFECT;
		else if(type.equals(JAZZ))
			return JAZZ_EFFECT;
		else if(type.equals(CLASSICAL))
			return CLASSICAL_EFFECT;
		else if(type.equals(ROCK))
			return ROCK_EFFECT;
		else if(type.equals(HUMAN))
			return HUMAN_EFFECT;
		else
			return FLAT_EFFECT;
	}
	
	public static String getEqTypeAnother() {
		String path = EQ_FILE_POSITION + QE_FUNC;
		return getFileInfo(path);
//		return "6";
	}
	
	public static String getFileBand1() {
		return getFileInfo(BAND1_POSITION);
	}
	public static String getFileBand2() {
		return getFileInfo(BAND2_POSITION);
	}
	public static String getFileBand3() {
		return getFileInfo(BAND3_POSITION);
	}
	public static String getFileBand4() {
		return getFileInfo(BAND4_POSITION);
	}
	public static String getFileBand5() {
		return getFileInfo(BAND5_POSITION);
	}
	
	public static final Uri CONTENT_URI = Uri.parse("content://car_settings/content");
	public static final String EQUALIZER_MODE = "equalizer_mode";
    public static final Uri uri = Uri.withAppendedPath(CONTENT_URI, EQUALIZER_MODE);
    
	public static String getCarSettingsString(ContentResolver cr, String name) {
		String[] select = new String[] { "value" };
		Cursor cursor = cr.query(CONTENT_URI, select, "name=?", new String[]{ name }, null);
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
		Cursor cursor = cr.query(CONTENT_URI, select, "name=?", new String[]{ name }, null);
		String value = defaultValue;
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				value = cursor.getString(0);
			}
			cursor.close();
		}
		return value;
	}

	public static boolean putCarSettingsString(ContentResolver cr, String name, String value) {
		String[] select = new String[] { "value" };
		Cursor cursor = cr.query(CONTENT_URI, select, "name=?", new String[]{ name }, null);
		if (cursor != null && cursor.moveToFirst()) {
			if (cursor != null)
				cursor.close();
			ContentValues values = new ContentValues();
			values.put("value", value);
			cr.update(CONTENT_URI, values, "name=?", new String[]{ name });
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
	
	/**
	 *FileWriter
	 * 
	 * @param fileName
	 * @param content
	 */
	private static boolean writeDataToFile(String fileName, String content) {
		LogUtils.d("writeDataToFile");
		try {
			FileWriter writer = new FileWriter(fileName, false);
			writer.write(content);
			writer.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			LogUtils.d("writeDataToFile error: " + e.toString());
		}
		return false;
	}
	
	/**
	 *
	 * @return
	 */
	private static String getFileInfo(String filePath) {
        try {
            FileReader fr = new FileReader(filePath);
            BufferedReader br = new BufferedReader(fr);
            String tmp = br.readLine();
            LogUtils.d(filePath + ":" + tmp);
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
		return null;
	}
	
    //记忆本地热点打开状态
    public static void memoryApstate(Context context, boolean apState) {
    	int state = apState ? 1 : 0;
    	SharedPreferences sp = context.getSharedPreferences("wifi_ap_state", Context.MODE_PRIVATE);
    	sp.edit().putInt("ap_state", state).apply();
    }
    
    public static int getMemoryApState(Context context) {
    	SharedPreferences sp = context.getSharedPreferences("wifi_ap_state", Context.MODE_PRIVATE);
    	return sp.getInt("ap_state", -1);
    }

}
