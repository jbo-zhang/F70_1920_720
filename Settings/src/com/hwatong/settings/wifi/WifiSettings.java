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

package com.hwatong.settings.wifi;

import static android.net.wifi.WifiConfiguration.INVALID_NETWORK_ID;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.security.Credentials;
import android.security.KeyStore;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hwatong.f70.main.F70Application;
import com.hwatong.f70.main.LogUtils;
import com.hwatong.settings.R;
import com.hwatong.settings.SettingsPreferenceFragment;
import com.hwatong.settings.preference.MyWifiConnected;
import com.hwatong.settings.preference.MyWifiPreference;
import com.hwatong.settings.preference.MyWifiSearch;
import com.hwatong.settings.preference.MyWifiSearch.OnSearchClickListener;
import com.hwatong.settings.preference.SelectWifiPreference;
import com.hwatong.settings.widget.SwitchButton;
import com.hwatong.settings.wifi.p2p.WifiP2pSettings;

/**
 * Two types of UI are provided here.
 * 
 * The first is for "usual Settings", appearing as any other Setup fragment.
 * 
 * The second is for Setup Wizard, with a simplified interface that hides the
 * action bar and menus.
 */
public class WifiSettings extends SettingsPreferenceFragment implements
		OnClickListener, OnSharedPreferenceChangeListener,
		Preference.OnPreferenceClickListener {
	private static final String TAG = "WifiSettings";

	private static final String KEY_WIFI_CONNECTED = "wifi_connected";
	private static final String KEY_WIFI_SELECT = "wifi_select";
	private static final String KEY_WIFI_SWITCH = "wifi_switch";
	private static final String KEY_NETWORK_SELECT = "select_network";

	private static final int MENU_ID_WPS_PBC = Menu.FIRST;
	private static final int MENU_ID_WPS_PIN = Menu.FIRST + 1;
	private static final int MENU_ID_P2P = Menu.FIRST + 2;
	private static final int MENU_ID_ADD_NETWORK = Menu.FIRST + 3;
	private static final int MENU_ID_ADVANCED = Menu.FIRST + 4;
	private static final int MENU_ID_SCAN = Menu.FIRST + 5;
	private static final int MENU_ID_CONNECT = Menu.FIRST + 6;
	private static final int MENU_ID_FORGET = Menu.FIRST + 7;
	private static final int MENU_ID_MODIFY = Menu.FIRST + 8;

	private static final int WIFI_DIALOG_ID = 1;
	private static final int WPS_PBC_DIALOG_ID = 2;
	private static final int WPS_PIN_DIALOG_ID = 3;
	private static final int WIFI_SKIPPED_DIALOG_ID = 4;
	private static final int WIFI_AND_MOBILE_SKIPPED_DIALOG_ID = 5;

	// Combo scans can take 5-6s to complete - set to 10s.
	private static final int WIFI_RESCAN_INTERVAL_MS = 10 * 1000;

	// Instance state keys
	private static final String SAVE_DIALOG_EDIT_MODE = "edit_mode";
	private static final String SAVE_DIALOG_ACCESS_POINT_STATE = "wifi_ap_state";

	private IntentFilter mFilter;
	private BroadcastReceiver mReceiver;
	private Scanner mScanner;

	private WifiManager mWifiManager;
	private ConnectivityManager connectivityManager;
	private WifiManager.ActionListener mConnectListener;
	private WifiManager.ActionListener mSaveListener;
	private WifiManager.ActionListener mForgetListener;
	private boolean mP2pSupported;

	private WifiEnabler mWifiEnabler;
	// An access point being editted is stored here.
	private AccessPoint mSelectedAccessPoint;

	private DetailedState mLastState;
	private WifiInfo mLastInfo;

	private AtomicBoolean mConnected = new AtomicBoolean(false);

	private int mKeyStoreNetworkId = INVALID_NETWORK_ID;

	// private WifiDialog mDialog;
	private F70_WifiDialog2 mDialog;

	private TextView mEmptyView;

	/* Used in Wifi Setup context */

	// this boolean extra specifies whether to disable the Next button when not
	// connected
	private static final String EXTRA_ENABLE_NEXT_ON_CONNECT = "wifi_enable_next_on_connect";

	// this boolean extra specifies whether to auto finish when connection is
	// established
	private static final String EXTRA_AUTO_FINISH_ON_CONNECT = "wifi_auto_finish_on_connect";

	// this boolean extra shows a custom button that we can control
	protected static final String EXTRA_SHOW_CUSTOM_BUTTON = "wifi_show_custom_button";

	// show a text regarding data charges when wifi connection is required
	// during setup wizard
	protected static final String EXTRA_SHOW_WIFI_REQUIRED_INFO = "wifi_show_wifi_required_info";

	// this boolean extra is set if we are being invoked by the Setup Wizard
	private static final String EXTRA_IS_FIRST_RUN = "firstRun";

	// should Next button only be enabled when we have a connection?
	private boolean mEnableNextOnConnection;

	// should activity finish once we have a connection?
	private boolean mAutoFinishOnConnection;

	// Save the dialog details
	private boolean mDlgEdit;
	private AccessPoint mDlgAccessPoint;
	private Bundle mAccessPointSavedState;

	// add by lcb
	private int mFixedPreferenceCount;
	private MyWifiConnected mWifiConnected;
	private MyWifiSearch mWifiSearch;

	private SelectWifiPreference mSelectWifiPreference;
	private SelectWifiPreference.OnSelectWifiListener onSelectWifiListener;
	private int whichNetwork;
	// private MyWifiPreference mWifiSwitch;

	/* End of "used in Wifi Setup context" */

	public WifiSettings(
			SelectWifiPreference.OnSelectWifiListener onSelectWifiListener,
			int what) {
		mFilter = new IntentFilter();
		mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		mFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
		mFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		mFilter.addAction(WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION);
		mFilter.addAction(WifiManager.LINK_CONFIGURATION_CHANGED_ACTION);
		mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		mFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				handleEvent(context, intent);
			}
		};

		mScanner = new Scanner();
		this.onSelectWifiListener = onSelectWifiListener;
		this.whichNetwork = what;
	}

	public WifiSettings() {

	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

	}

	@Override
	public View onCreateView(final LayoutInflater inflater,
			ViewGroup container, Bundle savedInstanceState) {
		F70Application.isShowWifi = true;
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mP2pSupported = getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_WIFI_DIRECT);
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

		mConnectListener = new WifiManager.ActionListener() {
			public void onSuccess() {
			}

			public void onFailure(int reason) {
				Activity activity = getActivity();
				if (activity != null) {
					Toast.makeText(activity,
							R.string.wifi_failed_connect_message,
							Toast.LENGTH_SHORT).show();
				}
			}
		};

		mSaveListener = new WifiManager.ActionListener() {
			public void onSuccess() {
			}

			public void onFailure(int reason) {
				Activity activity = getActivity();
				if (activity != null) {
					Toast.makeText(activity, R.string.wifi_failed_save_message,
							Toast.LENGTH_SHORT).show();
				}
			}
		};

		mForgetListener = new WifiManager.ActionListener() {
			public void onSuccess() {
			}

			public void onFailure(int reason) {
				Activity activity = getActivity();
				if (activity != null) {
					Toast.makeText(activity,
							R.string.wifi_failed_forget_message,
							Toast.LENGTH_SHORT).show();
				}
			}
		};

		if (savedInstanceState != null
				&& savedInstanceState
						.containsKey(SAVE_DIALOG_ACCESS_POINT_STATE)) {
			mDlgEdit = savedInstanceState.getBoolean(SAVE_DIALOG_EDIT_MODE);
			mAccessPointSavedState = savedInstanceState
					.getBundle(SAVE_DIALOG_ACCESS_POINT_STATE);
		}

		final Activity activity = getActivity();
		final Intent intent = activity.getIntent();

		// first if we're supposed to finish once we have a connection
		mAutoFinishOnConnection = intent.getBooleanExtra(
				EXTRA_AUTO_FINISH_ON_CONNECT, false);

		if (mAutoFinishOnConnection) {
			// Hide the next button
			if (hasNextButton()) {
				getNextButton().setVisibility(View.GONE);
			}

			final ConnectivityManager connectivity = (ConnectivityManager) activity
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null
					&& connectivity.getNetworkInfo(
							ConnectivityManager.TYPE_WIFI).isConnected()) {
				activity.setResult(Activity.RESULT_OK);
				activity.finish();
				return;
			}
		}

		// if we're supposed to enable/disable the Next button based on our
		// current connection
		// state, start it off in the right state
		mEnableNextOnConnection = intent.getBooleanExtra(
				EXTRA_ENABLE_NEXT_ON_CONNECT, false);

		if (mEnableNextOnConnection) {
			if (hasNextButton()) {
				final ConnectivityManager connectivity = (ConnectivityManager) activity
						.getSystemService(Context.CONNECTIVITY_SERVICE);
				if (connectivity != null) {
					NetworkInfo info = connectivity
							.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
					changeNextButtonState(info.isConnected());
				}
			}
		}

		addPreferencesFromResource(R.xml.wifi_settings);
		mSelectWifiPreference = (SelectWifiPreference) findPreference(KEY_NETWORK_SELECT);
		mSelectWifiPreference.setOnSelectWifiListener(onSelectWifiListener);
		mSelectWifiPreference.update(whichNetwork);
		// mWifiSwitch = (MyWifiPreference) findPreference(KEY_WIFI_SWITCH);
		// mWifiSwitch.setOnPreferenceClickListener(this);

		mFixedPreferenceCount = getPreferenceScreen().getPreferenceCount();
		mEmptyView = (TextView) getView().findViewById(android.R.id.empty);
		getListView().setEmptyView(mEmptyView);
		setHasOptionsMenu(true);
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		if (mDialog != null) {
			removeDialog(WIFI_DIALOG_ID);
			mDialog = null;
		} 
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);

		if (mWifiEnabler != null) {
			mWifiEnabler.resume();
		}
		if(getActivity() != null)
			getActivity().registerReceiver(mReceiver, mFilter);
		if (mKeyStoreNetworkId != INVALID_NETWORK_ID
				&& KeyStore.getInstance().state() == KeyStore.State.UNLOCKED) {
			mWifiManager.connect(mKeyStoreNetworkId, mConnectListener);
		}
		mKeyStoreNetworkId = INVALID_NETWORK_ID;

		updateAccessPoints();
		changedActivityImage(this.getClass().getName());
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mWifiEnabler != null) {
			mWifiEnabler.pause();
		}
		getActivity().unregisterReceiver(mReceiver);
