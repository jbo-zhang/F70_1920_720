package com.hwatong.settings;

import java.util.Calendar;

import android.app.AlarmManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.TextView;

import com.hwatong.settings.wheel.StrericWheelAdapter;
import com.hwatong.settings.wheel.WheelView;
import com.hwatong.settings.R;

public class MyDateTimePreference extends DialogPreference {

    private static final String TAG="MyDateTimePreference";
    private static final boolean DBG=true;

    private WheelView yearWheel,monthWheel,dayWheel,hourWheel,minuteWheel;
	public static String[] yearContent=null;
	public static String[] monthContent=null;
	public static String[] dayContent=null;
	public static String[] hourContent = null;
	public static String[] minuteContent=null;
	private Context mContext;
	private TextView mTextView;

    public MyDateTimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext=context;
        initContent();
        
        setDialogLayoutResource(R.layout.preference_dialog_datetime);
        setDialogIcon(R.drawable.ic_settings_date_time);
    }

    @Override
	protected void onClick() {
		// TODO Auto-generated method stub
		super.onClick();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		super.onClick(dialog, which);
	}

	@Override
    protected void showDialog(Bundle state) {
    	
        super.showDialog(state);
//        Window w = getDialog().getWindow();
//        w.setContentView(R.layout.preference_dialog_datetime);
//        initViews(w.getDecorView());
    }

    private void initViews(View view) {
    	
		Calendar calendar = Calendar.getInstance();
	    int curYear = calendar.get(Calendar.YEAR);
        int curMonth= calendar.get(Calendar.MONTH)+1;
        int curDay = calendar.get(Calendar.DAY_OF_MONTH);
        int curHour = calendar.get(Calendar.HOUR_OF_DAY);
        int curMinute = calendar.get(Calendar.MINUTE);
        Log.d(TAG, "initViews: calendar.getTimeInMillis()="+calendar.getTimeInMillis());
 	    
	    yearWheel = (WheelView)view.findViewById(R.id.yearwheel);
	    monthWheel = (WheelView)view.findViewById(R.id.monthwheel);
	    dayWheel = (WheelView)view.findViewById(R.id.daywheel);
	    hourWheel = (WheelView)view.findViewById(R.id.hourwheel);
	    minuteWheel = (WheelView)view.findViewById(R.id.minutewheel);
	    
        yearWheel.setAdapter(new StrericWheelAdapter(yearContent));
	 	yearWheel.setCurrentItem(curYear-2013);
	    yearWheel.setCyclic(true);
	    yearWheel.setInterpolator(new AnticipateOvershootInterpolator());
        
 
        monthWheel.setAdapter(new StrericWheelAdapter(monthContent));
       
        monthWheel.setCurrentItem(curMonth-1);
     
        monthWheel.setCyclic(true);
        monthWheel.setInterpolator(new AnticipateOvershootInterpolator());
        
        dayWheel.setAdapter(new StrericWheelAdapter(dayContent));
        dayWheel.setCurrentItem(curDay-1);
        dayWheel.setCyclic(true);
        dayWheel.setInterpolator(new AnticipateOvershootInterpolator());
        
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
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        initViews(view);
    }

    @Override
	protected View onCreateView(ViewGroup parent) {
		setWidgetLayoutResource(R.layout.preference_widget_textview);
		return super.onCreateView(parent);
	}

	@Override
	protected void onBindView(View view) {
		mTextView = (TextView)view.findViewById(R.id.textview1);
    	updateDateTimeDisplay();
		super.onBindView(view);
	}

	@Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
        	setDateTime();
        	updateDateTimeDisplay();
        }
    }
    public void initContent()
	{
		yearContent = new String[10];
		for(int i=0;i<10;i++)
			yearContent[i] = String.valueOf(i+2013);
		
		monthContent = new String[12];
		for(int i=0;i<12;i++)
		{
			monthContent[i]= String.valueOf(i+1);
			if(monthContent[i].length()<2)
	        {
				monthContent[i] = "0"+monthContent[i];
	        }
		}
			
		dayContent = new String[31];
		for(int i=0;i<31;i++)
		{
			dayContent[i]=String.valueOf(i+1);
			if(dayContent[i].length()<2)
	        {
				dayContent[i] = "0"+dayContent[i];
	        }
		}	
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
        return DateFormat.is24HourFormat(mContext);
    }
    private void setDateTime() {
		int year = Integer.valueOf(yearWheel.getCurrentItemValue());
		int month = Integer.valueOf(monthWheel.getCurrentItemValue());
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
		setDate(mContext, year, month, day);
		setTime(mContext, hour, minute);
    }
    public void updateDateTimeDisplay() {
        java.text.DateFormat shortDateFormat = DateFormat.getDateFormat(mContext);
        final Calendar now = Calendar.getInstance();
        mTextView.setText(shortDateFormat.format(now.getTime()) +" "+ DateFormat.getTimeFormat(mContext).format(now.getTime()));
    }
//    private void refreshTextView() {
//		StringBuffer sb = new StringBuffer();  
//		sb.append(yearWheel.getCurrentItemValue()).append("-").append(monthWheel.getCurrentItemValue()).append("-").append(dayWheel.getCurrentItemValue());
//		sb.append(" ");
//		sb.append(hourWheel.getCurrentItemValue()).append(":").append(minuteWheel.getCurrentItemValue());
//        mTextView.setText(sb);
//    	
//    }
}

