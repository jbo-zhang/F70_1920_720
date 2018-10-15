package com.hwatong.f70.commonsetting;

import com.hwatong.f70.baseview.BaseFragment.OnFragmentImageChangedListener;
import com.hwatong.f70.main.F70Application;
import com.hwatong.f70.main.LogUtils;
import com.hwatong.f70.main.Utils;
import com.hwatong.settings.CommonSettings;
import com.hwatong.settings.R;
import com.hwatong.settings.widget.MyRadioGroup;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;
import android.widget.RelativeLayout;

public class BaseCommonSettingActivity extends Activity implements
		OnClickListener, com.hwatong.settings.widget.MyRadioGroup.OnCheckedChangeListener, OnFragmentImageChangedListener {

	private TimeSetting mTimeSetting;
	private DisplaySetting mDisplaySetting;
	private LanguageSetting mLanguageSetting;
	private PressSound mPressSound;
	private VersionInfo mVersionInfo;
	private static final String TAG = "BaseCommonSettingActivity";

	private final String TIMESETTINGFLAG = "timesetting";
	private final String DISPLAYSSETTINGFLAG = "displayssetting";
	private final String LANGUAGESETTINGFLAG = "languagesetting";
	private final String PRESSSOUNDFLAG = "presssound";
	private final String VERSIONINFOFLAG = "versioninfo";
	private String currentFragment = "";

	private MyRadioGroup commonSettingHeaders;
	private RadioButton languageRb, timeRb, displayRb, presssoundRb, versionRb;
	private RelativeLayout button;
	private Toast mDevHitToast;
	private ImageView bigImage;
	private FragmentManager fragmentManager;
	private static final int TAPS_TO_BE_A_DEVELOPER = 10;
	private int mDevHitCountdown = TAPS_TO_BE_A_DEVELOPER;

	private final int COMMONSETTING_FRAGMENT = R.id.common_fragment;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.f70_commmonsetting_main);
		initWidget();
	}

	private void initWidget() {
		initFragment();
		commonSettingHeaders = (MyRadioGroup) findViewById(R.id.common_navi);
		commonSettingHeaders.setOnCheckedChangeListener(this);

		languageRb = (RadioButton) findViewById(R.id.languagesetting);
		timeRb = (RadioButton) findViewById(R.id.timesetting);
		displayRb = (RadioButton) findViewById(R.id.displaysetting);
		presssoundRb = (RadioButton) findViewById(R.id.presssound);
		versionRb = (RadioButton) findViewById(R.id.version);
		bigImage = (ImageView) findViewById(R.id.bigimage);

		languageRb.setOnClickListener(this);
		timeRb.setOnClickListener(this);
		displayRb.setOnClickListener(this);
		presssoundRb.setOnClickListener(this);
		versionRb.setOnClickListener(this);

		button = (RelativeLayout) findViewById(R.id.setting_main_back);
		button.setOnClickListener(this);
		
		startFragment(mLanguageSetting, LANGUAGESETTINGFLAG);
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		mDevHitToast = null;

		F70Application.getInstance().sendCurrentPageName("setting_common");
		LogUtils.d("onResume");

		languageRb.setText(R.string.system_language);
		timeRb.setText(R.string.timesetting);
		displayRb.setText(R.string.displaysetting);
		presssoundRb.setText(R.string.press_sound);
		versionRb.setText(R.string.system_version_title);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		LogUtils.d(TAG, "onDestroy");
	}

	@Override
	public void onClick(View v) {
		int resId = v.getId();
		switch (resId) {
		case R.id.setting_main_back: 
			finish();
			overridePendingTransition(R.anim.activity_open_enter, R.anim.activity_close_exit);
			break;
		case R.id.version:
			performVersionClick(); 
			break;
		default:
			break;
		}
	}
	
	@Override
	public void onFragmentChanged(String fragmentName) {
				
		if(fragmentName.equals(TimeSetting.class.getName())) {
			bigImage.setImageResource(R.drawable.commonsetting_time);
		} else if(fragmentName.equals(DisplaySetting.class.getName())) {
			bigImage.setImageResource(R.drawable.commonsetting_display);
		} else if(fragmentName.equals(LanguageSetting.class.getName())) {
			bigImage.setImageResource(R.drawable.commonsetting_language);
		}else if(fragmentName.equals(PressSound.class.getName())) {
			bigImage.setImageResource(R.drawable.commonsetting_presssound);
		}else if(fragmentName.equals(VersionInfo.class.getName())) {
			bigImage.setImageResource(R.drawable.commonsetting_version);
		}
	}

	private void initFragment() {
		fragmentManager = this.getFragmentManager();
		mTimeSetting = new TimeSetting();
		mDisplaySetting = new DisplaySetting();
		mLanguageSetting = new LanguageSetting();
		mPressSound = new PressSound();
		mVersionInfo = new VersionInfo();
		
		if(!Utils.isLowDesityMachine(BaseCommonSettingActivity.this)) {
			mTimeSetting.setOnFragmentImageChangedListener(this);
			mDisplaySetting.setOnFragmentImageChangedListener(this);
			mLanguageSetting.setOnFragmentImageChangedListener(this);
			mPressSound.setOnFragmentImageChangedListener(this);
			mVersionInfo.setOnFragmentImageChangedListener(this);
		}
	}
	
	@Override
	public void onCheckedChanged(MyRadioGroup myRadioGroup, int checkedId) {
		if(checkedId != R.id.version && mDevHitCountdown < TAPS_TO_BE_A_DEVELOPER)
			mDevHitCountdown = TAPS_TO_BE_A_DEVELOPER;
		switch (checkedId) {
		case R.id.timesetting:
			startFragment(mTimeSetting, TIMESETTINGFLAG);
			break;
		case R.id.displaysetting:
			startFragment(mDisplaySetting, DISPLAYSSETTINGFLAG);
			break;
		case R.id.languagesetting:
			startFragment(mLanguageSetting, LANGUAGESETTINGFLAG);
			break;
		case R.id.presssound:
			startFragment(mPressSound, PRESSSOUNDFLAG);
			break;
		case R.id.version:
			startFragment(mVersionInfo, VERSIONINFOFLAG);
			break;
		default:
			break;
		}
	}
	
	
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				Log.d("zjb", "refresh radiogroup");
				commonSettingHeaders.enabledAllRadioButton();
				break;

			default:
				break;
			}
		};
	};
	
	private synchronized void startFragment(final Fragment f, final String fragmentFlag) {
		Log.d("zjb", "startFragment isVisible" + f.isVisible() + "  isAdded: " + f.isAdded() + " " + fragmentFlag + " " + currentFragment + Thread.currentThread().getName());
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
					fragmentTransaction.replace(COMMONSETTING_FRAGMENT, f, fragmentFlag);
					fragmentTransaction.commit();
					currentFragment = fragmentFlag;
					syncLabel();
				}
			});
			commonSettingHeaders.disEnabledAllRadioButton();
			handler.sendEmptyMessageDelayed(1, 200);
		}
	}
	
	private void syncLabel() {
		if(TIMESETTINGFLAG.equals(currentFragment)) {
			 commonSettingHeaders.check(R.id.timesetting);
			 
		} else if(DISPLAYSSETTINGFLAG.equals(currentFragment)) {
			 commonSettingHeaders.check(R.id.displaysetting);
			 
		} else if(LANGUAGESETTINGFLAG.equals(currentFragment)) {
			 commonSettingHeaders.check(R.id.languagesetting);
			 
		} else if(PRESSSOUNDFLAG.equals(currentFragment)) {
			 commonSettingHeaders.check(R.id.presssound);
			 
		} else if(VERSIONINFOFLAG.equals(currentFragment)) {
			commonSettingHeaders.check(R.id.version);
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
	
	private void performVersionClick() {
		LogUtils.d("mDevHitCountdown: " + mDevHitCountdown);
        if (mDevHitCountdown > 0) {
            mDevHitCountdown--;
            if (mDevHitCountdown == 0) {         		
                if (mDevHitToast != null) {
                    mDevHitToast.cancel();
                }
                mDevHitToast = Toast.makeText(this, R.string.show_pro_on,
                        Toast.LENGTH_LONG);
                mDevHitToast.show();
                F70Application.isShowProgram = 1;
            } else if (mDevHitCountdown > 0
                    && mDevHitCountdown < (TAPS_TO_BE_A_DEVELOPER-2)) {
                if (mDevHitToast != null) {
                    mDevHitToast.cancel();
                }
            }
        }
	}

}
