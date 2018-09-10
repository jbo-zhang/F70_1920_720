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

package com.hwatong.settings;

import com.hwatong.f70.bluetooth.BaseBluetoothSettingActivity;
import com.hwatong.f70.commonsetting.BaseCommonSettingActivity;
import com.hwatong.f70.huachenyun.BaseHuaChenYunActivity;
import com.hwatong.f70.main.F70MainActivity;
import com.hwatong.f70.soundsetting.BaseSoundSettingActivity;
import com.hwatong.settings.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.view.View;
import android.view.View.OnClickListener;

public class ResetSettings extends SettingsPreferenceFragment implements OnClickListener {
    private static final String TAG = "CommonSettings";

	private static final String KEY_CUSTOM = "custom_pref";
    private static final String KEY_PHONE_RESET= "phone_reset";
    private static final String KEY_MAP_RESET = "map_reset";

    private InfoDialog mDialog;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.reset_settings);
        getPreferenceScreen().removePreference(findPreference(KEY_MAP_RESET));
    }
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getKey().equals(KEY_PHONE_RESET)){
        	mDialog = new InfoDialog(getActivity(), this);
        	mDialog.show();
        	mDialog.setTitle(R.string.phone_reset_title);
        	mDialog.setMessage(getResources().getString(R.string.phone_reset_message));
        	mDialog.setSubmitButton(getResources().getString(R.string.dlg_ok));
        	mDialog.setCancelButton(getResources().getString(R.string.dlg_cancel));
    		return true;
    	}
    	if (preference.getKey().equals(KEY_MAP_RESET)){
            new AlertDialog.Builder(getActivity())
            .setTitle(R.string.map_reset_title)
            .setMessage(R.string.master_clear_confirm_title)
            .setNegativeButton(R.string.dlg_cancel, null)
            .setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
		            if (Utils.isMonkeyRunning()) {
		                return;
		            }
	                getActivity().sendBroadcast(new Intent("com.mxnavi.mxnavi.CMD_NAVI_FACTORY_RESET"));
				}
			})
            .create()
            .show();
    		return true;
    	}
    	if (super.onPreferenceTreeClick(preferenceScreen, preference)) {
            finish();
    		return true;
    	}else {
    		return false;
    	}
    }

    @Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	public boolean onPreferenceChange(Preference preference, Object objValue) {
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
	@Override
	public void onClick(View v) {
		if (v.getId()==R.id.button1) {
            if (Utils.isMonkeyRunning()) {
                return;
            }
			mDialog.dismiss();
			Intent intent = new Intent(getActivity(), F70MainActivity.class);
			startActivity(intent);
//            getActivity().sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
			
		}
		else if(v.getId()==R.id.button2) {
			mDialog.dismiss();
		}
	}
}
