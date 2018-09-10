package com.hwatong.settings.wifi;

import com.hwatong.f70.main.F70Application;
import com.hwatong.f70.main.LogUtils;
import com.hwatong.settings.R;
import com.hwatong.settings.SettingsPreferenceFragment;
import com.hwatong.settings.preference.SelectWifiPreference;
import com.hwatong.settings.preference.WifiApEditPreference;
import com.hwatong.settings.preference.WifiApSwichPreference;
import com.hwatong.settings.wifi.F70_WifiAp_Dialog.OnCustomDialogListener;
import com.tricheer.remoteservice.IRemoteService;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;

public class WifiApSettings extends SettingsPreferenceFragment implements
		OnClickListener, OnSharedPreferenceChangeListener,
		Preference.OnPreferenceClickListener {

	private static final String KEY_NETWORK_SELECT = "select_network";
	private static final String WIFI_AP_SWITCH = "wifi_switch_ap";
	private static final String WIFI_AP_NAME = "wifi_ap_name";
	private static final String WIFI_AP_ENCRY = "wifi_ap_encry";
	private static final String WIFI_AP_TRIFFIC = "wifiap_traffic";

	private SelectWifiPreference mSelectWifiPreference; //选择网络
	private WifiApSwichPreference mWifiApSwichPreference; //wifi热点开关
	private WifiApEditPreference mWifiApTrafficPreference; //wifi热点开关
	private WifiApEditPreference mWifiApNamePreference;//wifi热点名称
	private WifiApEditPreference mWifiApEncryPreference;//wifi热点密码
//	private WifiApEditPreference mWifiApConnectedPreference;//连接的设备
	
	private WifiManager wifiManager;
	private IntentFilter intentFilter;
	private Resources resources;
	private WifiConfiguration mWifiConfiguration;
	private PreferenceScreen thisPreferenceScreen;
	private IRemoteService mService;
	
	private F70_WifiAp_Dialog passwordDialog, nameDialog;
	
	private static final int WIFIAP_DIALOG_SSID_EDIT_MAX = 15;
	private static final int WIFIAP_DIALOG_PASSWORD_EDIT_MAX = 15;

	private SelectWifiPreference.OnSelectWifiListener onSelectWifiListener;
	private int whichNetwork;

	public WifiApSettings(
			SelectWifiPreference.OnSelectWifiListener onSelectWifiListener,
			int what) {
		this.onSelectWifiListener = onSelectWifiListener;
		this.whichNetwork = what;
		
	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (WifiManager.WIFI_AP_STATE_CHANGED_ACTION.equals(action)) {
				handleWifiApStateChanged(intent.getIntExtra(
						WifiManager.EXTRA_WIFI_AP_STATE,
						WifiManager.WIFI_AP_STATE_FAILED));
			} else if (ConnectivityManager.ACTION_TETHER_STATE_CHANGED
					.equals(action)) {

			} else if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
			}

		}
	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		addPreferencesFromResource(R.xml.wifi_ap_settings);
		thisPreferenceScreen = getPreferenceScreen();
		wifiManager = (WifiManager) getActivity().getSystemService(
				Context.WIFI_SERVICE);
		
		intentFilter = new IntentFilter(
				WifiManager.WIFI_AP_STATE_CHANGED_ACTION);
		getActivity().registerReceiver(mReceiver, intentFilter);
		resources = getActivity().getResources();
		
		mSelectWifiPreference = (SelectWifiPreference) findPreference(KEY_NETWORK_SELECT);
		mSelectWifiPreference.setOnSelectWifiListener(onSelectWifiListener);
		mSelectWifiPreference.update(whichNetwork);

		mWifiApSwichPreference = (WifiApSwichPreference) findPreference(WIFI_AP_SWITCH);
		mWifiApTrafficPreference = (WifiApEditPreference) findPreference(WIFI_AP_TRIFFIC);
