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

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import com.hwatong.settings.R;
import com.hwatong.settings.Utils;
import com.hwatong.settings.ZonePicker;
import com.hwatong.providers.carsettings.SettingsProvider;

public class MyDateTimeSettings extends MySettingsPreferenceFragment implements OnCheckedChangeListener,OnClickListener{
	private static final String TAG = "MyDateTimeSettings";

	private static final String HOURS_12 = "12";
	private static final String HOURS_24 = "24";

	private TextView mTextLine1,mTextLine2,mTextTitleLine1,mTextTitleLine2;

	private View mViewLine1, mViewLine2;
	private Switch mTimeSourceSelect,mTimeFormatSelect;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_datetime, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mViewLine1= getView().findViewById(R.id.rl_line1);
		mViewLine1.setOnClickListener(this);
		mTextLine1 = (TextView)getView().findViewById(R.id.tv_line1);
		mViewLine2= getView().findViewById(R.id.rl_line2);
		mViewLine2.setOnClickListener(this);
		mTextLine2 = (TextView)getView().findViewById(R.id.tv_line2);
		mTextTitleLine1 = (TextView)getView().findViewById(R.id.tv_title_line1);
		mTextTitleLine2 = (TextView)getView().findViewById(R.id.tv_title_line2);

		mTimeSourceSelect= (Switch) getView().findViewById(R.id.switch_source);
		mTimeSourceSelect.setOnCheckedChangeListener(this);
		mTimeFormatSelect = (Switch) getView().findViewById(R.id.switch_24hour);
		mTimeFormatSelect.setOnCheckedChangeListener(this);
		updateData();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void updateData() {
		fromTouch = false;
		boolean autoTimeEnabled = Utils.getCarSettingsString(getContentResolver(), "auto_time", SettingsProvider.DEFAULT_AUTO_TIME).equals("1");
		mTimeSourceSelect.setChecked(autoTimeEnabled);
		mTimeFormatSelect.setChecked(is24Hour());
		fromTouch = true;
	}
	@Override
	public void onResume() {
		super.onResume();

		// Register for time ticks and other reasons for time change
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_TIME_TICK);
		filter.addAction(Intent.ACTION_TIME_CHANGED);
		filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		getActivity().registerReceiver(mIntentReceiver, filter, null, null);

		updateTimeAndDateDisplay(getActivity());
		updateTimeSource();
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(mIntentReceiver);
	}

	public void updateTimeSource() {
		boolean autoTimeEnabled = Utils.getCarSettingsString(getContentResolver(), "auto_time", SettingsProvider.DEFAULT_AUTO_TIME).equals("1");
		if (autoTimeEnabled) {
			mTextLine1.setTextColor(Color.GRAY);
			mTextLine2.setTextColor(Color.GRAY);
			mTextTitleLine1.setTextColor(Color.GRAY);
			mTextTitleLine2.setTextColor(Color.GRAY);
			mViewLine1.setBackground(null);
			mViewLine2.setBackground(null);
		}else {
			mTextLine1.setTextColor(getResources().getColor(R.color.text_color1));
			mTextLine2.setTextColor(getResources().getColor(R.color.text_color1));
			mTextTitleLine1.setTextColor(Color.WHITE);
			mTextTitleLine2.setTextColor(Color.WHITE);
			mViewLine1.setBackgroundResource(R.drawable.list_selector);
			mViewLine2.setBackgroundResource(R.drawable.list_selector);
		}
	}

	public void updateTimeAndDateDisplay(Context context) {

		java.text.DateFormat shortDateFormat = DateFormat.getDateFormat(context);
		final Calendar now = Calendar.getInstance();
		mTextLine1.setText(shortDateFormat.format(now.getTime()) + " " +DateFormat.getTimeFormat(getActivity()).format(now.getTime()));
		mTextLine2.setText(getTimeZoneText(now.getTimeZone()));
	}

	private void timeUpdated() {
		Intent timeChanged = new Intent(Intent.ACTION_TIME_CHANGED);
		getActivity().sendBroadcast(timeChanged);
	}

	/*  Get & Set values from the system settings  */

	private void set24Hour(boolean is24Hour) {
		Settings.System.putString(getContentResolver(),Settings.System.TIME_12_24,is24Hour? HOURS_24 : HOURS_12);
	}

	private String getDateFormat() {
		return Settings.System.getString(getContentResolver(),Settings.System.DATE_FORMAT);
	}

	private boolean getAutoState(String name) {
		try {
			return Settings.Global.getInt(getContentResolver(), name) > 0;
		} catch (SettingNotFoundException snfe) {
			return false;
		}
	}

	private boolean is24Hour() {
		return DateFormat.is24HourFormat(getActivity());
	}
	
	/* package */ static String getTimeZoneText(TimeZone tz) {
		// Similar to new SimpleDateFormat("'GMT'Z, zzzz").format(new Date()), but
		// we want "GMT-03:00" rather than "GMT-0300".
		Date now = new Date();
		return tz.getDisplayName(tz.inDaylightTime(now), TimeZone.LONG).toString();
//		return formatOffset(new StringBuilder(), tz, now).
//				append(", ").
//				append(tz.getDisplayName(tz.inDaylightTime(now), TimeZone.LONG)).toString();
	}

	private static StringBuilder formatOffset(StringBuilder sb, TimeZone tz, Date d) {
		int off = tz.getOffset(d.getTime()) / 1000 / 60;

		sb.append("GMT");
		if (off < 0) {
			sb.append('-');
			off = -off;
		} else {
			sb.append('+');
		}

		int hours = off / 60;
		int minutes = off % 60;

		sb.append((char) ('0' + hours / 10));
		sb.append((char) ('0' + hours % 10));

		sb.append(':');

		sb.append((char) ('0' + minutes / 10));
		sb.append((char) ('0' + minutes % 10));

		return sb;
	}

	private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final Activity activity = getActivity();
			if (activity != null) {
				updateTimeAndDateDisplay(activity);
			}
		}
	};

	@Override
	public void onClick(View v) {
		super.onClick(v);
		
		Log.d(TAG, "onClick: v="+v);
		boolean autoTimeEnabled = Utils.getCarSettingsString(getContentResolver(), "auto_time", SettingsProvider.DEFAULT_AUTO_TIME).equals("1");
		switch(v.getId()) {
		case R.id.rl_line1:
			if (!autoTimeEnabled)
				startFragment(this, MyDateAndTime.class.getCanonicalName(), -1, null);
			break;
		case R.id.rl_line2:
			if (!autoTimeEnabled)
				startFragment(this, MyZonePicker.class.getCanonicalName(), -1, null);
			break;
		}
	}

	private boolean fromTouch=false;
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (!fromTouch) return;
		switch(buttonView.getId()) {
		case R.id.switch_source:
			Utils.putCarSettingsString(getContentResolver(), "auto_time", isChecked?"1":"0");
			updateTimeSource();
			break;
		case R.id.switch_24hour:
			set24Hour(isChecked);
			updateTimeAndDateDisplay(getActivity());
			timeUpdated();
			break;
		}
	}

	@Override
	protected int getCurrentId() {return 0;	}
}