//		mScanner.pause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		final boolean wifiIsEnabled = mWifiManager.isWifiEnabled();
		menu.add(Menu.NONE, MENU_ID_WPS_PBC, 0, R.string.wifi_menu_wps_pbc)
				.setIcon(R.drawable.ic_wps).setEnabled(wifiIsEnabled)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		menu.add(Menu.NONE, MENU_ID_ADD_NETWORK, 0, R.string.wifi_add_network)
				.setIcon(R.drawable.ic_menu_add).setEnabled(wifiIsEnabled)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		menu.add(Menu.NONE, MENU_ID_SCAN, 0, R.string.wifi_menu_scan)
				// .setIcon(R.drawable.ic_menu_scan_network)
				.setEnabled(wifiIsEnabled)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		menu.add(Menu.NONE, MENU_ID_WPS_PIN, 0, R.string.wifi_menu_wps_pin)
				.setEnabled(wifiIsEnabled)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		if (mP2pSupported) {
			menu.add(Menu.NONE, MENU_ID_P2P, 0, R.string.wifi_menu_p2p)
					.setEnabled(wifiIsEnabled)
					.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		}
		menu.add(Menu.NONE, MENU_ID_ADVANCED, 0, R.string.wifi_menu_advanced)
		// .setIcon(android.R.drawable.ic_menu_manage)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// If the dialog is showing, save its state.
		// if (mDialog != null && mDialog.isShowing()) {
		// outState.putBoolean(SAVE_DIALOG_EDIT_MODE, mDlgEdit);
		// if (mDlgAccessPoint != null) {
		// mAccessPointSavedState = new Bundle();
		// mDlgAccessPoint.saveWifiState(mAccessPointSavedState);
		// outState.putBundle(SAVE_DIALOG_ACCESS_POINT_STATE,
		// mAccessPointSavedState);
		// }
		// }
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ID_WPS_PBC:
			showDialog(WPS_PBC_DIALOG_ID);
			return true;
		case MENU_ID_P2P:
			if (getActivity() instanceof PreferenceActivity) {
				((PreferenceActivity) getActivity()).startPreferencePanel(
						WifiP2pSettings.class.getCanonicalName(), null,
						R.string.wifi_p2p_settings_title, null, this, 0);
			} else {
				startFragment(this, WifiP2pSettings.class.getCanonicalName(),
						-1, null);
			}
			return true;
		case MENU_ID_WPS_PIN:
			showDialog(WPS_PIN_DIALOG_ID);
			return true;
		case MENU_ID_SCAN:
			if (mWifiManager.isWifiEnabled()) {
				mScanner.forceScan();
			}
			return true;
		case MENU_ID_ADD_NETWORK:
			if (mWifiManager.isWifiEnabled()) {
				onAddNetworkPressed();
			}
			return true;
		case MENU_ID_ADVANCED:
			if (getActivity() instanceof PreferenceActivity) {
				((PreferenceActivity) getActivity()).startPreferencePanel(
						AdvancedWifiSettings.class.getCanonicalName(), null,
						R.string.wifi_advanced_titlebar, null, this, 0);
			} else {
				startFragment(this,
						AdvancedWifiSettings.class.getCanonicalName(), -1, null);
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo info) {
		if (info instanceof AdapterContextMenuInfo) {
			Preference preference = (Preference) getListView()
					.getItemAtPosition(((AdapterContextMenuInfo) info).position);

			if (preference instanceof AccessPoint) {
				mSelectedAccessPoint = (AccessPoint) preference;
				menu.setHeaderTitle(mSelectedAccessPoint.ssid);
				if (mSelectedAccessPoint.getLevel() != -1
						&& mSelectedAccessPoint.getState() == null) {
					menu.add(Menu.NONE, MENU_ID_CONNECT, 0,
							R.string.wifi_menu_connect);
				}
				if (mSelectedAccessPoint.networkId != INVALID_NETWORK_ID) {
					menu.add(Menu.NONE, MENU_ID_FORGET, 0,
							R.string.wifi_menu_forget);
					menu.add(Menu.NONE, MENU_ID_MODIFY, 0,
							R.string.wifi_menu_modify);
				}
			}
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (mSelectedAccessPoint == null) {
			return super.onContextItemSelected(item);
		}
		switch (item.getItemId()) {
		case MENU_ID_CONNECT: {
			if (mSelectedAccessPoint.networkId != INVALID_NETWORK_ID) {
				if (!requireKeyStore(mSelectedAccessPoint.getConfig())) {
					mWifiManager.connect(mSelectedAccessPoint.networkId,
							mConnectListener);
				}
			} else if (mSelectedAccessPoint.security == AccessPoint.SECURITY_NONE) {
				/** Bypass dialog for unsecured networks */
				mSelectedAccessPoint.generateOpenNetworkConfig();
				mWifiManager.connect(mSelectedAccessPoint.getConfig(),
						mConnectListener);
			} else {
				showDialog(mSelectedAccessPoint, true);
			}
			return true;
		}
		case MENU_ID_FORGET: {
			mWifiManager
					.forget(mSelectedAccessPoint.networkId, mForgetListener);
			return true;
		}
		case MENU_ID_MODIFY: {
			showDialog(mSelectedAccessPoint, true);
			return true;
		}
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen screen,
			Preference preference) {
		if (preference instanceof AccessPoint) {
			mSelectedAccessPoint = (AccessPoint) preference;
			/** Bypass dialog for unsecured, unsaved networks */
			if (mSelectedAccessPoint.security == AccessPoint.SECURITY_NONE
					&& mSelectedAccessPoint.networkId == INVALID_NETWORK_ID) {
				mSelectedAccessPoint.generateOpenNetworkConfig();
				mWifiManager.connect(mSelectedAccessPoint.getConfig(),
						mConnectListener);
			} else {
				showDialog(mSelectedAccessPoint, false);
			}
		} else if (preference instanceof MyWifiConnected
				&& mWifiConnected.getAccessPoint() != null) {
			mSelectedAccessPoint = (AccessPoint) mWifiConnected
					.getAccessPoint();
			showDialog(mSelectedAccessPoint, false);
		} else {
			return super.onPreferenceTreeClick(screen, preference);
		}
		return true;
	}

	private void showDialog(AccessPoint accessPoint, boolean edit) {
		if (mDialog != null) {
			removeDialog(WIFI_DIALOG_ID);
			mDialog = null;
		}

		// Save the access point and edit mode
		mDlgAccessPoint = accessPoint;
		mDlgEdit = edit;

		showDialog(WIFI_DIALOG_ID);
	}

	@Override
	public Dialog onCreateDialog(int dialogId) {
		switch (dialogId) {
		case WIFI_DIALOG_ID:
			AccessPoint ap = mDlgAccessPoint; // For manual launch
			if (ap == null) { // For re-launch from saved state
				if (mAccessPointSavedState != null) {
					ap = new AccessPoint(getActivity(), mAccessPointSavedState);
					// For repeated orientation changes
					mDlgAccessPoint = ap;
				}
			}
			// If it's still null, fine, it's for Add Network
			mSelectedAccessPoint = ap;
			mDialog = new F70_WifiDialog2(getActivity(), this, ap, mDlgEdit);
			return mDialog;
		case WPS_PBC_DIALOG_ID:
			return new WpsDialog(getActivity(), WpsInfo.PBC);
		case WPS_PIN_DIALOG_ID:
			return new WpsDialog(getActivity(), WpsInfo.DISPLAY);
		case WIFI_SKIPPED_DIALOG_ID:
			return new AlertDialog.Builder(getActivity())
					.setMessage(R.string.wifi_skipped_message)
					.setCancelable(false)
					.setNegativeButton(R.string.wifi_skip_anyway,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									getActivity().setResult(
											Activity.RESULT_CANCELED);
									getActivity().finish();
								}
							})
					.setPositiveButton(R.string.wifi_dont_skip,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
								}
							}).create();
		case WIFI_AND_MOBILE_SKIPPED_DIALOG_ID:
			return new AlertDialog.Builder(getActivity())
					.setMessage(R.string.wifi_and_mobile_skipped_message)
					.setCancelable(false)
					.setNegativeButton(R.string.wifi_skip_anyway,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									getActivity().setResult(
											Activity.RESULT_CANCELED);
									getActivity().finish();
								}
							})
					.setPositiveButton(R.string.wifi_dont_skip,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
								}
							}).create();

		}
		return super.onCreateDialog(dialogId);
	}

	private void setWifiSelectInfoVisible() {
		// mWifiConnected = (MyWifiConnected)findPreference(KEY_WIFI_CONNECTED);
		// LJW 2017.9.8 remove connected Info ah...
		// getPreferenceScreen().removePreference(mWifiConnected);
		// mWifiSearch = (MyWifiSearch)findPreference(KEY_WIFI_SELECT);
		if (mWifiConnected == null && mWifiSearch == null) {
			mWifiConnected = new MyWifiConnected(getActivity());
			mWifiSearch = new MyWifiSearch(getActivity());

			mWifiConnected.setTitle(R.string.wifi_connected);
			mWifiConnected
					.setWidgetLayoutResource(R.layout.preference_widget_wifi_signal);

			mWifiSearch.setTitle(R.string.wifi_select);
		}

		getPreferenceScreen().addPreference(mWifiConnected);
		getPreferenceScreen().addPreference(mWifiSearch);
	}

	private void setWifiSelectInfoInVisible() {
		if (mWifiConnected != null && mWifiSearch != null) {
			getPreferenceScreen().removePreference(mWifiConnected);
			getPreferenceScreen().removePreference(mWifiSearch);

			mWifiConnected = null;
			mWifiSearch = null;
		}
	}

	private boolean isPhone() {
		final TelephonyManager telephonyManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager != null
				&& telephonyManager.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE;
	}

	/**
	 * Return true if there's any SIM related impediment to connectivity. Treats
	 * Unknown as OK. (Only returns true if we're sure of a SIM problem.)
	 */
	protected boolean hasSimProblem() {
		final TelephonyManager telephonyManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager != null
				&& telephonyManager.getCurrentPhoneType() == TelephonyManager.PHONE_TYPE_GSM
				&& telephonyManager.getSimState() != TelephonyManager.SIM_STATE_READY
				&& telephonyManager.getSimState() != TelephonyManager.SIM_STATE_UNKNOWN;
	}

	private boolean requireKeyStore(WifiConfiguration config) {
		if (WifiConfigController.requireKeyStore(config)
				&& KeyStore.getInstance().state() != KeyStore.State.UNLOCKED) {
			mKeyStoreNetworkId = config.networkId;
			Credentials.getInstance().unlock(getActivity());
			return true;
		}
		return false;
	}

	/**
	 * Shows the latest access points available with supplimental information
	 * like the strength of network and the security for it.
	 */
	private void updateAccessPoints() {
		// Safeguard from some delayed event handling
		if (getActivity() == null)
			return;

		final int wifiState = mWifiManager.getWifiState();
		Log.d(TAG, "updateAccessPoints wifi " + wifiState);

		switch (wifiState) {
		case WifiManager.WIFI_STATE_ENABLED:
			// AccessPoints are automatically sorted with TreeSet.
			final Collection<AccessPoint> accessPoints = constructAccessPoints();
			for (AccessPoint accessPoint : accessPoints)
				LogUtils.d("return All List: " + accessPoint.getSSID() + ", "
						+ accessPoint.getLevel());
			// getPreferenceScreen().removeAll();
			removeAll();
			if (accessPoints.size() == 0) {
				addMessagePreference(R.string.wifi_empty_list_wifi_on);
			}

			// 在刷新wifi列表之前先加载已连接设备和选择网络的框
			setWifiSelectInfoVisible();

			for (AccessPoint accessPoint : accessPoints) {
				LogUtils.d("accessPoints level: " + accessPoint.getLevel()
						+ "accessPoints name: " + accessPoint.ssid);
				if (accessPoint.getLevel() != -1) {
					if (mWifiConnected.getAccessPoint() == null
							|| !accessPoint.getSSID().equals(
									mWifiConnected.getAccessPoint().getSSID())) {
						// Log.d(TAG,
						// "updateAccessPoints mWifiConnected.getAccessPoint()="+mWifiConnected.getAccessPoint()+
						// ", accessPoint="+accessPoint);
						getPreferenceScreen().addPreference(accessPoint);
					} else if (mWifiConnected.getAccessPoint() != null
							|| accessPoint.getSSID().equals(
									mWifiConnected.getAccessPoint().getSSID())) {
						mWifiConnected.update(accessPoint);
					}
				}
			}
			break;

		case WifiManager.WIFI_STATE_ENABLING:
			removeAll();
			// if(mWifiSearch != null) {
			//
			// mWifiSearch.setSearching(true);
			// mWifiSearch.setEnabled(true);
			// }
			// getPreferenceScreen().removeAll();
			break;

		case WifiManager.WIFI_STATE_DISABLING:
			addMessagePreference(R.string.wifi_stopping);
			// if(mWifiSearch != null) {
			//
			// mWifiSearch.setEnabled(false);
			// mWifiSearch.setSearching(false);
			// }
			break;

		case WifiManager.WIFI_STATE_DISABLED:
			LogUtils.d("mWifiConnected.update: WIFI_STATE_DISABLED");
			if (mWifiConnected != null) {

				mWifiConnected.update(null, null, null);
				// mWifiSearch.setSearching(false);
				// mWifiSearch.setEnabled(false);
			}
			addMessagePreference(R.string.wifi_empty_list_wifi_off);
			// setWifiSelectInfoInVisible();
			break;
		}
	}

	private void addMessagePreference(int messageId) {
		removeAll();
		// if (mEmptyView != null) mEmptyView.setText(messageId);
		// getPreferenceScreen().removeAll();
	}

	/** Returns sorted list of access points */
	private List<AccessPoint> constructAccessPoints() {
		ArrayList<AccessPoint> accessPoints = new ArrayList<AccessPoint>();
		/**
		 * Lookup table to more quickly update AccessPoints by only considering
		 * objects with the correct SSID. Maps SSID -> List of AccessPoints with
		 * the given SSID.
		 */
		Multimap<String, AccessPoint> apMap = new Multimap<String, AccessPoint>();

		final List<WifiConfiguration> configs = mWifiManager
				.getConfiguredNetworks();
		final List<ScanResult> results = mWifiManager.getScanResults();
		for (ScanResult result : results)
			LogUtils.d("constructAccessPoints name ssssssssss :" + result.SSID
					+ ", level: " + result.level);
		if (configs != null) {
			for (WifiConfiguration config : configs) {
				LogUtils.d("get WifiConfiguration: " + config.toString());
				AccessPoint accessPoint = new AccessPoint(getActivity(), config);
				accessPoint
						.setLayoutResource(R.layout.preference_wifi_accesspoint);
				accessPoint.update(mLastInfo, mLastState);
				accessPoints.add(accessPoint);
				apMap.put(accessPoint.ssid, accessPoint);
			}
		}
		// for(AccessPoint accessPoint : accessPoints)
		// LogUtils.d("constructAccessPoints configs List: " +
		// accessPoint.getSSID() + ", " + accessPoint.getLevel());

		if (results != null) {
			for (ScanResult result : results) {
				// Ignore hidden and ad-hoc networks.
				if (result.SSID == null || result.SSID.length() == 0
						|| result.capabilities.contains("[IBSS]")) {
					continue;
				}
				boolean found = false;
				for (AccessPoint accessPoint : apMap.getAll(result.SSID)) {
					if (accessPoint.update(result))
						found = true;
				}
				if (!found) {
					AccessPoint accessPoint = new AccessPoint(getActivity(),
							result);
					accessPoint
							.setLayoutResource(R.layout.preference_wifi_accesspoint);
					accessPoint.update(result);
					accessPoints.add(accessPoint);
					apMap.put(accessPoint.ssid, accessPoint);
				}
			}
		}

		// Pre-sort accessPoints to speed preference insertion
		Collections.sort(accessPoints);
		for (AccessPoint accessPoint : accessPoints)
			LogUtils.d("constructAccessPoints All List: "
					+ accessPoint.getSSID() + ", " + accessPoint.getLevel());
		return accessPoints;
	}

	/** A restricted multimap for use in constructAccessPoints */
	private class Multimap<K, V> {
		private HashMap<K, List<V>> store = new HashMap<K, List<V>>();

		/** retrieve a non-null list of values with key K */
		List<V> getAll(K key) {
			List<V> values = store.get(key);
			return values != null ? values : Collections.<V> emptyList();
		}

		void put(K key, V val) {
			List<V> curVals = store.get(key);
			if (curVals == null) {
				curVals = new ArrayList<V>(3);
				store.put(key, curVals);
			}
			curVals.add(val);
		}
	}

	private void handleEvent(Context context, Intent intent) {
		String action = intent.getAction();
		if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
			updateWifiState(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
					WifiManager.WIFI_STATE_UNKNOWN));
		} else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)
				|| WifiManager.CONFIGURED_NETWORKS_CHANGED_ACTION
						.equals(action)
				|| WifiManager.LINK_CONFIGURATION_CHANGED_ACTION.equals(action)) {
			updateAccessPoints();
			LogUtils.d("wifi tmp: LINK_CONFIGURATION_CHANGED_ACTION");
			// 鎵弿瀹屾垚
			if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
				mWifiSearch.setSearching(false);
			}
			SupplicantState state = (SupplicantState) intent
					.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
			updateConnectionState(WifiInfo.getDetailedStateOf(state),
					"SUPPLICANT_STATE_CHANGED_ACTION");
		} else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
			// Ignore supplicant state changes when network is connected
			// TODO: we should deprecate SUPPLICANT_STATE_CHANGED_ACTION and
			// introduce a broadcast that combines the supplicant and network
			// network state change events so the apps dont have to worry about
			// ignoring supplicant state change when network is connected
			// to get more fine grained information.
			int linkWifiResult = intent.getIntExtra(
					WifiManager.EXTRA_SUPPLICANT_ERROR, 123);
			if (linkWifiResult == WifiManager.ERROR_AUTHENTICATING) {
//				WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
				NetworkInfo info = connectivityManager.getActiveNetworkInfo();
				if (info == null || info.getType() != ConnectivityManager.TYPE_WIFI) {
					toastWifiPwdError();
				}
				
			}
			SupplicantState state = (SupplicantState) intent
					.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
			if (!mConnected.get() && SupplicantState.isHandshakeState(state)) {
				updateConnectionState(WifiInfo.getDetailedStateOf(state),
						"SUPPLICANT_STATE_CHANGED_ACTION");
			}
		} else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
			NetworkInfo info = (NetworkInfo) intent
					.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			mConnected.set(info.isConnected());
			changeNextButtonState(info.isConnected());
			updateAccessPoints();
			updateConnectionState(info.getDetailedState(),
					"NETWORK_STATE_CHANGED_ACTION");
			if (mAutoFinishOnConnection && info.isConnected()) {
				Activity activity = getActivity();
				if (activity != null) {
					activity.setResult(Activity.RESULT_OK);
					activity.finish();
				}
				return;
			}
		} else if (WifiManager.RSSI_CHANGED_ACTION.equals(action)) {
			updateConnectionState(null, "RSSI_CHANGED_ACTION");
			Log.d("tmpwifi", "RSSI_CHANGED_ACTION");
		} else if (WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION
				.equals(action)) {
			// Log.d("tmpwifi", "SUPPLICANT_CONNECTION_CHANGE_ACTION");
		} else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
			LogUtils.d("ConnectivityManager.CONNECTIVITY_ACTION");
