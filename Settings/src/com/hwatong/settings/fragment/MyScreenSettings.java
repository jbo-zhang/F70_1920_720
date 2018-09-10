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

package com.hwatong.settings.fragment;

import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.hwatong.settings.R;
import com.hwatong.settings.Utils;
import com.hwatong.providers.carsettings.SettingsProvider;

public class MyScreenSettings extends MySettingsPreferenceFragment implements SeekBar.OnSeekBarChangeListener, OnClickListener, OnCheckedChangeListener{
    private final String TAG="MyScreenSettings";
    private final boolean DBG=true;
    
    private static final int FALLBACK_SCREEN_TIMEOUT_VALUE = 10000;
    private static final int NEVERON_SCREEN_TIMEOUT_VALUE = 604800000;
    private static final int MINIMUM_BACKLIGHT = 10;
    private static final int MAXIMUM_BACKLIGHT = 255;
    private static final String SCREEN_BRIGHTNESS = "screen_brightness";
    private static final String HUD_BRIGHTNESS = "hud_brightness";
    private static final int MINIMUM_HUDLIGHT = 0;
    private static final int MAXIMUM_HUDLIGHT = 2;

    private SeekBar mSeekBar;
    private SeekBar mHudSeekBar;
    private TextView mTextView, mHudTextView;
    private Switch mSwitch;
    private Button mRestoreButton;


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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_screen, container, false);  
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

        mSeekBar = (SeekBar) getView().findViewById(R.id.screen_brightness_seekbar);
        mSeekBar.setMax(MAXIMUM_BACKLIGHT - MINIMUM_BACKLIGHT);
        mSeekBar.setOnSeekBarChangeListener(this);
        ImageButton btnMinus = (ImageButton)getView().findViewById(R.id.screen_brightness_minus);
        btnMinus.setOnClickListener(this);
        ImageButton btnPlus = (ImageButton)getView().findViewById(R.id.screen_brightness_plus);
        btnPlus.setOnClickListener(this);
        mTextView = (TextView)getView().findViewById(R.id.screen_brightness_text);

        onBrightnessChanged();

        mHudSeekBar = (SeekBar) getView().findViewById(R.id.screen_hud_seekbar);
        mHudSeekBar.setMax(MAXIMUM_HUDLIGHT - MINIMUM_HUDLIGHT);
        mHudSeekBar.setOnSeekBarChangeListener(this);
        ImageButton btnHudMinus = (ImageButton)getView().findViewById(R.id.screen_hud_minus);
        btnHudMinus.setOnClickListener(this);
        ImageButton btnHudPlus = (ImageButton)getView().findViewById(R.id.screen_hud_plus);
        btnHudPlus.setOnClickListener(this);
        mHudTextView = (TextView)getView().findViewById(R.id.screen_hud_text);

        onHudBrightnessChanged();
        
        final long currentTimeout = Settings.System.getLong(getContentResolver(), SCREEN_OFF_TIMEOUT,NEVERON_SCREEN_TIMEOUT_VALUE);
        mSwitch = (Switch) getView().findViewById(R.id.switch_screen_timeout);
        mSwitch.setChecked(currentTimeout==FALLBACK_SCREEN_TIMEOUT_VALUE);
        mSwitch.setOnCheckedChangeListener(this);
        
        mRestoreButton = (Button) getView().findViewById(R.id.btn_screen_restore);
        mRestoreButton.setOnClickListener(this);
	}

    @Override
	public void onResume() {
		super.onResume();
        getActivity().getContentResolver().registerContentObserver(Uri.withAppendedPath(Utils.CONTENT_URI, SettingsProvider.SCREEN_BRIGHTNESS), true,mBrightnessObserver);
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
        final ContentResolver resolver = getActivity().getContentResolver();
        resolver.unregisterContentObserver(mBrightnessObserver);
        resolver.unregisterContentObserver(mHudBrightnessObserver);
	}

    public void onProgressChanged(SeekBar seekBar, int progress,boolean fromTouch) {
    	switch(seekBar.getId()) {
    	case R.id.screen_brightness_seekbar:
    		if (fromTouch) {
		        mHandler.removeMessages(0);
			    android.os.Message m = mHandler.obtainMessage(0, progress + MINIMUM_BACKLIGHT, 0);
    			mHandler.sendMessageDelayed(m, 200);
    		}
    		break;
    	case R.id.screen_hud_seekbar:
    		if (fromTouch)
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
        mTextView.setText(String.valueOf(brightness));
    }
    private void onHudBrightnessChanged() {
        int brightness = Integer.valueOf(Utils.getCarSettingsString(getActivity().getContentResolver(), HUD_BRIGHTNESS));
        mHudSeekBar.setProgress(brightness - MINIMUM_HUDLIGHT);
        mHudTextView.setText(String.valueOf(brightness));
    }


	@Override
	public void onClick(View v) {
		super.onClick(v);
		
		switch(v.getId()) {
		case R.id.screen_brightness_minus:
			setBrightness(mSeekBar.getProgress() - 1 + MINIMUM_BACKLIGHT);
			break;
		case R.id.screen_brightness_plus:
			setBrightness(mSeekBar.getProgress() + 1 + MINIMUM_BACKLIGHT);
			break;
		case R.id.screen_hud_minus:
			setHudProgress(false);
			break;
		case R.id.screen_hud_plus:
			setHudProgress(true);
			break;
		case R.id.btn_screen_restore:
			Utils.putCarSettingsString(getActivity().getContentResolver(), SCREEN_BRIGHTNESS, SettingsProvider.DEFAULT_SCREEN_BRIGHTNESS);
	        mSwitch.setChecked(false);
			break;
		}

	}

	private static final int[] BRIGHTNESS_VALUE = { 10, 45, 80, 115, 150, 185, 220, 255 };
	
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

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
        	setBrightness(msg.arg1);
        }
    };
    
    private void setBrightness(int brightness) {
        final String s = Utils.getCarSettingsString(getActivity().getContentResolver(), SCREEN_BRIGHTNESS);
        int old = BRIGHTNESS_VALUE[3];
        try {
        	old = Integer.valueOf(s);
        } catch (NumberFormatException e) {
            old = BRIGHTNESS_VALUE[3];
        }
    	Log.d(TAG, "brightness " + old + " -> " + brightness);

		if (brightness > old) {
			int i;
			for (i = 0; i < BRIGHTNESS_VALUE.length; i++) {
				if (brightness <= BRIGHTNESS_VALUE[i]) {
					break;
				}
			}
			if (i == BRIGHTNESS_VALUE.length)
				return;
			brightness = BRIGHTNESS_VALUE[i];
		} else if (brightness < old) {
			int i;
			for (i = BRIGHTNESS_VALUE.length - 1; i >= 0; i--) {
				if (brightness >= BRIGHTNESS_VALUE[i]) {
					break;
				}
			}
			if (i < 0)
				return;
			brightness = BRIGHTNESS_VALUE[i];
		}

        if (brightness != old)
			Utils.putCarSettingsString(getActivity().getContentResolver(), SCREEN_BRIGHTNESS, String.valueOf(brightness));
    }
    
    private void setHudBrightness(int brightness) {
    	Log.d(TAG, "hud_brightness=" + brightness);
		Utils.putCarSettingsString(getActivity().getContentResolver(), HUD_BRIGHTNESS, String.valueOf(brightness));
    }
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        try {
            Settings.System.putInt(getContentResolver(), SCREEN_OFF_TIMEOUT, isChecked?FALLBACK_SCREEN_TIMEOUT_VALUE:NEVERON_SCREEN_TIMEOUT_VALUE);
        } catch (NumberFormatException e) {
            Log.e(TAG, "could not persist screen timeout setting", e);
        }
	}
	
	@Override
	protected int getCurrentId() {return 1;}
}

