package com.hwatong.f70.huachenyun;

import com.hwatong.f70.carsetting.F70CarSettingCommand;
import com.hwatong.f70.main.F70CanbusUtils;
import com.hwatong.f70.main.LogUtils;
import com.hwatong.settings.R;
import com.hwatong.settings.widget.SwitchButton;

import android.app.Fragment;
import android.canbus.ICanbusService;
import android.canbus.IRemoteFunctionListener;
import android.canbus.RemoteFunction;
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

public class SmartNaviSetting extends Fragment implements
		OnCheckedChangeListener {

	private SwitchButton positionTocarSwitch;
	private SwitchButton receiveCarinfoSwitch;
	
	private ICanbusService iCanbusService;// 远程调用接口

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.f70_smartnavi, container,
				false);

		initWidget(rootView);
		initService();
		return rootView;
	}
	
	@Override
	public void onResume() {
		initConvenientSetting();
		super.onResume();
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
			RemoteFunction remoteFunction = iCanbusService.getLastRemoteFunction(getActivity().getPackageName());
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
		positionTocarSwitch = (SwitchButton) rootView
				.findViewById(R.id.switch_position_tocar);
		receiveCarinfoSwitch = (SwitchButton) rootView
				.findViewById(R.id.switch_receive_carinfo);

		positionTocarSwitch.setOnCheckedChangeListener(this);
		receiveCarinfoSwitch.setOnCheckedChangeListener(this);
	}

	@Override
	public void onCheckedChanged(CompoundButton button, boolean isChecked) {
		int buttonId = button.getId();
		switch (buttonId) {
		case R.id.switch_position_tocar:// 位置发送到车机
			F70CanbusUtils.getInstance().writeRemoteFuntion(
					iCanbusService,
					F70CarSettingCommand.TYPE_POSITIONTOCAR,
					isChecked ? F70CarSettingCommand.OPEN
							: F70CarSettingCommand.CLOSE);
			break;

		case R.id.switch_receive_carinfo:// 接收车机位置信息

			break;

		default:
			break;
		}
	}
	
	
	
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			handleRemoteFuntionChanged((RemoteFunction) msg.obj);
		}

	};
	
	/**
	 * 处理车辆配置信息以及UI显示
	 */
	private void handleRemoteFuntionChanged(RemoteFunction remoteFunction) {
		LogUtils.d(
				"switch_position_tocar: " + remoteFunction.getStatus7());
		updateSwitchFaultRemind(remoteFunction.getStatus2());
	}
	
	/**
	 * 更新位置发送到车机
	 */
	private void updateSwitchFaultRemind(int status) {
		positionTocarSwitch
		.setChecked(status == F70CarSettingCommand.OPEN ? true : false);
	}
	
	IRemoteFunctionListener iListener = new IRemoteFunctionListener.Stub() {
		@Override
		public void onReceived(RemoteFunction remoteFunction)
				throws RemoteException {
			LogUtils.d(
					"switch_position_tocar: " + remoteFunction.getStatus7());
			
			Message configMessage = Message.obtain();
			configMessage.obj = remoteFunction;
			handler.sendMessage(configMessage);
		}
	};
}
