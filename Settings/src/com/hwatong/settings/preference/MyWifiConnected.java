package com.hwatong.settings.preference;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceManager.OnActivityDestroyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hwatong.f70.main.LogUtils;
import com.hwatong.settings.R;
import com.hwatong.settings.wifi.AccessPoint;


public class MyWifiConnected extends Preference implements OnActivityDestroyListener{
	private static final String TAG = "MyWifiConnected";

	private Context mContext;
	private ImageView mSignalImageView;
	private AccessPoint mAccessPoint;
		
	public MyWifiConnected(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}

	public MyWifiConnected(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public MyWifiConnected(Context context) {
		super(context);
		mContext = context;
	}

	public AccessPoint getAccessPoint() { return mAccessPoint;}
	
	@Override
	protected View onCreateView(ViewGroup parent) {
		final LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View layout = layoutInflater.inflate(R.layout.preference_wifi_connected, parent, false);
		mSignalImageView = (ImageView) layout.findViewById(R.id.signal);
		refresh();
		return layout;
	}

	private void refresh() {
		if (mSignalImageView==null) return;
		LogUtils.d("accesspoint: " + mAccessPoint);
		if (mAccessPoint==null) {
			setSummary(null);
			mSignalImageView.setVisibility(View.GONE);
		}else {
			mSignalImageView.setImageLevel(mAccessPoint.getLevel());
			setSummary(mAccessPoint.getTitle());
			mSignalImageView.setVisibility(View.VISIBLE);
		}
	}
	
	public void update(AccessPoint accessPoint) {
		mAccessPoint = accessPoint;
		refresh();
	}
	
	public boolean update(AccessPoint accessPoint, WifiInfo info, DetailedState state) {
		if (accessPoint==null) {
    		Log.d(TAG, "update: disconnected!");
        	mAccessPoint = null;
        	
            refresh();
		} else if (info != null && accessPoint.getNetworkId() != WifiConfiguration.INVALID_NETWORK_ID && 
        		accessPoint.getNetworkId() == info.getNetworkId() && state == DetailedState.CONNECTED) {
        	mAccessPoint = accessPoint;
        	LogUtils.d("DetailedState: " + mAccessPoint);
    		Log.d(TAG, "update: connected!");
            refresh();
            return true;
        } 
		else if (accessPoint.getInfo() != null) {
        	mAccessPoint = null;
    		Log.d(TAG, "update: disconnected!");
            refresh();
        }
		return false;
	}
	
	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
	}

	@Override
	public void setSummary(CharSequence summary) {
		super.setSummary(summary);
	}

	@Override
	public void setSummary(int summaryResId) {
		super.setSummary(summaryResId);
	}

	@Override
	public void onActivityDestroy() {
	}	
}