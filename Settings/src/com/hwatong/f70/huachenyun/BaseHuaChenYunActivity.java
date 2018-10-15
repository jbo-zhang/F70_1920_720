package com.hwatong.f70.huachenyun;

import com.hwatong.f70.baseview.BaseFragment.OnFragmentImageChangedListener;
import com.hwatong.f70.main.F70Application;
import com.hwatong.f70.main.LogUtils;
import com.hwatong.f70.main.Utils;
import com.hwatong.settings.R;
import com.hwatong.settings.preference.SelectWifiPreference.OnSelectWifiListener;
import com.hwatong.settings.widget.MyRadioGroup;
import com.hwatong.settings.wifi.WifiApSettings;
import com.hwatong.settings.wifi.WifiSettings;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

public class BaseHuaChenYunActivity extends Activity implements
		OnClickListener,
		com.hwatong.settings.widget.MyRadioGroup.OnCheckedChangeListener, OnSelectWifiListener, OnFragmentImageChangedListener
		, com.hwatong.settings.SettingsPreferenceFragment.OnWifiFragmentImageChangedListener{

	private WifiApSettings mWifiApSettings;
	private WifiSettings mWifiSettings;
	private ConvenientSetting mConvenientSetting;
	private SmartNaviSetting mNaviSetting;
	private SefetySetting mSefetySetting;

	private final String WIFISETTINGFLAG = "wifisetting";
	private final String WIFIAPSETTINGFLAG = "wifiapsetting";
	private final String CONVENIENTSETTINGFLAG = "convenientsetting";
	private final String SMARTNAVISETTINGFLAG = "smartnavisetting";
	private final String SEFETYSETTINGFLAG = "sefetysetting";
	private String currentFragment = "";

	private final int HUACHENSETTING_FRAGMENT = R.id.huachenyun_fragment;

	private MyRadioGroup huachenSettingHeaders;
	private RadioButton netRb, convenientRb, safeRb;
	private RelativeLayout button;
	private ImageView bigImage;
	private FragmentManager fragmentManager;
	int curPress = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_
//                | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
						| WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		setContentView(R.layout.f70_huachenyun_main);
		initFragment();
		huachenSettingHeaders = (MyRadioGroup) findViewById(R.id.huachenyun_navi);
		huachenSettingHeaders.setOnCheckedChangeListener(this);

		netRb = (RadioButton) findViewById(R.id.networksetting);
		convenientRb = (RadioButton) findViewById(R.id.convenientsetting);
		safeRb = (RadioButton) findViewById(R.id.sefety);

		netRb.setOnClickListener(this);
		convenientRb.setOnClickListener(this);
		safeRb.setOnClickListener(this);

		button = (RelativeLayout) findViewById(R.id.setting_main_back);
		button.setOnClickListener(this);
		
		bigImage = (ImageView) findViewById(R.id.bigimage);
		
		if(F70Application.isShowWifi)			
			startFragment(mWifiSettings, WIFISETTINGFLAG);
		else
			startFragment(mWifiApSettings, WIFIAPSETTINGFLAG);
	}

	@Override
	protected void onResume() {
		super.onResume();
		F70Application.getInstance().sendCurrentPageName("setting_huachenyun");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void initFragment() {
		fragmentManager = this.getFragmentManager();
		mWifiSettings = new WifiSettings(this, 1);
		mWifiApSettings = new WifiApSettings(this, 0);
		mConvenientSetting = new ConvenientSetting();
		mNaviSetting = new SmartNaviSetting();
		mSefetySetting = new SefetySetting();
		
		if(!Utils.isLowDesityMachine(BaseHuaChenYunActivity.this)) {
			
			mWifiSettings.setOnFragmentImageChangedListener(this);
			mWifiApSettings.setOnFragmentImageChangedListener(this);
			mConvenientSetting.setOnFragmentImageChangedListener(this);
			mSefetySetting.setOnFragmentImageChangedListener(this);
		}
	}
	
	@Override
	public void onCheckedChanged(MyRadioGroup group, int checkedId) {
		// hideFragments();
		switch (checkedId) {
		case R.id.networksetting:
			LogUtils.d("isShowWifiL: " + F70Application.isShowWifi);
			if(F70Application.isShowWifi)			
				startFragment(mWifiSettings, WIFISETTINGFLAG);
			else
				startFragment(mWifiApSettings, WIFIAPSETTINGFLAG);
			break;
		case R.id.convenientsetting:
			startFragment(mConvenientSetting, CONVENIENTSETTINGFLAG);
			break;
		case R.id.smartnavi:
			startFragment(mNaviSetting, SMARTNAVISETTINGFLAG);
			break;
		case R.id.sefety:
			startFragment(mSefetySetting, SEFETYSETTINGFLAG);
			break;
		default:
			break;
		}		
	}

	@Override
	public void onClick(View v) {
		int resId = v.getId();
		switch (resId) {
		case R.id.setting_main_back:
			finish();
			overridePendingTransition(R.anim.activity_open_enter, R.anim.activity_close_exit);
			break;

		default:
			break;
		}
	}

	@Override
	public void onFragmentChanged(String fragmentName) {
		bigImage.setImageResource(R.drawable.hcy_sefety);
	}
	
	@Override
	public void onWifiFragmentChanged(String fragmentName) {
		bigImage.setImageResource(R.drawable.hcy_wifi);
	}
	
	
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				Log.d("zjb", "refresh radiogroup");
				huachenSettingHeaders.enabledAllRadioButton();
				break;

			default:
				break;
			}
		};
	};
	
	
	private synchronized void startFragment(final Fragment f, final String fragmentFlag) {
		Log.d("zjb", "startFragment isVisible " + f.isVisible() + "  isAdded: " + f.isAdded() + " " + fragmentFlag + " " + currentFragment + Thread.currentThread().getName());
		
		if(handler.hasMessages(1)) {
			Log.d("zjb", "contain message too fast");
			syncLabel();
		} else {
			Log.d("zjb", "not have message");
			handler.removeCallbacksAndMessages(null);
			handler.post(new Runnable() {
				@Override
				public void run() {
					if(f.isVisible()) {
						return;
					}
					FragmentTransaction fragmentTransaction = fragmentManager
							.beginTransaction();
					fragmentTransaction.replace(HUACHENSETTING_FRAGMENT, f, fragmentFlag);
					fragmentTransaction.commit();
					currentFragment = fragmentFlag;
					syncLabel();
				}
			});
			huachenSettingHeaders.disEnabledAllRadioButton();
			handler.sendEmptyMessageDelayed(1, 200);
		}
	}
	
	/**
	 * 为了保证按钮与fragment同步
	 */
	private void syncLabel() {
		if(WIFISETTINGFLAG.equals(currentFragment)) {
			huachenSettingHeaders.check(R.id.networksetting);
			
		} else if(WIFIAPSETTINGFLAG.equals(currentFragment)) {
			huachenSettingHeaders.check(R.id.networksetting);
			
		} else if(CONVENIENTSETTINGFLAG.equals(currentFragment)) {
			huachenSettingHeaders.check(R.id.convenientsetting);
			
		} else if(SMARTNAVISETTINGFLAG.equals(currentFragment)) {
			huachenSettingHeaders.check(R.id.smartnavi);
			
		} else if(SEFETYSETTINGFLAG.equals(currentFragment)) {
			huachenSettingHeaders.check(R.id.sefety);
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
	public void onSearchFinish(int which) {
		LogUtils.d("which: " + which);
		if(which == 0)
			startFragment(mWifiApSettings, WIFIAPSETTINGFLAG);
		else
			startFragment(mWifiSettings, WIFISETTINGFLAG);
	}
}
