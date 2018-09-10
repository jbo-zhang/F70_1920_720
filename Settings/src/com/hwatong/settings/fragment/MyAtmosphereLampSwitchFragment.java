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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.hwatong.settings.R;
import com.hwatong.settings.SettingsPreferenceFragment;
import com.hwatong.settings.Utils;
import com.hwatong.providers.carsettings.SettingsProvider;

public class MyAtmosphereLampSwitchFragment extends SettingsPreferenceFragment implements OnCheckedChangeListener, OnClickListener, SeekBar.OnSeekBarChangeListener{
	private static final String TAG = "MyAtmosphereLampSwitchSettings";

	private View mContentView;

	private Switch mCloseSwitch,mBacklightControlSwitch,mForceSwitch;
	private SeekBar mSeekBar;
	private TextView mTextView;

    private static final int MINIMUM_BACKLIGHT = 0;
    private static final int MAXIMUM_BACKLIGHT = 7;
	private int mOldBrightness=MAXIMUM_BACKLIGHT;

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void initUI() {
		mCloseSwitch = (Switch) mContentView.findViewById(R.id.switch_close);
		mCloseSwitch.setOnCheckedChangeListener(this);
		mBacklightControlSwitch = (Switch) mContentView.findViewById(R.id.switch_backlight_control);
		mBacklightControlSwitch.setOnCheckedChangeListener(this);
		mForceSwitch = (Switch) mContentView.findViewById(R.id.switch_force);
		mForceSwitch.setOnCheckedChangeListener(this);
		mSeekBar = (SeekBar) mContentView.findViewById(R.id.atmospherelamp_brightness_seekbar);
        mSeekBar.setMax(MAXIMUM_BACKLIGHT - MINIMUM_BACKLIGHT);
        mSeekBar.setProgress(mOldBrightness - MINIMUM_BACKLIGHT);
		mSeekBar.setOnSeekBarChangeListener(this);
		mTextView = (TextView) mContentView.findViewById(R.id.atmospherelamp_brightness_text);
		mContentView.findViewById(R.id.atmospherelamp_brightness_minus).setOnClickListener(this);
		mContentView.findViewById(R.id.atmospherelamp_brightness_plus).setOnClickListener(this);
		
		if (mTextView!=null) mTextView.setText(String.valueOf(mOldBrightness - MINIMUM_BACKLIGHT));
		refreshSwitch();
	}

	private boolean fromTouch=true;
	private void refreshSwitch() {
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (!fromTouch) return;
		switch(buttonView.getId()) {
		case R.id.switch_close:
			break;
		case R.id.switch_backlight_control:
			break;
		case R.id.switch_force:
			break;
		}
		refreshSwitch();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mContentView = inflater.inflate(R.layout.fragment_atmospherelamp_switch, container, false);
		initUI();
		return mContentView;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.atmospherelamp_brightness_minus:
			setMyProgress(0,false);
			break;
		case R.id.atmospherelamp_brightness_plus:
			setMyProgress(0,true);
			break;
		}		
	}
	private void setMyProgress(int index, boolean add) {
		int progress = mSeekBar.getProgress();
		int max = mSeekBar.getMax();
		int step =  max/10;
		if (step<1) step=1;
		if (add) {
			if (progress<max) {
				int value = (progress+step>max)?max:progress+step;
				mSeekBar.setProgress(value);
				if (mTextView!=null) mTextView.setText(String.valueOf(value));
			}
		}else {
			if (progress>0) {
				int value = (progress-step<0)?0:progress-step;
				mSeekBar.setProgress(value);
				if (mTextView!=null) mTextView.setText(String.valueOf(value));
			}
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		mTextView.setText(String.valueOf(progress));
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
}
