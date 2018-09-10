/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hwatong.settings;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import com.hwatong.settings.R;

public class BluetoothSettings extends SettingsPreferenceFragment implements OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener{
	private static final String TAG = "BluetoothSettings";
/*
	private IGocsdkService mService;
	private SdkCallback mSdkCallback = new SdkCallback();
	private class SdkCallback extends IGocsdkCallback.Stub {
		@Override public void onHfpDisconnected() throws RemoteException { onDisconnected(); }
		@Override public void onHfpConnected() throws RemoteException { onConnected(); }
		@Override public void onCallSucceed(String str) throws RemoteException {}
		@Override public void onIncoming(String number) throws RemoteException {}
		@Override public void onHangUp() throws RemoteException {}
		@Override public void onTalking(String str) throws RemoteException {}
		@Override public void onRingStart() throws RemoteException {}
		@Override public void onRingStop() throws RemoteException {}
		@Override public void onHfpLocal() throws RemoteException {}
		@Override public void onHfpRemote() throws RemoteException {}
		@Override public void onInPairMode() throws RemoteException {}
		@Override public void onExitPairMode() throws RemoteException {}
		@Override public void onInitSucceed() throws RemoteException {}
		@Override public void onMusicPlaying() throws RemoteException {}
		@Override public void onMusicStopped() throws RemoteException {}
		@Override public void onAutoConnectAccept(boolean autoConnect, boolean autoAccept) throws RemoteException {}
		@Override public void onCurrentAddr(String addr) throws RemoteException { }
		@Override public void onCurrentName(String name) throws RemoteException { onGetConnectedDeviceName(name); }
		@Override public void onHfpStatus(int status) throws RemoteException {}
		@Override public void onAvStatus(int status) throws RemoteException {}
		@Override public void onVersionDate(String version) throws RemoteException {}
		@Override public void onCurrentDeviceName(String name) throws RemoteException { onGetDeviceName(name);}
		@Override public void onCurrentPinCode(String code) throws RemoteException { onGetPinCode(code);}
		@Override public void onA2dpConnected() throws RemoteException { }
		@Override public void onCurrentAndPairList(int index, String name, String addr) throws RemoteException {}
		@Override public void onA2dpDisconnected() throws RemoteException {}
		@Override public void onPhoneBook(String name, String number) throws RemoteException { }
		@Override public void onSimBook(String name, String number) throws RemoteException { }
		@Override public void onPhoneBookDone() throws RemoteException { }
		@Override public void onSimDone() throws RemoteException {}
		@Override public void onCalllogDone() throws RemoteException {}
		@Override public void onCalllog(int type, String name, String number, String date) throws RemoteException {}
		@Override public void onDiscovery(String name, String addr) throws RemoteException {onDeviceDiscovery(name,addr);} 
		@Override public void onDiscoveryDone() throws RemoteException {onDeviceDiscoveryDone();}
		@Override public void onLocalAddress(String addr) throws RemoteException {}
		@Override public void onOutGoingOrTalkingNumber(String number) throws RemoteException {}
		@Override public void onConnecting() throws RemoteException { }
		@Override public void onSppData(int index, String data) throws RemoteException {}
		@Override public void onSppConnect(int index) throws RemoteException {}
		@Override public void onSppDisconnect(int index) throws RemoteException {}
		@Override public void onSppStatus(int status) throws RemoteException {}
		@Override public void onOppReceivedFile(String path) throws RemoteException {}
		@Override public void onOppPushSuccess() throws RemoteException {}
		@Override public void onOppPushFailed() throws RemoteException {}
		@Override public void onHidConnected() throws RemoteException {}
		@Override public void onHidDisconnected() throws RemoteException {}
		@Override public void onHidStatus(int status) throws RemoteException {}
		@Override public void onMusicInfo(String name, String artist, int duration, int pos,int total) throws RemoteException {}
		@Override public void onPanConnect() throws RemoteException {}
		@Override public void onPanDisconnect() throws RemoteException {}
		@Override public void onPanStatus(int status) throws RemoteException {}
		@Override public void onVoiceConnected() throws RemoteException {}
		@Override public void onVoiceDisconnected() throws RemoteException {}
		@Override public void onProfileEnbled(boolean[] enabled) throws RemoteException {}
		@Override public void onPhoneMessage(String handle, boolean read, String time, String name, String num, String title) throws RemoteException {}
		@Override public void onPhoneMessageText(String text) throws RemoteException {}
	}

	private ServiceConnection sdkConnect = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder serv) {
			Log.d(TAG, "onServiceConnected:");
			mService = IGocsdkService.Stub.asInterface(serv);
			try {
				mService.registerCallback(mSdkCallback);
				mService.getLocalName();
				mService.getPinCode();
				refreshConnected();
				startSearch();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mService = null;
		}	
	};
	private void stopSearch() {
		if (mSearching) {
			if (mService != null) {
				try {
					mService.stopDiscovery();
					mSearching = false;
					refreshSearchState();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void startSearch() {
		Log.d(TAG, "startSearch:");
		if (!mSearching && mService != null) {
			try {
				mSearchDeviceList.clear();
				updateSearchList();

				mService.startDiscovery();
				mSearching = true;
				refreshSearchState();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	//////////////////////////////////////////////////////////////////////
	//
	private BtDevice mLastDevice;
	private AlertDialog mDisconnectDialog;
	private void showDisconnectDialog(String deviceName) {
		if (mDisconnectDialog == null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(deviceName);
			builder.setMessage(R.string.bluetooth_confirm_disconnect);
			builder.setPositiveButton(R.string.bluetooth_confirm, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (mService != null) {
						try {
							BtDevice device = mService.getConnectDevice();
							if (device != null) {
								mLastDevice = new BtDevice();
								mLastDevice.name = device.name;
								mLastDevice.addr = device.addr;
							}

							mService.disconnect();
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				}
			});
			builder.setNegativeButton(R.string.cancel, null);
			mDisconnectDialog = builder.create();

			WindowManager.LayoutParams params = mDisconnectDialog.getWindow().getAttributes();
			params.token = mListView.getWindowToken();
			params.type = WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL;
			mDisconnectDialog.getWindow().setAttributes(params);
		}
		mDisconnectDialog.show();
	}


	private AlertDialog mConnectDialog;
	private void showConnectDialog() {
		if (mConnectDialog == null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(getActivity().getString(R.string.bluetooth_confirm_connect)+mDeviceConfirmConnect.name+" ?");
			builder.setPositiveButton(R.string.bluetooth_confirm, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (mService != null) {
						try {
							stopSearch();
							mService.disconnect();
							mService.connectDevice(mDeviceConfirmConnect.addr);

							mDeviceConnecting = mDeviceConfirmConnect;
							//							mSearchAdapter.notifyDataSetChanged();
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				}
			});
			builder.setNegativeButton(R.string.cancel, null);
			mConnectDialog = builder.create();

			WindowManager.LayoutParams params = mConnectDialog.getWindow().getAttributes();
			params.token = mListView.getWindowToken();
			params.type = WindowManager.LayoutParams.TYPE_APPLICATION_SUB_PANEL;
			mConnectDialog.getWindow().setAttributes(params);
		}
		mConnectDialog.show();
	}

	private List<BtDevice> mSearchDeviceList = new ArrayList<BtDevice>();
	private List<BtDevice> mPairedDeviceList = new ArrayList<BtDevice>();
	private List<BtDevice> mConnectedDeviceList = new ArrayList<BtDevice>();

	private boolean mSearching;
	private BtDevice mDeviceConnecting;
	private BtDevice mDeviceConfirmConnect;

	private String deviceName, pinCode, connectedDeviceName;
	private final static int ID_SET_DEVICENAME=0x1101;
	private final static int ID_SET_PINCODE=0x1102;
	private final static int ID_SET_CONNECTED_DEVICENAME=0x1103;
	@SuppressLint("HandlerLeak") 
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case ID_SET_DEVICENAME:
				mDeviceNamePref.setText(deviceName);
				break;
			case ID_SET_PINCODE:
				mPinCodePref.setText(pinCode);
				break;
			case ID_SET_CONNECTED_DEVICENAME:
				Log.d(TAG, "ID_SET_CONNECTED_DEVICENAME connectedDeviceName="+connectedDeviceName);
				mConnectedPref.setText(connectedDeviceName);
				break;
			}
		}
	};
	private void onGetDeviceName(String deviceName) {
		Log.d(TAG, "onGetDeviceName: deviceName="+deviceName);
		this.deviceName=deviceName;
		mHandler.sendEmptyMessage(ID_SET_DEVICENAME);
	}
	private void onGetPinCode(String pincode) {
		Log.d(TAG, "onGetDeviceName: pincode="+pincode);
		this.pinCode=pincode;
		mHandler.sendEmptyMessage(ID_SET_PINCODE);
	}
	private void onGetConnectedDeviceName(String deviceName) {
		Log.d(TAG, "onGetConnectedDeviceName: deviceName="+deviceName);
		refreshConnected();
		updateSearchList();
	}
	private void onDisconnected() {
		Log.d(TAG, "onDisconnected:");
		mConnectedDeviceList.clear();
		if (mLastDevice != null) {
			mSearchDeviceList.add(0, mLastDevice);
			mLastDevice = null;
			updateSearchList();
		}
		refreshConnected();
	}
	private void onConnected() {
		Log.d(TAG, "onConnected:");
		if (mDeviceConnecting != null) {
			mDeviceConnecting = null;
			updateSearchList();
		}
		refreshConnected();
	}
	private void onDeviceDiscovery(String deviceName, String deviceAddr) {
		Log.d(TAG, "onDeviceDiscovery: deviceName="+deviceName + ", deviceAddr="+deviceAddr);
		try {
			BtDevice d = new BtDevice(deviceName, deviceAddr, BtDevice.STATE_DISCONNECTED);
			BtDevice connect = mService.getConnectDevice();
			if(connect != null && connect.equals(d)) {
				return;
			}
			mSearchDeviceList.add(d);
			addPreference(d);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	private void onDeviceDiscoveryDone() {
		Log.d(TAG, "onDeviceDiscoveryDone");
		mSearching = false;
		refreshSearchState();
	}
	private void refreshConnected(){
		Log.d(TAG, "refreshConnected:");
		try {
			BtDevice device = mService.getConnectDevice();
			String deviceName="";
			if (device != null) {
				deviceName=device.name;
				if (device.name.isEmpty()) {
					deviceName=getActivity().getResources().getString(R.string.bluetooth_unknown_device);
				}
			}
			Log.d(TAG, "refreshConnected:deviceName="+deviceName);
			if (!deviceName.equals(connectedDeviceName)){
				this.connectedDeviceName=deviceName;
				mHandler.sendEmptyMessage(ID_SET_CONNECTED_DEVICENAME);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	private void refreshSearchState() {
		if (mSearching) {
			//			mBtnSearch.setText(R.string.cancel_search);
			//			startSearchAni();
		} else {
			//			mBtnSearch.setText(R.string.search);
			//			stopSearchAni();
		}
	}
	private void removeAll() {
		PreferenceScreen ps=getPreferenceScreen();
		if (ps!=null) {
			synchronized(ps) {
				for (int i = ps.getPreferenceCount() - 1; i >= mFixedPrefCount; i--) {
					ps.removePreference(ps.getPreference(mFixedPrefCount));
				}
			}
		}
	}
	private void updateSearchList() {
		removeAll();
		BtDevice device=null;
		try{device = mService.getConnectDevice();}catch(Exception e){e.printStackTrace();}
		for (int i = 0; i < mSearchDeviceList.size(); i++) {
			if (device == null || !device.equals(mSearchDeviceList.get(i))) {
				addPreference(mSearchDeviceList.get(i));
			}
		}
	}
	private void addPreference(BtDevice device) {
		Preference pref = new Preference(getActivity());
		pref.setWidgetLayoutResource(R.layout.preference_widget_arrow);
		pref.setTitle(device.name);
		pref.setIcon(R.drawable.ic_bt_cellphone);
		getPreferenceScreen().addPreference(pref);
	}
//	private void updateConnected(){
//		try{
//			BtDevice device = mService.getConnectDevice();
//			if (device!=null) 
//				updateConnected(device.name);
//		}catch(Exception e)
//		{e.printStackTrace();}
//	}
//	private void updateConnected(String deviceName) {
//		if (deviceName==null || deviceName.isEmpty()) {
//			mConnectedDevice.setText(R.string.bluetooth_unknown_device);
//		}else {
//			mConnectedDevice.setText(deviceName);
//		}
//	}
	private View.OnClickListener mSearchClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mSearching) {
				stopSearch();
			} else {
				startSearch();
			}
		}
	};
	private OnItemClickListener mDeviceItemClick = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if (mService != null) {
				BtDevice device = mSearchDeviceList.get(position-mFixedPrefCount);
				if (device.state == BtDevice.STATE_DISCONNECTED && !device.equals(mDeviceConnecting)) {
					mDeviceConfirmConnect = device;
					showConnectDialog();
				}
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////////
	//
	private static final String KEY_DEVICE_NAME = "device_name";
	private static final String KEY_PIN_CODE= "pin_code";
	private static final String KEY_CONNECTED = "connected_device";
	private ListView mListView;

	private int mFixedPrefCount;
	private MyEditTextPreference mDeviceNamePref;
	private MyEditTextPreference mPinCodePref;
	private MyTextViewPreference mConnectedPref;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.bluetooth_settings);

		mDeviceNamePref = (MyEditTextPreference)findPreference(KEY_DEVICE_NAME);
		mPinCodePref = (MyEditTextPreference)findPreference(KEY_PIN_CODE);
		mConnectedPref = (MyTextViewPreference)findPreference(KEY_CONNECTED);
		mDeviceNamePref.setOnPreferenceChangeListener(this);
		mPinCodePref.setOnPreferenceChangeListener(this);
		mSdkCallback = new SdkCallback();
		getActivity().bindService(new Intent("com.hwatong.btphone.service.GocsdkService"),sdkConnect, Context.BIND_AUTO_CREATE);		
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mFixedPrefCount=getPreferenceScreen().getPreferenceCount();
		mListView = getListView();
	}

	@Override
	public void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		Log.d(TAG, "onPreferenceTreeClick: key="+ preference.getKey());
		String key= preference.getKey();
		if (key!=null && key.equals(KEY_CONNECTED) && connectedDeviceName!=null && !connectedDeviceName.equals("")) {
			showDisconnectDialog(connectedDeviceName);			
		}
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	@Override
	public void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onDestroy() {
		stopSearch();
		try {
			if(mService != null)
				mService.unregisterCallback(mSdkCallback);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		mService = null;
		getActivity().unbindService(sdkConnect);
		super.onDestroy();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Log.d(TAG, "onSharedPreferenceChanged: key="+ key );

	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		Log.d(TAG, "onPreferenceChange: key="+ preference.getKey() + ", newValue="+newValue);
		String text = (String)newValue;
		if (mService != null && preference.getKey().equals(KEY_DEVICE_NAME)) {
			if (!text.isEmpty()) {
				if (text.length() <= 15) {
					if (mService != null) {
						try {
							mService.setLocalName(text);
							return true; 
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				} else {
					Toast.makeText(getActivity(), R.string.bluetooth_btname_hint, Toast.LENGTH_SHORT).show();
				}
			}
		}
		if (mService != null && preference.getKey().equals(KEY_PIN_CODE)) {
			Pattern pattern = Pattern.compile("[0-9]{4}");
			if (!text.isEmpty()) {
				Matcher match = pattern.matcher(text);
				if (match.matches()) {
					if (mService != null) {
						try {
							mService.setPinCode(text);
							return true; 
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				} else {
					Toast.makeText(getActivity(), R.string.bluetooth_pincode_hint, Toast.LENGTH_SHORT).show();
				}
			} 
		}
		return false;
	}
	*/

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.bluetooth_settings);

	}
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		// TODO Auto-generated method stub
		
	}
}