//			portalWifi();
		}

	}

	private void toastWifiPwdError() {
		Activity activity = getActivity();
		if (activity != null) {
	        final DisplayMetrics dm = new DisplayMetrics();
	        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);

	        final int desiredMinimumWidth = dm.widthPixels; //屏幕宽度
	        final int desiredMinimumHeight = dm.heightPixels; //屏幕高度
	        
			Toast toast = Toast.makeText(getActivity(),
					R.string.wifi_pswd_error, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.LEFT, desiredMinimumWidth / 3, desiredMinimumHeight * 3 / 10 );
			toast.setDuration(3000);
			toast.show();
		}
	}

	private void updateConnectionState(DetailedState state, String method) {
		Log.d(TAG, "updateConnectionState state=" + state);
		/* sticky broadcasts can call this when wifi is disabled */
		if (!mWifiManager.isWifiEnabled()) {
			mScanner.pause();
			return;
		}

		if (state == DetailedState.OBTAINING_IPADDR) {
			mScanner.pause();
		} else {
			mScanner.resume();
		}

		// if (state == DetailedState.SCANNING) {
		// mSelectPreference.setSummary(R.string.progress_scanning);
		// View v = mSelectPreference.findWidgetById(R.id.search);
		// if (v!=null) v.setVisibility(View.GONE);
		// }else {
		// mSelectPreference.setSummary("");
		// View v = mSelectPreference.findWidgetById(R.id.search);
		// if (v!=null) v.setVisibility(View.VISIBLE);
		// }
		mLastInfo = mWifiManager.getConnectionInfo();
		final List<ScanResult> results = mWifiManager.getScanResults();
		if (state != null) {
			mLastState = state;
		}
		if (mLastInfo == null
				|| (mLastInfo != null && mLastInfo.getBSSID() != null && mLastInfo
						.getBSSID().equals("00:00:00:00:00:00")))
			mWifiConnected.update(null, null, null);

		boolean find = false;
		for (int i = getPreferenceScreen().getPreferenceCount() - 1; i >= mFixedPreferenceCount; --i) {
			// Maybe there's a WifiConfigPreference
			Preference preference = getPreferenceScreen().getPreference(i);
			if (preference instanceof AccessPoint) {
				final AccessPoint accessPoint = (AccessPoint) preference;

				// //解决已连接设备从0升到2
				if (results != null) {
					for (ScanResult result : results) {
						if (mLastInfo != null
								&& result.BSSID.equals(mLastInfo.getBSSID())
								&& mLastInfo.getRssi() != result.level) {
							LogUtils.d("current wifi name： "
									+ " updateConnectionState level :"
									+ result.level);
							LogUtils.d("current updateConnectionState wifi name："
									+ result.SSID
									+ ", level: "
									+ WifiManager.calculateSignalLevel(
											result.level, 4));
							try {
								Field field = mLastInfo.getClass()
										.getDeclaredField("mRssi");
								field.setAccessible(true);
								try {
									field.set(mLastInfo, result.level);
								} catch (IllegalArgumentException e) {
									e.printStackTrace();
								} catch (IllegalAccessException e) {
									e.printStackTrace();
								}
							} catch (NoSuchFieldException e) {
								e.printStackTrace();
							}
							break;
						}
					}
				}
				LogUtils.d("mLastInfo: " + mLastInfo.getRssi());
				accessPoint.update(mLastInfo, mLastState);
				find = mWifiConnected
						.update(accessPoint, mLastInfo, mLastState);
			}
		}
		if (mWifiConnected.getAccessPoint() != null) {
			if (find) {
				getPreferenceScreen().removePreference(
						mWifiConnected.getAccessPoint());
			} else {
				Log.d(TAG,
						"updateConnectionState mWifiConnected.update finded:"
								+ mWifiConnected.getAccessPoint().getLevel());
				// mWifiConnected.update(mWifiConnected.getAccessPoint(),
				// mLastInfo, mLastState);
			}
		}
	}

	private String removeDoubleQuotes(String string) {
		int length = string.length();
		if ((length > 1) && (string.charAt(0) == '"')
				&& (string.charAt(length - 1) == '"')) {
			return string.substring(1, length - 1);
		}
		return string;
	}

