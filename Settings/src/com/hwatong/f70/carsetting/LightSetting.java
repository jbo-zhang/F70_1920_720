package com.hwatong.f70.carsetting;

import com.hwatong.f70.baseview.BaseFragment;
import com.hwatong.f70.main.F70CanbusUtils;
import com.hwatong.f70.main.LogUtils;
import com.hwatong.settings.R;
import com.hwatong.settings.widget.SwitchButton;
import com.nforetek.bt.res.MsgOutline;

import android.app.Fragment;
import android.canbus.CarConfig;
import android.canbus.CarStatus;
import android.canbus.ICanbusService;
import android.canbus.ICarConfigListener;
import android.canbus.ICarStatusListener;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * 
 * @author ljw
 * 
 */
public class LightSetting extends BaseFragment implements OnCheckedChangeListener,
		android.widget.RadioGroup.OnCheckedChangeListener, OnClickListener {
	private SwitchButton dayCarLightSwitch;
	private RadioGroup lightTimeSelect;
	private RadioButton lightClose, light30s, light60s, light90s, light120s;

	private ICanbusService iCanbusService;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.f70_lightsetting, container,
				false);

		initWidget(rootView);
		initService();
		return rootView;
	}

	@Override
	public void onResume() {
		initLight();
		super.onResume();
		changedActivityImage(this.getClass().getName());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			iCanbusService.removeCarConfigListener(iCarConfigListener);
			iCanbusService.removeCarStatusListener(iCarStatusListener);
		} catch (RemoteException e) {
			LogUtils.d(e.toString());
			e.printStackTrace();
		}

		handler.removeCallbacks(null);
	}

	private void initService() {
		iCanbusService = ICanbusService.Stub.asInterface(ServiceManager
				.getService("canbus"));

		try {
			iCanbusService.addCarConfigListener(iCarConfigListener);
			iCanbusService.addCarStatusListener(iCarStatusListener);
		} catch (RemoteException e) {
			LogUtils.d(e.toString());
			e.printStackTrace();
		}
	}

	
	private void initLight() {
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

	private void initWidget(View rootView) {
		dayCarLightSwitch = (SwitchButton) rootView
				.findViewById(R.id.switch_day_carlight);
//		dayCarLightSwitch.setNoNeedAutoFeedback(true);
		dayCarLightSwitch.setOnCheckedChangeListener(this);

		lightTimeSelect = (RadioGroup) rootView
				.findViewById(R.id.lighttimeselect);
		lightTimeSelect.setOnCheckedChangeListener(this);

		lightClose = (RadioButton) rootView.findViewById(R.id.light_close);
		light30s = (RadioButton) rootView.findViewById(R.id.light_30s);
		light60s = (RadioButton) rootView.findViewById(R.id.light_60s);
		light90s = (RadioButton) rootView.findViewById(R.id.light_90s);
		light120s = (RadioButton) rootView.findViewById(R.id.light_120s);
		
		lightClose.setOnClickListener(this);
		light30s.setOnClickListener(this);
		light60s.setOnClickListener(this);
		light90s.setOnClickListener(this);
		light120s.setOnClickListener(this);
		
	}
	
//	@Override
//	public void onUserCheckedChanged(CompoundButton buttonView,
//			boolean isChecked) {
//
//	}
	private static final int MSG_LIGHT = 0x08;
	private static final int MSG_TIME_30S = 0x09;
	private static final int MSG_TIME_60S = 0x0a;
	private static final int MSG_TIME_90S = 0x0b;
	private static final int MSG_TIME_120S = 0x0c;
	private static final int MSG_FROMMEHOME_CLOSE = 0x0d;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_LIGHT:
				F70CanbusUtils.getInstance().writeCarConfig(
						iCanbusService,
						F70CarSettingCommand.TYPE_LIGHT,
						(Boolean) msg.obj ? F70CarSettingCommand.OPEN
								: F70CarSettingCommand.CLOSE);
				break;
			case MSG_FROMMEHOME_CLOSE:
			case MSG_TIME_120S:
			case MSG_TIME_30S:
			case MSG_TIME_60S:
			case MSG_TIME_90S:
				F70CanbusUtils.getInstance().writeCarConfig(iCanbusService,
						F70CarSettingCommand.TYPE_FOLLOWLIGHT,
						msg.arg1);
				break;
			case 0x01:				
				handleCarConfigChanged((CarConfig) msg.obj);
			default:
				break;
			}
		}
	};

	/**
	 *
	 */
	private void handleCarConfigChanged(CarConfig carConfig) {
		LogUtils.d("light:" + carConfig.getStatus4() + ", time:"
				+ carConfig.getStatus1());
		updateLightStatus(carConfig.getStatus4());
		
		
//		lightTimeSelect.setOnCheckedChangeListener(null);
		updateLightTime(carConfig.getStatus1());
//		lightTimeSelect.setOnCheckedChangeListener(this);
	}

	/**
	 * 
	 * 
	 * @param status
	 */
	private void updateLightStatus(int status) {
		dayCarLightSwitch
				.setChecked(status == F70CarSettingCommand.CLOSE ? false : true);
//		setRadioGroupEnabled(status == F70CarSettingCommand.OPEN);
	}

	/**
	 * 
	 */
	private void updateLightTime(int status) {
		switch (status) {
		case F70CarSettingCommand.TIME_30S:
			lightTimeSelect.check(R.id.light_30s);
			break;

		case F70CarSettingCommand.TIME_60S:
			lightTimeSelect.check(R.id.light_60s);
			break;

		case F70CarSettingCommand.TIME_90S:
			lightTimeSelect.check(R.id.light_90s);
			break;

		case F70CarSettingCommand.TIME_120S:
			lightTimeSelect.check(R.id.light_120s);
			break;

		default:
			break;
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int buttonId) {
		LogUtils.d("select light time");
		int command = -1;
		int type = 0;
		switch (buttonId) {
		case R.id.light_close:
			command = F70CarSettingCommand.CLOSE;
			type = MSG_FROMMEHOME_CLOSE;
			break;

		case R.id.light_30s:
			command = F70CarSettingCommand.TIME_30S;
			type = MSG_TIME_30S;
			break;

		case R.id.light_60s:
			command = F70CarSettingCommand.TIME_60S;
			type = MSG_TIME_60S;
			break;

		case R.id.light_90s:
			command = F70CarSettingCommand.TIME_90S;
			type = MSG_TIME_90S;
			break;

		case R.id.light_120s:
			command = F70CarSettingCommand.TIME_120S;
			type = MSG_TIME_120S;
			break;

		default:
			break;
		}
		LogUtils.d("set light error: ui error");
		Message.obtain(handler, type, command, 0, buttonId).sendToTarget();
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		Message.obtain(handler, MSG_LIGHT, 0, 0, isChecked).sendToTarget();
		LogUtils.d("change light status");
//		setRadioGroupEnabled(isChecked);
	}

	ICarConfigListener iCarConfigListener = new ICarConfigListener.Stub() {

		@Override
		public void onReceived(CarConfig carConfig) throws RemoteException {
			LogUtils.d("LightData:" + carConfig.getStatus1());
			Message configMessage = Message.obtain();
			configMessage.what = 0x01;
			configMessage.obj = carConfig;
			handler.sendMessage(configMessage);
		}
	};

	ICarStatusListener iCarStatusListener = new ICarStatusListener.Stub() {

		@Override
		public void onReceived(CarStatus carStatus) throws RemoteException {

		}
	};
	
	private void setRadioGroupEnabled(boolean isEnabled) {
        for (int i = 0; i < lightTimeSelect.getChildCount(); i++) {
        	lightTimeSelect.getChildAt(i).setEnabled(isEnabled);
        }
	}

	@Override
	public void onClick(View v) {
		
	}

}
