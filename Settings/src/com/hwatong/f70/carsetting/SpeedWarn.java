package com.hwatong.f70.carsetting;

import com.hwatong.f70.baseview.BaseFragment;
import com.hwatong.f70.main.LogUtils;
import com.hwatong.settings.R;
import com.hwatong.settings.widget.SwitchButton;

import android.app.Fragment;
import android.net.Uri;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 
 * @author ljw
 * 
 */
public class SpeedWarn extends BaseFragment implements OnClickListener,
		OnCheckedChangeListener {
	private ImageButton speedAdd, speedDecre;
	private SwitchButton speedWarnSwitch;
	private TextView speedWarnValue, speedWarnUnit;
	private static final int DEFAULT_SPEED_VALUE = 30;
	private final static int MAXSPEED = 220;
	private final static int MINSPEED = DEFAULT_SPEED_VALUE;
	private final static int SPEED_STEP = 5;
	private static final int BUTTON_RESTORE = 0X08;//按钮恢复可用
	private static final int BUTTON_LIMIT_SPEED = 300;//UI限制操作1000毫秒

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.f70_speedwarn, container,
				false);

		initWidget(rootView);

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		changedActivityImage(this.getClass().getName());
	}

	@Override
	public void onDestroy() {
		handler.removeCallbacks(null);

		super.onDestroy();
	}

	private void initWidget(View rootView) {
		speedAdd = (ImageButton) rootView.findViewById(R.id.speed_add);
		speedDecre = (ImageButton) rootView.findViewById(R.id.speed_decre);
		speedAdd.setOnClickListener(this);
		speedDecre.setOnClickListener(this);

		speedWarnValue = (TextView) rootView.findViewById(R.id.speed_warn_value);
		speedWarnUnit = (TextView) rootView.findViewById(R.id.speed_warn_unit);

        String v1 = getCarSettingsString(this.getActivity().getContentResolver(), "overspeed_warning", "0");
        int v2 = getCarSettingsInt(this.getActivity().getContentResolver(), "limitative_vehicle", 0);

		speedWarnSwitch = (SwitchButton) rootView.findViewById(R.id.switch_speed_warn);
//		speedWarnSwitch.setNoNeedAutoFeedback(true);
		speedWarnSwitch.setChecked("1".equals(v1));
		speedWarnSwitch.setOnCheckedChangeListener(this);

		setButtonEnabled("1".equals(v1));

		speedWarnValue.setText(String.valueOf(v2 * SPEED_STEP + DEFAULT_SPEED_VALUE));
		
	}

	@Override
	public void onClick(View v) {
		int resId = v.getId();
		int value = 0;
		switch (resId) {
		case R.id.speed_add:
			value = getChangeSpeedWarnValue(true);
			value = setSpeedWarnValue(value);
			speedWarnValue.setText(String.valueOf(value));
			break;
		case R.id.speed_decre:
			value = getChangeSpeedWarnValue(false);
			value = setSpeedWarnValue(value);
			speedWarnValue.setText(String.valueOf(value));
			break;
		default:
			break;
		}
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(msg.what == BUTTON_RESTORE) {
				((CompoundButton) msg.obj).setEnabled(true);
			}
		}
	};

	private int getChangeSpeedWarnValue(boolean isAdd) {
		int changeValue = 0;
		int currentValue = Integer.parseInt(speedWarnValue.getText().toString().trim());
		if (isAdd) { 
			changeValue = currentValue + SPEED_STEP;
			if (changeValue <= MAXSPEED) {
				return changeValue;
			} else
				
				return currentValue;
		} else {
			changeValue = currentValue - SPEED_STEP;
			if (changeValue >= MINSPEED) {
				return changeValue;
			} else
				
				return DEFAULT_SPEED_VALUE;
		}
	}

	
	private int setSpeedWarnValue(int value) {
		value = (value - 30) / SPEED_STEP;

        putCarSettingsString(this.getActivity().getContentResolver(), "limitative_vehicle", String.valueOf(value));

        return value * SPEED_STEP + 30;
	}

	private void setButtonEnabled(boolean isEnabled) {
		LogUtils.d("setButtonEnabled: " + isEnabled);
		speedAdd.setEnabled(isEnabled);
		speedDecre.setEnabled(isEnabled);
		speedWarnValue.setEnabled(isEnabled);
		speedWarnUnit.setEnabled(isEnabled);
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        putCarSettingsString(this.getActivity().getContentResolver(), "overspeed_warning", isChecked ? "1" : "0");

		LogUtils.d("speed set status: ");
		setButtonEnabled(isChecked);

		// 限制UI操作速度
		buttonView.setEnabled(false);
		Message buttonMessage = Message.obtain();
		buttonMessage.what = BUTTON_RESTORE;
		buttonMessage.obj = buttonView;
		handler.sendMessageDelayed(buttonMessage, BUTTON_LIMIT_SPEED);
	}

	public static final Uri CONTENT_URI = Uri.parse("content://car_settings/content");

    public static int getCarSettingsInt(ContentResolver cr, String name, int def) {
        String v = getCarSettingsString(cr, name);
        if (v != null) {
            try {
                return Integer.parseInt(v);
            } catch (NumberFormatException e) {
            }
        }
        return def;
    }

	public static String getCarSettingsString(ContentResolver cr, String name) {
		String[] select = new String[] { "value" };
		Cursor cursor = cr.query(CONTENT_URI, select, "name=?", new String[]{ name }, null);
		if (cursor == null)
			return null;
		String value = null;
		if (cursor.moveToFirst()) {
			value = cursor.getString(0);
		}
		cursor.close();
		return value;
	}

	public static String getCarSettingsString(ContentResolver cr, String name, String defaultValue) {
		String[] select = new String[] { "value" };
		Cursor cursor = cr.query(CONTENT_URI, select, "name=?", new String[]{ name }, null);
		String value = defaultValue;
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				value = cursor.getString(0);
			}
			cursor.close();
		}
		return value;
	}

	private static boolean putCarSettingsString(ContentResolver cr, String name, String value) {
		String[] select = new String[] { "value" };
		Cursor cursor = cr.query(CONTENT_URI, select, "name=?", new String[]{ name }, null);
		if (cursor != null && cursor.moveToFirst()) {
			if (cursor != null)
				cursor.close();
			ContentValues values = new ContentValues();
			values.put("value", value);
			cr.update(CONTENT_URI, values, "name=?", new String[]{ name });
		} else {
			if (cursor != null)
				cursor.close();
			ContentValues values = new ContentValues();
			values.put("name", name);
			values.put("value", value);
			cr.insert(CONTENT_URI, values);
		}
		return true;
	}

}
