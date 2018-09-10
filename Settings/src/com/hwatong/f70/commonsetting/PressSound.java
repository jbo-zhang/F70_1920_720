package com.hwatong.f70.commonsetting;

import com.hwatong.f70.baseview.BaseFragment;
import com.hwatong.f70.main.LogUtils;
import com.hwatong.settings.R;
import com.hwatong.settings.widget.SwitchButton;

import android.app.Fragment;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class PressSound extends BaseFragment implements OnCheckedChangeListener{

	private SwitchButton pressSoundSwitch;
	private AudioManager mAudioManager;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.f70_presssound, container,
				false);
		mAudioManager = (AudioManager) getActivity().getSystemService(
				Context.AUDIO_SERVICE);

		pressSoundSwitch = (SwitchButton) rootView
				.findViewById(R.id.presssound);
		pressSoundSwitch.setOnCheckedChangeListener(this);
		initPressSound();

		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		changedActivityImage(this.getClass().getName());
	}

	private void initPressSound() {
		int value = 0;
		try {
			value = Settings.System.getInt(getActivity().getContentResolver(),
					Settings.System.SOUND_EFFECTS_ENABLED);
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
			LogUtils.d("set PressSound Error: " + e.toString());
		}

		pressSoundSwitch.setChecked(value == 1 ? true : false);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onCheckedChanged(CompoundButton checkBox, boolean isCheck) {
		
		Message.obtain(handler, MSG_PRESSSOUND, isCheck).sendToTarget();
		
		Settings.System.putInt(getActivity().getContentResolver(),
				Settings.System.SOUND_EFFECTS_ENABLED, isCheck ? 1 : 0);
		
	}
	
	private static final int MSG_PRESSSOUND = 0x08;
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			boolean isChecked = (Boolean) msg.obj;
			if (isChecked) {
				mAudioManager.loadSoundEffects();
			} else {
				mAudioManager.unloadSoundEffects();
			}
		}
		
	};
}
