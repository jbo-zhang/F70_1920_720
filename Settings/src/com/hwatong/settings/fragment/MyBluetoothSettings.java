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

package com.hwatong.settings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.Selection;
import android.text.Spannable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.hwatong.btphone.ICallback;
import com.hwatong.btphone.IService;
import com.hwatong.settings.InfoDialog;
import com.hwatong.settings.R;
import com.hwatong.settings.SettingsPreferenceFragment;
import com.hwatong.settings.Utils;
import com.hwatong.providers.carsettings.SettingsProvider;

public class MyBluetoothSettings {
//extends SettingsPreferenceFragment implements OnClickListener, OnLongClickListener, OnItemClickListener, OnCheckedChangeListener, OnItemLongClickListener
//{
//	private static final String TAG = "MyBluetoothSettings";
//	private ListView mPairedListView, mSearchListView;
//	private DevicesAdapter mPairedAdapter, mSearchAdapter;
//	private View mContentView;
//	private Button btnDevName, btnSearch;
//
//	private ImageView mPairedView, mDeviceView;
//	private View mLayoutSearchList, mLayoutPairedList;
//	private EditText etDevName;
//	private TextView tvDevName, tvConnected, tvSearching;
//	private Switch mBluetoothSwitch;
//	private InfoDialog mDialog;
//
//	private boolean nextConnectCmd=false;
//	private boolean closeBluetooth=false;
//
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//
//		mSdkCallback = new SdkCallback();
//		getActivity().bindService(new Intent("com.hwatong.btphone.service.GocsdkService"),sdkConnect, Context.BIND_AUTO_CREATE);		
//	}
//
//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//		mContentView = inflater.inflate(R.layout.fragment_bluetooth, container, false);
//		tvDevName = (TextView)mContentView.findViewById(R.id.tv_device_name);
//		etDevName = (EditText)mContentView.findViewById(R.id.et_device_name);
//		btnDevName=(Button)mContentView.findViewById(R.id.btn_device_name);
//		btnDevName.setOnClickListener(this);
//		btnDevName.setOnLongClickListener(this);
//		tvConnected = (TextView)mContentView.findViewById(R.id.tv_bluetooth_connected);
//		btnSearch = (Button)mContentView.findViewById(R.id.btn_bluetooth_device);
//		btnSearch.setOnClickListener(this);
//		tvSearching = (TextView)mContentView.findViewById(R.id.tv_searching);
//
//		mSearchListView = (ListView)mContentView.findViewById(R.id.list_bluetooth_devices);
//		mPairedListView = (ListView)mContentView.findViewById(R.id.list_bluetooth_paired);
//
//		mSearchAdapter = new DevicesAdapter(getActivity(), mSearchDeviceList);
//		mPairedAdapter = new DevicesAdapter(getActivity(), mPairedDeviceList);
//		mSearchListView.setAdapter(mSearchAdapter);
//		mPairedListView.setAdapter(mPairedAdapter);
//		mSearchListView.setOnItemClickListener(this);
//		mPairedListView.setOnItemClickListener(this);
//		mPairedListView.setOnItemLongClickListener(this);
//
//		mDeviceView = (ImageView)mContentView.findViewById(R.id.iv_bluetooth_device);
//		mPairedView = (ImageView)mContentView.findViewById(R.id.iv_bluetooth_paired);
//		mPairedView.setOnClickListener(this);
//		mLayoutSearchList = mContentView.findViewById(R.id.ll_searchlist);
//		mLayoutPairedList = mContentView.findViewById(R.id.ll_pairedlist);
//
//
//		mBluetoothSwitch = (Switch)mContentView.findViewById(R.id.switch_bluetooth);
//		mBluetoothSwitch.setOnCheckedChangeListener(this);
//
//		refreshSearchSwitch();
//		return mContentView;
//	}
//
//	@Override
//	public void onActivityCreated(Bundle savedInstanceState) {
//		super.onActivityCreated(savedInstanceState);
//	}
//
//	@Override
//	public void onResume() {
//		super.onResume();
//	}
//
//	@Override
//	public void onPause() {
//		super.onPause();
//	}
//
//	@Override
//	public void onDestroy() {
//		stopSearch();
//		try {
//			if(mService != null)
//				mService.unregisterCallback(mSdkCallback);
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		}
//		mService = null;
//		getActivity().unbindService(sdkConnect);
//		super.onDestroy();
//	}
//
//	public static void hideIM(Context mContext, View view){
//		try{
//			InputMethodManager inputMethodManager = (InputMethodManager) ((Activity)mContext).getSystemService(Context.INPUT_METHOD_SERVICE);
//			if(inputMethodManager != null){
//				if(view == null)
//					view = ((Activity)mContext).getCurrentFocus();
//				if (view != null && view.getWindowToken() != null){
//					inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
//				}
//			}
//		}catch (Exception e){
//			e.printStackTrace();
//		}
//	}
//	public static void showIM(Context mContext, View view){
//		try{
//			InputMethodManager inputMethodManager = (InputMethodManager) ((Activity)mContext).getSystemService(Context.INPUT_METHOD_SERVICE);
//			if(inputMethodManager != null){
//				if (view != null){
//					inputMethodManager.showSoftInput(view, 0);
//				}
//			}  
//		}catch (Exception e){
//			e.printStackTrace();
//		}
//	}
//
//	protected void setTextSursorEnd(TextView view){
//		CharSequence text = view.getText();
//		if (text instanceof Spannable) {
//			Spannable spanText = (Spannable)text;
//			Selection.setSelection(spanText, text.length());
//		}
//	}
//
//	public void setListViewHeightBasedOnChildren(ListView listView) {
//
//		// 鑾峰彇ListView瀵瑰簲鐨凙dapter
//
//		ListAdapter listAdapter = listView.getAdapter();
//
//		if (listAdapter == null) {
//
//			return;
//
//		}
//
//		int totalHeight = 0;
//
//		for (int i = 0; i < listAdapter.getCount(); i++) { // listAdapter.getCount()杩斿洖鏁版嵁椤圭殑鏁扮洰
//
//			View listItem = listAdapter.getView(i, null, listView);
//
//			listItem.measure(0, 0); // 璁＄畻瀛愰」View 鐨勫楂�
//
//			totalHeight += listItem.getMeasuredHeight(); // 缁熻鎵�鏈夊瓙椤圭殑鎬婚珮搴�
//
//		}
//
//		ViewGroup.LayoutParams params = listView.getLayoutParams();
//
//		params.height = totalHeight
//				+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
//
//		// listView.getDividerHeight()鑾峰彇瀛愰」闂村垎闅旂鍗犵敤鐨勯珮搴�
//
//		// params.height鏈�鍚庡緱鍒版暣涓狶istView瀹屾暣鏄剧ず闇�瑕佺殑楂樺害
//
//		listView.setLayoutParams(params);
//
//	}	
//	private void editDevName() {
//		if(tvDevName.getVisibility()==View.VISIBLE) {
//			btnDevName.setText(R.string.bluetooth_edit_ok);
//			tvDevName.setVisibility(View.GONE);
//			etDevName.setText(tvDevName.getText());
//			etDevName.setVisibility(View.VISIBLE);
//			setTextSursorEnd(etDevName);
//			etDevName.requestFocus();
//			showIM(getActivity(), etDevName);
//		}else {
//			tvDevName.setVisibility(View.VISIBLE);
//			etDevName.setVisibility(View.GONE);
//			hideIM(getActivity(), etDevName);
//			btnDevName.setText(R.string.bluetooth_edit_modify);
//			if (!tvDevName.getText().toString().equals(etDevName.getText().toString())) {
//				try {
//					mService.setLocalName(etDevName.getText().toString());
//				} catch (RemoteException e) {
//					e.printStackTrace();
//				}
//				tvDevName.setText(etDevName.getText());
//			}
//		}
//	}
//	private ServiceConnection sdkConnect = new ServiceConnection() {
//
//		@Override
//		public void onServiceConnected(ComponentName name, IBinder serv) {
//			Log.d(TAG, "onServiceConnected:");
//			mService = IGocsdkService.Stub.asInterface(serv);
//			try {
//				mService.registerCallback(mSdkCallback);
//				mService.getLocalName();
//				mService.getPinCode();
//				if (!closeBluetooth) {
//					mService.getPairList(); 
//				}
//				mHandler.sendEmptyMessage(ID_REFRESH_CONNECTED_DEVICENAME);
//
//			} catch (RemoteException e) {
//				e.printStackTrace();
//			}
//		}
//
//		@Override
//		public void onServiceDisconnected(ComponentName arg0) {
//			mService = null;
//		}	
//	};
//
//	private void stopSearch() {
//		if (mSearching) {
//			if (mService != null) {
//				try {
//					mSearching = false;
//					mService.stopDiscovery();
//					mHandler.sendEmptyMessage(ID_REFRESH_SEARCH_STATE);
//				} catch (RemoteException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//	}
//
//	private void startSearch() {
//		Log.d(TAG, "startSearch:");
//		if (!mSearching && mService != null) {
//			try {
//				mSearchDeviceList.clear();
//				mService.startDiscovery();
//				mSearching = true;
//				mHandler.sendEmptyMessage(ID_REFRESH_SEARCH_STATE);
//				mHandler.sendEmptyMessage(ID_REFRESH_SEARCH_LISTVIEW);
//
//			} catch (RemoteException e) {
//				e.printStackTrace();
//			}
//		}
//	}
//
//
//	//////////////////////////////////////////////////////////////////////
//	//
//	private void refreshSearchState() {
//		if (mSearching) {
//			btnSearch.setVisibility(View.GONE);
//			tvSearching.setVisibility(View.VISIBLE);
//		} else {
//			btnSearch.setVisibility(View.VISIBLE);
//			tvSearching.setVisibility(View.GONE);
//		}
//	}
//	private boolean fromTouch=true;
//	private void refreshSearchSwitch() {
//		fromTouch=false;
//		closeBluetooth = Utils.getCarSettingsString(getContentResolver(), SettingsProvider.BLUETOOTH_SWITCH, SettingsProvider.DEFAULT_BLUETOOTH_SWITCH).equals("0");
//		mBluetoothSwitch.setChecked(!closeBluetooth);
//		btnSearch.setEnabled(!closeBluetooth);
//		fromTouch=true;
//	}
//
//	private void updateListState() {
//		synchronized(mPairedDeviceList) {
//			for(BtDevice d:mPairedDeviceList) {
//				if (d.equals(mDeviceConnecting)) {
//					d.state = BtDevice.STATE_CONNECTING;
//				}else if (d.equals(mDeviceDisconnecting)) {
//					d.state = BtDevice.STATE_DISCONNECTING;
//				}else if (d.equals(mDeviceConnected)) {
//					d.state = BtDevice.STATE_CONNECTED;
//				}else{
//					d.state = BtDevice.STATE_PAIRED;
//				}
//			}
//		}
//		synchronized(mSearchDeviceList) {
//			for(BtDevice d:mSearchDeviceList) {
//				if (d.equals(mDeviceConnecting)) {
//					d.state = BtDevice.STATE_CONNECTING;
//				}else if (d.equals(mDeviceDisconnecting)) {
//					d.state = BtDevice.STATE_DISCONNECTING;
//				}else if (d.equals(mDeviceConnected)) {
//					d.state = BtDevice.STATE_CONNECTED;
//				}else{
//					d.state = BtDevice.STATE_NOTCONNECTED;
//				}
//			}
//		}
//		mPairedAdapter.notifyDataSetChanged();
//		mSearchAdapter.notifyDataSetChanged();
//	}
//
//	///////////////////////////////////////////////////////////////////////////////////////////////
//	//adapter
//	class DevicesAdapter extends BaseAdapter {
//		private List<BtDevice> list;
//		private Context ctx;
//
//		public DevicesAdapter(Context context, List<BtDevice> list) {
//			this.list=list;
//			this.ctx = context;
//		}
//
//		@Override
//		public int getCount() {
//			return list.size();
//		}
//
//		@Override
//		public BtDevice getItem(int position) {
//			return list.get(position);
//		}
//
//		@Override
//		public long getItemId(int position) {
//			return position;
//		}
//
//		@Override
//		public View getView(int position, View convertView, ViewGroup parent) {
//			if (convertView == null)
//				convertView = View.inflate(ctx, R.layout.listview_bluetooth_item, null);
//			ViewHolder viewHolder = (ViewHolder) convertView.getTag();
//			if (viewHolder == null) {
//				viewHolder = new ViewHolder();
//				viewHolder.mName = (TextView) convertView.findViewById(R.id.tv_device);
//				viewHolder.mStatus = (TextView) convertView.findViewById(R.id.tv_state);
//				convertView.setTag(viewHolder);
//			}
//
//			synchronized(list) {
//				BtDevice d = list.get(position);
//				if (d.name.isEmpty()) {
//					viewHolder.mName.setText(R.string.bluetooth_unknown_device);
//				} else {
//					viewHolder.mName.setText(d.name);
//				}
//
//				if (d.state == BtDevice.STATE_NOTCONNECTED) {
//					viewHolder.mStatus.setText(R.string.bluetooth_notconnected);
//				} else if (d.state == BtDevice.STATE_CONNECTING){
//					viewHolder.mStatus.setText(R.string.bluetooth_connecting);
//				} else if (d.state == BtDevice.STATE_DISCONNECTING){
//					viewHolder.mStatus.setText(R.string.bluetooth_disconnecting);
//				} else if (d.state == BtDevice.STATE_PAIRED){
//					viewHolder.mStatus.setText(R.string.bluetooth_paired);
//				} else if (d.state == BtDevice.STATE_CONNECTED){
//					viewHolder.mStatus.setText(R.string.bluetooth_connected);
//				} else {
//					viewHolder.mStatus.setText("");
//				}
//			}
//			return convertView;
//		}
//	}
//
//	static class ViewHolder {
//		TextView mName;
//		TextView mStatus;
//	}
//
//	///////////////////////////////////////////////////////////////////////////////////////////////
//	//server
//	private IGocsdkService mService;
//	private SdkCallback mSdkCallback = new SdkCallback();
//	private class SdkCallback extends IGocsdkCallback.Stub {
//		@Override public void onHfpDisconnected() throws RemoteException { Log.d(TAG, "onDisconnected:"); onDisconnected(); }
//		@Override public void onHfpConnected() throws RemoteException { Log.d(TAG, "onConnected:"); onConnected(); }
//		@Override public void onSignalBattery(String signal, String battery) throws RemoteException {Log.d(TAG, "onSignalBattery: signal="+signal+", battery="+battery); }
//		@Override public void onCallSucceed(String str) throws RemoteException {Log.d(TAG, "onCallSucceed:str="+str);}
//		@Override public void onIncoming(String number) throws RemoteException {Log.d(TAG, "onIncoming:number="+number);}
//		@Override public void onHangUp() throws RemoteException {Log.d(TAG, "onHangUp:");}
//		@Override public void onTalking(String str) throws RemoteException {Log.d(TAG, "onTalking:str="+str);}
//		@Override public void onRingStart() throws RemoteException {Log.d(TAG, "onRingStart:");}
//		@Override public void onRingStop() throws RemoteException {Log.d(TAG, "onRingStop:");}
//		@Override public void onHfpLocal() throws RemoteException {Log.d(TAG, "onHfpLocal:");}
//		@Override public void onHfpRemote() throws RemoteException {Log.d(TAG, "onHfpRemote:");}
//		@Override public void onInPairMode() throws RemoteException {Log.d(TAG, "onInPairMode:");}
//		@Override public void onExitPairMode() throws RemoteException {Log.d(TAG, "onExitPairMode:");}
//		@Override public void onInitSucceed() throws RemoteException {Log.d(TAG, "onInitSucceed:");}
//		@Override public void onMusicPlaying() throws RemoteException {Log.d(TAG, "onMusicPlaying:");}
//		@Override public void onMusicStopped() throws RemoteException {Log.d(TAG, "onMusicStopped:");}
//		@Override public void onAutoConnectAccept(boolean autoConnect, boolean autoAccept) throws RemoteException {Log.d(TAG, "onAutoConnectAccept: autoConnect="+autoConnect+", autoAccept="+autoAccept);}
//		@Override public void onCurrentAddr(String addr) throws RemoteException {Log.d(TAG, "onCurrentAddr: addr="+addr); onConnectedDeviceAddr(addr);}
//		@Override public void onCurrentName(String name) throws RemoteException { Log.d(TAG, "onCurrentName: name="+name); onConnectedDeviceName(name); }
//		@Override public void onHfpStatus(int status) throws RemoteException {Log.d(TAG, "onHfpStatus: status="+status);}
//		@Override public void onAvStatus(int status) throws RemoteException {Log.d(TAG, "onAvStatus: status="+status);}
//		@Override public void onVersionDate(String version) throws RemoteException {Log.d(TAG, "onVersionDate: version="+version);}
//		@Override public void onCurrentDeviceName(String name) throws RemoteException { Log.d(TAG, "onCurrentDeviceName: name="+name); onGetDeviceName(name);}
//		@Override public void onCurrentPinCode(String code) throws RemoteException { Log.d(TAG, "onCurrentPinCode: pincode="+code); onGetPinCode(code);}
//		@Override public void onA2dpConnected() throws RemoteException {Log.d(TAG, "onA2dpConnected:");}
//		@Override public void onCurrentAndPairList(int index, String name, String addr) throws RemoteException { Log.d(TAG, "onCurrentAndPairList锛歩ndex="+index+", name="+name+", addr="+addr);	 onGetPairList(index, name, addr);}
//		@Override public void onA2dpDisconnected() throws RemoteException {Log.d(TAG, "onA2dpDisconnected:");}
//		@Override public void onPhoneBook(String name, String number) throws RemoteException {Log.d(TAG, "onPhoneBook: name="+name +", number="+number); }
//		@Override public void onSimBook(String name, String number) throws RemoteException {Log.d(TAG, "onSimBook: name="+name +", number="+number);}
//		@Override public void onPhoneBookDone() throws RemoteException {Log.d(TAG, "onPhoneBookDone:");}
//		@Override public void onSimDone() throws RemoteException {Log.d(TAG, "onSimDone:");}
//		@Override public void onCalllogDone() throws RemoteException {Log.d(TAG, "onCalllogDone:");}
//		@Override public void onCalllog(int type, String name, String number, String date) throws RemoteException {Log.d(TAG, "onCalllog: type="+type+", name="+name +", number="+number); }
//		@Override public void onDiscovery(String name, String addr) throws RemoteException {Log.d(TAG, "onDiscovery: name="+name + ", addr="+addr); onDeviceDiscovery(name,addr);} 
//		@Override public void onDiscoveryDone() throws RemoteException {Log.d(TAG, "onDiscoveryDone"); onDeviceDiscoveryDone();}
//		@Override public void onLocalAddress(String addr) throws RemoteException {Log.d(TAG, "onLocalAddress: addr="+addr);}
//		@Override public void onOutGoingOrTalkingNumber(String number) throws RemoteException {Log.d(TAG, "onOutGoingOrTalkingNumber: number="+number);}
//		@Override public void onConnecting() throws RemoteException {Log.d(TAG, "onConnecting:");}
//		@Override public void onSppData(int index, String data) throws RemoteException {Log.d(TAG, "onSppData: index="+index+ ", data="+data);}
//		@Override public void onSppConnect(int index) throws RemoteException {Log.d(TAG, "onSppConnect: index="+index);}
//		@Override public void onSppDisconnect(int index) throws RemoteException {Log.d(TAG, "onSppDisconnect: index="+index);}
//		@Override public void onSppStatus(int status) throws RemoteException {Log.d(TAG, "onSppStatus: status="+status);}
//		@Override public void onOppReceivedFile(String path) throws RemoteException {Log.d(TAG, "onOppReceivedFile: path="+path);}
//		@Override public void onOppPushSuccess() throws RemoteException {Log.d(TAG, "onOppPushSuccess:");}
//		@Override public void onOppPushFailed() throws RemoteException {Log.d(TAG, "onOppPushFailed:");}
//		@Override public void onHidConnected() throws RemoteException {Log.d(TAG, "onHidConnected:");}
//		@Override public void onHidDisconnected() throws RemoteException {Log.d(TAG, "onHidDisconnected:");}
//		@Override public void onHidStatus(int status) throws RemoteException {Log.d(TAG, "onHidStatus: status="+status);}
//		@Override public void onMusicInfo(String name, String artist, int duration, int pos,int total) throws RemoteException {Log.d(TAG, "onMusicInfo: name="+name+", artist="+artist+", duration="+duration+", pos="+pos+", total="+total);}
//		@Override public void onPanConnect() throws RemoteException {Log.d(TAG, "onPanConnect:");}
//		@Override public void onPanDisconnect() throws RemoteException {Log.d(TAG, "onPanDisconnect:");}
//		@Override public void onPanStatus(int status) throws RemoteException {Log.d(TAG, "onPanStatus: status="+status);}
//		@Override public void onVoiceConnected() throws RemoteException {Log.d(TAG, "onVoiceConnected:");}
//		@Override public void onVoiceDisconnected() throws RemoteException {Log.d(TAG, "onVoiceDisconnected:");}
//		@Override public void onProfileEnbled(boolean[] enabled) throws RemoteException {Log.d(TAG, "onProfileEnbled: enabled="+enabled);}
//		@Override public void onPhoneMessage(String handle, boolean read, String time, String name, String num, String title) throws RemoteException {Log.d(TAG, "onPhoneMessage: handle="+handle+", read="+read+", time="+time+", name="+name+", num="+num+", title="+title);}
//		@Override public void onPhoneMessageText(String text) throws RemoteException {Log.d(TAG, "onPhoneMessageText: text="+text);}
//	}
//
//	private List<BtDevice> mSearchDeviceList = new ArrayList<BtDevice>();
//	private List<BtDevice> mPairedDeviceList = new ArrayList<BtDevice>();
//
//	private boolean mSearching, mPairedShow;
//	private BtDevice mDeviceConnected;
//	private BtDevice mDeviceConfirmConnect;
//	private BtDevice mDeviceConnecting;
//	private BtDevice mDeviceDisconnecting;
//
//	private String deviceName, pinCode;
//	private final static int ID_SET_DEVICENAME=0x1101;
//	private final static int ID_SET_PINCODE=0x1102;
//	private final static int ID_REFRESH_CONNECTED_DEVICENAME=0x1103;
//	private final static int ID_REFRESH_SEARCH_STATE=0x1104;
//	private final static int ID_REFRESH_SEARCH_LISTVIEW=0x1105;
//	private final static int ID_REFRESH_PAIRED_LISTVIEW=0x1106;
//
//	@SuppressLint("HandlerLeak") 
//	private Handler mHandler = new Handler() {
//		@Override
//		public void handleMessage(Message msg) {
//			switch(msg.what) {
//			case ID_SET_DEVICENAME:
//				tvDevName.setText(deviceName);
//				break;
//			case ID_SET_PINCODE:
//				//				mPinCodePref.setText(pinCode);
//				break;
//			case ID_REFRESH_CONNECTED_DEVICENAME:
//				try{
//					if (mService!=null) { 
//						mDeviceConnected = mService.getConnectDevice();
//					}
//				}catch(RemoteException e)
//				{
//					e.printStackTrace();
//				}
//				if (mDeviceConnected!=null) {
//					Log.d(TAG, "ID_REFRESH_CONNECTED_DEVICENAME Name="+mDeviceConnected.name);
//					if (closeBluetooth) {
//						disconnect();
//					}else {
//						tvConnected.setText(mDeviceConnected.name);
//						mDeviceView.setVisibility(View.VISIBLE);
//					}
//				}else {
//					tvConnected.setText("");
//					mDeviceView.setVisibility(View.GONE);
//				}
//
//				updateListState();
//				break;
//			case ID_REFRESH_SEARCH_STATE:
//				refreshSearchState();
//				break;
//			case ID_REFRESH_SEARCH_LISTVIEW:
//				setListViewHeightBasedOnChildren(mSearchListView);
//				if (mLayoutSearchList.getVisibility()==View.GONE) 
//					mLayoutSearchList.setVisibility(View.VISIBLE);
//				break;
//			case ID_REFRESH_PAIRED_LISTVIEW:
//				if (mPairedShow && mPairedDeviceList.size()>0) {
//					setListViewHeightBasedOnChildren(mPairedListView);
//					mPairedView.setImageResource(R.drawable.arrow_up);
//					mLayoutPairedList.setVisibility(View.VISIBLE);
//				}else {
//					mPairedView.setImageResource(R.drawable.arrow_down);
//					mLayoutPairedList.setVisibility(View.GONE);
//				}
//				break;
//			}
//		}
//	};
//	private void onGetDeviceName(String deviceName) {
//		this.deviceName=deviceName;
//		mHandler.sendEmptyMessage(ID_SET_DEVICENAME);
//	}
//	private void onGetPinCode(String pincode) {
//		this.pinCode=pincode;
//		mHandler.sendEmptyMessage(ID_SET_PINCODE);
//	}
//	private void onConnectedDeviceName(String deviceName) {
//		mHandler.sendEmptyMessage(ID_REFRESH_CONNECTED_DEVICENAME);
//	}
//	private void onConnectedDeviceAddr(String deviceName) {
//	}
//
//	private void onDisconnected() {
//		mDeviceConnecting=null;
//		mDeviceDisconnecting=null;
//		mDeviceConnected=null;
//
//		mHandler.sendEmptyMessage(ID_REFRESH_CONNECTED_DEVICENAME);
//		//濡傛灉鏄负杩炴帴闇�瑕佹柇寮�鍘熸湁鐨勮繛鎺ワ紝鍒欑户缁墽琛岃繛鎺ュ懡浠�
//		if (nextConnectCmd) {
//			nextConnectCmd=false;
//			connect();
//		}
//	}
//	private void onConnected() {
//		mDeviceDisconnecting=null;
//		mDeviceConnecting=null;
//	}
//	private void onDeviceDiscovery(String deviceName, String deviceAddr) {
//		if (!mSearching) return;
//		try {
//			BtDevice d = new BtDevice(deviceName, deviceAddr, BtDevice.STATE_NOTCONNECTED);
//			synchronized(mSearchDeviceList) {
//				if (!mPairedDeviceList.contains(d)) {
//					mSearchDeviceList.add(d);
//					mHandler.sendEmptyMessage(ID_REFRESH_SEARCH_LISTVIEW);
//				}
//			}
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
//	}
//	private void onDeviceDiscoveryDone() {
//		mSearching = false;
//		mHandler.sendEmptyMessage(ID_REFRESH_SEARCH_STATE);
//	}
//	private void onGetPairList(int index, String name, String addr) {
//		BtDevice d = new BtDevice(name, addr, BtDevice.STATE_PAIRED);
//		if (d.equals(mDeviceConnected)) d.state = BtDevice.STATE_CONNECTED;
//		else if (d.equals(mDeviceConnecting)) d.state = BtDevice.STATE_CONNECTING;
//		else if (d.equals(mDeviceDisconnecting)) d.state = BtDevice.STATE_DISCONNECTING;
//
//		synchronized (mPairedDeviceList) {
//			if (!mPairedDeviceList.contains(d)) {
//				mPairedDeviceList.add(d);
//				mHandler.sendEmptyMessage(ID_REFRESH_PAIRED_LISTVIEW);
//			}
//		}
//		synchronized (mSearchDeviceList) {
//			for(BtDevice bt:mSearchDeviceList) {
//				if (bt.equals(d)) {
//					mSearchDeviceList.remove(bt);
//					mHandler.sendEmptyMessage(ID_REFRESH_SEARCH_LISTVIEW);
//					break;
//				}
//			}
//		}
//	}
//
//	@Override
//	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//		if (!fromTouch) return;
//		switch(buttonView.getId()) {
//		case R.id.switch_bluetooth: //钃濈墮寮�鍏�
//			if (isChecked) {
//				 
//				Utils.putCarSettingsString(getContentResolver(), SettingsProvider.BLUETOOTH_SWITCH, "1");
//				try{mService.getPairList();}catch(RemoteException e) {e.printStackTrace();} //mService.openBluetooth();
//			}else{
//				Utils.putCarSettingsString(getContentResolver(), SettingsProvider.BLUETOOTH_SWITCH, "0");
//				if (mDeviceConnected!=null) {
//					disconnect();
//					mDeviceConnected=null;
//					mHandler.sendEmptyMessage(ID_REFRESH_CONNECTED_DEVICENAME);
//				}
//				stopSearch();
////				try{mService.closeBluetooth();}catch(RemoteException e) {e.printStackTrace();}
//				
//				synchronized(mSearchDeviceList) {mSearchDeviceList.clear();}
//				mHandler.sendEmptyMessage(ID_REFRESH_SEARCH_LISTVIEW);
//
//				synchronized(mPairedDeviceList) {mPairedDeviceList.clear();}
//				mHandler.sendEmptyMessage(ID_REFRESH_PAIRED_LISTVIEW);
//			}
//			refreshSearchSwitch();
//			break;
//		}
//	}
//
//	@Override
//	public void onClick(View v) {
//		switch(v.getId()) {
//		case R.id.btn_device_name:
//			editDevName();
//			break;
//		case R.id.btn_bluetooth_device:
//			startApp("com.hwatong.fileman");
////			if (mSearching) {
////				stopSearch();
////			} else {
////				startSearch();
////			}
//			break;
//		case R.id.iv_bluetooth_paired:
//			mPairedShow = !mPairedShow;
//			mHandler.sendEmptyMessage(ID_REFRESH_PAIRED_LISTVIEW);
//			break;
//		case R.id.button1:
//			if (isLongClick) {//闀挎寜鍒犻櫎閰嶅
//				if (mService != null) {
//					deletePaired();
//				}
//
//			}else {//鐭寜杩炴帴鍜屾柇寮�
//				if (mService != null && mDeviceConfirmConnect.state == BtDevice.STATE_CONNECTED) {
//					disconnect();
//				} else if (mService != null && mDeviceConfirmConnect.state == BtDevice.STATE_NOTCONNECTED) {
//					connect();
//				}
//			}
//			mDialog.dismiss();
//			break;
//		case R.id.button2:
//			mDialog.dismiss();
//			break;
//		}
//	}
//	@Override
//	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//		Log.d(TAG, "onItemClick: id="+parent.getId()+", position="+position);
//		if (mService == null) return;
//
//		isLongClick=false;
//		if (parent.getId()==R.id.list_bluetooth_devices) {
//			BtDevice device = mSearchDeviceList.get(position);
//			if (device.state == BtDevice.STATE_NOTCONNECTED) { //杩炴帴鍒拌澶囷紝闇�瑕佹墜鏈虹杈撳叆閰嶅瀵嗙爜0000
//				mDeviceConfirmConnect = device;
//				String title = getActivity().getString(R.string.bluetooth_notif_title);
//				String message = getActivity().getString(R.string.bluetooth_notif_message, device.name);
//				mDialog = new InfoDialog(getActivity(), this);
//				mDialog.show();
//				mDialog.setTitle(title);
//				mDialog.setMessage(message);
//				mDialog.setSubmitButton(getResources().getString(R.string.dlg_ok));
//				mDialog.setCancelButton(getResources().getString(R.string.dlg_cancel));
//			}
//		}else if (parent.getId()==R.id.list_bluetooth_paired) {
//			BtDevice device = mPairedDeviceList.get(position);
//			mDeviceConfirmConnect = device;
//
//			if (device.state == BtDevice.STATE_CONNECTED) {
//				String title = device.name;
//				String message = getActivity().getString(R.string.bluetooth_disconnect_title);
//				mDialog = new InfoDialog(getActivity(), this);
//				mDialog.show();
//				mDialog.setTitle(title);
//				mDialog.setMessage(message);
//				mDialog.setSubmitButton(getResources().getString(R.string.dlg_ok));
//				mDialog.setCancelButton(getResources().getString(R.string.dlg_cancel));
//			}else if (device.state == BtDevice.STATE_PAIRED) {
//				connect();
//			}
//		}
//	}
//	private boolean isLongClick=false;
//	@Override
//	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//		Log.d(TAG, "onItemLongClick: id="+parent.getId()+", position="+position);
//		isLongClick=true;
//		if (parent.getId()==R.id.list_bluetooth_paired) {
//			BtDevice device = mPairedDeviceList.get(position);
//			mDeviceConfirmConnect = device;
//			String title = device.name;
//			String message = getActivity().getString(R.string.bluetooth_device_context_unpair);
//			if (device.state == BtDevice.STATE_CONNECTED) {
//				message = getActivity().getString(R.string.bluetooth_device_context_disconnect_unpair);
//			}
//
//			mDialog = new InfoDialog(getActivity(), this);
//			mDialog.show();
//			mDialog.setTitle(title);
//			mDialog.setMessage(message);
//			mDialog.setSubmitButton(getResources().getString(R.string.dlg_ok));
//			mDialog.setCancelButton(getResources().getString(R.string.dlg_cancel));
//		} 
//
//		return false;
//	}
//	
//
//	private void disconnect() {//鏂紑璁惧
//		Log.d(TAG, "disconnect");
//		if (mService != null) { //杩炴帴鍒拌澶�
//			try {
//				mDeviceDisconnecting = mDeviceConfirmConnect;
//				updateListState();
//				mService.disconnect();
//			} catch (RemoteException e)
//			{
//				e.printStackTrace();
//			}
//		}
//	}
//
//	private void deletePaired() { //鍒犻櫎閰嶅
//		Log.d(TAG, "deletePaired");
//		try {
//			if (mDeviceConfirmConnect.state == BtDevice.STATE_CONNECTED) {
//				mService.disconnect();
//			}
//			mService.deletePair(mDeviceConfirmConnect.addr);
//			mPairedDeviceList.clear();
//			mService.getPairList();
//		} catch (RemoteException e)
//		{
//			e.printStackTrace();
//		}
//	}
//	private void connect() {
//		Log.d(TAG, "connect");
//		if (mService != null) { //杩炴帴鍒拌澶�
//			try {
//				stopSearch();
//				if (mDeviceConnected!=null) {
//					disconnect();
//					nextConnectCmd=true;
//				}else {
//					mDeviceConnecting = mDeviceConfirmConnect;
//					updateListState();
//					Log.d(TAG, "connect to device: name=" + mDeviceConfirmConnect.name);
//					mService.connectDevice(mDeviceConfirmConnect.addr);
//				}
//
//			} catch (RemoteException e) {
//				e.printStackTrace();
//			}
//		}
//
//	}
//	//////////////////////////////////////////////////////////////////////////////
//	//
//
//	@Override
//	public boolean onLongClick(View v) {
//		if(v.getId() == R.id.btn_device_name) {
//			startApp("com.hwatong.zhongtai_fileman");
//		}
//		return false;
//	}
//	private void toast(String s) {
//		Toast.makeText(getActivity(), s, Toast.LENGTH_SHORT).show();
//	}
//	
//	private void startApp(String app) {
//		Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage(app);
//		if (intent != null) {
//			try {
//				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				startActivity(intent);
//			} catch (Exception e) {
//				Log.e(TAG, "startApp error: " + e);
//			}
//		}
//	}

}