//		mWifiApTrafficPreference.updateContent(resources.getString(R.string.wifi_ap_defaulttraffic));
//		mWifiApNamePreference = (WifiApEditPreference) findPreference(WIFI_AP_NAME);
//		mWifiApEncryPreference = (WifiApEditPreference) findPreference(WIFI_AP_ENCRY);
		wifiManager = (WifiManager) getActivity().getSystemService(
				Context.WIFI_SERVICE);
		intentFilter = new IntentFilter(
				WifiManager.WIFI_AP_STATE_CHANGED_ACTION);
		getActivity().registerReceiver(mReceiver, intentFilter);
		resources = getActivity().getResources();
		handleWifiApStateChanged(wifiManager.getWifiApState());
	}
	
	private void initService() {
		getActivity().bindService(new Intent("com.tricheer.remoteservice"),
				mServiceConnection, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		F70Application.isShowWifi = false;
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		initService();
		if(mWifiApSwichPreference != null)
			mWifiApSwichPreference.resume();
		
		changedActivityImage(this.getClass().getName());
	}
	
	@Override
	public void onStop() {
		super.onStop();
		if(passwordDialog != null && passwordDialog.isShowing()) {
			passwordDialog.dismiss();
			passwordDialog = null;
		}
		
		if(nameDialog != null && nameDialog.isShowing()) {
			nameDialog.dismiss();
			nameDialog = null;
		}
	}
	

	@Override
	public void onPause() {
		LogUtils.d("onPause");
		super.onPause();
		if (mService != null) {
			try {
				mService.unregisterCallback(mCallback);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		getActivity().unbindService(mServiceConnection);
		if(mWifiApSwichPreference != null)
			mWifiApSwichPreference.pause();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		removeWifiApInfo();
		getActivity().unregisterReceiver(mReceiver);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		String title = preference.getTitle().toString();
		if (title.equals(mWifiApNamePreference.getTitle().toString())) {
			showNameDialog(mWifiApNamePreference.getTitle().toString(), mWifiApNamePreference.getContent().toString());

		} else if (title.equals(mWifiApEncryPreference.getTitle().toString())) {
			showPasswordDialog(mWifiApEncryPreference.getTitle().toString());
		}
		return false;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {

	}

	@Override
	public void onClick(View v) {
	}

	private void handleWifiApStateChanged(int state) {
		LogUtils.d("WifiApstate changed: " + state);
		switch (state) {
		case WifiManager.WIFI_AP_STATE_ENABLING:
			// getPreferenceScreen().addPreference(mWifiApEncryPreference);
			// getPreferenceScreen().addPreference(mWifiApNamePreference);
			break;
		case WifiManager.WIFI_AP_STATE_ENABLED:
			initWifiApInfo();

			break;
		case WifiManager.WIFI_AP_STATE_DISABLING:
			removeWifiApInfo();
			break;
		case WifiManager.WIFI_AP_STATE_DISABLED:
			// if(mWifiApNamePreference != null && mWifiApEncryPreference !=
			// null) {
			// getPreferenceScreen().removePreference(mWifiApEncryPreference);
			// getPreferenceScreen().removePreference(mWifiApNamePreference);
			// }
			break;
		default:
			break;
		}
	}
	
	private void initWifiApInfo() {
		if(mWifiApNamePreference != null && mWifiApEncryPreference != null)
			return;
		mWifiConfiguration = wifiManager.getWifiApConfiguration();
		LogUtils.d("initWifiApInfo: " + "ssid: " + mWifiConfiguration.SSID + "password: " + mWifiConfiguration.preSharedKey);
		mWifiApNamePreference = new WifiApEditPreference(getActivity());
		mWifiApEncryPreference = new WifiApEditPreference(getActivity());
//		mWifiApConnectedPreference = new WifiApEditPreference(getActivity());

		thisPreferenceScreen.addPreference(mWifiApNamePreference);
		thisPreferenceScreen.addPreference(mWifiApEncryPreference);
//		thisPreferenceScreen.addPreference(mWifiApConnectedPreference);

		mWifiApNamePreference.setOnPreferenceClickListener(this);
		mWifiApEncryPreference.setOnPreferenceClickListener(this);

		mWifiApNamePreference.updateTitle(resources
				.getString(R.string.wifi_ap_name));
		mWifiApEncryPreference.updateTitle(resources
				.getString(R.string.wifi_ap_encrypted));
//		mWifiApConnectedPreference.updateTitle(resources.getString(R.string.wifi_ap_device));
		
		mWifiApNamePreference.updateContent(mWifiConfiguration.SSID);
		mWifiApEncryPreference.updateContent(mWifiConfiguration.preSharedKey);
		
		mWifiApNamePreference.notifyViewChanged();
		mWifiApEncryPreference.notifyViewChanged();
	}
	
	private void removeWifiApInfo() {
		if (mWifiApNamePreference != null && mWifiApEncryPreference != null) {
			thisPreferenceScreen.removePreference(mWifiApEncryPreference);
			thisPreferenceScreen.removePreference(mWifiApNamePreference);
//			thisPreferenceScreen.removePreference(mWifiApConnectedPreference);
			mWifiApEncryPreference.setOnPreferenceClickListener(null);
			mWifiApNamePreference.setOnPreferenceClickListener(null);
			mWifiApEncryPreference = null;
			mWifiApNamePreference = null;
		}
	}

	private void showPasswordDialog(String title) {
		if(passwordDialog != null && passwordDialog.isShowing()) {
			passwordDialog.dismiss();
			passwordDialog = null;
		}
		passwordDialog = new F70_WifiAp_Dialog(getActivity(),
				title, true, new OnCustomDialogListener() {

					@Override
					public void back(String name) {
						LogUtils.d("get password: " + name);
						resetNetwork(mWifiApNamePreference.getContent(), name);
					}
				});
		Window win = passwordDialog.getWindow();
		LayoutParams params = new LayoutParams();
		params.width = LayoutParams.WRAP_CONTENT;
		params.height = LayoutParams.WRAP_CONTENT;
		params.x = -210;// 设置x坐标
		win.setAttributes(params);
		passwordDialog.show();
	}
	
	private void showNameDialog(String title, String currentName) {
		if(nameDialog != null && nameDialog.isShowing()) {
			nameDialog.dismiss();
			nameDialog = null;
		}
		
		nameDialog = new F70_WifiAp_Dialog(getActivity(),
				title, false, new OnCustomDialogListener() {

					@Override
					public void back(String name) {
						LogUtils.d("get name: " + name);
						String xx = TextUtils.isEmpty(name) ? "F70" : name;
						resetNetwork(xx, mWifiApEncryPreference.getContent());
					}
				});
		Window win = nameDialog.getWindow();
		LayoutParams params = new LayoutParams();
		params.width = LayoutParams.WRAP_CONTENT;
		params.height = LayoutParams.WRAP_CONTENT;
		params.x = -210;// 设置x坐标
		win.setAttributes(params);
		
		nameDialog.show();
		nameDialog.setEditContent(currentName);
	}
	
    /**
     * accept a encrtpt type parm
     */
    private void resetNetwork(String ssid, String password) {
        WifiConfiguration config = new WifiConfiguration();
        LogUtils.d("resetNetwork: " + "ssid: " + ssid + ", password: " + password);
//        config.channel = mWifiConfiguration.channel;
//        config.channelWidth = mWifiConfiguration.channelWidth;
        
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA2_PSK);
        config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        config.preSharedKey = password; 
        config.SSID = ssid;
            if (wifiManager.getWifiApState() == WifiManager.WIFI_AP_STATE_ENABLED) {
            	wifiManager.setWifiApEnabled(null, false);
            	wifiManager.setWifiApEnabled(config, true);
            } else {
            	wifiManager.setWifiApConfiguration(config);
            }
    }
    
    private void updateFlow(int usedFlow, int setFlow) {
    	if(mWifiApTrafficPreference != null) {
    		mWifiApTrafficPreference.updateContent(String.valueOf(usedFlow) + "MB/" + String.valueOf(setFlow) + "MB");
    		mWifiApTrafficPreference.notifyViewChanged();
    	}
    	else
    		LogUtils.d("mWifiApTrafficPreference is null");
    }
    
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			updateFlow(msg.arg1, msg.arg2);
		}
	};
    
	private final ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LogUtils.d("iRemoteservice onServiceConnected");
			mService = com.tricheer.remoteservice.IRemoteService.Stub
					.asInterface(service);
			if (mService != null) {
				try {
					mService.registerCallback(mCallback);
					mService.getFlow();
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
			} else
				LogUtils.d("iRemoteService is null");

		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			if (mService != null) {
				mService = null;
			}
		}
	};

	private final com.tricheer.remoteservice.IRemoteCallback.Stub mCallback = new com.tricheer.remoteservice.IRemoteCallback.Stub() {
		@Override
		public void onLogin() throws RemoteException {
		}

		@Override
		public void onLogout() throws RemoteException {
			
			
			
		}

		@Override
		public void onContacts(String name, String phone)
				throws RemoteException {
		}

		@Override
		public void onFlow(int flow, int usedFlow, int setFlow) throws RemoteException {
			LogUtils.d("get usedflow: " + usedFlow + ", setFlow: " + setFlow);
			Message msg = Message.obtain();
			msg.arg1 = usedFlow;
			msg.arg2 = setFlow;
			handler.sendMessage(msg);
		}

	};

}
