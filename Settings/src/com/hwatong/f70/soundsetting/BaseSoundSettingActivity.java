package com.hwatong.f70.soundsetting;

import java.util.Arrays;

import com.hwatong.f70.baseview.BaseFragment.OnFragmentImageChangedListener;
import com.hwatong.f70.baseview.BaseFragment.OnFragmentPausedListener;
import com.hwatong.f70.main.F70Application;
import com.hwatong.f70.main.LogUtils;
import com.hwatong.f70.main.Utils;
import com.hwatong.f70.observable.Function;
import com.hwatong.f70.observable.ObservableManager;
import com.hwatong.settings.R;
import com.hwatong.settings.widget.DrawView;
import com.hwatong.settings.widget.VerticalSeekBar;
import com.hwatong.settings.widget.DrawView.OnXYChangeListener;
import com.hwatong.settings.widget.MyRadioGroup;
import com.hwatong.settings.widget.VerticalSeekBar.UpDownListener;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class BaseSoundSettingActivity extends Activity implements
		OnClickListener,
		com.hwatong.settings.widget.MyRadioGroup.OnCheckedChangeListener, OnFragmentImageChangedListener, OnXYChangeListener, Function<Object, Object>, OnFragmentPausedListener{

	private CurrentSoundSetting currentSoundSetting;
	private EqualizerSetting equalizerSetting;
	private SpeedCompensationSetting speedCompensationSetting;
	private LoudnessSetting loudnessSetting;

	private final String CURRENTSOUNDSETTINGFLAG = "currentSoundSetting";
	private final String EQUALIZERSETTINGFLAG = "equalizerSetting";
	private final String SPEEDCOMPENSATIONSETTINGFLAG = "speedCompensationSetting";
	private final String LOUDNESSSETTINGFLAG = "loudnessSetting";
	private String currentFragment = "";
	private MyRadioGroup soundSettingHeaders;
	private RadioButton currentRb, equalizerRb, speedRb, loudnessRb;
	private RelativeLayout backBt;
	private ImageView bigImage;
	private RelativeLayout currentSoundLayout; //current panel
	private LinearLayout equalizerLayout; //equalizer panel
	private Button resetBt, upBt, leftBt, downBt, rightBt;
	private DrawView touchBt;
	private VerticalSeekBar band1Sb, band2Sb, band3Sb, band4Sb, band5Sb;
	private FragmentManager fragmentManager;
	
	private final int TOUCH_MAX = DrawView.getMax();
	private static final int BAND_MAX = 24;
	private static final int BAND_DIFF = 12;

	private EffectContentObserver effectContentObserver;

	public static final String CONTENT_CHANGED = "content_changed";
	private static final Uri CONTENT_URI = Uri
			.parse("content://car_settings/content");
	private static final String FADER_BALANCE = "fader_balance";
	private Uri uri = Uri.withAppendedPath(CONTENT_URI, FADER_BALANCE);
	int curPress = -1;

	private final int SOUNDSETTING_FRAGMENT = R.id.sound_fragment;

	@Override
	protected void onCreate(Bundle saveInstance) {
		super.onCreate(saveInstance);
		setContentView(R.layout.f70_soundsetting_main);
		
		ObservableManager.newInstance().registerObserver(FuntionCommon.CURRENT_SOUND_SETTING_RESULT_ACTIVITY, this);
	}
	
	@Override
	public void onContentChanged() {
		super.onContentChanged();
		
		initFragment();
		soundSettingHeaders = (MyRadioGroup) findViewById(R.id.sound_navi);
		soundSettingHeaders.setOnCheckedChangeListener(this);
		backBt = (RelativeLayout) findViewById(R.id.setting_main_back);
		backBt.setOnClickListener(this);

		currentRb = (RadioButton) findViewById(R.id.currentsoundsettingsetting);
		equalizerRb = (RadioButton) findViewById(R.id.equalizersettingsetting);
		speedRb = (RadioButton) findViewById(R.id.speedcompensationsettingsetting);
		loudnessRb = (RadioButton) findViewById(R.id.loudnesssetting);
		bigImage = (ImageView) findViewById(R.id.bigimage);
//		bigImage.setOnClickListener(this);

		currentRb.setOnClickListener(this);
		equalizerRb.setOnClickListener(this);
		speedRb.setOnClickListener(this);
		loudnessRb.setOnClickListener(this);

		effectContentObserver = new EffectContentObserver(new Handler());
		getContentResolver().registerContentObserver(uri, true,
				effectContentObserver);
		initTouchFadeLayout();
		
		initEqualizerLayout();

		startFragment(currentSoundSetting, CURRENTSOUNDSETTINGFLAG);
	}

	@Override
	protected void onResume() {
		super.onResume();
		F70Application.getInstance().sendCurrentPageName("setting_sound");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		getContentResolver().unregisterContentObserver(effectContentObserver);
		
		ObservableManager.newInstance().removeObserver(this);
	}

	private class EffectContentObserver extends ContentObserver {

		public EffectContentObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			LogUtils.d("内容改变");
		}
	}

	private void initFragment() {
		fragmentManager = this.getFragmentManager();
		currentSoundSetting = new CurrentSoundSetting();
		equalizerSetting = new EqualizerSetting();
		speedCompensationSetting = new SpeedCompensationSetting();
		loudnessSetting = new LoudnessSetting();
		
		if(!Utils.isLowDesityMachine(BaseSoundSettingActivity.this))	 {
			
			currentSoundSetting.setOnFragmentImageChangedListener(this);
			currentSoundSetting.setOnFragmentPausedLiatener(this);
			equalizerSetting.setOnFragmentImageChangedListener(this);
			speedCompensationSetting.setOnFragmentImageChangedListener(this);
			loudnessSetting.setOnFragmentImageChangedListener(this);
		}
	}
	
	private void initTouchFadeLayout() {
		currentSoundLayout = (RelativeLayout) findViewById(R.id.current_sound_layout);
		
		resetBt = (Button) findViewById(R.id.sound_reset);
		upBt = (Button) findViewById(R.id.sound_up);
		leftBt = (Button) findViewById(R.id.sound_left);
		downBt = (Button) findViewById(R.id.sound_down);
		rightBt = (Button) findViewById(R.id.sound_right);
		
		touchBt = (DrawView) findViewById(R.id.touch_layout);
		
		touchBt.setOnXYChangeListener(this);

	}
	
	private void initEqualizerLayout() {
		equalizerLayout = (LinearLayout) findViewById(R.id.equalizer_layout);
		initSeekBar();
	}
	
	private void initSeekBar() {
		band1Sb = (VerticalSeekBar) findViewById(R.id.hz63);
		band2Sb = (VerticalSeekBar) findViewById(R.id.hz400);
		band3Sb = (VerticalSeekBar) findViewById(R.id.hz1000);
		band4Sb = (VerticalSeekBar) findViewById(R.id.hz2500);
		band5Sb = (VerticalSeekBar) findViewById(R.id.hz6300);
				
		band1Sb.setMax(BAND_MAX);
		band2Sb.setMax(BAND_MAX);
		band3Sb.setMax(BAND_MAX);
		band4Sb.setMax(BAND_MAX);
		band5Sb.setMax(BAND_MAX);
		
		band1Sb.setUpDownListener(new UpDownListener() {
			
			@Override
			public void onTouch(int progress) {
				EffectUtils.setBand1(BaseSoundSettingActivity.this, progress);
				EffectUtils.setBand1ToFile((progress));
			}
		});
		band2Sb.setUpDownListener(new UpDownListener() {
			
			@Override
			public void onTouch(int progress) {
				EffectUtils.setBand2(BaseSoundSettingActivity.this, progress);
				EffectUtils.setBand2ToFile(progress);
			}
		});
		band3Sb.setUpDownListener(new UpDownListener() {
			
			@Override
			public void onTouch(int progress) {
				EffectUtils.setBand3(BaseSoundSettingActivity.this, progress);
				EffectUtils.setBand3ToFile(progress);
			}
		});
		band4Sb.setUpDownListener(new UpDownListener() {
			
			@Override
			public void onTouch(int progress) {
				EffectUtils.setBand4(BaseSoundSettingActivity.this, progress);
				EffectUtils.setBand4ToFile(progress);
			}
		});
		band5Sb.setUpDownListener(new UpDownListener() {
			
			@Override
			public void onTouch(int progress) {
				EffectUtils.setBand5(BaseSoundSettingActivity.this, progress);
				EffectUtils.setBand5ToFile(progress);
			}
		});
		
	}
	
	@Override
	public void onCheckedChanged(MyRadioGroup group, int checkedId) {
		// hideFragments();
		switch (checkedId) {
		case R.id.currentsoundsettingsetting:
			startFragment(currentSoundSetting, CURRENTSOUNDSETTINGFLAG);
			break;
		case R.id.equalizersettingsetting:
			startFragment(equalizerSetting, EQUALIZERSETTINGFLAG);
			break;
		case R.id.speedcompensationsettingsetting:
			startFragment(speedCompensationSetting,
					SPEEDCOMPENSATIONSETTINGFLAG);
			break;
		case R.id.loudnesssetting:
			startFragment(loudnessSetting, LOUDNESSSETTINGFLAG);
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
				soundSettingHeaders.enabledAllRadioButton();
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
					fragmentTransaction.replace(SOUNDSETTING_FRAGMENT, f, fragmentFlag);
					fragmentTransaction.commit();
					currentFragment = fragmentFlag;
					syncLabel();
				}
			});
			soundSettingHeaders.disEnabledAllRadioButton();
			handler.sendEmptyMessageDelayed(1, 200);
		}
	}
	
	/**
	 * 为了保证按钮与fragment同步
	 */
	private void syncLabel() {
		if(CURRENTSOUNDSETTINGFLAG.equals(currentFragment)) {
			soundSettingHeaders.check(R.id.currentsoundsettingsetting);
			
		} else if(EQUALIZERSETTINGFLAG.equals(currentFragment)) {
			soundSettingHeaders.check(R.id.equalizersettingsetting);
			
		} else if(SPEEDCOMPENSATIONSETTINGFLAG.equals(currentFragment)) {
			soundSettingHeaders.check(R.id.speedcompensationsettingsetting);
			
		} else if(LOUDNESSSETTINGFLAG.equals(currentFragment)) {
			soundSettingHeaders.check(R.id.loudnesssetting);
		}
	}
	

	@Override
	public void onClick(View v) {
		int resId = v.getId();
		switch (resId) {
		case R.id.setting_main_back:
			finish();
			break;
		case R.id.sound_up:
			touchBt.setAddY(false);
			break;
			
		case R.id.sound_left:
			touchBt.setAddX(false);
			break;
			
		case R.id.sound_down:
			touchBt.setAddY(true);
			break;
			
		case R.id.sound_right:
			touchBt.setAddX(true);
			break;
		case R.id.sound_reset:
			resetBalanceAndFade();
			break;
		default:
			break;
		}
	}
	
	@Override
	public void onListChange(int x, int y, boolean fromUser) {
		Log.d("onListChange", "x: " + x + ", y: " + y);
		if(fromUser) {
			syncCurrentFragmentData(x, y);
		} else {
			buttonsyncFragmentDataUp(x, y);
		}
	}
	
	//fragment data return activity
	@Override
	public Object function(Object... data) {
		LogUtils.d("ljwtestfuntion", "get current fragment result data: " + Arrays.asList(data));
		if(data[0] instanceof Integer)
			touchBt.setXY((Integer)data[0], (Integer)data[1]);
		else {
			String type = (String) data[0];
			LogUtils.d("ljwtestfuntion", "get eq fragment result data: " + type);
			showBandValue(type);
		}
		return null;
	}
	
	@Override
	public void onFragmentChanged(String fragmentName) {

		if(fragmentName.equals(CurrentSoundSetting.class.getName())) {
			showCurrentSettingPanel();
			bigImage.setImageResource(R.drawable.soundsetting_current);
		}else if(fragmentName.equals(EqualizerSetting.class.getName())) {
			showEquailizerPanel();
			bigImage.setImageResource(R.drawable.soundsetting_eq);
		} else if(fragmentName.equals(SpeedCompensationSetting.class.getName())) {
			showOtherPanel();
			bigImage.setImageResource(R.drawable.soundsetting_speedconp);
		} else if(fragmentName.equals(LoudnessSetting.class.getName())) {
			showOtherPanel();
			bigImage.setImageResource(R.drawable.soundsetting_eq);
		}
	}
	
	@Override
	public void onFragmentPaused() {
		LogUtils.d("onFragmentPaused");
//		goneCurrentSettingPanel();
	}
	
	private void showBandValue(String type) {
		int[] typeArray = {0, 0, 0, 0, 0,};
		if(type.equals(EffectUtils.CUSTOMER)) {
			typeArray[0] = EffectUtils.getBand1(BaseSoundSettingActivity.this);
			typeArray[1] = EffectUtils.getBand2(BaseSoundSettingActivity.this);
			typeArray[2] = EffectUtils.getBand3(BaseSoundSettingActivity.this);
			typeArray[3] = EffectUtils.getBand4(BaseSoundSettingActivity.this);
			typeArray[4] = EffectUtils.getBand5(BaseSoundSettingActivity.this);
			band1Sb.setProgress(typeArray[0]);
			band2Sb.setProgress(typeArray[1]);
			band3Sb.setProgress(typeArray[2]);
			band4Sb.setProgress(typeArray[3]);
			band5Sb.setProgress(typeArray[4]);
			band1Sb.setEnabled(true);
			band2Sb.setEnabled(true);
			band3Sb.setEnabled(true);
			band4Sb.setEnabled(true);
			band5Sb.setEnabled(true);			
		} else {
			typeArray = EffectUtils.getEqTypeAnother(type);
			band1Sb.setProgress(typeArray[0] + BAND_DIFF);
			band2Sb.setProgress(typeArray[1] + BAND_DIFF);
			band3Sb.setProgress(typeArray[2] + BAND_DIFF);
			band4Sb.setProgress(typeArray[3] + BAND_DIFF);
			band5Sb.setProgress(typeArray[4] + BAND_DIFF);
			band1Sb.setEnabled(false);
			band2Sb.setEnabled(false);
			band3Sb.setEnabled(false);
			band4Sb.setEnabled(false);
			band5Sb.setEnabled(false);
		}
		for(int i : typeArray)
			LogUtils.d("showBandValue get value: " + i);

	}
		
	private void showCurrentSettingPanel() {
		
		//click sound invalid when this view GONE
		resetBt.setOnClickListener(this);
		upBt.setOnClickListener(this);
		leftBt.setOnClickListener(this);
		downBt.setOnClickListener(this);
		rightBt.setOnClickListener(this);
		
		currentSoundLayout.setVisibility(View.VISIBLE);
		bigImage.setVisibility(View.GONE);
		equalizerLayout.setVisibility(View.GONE);
		resetBand();
	}
	
	private void goneCurrentSettingPanel() {
		resetBt.setOnClickListener(null);
		upBt.setOnClickListener(null);
		leftBt.setOnClickListener(null);
		downBt.setOnClickListener(null);
		rightBt.setOnClickListener(null);
	}

	private void showEquailizerPanel() {
		equalizerLayout.setVisibility(View.VISIBLE);
		bigImage.setVisibility(View.GONE);
		currentSoundLayout.setVisibility(View.GONE);
		resetBand();
	}

	private void showOtherPanel() {
		bigImage.setVisibility(View.VISIBLE);
		equalizerLayout.setVisibility(View.GONE);
		currentSoundLayout.setVisibility(View.GONE);
		resetBand();
	}
	
	void resetBand() {
		band1Sb.setProgress(0);
		band2Sb.setProgress(0);
		band3Sb.setProgress(0);
		band4Sb.setProgress(0);
		band5Sb.setProgress(0);
	}
	

	private void hideFragments() {
		Fragment fragment = fragmentManager.findFragmentByTag(currentFragment);
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();
		if (fragment != null)
			fragmentTransaction.hide(fragment);
		fragmentTransaction.commit();
	}
		
	private void syncCurrentFragmentData(int balance, int fade) {
	    Object notify = ObservableManager.newInstance()
	            .notify(FuntionCommon.CURRENT_SOUND_SETTING_RESULT_FRAGMENT, balance,
	                fade);
	}
	
	private void buttonsyncFragmentDataUp(int balance, int fade) {
		if(touchLimitHandler.hasMessages(BUTTON_TOUCH_LIMIT))
			touchLimitHandler.removeMessages(BUTTON_TOUCH_LIMIT);
		touchLimitHandler.sendMessageDelayed(Message.obtain(touchLimitHandler,
				BUTTON_TOUCH_LIMIT, balance, fade), BUTTON_TOUCH_LIMIT_DELAY);
	}
	
	private void resetBalanceAndFade() {
		touchBt.setXY(0, 0);
		syncCurrentFragmentData(0, 0);
	}
	
	private static final int BUTTON_TOUCH_LIMIT = 0x98;
	private static final int BUTTON_TOUCH_LIMIT_DELAY = 150;
	
	private Handler touchLimitHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			LogUtils.d("delay syncFragmentData: " + msg.arg1 + ", " + msg.arg2);
			syncCurrentFragmentData(msg.arg1, msg.arg2);
		}
		
	};
	
}
