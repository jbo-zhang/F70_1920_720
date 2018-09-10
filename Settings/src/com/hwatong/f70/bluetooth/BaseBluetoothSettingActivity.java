package com.hwatong.f70.bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.hwatong.bt.BtDevice;
import com.hwatong.f70.baseview.BaseFragment.OnFragmentImageChangedListener;
import com.hwatong.f70.baseview.BaseFragment.OnInputMethodHideListener;
import com.hwatong.f70.bluetooth.BluetoothContract.Presenter;
import com.hwatong.f70.bluetooth.SearchDevice.OnBluetoothSearchFinish;
import com.hwatong.f70.main.F70Application;
import com.hwatong.f70.main.LogUtils;
import com.hwatong.f70.main.Utils;
import com.hwatong.settings.R;

public class BaseBluetoothSettingActivity extends Activity implements
		OnClickListener, OnCheckedChangeListener, android.widget.CompoundButton.OnCheckedChangeListener, BluetoothContract.View
		, OnFragmentImageChangedListener{

	private BluetoothSwitchSetting bluetoothSetting;
	private DeviceList deviceList;
	private SearchDevice searchDevice;
	private boolean fromUser = true;

	private final String BLUETOOTHSETTINGFLAG = "bluetoothSetting";
	private final String DEVICELISTFLAG = "displayssetting";
	private final String SEARCHDEVICEFLAG = "languagesetting";
	private String currentFragment = "";
	private boolean isVoiceSearch = false;

	private LinearLayout bluetoothSettingHeaders;
	private RadioButton deviceListRb, searchListRb;
	private RelativeLayout backBt;
	private ImageView bigImage;
	private FragmentManager fragmentManager;
	private InputMethodManager inputMethodManager;
	int curPress = -1;

	private NForeBluetoothPresenter mBluetoothPresenter;
	private NForeBluetoothPresenter activityPresenter;
	
	private BluetoothContract.Presenter mPresenter;
	
	private boolean isDeviceConnected;
	private boolean isBtOpen;
	Toast toast = null;


	private final int BLUETOOTHSETTING_FRAGMENT = R.id.bluetooth_fragment;
	
	@Override
	protected void onCreate(Bundle saveInstance) {
		super.onCreate(saveInstance);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		setContentView(R.layout.f70_bluetoothsetting);

		initFragment();

		initWidget();
		
		initReceiver();
		
		receiveIntentIfExit();
		activityPresenter = new NForeBluetoothPresenter(getApplicationContext(), this);
		LogUtils.d("BtActivity: onCreate");
//		bindService(new Intent("com.hwatong.bt.service"),
//				btServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
//		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		F70Application.getInstance().sendCurrentPageName("setting_ble");
		mPresenter.getConnectedDevice();
		mPresenter.isBluetoothOpen();
	}

	@Override
	public void onDestroy() {
		LogUtils.d("onDestroy");
		super.onDestroy();
		mBluetoothPresenter = null;
		unregisterReceiver(receiver);
		mPresenter.unbindService();
		mPresenter = null;
	}
	
	@Override
	public void onFragmentChanged(String fragmentName) {
		if(fragmentName.equals(BluetoothSwitchSetting.class.getName())) {
			bigImage.setImageResource(R.drawable.bluetoothsetting_setting);
		} else if(fragmentName.equals(DeviceList.class.getName())) {
			bigImage.setImageResource(R.drawable.bluetoothsetting_devicelist);
		} else if(fragmentName.equals(SearchDevice.class.getName())) {
			bigImage.setImageResource(R.drawable.bluetoothsetting_searchlist);
		}
	}
	
	/********************************************** init widget and listener ******************************************/

	private void initWidget() {
		bluetoothSettingHeaders = (LinearLayout) findViewById(R.id.bluetooth_navi);
//		bluetoothSettingHeaders.setOnCheckedChangeListener(this);

		
		for (int i = 0; i < bluetoothSettingHeaders.getChildCount(); i++) {
			if(bluetoothSettingHeaders
					.getChildAt(i) instanceof RadioButton) {
				final RadioButton menu = (RadioButton) bluetoothSettingHeaders
						.getChildAt(i);
				menu.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							curPress = menu.getId();

							for (int j = 0; j < bluetoothSettingHeaders
									.getChildCount(); j++) {
								if (curPress != bluetoothSettingHeaders.getChildAt(j)
										.getId()) {
									bluetoothSettingHeaders.getChildAt(j).setEnabled(
											false);
								}
							}
							break;
						case MotionEvent.ACTION_UP:
							for (int j = 0; j < bluetoothSettingHeaders
									.getChildCount(); j++) {
								if (curPress != bluetoothSettingHeaders.getChildAt(j)
										.getId()) {
									bluetoothSettingHeaders.getChildAt(j).setEnabled(
											true);
								}
							}

							curPress = -1;
							break;
						}
						return false;
					}
				});
			}
		}
		 deviceListRb = (RadioButton) findViewById(R.id.devicelist_rb);
		 searchListRb = (RadioButton) findViewById(R.id.searchdevice_rb);
