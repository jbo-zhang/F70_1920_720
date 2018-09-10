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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hwatong.settings.R;
import com.hwatong.settings.Utils;

public class MyVersionSettings extends MySettingsPreferenceFragment implements OnClickListener{
    private static final String TAG = "MyVersionSettings";

	private View mViewLine1;
	private TextView mSoftText,mHardwareText,mPanelText;
    public static String formatSoftwareVersion(String SoftwareVersion) {
        long currentTimeMillis = 0;

        String[] Version = SoftwareVersion.split(" ");
        String result = "";
        if (Version.length >= 2){
            result = result + Version[0];
            Log.d(TAG, "formatSoftwareVersion Version[0]:" + Version[0]);
            try {
                currentTimeMillis = Long.parseLong(Version[Version.length - 2]);
            } catch (NumberFormatException e){
            }
	        Calendar c = Calendar.getInstance();
	        c.setTimeInMillis(currentTimeMillis);

            result = result +"-"+ c.get(Calendar.YEAR) +
                        ((c.get(Calendar.MONTH) + 1) < 10 ? "0" : "") + (c.get(Calendar.MONTH) + 1) +
                        (c.get(Calendar.DAY_OF_MONTH) < 10 ? "0" : "") + c.get(Calendar.DAY_OF_MONTH);
            Log.d(TAG, "formatSoftwareVersion c.get(Calendar.YEAR)" + c.get(Calendar.YEAR)
                            + " " + c.get(Calendar.MONTH) + " " + c.get(Calendar.DAY_OF_MONTH));
//            result =  result + "-" + Version[Version.length - 1];
        }

        return result;
    }

    public static String getHardwareInfo(String filename) {
    	String version="";
        try {
            FileInputStream is = new FileInputStream(filename);
            byte[] buf = new byte[32];
            int len;
            try {
                len = is.read(buf);
    			version= new String(buf, 0, len);
            } catch (IOException e) {
                e.printStackTrace();
            }
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    	return version;
    }
    
	public void onClick(View v) {
		super.onClick(v);
		
		switch(v.getId()) {
		case R.id.rl_line1:
			try{startActivity(new Intent("android.intent.action.SYSTEM_UPDATE_SETTINGS"));}catch(Exception e){e.printStackTrace();}
			break;
		}
	}

	private void updateData() {
		mSoftText.setText(formatSoftwareVersion(Utils.getBuildID() + " " + Build.TIME + " 01"));
		mHardwareText.setText(getHardwareInfo("/sys/devices/platform/imx-i2c.1/i2c-1/1-0019/version"));
        mPanelText.setText(Utils.getCarSettingsString(getContentResolver(), "panel_version", ""));
	}
    
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_version, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mViewLine1= getView().findViewById(R.id.rl_line1);
		mViewLine1.setOnClickListener(this);
		mSoftText = (TextView)getView().findViewById(R.id.tv_software_version);
		mHardwareText = (TextView)getView().findViewById(R.id.tv_hardware_version);
		mPanelText = (TextView)getView().findViewById(R.id.tv_panel_version);
		updateData();
	}

	@Override
	protected int getCurrentId() {return 5;	}

}
