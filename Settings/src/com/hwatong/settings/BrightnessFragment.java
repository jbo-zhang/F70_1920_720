/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hwatong.settings;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;

import com.hwatong.providers.carsettings.SettingsProvider;
import com.hwatong.settings.R;

public class BrightnessFragment extends SettingsPreferenceFragment implements SeekBar.OnSeekBarChangeListener, OnClickListener{
    private final String TAG="BrightnessFragment";
    private final boolean DBG=true;
    
    private static final int MINIMUM_BACKLIGHT = 10;
    private static final int MAXIMUM_BACKLIGHT = 255;
    private static final String SCREEN_BRIGHTNESS = "screen_brightness";
    private static final String HUD_BRIGHTNESS = "hud_brightness";
    private static final int MINIMUM_HUDLIGHT = 0;
    private static final int MAXIMUM_HUDLIGHT = 2;

    private SeekBar mSeekBar;
    private SeekBar mHudSeekBar;
    private int mOldBrightness=MAXIMUM_BACKLIGHT;
    private int mOldHudBrightness=MAXIMUM_HUDLIGHT;


    private View mContentView;

    private ContentObserver mBrightnessObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
        	if (DBG) Log.d(TAG, "Brightness onChange; selfChange=" + selfChange);
            onBrightnessChanged();
        }
    };
    private ContentObserver mHudBrightnessObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
        	if (DBG) Log.d(TAG, "HUD Brightness onChange; selfChange=" + selfChange);
            onHudBrightnessChanged();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    
    @Override
	public void onResume() {
		super.onResume();
        getActivity().getContentResolver().registerContentObserver(Utils.CONTENT_URI, true,mBrightnessObserver);
        getActivity().getContentResolver().registerContentObserver(Utils.CONTENT_URI, true,mHudBrightnessObserver);
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
        final ContentResolver resolver = getActivity().getContentResolver();
        resolver.unregisterContentObserver(mBrightnessObserver);
        resolver.unregisterContentObserver(mHudBrightnessObserver);
	}

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mContentView = inflater.inflate(R.layout.preference_dialog_brightness, container, false);  
    	
        mSeekBar = (SeekBar) mContentView.findViewById(R.id.seekbar_brightness);
        mSeekBar.setMax(MAXIMUM_BACKLIGHT - MINIMUM_BACKLIGHT);
    	mOldBrightness = Integer.valueOf(Utils.getCarSettingsString(getActivity().getContentResolver(), SCREEN_BRIGHTNESS, SettingsProvider.DEFAULT_SCREEN_BRIGHTNESS));
        mSeekBar.setProgress(mOldBrightness - MINIMUM_BACKLIGHT);
        mSeekBar.setEnabled(true);
        mSeekBar.setOnSeekBarChangeListener(this);
        ImageButton btnMinus = (ImageButton)mContentView.findViewById(R.id.brightness_minus);
        btnMinus.setOnClickListener(this);
        ImageButton btnPlus = (ImageButton)mContentView.findViewById(R.id.brightness_plus);
        btnPlus.setOnClickListener(this);

        mHudSeekBar = (SeekBar) mContentView.findViewById(R.id.hud_seekbar_brightness);
        mHudSeekBar.setEnabled(true);
        mHudSeekBar.setOnSeekBarChangeListener(this);
        ImageButton btnHudMinus = (ImageButton)mContentView.findViewById(R.id.hud_brightness_minus);
        btnHudMinus.setOnClickListener(this);
        ImageButton btnHudPlus = (ImageButton)mContentView.findViewById(R.id.hud_brightness_plus);
        btnHudPlus.setOnClickListener(this);

        return mContentView;
    }	

    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromTouch) {
        if (!fromTouch) {
            return;
        }

    	switch(seekBar.getId()) {
    	case R.id.seekbar_brightness:
            setBrightness(progress+ MINIMUM_BACKLIGHT);
    		break;
    	case R.id.hud_seekbar_brightness:
            setHudBrightness(progress+ MINIMUM_HUDLIGHT);
    		break;
		default:
			break;
    	}
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        // NA
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        // NA
    }

    private void onBrightnessChanged() {
        int brightness = Integer.valueOf(Utils.getCarSettingsString(getActivity().getContentResolver(), SCREEN_BRIGHTNESS));
        mSeekBar.setProgress(brightness - MINIMUM_BACKLIGHT);
    }
    private void onHudBrightnessChanged() {
        int brightness = Integer.valueOf(Utils.getCarSettingsString(getActivity().getContentResolver(), HUD_BRIGHTNESS));
        mHudSeekBar.setProgress(brightness - MINIMUM_HUDLIGHT);
    }


	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.brightness_minus:
			setProgress(false);
			break;
		case R.id.brightness_plus:
			setProgress(true);
			break;
		case R.id.hud_brightness_minus:
			setHudProgress(false);
			break;
		case R.id.hud_brightness_plus:
			setHudProgress(true);
			break;
		}

	}
	private void setProgress(boolean add) {
		int progress = mSeekBar.getProgress();
		int max = mSeekBar.getMax();
		int step =  max/10;
		if (add) {
			if (progress<max) {
				int value = (progress+step>max)?max:progress+step;
				mSeekBar.setProgress(value);
	            setBrightness(value + MINIMUM_BACKLIGHT);
			}
		}else {
			if (progress>0) {
				int value = (progress-step<0)?0:progress-step;
				mSeekBar.setProgress(value);
	            setBrightness(value + MINIMUM_BACKLIGHT);
			}
		}
	}
	private void setHudProgress(boolean add) {
		int progress = mHudSeekBar.getProgress();
		int max = mHudSeekBar.getMax();
		int step =  1;
		if (add) {
			if (progress<max) {
				int value = (progress+step>max)?max:progress+step;
				mHudSeekBar.setProgress(value);
	            setHudBrightness(value + MINIMUM_HUDLIGHT);
			}
		}else {
			if (progress>0) {
				int value = (progress-step<0)?0:progress-step;
				mHudSeekBar.setProgress(value);
	            setHudBrightness(value + MINIMUM_HUDLIGHT);
			}
		}
	}
    private void setBrightness(int brightness) {
    	Log.d(TAG, "brightness=" + brightness);
		Utils.putCarSettingsString(getActivity().getContentResolver(), SCREEN_BRIGHTNESS, String.valueOf(brightness));
    }
    private void setHudBrightness(int brightness) {
    	Log.d(TAG, "hud_brightness=" + brightness);
		Utils.putCarSettingsString(getActivity().getContentResolver(), HUD_BRIGHTNESS, String.valueOf(brightness));
    }
}

