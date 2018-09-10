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

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.Switch;

import com.hwatong.settings.R;
import com.hwatong.settings.SettingsPreferenceFragment;
import com.hwatong.settings.Utils;
import com.hwatong.providers.carsettings.SettingsProvider;
import com.hwatong.settings.view.ColorSeekBar;

public class MyAtmosphereLampStaticFragment extends SettingsPreferenceFragment implements OnCheckedChangeListener, ColorSeekBar.ColorChangeListener{
	private static final String TAG = "MyAtmosphereLampStaticFragment";

	private View mContentView;

	private Switch mMonoStaticSwitch,mMonoRespirationSwitch,mDiscRespirationSwitch;
	private ColorSeekBar mSeekBar;
	private ImageView imageSampingColor;
	private GradientDrawable gradientSampingColor;
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void initUI() {
		mMonoStaticSwitch = (Switch) mContentView.findViewById(R.id.switch_monochrome_static);
		mMonoStaticSwitch.setOnCheckedChangeListener(this);
		mMonoRespirationSwitch = (Switch) mContentView.findViewById(R.id.switch_monochrome_respiration);
		mMonoRespirationSwitch.setOnCheckedChangeListener(this);
		mDiscRespirationSwitch = (Switch) mContentView.findViewById(R.id.switch_discolored_respiration);
		mDiscRespirationSwitch.setOnCheckedChangeListener(this);
		mSeekBar = (ColorSeekBar) mContentView.findViewById(R.id.seekbar_sampling_color);
		imageSampingColor = (ImageView) mContentView.findViewById(R.id.iv_sampling_color);
		gradientSampingColor = (GradientDrawable)imageSampingColor.getBackground();
		mSeekBar.setOnColorChangerListener(this);
		refreshSwitch();
		refreshProgress();
	}
	private boolean fromTouch=true;
	private void refreshSwitch() {
		fromTouch = false;
		fromTouch = true;
	}
	private void refreshProgress() {
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
		case R.id.switch_monochrome_static:
			break;
		case R.id.switch_monochrome_respiration:
			break;
		case R.id.switch_discolored_respiration:
			break;
		}
		refreshSwitch();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mContentView = inflater.inflate(R.layout.fragment_atmospherelamp_static, container, false);
		initUI();
		return mContentView;
	}

	@Override
	public void colorChange(int color, float rate) {
		gradientSampingColor.setColor(color);
	}
}
