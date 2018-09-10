package com.hwatong.f70.carsetting;

import com.hwatong.bt.BtDevice;
import com.hwatong.f70.baseview.BaseFragment;
import com.hwatong.f70.bluetooth.BluetoothDeleteDialog;
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
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

/**
 * 
 * @author ljw
 * 
 */
public class KeepFitTips extends BaseFragment implements OnCheckedChangeListener {

	private SwitchButton keepFitSwitch;
	private ICanbusService iCanbusService;// 远程调用接口
	private TextView distance, unit, distance2, unit2;
	private Button resetBtn;
	private static final int DIFF = -32768;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.f70_keepfitsetting,
				container, false);

		initWidget(rootView);
		initService();
		return rootView;
	}

	@Override
	public void onResume() {
		initKeepFitStatus();
		super.onResume();
		
		changedActivityImage(this.getClass().getName());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			iCanbusService.removeCarConfigListener(mICarConfigListener);
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
			iCanbusService.addCarConfigListener(mICarConfigListener);
		} catch (RemoteException e) {
			LogUtils.d(e.toString());
			e.printStackTrace();
		}
	}
	

	private void initKeepFitStatus() {
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
		keepFitSwitch = (SwitchButton) rootView
				.findViewById(R.id.switch_keepfit);
		distance = (TextView) rootView.findViewById(R.id.next_keep_instance_value);
		distance2 = (TextView) rootView.findViewById(R.id.next_keep_instance2_value);
		unit = (TextView) rootView.findViewById(R.id.keepfit_unit);
		unit2 = (TextView) rootView.findViewById(R.id.keepfit_unit2);
		
//		keepFitSwitch.setNoNeedAutoFeedback(true);
//		keepFitSwitch.setOnCheckedChangeListener(this);
		
		resetBtn = (Button)rootView.findViewById(R.id.resetdistance);
		resetBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showDeleteBluetoothDialog();
			}
		});
	}
	
//	@Override
//	public void onUserCheckedChanged(CompoundButton buttonView,
//			boolean isChecked) {
//
//	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		Message.obtain(handler, MSG_KEEPFIT, 0, 0, isChecked).sendToTarget();
		setTextEnabled(isChecked);
	}

	private static final int MSG_KEEPFIT = 0x08;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(msg.what == MSG_KEEPFIT) {
				F70CanbusUtils.getInstance().writeCarConfig(
						iCanbusService,
						F70CarSettingCommand.TYPE_KEEPFIT,
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
		LogUtils.d("KeepFitSwitchStatus: " + carConfig.getStatus17() + ", KeepFitDistance: " + carConfig.getStatus20() + ", NextCheck: " + carConfig.getNextCheck());
		
		updateNextDistanceStatus(carConfig.getStatus20());
		
		updateKeepFitDistance2Status(carConfig.getNextCheck());
		
		keepFitSwitch.setOnCheckedChangeListener(null);
		
//		updateKeepFitSwitchStstaus(carConfig.getStatus17());
//		keepFitSwitch.setOnCheckedChangeListener(this);
		
	}

	private void updateNextDistanceStatus(int status) {
		String nextDistance = String.valueOf(status + DIFF);
		distance.setText(nextDistance);
	}
	
	private void updateKeepFitSwitchStstaus(int status) {
		keepFitSwitch.setChecked(status == F70CarSettingCommand.OPEN ? true : false);
		setTextEnabled(status == F70CarSettingCommand.OPEN);
	}
	
	private void updateKeepFitDistance2Status(int status) {
		String nextCheckDistance = String.valueOf(status + DIFF);
		distance2.setText(nextCheckDistance);
	}
	
	private void setTextEnabled(boolean isEnabled) {
		distance.setEnabled(isEnabled);
		distance2.setEnabled(isEnabled);
		
		unit.setEnabled(isEnabled);
		unit2.setEnabled(isEnabled);
		
		resetBtn.setEnabled(isEnabled);
	}
	
	private void showDeleteBluetoothDialog() {
		final KeepFitResetDialog dialog = new KeepFitResetDialog(
				getActivity());
		Window win = dialog.getWindow();
		LayoutParams params = new LayoutParams();
		params.width = LayoutParams.WRAP_CONTENT;
		params.height = LayoutParams.WRAP_CONTENT;
		params.x = -210;// 设置x坐标
		win.setAttributes(params);
		dialog.setOnPositiveListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				F70CanbusUtils.getInstance().writeCarConfig(
						iCanbusService,
						F70CarSettingCommand.TYPE_KEEPFIT_RESET,F70CarSettingCommand.OPEN);
				dialog.dismiss();
			}
		});
		dialog.setOnNegativeListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});

		dialog.show();
	}

	ICarConfigListener mICarConfigListener = new ICarConfigListener.Stub() {

		@Override
		public void onReceived(CarConfig carConfig) throws RemoteException {
			Message configMessage = Message.obtain();
			configMessage.obj = carConfig;
			handler.sendMessage(configMessage);
		}
	};

}
