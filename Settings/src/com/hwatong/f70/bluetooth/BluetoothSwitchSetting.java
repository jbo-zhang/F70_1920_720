package com.hwatong.f70.bluetooth;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.hwatong.bt.BtDevice;
import com.hwatong.f70.baseview.BaseFragment;
import com.hwatong.f70.bluetooth.BluetoothContract.Presenter;
import com.hwatong.f70.bluetooth.SearchDevice.OnBluetoothSearchFinish;
import com.hwatong.f70.main.LogUtils;
import com.hwatong.f70.main.Utils;
import com.hwatong.settings.R;
import com.hwatong.settings.widget.SwitchButton;

/**
 * 
 * @author ljw
 * 
 */
public class BluetoothSwitchSetting extends BaseFragment implements
		OnCheckedChangeListener, BluetoothContract.View {

	private SwitchButton mBluetoothSwitch, autoConnectSwitch, autoAnswerSwitch;
	private EditText bluetoothName;
	private boolean fromTouch = true;

	private BluetoothContract.Presenter mPresenter;
	private InputMethodManager manager;
	
	private static BtDevice globleDeviceConnected;

//	@Override
//	public void onAttach(Activity activity) {
//		super.onAttach(activity);
//		
//	}

//	@Override
//	public void onDetach() {
//		super.onDetach();
//
//	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.f70_bluetooth_switch_all,
				container, false);
		initWidget(rootView);
		if(mPresenter != null)
			mPresenter.bindService();
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		
		if(mPresenter != null) {
			mPresenter.isBluetoothOpen();
			mPresenter.isAutoConnectedOpen();
			mPresenter.isAutoAnswerOpen();
			mPresenter.getBluetoothName();
		}
		
		changedActivityImage(this.getClass().getName());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(mPresenter != null) {
			mPresenter.unregisterBtCallback();
			mPresenter.unbindService();
		}
		if(manager.isActive(bluetoothName)) {
			manager.hideSoftInputFromWindow(
					bluetoothName.getWindowToken(), 0);
		}
	}

	private void initWidget(View rootView) {
		mBluetoothSwitch = (SwitchButton) rootView
				.findViewById(R.id.switch_bluetooth);
		autoConnectSwitch = (SwitchButton) rootView
				.findViewById(R.id.bluetooth_autoconnection);
		autoAnswerSwitch = (SwitchButton) rootView
				.findViewById(R.id.bluetooth_autoanswer);

		mBluetoothSwitch.setOnCheckedChangeListener(this);
		autoConnectSwitch.setOnCheckedChangeListener(this);
		autoAnswerSwitch.setOnCheckedChangeListener(this);

		bluetoothName = (EditText) rootView.findViewById(R.id.bluetoothname);
		bluetoothName.setOnKeyListener(onKeyListener);
//		bluetoothName.setCursorVisible(false);
		bluetoothName.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				bluetoothName.setCursorVisible(true);
			}
		});
		manager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	}

	
	private void autoAjustBluetoothEdittext(boolean isOpen) {
		if (isOpen) {
			enableBluetoothNameEdittext();
		} else {
			unEnableBluetoothNameEdittext();
		}
	}

	private void enableBluetoothNameEdittext() {
		bluetoothName.setEnabled(true);
//		bluetoothName.setFocusable(true);
//		bluetoothName.requestFocus(); 
//		bluetoothName.setFocusableInTouchMode(true);
		bluetoothName.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				bluetoothName.setCursorVisible(true);
			}
		});
	}

	private void unEnableBluetoothNameEdittext() {
		bluetoothName.setEnabled(false);
//		bluetoothName.clearFocus();
//		bluetoothName.setFocusable(false);
//		bluetoothName.setFocusableInTouchMode(false);
	}

	@Override
	public void onCheckedChanged(CompoundButton view, boolean isChecked) {
		if (!fromTouch)
			return;
		int resId = view.getId();
		int type = -1;
		switch (resId) {
		case R.id.switch_bluetooth:
			type = MSG_BT;
			//开启蓝牙开关后防止狂按引起的状态错乱，每次改变状态后都变为不可点击，等待完全开启或完全关闭后方可再次点击
			if(!isChecked)
				mBluetoothSwitch.setClickable(false);
			break;
		case R.id.bluetooth_autoconnection:
			type = MSG_AUTO_CONNECTED;
			break;
		case R.id.bluetooth_autoanswer:
			type = MSG_AUTO_ANSWER;
			break;
		}
		Message.obtain(handler, type, 0, 0, isChecked).sendToTarget();
	}

	
	private View.OnKeyListener onKeyListener = new View.OnKeyListener() {

		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_ENTER
					&& event.getAction() == KeyEvent.ACTION_DOWN) {
//				InputMethodManager inputMethodManager = (InputMethodManager) getActivity()
//						.getSystemService(Context.INPUT_METHOD_SERVICE);
				if (manager.isActive()) {
					manager.hideSoftInputFromWindow(
							v.getApplicationWindowToken(), 0);
				}
				LogUtils.d("KeyEvent.KEYCODE_ENTER || KeyEvent.ACTION_DOWN");
				String name = bluetoothName.getText().toString().trim();
				
				if(!TextUtils.isEmpty(name) && !getSharedPreferenceBluetoothName().equals(name)) {
					mPresenter.setBtName(name);
				}
				
				if (TextUtils.isEmpty(name))
					bluetoothName.setText("F70");
				setSharedPreferenceBluetoothName(bluetoothName.getText().toString().trim());
				
				bluetoothName.setCursorVisible(false);
				return true;
			}
			return false;
		}
	};
	
	private static final int MSG_BT = 0X08;
	private static final int MSG_AUTO_CONNECTED = 0X09;
	private static final int MSG_AUTO_ANSWER = 0X0a;
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_BT:
				mPresenter.enabledBluetooth((Boolean) msg.obj);
				break;
			case MSG_AUTO_CONNECTED:
				mPresenter.enabledAutoConnect((Boolean) msg.obj);
				break;
			case MSG_AUTO_ANSWER:
				mPresenter.enabledAutoAnswer((Boolean) msg.obj);
				break;

			default:
				break;
			}
		}
		
	};

	@Override
	public void setPresenter(Presenter presenter) {
		mPresenter = presenter;
	}

	@Override
	public void showIsBluetoothOpen(boolean isOpen) {
		fromTouch = false;
		mBluetoothSwitch.setChecked(isOpen);
		fromTouch = true;
		
		autoConnectSwitch.setEnabled(isOpen);
		autoAnswerSwitch.setEnabled(isOpen);
		
		
		if(getActivity() != null) {
			Intent intent = new Intent("com.hwatong.f70.bluetoothstatus");
			intent.putExtra("status", isOpen ? 1 : 0);
			getActivity().sendBroadcast(intent);
		}
		autoAjustBluetoothEdittext(isOpen);
	}

	@Override
	public void showIsAutoconnectedOpen(boolean isOpen) {
		fromTouch = false;
		autoConnectSwitch.setChecked(isOpen);
		fromTouch = true;
	}

	@Override
	public void showGetBluetoothName(String name) {
		name = TextUtils.isEmpty(name) ? getSharedPreferenceBluetoothName()
				: name;
		bluetoothName.setText(name);
	}

	@Override
	public void showIsAutoAnswerOpen(boolean isOpen) {
		fromTouch = false;
		autoAnswerSwitch.setChecked(isOpen);
		fromTouch = true;
	}

	@Override
	public void showDiscoveryDone() {

	}

	@Override
	public void showUpdateDiscoveryDevice(BtDevice device) {

	}

	@Override
	public void showDisconnected() {

	}

	@Override
	public void showConnected() {
		// BluetoothCommonView.bluetoothConnectedInfoToast(getActivity(),
		// BluetoothCommonView.CONNECTED_TOAST_LAYOUTID).show();
//		Utils.startApp(getActivity(), "com.hwatong.btphone.MAIN");
//		if (getActivity() != null)
//			getActivity().finish();
	}

	@Override
	public void showUpdatePairedDevice(BtDevice device) {

	}

	@Override
	public void showUpdateConnectedDeviceChanged(BtDevice device) {

	}

	@Override
	public void showConnectingTimeout() {

	}

	@Override
	public void showBluetoothStatusChanged(BluetoothStatus status) {
//		LogUtils.d("showBluetoothStatusChanged: " + status);
		if(status == BluetoothStatus.TURN_ON || status == BluetoothStatus.ON) {
			fromTouch = false;
			mBluetoothSwitch.setChecked(true);
			fromTouch = true;
		} else {}
		boolean isEnabled = !(status == BluetoothStatus.TRUN_OFF || status == BluetoothStatus.TURN_ON);
		LogUtils.d("isEnabled:" + isEnabled);
//		mBluetoothSwitch.setEnabled(isEnabled);
		mBluetoothSwitch.setClickable(isEnabled);
		
		
		autoAnswerSwitch.setEnabled(status == BluetoothStatus.ON);
		autoConnectSwitch.setEnabled(status == BluetoothStatus.ON);
		
		
		if(getActivity() != null) {
			Intent intent = new Intent("com.hwatong.f70.bluetoothstatus");
			intent.putExtra("status", status == BluetoothStatus.ON ? 1 : 0);
			getActivity().sendBroadcast(intent);
		}

		autoAjustBluetoothEdittext(status == BluetoothStatus.ON);
	}

	private String getSharedPreferenceBluetoothName() {
		if (getActivity() == null)
			return "F70";
		SharedPreferences pref = getActivity().getSharedPreferences(
				"local_bluetooth", Context.MODE_PRIVATE);
		return pref.getString("name", "F70");
	}

	private void setSharedPreferenceBluetoothName(String name) {
		if (getActivity() == null)
			return;
		name = TextUtils.isEmpty(name) ? "F70" : name;
		SharedPreferences pref = getActivity().getSharedPreferences(
				"local_bluetooth", Context.MODE_PRIVATE);
		pref.edit().putString("name", name).apply();
	}
	
}
