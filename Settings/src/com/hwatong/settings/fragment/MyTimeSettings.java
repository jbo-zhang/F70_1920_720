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

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hwatong.settings.R;
import com.hwatong.settings.SettingsPreferenceFragment;
import com.hwatong.settings.view.ClockView;

public class MyTimeSettings extends SettingsPreferenceFragment implements OnClickListener{
	private static final String TAG = "MyDateTimeSettings";

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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mContentView = inflater.inflate(R.layout.fragment_time, container, false);
		for (int i = 0; i < TEXTVIEW_VOLUME.length; i++) {
			mTextViews[i] = (TextView)mContentView.findViewById(TEXTVIEW_VOLUME[i]);
		}
		for (int i = 0; i < TEXTVIEW_MINUS_ID.length; i++) {
			mContentView.findViewById(TEXTVIEW_MINUS_ID[i]).setOnClickListener(this);
		}
		for (int i = 0; i < TEXTVIEW_PLUS_ID.length; i++) {
			mContentView.findViewById(TEXTVIEW_PLUS_ID[i]).setOnClickListener(this);
		}

		if (is24Hour()) {
			mContentView.findViewById(R.id.ll_ampm).setVisibility(View.GONE);			
		}
		updateTime();
		drawClock();
		return mContentView;
	}



	private LinearLayout myClock;
	private ClockView drawClock;
	protected static final int MESSAGE = 123;  
	public Handler handler;  
	private myThread mthread;  
	TextView tv = null;  
	@SuppressLint("HandlerLeak")
	private void drawClock() {
		myClock = (LinearLayout) mContentView.findViewById(R.id.ll_clock);  
		drawClock = new ClockView(this.getActivity());  
		myClock.addView(drawClock);  

		handler = new Handler() {  
			public void handleMessage(Message mess) {  
				if (mess.what == MESSAGE) {  
					myClock.removeView(drawClock);  
					drawClock = new ClockView(getActivity());  
					myClock.addView(drawClock);
				}  
				super.handleMessage(mess);  
			}  
		};  

		mthread = new myThread();  
		mthread.start();  		
	}

	class myThread extends Thread {
		private Boolean mStop=false;
		public void run() {  
			super.run();  
			while (true) {  
				try {  
					Thread.sleep(1000);  
				} catch (InterruptedException e) {  
					e.printStackTrace();  
				}
				synchronized (mStop) {
					if (!mStop) {
						Message m = new Message();  
						m.what = MESSAGE;  
						handler.sendMessage(m);
					}
				}
			}  
		}
		public void Stop() {
			synchronized (mStop) {
				mStop=true;
				handler.removeMessages(MESSAGE);
			}
		}
	}      
	@Override
	public void onDestroy() {
		mthread.Stop();
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

	/* package */ 
	private static void setTime(Context context, int hourOfDay, int minute) {
		Calendar c = Calendar.getInstance();

		c.set(Calendar.HOUR_OF_DAY, hourOfDay);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		long when = c.getTimeInMillis();

		if (when / 1000 < Integer.MAX_VALUE) {
			((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).setTime(when);
		}
	}
	private boolean is24Hour() {
		return DateFormat.is24HourFormat(getActivity());
	}

	@Override
	public void onClick(View v) {
		Calendar calendar = Calendar.getInstance();
		int curHour = calendar.get(Calendar.HOUR_OF_DAY);
		int curMinute = calendar.get(Calendar.MINUTE);
		switch(v.getId()) {
		case R.id.volume_minus1:
			setTime(getActivity(), (curHour+1>23)?0:curHour+1, curMinute);
			break;
		case R.id.volume_minus2:
			setTime(getActivity(), curHour, (curMinute+1>59)?0:curMinute+1);
			break;
		case R.id.volume_minus3:
			break;
		case R.id.volume_plus1:
			setTime(getActivity(), (curHour-1<0)?23:curHour-1, curMinute);
			break;
		case R.id.volume_plus2:
			setTime(getActivity(), curHour, (curMinute-1<0)?0:curMinute-1);
			break;
		case R.id.volume_plus3:
			setTime(getActivity(), (curHour>=12)?curHour-12:curHour+12, curMinute);
			break;
		}
		updateTime();
	}
	private void formatOut(TextView view, int val) {
		String newstr = String.valueOf(val);
		if (newstr.length()>2) newstr = newstr.substring(newstr.length()-2);
		if (newstr.length()<2) 
			view.setText("0"+newstr);
		else
			view.setText(newstr); 
	}	
	private void updateTime() {
		Calendar calendar = Calendar.getInstance();
		int curHour = calendar.get(Calendar.HOUR_OF_DAY);
		int curMinute = calendar.get(Calendar.MINUTE);
		formatOut(mTextViews[0], is24Hour()?curHour:curHour%12);
		formatOut(mTextViews[1], curMinute);
		if (is24Hour()) return;
		if (curHour>12) 
			mTextViews[2].setText(getActivity().getResources().getString(R.string.datetime_pm));
		else 
			mTextViews[2].setText(getActivity().getResources().getString(R.string.datetime_am));
	}
}
