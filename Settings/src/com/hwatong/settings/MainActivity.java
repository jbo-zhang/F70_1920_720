package com.hwatong.settings;

import com.hwatong.settings.R;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;


public class MainActivity extends Activity implements OnClickListener{
	private final String TAG = "MainActivity";
	private ImageView mSoundSettings;
	private ImageView mScreenSettings;
	private ImageView mTimeSettings;
	private ImageView mVersionSettings;
	private ImageView mWallpaperSettings;
	private ImageView mSystemSettings;
//	private ImageView mApplicationsSettings;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    
        mSoundSettings = (ImageView) findViewById(R.id.iv_sound_settings);
        mSoundSettings.setOnClickListener(this);
        mScreenSettings = (ImageView) findViewById(R.id.iv_screen_settings);
        mScreenSettings.setOnClickListener(this);
        mTimeSettings = (ImageView) findViewById(R.id.iv_time_settings);
        mTimeSettings.setOnClickListener(this);
        mVersionSettings = (ImageView) findViewById(R.id.iv_version_settings);
        mVersionSettings.setOnClickListener(this);
        mWallpaperSettings = (ImageView) findViewById(R.id.iv_wallpaper_settings);
        mWallpaperSettings.setOnClickListener(this);
        mSystemSettings = (ImageView) findViewById(R.id.iv_system_settings);
        mSystemSettings.setOnClickListener(this);
//        mApplicationsSettings = (ImageView) findViewById(R.id.iv_applications_settings);
//        mApplicationsSettings.setOnClickListener(this);
        
    }

//	@Override
//	protected void onNewIntent(Intent intent) {
//		// TODO Auto-generated method stub
//    	String fragment = intent.getStringExtra("fragment");
//    	if (fragment!=null && fragment.equals("equalizer")) {
//    		
//			Intent intent1 = new Intent();
//			intent1.setClassName("com.hwatong.equalizer", "com.hwatong.equalizer.EqMenuActivity");
//			intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			try {
//				startActivity(intent1);
//			} catch(ActivityNotFoundException e) {}
//    		return;
//    	}
//		super.onNewIntent(intent);
//	}

	@Override
	public void onClick(View v) {
        Intent intent = null;

		switch(v.getId()) {
		case R.id.iv_sound_settings:
	        intent = new Intent(Intent.ACTION_MAIN);
	        intent.setClass(this, SubSettings.class);
	        intent.putExtra(Settings.EXTRA_SHOW_FRAGMENT, SoundSettings.class.getName());
	        intent.putExtra(Settings.EXTRA_NO_HEADERS, true);
			break;
		case R.id.iv_screen_settings:
	        intent = new Intent(Intent.ACTION_MAIN);
	        intent.setClass(this, SubSettings.class);
	        intent.putExtra(Settings.EXTRA_SHOW_FRAGMENT, BrightnessFragment.class.getName());
	        intent.putExtra(Settings.EXTRA_NO_HEADERS, true);
			break;
		case R.id.iv_time_settings:
	        intent = new Intent(Intent.ACTION_MAIN);
	        intent.setClass(this, SubSettings.class);
	        intent.putExtra(Settings.EXTRA_SHOW_FRAGMENT, DateTimeSettings.class.getName());
	        intent.putExtra(Settings.EXTRA_NO_HEADERS, true);
			break;
		case R.id.iv_version_settings:
	        intent = new Intent(Intent.ACTION_MAIN);
	        intent.setClass(this, SubSettings.class);
	        intent.putExtra(Settings.EXTRA_SHOW_FRAGMENT, DeviceInfoSettings.class.getName());
	        intent.putExtra(Settings.EXTRA_NO_HEADERS, true);
			break;
		case R.id.iv_wallpaper_settings:
	        intent = new Intent("android.intent.action.WALLPAPER_SETTINGS");
			break;
		case R.id.iv_system_settings:
	        intent = new Intent(Intent.ACTION_MAIN);
	        intent.setClass(this, SubSettings.class);
	        intent.putExtra(Settings.EXTRA_SHOW_FRAGMENT, CommonSettings.class.getName());
	        intent.putExtra(Settings.EXTRA_NO_HEADERS, true);
			break;
//		case R.id.iv_applications_settings:
//	        intent = new Intent(Intent.ACTION_MAIN);
//	        intent.setClass(this, SubSettings.class);
//	        intent.putExtra(Settings.EXTRA_SHOW_FRAGMENT, ManageApplications.class.getName());
//	        intent.putExtra(Settings.EXTRA_NO_HEADERS, true);
//			break;
		}
		if (intent!=null)
			startActivity(intent);
	}

}
