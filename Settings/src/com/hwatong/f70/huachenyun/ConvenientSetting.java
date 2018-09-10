package com.hwatong.f70.huachenyun;

import com.hwatong.f70.baseview.BaseFragment;
import com.hwatong.f70.carsetting.F70CarSettingCommand;
import com.hwatong.f70.main.F70CanbusUtils;
import com.hwatong.f70.main.LogUtils;
import com.hwatong.settings.R;
import com.hwatong.settings.widget.SwitchButton;

import android.app.Fragment;
import android.canbus.CarConfig;
import android.canbus.ICanbusService;
import android.canbus.IRemoteFunctionListener;
import android.canbus.RemoteFunction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.Toast;

/**
 * @author PC
 *
 */
public class ConvenientSetting extends BaseFragment implements
		OnCheckedChangeListener {

	private SwitchButton faultRemindSwitch, arriveTrackSwitch,
			remoteOpenLightSwitch, remoteOpenMicSwitch, remoteOpenEngineSwitch,
			remoteOpenAirSwitch, remoteLockSwitch;

	private ICanbusService iCanbusService;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.f70_convenient_setting,
				container, false);
		initWidget(rootView);
		initService();
		return rootView;
	}

	@Override
	public void onResume() {
		initConvenientSetting();
		super.onResume();
		changedActivityImage(this.getClass().getName());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			iCanbusService.removeRemoteFunctionListener(iListener);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void initConvenientSetting() {
		try {
			RemoteFunction remoteFunction = iCanbusService
					.getLastRemoteFunction(getActivity().getPackageName());
			if (remoteFunction != null)
				handleRemoteFuntionChanged(remoteFunction);
			else
				LogUtils.d("remoteFunction is null");
		} catch (RemoteException e) {
			LogUtils.d(e.toString());
			e.printStackTrace();
		}
	}

	private void initService() {
		iCanbusService = ICanbusService.Stub.asInterface(ServiceManager
				.getService("canbus"));
		try {
			iCanbusService.addRemoteFunctionListener(iListener);
		} catch (RemoteException e) {
			LogUtils.d(e.toString());
			e.printStackTrace();
		}
	}

	private void initWidget(View rootView) {
		faultRemindSwitch = (SwitchButton) rootView
				.findViewById(R.id.switch_fault_remind);
		arriveTrackSwitch = (SwitchButton) rootView
				.findViewById(R.id.switch_arrive_track);
		remoteOpenLightSwitch = (SwitchButton) rootView
				.findViewById(R.id.switch_remote_open_findcar);
		remoteOpenMicSwitch = (SwitchButton) rootView
				.findViewById(R.id.switch_remote_open_mic);
		remoteOpenEngineSwitch = (SwitchButton) rootView
				.findViewById(R.id.switch_remote_open_engine);
		remoteOpenAirSwitch = (SwitchButton) rootView
				.findViewById(R.id.switch_remote_open_air);
		remoteLockSwitch = (SwitchButton) rootView
				.findViewById(R.id.switch_remote_lock);
		
//		faultRemindSwitch.setNoNeedAutoFeedback(true);
//		arriveTrackSwitch.setNoNeedAutoFeedback(true);
//		remoteOpenLightSwitch.setNoNeedAutoFeedback(true);
//		remoteOpenEngineSwitch.setNoNeedAutoFeedback(true);
//		remoteOpenAirSwitch.setNoNeedAutoFeedback(true);
//		remoteLockSwitch.setNoNeedAutoFeedback(true);

//		faultRemindSwitch.setOnCheckedChangeListener(this);
//		arriveTrackSwitch.setOnCheckedChangeListener(this);
//		remoteOpenLightSwitch.setOnCheckedChangeListener(this);
//		remoteOpenMicSwitch.setOnCheckedChangeListener(this);
//		remoteOpenEngineSwitch.setOnCheckedChangeListener(this);
//		remoteOpenAirSwitch.setOnCheckedChangeListener(this);
//		remoteLockSwitch.setOnCheckedChangeListener(this);
		registerSwitchButton();
	}
	
	private void registerSwitchButton() {
		faultRemindSwitch.setOnCheckedChangeListener(this);
		arriveTrackSwitch.setOnCheckedChangeListener(this);
		remoteOpenLightSwitch.setOnCheckedChangeListener(this);
		remoteOpenMicSwitch.setOnCheckedChangeListener(this);
		remoteOpenEngineSwitch.setOnCheckedChangeListener(this);
		remoteOpenAirSwitch.setOnCheckedChangeListener(this);
		remoteLockSwitch.setOnCheckedChangeListener(this);
	}

	private void unRegisterSwitchButton() {
		faultRemindSwitch.setOnCheckedChangeListener(null);
		arriveTrackSwitch.setOnCheckedChangeListener(null);
		remoteOpenLightSwitch.setOnCheckedChangeListener(null);
		remoteOpenMicSwitch.setOnCheckedChangeListener(null);
		remoteOpenEngineSwitch.setOnCheckedChangeListener(null);
		remoteOpenAirSwitch.setOnCheckedChangeListener(null);
		remoteLockSwitch.setOnCheckedChangeListener(null);
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		int buttonId = buttonView.getId();
		int type = -1;
		switch (buttonId) {
		case R.id.switch_fault_remind:// 故障提醒
			type = F70CarSettingCommand.TYPE_FAULTREMIND;
			LogUtils.d("ConvenientSetting onCheckedChanged:" + "故障提醒");
			break;

		case R.id.switch_arrive_track:// 到位追踪
			type = F70CarSettingCommand.TYPE_ARRIVETRACK;
			LogUtils.d("ConvenientSetting onCheckedChanged:" + "到位追踪");
			break;

		case R.id.switch_remote_open_findcar:// 远程开启\关闭寻车
			type = F70CarSettingCommand.TYPE_FINDMYCAR;
			LogUtils.d("ConvenientSetting onCheckedChanged:" + "远程开启关闭寻车");
			break;

		case R.id.switch_remote_open_engine:// 远程开启\关闭发动机
			type = F70CarSettingCommand.TYPE_OPENENGINE;
			LogUtils.d("ConvenientSetting onCheckedChanged:" + "远程开启关闭发动机");
			break;

		case R.id.switch_remote_open_air:// 远程开启\关闭空调
			type = F70CarSettingCommand.TYPE_OPENAIR;
			LogUtils.d("ConvenientSetting onCheckedChanged:" + "远程开启关闭空调");
			break;

		case R.id.switch_remote_lock:// 远程上锁\解锁
			type = F70CarSettingCommand.TYPE_REMOTELOCK;
			LogUtils.d("ConvenientSetting onCheckedChanged:" + "远程上锁解锁");
			break;

		default:
			break;
		}
		if (type == -1)
			LogUtils.d("canbus command error: ui error");
		Message.obtain(handler, MSG_REMOTE_SETTING, type, 0, isChecked).sendToTarget();
	}

	private static final int MSG_REMOTE_SETTING = 0x08;
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(msg.what == MSG_REMOTE_SETTING) {
				F70CanbusUtils.getInstance().writeRemoteFuntion(
						iCanbusService,
						msg.arg1,
						(Boolean) msg.obj ? F70CarSettingCommand.OPEN
								: F70CarSettingCommand.CLOSE);
			} else
				handleRemoteFuntionChanged((RemoteFunction) msg.obj);
		}

	};

	/**
	 * 处理车辆配置信息以及UI显示
	 */
	private void handleRemoteFuntionChanged(RemoteFunction remoteFunction) {
		LogUtils.d("switch_fault_remind: " + remoteFunction.getStatus2()
				+ ", switch_arrive_track: " + remoteFunction.getStatus3()
				+ ", switch_remote_open_engine: " + remoteFunction.getStatus1()
				+ ", switch_remote_open_air: " + remoteFunction.getStatus6()
				+ ", switch_remote_lock: " + remoteFunction.getStatus5());
		//有状态回调时不回调onCheckChanged方法
		unRegisterSwitchButton();
		
		updateSwitchFaultRemind(remoteFunction.getStatus2());
		updateSwitchArriveTrack(remoteFunction.getStatus3());
		updateSwitchRemoteOpenEngine(remoteFunction.getStatus1());
		updateSwitchRemoteOpenAir(remoteFunction.getStatus6());
		updateSwitchRemoteLock(remoteFunction.getStatus5());
		updateSwitchFindMyCar(remoteFunction.getStatus4());
		
		registerSwitchButton();
	}

	/**
	 * 更新故障提醒
	 */
	private void updateSwitchFaultRemind(int status) {
		faultRemindSwitch.setChecked(status == F70CarSettingCommand.OPEN ? true
				: false);
	}

	/**
	 * 更新到位追踪
	 */
	private void updateSwitchArriveTrack(int status) {
		arriveTrackSwitch.setChecked(status == F70CarSettingCommand.OPEN ? true
				: false);
	}
	
	/**
	 * 更新远程寻车
	 */
	private void updateSwitchFindMyCar(int status) {
		remoteOpenLightSwitch.setChecked(status == F70CarSettingCommand.OPEN ? true
				: false);
	}

	/**
	 * 更新远程开启\关闭发动机
	 */
	private void updateSwitchRemoteOpenEngine(int status) {
		remoteOpenEngineSwitch
				.setChecked(status == F70CarSettingCommand.OPEN ? true : false);
	}

	/**
	 * 更新远程开启\关闭空调
	 */
	private void updateSwitchRemoteOpenAir(int status) {
		remoteOpenAirSwitch
				.setChecked(status == F70CarSettingCommand.OPEN ? true : false);
	}

	/**
	 * 更新远程上锁\解锁
	 */
	private void updateSwitchRemoteLock(int status) {
		remoteLockSwitch.setChecked(status == F70CarSettingCommand.OPEN ? true
				: false);
	}
	
	IRemoteFunctionListener iListener = new IRemoteFunctionListener.Stub() {
		@Override
		public void onReceived(RemoteFunction remoteFunction)
				throws RemoteException {
			LogUtils.d("switch_fault_remind: " + remoteFunction.getStatus2()
					+ ", switch_arrive_track: " + remoteFunction.getStatus3()
					+ ", switch_remote_open_engine: "
					+ remoteFunction.getStatus1()
					+ ", switch_remote_open_air: "
					+ remoteFunction.getStatus6() + ", switch_remote_lock: "
					+ remoteFunction.getStatus5() + "find my car: " + remoteFunction.getStatus4());

//			Message configMessage = Message.obtain();
//			configMessage.what = 0x01;
//			configMessage.obj = remoteFunction;
//			handler.sendMessage(configMessage);
		}
	};

}
