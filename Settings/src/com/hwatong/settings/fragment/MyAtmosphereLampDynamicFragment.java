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

public class MyAtmosphereLampDynamicFragment extends SettingsPreferenceFragment implements  OnCheckedChangeListener, OnClickListener, SeekBar.OnSeekBarChangeListener{
	private static final String TAG = "MyAtmosphereLampDynamicFragment";

	private View mContentView;

	private Switch mCloseSwitch;
	private SeekBar mSeekBar;
	private TextView mTextView;

    private static final int MINIMUM_SPEED = 0;
    private static final int MAXIMUM_SPEED = 5;
    private int mOldSpeed=MAXIMUM_SPEED;
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void initUI() {
		mCloseSwitch = (Switch) mContentView.findViewById(R.id.switch_dynamic_close);
		mCloseSwitch.setOnCheckedChangeListener(this);

		mSeekBar = (SeekBar) mContentView.findViewById(R.id.atmospherelamp_dynamic_speed_seekbar);
        mSeekBar.setMax(MAXIMUM_SPEED - MINIMUM_SPEED);
        mSeekBar.setProgress(mOldSpeed - MINIMUM_SPEED);
		mSeekBar.setOnSeekBarChangeListener(this);
		mTextView = (TextView) mContentView.findViewById(R.id.atmospherelamp_dynamic_speed_text);
		mContentView.findViewById(R.id.atmospherelamp_dynamic_speed_minus).setOnClickListener(this);
		mContentView.findViewById(R.id.atmospherelamp_dynamic_speed_plus).setOnClickListener(this);
		if (mTextView!=null) mTextView.setText(String.valueOf(mOldSpeed - MINIMUM_SPEED));
		
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
		switch(buttonView.getId()) {
		case R.id.switch_dynamic_close:
			break;

		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mContentView = inflater.inflate(R.layout.fragment_atmospherelamp_dynamic, container, false);
		initUI();
		return mContentView;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.atmospherelamp_dynamic_speed_minus:
			setMyProgress(0,false);
			break;
		case R.id.atmospherelamp_dynamic_speed_plus:
			setMyProgress(0,true);
			break;
		}		
	}
	private void setMyProgress(int index, boolean add) {
		int progress = mSeekBar.getProgress();
		int max = mSeekBar.getMax();
		int step =  max/MAXIMUM_SPEED;
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
		if (fromUser) {
		}
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
