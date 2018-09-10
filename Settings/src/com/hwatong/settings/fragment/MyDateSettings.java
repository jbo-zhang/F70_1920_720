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

import android.app.AlarmManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hwatong.settings.R;
import com.hwatong.settings.SettingsPreferenceFragment;
import com.hwatong.settings.view.MonthDateView;
import com.hwatong.settings.view.MonthDateView.DateClick;
import com.hwatong.settings.view.WeekDayView;

public class MyDateSettings extends SettingsPreferenceFragment implements OnClickListener{
	private static final String TAG = "MyDateSettings";

	private static final int[] TEXTVIEW_MINUS_ID = new int[] {
		R.id.volume_minus1,
		R.id.volume_minus2,
		R.id.volume_minus3
	};
	private static final int[] TEXTVIEW_PLUS_ID = new int[] {
		R.id.volume_plus1,
		R.id.volume_plus2,
		R.id.volume_plus3
	};
	private static final int[] TEXTVIEW_VOLUME = new int[] {
		R.id.volume_text1,
		R.id.volume_text2,
		R.id.volume_text3
	};
    
    private View mContentView;
	private TextView mTextViews[] = new TextView[TEXTVIEW_VOLUME.length];
	private MonthDateView mMonthDateView;
	private WeekDayView mWeekDayView; 
	private int weekRes[] = new int[] {R.string.datetime_sunday, R.string.datetime_monday, R.string.datetime_tuesday, R.string.datetime_wednesday, R.string.datetime_thursday, R.string.datetime_friday, R.string.datetime_saturday};
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	mContentView = inflater.inflate(R.layout.fragment_date, container, false);
		for (int i = 0; i < TEXTVIEW_VOLUME.length; i++) {
			mTextViews[i] = (TextView)mContentView.findViewById(TEXTVIEW_VOLUME[i]);
		}
		for (int i = 0; i < TEXTVIEW_MINUS_ID.length; i++) {
			mContentView.findViewById(TEXTVIEW_MINUS_ID[i]).setOnClickListener(this);
		}
		for (int i = 0; i < TEXTVIEW_PLUS_ID.length; i++) {
			mContentView.findViewById(TEXTVIEW_PLUS_ID[i]).setOnClickListener(this);
		}
		mMonthDateView = (MonthDateView)mContentView.findViewById(R.id.month_date_view);
		mMonthDateView.setDateClick(new DateClick() {
			
			@Override
			public void onClickOnDate() {
				setDate(getActivity(), mMonthDateView.getmSelYear(), mMonthDateView.getmSelMonth(), mMonthDateView.getmSelDay());
		        updateDate();
			}
		});
		mWeekDayView = (WeekDayView)mContentView.findViewById(R.id.week_day_view);
		String weekString[] = new String[weekRes.length];
		for (int i = 0; i < weekRes.length; i++) {
			weekString[i] = getResources().getString(weekRes[i]);
		}
		mWeekDayView.setWeekString(weekString);
		updateDate();
    	return mContentView;
	}

	private void updateDate() {
		Calendar calendar = Calendar.getInstance();
	    int curYear = calendar.get(Calendar.YEAR);
        int curMonth= calendar.get(Calendar.MONTH)+1;
        int curDay = calendar.get(Calendar.DAY_OF_MONTH);
		formatOut(mTextViews[0], curYear);
		formatOut(mTextViews[1], curMonth);
		formatOut(mTextViews[2], curDay);
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();

	}

	@Override
	public void onPause() {
		super.onPause();
	}

	
	private void checkDate() {
		String year = mTextViews[0].getText().toString();
		String month = mTextViews[1].getText().toString();
		String day = mTextViews[2].getText().toString();
		int nYear=0,nMonth=0,nDay=0;
		try{nYear=Integer.valueOf(year);}catch(Exception e){e.printStackTrace();}		
		try{nMonth=Integer.valueOf(month);}catch(Exception e){e.printStackTrace();}		
		try{nDay=Integer.valueOf(day);}catch(Exception e){e.printStackTrace();}
		Calendar calendar = Calendar.getInstance();
	    int curYear = calendar.get(Calendar.YEAR);
        int curMonth= calendar.get(Calendar.MONTH)+1;
        int curDay = calendar.get(Calendar.DAY_OF_MONTH);
		if (curYear!=nYear||curMonth!=nMonth||curDay!=nDay){
			setDate(getActivity(), nYear, nMonth, nDay);
		}
	}
    /* package */
    private static void setDate(Context context, int year, int month, int day) {
        Calendar c = Calendar.getInstance();

        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        long when = c.getTimeInMillis();

        Log.d(TAG, "setDate: calendar.getTimeInMillis()="+when);
        if (when / 1000 < Integer.MAX_VALUE) {
            ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setTime(when);
        }
    }
    private static void setDate(Context context, Calendar c) {
        long when = c.getTimeInMillis();

        Log.d(TAG, "setDate: calendar.getTimeInMillis()="+when);
        if (when / 1000 < Integer.MAX_VALUE) {
            ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setTime(when);
        }
    }

	@Override
	public void onClick(View v) {
		Calendar calendar = Calendar.getInstance();
		switch(v.getId()) {
		case R.id.volume_minus1:
			calendar.add(Calendar.YEAR, 1);
			setDate(getActivity(), calendar);
			break;
		case R.id.volume_minus2:
			calendar.add(Calendar.MONTH, 1);
			setDate(getActivity(), calendar);
			break;
		case R.id.volume_minus3:
			calendar.add(Calendar.DATE, 1);
			setDate(getActivity(), calendar);
			break;
		case R.id.volume_plus1:
			calendar.add(Calendar.YEAR, -1);
			setDate(getActivity(), calendar);
			break;
		case R.id.volume_plus2:
			calendar.add(Calendar.MONTH, -1);
			setDate(getActivity(), calendar);
			break;
		case R.id.volume_plus3:
			calendar.add(Calendar.DATE, -1);
			setDate(getActivity(), calendar);
			break;
		}
        updateDate();
        mMonthDateView.update();
	}
	private void formatOut(TextView view, int val) {
	    String newstr = String.valueOf(val);
	    if (newstr.length()>2) newstr = newstr.substring(newstr.length()-2);
		if (newstr.length()<2) 
			view.setText("0"+newstr);
		else
			view.setText(newstr); 
	}	
}
