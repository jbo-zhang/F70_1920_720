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

import java.util.Locale;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.android.internal.app.LocalePicker;
import com.hwatong.settings.R;

public class MyLanguageSettings extends MySettingsPreferenceFragment implements RadioGroup.OnCheckedChangeListener{
    private static final String TAG = "MyLanguageSettings";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_language, container, false);
	}

    @Override
    public void onResume() {
    	super.onResume();

		final RadioGroup mLanguage = (RadioGroup)getView().findViewById(R.id.rg_language);
        final Locale loc = getResources().getConfiguration().locale;
        mLanguage.check(Locale.US.getLanguage().equals(loc.getLanguage()) ? R.id.rb_language_english : R.id.rb_language_chinese);
		mLanguage.setOnCheckedChangeListener(this);
    }
    
    public void onPause() {
		final RadioGroup mLanguage = (RadioGroup)getView().findViewById(R.id.rg_language);
		mLanguage.setOnCheckedChangeListener(null);

		super.onPause();
    }
    
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch(checkedId) {
		case R.id.rb_language_chinese:
			LocalePicker.updateLocale(Locale.CHINA);
			break;
		case R.id.rb_language_english:
			LocalePicker.updateLocale(Locale.US);
			break;
		}
	}

	@Override
	protected int getCurrentId() {return 2;	}

}
