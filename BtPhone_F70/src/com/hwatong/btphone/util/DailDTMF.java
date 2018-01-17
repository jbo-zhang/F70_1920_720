package com.hwatong.btphone.util;

import java.util.HashMap;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.provider.Settings;

public class DailDTMF {

	private final static String thiz = DailDTMF.class.getSimpleName();
	
	private static final HashMap<Character, Integer> mToneMap = new HashMap<Character, Integer>();
	static {
		mToneMap.put('0', ToneGenerator.TONE_DTMF_0);
		mToneMap.put('1', ToneGenerator.TONE_DTMF_1);
		mToneMap.put('2', ToneGenerator.TONE_DTMF_2);
		mToneMap.put('3', ToneGenerator.TONE_DTMF_3);
		mToneMap.put('4', ToneGenerator.TONE_DTMF_4);
		mToneMap.put('5', ToneGenerator.TONE_DTMF_5);
		mToneMap.put('6', ToneGenerator.TONE_DTMF_6);
		mToneMap.put('7', ToneGenerator.TONE_DTMF_7);
		mToneMap.put('8', ToneGenerator.TONE_DTMF_8);
		mToneMap.put('9', ToneGenerator.TONE_DTMF_9);
		mToneMap.put('*', ToneGenerator.TONE_DTMF_S);
		mToneMap.put('#', ToneGenerator.TONE_DTMF_P);
		mToneMap.put('+', ToneGenerator.TONE_DTMF_S);
	}

	private Context mContext;

	private static final int DTMF_DURATION_MS = 120;

	private Object mToneGeneratorLock = new Object();
	private ToneGenerator mToneGenerator;
	private static boolean mDTMFToneEnabled;

	public DailDTMF(Context contex) {

		try {
			mDTMFToneEnabled = Settings.System.getInt(contex.getContentResolver(),
					Settings.System.DTMF_TONE_WHEN_DIALING, 1) == 1;
			synchronized (mToneGeneratorLock) {
				if (mDTMFToneEnabled && mToneGenerator == null) {
					mToneGenerator = new ToneGenerator(AudioManager.STREAM_RING, 80);
				}
			}
		} catch (Exception e) {
			mDTMFToneEnabled = false;
			mToneGenerator = null;
		}

		mContext = contex;
	}

	public void playTone(Character c) {
		if(!mDTMFToneEnabled)
			return;

		if(mToneMap.get(c) == null)
			return;
		AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		int ringerMode = audioManager.getRingerMode();
		if ((ringerMode == AudioManager.RINGER_MODE_SILENT)
				|| (ringerMode == AudioManager.RINGER_MODE_VIBRATE)) {
			return;
		}

		synchronized (mToneGeneratorLock) {
			if (mToneGenerator == null) {
				return;
			}

			int tone = mToneMap.get(c);
			L.d(thiz, "startTone() tone 2 : " + tone);
			mToneGenerator.startTone(tone, DTMF_DURATION_MS);
		}
	}

	public void destory() {
		synchronized (mToneGeneratorLock) {
			if (mToneGenerator != null) {
				mToneGenerator.release();
				mToneGenerator = null;
			}
		}
		mContext = null;
	}
}