//		 deviceListRb.setOnCheckedChangeListener(this);
//		 searchListRb.setOnCheckedChangeListener(this);
		 deviceListRb.setOnClickListener(this);
		 searchListRb.setOnClickListener(this);
		 
		 bigImage = (ImageView) findViewById(R.id.bigimage);
		backBt = (RelativeLayout) findViewById(R.id.setting_main_back);
		backBt.setOnClickListener(this);
		
		inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	}
	
	private void initReceiver() {
		IntentFilter intentFilter = new IntentFilter("com.hwatong.voice.SEARCH_BT");
		intentFilter.addAction("com.hwatong.f70.bluetoothstatus");
		registerReceiver(receiver, intentFilter);
	}	
	
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@SuppressLint("ResourceAsColor")
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if(action.equals("com.hwatong.voice.SEARCH_BT")) {
				searchListRb.performClick();
			}
			
			
			else if(action.equals("com.hwatong.f70.bluetoothstatus")) {
				int status = intent.getIntExtra("status", 2);
				deviceListRb.setEnabled(status == 1);
				searchListRb.setEnabled(status == 1);
				
				if(status == 1) {
					deviceListRb.setChecked(false);
					searchListRb.setChecked(false);
				}
				
				deviceListRb.setBackgroundResource(status == 1 ? R.drawable.bluetooth_devicelist_selector : R.drawable.f70_devicelist_disenabled);
				searchListRb.setBackgroundResource(status == 1 ? R.drawable.bluetooth_searchlist_selector : R.drawable.f70_searchlist_disenabled);
				
				deviceListRb.setTextColor(status == 1 ? getResources().getColor(R.color.white) : getResources().getColor(R.color.gray));
				searchListRb.setTextColor(status == 1 ? getResources().getColor(R.color.white) : getResources().getColor(R.color.gray));
			}
		}
		
	};
	
	
	
	private void receiveIntentIfExit() {
		Intent intent = getIntent();
		if(intent.hasExtra("start_type")) {
			LogUtils.d("go to search");
			isVoiceSearch = true;
//			searchListRb.performClick();
		}
		else {
			LogUtils.d("go to settings");			
			// show bluetooth switch at first
			startFragment(bluetoothSetting, BLUETOOTHSETTINGFLAG);
		}
	}
	
	private void initFragment() {
		fragmentManager = this.getFragmentManager();
		bluetoothSetting = new BluetoothSwitchSetting();
		deviceList = new DeviceList();
		searchDevice = new SearchDevice();
		
		if(Utils.isLowDesityMachine(BaseBluetoothSettingActivity.this)) {			
			bluetoothSetting.setOnFragmentImageChangedListener(this);
			deviceList.setOnFragmentImageChangedListener(this);
			searchDevice.setOnFragmentImageChangedListener(this);
		}
	}

	private void startFragment(Fragment f, String fragmentFlag) {
		if (!f.isAdded()
				&& fragmentManager.findFragmentByTag(fragmentFlag) == null) {
			if (fragmentFlag.equals(BLUETOOTHSETTINGFLAG))
				mBluetoothPresenter = new NForeBluetoothPresenter(
						getApplicationContext(), bluetoothSetting);
			else if (fragmentFlag.equals(DEVICELISTFLAG))
				mBluetoothPresenter = new NForeBluetoothPresenter(
						getApplicationContext(), deviceList);
			else
				mBluetoothPresenter = new NForeBluetoothPresenter(
						getApplicationContext(), searchDevice);

			FragmentTransaction fragmentTransaction = fragmentManager
					.beginTransaction();
			// if (fragmentManager.findFragmentByTag(fragmentFlag) != null) {
			// fragmentTransaction.show(f);
			// } else {
			fragmentTransaction.replace(BLUETOOTHSETTING_FRAGMENT, f,
					fragmentFlag);

			// }
			fragmentTransaction.commitAllowingStateLoss();
			currentFragment = fragmentFlag;
		}
	}

	private void hideFragments() {
		Fragment fragment = fragmentManager.findFragmentByTag(currentFragment);
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		if (fragment != null)
			fragmentTransaction.hide(fragment);
		fragmentTransaction.commit();
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if (!fromUser)
			return;
		hideFragments();
		switch (checkedId) {
		case R.id.devicelist_rb:
			startFragment(deviceList, DEVICELISTFLAG);
			break;
		case R.id.searchdevice_rb:
			startFragment(searchDevice, SEARCHDEVICEFLAG);
			break;
		default:
			break;
		}
	}
	
	
	private void setOnFinishListener() {
		if (searchDevice != null) {
			searchDevice
					.setOnBluetoothSearchFinish(new OnBluetoothSearchFinish() {

						@Override
						public void onSearchFinish() {
							LogUtils.d("onSearchFinish");
							searchListRb.setChecked(false);
						}
					});
		}
	}
	
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		int resId = buttonView.getId();
		switch (resId) {
		case R.id.devicelist_rb:
//			if(deviceListRb.isChecked())
//				return;
			searchListRb.setChecked(false);
//			startFragment(deviceList, DEVICELISTFLAG);
			LogUtils.d("searchdevice state: " + searchListRb.isChecked() + "devicelist state: " + deviceListRb.isChecked());
			break;
		case R.id.searchdevice_rb:
//			if(searchListRb.isChecked())
//				return;
			deviceListRb.setChecked(false);
//			startFragment(searchDevice, SEARCHDEVICEFLAG);
			LogUtils.d("devicelist state: " + deviceListRb.isChecked() + "searchdevice state: " + searchListRb.isChecked());
		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		int resId = v.getId();
		switch (resId) {
		
		case R.id.setting_main_back: {
			LogUtils.d("deviceList.isHidden():" + deviceList.isVisible()
					+ ",searchDevice.isHidden():" + searchDevice.isVisible());
			searchListRb.setChecked(false);
			deviceListRb.setChecked(false);
			if (deviceList.isVisible() || searchDevice.isVisible()) {
				fromUser = false;
				hideFragments();
				startFragment(bluetoothSetting, BLUETOOTHSETTINGFLAG);
//				bluetoothSettingHeaders.clearCheck();
				fromUser = true;
			} else if (!deviceList.isVisible() || !searchDevice.isVisible()) {
				onBackPressed();
			}
		}
			break;
		
		case R.id.devicelist_rb:
			searchListRb.setChecked(false);
			searchListRb.setEnabled(false);			
			handler.sendEmptyMessageDelayed(LIMIT_SEARCH_BUTTON, LIMIT_BUTTON_DALAY_TIME);
			if(!deviceList.isVisible())
				startFragment(deviceList, DEVICELISTFLAG);
			break;
		
		case R.id.searchdevice_rb:
			
			if (isDeviceConnected) {
				LogUtils.d("BtActivity: bluetoothConnectedInfoToast");
				if(toast == null)
					toast = BluetoothCommonView.bluetoothConnectedInfoToast(this,
						BluetoothCommonView.CONNECTED_TOAST_LAYOUTID);
				toast.show();
				searchListRb.setChecked(false);
				break;
			}
			LogUtils.d("searchdevice_rb click");
			//若当前无设备连接且蓝牙开启，则先让提示框消失，并且开始搜索，若当前不在搜索界面，则进入搜索界面
			deviceListRb.setChecked(false);
			deviceListRb.setEnabled(false);
			handler.sendEmptyMessageDelayed(LIMIT_PARIED_BUTTON, LIMIT_BUTTON_DALAY_TIME);
			if(isBtOpen) {
				if (searchDevice.isVisible()) {
					if(toast != null)
						toast.cancel();
					goOnStartSearch();
				} else {
					if(!isDeviceConnected) {						
						handler.sendEmptyMessageDelayed(START_DISCOVERY, START_DISCOVERY_DELAY);
						LogUtils.d("startFragment searchDevice");
					}
				}
			}

			break;
			
		default:
			break;
		}
	}
	private static final int START_DISCOVERY_DELAY = 150;
	private static final int START_DISCOVERY = 1;
	private static final int LIMIT_PARIED_BUTTON = 2;
	private static final int LIMIT_SEARCH_BUTTON = 3;
	private static final int LIMIT_BUTTON_DALAY_TIME = 500;
	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case START_DISCOVERY:
				if(isBtOpen) {
					LogUtils.d("delay discovery receive: repeat check ok");
					setOnFinishListener();
					startFragment(searchDevice, SEARCHDEVICEFLAG);
				}
				break;
				
			case LIMIT_PARIED_BUTTON:
				deviceListRb.setEnabled(true);
				break;
				
			case LIMIT_SEARCH_BUTTON:
				searchListRb.setEnabled(true);
				break;

			default:
				break;
			}
		}
		
	};
	
	private void goOnStartSearch() {
		Intent intent = new Intent("com.hwatong.gotosearch.uha");
//		sendBroadcastAsUser(intent, UserHandle.ALL);
		sendBroadcast(intent);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	public void setPresenter(Presenter presenter) {
		LogUtils.d("BtActivity: setPresenter");
		mPresenter = presenter;
		mPresenter.bindService();
	}

	@Override
	public void showIsBluetoothOpen(boolean isOpen) {
		LogUtils.d("BtActivity: showIsBluetoothOpen: " + isOpen);
		isBtOpen = isOpen;
		
		
		if(isOpen) {
			mPresenter.getConnectedDevice();
			if(isVoiceSearch)
				searchListRb.performClick();
		}
	}

	@Override
	public void showIsAutoconnectedOpen(boolean isOpen) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showGetBluetoothName(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showIsAutoAnswerOpen(boolean isOpen) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showDiscoveryDone() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showUpdateDiscoveryDevice(BtDevice device) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showDisconnected() {
		LogUtils.d("BtActivity: showDisconnected: ");
		
		isDeviceConnected = false;		
	}

	@Override
	public void showConnected() {
		LogUtils.d("BtActivity: showConnected: ");
		
		isDeviceConnected = true;
	}

	@Override
	public void showUpdatePairedDevice(BtDevice device) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showUpdateConnectedDeviceChanged(BtDevice device) {
		LogUtils.d("BtActivity: showUpdateConnectedDeviceChanged: " + device);
		
		isDeviceConnected = (device != null);
	}

	@Override
	public void showConnectingTimeout() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showBluetoothStatusChanged(BluetoothStatus status) {
		LogUtils.d("BtActivity: showBluetoothStatusChanged: " + status);
		
		isBtOpen = (status == BluetoothStatus.ON);		
	}
	
}
