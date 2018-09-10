/*
 * Copyright (C) 2007 The Android Open Source Project
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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;

import com.hwatong.settings.R;
import com.hwatong.settings.SettingsPreferenceFragment;
import com.hwatong.settings.Utils;
import com.hwatong.providers.carsettings.SettingsProvider;

public class MyAtmosphereLampSettings extends SettingsPreferenceFragment implements 
		OnSharedPreferenceChangeListener,Preference.OnPreferenceChangeListener {
    private static final String TAG = "SoundSettings";

    private static final int DIALOG_NOT_DOCKED = 1;

    private static final String KEY_SWITCH_PREFERENCE = "switch_preference";
    private static final String KEY_STATIC = "static_settings";
    private static final String KEY_DYNAMIC = "dynamic_settings";
    private static final String KEY_INSTRUMENT = "instrument_table";

    private static final int MSG_UPDATE_RINGTONE_SUMMARY = 1;

    private SwitchPreference mInstrumentSwitch;
    private Preference mSwitchPreference;
    private Preference mStaticPreference;
    private Preference mDynamicPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.atmospherelamp_settings);
        
        mInstrumentSwitch = (SwitchPreference) findPreference(KEY_INSTRUMENT);
        mInstrumentSwitch.setWidgetLayoutResource(R.layout.switch_pref);
        mInstrumentSwitch.setOnPreferenceChangeListener(this);

        mSwitchPreference = findPreference(KEY_SWITCH_PREFERENCE);
        mStaticPreference = findPreference(KEY_STATIC);
        mDynamicPreference = findPreference(KEY_DYNAMIC);


    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    private boolean fromTouch=true;
    @Override
    public void onPause() {
        super.onPause();

        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
//    	if (preference == mMusicFx || preference == mSoundField) {
//            finish();
//    		return false;
//        }
    	return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
//        if (KEY_SOUND_EFFECTS.equals(key)) {
//        }

        return true;
    }

    @Override
    protected int getHelpResource() {
        return R.string.help_url_sound;
    }

    @Override
    public Dialog onCreateDialog(int id) {
        if (id == DIALOG_NOT_DOCKED) {
            return createUndockedMessage();
        }
        return null;
    }

    private Dialog createUndockedMessage() {
        final AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
        ab.setTitle(R.string.dock_not_found_title);
        ab.setMessage(R.string.dock_not_found_text);
        ab.setPositiveButton(android.R.string.ok, null);
        return ab.create();
    }

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,String key) {
		Log.d(TAG, "onSharedPreferenceChanged key=" +key);
        if (KEY_INSTRUMENT.equals(key) && fromTouch) {
        }
	}
}