//	private void portalWifi() {
//		CheckWifiLoginTask
//				.checkWifi(new CheckWifiLoginTask.ICheckWifiCallBack() {
//					@Override
//					public void portalNetWork(boolean isLogin) {
//						if (!isLogin) {
//							LogUtils.d("CONNECTIVITY_ACTION changed is : " + false);
//						} else {
//							// TODO...
//							LogUtils.d("CONNECTIVITY_ACTION changed is : " + true);
//						}
//					}
//				});
//	}

	private void updateWifiState(int state) {
		Log.d(TAG, "updateWifiState wifi " + state);
		Activity activity = getActivity();
		if (activity != null) {
			activity.invalidateOptionsMenu();
		}

		switch (state) {
		case WifiManager.WIFI_STATE_ENABLED:
			mScanner.resume();
			return; // not break, to avoid the call to pause() below

		case WifiManager.WIFI_STATE_ENABLING:
			// mWifiSearch.setSearching(true);
			// mWifiSearch.setEnabled(true);
			addMessagePreference(R.string.wifi_starting);
			break;

		case WifiManager.WIFI_STATE_DISABLED:
			LogUtils.d("mWifiConnected.update WIFI_STATE_DISABLED");
			if (mWifiConnected != null) {

				mWifiConnected.update(null, null, null);
				// mWifiSearch.setSearching(false);
				// mWifiSearch.setEnabled(false);
			}
			addMessagePreference(R.string.wifi_empty_list_wifi_off);
			// setWifiSelectInfoInVisible();
			break;
		}

		mLastInfo = null;
		mLastState = null;
		mScanner.pause();
	}

	private class Scanner extends Handler {
		private int mRetry = 0;

		void resume() {
			if (!hasMessages(0)) {
				sendEmptyMessage(0);
			}
		}

		void forceScan() {
			removeMessages(0);
			sendEmptyMessage(0);
		}

		void pause() {
			mRetry = 0;
			removeMessages(0);
		}

		@Override
		public void handleMessage(Message message) {
			if (mWifiManager.startScanActive()) {
				mRetry = 0;
			} else if (++mRetry >= 3) {
				mRetry = 0;
				Activity activity = getActivity();
				if (activity != null) {
					Toast.makeText(activity, R.string.wifi_fail_to_scan,
							Toast.LENGTH_LONG).show();
				}
				return;
			}
			sendEmptyMessageDelayed(0, WIFI_RESCAN_INTERVAL_MS);
		}
	}

	/**
	 * Renames/replaces "Next" button when appropriate. "Next" button usually
	 * exists in Wifi setup screens, not in usual wifi settings screen.
	 * 
	 * @param connected
	 *            true when the device is connected to a wifi network.
	 */
	private void changeNextButtonState(boolean connected) {
		if (mEnableNextOnConnection && hasNextButton()) {
			getNextButton().setEnabled(connected);
		}
	}

	/**
	 * Refreshes acccess points and ask Wifi module to scan networks again.
	 */
	/* package */void refreshAccessPoints() {
		if (mWifiManager.isWifiEnabled()) {
			mScanner.resume();
		}

		// getPreferenceScreen().removeAll();
		removeAll();
	}

	/**
	 * Called when "add network" button is pressed.
	 */
	/* package */void onAddNetworkPressed() {
		// No exact access point is selected.
		mSelectedAccessPoint = null;
		showDialog(null, true);
	}

	/* package */int getAccessPointsCount() {
		final boolean wifiIsEnabled = mWifiManager.isWifiEnabled();
		if (wifiIsEnabled) {
			return getPreferenceScreen().getPreferenceCount()
					- mFixedPreferenceCount;
		} else {
			return 0;
		}
	}

	/**
	 * Requests wifi module to pause wifi scan. May be ignored when the module
	 * is disabled.
	 */
	/* package */void pauseWifiScan() {
		if (mWifiManager.isWifiEnabled()) {
			mScanner.pause();
		}
	}

	/**
	 * Requests wifi module to resume wifi scan. May be ignored when the module
	 * is disabled.
	 */
	/* package */void resumeWifiScan() {
		if (mWifiManager.isWifiEnabled()) {
			mScanner.resume();
		}
	}

	@Override
	protected int getHelpResource() {
		return R.string.help_url_wifi;
	}

	/**
	 * Used as the outer frame of all setup wizard pages that need to adjust
	 * their margins based on the total size of the available display. (e.g.
	 * side margins set to 10% of total width.)
	 */
	public static class ProportionalOuterFrame extends RelativeLayout {
		public ProportionalOuterFrame(Context context) {
			super(context);
		}

		public ProportionalOuterFrame(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		public ProportionalOuterFrame(Context context, AttributeSet attrs,
				int defStyle) {
			super(context, attrs, defStyle);
		}

		/**
		 * Set our margins and title area height proportionally to the available
		 * display size
		 */
		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
			int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
			final Resources resources = getContext().getResources();
			float titleHeight = resources.getFraction(
					R.dimen.setup_title_height, 1, 1);
			float sideMargin = resources.getFraction(
					R.dimen.setup_border_width, 1, 1);
			int bottom = resources
					.getDimensionPixelSize(R.dimen.setup_margin_bottom);
			setPadding((int) (parentWidth * sideMargin), 0,
					(int) (parentWidth * sideMargin), bottom);
			View title = findViewById(R.id.title_area);
			if (title != null) {
				title.setMinimumHeight((int) (parentHeight * titleHeight));
			}
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.button3 && mSelectedAccessPoint != null) {
			forget();
		} else if (v.getId() == R.id.button1) {
			submit(mDialog.getController());
		}
		mDialog.dismiss();
	}

	/* package */void submit(WifiConfigController configController) {

		final WifiConfiguration config = configController.getConfig();

		if (config == null) {
			if (mSelectedAccessPoint != null
					&& !requireKeyStore(mSelectedAccessPoint.getConfig())
					&& mSelectedAccessPoint.networkId != INVALID_NETWORK_ID) {
				mWifiManager.connect(mSelectedAccessPoint.networkId,
						mConnectListener);
			}
		} else if (config.networkId != INVALID_NETWORK_ID) {
			if (mSelectedAccessPoint != null) {
				mWifiManager.save(config, mSaveListener);
			}
		} else {
			if (configController.isEdit() || requireKeyStore(config)) {
				mWifiManager.save(config, mSaveListener);
			} else {
				mWifiManager.connect(config, mConnectListener);
			}
		}

		if (mWifiManager.isWifiEnabled()) {
			mScanner.resume();
		}
		updateAccessPoints();
	}

	/* package */void forget() {
		if (mSelectedAccessPoint.networkId == INVALID_NETWORK_ID) {
			// Should not happen, but a monkey seems to triger it
			Log.e(TAG, "Failed to forget invalid network "
					+ mSelectedAccessPoint.getConfig());
			return;
		}

		mWifiManager.forget(mSelectedAccessPoint.networkId, mForgetListener);

		if (mWifiManager.isWifiEnabled()) {
			mScanner.resume();
		}
		// updateAccessPoints();

		// We need to rename/replace "Next" button in wifi setup context.
		changeNextButtonState(false);
	}

	// add by lcb
	private void removeAll() {
		PreferenceScreen ps = getPreferenceScreen();
		if (ps != null) {
			synchronized (ps) {
				for (int i = ps.getPreferenceCount() - 1; i >= mFixedPreferenceCount; i--) {
					ps.removePreference(ps.getPreference(mFixedPreferenceCount));
				}
			}
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences preferences,
			String key) {
		Log.d(TAG, "Key=" + key);
		// if (key.equals(KEY_DATE_FORMAT)) {

	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (KEY_WIFI_SWITCH.equals(preference.getKey())) {
			return true;
		}
		return false;
	}

}
