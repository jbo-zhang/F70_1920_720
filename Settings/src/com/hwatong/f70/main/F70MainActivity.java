package com.hwatong.f70.main;

import java.io.FileInputStream;
import java.util.Properties;

import com.hwatong.f70.bluetooth.BaseBluetoothSettingActivity;
import com.hwatong.f70.carsetting.BaseCarSettingActivity;
import com.hwatong.f70.commonsetting.BaseCommonSettingActivity;
import com.hwatong.f70.huachenyun.BaseHuaChenYunActivity;
import com.hwatong.f70.soundsetting.BaseSoundSettingActivity;
import com.hwatong.settings.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

public class F70MainActivity extends Activity implements OnClickListener {

	private RelativeLayout commonSetting, soundSetting, bluetoothSetting,
			carSetting, huachenSetting, backupSetting, programerMode;

	private ImageButton killProcess;
	private static final int COMFIRM_BACKUP = 0x001;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.f70_activity_main);
		commonSetting = (RelativeLayout) findViewById(R.id.commonsetting_title);
		commonSetting.setOnClickListener(this);

		soundSetting = (RelativeLayout) findViewById(R.id.soundsetting_title);
		soundSetting.setOnClickListener(this);

		bluetoothSetting = (RelativeLayout) findViewById(R.id.bluetoothsetting_title);
		bluetoothSetting.setOnClickListener(this);

		carSetting = (RelativeLayout) findViewById(R.id.carsetting_title);
		carSetting.setOnClickListener(this);

		huachenSetting = (RelativeLayout) findViewById(R.id.huachenyun_title);
		huachenSetting.setOnClickListener(this);

		backupSetting = (RelativeLayout) findViewById(R.id.backupsetting_title);
		backupSetting.setOnClickListener(this);

		programerMode = (RelativeLayout) findViewById(R.id.programmode_title);
		programerMode.setOnClickListener(this);

		killProcess = (ImageButton) findViewById(R.id.killprocess);
		killProcess.setOnClickListener(this);
		
		if(ConfigrationVersion.getInstance().isHight())
			huachenSetting.setVisibility(View.VISIBLE);
		
		if(savedInstanceState == null)
			F70Application.isShowProgram = 0;			
	}

	@Override
	protected void onResume() {
		super.onResume();
		LogUtils.d("onResume");
		F70Application.getInstance().sendCurrentPageName("setting_main");

		addProgramerMode();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Utils.setProgramMode(getApplicationContext(), Utils.PROGRAMER_MODE_OFF);
		LogUtils.d("onDestroy");
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		LogUtils.d("onSaveInstanceState");
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		LogUtils.d("onRestoreInstanceState");
	}

	public Properties loadConfig(Context context, String file) {
		Properties properties = new Properties();
		try {
			FileInputStream s = new FileInputStream(file);
			properties.load(s);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return properties;
	}

	@Override
	public void onClick(View v) {
		int resId = v.getId();
		switch (resId) {
		case R.id.killprocess:
			Intent intent8 = new Intent(Intent.ACTION_MAIN);
			intent8.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent8.addCategory(Intent.CATEGORY_HOME);
			startActivity(intent8);
			onBackPressed();
			break;
		case R.id.commonsetting_title:
			Intent intent = new Intent(this, BaseCommonSettingActivity.class);
			startActivity(intent);
			break;
		case R.id.soundsetting_title:
			Intent intent2 = new Intent(this, BaseSoundSettingActivity.class);
			startActivity(intent2);
			break;
		case R.id.bluetoothsetting_title:
			Intent intent3 = new Intent(this,
					BaseBluetoothSettingActivity.class);
			startActivity(intent3);
			break;
		case R.id.carsetting_title:
			Intent intent4 = new Intent(this, BaseCarSettingActivity.class);
			startActivity(intent4);
			break;
		case R.id.huachenyun_title:
			Intent intent5 = new Intent(this, BaseHuaChenYunActivity.class);
			startActivity(intent5);
			break;
		case R.id.backupsetting_title:
			confirmBackUp();
			break;
		case R.id.programmode_title:
			Utils.startApp(this, "com.hwatong.projectmode", "com.hwatong.projectmode.MainActivity");
//			Utils.openApplication(this, pkgName)
			break;
		default:
			break;
		}
	}

	@Override
	public void onBackPressed() {
//		super.onBackPressed();
		finish();
		overridePendingTransition(R.anim.activity_open_enter, R.anim.activity_close_exit);
	}

	
	private void confirmBackUp() {
	if(!mHandler.hasMessages(COMFIRM_BACKUP)) {	
		final SettingBackUpDialog dialog = new SettingBackUpDialog(this);
		int offset = (int)getResources().getDimension(R.dimen.wifidialog_offset);
		Window win = dialog.getWindow();
		LayoutParams params = new LayoutParams();
		params.width = LayoutParams.WRAP_CONTENT;
		params.height = LayoutParams.WRAP_CONTENT;
		params.x = -offset;
		win.setAttributes(params);
		dialog.setOnPositiveListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// mHandler.removeCallbacks(runnable);
				mHandler.sendEmptyMessageDelayed(COMFIRM_BACKUP, 5000);
				dialog.dismiss();
			}
		});
		dialog.setOnNegativeListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});

		dialog.show();
	}
	}

	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case COMFIRM_BACKUP:
				sendBroadcast(new Intent("android.intent.action.MASTER_CLEAR"));
				break;

			default:
				break;
			}

		}
	};

	private void addProgramerMode() {
		LogUtils.d("getProgramMode: " + F70Application.isShowProgram);
		programerMode
				.setVisibility(F70Application.isShowProgram == 1 ? View.VISIBLE
						: View.GONE);
	}

}
