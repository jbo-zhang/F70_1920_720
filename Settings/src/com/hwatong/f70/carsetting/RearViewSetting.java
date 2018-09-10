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
import android.content.IntentSender.SendIntentException;
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

/**
 * 
 * @author ljw
 *
 */
public class RearViewSetting extends BaseFragment implements
		OnCheckedChangeListener {

	private SwitchButton autoFolding;

	private ICanbusService iCanbusService;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.f70_rearviewsetting,
				container, false);
		initWidget(rootView);
		initService();
		return rootView;
	}

	@Override
	public void onResume() {
		initRearView();
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

	
	private void initRearView() {
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
		autoFolding = (SwitchButton) rootView
				.findViewById(R.id.switch_rearview_autofold);
		// autoFolding.setNoNeedAutoFeedback(true);
		autoFolding.setOnCheckedChangeListener(this);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		Message.obtain(handler,MSG_REARVIEW_AUTOFOLD, 0, 0, isChecked).sendToTarget();
		LogUtils.d("change rearview status");
	}

	private static final int MSG_REARVIEW_AUTOFOLD = 0x10;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(msg.what == MSG_REARVIEW_AUTOFOLD) {
				F70CanbusUtils.getInstance().writeCarConfig(
						iCanbusService,
						F70CarSettingCommand.TYPE_REARVIEW_AUTOFOLD,
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
		LogUtils.d("RearViewStatus:" + carConfig.getStatus2());
		
		autoFolding.setOnCheckedChangeListener(null);
		updateRearViewStatus(carConfig.getStatus2());
		autoFolding.setOnCheckedChangeListener(this);
	}

	/**
	 *
	 */
	private void updateRearViewStatus(int status) {
		autoFolding.setChecked(status == F70CarSettingCommand.OPEN ? true
				: false);
	}

	ICarConfigListener iCarConfigListener = new ICarConfigListener.Stub() {

		@Override
		public void onReceived(CarConfig carConfig) throws RemoteException {
			LogUtils.d("updateRearViewStatus:" + carConfig.getStatus2());
			Message configMessage = Message.obtain();
			configMessage.what = 0x09;
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
