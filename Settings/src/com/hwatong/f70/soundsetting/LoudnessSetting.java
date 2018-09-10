package com.hwatong.f70.soundsetting;

import com.hwatong.f70.baseview.BaseFragment;
import com.hwatong.f70.carsetting.F70CarSettingCommand;
import com.hwatong.f70.main.LogUtils;
import com.hwatong.providers.carsettings.SettingsProvider;
import com.hwatong.settings.R;
import com.hwatong.settings.Utils;
import com.hwatong.settings.widget.SwitchButton;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class LoudnessSetting extends BaseFragment implements
		OnCheckedChangeListener {

	private SwitchButton loudnessSwitch;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.f70_loudness_setting,
				container, false);
		initWidget(rootView);
		return rootView;
	}

	@Override
	public void onResume() {
		initLoudness();
		super.onResume();
		changedActivityImage(this.getClass().getName());
	}
	
	@Override
	public void onStop() {
		super.onStop();
		LogUtils.d("onStop");
	}
	
	@Override
	public void onPause() {
		super.onPause();
		LogUtils.d("onPause");
	}

	private void initWidget(View rootView) {
		loudnessSwitch = (SwitchButton) rootView.findViewById(R.id.loudness);
		loudnessSwitch.setOnCheckedChangeListener(this);
	}

	private void initLoudness() {
//		LogUtils.d("Loudness: "
//				+ Utils.getCarSettingsString(
//						getActivity().getContentResolver(),
//						SettingsProvider.LOUDNESS_ENABLED));
		LogUtils.d("Loudness: "
				+ EffectUtils.getLoudnessValue());
//		boolean isOpen = Utils.getCarSettingsString(
//				getActivity().getContentResolver(),
//				SettingsProvider.LOUDNESS_ENABLED).equals(
//				F70CarSettingCommand.LOCAL_OPEN);
		
		boolean isOpen = EffectUtils.getLoudnessValue().equals(F70CarSettingCommand.LOCAL_OPEN);
		loudnessSwitch.setOnCheckedChangeListener(null);
		
		loudnessSwitch.setChecked(isOpen);
		
		loudnessSwitch.setOnCheckedChangeListener(this);
	} 

	@Override
	public void onCheckedChanged(CompoundButton button, boolean isChecked) {
		Utils.putCarSettingsString(getActivity().getContentResolver(),
				SettingsProvider.LOUDNESS_ENABLED,
				isChecked ? F70CarSettingCommand.LOCAL_OPEN
						: F70CarSettingCommand.LOCAL_CLOSE);
		LogUtils.d("loudness is changed: " + isChecked);
		EffectUtils.setLoudnessValue(isChecked ? 1 : 0);
//		EffectUtils.setCurrentLoudness(isChecked ? 1 : 0);
	}

}
