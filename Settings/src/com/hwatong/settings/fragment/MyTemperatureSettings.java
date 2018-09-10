/*
 * Copyright (C) 2010 The Android Open Source Project
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
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.hwatong.settings.R;
import com.hwatong.settings.Utils;
import com.hwatong.providers.carsettings.SettingsProvider;

public class MyTemperatureSettings extends MySettingsPreferenceFragment implements RadioGroup.OnCheckedChangeListener{
    private static final String TAG = "MyTemperatureSettings";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        android.util.Log.d("Temp", "onCreateView " + this + ", " + getResources().getConfiguration().locale);
		return inflater.inflate(R.layout.fragment_temperature, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
        android.util.Log.d("Temp", "onActivityCreated " + this + ", " + getResources().getConfiguration().locale);
		super.onActivityCreated(savedInstanceState);
	}

    @Override
    public void onDestroyView() {
        android.util.Log.d("Temp", "onDestroyView " + this + ", " + getResources().getConfiguration().locale);
        super.onDestroyView();
    }

    @Override
    public void onResume() {
    	super.onResume();

        android.util.Log.d("Temp", "onResume " + this + ", " + getResources().getConfiguration().locale);

		final RadioGroup mTemperature = (RadioGroup)getView().findViewById(R.id.rg_temperature);

		mTemperature.setOnCheckedChangeListener(this);
    }
    
    public void onPause() {
        android.util.Log.d("Temp", "onPause " + this + ", " + getResources().getConfiguration().locale);

		final RadioGroup mTemperature = (RadioGroup)getView().findViewById(R.id.rg_temperature);
		mTemperature.setOnCheckedChangeListener(null);

		super.onPause();
    }
    
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch(checkedId) {
		case R.id.rb_centigrade:
			break;
		case R.id.rb_fahrenheit:
			break;
		}
	}

	@Override
	protected int getCurrentId() {return 4;	}

}
