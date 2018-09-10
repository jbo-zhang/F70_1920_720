package com.hwatong.f70.commonsetting;

import java.util.Calendar;

import com.hwatong.f70.baseview.BaseFragment;
import com.hwatong.f70.carsetting.F70CarSettingCommand;
import com.hwatong.f70.main.ConfigrationVersion;
import com.hwatong.f70.main.LogUtils;
import com.hwatong.providers.carsettings.SettingsProvider;
import com.hwatong.settings.R;
import com.hwatong.settings.Utils;
import com.hwatong.settings.wheel.OnWheelChangedListener;
import com.hwatong.settings.wheel.StrericWheelAdapter;
import com.hwatong.settings.wheel.WheelView;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Fragment;
import android.canbus.GpsStatus;
import android.canbus.ICanbusService;
import android.canbus.IGpsStatusListener;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TimeSetting extends BaseFragment implements OnClickListener,
		OnWheelChangedListener, OnCheckedChangeListener {
	private boolean initWheel;
	private WheelView yearWheel, monthWheel, dayWheel, hourWheel, minuteWheel;
	private RadioGroup timeSetMode;
	private RadioButton autoRb, manaulRb;
	// all wheelview content init
	public static String[] yearContent = null;
	public static String[] monthContent = null;
	public static String[] dayContent = null;
	public static String[] hourContent = null;
	public static String[] minuteContent = null;
	private final static int YEARSETTING = 0;
	private final static int MONTHSETTING = 1;
	private final static int DAYSETTING = 2;
	private final static int HOURSETTING = 3;
	private final static int MINUTESETTING = 4;
	private final static int MAX_YEARS = 102;
	private final static int START_YEARS = 2000;
	// private final static int MAX_YEARS = 68;
	// private final static int START_YEARS = 1970;

	private boolean isTimeWheelChanged = false;
	private boolean isModeChanged = false;
	private boolean isAlreadyRegisGpsStatus = false;

	private static final int[] MONTH_DAYS = { 0, 31, 28, 31, 30, 31, 30, 31,
			31, 30, 31, 30, 31 };

	private ICanbusService iCanbusService;

	private ImageButton yearAdd, yearDecre, monthAdd, monthDecre, dayAdd,
			dayDecre, hourAdd, hourDecre, minuteAdd, minuteDecre;
	private TextView timeColon;
	private RelativeLayout autoTimeLayout;

	// private int currentYear, currentMonth, currentDay, currentHour,
	// currentMinute;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LogUtils.d("onCreateView");
		View rootView = inflater.inflate(R.layout.f70_timesetting, container,
				false);
		initService();
		initViews(rootView);
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		// IntentFilter filter = new IntentFilter();
		// filter.addAction(Intent.ACTION_TIME_TICK);
		// filter.addAction(Intent.ACTION_TIME_CHANGED);
		// filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		// getActivity().registerReceiver(mIntentReceiver, filter, null, null);

		initContent();

		initTimeMode();
		
		changedActivityImage(this.getClass().getName());
	}

	@Override
	public void onStop() {
		super.onStop();
		// getActivity().unregisterReceiver(mIntentReceiver);
		LogUtils.d("onStop");

		if (ConfigrationVersion.getInstance().isHight()
				|| ConfigrationVersion.getInstance().isMiddleElite()
				|| ConfigrationVersion.getInstance().isMiddleLuxury() && isModeChanged
				|| isTimeWheelChanged) 
			setIPCTime();

		// if (isTimeWheelChanged) 
		// {
		// setIPCTime();
		// }

		int year = Integer.valueOf(yearWheel.getCurrentItemValue());
		int month = Integer.valueOf(monthWheel.getCurrentItemValue());
		int day = Integer.valueOf(dayWheel.getCurrentItemValue());
		int hour = Integer.valueOf(hourWheel.getCurrentItemValue());
		int minute = Integer.valueOf(minuteWheel.getCurrentItemValue());
		LogUtils.d("set value: " + year + ", " + month + ", " + day + ", "
				+ hour + ", " + minute);
		// setDate(getActivity(), currentYear, currentMonth, currentDay);
		// setTime(getActivity(), currentHour, currentMinute);
		setDateAndTime(year, month, day, hour, minute);
		isTimeWheelChanged = false;
		isModeChanged = false;
	}

	@Override
	public void onPause() {
		super.onPause();
		if (isAlreadyRegisGpsStatus)
			unRegisGpsService();
	}

	@Override
	public void onDestroy() {
		mHandler.removeCallbacksAndMessages(null);
		super.onDestroy();
	}

	// private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
	// @Override
	// public void onReceive(Context context, Intent intent) {
	// LogUtils.d("Time changed!!!");
	// setWidgetTime();
	// }
	// };

	private void initService() {
		iCanbusService = ICanbusService.Stub.asInterface(ServiceManager
				.getService("canbus"));
	}

	private void regisGpsService() {
		try {
			iCanbusService.addGpsStatusListener(iGpsStatusListener);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		isAlreadyRegisGpsStatus = true;
	}

	private void getAndInitGpsTime() {
		GpsStatus gpsStatus;
		try {
			gpsStatus = iCanbusService.getLastGpsStatus(getActivity()
					.getPackageName());
			if (gpsStatus != null
					&& (gpsStatus.getFlags() & 0x02) == GpsStatus.GPS_HAS_TIME)
				initGpsTime(gpsStatus.getTime());
			else
				LogUtils.d("GpsStatus is invalid!!");
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	private void unRegisGpsService() {
		try {
			iCanbusService.removeGpsStatusListener(iGpsStatusListener);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		isAlreadyRegisGpsStatus = false;
	}

	private void initGpsTime(long gpsTime) {
		Calendar gps = Calendar.getInstance();
		gps.setTimeInMillis(gpsTime);
		LogUtils.d("initGpsTime: " + gpsTime + ", " + gps.get(Calendar.YEAR)
				+ ", " + gps.get(Calendar.MONTH) + ", "
				+ (gps.get(Calendar.DAY_OF_MONTH) - 1) + ", "
				+ gps.get(Calendar.HOUR_OF_DAY) + ", "
				+ gps.get(Calendar.MINUTE));
		yearWheel.setCurrentItem(gps.get(Calendar.YEAR) - START_YEARS);

		monthWheel.setCurrentItem(gps.get(Calendar.MONTH));

		dayWheel.setCurrentItem(gps.get(Calendar.DAY_OF_MONTH) - 1);

		hourWheel.setCurrentItem(gps.get(Calendar.HOUR_OF_DAY));

		minuteWheel.setCurrentItem(gps.get(Calendar.MINUTE));

		gps.clear();
	}

	
	private void initTimeMode() {
		LogUtils.d("time mode:"
				+ Utils.getCarSettingsString(
						getActivity().getContentResolver(),
						SettingsProvider.AUTO_TIME));
		boolean isManaul = Utils.getCarSettingsString(
				getActivity().getContentResolver(), SettingsProvider.AUTO_TIME)
				.equals(F70CarSettingCommand.LOCAL_CLOSE);
		if (ConfigrationVersion.getInstance().isHight()
				|| ConfigrationVersion.getInstance().isMiddleElite()
				|| ConfigrationVersion.getInstance().isMiddleLuxury()) 
			setTimeWidgetEnabled(Utils.getCarSettingsString(getActivity()
					.getContentResolver(), SettingsProvider.AUTO_TIME));
		// timeSetMode.setOnCheckedChangeListener(null);
		if (isManaul)
			timeSetMode.check(R.id.time_manual);
		else {
			timeSetMode.check(R.id.time_auto);
			regisGpsService();
			getAndInitGpsTime();
		}

		timeSetMode.setOnCheckedChangeListener(this);
	}

	private void initViews(View view) {

		yearWheel = (WheelView) view.findViewById(R.id.yearwheel);
		monthWheel = (WheelView) view.findViewById(R.id.monthwheel);
		dayWheel = (WheelView) view.findViewById(R.id.daywheel);
		hourWheel = (WheelView) view.findViewById(R.id.hourwheel);
		minuteWheel = (WheelView) view.findViewById(R.id.minutewheel);
		
		yearWheel.setIsScrolled(false);
		monthWheel.setIsScrolled(false);
		dayWheel.setIsScrolled(false);
		hourWheel.setIsScrolled(false);
		minuteWheel.setIsScrolled(false);
		
		initWheel = false;

		yearAdd = (ImageButton) view.findViewById(R.id.year_add);
		yearDecre = (ImageButton) view.findViewById(R.id.year_decre);
		monthAdd = (ImageButton) view.findViewById(R.id.month_add);
		monthDecre = (ImageButton) view.findViewById(R.id.month_decre);
		dayAdd = (ImageButton) view.findViewById(R.id.day_add);
		dayDecre = (ImageButton) view.findViewById(R.id.day_decre);
		hourAdd = (ImageButton) view.findViewById(R.id.hour_add);
		hourDecre = (ImageButton) view.findViewById(R.id.hour_decre);
		minuteAdd = (ImageButton) view.findViewById(R.id.minute_add);
		minuteDecre = (ImageButton) view.findViewById(R.id.minute_decre);

		timeColon = (TextView) view.findViewById(R.id.time_colon);

		yearAdd.setOnClickListener(this);
		yearDecre.setOnClickListener(this);
		monthAdd.setOnClickListener(this);
		monthDecre.setOnClickListener(this);
		dayAdd.setOnClickListener(this);
		dayDecre.setOnClickListener(this);
		hourAdd.setOnClickListener(this);
		hourDecre.setOnClickListener(this);
		minuteAdd.setOnClickListener(this);
		minuteDecre.setOnClickListener(this);

		timeSetMode = (RadioGroup) view.findViewById(R.id.timemoderg);
		autoRb = (RadioButton) view.findViewById(R.id.time_auto);
		manaulRb = (RadioButton) view.findViewById(R.id.time_manual);

		autoRb.setOnClickListener(this);
		manaulRb.setOnClickListener(this);

		
		if (ConfigrationVersion.getInstance().isHight()
				|| ConfigrationVersion.getInstance().isMiddleElite()
				|| ConfigrationVersion.getInstance().isMiddleLuxury()) {
			autoTimeLayout = (RelativeLayout) view
					.findViewById(R.id.auto_title);
			autoTimeLayout.setVisibility(View.VISIBLE);
		}

		// timeSetMode.setOnCheckedChangeListener(this);
	}

	private void registerWheelListener() {
		yearWheel.addChangingListener(this);
		monthWheel.addChangingListener(this);
		dayWheel.addChangingListener(this);
		hourWheel.addChangingListener(this);
		minuteWheel.addChangingListener(this);
	}

	private void unRegisterWheelListener() {
		yearWheel.removeChangingListener(this);
		monthWheel.removeChangingListener(this);
		dayWheel.removeChangingListener(this);
		hourWheel.removeChangingListener(this);
		minuteWheel.removeChangingListener(this);
	}

	@Override
	public void onClick(View v) {
		int resId = v.getId();
		switch (resId) {
		case R.id.year_add:
			manualSetTime(YEARSETTING, true);
			break;
		case R.id.year_decre:
			manualSetTime(YEARSETTING, false);
			break;
		case R.id.month_add:
			manualSetTime(MONTHSETTING, true);
			break;
		case R.id.month_decre:
			manualSetTime(MONTHSETTING, false);
			break;
		case R.id.day_add:
			manualSetTime(DAYSETTING, true);
			break;
		case R.id.day_decre:
			manualSetTime(DAYSETTING, false);
			break;
		case R.id.hour_add:
			manualSetTime(HOURSETTING, true);
			break;
		case R.id.hour_decre:
			manualSetTime(HOURSETTING, false);
			break;
		case R.id.minute_add:
			manualSetTime(MINUTESETTING, true);
			break;
		case R.id.minute_decre:
			manualSetTime(MINUTESETTING, false);
			break;
		}
	}

	private void initDay() {
		Calendar c = Calendar.getInstance();

		int days = c.getActualMaximum(Calendar.DAY_OF_MONTH);
		dayContent = new String[days];
		for (int i = 0; i < days; i++) {
			dayContent[i] = String.valueOf(i + 1);
			if (dayContent[i].length() < 2) {
				dayContent[i] = "0" + dayContent[i];
			}
		}
		dayWheel.setAdapter(new StrericWheelAdapter(dayContent));
		monthWheel.setCurrentItem(c.get(Calendar.MONTH) + 1 - 1);
		dayWheel.setCyclic(true);
		dayWheel.setInterpolator(new AnticipateOvershootInterpolator());
	}

	private void initUiDays() {
		int currentYear = Integer.valueOf(yearWheel.getCurrentItemValue());
		int currentMonth = Integer.valueOf(monthWheel.getCurrentItemValue());
		int currentDay = Integer.valueOf(dayWheel.getCurrentItemValue());

		LogUtils.d("current daywheel value: " + monthWheel.getCurrentItem());
		int days = MONTH_DAYS[monthWheel.getCurrentItem() + 1];
		if (currentMonth == 2 && !isLeapYear(currentYear + START_YEARS)) {
			if (currentDay == 30 || currentDay == 31)
				dayWheel.setCurrentItem(28);
			days = 29;
		} else if (currentMonth == 2 && isLeapYear(currentYear + START_YEARS)) {
			if (currentDay == 29 || currentDay == 30 || currentDay == 31)
				dayWheel.setCurrentItem(27);
			days = 28;
		} else if (currentMonth == 4 || currentMonth == 6 || currentMonth == 9
				|| currentMonth == 11) {
			if (currentDay == 31)
				dayWheel.setCurrentItem(29);
			days = 30;
		}

		dayContent = null;
		dayContent = new String[days];
		for (int i = 0; i < days; i++) {
			dayContent[i] = String.valueOf(i + 1);
			if (dayContent[i].length() < 2) {
				dayContent[i] = "0" + dayContent[i];
			}
		}
		dayWheel.setAdapter(new StrericWheelAdapter(dayContent));
		dayWheel.setCyclic(true);
		dayWheel.setInterpolator(new AnticipateOvershootInterpolator());
	}

	public void initContent() {
		Calendar calendar = Calendar.getInstance();

		yearContent = new String[MAX_YEARS];
		for (int i = 0; i < MAX_YEARS; i++)
			yearContent[i] = String.valueOf(i + START_YEARS);

		monthContent = new String[12];
		for (int i = 0; i < 12; i++) {
			monthContent[i] = String.valueOf(i + 1);
			if (monthContent[i].length() < 2) {
				monthContent[i] = "0" + monthContent[i];
			}
		}

		int days = calendar.getActualMaximum(Calendar.DATE);
		dayContent = new String[days];
		for (int i = 0; i < days; i++) {
			dayContent[i] = String.valueOf(i + 1);
			if (dayContent[i].length() < 2) {
				dayContent[i] = "0" + dayContent[i];
			}
		}

		hourContent = new String[24];
		for (int i = 0; i < 24; i++) {
			int t = i;
			hourContent[i] = String.valueOf(t);
			if (hourContent[i].length() < 2) {
				hourContent[i] = "0" + hourContent[i];
			}
		}

		minuteContent = new String[60];
		for (int i = 0; i < 60; i++) {
			minuteContent[i] = String.valueOf(i);
			if (minuteContent[i].length() < 2) {
				minuteContent[i] = "0" + minuteContent[i];
			}
		}
		int curYear = calendar.get(Calendar.YEAR);
		int curMonth = calendar.get(Calendar.MONTH);
		Log.d("monthtmp", "curMonth: " + curYear);
		int curDay = calendar.get(Calendar.DAY_OF_MONTH);
		int curHour = calendar.get(Calendar.HOUR_OF_DAY);
		int curMinute = calendar.get(Calendar.MINUTE);
		unRegisterWheelListener();

		yearWheel.setAdapter(new StrericWheelAdapter(yearContent));
		yearWheel.setCurrentItem(curYear - START_YEARS);
		yearWheel.setCyclic(true);
		yearWheel.setInterpolator(new AnticipateOvershootInterpolator());

		monthWheel.setAdapter(new StrericWheelAdapter(monthContent));
		monthWheel.setCurrentItem(curMonth);
		monthWheel.setCyclic(true);
		monthWheel.setInterpolator(new AnticipateOvershootInterpolator());

		dayWheel.setAdapter(new StrericWheelAdapter(dayContent));
		dayWheel.setCurrentItem(curDay - 1);
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

		registerWheelListener();
		initWheel = true;
	}

	@Override
	public void onChanged(WheelView wheel, int oldValue, int newValue) {
		// if (!initWheel) {
		// return;
		// }
		Log.i("wheelchanged", "onChanged: " + initWheel);
		LogUtils.d("wheelchanged");
		switch (wheel.getId()) {
		case R.id.yearwheel:
		case R.id.monthwheel:
		case R.id.daywheel:
		case R.id.hourwheel:
		case R.id.minutewheel:
			isTimeWheelChanged = true;
			// mHandler.removeMessages(ID_SET_DATETIME);
			// Message msg = Message.obtain();
			// msg.what = ID_SET_DATETIME;
			// msg.obj = wheel;
			// msg.arg1 = oldValue;
			// msg.arg2 = newValue;
			// mHandler.sendMessageDelayed(msg, 100);
			// mHandler.sendEmptyMessageDelayed(ID_SET_DATETIME, 100);
			break;
		}

	}

	private static final int ID_SET_DATETIME = 0x1001;
	private static final int GPS_SET_DATETIME = 0x1002;
	@SuppressLint("HandlerLeak")
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ID_SET_DATETIME:
				mHandler.removeMessages(ID_SET_DATETIME);
				try {
					// setDateTime((WheelView) msg.obj, msg.arg1, msg.arg2);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case GPS_SET_DATETIME:
				mHandler.removeMessages(ID_SET_DATETIME);
				initGpsTime((Long) msg.obj);
				break;
			}
		}
	};

	private void manualSetTime(int whichWheel, boolean isAdd) {
		int setValue = 0;
		switch (whichWheel) {
		case YEARSETTING:
			setValue = isAdd ? (yearWheel.getCurrentItem() + 1) : (yearWheel
					.getCurrentItem() - 1);
			yearWheel.setCurrentItem(setValue, false);
			initUiDays();
			break;
		case MONTHSETTING:
			setValue = isAdd ? (monthWheel.getCurrentItem() + 1) : (monthWheel
					.getCurrentItem() - 1);
			monthWheel.setCurrentItem(setValue, false);
			initUiDays();
			break;
		case DAYSETTING:
			setValue = isAdd ? (dayWheel.getCurrentItem() + 1) : (dayWheel
					.getCurrentItem() - 1);
			dayWheel.setCurrentItem(setValue, false);
			break;
		case HOURSETTING:
			setValue = isAdd ? (hourWheel.getCurrentItem() + 1) : (hourWheel
					.getCurrentItem() - 1);
			hourWheel.setCurrentItem(setValue, false);
			break;
		case MINUTESETTING:
			setValue = isAdd ? (minuteWheel.getCurrentItem() + 1)
					: (minuteWheel.getCurrentItem() - 1);
			minuteWheel.setCurrentItem(setValue, false);
			break;
		default:
			break;
		}
	}

	// private void setDateTime(WheelView wheelView, int oldValue, int newValue)
	// {
	//
	// currentYear = Integer.valueOf(yearWheel.getCurrentItemValue());
	// currentMonth = Integer.valueOf(monthWheel.getCurrentItemValue());
	// currentDay = Integer.valueOf(dayWheel.getCurrentItemValue());
	// currentHour = Integer.valueOf(hourWheel.getCurrentItemValue());
	// currentMinute = Integer.valueOf(minuteWheel.getCurrentItemValue());
	//
	// if (wheelView.getId() == R.id.monthwheel) {
	// LogUtils.d("WheelView: " + wheelView.getId() + "oldvalue: "
	// + oldValue + "newValue: " + newValue);
	// // int oldMaxDayOfMonth = day;
	// // int newMaxDayOfMonth = MONTH_DAYS[newValue + 1];
	// // LogUtils.d("oldMaxDayOfMonth: " + oldMaxDayOfMonth +
	// // ", newMaxDayOfMonth: " + newMaxDayOfMonth);
	// // if(oldMaxDayOfMonth > newMaxDayOfMonth)
	// // day = newMaxDayOfMonth;
	// currentDay = getDaysWhenMonthChanged(currentDay, newValue);
	// if (!isLeapYear(currentYear) && currentDay == 28)
	// currentDay += 1;
	// } else if (wheelView.getId() == R.id.yearwheel) {
	// LogUtils.d("year WheelView: " + wheelView.getId() + "oldvalue: "
	// + oldValue + "newValue: " + newValue);
	// if (currentMonth == 2 && (currentDay == 29))
	// currentDay = getDaysWhenYearChanged(oldValue + START_YEARS,
	// newValue + START_YEARS, currentDay);
	//
	// LogUtils.d("yearchanged£º " + currentDay);
	// }
	// }

	private int getDaysWhenMonthChanged(int day, int newValue) {
		int fitDays = day;
		int newMaxDayOfMonth = MONTH_DAYS[newValue + 1];
		LogUtils.d("oldMaxDayOfMonth: " + day + ", newMaxDayOfMonth: "
				+ newMaxDayOfMonth);
		if (fitDays > newMaxDayOfMonth)
			fitDays = newMaxDayOfMonth;
		return fitDays;
	}

	private int getDaysWhenYearChanged(int oldValue, int newValue, int day) {
		LogUtils.d("getDaysWhenYearChanged: " + day + "isLeapYear(oldValue): "
				+ isLeapYear(oldValue) + "isLeapYear(newValue): "
				+ isLeapYear(newValue));
		// if(isLeapYear(oldValue) && !isLeapYear(newValue))
		// return day;
		// else
		if (!isLeapYear(oldValue) && isLeapYear(newValue))
			return day - 1;
		return 0;
		// else
		// return day;
		// }else
	}

	private void setIPCTime() {
		int year = Integer.valueOf(yearWheel.getCurrentItemValue());
		int month = Integer.valueOf(monthWheel.getCurrentItemValue());
		int day = Integer.valueOf(dayWheel.getCurrentItemValue());
		int hour = Integer.valueOf(hourWheel.getCurrentItemValue());
		int minute = Integer.valueOf(minuteWheel.getCurrentItemValue());

		Calendar now = Calendar.getInstance();
		int second = now.get(Calendar.SECOND);
		LogUtils.d("now second: " + second + ", year: " + (year - START_YEARS));
		int mode = getUiTimeMode();
		LogUtils.d("set IPC mode: " + mode);
		
		try {
			iCanbusService.writeDateTime((year - START_YEARS), month, day,
					hour, minute, second, mode);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		// Utils.putCarSettingsString(getActivity().getContentResolver(),
		// SettingsProvider.AUTO_TIME,
		// mode == 1 ? F70CarSettingCommand.LOCAL_CLOSE
		// : F70CarSettingCommand.LOCAL_OPEN);
	}

	private void setIPCTimeMode() {
		int mode = getUiTimeMode();
		LogUtils.d("set IPC mode: " + mode);
		
		try {
			iCanbusService.writeDateTime(127, 15, 0, 31, 63, 63, mode);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		Utils.putCarSettingsString(getActivity().getContentResolver(),
				SettingsProvider.AUTO_TIME,
				mode == 1 ? F70CarSettingCommand.LOCAL_CLOSE
						: F70CarSettingCommand.LOCAL_OPEN);
	}

	private int getUiTimeMode() {
		return autoRb.isChecked() ? 0 : 1;
	}

	private void setDateAndTime(int year, int month, int day, int hour,
			int minute) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month - 1);
		c.set(Calendar.DAY_OF_MONTH, day);
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		long now = c.getTimeInMillis();

		if (!SystemClock.setCurrentTimeMillis(now))
			LogUtils.d("F70 set time failed!!!");
	}

	/* package */
	private static void setDate(Context context, int year, int month, int day) {
		Calendar c = Calendar.getInstance();
		Log.d("monthtmp", "setDate: " + month);
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month - 1);
		c.set(Calendar.DAY_OF_MONTH, day);
		long when = c.getTimeInMillis();

		// if (when / 1000 < Integer.MAX_VALUE) {
		// ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE))
		// .setTime(when);
		// }
		boolean isSuc = SystemClock.setCurrentTimeMillis(when);
	}

	private boolean isLeapYear(int year) {
		LogUtils.d("in year: " + year);
		return (year % 4 == 0 && year % 100 != 0 || year % 400 == 0) ? false
				: true;
	}

	/* package */
	private static void setTime(Context context, int hourOfDay, int minute) {
		Calendar c = Calendar.getInstance();

		c.set(Calendar.HOUR_OF_DAY, hourOfDay);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		long when = c.getTimeInMillis();

		// if (when / 1000 < Integer.MAX_VALUE) {
		((AlarmManager) context.getSystemService(Context.ALARM_SERVICE))
				.setTime(when);
		// }
	}

	/**
	 *
	 */
	private void setTimeWidgetEnabled(String enabled) {
		boolean value = enabled.equals(F70CarSettingCommand.LOCAL_CLOSE);
		LogUtils.d("time mode is " + (value ? "manaul" : "auto"));
//		yearWheel.setIsScrolled(value);
//		monthWheel.setIsScrolled(value);
//		dayWheel.setIsScrolled(value);
//		hourWheel.setIsScrolled(value);
//		minuteWheel.setIsScrolled(value);
		yearWheel.setTextEnabled(value);
		monthWheel.setTextEnabled(value);
		dayWheel.setTextEnabled(value);
		hourWheel.setTextEnabled(value);
		minuteWheel.setTextEnabled(value);
		timeColon.setTextColor(value ? getActivity().getResources().getColor(
				R.color.white) : getActivity().getResources().getColor(
				R.color.gray));
		yearAdd.setEnabled(value);
		yearDecre.setEnabled(value);
		monthAdd.setEnabled(value);
		monthDecre.setEnabled(value);
		dayAdd.setEnabled(value);
		dayDecre.setEnabled(value);
		hourAdd.setEnabled(value);
		hourDecre.setEnabled(value);
		minuteAdd.setEnabled(value);
		minuteDecre.setEnabled(value);
	}

	/**
	 *
	 */
	private void setWidgetTime() {
		initDay();
		Calendar calendar = Calendar.getInstance();
		int curYear = calendar.get(Calendar.YEAR);
		// int curMonth = calendar.get(Calendar.MONTH);
		// Log.d("monthtmp", "curMonth: " + curMonth);
		int curDay = calendar.get(Calendar.DATE);
		Log.d("daytmp", "curDay: " + curDay);
		int curHour = calendar.get(Calendar.HOUR_OF_DAY);
		int curMinute = calendar.get(Calendar.MINUTE);

		yearWheel.setCurrentItem(curYear - START_YEARS);

		// monthWheel.setCurrentItem(curMonth);

		dayWheel.setCurrentItem(curDay - 1);

		hourWheel.setCurrentItem(curHour);

		minuteWheel.setCurrentItem(curMinute);
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.time_manual:
			setTimeWidgetEnabled(F70CarSettingCommand.LOCAL_CLOSE);
			if (isAlreadyRegisGpsStatus)
				unRegisGpsService();
			break;
		case R.id.time_auto:
			setTimeWidgetEnabled(F70CarSettingCommand.LOCAL_OPEN);
			if (!isAlreadyRegisGpsStatus)
				regisGpsService();
			break;

		default:
			break;
		}
		isModeChanged = true;
	}

	IGpsStatusListener iGpsStatusListener = new IGpsStatusListener.Stub() {

		@Override
		public void onReceived(GpsStatus gpsStatus) throws RemoteException {
			LogUtils.d("value: " + gpsStatus.getFlags()
					+ ", GpsStatus.GPS_HAS_TIME: " + GpsStatus.GPS_HAS_TIME);
			if ((gpsStatus.getFlags() & 0x02) == GpsStatus.GPS_HAS_TIME) {
				long time = gpsStatus.getTime();
				LogUtils.d("gps time: " + time);
				Message message = Message.obtain();
				message.what = GPS_SET_DATETIME;
				message.obj = time;
				mHandler.sendMessageDelayed(message, 100);
			} else
				LogUtils.d("gps time invalid");

		}
	};
}
