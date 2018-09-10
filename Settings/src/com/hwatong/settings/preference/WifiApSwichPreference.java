package com.hwatong.settings.preference;


import com.hwatong.f70.main.LogUtils;
import com.hwatong.f70.soundsetting.EffectUtils;
import com.hwatong.settings.R;
import com.hwatong.settings.widget.SwitchButton;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.effect.Effect;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.preference.Preference;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;

public class WifiApSwichPreference extends Preference implements CompoundButton.OnCheckedChangeListener{
	private SwitchButton mSwitch;
	private IntentFilter mIntentFilter;
	private WifiManager wifiManager;
	private boolean mStateMachineEvent;
	private Context mContext;
	
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.WIFI_AP_STATE_CHANGED_ACTION.equals(action)) {
            	LogUtils.d("onReceive WIFI_AP_STATE_CHANGED_ACTION: " + intent.getIntExtra(
						WifiManager.EXTRA_WIFI_AP_STATE,
						WifiManager.WIFI_AP_STATE_FAILED));
                handleWifiApStateChanged(intent.getIntExtra(
						WifiManager.EXTRA_WIFI_AP_STATE,
						WifiManager.WIFI_AP_STATE_FAILED));
            } else if (ConnectivityManager.ACTION_TETHER_STATE_CHANGED.equals(action)) {

            } else if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(action)) {
            }

        }
    };
	
	public WifiApSwichPreference(Context context) {
		super(context);
		mContext = context;
		setLayoutResource(R.layout.preference_wifi_switch);
		mIntentFilter = new IntentFilter(WifiManager.WIFI_AP_STATE_CHANGED_ACTION);
	}
	
	public WifiApSwichPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		setLayoutResource(R.layout.preference_wifi_switch);
		mIntentFilter = new IntentFilter(WifiManager.WIFI_AP_STATE_CHANGED_ACTION);
	}

	public WifiApSwichPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		setLayoutResource(R.layout.preference_wifi_switch);
		mIntentFilter = new IntentFilter(WifiManager.WIFI_AP_STATE_CHANGED_ACTION);
	}
	
	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
        wifiManager = (WifiManager)getContext().getSystemService(Context.WIFI_SERVICE);
		mSwitch = (SwitchButton) view.findViewById(R.id.pref_switch);
		
		if (mSwitch != null) {
			mSwitch.setOnCheckedChangeListener(this);
		}
		
        final int wifiState = wifiManager.getWifiApState();
        	
//        if(wifiState == WifiManager.WIFI_AP_STATE_DISABLED)
//        	setSoftapEnabled(true);
		
        handleWifiApStateChanged(wifiState);
        
//        boolean isEnabled = (wifiState == WifiManager.WIFI_AP_STATE_ENABLED || wifiState == WifiManager.WIFI_AP_STATE_ENABLING);
//        mSwitch.setChecked(isEnabled);
//        mSwitch.setEnabled(isEnabled);
	}
	
	public void resume() {
		LogUtils.d("resume");
		getContext().registerReceiver(mReceiver, mIntentFilter);
	}
	
	public void pause() {
		LogUtils.d("pause");
		getContext().unregisterReceiver(mReceiver);
	}
	
    public void setSoftapEnabled(boolean enable) {
        if (mStateMachineEvent) {
            return;
        }
        final ContentResolver cr = mContext.getContentResolver();
        /**
         * Disable Wifi if enabling tethering
         */
        int wifiState = wifiManager.getWifiState();
        if (enable && ((wifiState == WifiManager.WIFI_STATE_ENABLING) ||
                    (wifiState == WifiManager.WIFI_STATE_ENABLED))) {
        	wifiManager.setWifiEnabled(false);
            Settings.Global.putInt(cr, Settings.Global.WIFI_SAVED_STATE, 1);
        }

        if (wifiManager.setWifiApEnabled(null, enable)) {
            /* Disable here, enabled on receiving success broadcast */
        	mSwitch.setEnabled(false);
        } else {
//        	mSwitch.setSummary(R.string.wifi_error);
        }
          
        EffectUtils.memoryApstate(mContext, enable);
        /**
         *  If needed, restore Wifi on tether disable
         */
        if (!enable) {
            int wifiSavedState = 0;
            try {
                wifiSavedState = Settings.Global.getInt(cr, Settings.Global.WIFI_SAVED_STATE);
            } catch (Settings.SettingNotFoundException e) {
                ;
            }
            if (wifiSavedState == 1) {
            	wifiManager.setWifiEnabled(true);
                Settings.Global.putInt(cr, Settings.Global.WIFI_SAVED_STATE, 0);
            }
        }
    }
	
    private void handleWifiApStateChanged(int state) {
    	LogUtils.d("WifiApSwichPreferencehandleWifiApStateChanged: " + state);
    	if(mSwitch == null)
    		return;
        switch (state) {
            case WifiManager.WIFI_AP_STATE_ENABLING:
            	setSwitchChecked(true);
            	mSwitch.setEnabled(false);
                break;
            case WifiManager.WIFI_AP_STATE_ENABLED:
                /*
                 * Summary on enable is handled by tether
                 * broadcast notice
                 */
                setSwitchChecked(true);
                mSwitch.setEnabled(true);
                break;
            case WifiManager.WIFI_AP_STATE_DISABLING:
            	setSwitchChecked(false);
            	mSwitch.setEnabled(false);
                break;
            case WifiManager.WIFI_AP_STATE_DISABLED:
                setSwitchChecked(false);
                mSwitch.setEnabled(true);
                break;
//            default:
//                mCheckBox.setChecked(false);
//                mCheckBox.setSummary(R.string.wifi_error);
//                enableWifiCheckBox();
        }
    }
    
    private void setSwitchChecked(boolean checked) {
    	if (mSwitch==null) 
    		return;
        if (checked != mSwitch.isChecked()) {
            mStateMachineEvent = true;
            mSwitch.setChecked(checked);
            mStateMachineEvent = false;
        }
    }
    
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		setSoftapEnabled(isChecked);
	}

}
