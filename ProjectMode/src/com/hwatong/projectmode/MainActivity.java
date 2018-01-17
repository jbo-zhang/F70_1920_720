package com.hwatong.projectmode;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.hwatong.projectmode.fragment.DebugModeFragment;
import com.hwatong.projectmode.fragment.SystemProfileFragment;
import com.hwatong.projectmode.fragment.SystemUpdateFragment;
import com.hwatong.projectmode.fragment.TboxUpdateFragment;
import com.hwatong.projectmode.fragment.UpdateFragment;
import com.hwatong.projectmode.fragment.VersionInfoFragment;
import com.hwatong.projectmode.iview.IActivity;
import com.hwatong.projectmode.utils.L;

public class MainActivity extends FragmentActivity implements IActivity, OnClickListener, OnCheckedChangeListener{
	private final static String thiz = MainActivity.class.getSimpleName();
	
    private UpdateFragment updateFragment;
	private TboxUpdateFragment tboxUpdateFragment;
	private VersionInfoFragment versionInfoFragment;
	private DebugModeFragment debugModeFragment;
	private SystemProfileFragment systemProfileFragment;

	private RelativeLayout backBt;
	private RadioGroup radioGroup;
	private RadioButton versionRb, updateRb, debugRb, paramRb;
	
	int curPress = -1;

	private Fragment currentFragment;
	
	private SystemUpdateFragment systemUpdateFragment;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        
        initFragments();
     
        toVersionInfo();
        
        radioGroup.check(versionRb.getId());
    }

	private void initViews() {
		radioGroup = (RadioGroup) findViewById(R.id.program_navi);
		
		backBt = (RelativeLayout) findViewById(R.id.setting_main_back);
		
		versionRb = (RadioButton) findViewById(R.id.version_info);
		updateRb = (RadioButton) findViewById(R.id.update);
		debugRb = (RadioButton) findViewById(R.id.debug_mode);
		paramRb = (RadioButton) findViewById(R.id.sys_param);
		
		setupClickEvent();
		
	}

	private void setupClickEvent() {
		radioGroup.setOnCheckedChangeListener(this);
		
		for (int i = 0; i < radioGroup.getChildCount(); i++) {
			if(radioGroup.getChildAt(i) instanceof RadioButton) {
				final RadioButton item = (RadioButton) radioGroup.getChildAt(i);
				item.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							curPress = item.getId();

							for (int j = 0; j < radioGroup.getChildCount(); j++) {
								if (curPress != radioGroup.getChildAt(j).getId()) {
									radioGroup.getChildAt(j).setEnabled(false);
								}
							}
							break;
						case MotionEvent.ACTION_UP:
							for (int j = 0; j < radioGroup.getChildCount(); j++) {
								if (curPress != radioGroup.getChildAt(j).getId()) {
									radioGroup.getChildAt(j).setEnabled(true);
								}
							}
							curPress = -1;
							break;
						}
						return false;
					}
				});
			}
		}
		
		versionRb.setOnClickListener(this);
		updateRb.setOnClickListener(this);
		debugRb.setOnClickListener(this);
		paramRb.setOnClickListener(this);
		
		backBt.setOnClickListener(this);
	}

	private void initFragments() {
		versionInfoFragment = new VersionInfoFragment();
		versionInfoFragment.setIActivity(this);
		
		updateFragment = new UpdateFragment();
		updateFragment.setIActivity(this);
		
		tboxUpdateFragment = new TboxUpdateFragment();
		tboxUpdateFragment.setIActivity(this);
		
		debugModeFragment = new DebugModeFragment();
		debugModeFragment.setIActivity(this);
		
		systemProfileFragment = new SystemProfileFragment();
		systemProfileFragment.setIActivity(this);
		
		systemUpdateFragment = new SystemUpdateFragment();
		systemUpdateFragment.setIActivity(this);
		
	}

	@Override
	public void toVersionInfo() {
		toFragment(versionInfoFragment);
	}

	@Override
	public void toUpdate() {
		toFragment(updateFragment);
	}

	@Override
	public void toSystemUpdate() {
		toFragment(systemUpdateFragment);
//		Intent intent = new Intent();
//		intent.setAction("android.intent.action.SYSTEM_UPDATE_SETTINGS");
//		if (intent.resolveActivity(getPackageManager()) != null) {
//			startActivity(intent);
//		} else {
//			Toast.makeText(this, "没有升级应用", Toast.LENGTH_SHORT).show();
//		}
	}

	@Override
	public void toTboxUpdate() {
		toFragment(tboxUpdateFragment);
	}

	@Override
	public void toDebugMode() {
		toFragment(debugModeFragment);
	}

	@Override
	public void toSystemProfile() {
		toFragment(systemProfileFragment);
	}
    
	
	private void toFragment(Fragment fragment) {
		currentFragment = fragment;
		
		if(fragment.isAdded()) {
			return;
		}
		getSupportFragmentManager().beginTransaction().replace(R.id.fl_content, fragment).commit();
	}

	
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		L.d(thiz, "checkedId : " + checkedId);
		switch (checkedId) {
		case R.id.version_info:
			toVersionInfo();
			break;
		case R.id.update:
			toUpdate();
			break;
		case R.id.debug_mode:
			toDebugMode();
			break;
		case R.id.sys_param:
			toSystemProfile();
			break;
		default:
			break;
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.setting_main_back:
			if(currentFragment == tboxUpdateFragment || currentFragment == systemUpdateFragment) {
				 toUpdate();
			} else {
				finish();
			}
			break;
		}		
	}
    
}
