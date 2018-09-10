package com.hwatong.f70.carsetting;


import com.hwatong.f70.baseview.BaseFragment;
import com.hwatong.f70.main.F70CanbusUtils;
import com.hwatong.f70.main.LogUtils;
import com.hwatong.settings.R;
import com.hwatong.settings.widget.SwitchButton;

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
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;


public class DoorSetting extends BaseFragment implements OnCheckedChangeListener {

	private SwitchButton autoDropLockSwitch, autoDealLockswitch,
			autoRepeatLockSwitch, singleDoorSwitch, downLockWithtWinkleSwitch,
			dealLockWithtWinkleSwitch;

	private ICanbusService iCanbusService;
	private static final int BUTTON_RESTORE = 0X01;
	private static final int HANDLE_CARCONFIG = 0X02;
	private static final int BUTTON_LIMIT_SPEED = 300;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.f70_doorsetting, container,
				false);

		initWidget(rootView);
		initService();
		return rootView;
	}

	@Override
	public void onResume() {
		initDoor();
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

	
	private void initDoor() {
		try {
			CarConfig carConfig = iCanbusService.getLastCarConfig(getActivity()
					.getPackageName());
			if (carConfig != null)
				handleCarConfigChanged(carConfig);
			else
				LogUtils.d("carConfig is null");
		} catch (RemoteException e) {
			LogUtils.d(e.toString());
			e.printStackTrace();
		}
	}

	private void initWidget(View rootView) {
		autoDropLockSwitch = (SwitchButton) rootView
				.findViewById(R.id.switch_auto_droplock);
		autoDealLockswitch = (SwitchButton) rootView
				.findViewById(R.id.switch_auto_deallock);
		autoRepeatLockSwitch = (SwitchButton) rootView
				.findViewById(R.id.switch_auto_repeatlock);
		singleDoorSwitch = (SwitchButton) rootView
				.findViewById(R.id.switch_singledoor);
		downLockWithtWinkleSwitch = (SwitchButton) rootView
				.findViewById(R.id.switch_downlock_withtwinkle);
		dealLockWithtWinkleSwitch = (SwitchButton) rootView
				.findViewById(R.id.switch_deallock_withtwinkle);
		
//		autoDropLockSwitch.setNoNeedAutoFeedback(true);
//		autoDealLockswitch.setNoNeedAutoFeedback(true);
//		autoRepeatLockSwitch.setNoNeedAutoFeedback(true);
//		singleDoorSwitch.setNoNeedAutoFeedback(true);
//		downLockWithtWinkleSwitch.setNoNeedAutoFeedback(true);
//		dealLockWithtWinkleSwitch.setNoNeedAutoFeedback(true);

//		autoDropLockSwitch.setOnCheckedChangeListener(this);
//		autoDealLockswitch.setOnCheckedChangeListener(this);
//		autoRepeatLockSwitch.setOnCheckedChangeListener(this);
//		singleDoorSwitch.setOnCheckedChangeListener(this);
//		downLockWithtWinkleSwitch.setOnCheckedChangeListener(this);
//		dealLockWithtWinkleSwitch.setOnCheckedChangeListener(this);
		registerSwitchButton();
	}
	
	private void registerSwitchButton() {
		autoDropLockSwitch.setOnCheckedChangeListener(this);
		autoDealLockswitch.setOnCheckedChangeListener(this);
		autoRepeatLockSwitch.setOnCheckedChangeListener(this);
		singleDoorSwitch.setOnCheckedChangeListener(this);
		downLockWithtWinkleSwitch.setOnCheckedChangeListener(this);
		dealLockWithtWinkleSwitch.setOnCheckedChangeListener(this);
	}

	private void unRegisterSwitchButton() {
		autoDropLockSwitch.setOnCheckedChangeListener(null);
		autoDealLockswitch.setOnCheckedChangeListener(null);
		autoRepeatLockSwitch.setOnCheckedChangeListener(null);
		singleDoorSwitch.setOnCheckedChangeListener(null);
		downLockWithtWinkleSwitch.setOnCheckedChangeListener(null);
		dealLockWithtWinkleSwitch.setOnCheckedChangeListener(null);
	}
	
//	@Override
//	public void onUserCheckedChanged(CompoundButton buttonView,
//			boolean isChecked) {
//
//	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		int buttonId = buttonView.getId();
		int type = -1;
		int msg = -1;
		switch (buttonId) {
		case R.id.switch_auto_droplock: // 自动落锁
			type = F70CarSettingCommand.TYPE_AUTODROPLOCK;
			msg = MSG_SWITCH_AUTO_DROPLOCK;
			break;

		case R.id.switch_auto_deallock:// 自动解锁
			type = F70CarSettingCommand.TYPE_AUTODEALLOCK;
			msg = MSG_SWITCH_AUTO_DEALLOCK;
			break;

		case R.id.switch_auto_repeatlock:// 自动重锁
			type = F70CarSettingCommand.TYPE_AUTOREPEATLOCK;
			msg = MSG_SWITCH_AUTO_REPEATLOCK;
			break;

		case R.id.switch_singledoor:// 单门解锁
			type = F70CarSettingCommand.TYPE_REMOTEUNLOCK;
			msg = MSG_SWITCH_SINGLEDOOR;
			break;

		case R.id.switch_downlock_withtwinkle:// 落锁闪烁
			type = F70CarSettingCommand.TYPE_DOWNLOCKWITHTWINKLE;
			msg = MSG_SWITCH_DOWNLOCK_WITHTWINKLE;
			break;

		case R.id.switch_deallock_withtwinkle:// 解锁闪烁
			type = F70CarSettingCommand.TYPE_DEALLOCKWITHTWINKLE;
			msg = MSG_SWITCH_DEALLOCK_WITHTWINKLE;
			break;

		default:
			break;
		}
		
		//限制UI操作速度
		buttonView.setEnabled(false);
		Message buttonMessage = Message.obtain();
		buttonMessage.what = BUTTON_RESTORE;
		buttonMessage.obj = buttonView;
		handler.sendMessageDelayed(buttonMessage, BUTTON_LIMIT_SPEED);
		
		Message.obtain(handler, msg, type, 0, isChecked).sendToTarget();
		
		if(type == -1)
			LogUtils.d("canbus error: ui error");
//		if(type == F70CarSettingCommand.TYPE_REMOTEUNLOCK)
//			F70CanbusUtils.getInstance().writeCarConfig(
//					iCanbusService,
//					type,
//					isChecked ? F70CarSettingCommand.DRIVER_DOOR
//							: F70CarSettingCommand.ALL_DOORS);
//		else
//			F70CanbusUtils.getInstance().writeCarConfig(
//					iCanbusService,
//					type,
//					isChecked ? F70CarSettingCommand.OPEN
//							: F70CarSettingCommand.CLOSE);
	}
	
	
	private static final int MSG_SWITCH_AUTO_DROPLOCK = 0X08;
	private static final int MSG_SWITCH_AUTO_DEALLOCK = 0X09;
	private static final int MSG_SWITCH_AUTO_REPEATLOCK = 0X0a;
	private static final int MSG_SWITCH_SINGLEDOOR = 0X0b;
	private static final int MSG_SWITCH_DOWNLOCK_WITHTWINKLE = 0X0c;
	private static final int MSG_SWITCH_DEALLOCK_WITHTWINKLE = 0X0d;
	
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case HANDLE_CARCONFIG:				
				handleCarConfigChanged((CarConfig) msg.obj);
				break;
			case BUTTON_RESTORE:
				((CompoundButton) msg.obj).setEnabled(true);
				break;
			case MSG_SWITCH_AUTO_DEALLOCK:
			case MSG_SWITCH_AUTO_DROPLOCK:
			case MSG_SWITCH_AUTO_REPEATLOCK:
			case MSG_SWITCH_DOWNLOCK_WITHTWINKLE:
			case MSG_SWITCH_DEALLOCK_WITHTWINKLE:
				F70CanbusUtils.getInstance().writeCarConfig(
						iCanbusService,
						msg.arg1,
						(Boolean) msg.obj ? F70CarSettingCommand.OPEN
								: F70CarSettingCommand.CLOSE);
				break;
			case MSG_SWITCH_SINGLEDOOR:
				F70CanbusUtils.getInstance().writeCarConfig(
						iCanbusService,
						msg.arg1,
						(Boolean) msg.obj ? F70CarSettingCommand.DRIVER_DOOR
								: F70CarSettingCommand.ALL_DOORS);
				break;
			default:
				break;
			}
		}

	};

	/**
	 *
	 */
	private void handleCarConfigChanged(CarConfig carConfig) {
		LogUtils.d("AutoDropLockStatus: " + carConfig.getStatus3()
				+ ", AutoDealLockStatus: " + carConfig.getStatus8()
				+ ", AutoRepeatLockStatus: " + carConfig.getStatus7()
				+ "DealLockWithtWinkleStatus: " + carConfig.getStatus5()
				+ "SingleDealLockStatus: " + carConfig.getStatus6()
				+ ", DownLockWithtWinkleStatus:" + carConfig.getStatus18());
		
		unRegisterSwitchButton();
		
		updateAutoDropLockStatus(carConfig.getStatus3());
		updateAutoDealLockStatus(carConfig.getStatus8());
		updateAutoRepeatLockStatus(carConfig.getStatus7());
		updateDealLockWithtWinkleStatus(carConfig.getStatus5());
		updateSingleDealLockStatus(carConfig.getStatus6());
		updateDownLockWithtWinkleStatus(carConfig.getStatus18());
		
		registerSwitchButton();
	}

	/**
	 *
	 */
	private void updateAutoDropLockStatus(int status) {
		autoDropLockSwitch
				.setChecked(status == F70CarSettingCommand.OPEN ? true : false);
	}

	/**
	 *
	 */
	private void updateAutoDealLockStatus(int status) {
		autoDealLockswitch
				.setChecked(status == F70CarSettingCommand.OPEN ? true : false);
	}

	/**
	 *
	 */
	private void updateAutoRepeatLockStatus(int status) {
		autoRepeatLockSwitch
				.setChecked(status == F70CarSettingCommand.OPEN ? true : false);
	}

	/**
	 *
	 */
	private void updateSingleDealLockStatus(int status) {
		singleDoorSwitch
				.setChecked(status == F70CarSettingCommand.DRIVER_DOOR ? true
						: false);
	}

	/**
	 *
	 */
	private void updateDownLockWithtWinkleStatus(int status) {
		downLockWithtWinkleSwitch
				.setChecked(status == F70CarSettingCommand.OPEN ? true : false);
	}

	/**
	 *
	 */
	private void updateDealLockWithtWinkleStatus(int status) {
		dealLockWithtWinkleSwitch
				.setChecked(status == F70CarSettingCommand.OPEN ? true : false);
	}

	ICarConfigListener iCarConfigListener = new ICarConfigListener.Stub() {

		@Override
		public void onReceived(CarConfig carConfig) throws RemoteException {
			LogUtils.d("AutoDropLockStatus:" + carConfig.getStatus3()
					+ ", AutoDealLockStatus:" + carConfig.getStatus8()
					+ ", AutoRepeatLockStatus:" + carConfig.getStatus7()
					+ ", DealLockWithtWinkleStatus:" + carConfig.getStatus5()
					+ ", DownLockWithtWinkleStatus:" + carConfig.getStatus18());
			Message configMessage = Message.obtain();
			configMessage.what = HANDLE_CARCONFIG;
			configMessage.obj = carConfig;
			handler.sendMessage(configMessage);
		}
	};

	ICarStatusListener iCarStatusListener = new ICarStatusListener.Stub() {

		@Override
		public void onReceived(CarStatus carStatus) throws RemoteException {

		}
	};

}
