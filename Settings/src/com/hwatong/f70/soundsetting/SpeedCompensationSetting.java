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

public class SpeedCompensationSetting extends BaseFragment implements
		OnCheckedChangeListener {
	SwitchButton speedCompensationSwitch;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.f70_speedconpent_setting,
				container, false);

		initWidget(rootView);
		return rootView;
	}

	@Override
	public void onResume() {
		initSpeedCompensation();
		super.onResume();
		changedActivityImage(this.getClass().getName());
	}

	private void initWidget(View rootView) {
		speedCompensationSwitch = (SwitchButton) rootView
				.findViewById(R.id.speedconpent);
		speedCompensationSwitch.setOnCheckedChangeListener(this);
	}

	
	private void initSpeedCompensation() {
		LogUtils.d("SpeedCompensation: "
				+ Utils.getCarSettingsString(
						getActivity().getContentResolver(),
						SettingsProvider.SOUND_AUTO_VOLUME));
		boolean isOpen = Utils.getCarSettingsString(
				getActivity().getContentResolver(),
				SettingsProvider.SOUND_AUTO_VOLUME).equals(
				F70CarSettingCommand.LOCAL_OPEN);

		speedCompensationSwitch.setChecked(isOpen);
	}

	@Override
	public void onCheckedChanged(CompoundButton button, boolean isChecked) {
		Utils.putCarSettingsString(getActivity().getContentResolver(),
				SettingsProvider.SOUND_AUTO_VOLUME,
				isChecked ? F70CarSettingCommand.LOCAL_OPEN
						: F70CarSettingCommand.LOCAL_CLOSE);
	}

}
