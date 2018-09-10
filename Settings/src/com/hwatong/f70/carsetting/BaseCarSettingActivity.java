package com.hwatong.f70.carsetting;

import com.hwatong.f70.baseview.BaseFragment.OnFragmentImageChangedListener;
import com.hwatong.f70.main.ConfigrationVersion;
import com.hwatong.f70.main.F70Application;
import com.hwatong.f70.main.Utils;
import com.hwatong.settings.R;
import com.hwatong.settings.widget.MyRadioGroup;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

public class BaseCarSettingActivity extends Activity implements
		OnClickListener,
		com.hwatong.settings.widget.MyRadioGroup.OnCheckedChangeListener, OnFragmentImageChangedListener {

	
	private DoorSetting mDoorSetting;
	private RearViewSetting mRearViewSetting;
	private LightSetting mLightSetting;
	private ReversingSetting mReversingSetting;
	private SpeedWarn mSpeedWarn;
	private KeepFitTips mFitTips;

	private final String DOORSETTINGFLAG = "doorsetting";
	private final String REARVIEWSETTINGFLAG = "rearviewsetting";
	private final String LIGHTSETTINGFLAG = "lightsetting";
	private final String REVERSINGSETTINGFLAG = "reversingsetting";
	private final String SPEEDWARNFLAG = "speedwarn";
	private final String KEEPFITTIPSFLAG = "keepfittips";
	private String currentFragment = "";

	private MyRadioGroup carSettingHeaders;
	private RadioButton doorRb, rearViewRb, lightRb, reversingRb, warnRb,
			keepfitRb;
	private RelativeLayout button;// ·µ»Ø¼ü
	private ImageView bigImage;
	private FragmentManager fragmentManager;
	private ImageView rearviewIntervel, speedwarnIntervel;
	private final int CARSETTING_FRAGMENT = R.id.car_fragment;
	int curPress = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.f70_carsetting_main);
		initFragment();
		carSettingHeaders = (MyRadioGroup) findViewById(R.id.carsetting_navi);
		carSettingHeaders.setOnCheckedChangeListener(this);

		button = (RelativeLayout) findViewById(R.id.setting_main_back);
		button.setOnClickListener(this);

		doorRb = (RadioButton) findViewById(R.id.cardoorsetting);
		rearViewRb = (RadioButton) findViewById(R.id.rearviewsetting);
		lightRb = (RadioButton) findViewById(R.id.carlightsetting);
		reversingRb = (RadioButton) findViewById(R.id.reversingsetting);
		warnRb = (RadioButton) findViewById(R.id.speedwarn);
		keepfitRb = (RadioButton) findViewById(R.id.keepfit);
		bigImage = (ImageView) findViewById(R.id.bigimage);
		doorRb.setOnClickListener(this);
		rearViewRb.setOnClickListener(this);
		lightRb.setOnClickListener(this);
		reversingRb.setOnClickListener(this);
		warnRb.setOnClickListener(this);
		keepfitRb.setOnClickListener(this);

		
		if (ConfigrationVersion.getInstance().isHight()
				|| ConfigrationVersion.getInstance().isMiddleLuxury()) {

			speedwarnIntervel = (ImageView) findViewById(R.id.speedwarn_intervel);
			speedwarnIntervel.setVisibility(View.VISIBLE);
			warnRb.setVisibility(View.VISIBLE);
			keepfitRb.setVisibility(View.VISIBLE);
		}
		
		
		if (ConfigrationVersion.getInstance().isHight()
				|| ConfigrationVersion.getInstance().isMiddleLuxury()
				|| ConfigrationVersion.getInstance().isMiddleElite()) {
			rearviewIntervel = (ImageView) findViewById(R.id.rearviewsetting_intervel);
			rearviewIntervel.setVisibility(View.VISIBLE);

			rearViewRb.setVisibility(View.VISIBLE);
		}

		startFragment(mDoorSetting, DOORSETTINGFLAG);
	}

	@Override
	protected void onResume() {
		super.onResume();
		F70Application.getInstance().sendCurrentPageName("setting_car");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void initFragment() {
		fragmentManager = this.getFragmentManager();
		mDoorSetting = new DoorSetting();
		mRearViewSetting = new RearViewSetting();
		mLightSetting = new LightSetting();
		mReversingSetting = new ReversingSetting();
		mSpeedWarn = new SpeedWarn();
		mFitTips = new KeepFitTips();
		
		if(!Utils.isLowDesityMachine(BaseCarSettingActivity.this)) {
			
			mDoorSetting.setOnFragmentImageChangedListener(this);
			mRearViewSetting.setOnFragmentImageChangedListener(this);
			mLightSetting.setOnFragmentImageChangedListener(this);
			mReversingSetting.setOnFragmentImageChangedListener(this);
			mSpeedWarn.setOnFragmentImageChangedListener(this);
			mFitTips.setOnFragmentImageChangedListener(this);
		}
	}

	@Override
	public void onCheckedChanged(MyRadioGroup group, int checkedId) {
		// hideFragments();
		switch (checkedId) {
		case R.id.cardoorsetting:
			startFragment(mDoorSetting, DOORSETTINGFLAG);
			break;
		case R.id.rearviewsetting:
			startFragment(mRearViewSetting, REARVIEWSETTINGFLAG);
			break;
		case R.id.carlightsetting:
			startFragment(mLightSetting, LIGHTSETTINGFLAG);
			break;
		case R.id.reversingsetting:
			startFragment(mReversingSetting, REVERSINGSETTINGFLAG);
			break;
		case R.id.speedwarn:
			startFragment(mSpeedWarn, SPEEDWARNFLAG);
			break;
		case R.id.keepfit:
			startFragment(mFitTips, KEEPFITTIPSFLAG);
			break;
		default:
			break;
		}
	}
	@Override
	public void onFragmentChanged(String fragmentName) {
		if(fragmentName.equals(DoorSetting.class.getName())) {
			bigImage.setImageResource(R.drawable.carsetting_door);
		} else if(fragmentName.equals(RearViewSetting.class.getName())) {
			bigImage.setImageResource(R.drawable.carsetting_reverview);
		}else if(fragmentName.equals(LightSetting.class.getName())) {
			bigImage.setImageResource(R.drawable.carsetting_light);
		}else if(fragmentName.equals(ReversingSetting.class.getName())) {
			bigImage.setImageResource(R.drawable.carsetting_reversing);
		}else if(fragmentName.equals(SpeedWarn.class.getName())) {
			bigImage.setImageResource(R.drawable.carsetting_speedwarn);
		}else if(fragmentName.equals(KeepFitTips.class.getName())) {
			bigImage.setImageResource(R.drawable.carsetting_keep);
		}
	}

	@Override
	public void onClick(View v) {
		int resId = v.getId();
		switch (resId) {
		case R.id.setting_main_back:
			finish();
			break;

		default:
			break;
		}
	}

	private void startFragment(Fragment f, String fragmentFlag) {

		
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		
		// if (fragmentManager.findFragmentByTag(fragmentFlag) != null) {
		// fragmentTransaction.show(f);
		// } else {
		
		fragmentTransaction.replace(CARSETTING_FRAGMENT, f, fragmentFlag);

		// }
		fragmentTransaction.commit();
		currentFragment = fragmentFlag;
	}

	private void hideFragments() {
		Fragment fragment = fragmentManager.findFragmentByTag(currentFragment);
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		if (fragment != null)
			fragmentTransaction.hide(fragment);
		fragmentTransaction.commit();
	}
}
