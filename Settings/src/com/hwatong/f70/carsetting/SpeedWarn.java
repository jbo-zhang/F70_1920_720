package com.hwatong.f70.carsetting;

import com.hwatong.f70.baseview.BaseFragment;
import com.hwatong.f70.main.F70CanbusUtils;
import com.hwatong.f70.main.LogUtils;
import com.hwatong.settings.R;
import com.hwatong.settings.widget.SwitchButton;

import android.app.Fragment;
import android.canbus.CarConfig;
import android.canbus.ICanbusService;
import android.canbus.ICarConfigListener;
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

	private ICanbusService iCanbusService;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.f70_speedwarn, container,
				false);

		initWidget(rootView);
		initService();
		return rootView;
	}

	@Override
	public void onResume() {
		initSpeedWarn();
		super.onResume();
		changedActivityImage(this.getClass().getName());
	}
	
	private void initService() {
		iCanbusService = ICanbusService.Stub.asInterface(ServiceManager
				.getService("canbus"));
		try {
			iCanbusService.addCarConfigListener(iCarConfigListener);
		} catch (RemoteException e) {
			LogUtils.d(e.toString());
			e.printStackTrace();
		}
	}
	
	
	private void initSpeedWarn() {
		try {
			CarConfig carConfig = iCanbusService.getLastCarConfig(getActivity()
					.getPackageName());
			if (carConfig != null)
				handleCarConfigChanged(carConfig);
			else
				LogUtils.d("carConfig is null");
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			iCanbusService.removeCarConfigListener(iCarConfigListener);
		} catch (RemoteException e) {
			LogUtils.d(e.toString());
			e.printStackTrace();
		}

		handler.removeCallbacks(null);
	}

	private void initWidget(View rootView) {
		speedAdd = (ImageButton) rootView.findViewById(R.id.speed_add);
		speedDecre = (ImageButton) rootView.findViewById(R.id.speed_decre);
		speedAdd.setOnClickListener(this);
		speedDecre.setOnClickListener(this);

		speedWarnValue = (TextView) rootView
				.findViewById(R.id.speed_warn_value);
		speedWarnUnit = (TextView) rootView.findViewById(R.id.speed_warn_unit);

		speedWarnSwitch = (SwitchButton) rootView
				.findViewById(R.id.switch_speed_warn);
//		speedWarnSwitch.setNoNeedAutoFeedback(true);
		speedWarnSwitch.setOnCheckedChangeListener(this);
		
	}

	@Override
	public void onClick(View v) {
		int resId = v.getId();
		int value = 0;
		switch (resId) {
		case R.id.speed_add:
			value = getChangeSpeedWarnValue(true);
			setSpeedWarnValue(value);
			speedWarnValue.setText(String.valueOf(value));
			break;
		case R.id.speed_decre:
			value = getChangeSpeedWarnValue(false);
			setSpeedWarnValue(value);
			speedWarnValue.setText(String.valueOf(value));
			break;
		default:
			break;
		}
	}

	private static final int MSG_SPEED_WARN = 0x09;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(msg.what == BUTTON_RESTORE) {
				((CompoundButton) msg.obj).setEnabled(true);
			} else if(msg.what == MSG_SPEED_WARN) {
				F70CanbusUtils.getInstance().writeCarConfig(
						iCanbusService,
						F70CarSettingCommand.TYPE_SPEEDWARN,
						(Boolean) msg.obj ? F70CarSettingCommand.OPEN
								: F70CarSettingCommand.CLOSE);
			} else
				handleCarConfigChanged((CarConfig) msg.obj);
		}
	};

	/**
	 *
	 */
	private void handleCarConfigChanged(CarConfig carConfig) {
		LogUtils.d("getSpeedWarn open: " + carConfig.getStatus15() + ", speedvalue: " + carConfig.getStatus16());
		
//		speedWarnSwitch.setOnCheckedChangeListener(null);
		updateSpeedWarnStatus(carConfig.getStatus15());
		updateSpeedWarnValue(carConfig.getStatus16());
//		speedWarnSwitch.setOnCheckedChangeListener(this);
	}

	private void updateSpeedWarnStatus(int status) {
		speedWarnSwitch.setChecked(status == F70CarSettingCommand.OPEN ? true
				: false);
		setButtonEnabled(status == F70CarSettingCommand.OPEN);
	}
	
	private void updateSpeedWarnValue(int status) {
		String displaySpeedWarnValue = String.valueOf(status * SPEED_STEP + DEFAULT_SPEED_VALUE);
		speedWarnValue.setText(displaySpeedWarnValue);
	}

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

	
	private void setSpeedWarnValue(int value) {
		value = (value - 30) / SPEED_STEP;
		F70CanbusUtils.getInstance().writeCarConfig(iCanbusService,
				F70CarSettingCommand.TYPE_SPEEDWARNVALUE, value);
			LogUtils.d("speed set value: " + value);
	}

	ICarConfigListener iCarConfigListener = new ICarConfigListener.Stub() {

		@Override
		public void onReceived(CarConfig carConfig) throws RemoteException {
				LogUtils.d("updateSpeedWarnStatus:" + carConfig.getStatus2());
			Message configMessage = Message.obtain();
			configMessage.obj = carConfig;
			handler.sendMessage(configMessage);
		}
	};

	private void setButtonEnabled(boolean isEnabled) {
		LogUtils.d("setButtonEnabled: " + isEnabled);
		speedAdd.setEnabled(isEnabled);
		speedDecre.setEnabled(isEnabled);
		speedWarnValue.setEnabled(isEnabled);
		speedWarnUnit.setEnabled(isEnabled);
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		Message.obtain(handler, MSG_SPEED_WARN, 0, 0, isChecked).sendToTarget();
		LogUtils.d("speed set status: ");
		setButtonEnabled(isChecked);

		// 限制UI操作速度
		buttonView.setEnabled(false);
		Message buttonMessage = Message.obtain();
		buttonMessage.what = BUTTON_RESTORE;
		buttonMessage.obj = buttonView;
		handler.sendMessageDelayed(buttonMessage, BUTTON_LIMIT_SPEED);
	}

}
