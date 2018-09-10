package com.hwatong.settings.fragment;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.hwatong.providers.carsettings.SettingsProvider;
import com.hwatong.settings.R;
import com.hwatong.settings.SettingsPreferenceFragment;
import com.hwatong.settings.Utils;
import com.hwatong.settings.wheel.OnWheelChangedListener;
import com.hwatong.settings.wheel.StrericWheelAdapter;
import com.hwatong.settings.wheel.WheelView;

public class MyDateAndTime extends MySettingsPreferenceFragment implements OnWheelChangedListener{
	private static final String TAG = "MyDateTimeSettings";

	private static final String HOURS_12 = "12";
	private static final String HOURS_24 = "24";

    private WheelView yearWheel,monthWheel,dayWheel,hourWheel,minuteWheel;
	public static String[] yearContent=null;
	public static String[] monthContent=null;
	public static String[] dayContent=null;
	public static String[] hourContent = null;
	public static String[] minuteContent=null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_datetime2, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		boolean autoTimeEnabled = Utils.getCarSettingsString(getContentResolver(), "auto_time", SettingsProvider.DEFAULT_AUTO_TIME).equals("1");

		final TextView mTextView = (TextView)getView().findViewById(R.id.tv_current_datetime);
		mTextView.setOnClickListener(this);
		getView().findViewById(R.id.iv_current_arrow).setOnClickListener(this);
        initViews(getView());
		setDateTimeEnabled(!autoTimeEnabled);
	}
	
	@Override
	public void onDestroy() {
		mHandler.removeMessages(ID_SET_DATETIME);
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_TIME_TICK);
		filter.addAction(Intent.ACTION_TIME_CHANGED);
		filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		getActivity().registerReceiver(mIntentReceiver, filter, null, null);

		initContent();		
		updateTimeAndDateDisplay(getActivity());
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(mIntentReceiver);
	}

	public void updateTimeAndDateDisplay(Context context) {
        java.text.DateFormat shortDateFormat = DateFormat.getDateFormat(context);
        final Calendar now = Calendar.getInstance();
		final TextView mTextView = (TextView)getView().findViewById(R.id.tv_current_datetime);
        mTextView.setText(shortDateFormat.format(now.getTime()) +" "+ DateFormat.getTimeFormat(context).format(now.getTime()));
        initDay();
        
//		final Calendar now = Calendar.getInstance();
//		mDummyDate.setTimeZone(now.getTimeZone());
//		// We use December 31st because it's unambiguous when demonstrating the date format.
//		// We use 13:00 so we can demonstrate the 12/24 hour options.
//		mDummyDate.set(now.get(Calendar.YEAR), 11, 31, 13, 0, 0);
//		Date dummyDate = mDummyDate.getTime();
//		
//		mTime24Pref.setSummary(DateFormat.getTimeFormat(getActivity()).format(dummyDate));
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
    private void setDateTime() {
		int year = Integer.valueOf(yearWheel.getCurrentItemValue());
		int month = Integer.valueOf(monthWheel.getCurrentItemValue())-1;
		int day = Integer.valueOf(dayWheel.getCurrentItemValue());
		int hour;
		if (is24Hour()){
			hour = Integer.valueOf(hourWheel.getCurrentItemValue());
		}else {
			hour = Integer.valueOf(hourWheel.getCurrentItemValue().substring(3));
			if (hour==12) hour=0;
			if (hourWheel.getCurrentItemValue().substring(0,2).equals("PM")) {
				hour+=12;
			}
		}
		int minute = Integer.valueOf(minuteWheel.getCurrentItemValue());
		setDate(getActivity(), year, month, day);
		setTime(getActivity(), hour, minute);
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
				Toast.makeText(activity, "开始更新时间", Toast.LENGTH_SHORT).show();
				updateTimeAndDateDisplay(activity);
			}
		}
	};

	boolean initWheel=false;
    private void initViews(View view) {
    	initWheel=true;
 	    
	    yearWheel = (WheelView)view.findViewById(R.id.yearwheel);
	    monthWheel = (WheelView)view.findViewById(R.id.monthwheel);
	    dayWheel = (WheelView)view.findViewById(R.id.daywheel);
	    hourWheel = (WheelView)view.findViewById(R.id.hourwheel);
	    minuteWheel = (WheelView)view.findViewById(R.id.minutewheel);

	    yearWheel.addChangingListener(this);
	    monthWheel.addChangingListener(this);
	    dayWheel.addChangingListener(this);
	    hourWheel.addChangingListener(this);
	    minuteWheel.addChangingListener(this);
	    
    	initWheel=false;
    }
    
    private void initDay() {
        Calendar c = Calendar.getInstance();

        int days = c.getActualMaximum(Calendar.DATE);
		dayContent = new String[days];
		for(int i=0;i<days;i++)
		{
			dayContent[i]=String.valueOf(i+1);
			if(dayContent[i].length()<2)
	        {
				dayContent[i] = "0"+dayContent[i];
	        }
		}	
        dayWheel.setAdapter(new StrericWheelAdapter(dayContent));
        monthWheel.setCurrentItem(c.get(Calendar.MONTH)+1-1);
        dayWheel.setCyclic(true);
        dayWheel.setInterpolator(new AnticipateOvershootInterpolator());
    }
    
    public void initContent()
	{
		yearContent = new String[100];
		for(int i=0;i<100;i++)
			yearContent[i] = String.valueOf(i+1970);
		
		monthContent = new String[12];
		for(int i=0;i<12;i++)
		{
			monthContent[i]= String.valueOf(i+1);
			if(monthContent[i].length()<2)
	        {
				monthContent[i] = "0"+monthContent[i];
	        }
		}
		initDay();
		hourContent = new String[24];
		for(int i=0;i<24;i++)
		{
			int t = i;
			if (!is24Hour()) {
				t = i%12;
			}
			hourContent[i]= String.valueOf(t);
			if(hourContent[i].length()<2)
	        {
				hourContent[i] = "0"+hourContent[i];
	        }
			if (!is24Hour()) {
				if (i==0) {
					hourContent[i]="AM 12";
				}else if (i==12){
					hourContent[i]="PM 12";
				} else if (i<12) {
					hourContent[i]="AM " + hourContent[i];
				}else {
					hourContent[i]="PM " + hourContent[i];
				}
			}
		}
			
		minuteContent = new String[60];
		for(int i=0;i<60;i++)
		{
			minuteContent[i]=String.valueOf(i);
			if(minuteContent[i].length()<2)
	        {
				minuteContent[i] = "0"+minuteContent[i];
	        }
		}
		Calendar calendar = Calendar.getInstance();
	    int curYear = calendar.get(Calendar.YEAR);
        int curMonth= calendar.get(Calendar.MONTH)+1;
        int curDay = calendar.get(Calendar.DAY_OF_MONTH);
        int curHour = calendar.get(Calendar.HOUR_OF_DAY);
        int curMinute = calendar.get(Calendar.MINUTE);
        yearWheel.setAdapter(new StrericWheelAdapter(yearContent));
	 	yearWheel.setCurrentItem(curYear-1970);
	    yearWheel.setCyclic(true);
	    yearWheel.setInterpolator(new AnticipateOvershootInterpolator());
        
        monthWheel.setAdapter(new StrericWheelAdapter(monthContent));
        dayWheel.setCurrentItem(curDay-1);
        monthWheel.setCyclic(true);
        monthWheel.setInterpolator(new AnticipateOvershootInterpolator());
        
        
        hourWheel.setAdapter(new StrericWheelAdapter(hourContent));
        hourWheel.setCurrentItem(curHour);
        hourWheel.setCyclic(true);
        hourWheel.setInterpolator(new AnticipateOvershootInterpolator());
        
        minuteWheel.setAdapter(new StrericWheelAdapter(minuteContent));
        minuteWheel.setCurrentItem(curMinute);
        minuteWheel.setCyclic(true);
        minuteWheel.setInterpolator(new AnticipateOvershootInterpolator());
	}

	@Override
	public void onChanged(WheelView wheel, int oldValue, int newValue) {
		if (initWheel) return;
	    switch(wheel.getId()) {
	    case R.id.yearwheel:
	    case R.id.monthwheel:
	    case R.id.daywheel:
	    case R.id.hourwheel:
	    case R.id.minutewheel:
			mHandler.removeMessages(ID_SET_DATETIME);
			mHandler.sendEmptyMessageDelayed(ID_SET_DATETIME, 1000);
	    	break;
	    }
	}
	public void setDateTimeEnabled(boolean enabled){
	    yearWheel.setEnabled(enabled);
	    monthWheel.setEnabled(enabled);
	    dayWheel.setEnabled(enabled);
	    hourWheel.setEnabled(enabled);
	    minuteWheel.setEnabled(enabled);
	}

	private static final int ID_SET_DATETIME=0x1001;
	@SuppressLint("HandlerLeak") 
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case ID_SET_DATETIME:
				mHandler.removeMessages(ID_SET_DATETIME);
		    	try{setDateTime();}catch(Exception e) { e.printStackTrace();}
				break;
			}
		}
	};
	@Override
	public void onClick(View v) {
		super.onClick(v);
		switch(v.getId()) {
		case R.id.iv_current_arrow:
		case R.id.tv_current_datetime:
			getActivity().onBackPressed();
			break;
		}
	}

	@Override
	protected int getCurrentId() {return 0;	}
}
