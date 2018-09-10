package com.hwatong.settings.preference;

import java.util.concurrent.atomic.AtomicBoolean;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.Toast;

import com.hwatong.f70.soundsetting.EffectUtils;
import com.hwatong.settings.R;
import com.hwatong.settings.widget.SwitchButton;


public class MyWifiPreference extends Preference implements CompoundButton.OnCheckedChangeListener{
	private static final String TAG = "MyWifiPreference";

	private SwitchButton mSwitch;

	
    private AtomicBoolean mConnected = new AtomicBoolean(false);
    
    private WifiManager wifiManager;
    private boolean mStateMachineEvent;
    private final IntentFilter mIntentFilter;
    private Context context;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                handleWifiStateChanged(intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN));
            } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
                if (!mConnected.get()) {
                    handleStateChanged(WifiInfo.getDetailedStateOf((SupplicantState)
                            intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE)));
                }
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(
                        WifiManager.EXTRA_NETWORK_INFO);
                mConnected.set(info.isConnected());
                handleStateChanged(info.getDetailedState());
            }
        }
    };
    private void handleWifiStateChanged(int state) {
    	if (mSwitch==null) 
    		return;
    	
        switch (state) {
            case WifiManager.WIFI_STATE_ENABLING:
            	setSwitchChecked(false);
            	mSwitch.setEnabled(false);
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                setSwitchChecked(true);
                mSwitch.setEnabled(true);
                break;
            case WifiManager.WIFI_STATE_DISABLING:
            	setSwitchChecked(false);
            	mSwitch.setEnabled(false);
                break;
            case WifiManager.WIFI_STATE_DISABLED:
                setSwitchChecked(false);
                mSwitch.setEnabled(true);
                break;
//            default:
//                setSwitchChecked(false);
//                mSwitch.setEnabled(true);
//                break;
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
    
    private void handleStateChanged(@SuppressWarnings("unused") NetworkInfo.DetailedState state) {
        // After the refactoring from a CheckBoxPreference to a Switch, this method is useless since
        // there is nowhere to display a summary.
        // This code is kept in case a future change re-introduces an associated text.
        /*
        // WifiInfo is valid if and only if Wi-Fi is enabled.
        // Here we use the state of the switch as an optimization.
        if (state != null && mSwitch.isChecked()) {
            WifiInfo info = mWifiManager.getConnectionInfo();
            if (info != null) {
                //setSummary(Summary.get(mContext, info.getSSID(), state));
            }
        }
        */
    }
    public void wifiSwitchChange(CompoundButton buttonView, boolean isChecked) {
    	Log.d(TAG, "wifiSwitchChange: isChecked="+isChecked);
        //Do nothing if called as a result of a state machine event
        if (mStateMachineEvent) {
            return;
        }
        // Disable tethering if enabling Wifi
        int wifiApState = wifiManager.getWifiApState();

        if (isChecked && ((wifiApState == WifiManager.WIFI_AP_STATE_ENABLING) ||
                (wifiApState == WifiManager.WIFI_AP_STATE_ENABLED))) {
            wifiManager.setWifiApEnabled(null, false);
            EffectUtils.memoryApstate(context, false);
        }
                
        if (wifiManager.setWifiEnabled(isChecked)) {
            // Intent has been taken into account, disable until new state is active
        	mSwitch.setEnabled(false);
        } 
        else {
            // Error
            Toast.makeText(getContext(), R.string.wifi_error, Toast.LENGTH_SHORT).show();
        }
    }
	
	private OnMyCheckedChangedListener mOnMyCheckedChangedListener;

	public interface OnMyCheckedChangedListener {
		public void onMyCheckedChanged(CompoundButton button, boolean checked);
	}

	//褰撹缃簡鐩戝惉绔彛鏃讹紝鍒嗙switch鍜宨tem鐨勭偣鍑绘秷鎭�
	public void setOnMyCheckedChangedListener(OnMyCheckedChangedListener listener) {
		mOnMyCheckedChangedListener = listener;
	}

	public MyWifiPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
	    mIntentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
	    // The order matters! We really should not depend on this. :(
	    setLayoutResource(R.layout.preference_wifi_switch);
	    mIntentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
	    mIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
	}

	public MyWifiPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		//閫氳繃璋冪敤setWidgetLayoutResource鏂规硶鏉ユ洿鏂皃reference鐨剋idgetLayout,鍗虫洿鏂版帶浠跺尯鍩�
		setLayoutResource(R.layout.preference_wifi_switch);
	    mIntentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
	    // The order matters! We really should not depend on this. :(
	    mIntentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
	    mIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
	}

	public MyWifiPreference(Context context) {
		super(context);
		this.context = context;
		//閫氳繃璋冪敤setWidgetLayoutResource鏂规硶鏉ユ洿鏂皃reference鐨剋idgetLayout,鍗虫洿鏂版帶浠跺尯鍩�
		setLayoutResource(R.layout.preference_wifi_switch);
	    mIntentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
	    // The order matters! We really should not depend on this. :(
	    mIntentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
	    mIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
	}

	
	@Override
	protected void onBindView(View view) {
        wifiManager = (WifiManager)getContext().getSystemService(Context.WIFI_SERVICE);
		mSwitch = (SwitchButton) view.findViewById(R.id.pref_switch);

		//switch寮�鍏崇殑鐐瑰嚮浜嬩欢
		if (mSwitch != null) {
			mSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton button, boolean checked) {
		    		wifiSwitchChange(button, checked);
				}
			});
		}
        final int wifiState = wifiManager.getWifiState();
            boolean isEnabled = (wifiState == WifiManager.WIFI_STATE_ENABLED || wifiState == WifiManager.WIFI_STATE_ENABLING);
            boolean isDisabled = wifiState == WifiManager.WIFI_STATE_DISABLED;
            mSwitch.setChecked(isEnabled);
            mSwitch.setEnabled(isEnabled);

//        mSwitch.setChecked(super.isChecked());
		super.onBindView(view);
	}

	public void resume() {
        getContext().registerReceiver(mReceiver, mIntentFilter);
	}
	public void pause() {
        getContext().unregisterReceiver(mReceiver);
	}
//	public Switch getSwitch() {
//		return mSwitch;
//	}
	public void setChecked(boolean bChecked) {
		Log.d(TAG, "setChecked()" + bChecked);
//		super.setChecked(bChecked);
	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		
	}
	
}