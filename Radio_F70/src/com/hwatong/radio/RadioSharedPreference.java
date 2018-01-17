package com.hwatong.radio;

import android.content.Context;
import android.content.SharedPreferences;

import com.hwatong.utils.L;

public class RadioSharedPreference {

	private static final String thiz = RadioSharedPreference.class.getSimpleName();
	
	private static final String SP_NAME = "radio";

	public static final String KEY_FM1_COLLECT = "key_fm1_collect";
	public static final String KEY_FM2_COLLECT = "key_fm2_collect";
	public static final String KEY_FM3_COLLECT = "key_fm3_collect";
	public static final String KEY_AM1_COLLECT = "key_am1_collect";
	public static final String KEY_AM2_COLLECT = "key_am2_collect";

	public static final String FM_POS_1 = "fm_pos_1";
	public static final String FM_POS_2 = "fm_pos_2";
	public static final String FM_POS_3 = "fm_pos_3";
	public static final String FM_POS_4 = "fm_pos_4";
	public static final String FM_POS_5 = "fm_pos_5";
	public static final String FM_POS_6 = "fm_pos_6";
	public static final String FM_POS_7 = "fm_pos_7";
	public static final String FM_POS_8 = "fm_pos_8";
	public static final String FM_POS_9 = "fm_pos_9";
	public static final String FM_POS_10 = "fm_pos_10";
	public static final String FM_POS_11 = "fm_pos_11";
	public static final String FM_POS_12 = "fm_pos_12";
	public static final String FM_POS_13 = "fm_pos_13";
	public static final String FM_POS_14 = "fm_pos_14";
	public static final String FM_POS_15 = "fm_pos_15";
	public static final String FM_POS_16 = "fm_pos_16";
	public static final String FM_POS_17 = "fm_pos_17";
	public static final String FM_POS_18 = "fm_pos_18";

	public static final String AM_POS_1 = "am_pos_1";
	public static final String AM_POS_2 = "am_pos_2";
	public static final String AM_POS_3 = "am_pos_3";
	public static final String AM_POS_4 = "am_pos_4";
	public static final String AM_POS_5 = "am_pos_5";
	public static final String AM_POS_6 = "am_pos_6";
	public static final String AM_POS_7 = "am_pos_7";
	public static final String AM_POS_8 = "am_pos_8";
	public static final String AM_POS_9 = "am_pos_9";
	public static final String AM_POS_10 = "am_pos_10";
	public static final String AM_POS_11 = "am_pos_11";
	public static final String AM_POS_12 = "am_pos_12";
	
	public static final String BAND_KEY = "band";
	public static final String FM_FREQ = "fm_freq";
	public static final String AM_FREQ = "am_freq";
	

	private String[] fmKeyArray = new String[] { "fm_pos_1", "fm_pos_2",
			"fm_pos_3", "fm_pos_4", "fm_pos_5", "fm_pos_6", "fm_pos_7",
			"fm_pos_8", "fm_pos_9", "fm_pos_10", "fm_pos_11", "fm_pos_12",
			"fm_pos_13", "fm_pos_14", "fm_pos_15", "fm_pos_16", "fm_pos_17",
			"fm_pos_18" };
	private String[] amKeyArray = new String[] { "am_pos_1", "am_pos_2",
			"am_pos_3", "am_pos_4", "am_pos_5", "am_pos_6", "am_pos_7",
			"am_pos_8", "am_pos_9", "am_pos_10", "am_pos_11", "am_pos_12" };

	public static final String KEY_LAST_MEMORY = "key_last_memory";

	private SharedPreferences mSp;

	public RadioSharedPreference(Context context) {
		mSp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
	}
	
	/**
	 * 获取fm所收藏的对应位置的电台
	 * @param position
	 * @return
	 */
	public int getFmPosFreq(int position) {
		return getPosFreq(fmKeyArray, position);
	}
	
	/**
	 * 获取am所收藏的对应位置的电台
	 * @param position
	 * @return
	 */
	public int getAmPosFreq(int position) {
		return getPosFreq(amKeyArray, position);
	}
	
	/**
	 * private 用于getFmPosFreq和getAmPosFreq;
	 * @param array
	 * @param position
	 * @return
	 */
	private int getPosFreq(String[] array, int position) {
		if(position < 0 || position > array.length) {
			return 0;
		}
		return mSp.getInt(array[position], 0);
	}
	
	/**
	 * 收藏FM对应位置频率
	 * @param position
	 * @param freq
	 */
	public void setFmPosFreq(int position, int freq) {
		setPosFreq(fmKeyArray, position, freq);
	}
	
	/**
	 * 收藏AM对应位置频率
	 * @param position
	 * @param freq
	 */
	public void setAmPosFreq(int position, int freq) {
		setPosFreq(amKeyArray, position, freq);
	}
	
	/**
	 * private 用于setFmPosFreq和setAmPosFreq;
	 * @param array
	 * @param position
	 * @param freq
	 */
	private void setPosFreq(String[] array, int position, int freq) {
		mSp.edit().putInt(array[position], freq).commit();
	}

	public void saveFmFreq(int freq) {
		mSp.edit().putInt(FM_FREQ, freq).commit();
	}
	
	public void saveAmFreq(int freq) {
		mSp.edit().putInt(AM_FREQ, freq).commit();
	}
	
	public int getFmFreq() {
		return mSp.getInt(FM_FREQ, 8750);
	}
	
	public int getAmFreq() {
		return mSp.getInt(AM_FREQ, 531);
	}
	
	
	public boolean isInit() {
		if(mSp.getBoolean("is_init", true)) {
			mSp.edit().putBoolean("is_init", false).commit();
			return true;
		} 
		return false;
	}
	
	public boolean isFMInit() {
		if(mSp.getBoolean("is_init_fm", true)) {
			mSp.edit().putBoolean("is_init_fm", false).commit();
			L.d(thiz, "isFMInit return true");
			return true;
		} 
		L.d(thiz, "isFMInit return false");
		return false;
	}
	
	public boolean isAMInit() {
		if(mSp.getBoolean("is_init_am", true)) {
			mSp.edit().putBoolean("is_init_am", false).commit();
			L.d(thiz, "isAMInit return true;");
			return true;
		} 
		L.d(thiz, "isAMInit return false;");
		return false;
	}
	
	
	
}
