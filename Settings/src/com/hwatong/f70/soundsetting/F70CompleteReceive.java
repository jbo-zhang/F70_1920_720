package com.hwatong.f70.soundsetting;

import java.util.List;

import com.hwatong.f70.bluetooth.BaseBluetoothSettingActivity;
import com.hwatong.f70.carsetting.F70CarSettingCommand;
import com.hwatong.f70.main.ConfigrationVersion;
import com.hwatong.f70.main.LogUtils;
import com.hwatong.f70.main.SettingBackUpDialog;
import com.hwatong.providers.carsettings.SettingsProvider;
import com.hwatong.settings.Utils;

import android.R.bool;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

public class F70CompleteReceive extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {

			LogUtils.d("F70CompleteReceive");
			int currentBass = EffectUtils.getCurrentBass();
			int currentMid = EffectUtils.getCurrentMid();
			int currentTreble = EffectUtils.getCurrentTreble();
			int currentBalance = EffectUtils.getCurrentBalance();
			int currentFader = EffectUtils.getCurrentFader();
//			String currentEq = EffectUtils.getCurrentEqAnother();
			String currentEq = EffectUtils.getCarSettingsString(context
					.getContentResolver(), EffectUtils.EQUALIZER_MODE);
			
//			int currentLoudness = EffectUtils.getCurrentLoudness(context);
			int currentLoudness = Integer.parseInt(Utils.getCarSettingsString(
							context.getContentResolver(),
							SettingsProvider.LOUDNESS_ENABLED));
			
			EffectUtils.setBassValue(currentBass);
			EffectUtils.setMidValue(currentMid);
			EffectUtils.setTrebleValue(currentTreble);
			EffectUtils.setBalanceValue(currentBalance);
			EffectUtils.setFaderValue(currentFader);
//			EffectUtils.setEqAnother(currentEq);
			EffectUtils.setLoudnessValue(currentLoudness);
			
			if(TextUtils.isEmpty(currentEq)) { //first boot
				EffectUtils.putCarSettingsString(context.getContentResolver(),
						EffectUtils.EQUALIZER_MODE, EffectUtils.POP);
				LogUtils.d("first set eq: ");
				EffectUtils.setEqAnother(EffectUtils.FLAT);
			} else {
				if(currentEq.equals(EffectUtils.CUSTOMER)) {
					LogUtils.d("CUSTOMER set eq: ");
					EffectUtils.setEqCustomer(context);
				} else {
					LogUtils.d("other set eq: ");
					EffectUtils.setEqAnother(currentEq);
				}
			}
			
			LogUtils.d("BOOT_COMPLETED set value: "
					+ EffectUtils.getBassValue() + ", "
					+ EffectUtils.getMidValue() + ", "
					+ EffectUtils.getTrebleValue() + ", "
					+ EffectUtils.getBalanceValue() + ", "
					+ EffectUtils.getFaderValue() + ", " + currentEq);
			
			LogUtils.d("F70.intent.action.BOOT_COMPLETED");
			LogUtils.d("BOOT_COMPLETED set value: " + currentBass + ", "
					+ currentMid + ", " + currentTreble + ", " + currentBalance
					+ ", " + currentFader + ", loudness: " + currentLoudness);
			LogUtils.d("get set eq value: " + EffectUtils.getFileBand1() + ", " + EffectUtils.getFileBand2() + ", " + EffectUtils.getFileBand3()
					 + ", " + EffectUtils.getFileBand4() + ", " + EffectUtils.getFileBand5());
			
//			if(TextUtils.isEmpty(EffectUtils.getCarSettingsString(context
//					.getContentResolver(), EffectUtils.EQUALIZER_MODE)))
//				EffectUtils.putCarSettingsString(context.getContentResolver(),
//						EffectUtils.EQUALIZER_MODE, EffectUtils.CUSTOMER);
			
			int apState = EffectUtils.getMemoryApState(context);
			LogUtils.d("current ap_State: " + apState);
			
			if(apState == 1) {
				int wifiState = wifiManager.getWifiState();
				if(wifiState == WifiManager.WIFI_STATE_ENABLED || wifiState == WifiManager.WIFI_STATE_ENABLING)
					wifiManager.setWifiEnabled(false);
				LogUtils.d("BOOT_COMPLETED open ap");
				wifiManager.setWifiApEnabled(null, true);
			}
		
		} else if(intent.getAction().equals("F70.intent.action.BOOT_COMPLETED")) {
			
		} else if(intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
			LogUtils.d("ACTION_AUDIO_BECOMING_NOISY");
		}else if (intent.getAction().equals("com.hwatong.voice.SEARCH_BT")) {
			final String btActivityName = "com.hwatong.f70.bluetooth.BaseBluetoothSettingActivity";
			boolean isBackGround = com.hwatong.f70.main.Utils
					.isActivityBackGround(context, "com.hwatong.settings",
							btActivityName);
			LogUtils.d("is btActivity running: " + isBackGround);
			if (isBackGround) {
				Intent intent1 = new Intent(context,
						BaseBluetoothSettingActivity.class);
				intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent1.putExtra("start_type", "search");
				context.startActivity(intent1);
			}
		} else if (intent.getAction().equals("com.ljw.testwifi")) {
		}
	}

	// private void getWifiLevel(Context context) {
	// WifiManager manager = (WifiManager)
	// context.getSystemService(Context.WIFI_SERVICE);
	// ConnectivityManager mConnectivityManager = (ConnectivityManager) context
	// .getSystemService(Context.CONNECTIVITY_SERVICE);
	//
	// NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
	// if (info == null || info.getType() != ConnectivityManager.TYPE_WIFI) {
	// LogUtils.d("no wifi");
	// Toast.makeText(context, "no wifi", Toast.LENGTH_SHORT).show();
	// return;
	// }
	// final List<ScanResult> results = manager.getScanResults();
	// WifiInfo wifiInfo = manager.getConnectionInfo();
	// if(results != null) {
	// for(ScanResult result : results) {
	// if(result.SSID.equals(removeDoubleQuotes(wifiInfo.getSSID()))) {
	// LogUtils.d("current wifi " + result.SSID + ", level: " +
	// result.level);
	// Toast.makeText(context, "current wifi " + result.SSID + ", level: "
	// + WifiManager.calculateSignalLevel(result.level, 3),
	// Toast.LENGTH_SHORT).show();
	// }
	// }
	// } else {
	// LogUtils.d("results is null");
	// Toast.makeText(context, "results is null", Toast.LENGTH_SHORT).show();
	// }
	// }
	//
	// private String removeDoubleQuotes(String string) {
	// int length = string.length();
	// if ((length > 1) && (string.charAt(0) == '"')
	// && (string.charAt(length - 1) == '"')) {
	// return string.substring(1, length - 1);
	// }
	// return string;
	// }
}
