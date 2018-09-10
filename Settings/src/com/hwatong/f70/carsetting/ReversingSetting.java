package com.hwatong.f70.carsetting;


import com.hwatong.f70.baseview.BaseFragment;
import com.hwatong.f70.main.LogUtils;
import com.hwatong.providers.carsettings.SettingsProvider;
import com.hwatong.settings.R;
import com.hwatong.settings.Utils;
import com.hwatong.settings.widget.SwitchButton;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class ReversingSetting extends BaseFragment implements
		OnCheckedChangeListener,
		android.widget.CompoundButton.OnCheckedChangeListener, OnClickListener {
	private SwitchButton muteSwitch;
	private RadioGroup setMuteRg;
	private RadioButton muteHight, muteMiddle, muteLow;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.f70_reversingsetting,
				container, false);

		initWidget(rootView);
		return rootView;
	}

	@Override
	public void onResume() {
		initReversingSetting();
		super.onResume();
		changedActivityImage(this.getClass().getName());
	}

	private void initWidget(View rootView) {
		setMuteRg = (RadioGroup) rootView.findViewById(R.id.setmute);
		setMuteRg.setOnCheckedChangeListener(this);

		muteHight = (RadioButton) rootView.findViewById(R.id.mute_hight);
		muteMiddle = (RadioButton) rootView.findViewById(R.id.mute_middle);
		muteLow = (RadioButton) rootView.findViewById(R.id.mute_low);
		
		muteHight.setOnClickListener(this);
		muteMiddle.setOnClickListener(this);
		muteLow.setOnClickListener(this);

		muteSwitch = (SwitchButton) rootView
				.findViewById(R.id.switch_reversing_mute);
//		muteSwitch.setNoNeedAutoFeedback(true);
		muteSwitch.setOnCheckedChangeListener(this);
	}

	
	private void initReversingSetting() {
		LogUtils.d("Reversing level: "
				+ Utils.getCarSettingsString(
						getActivity().getContentResolver(),
						SettingsProvider.REVERSE_VOLUME_LEVEL)
				+ ", ReversingSettingStatus"
				+ Utils.getCarSettingsString(
						getActivity().getContentResolver(),
						SettingsProvider.REVERSE_VOLUME_ENABLED));

		boolean isOpen = Utils.getCarSettingsString(
				getActivity().getContentResolver(),
				SettingsProvider.REVERSE_VOLUME_ENABLED).equals(
				F70CarSettingCommand.LOCAL_OPEN);

		String level = Utils.getCarSettingsString(getActivity()
				.getContentResolver(), SettingsProvider.REVERSE_VOLUME_LEVEL);

		muteSwitch.setChecked(isOpen);
		setRadioGroupEnabled(isOpen);

		if (!TextUtils.isEmpty(level)) {
			if (level.equals(F70CarSettingCommand.LOW))
				setMuteRg.check(R.id.mute_low);
			else if (level.equals(F70CarSettingCommand.MIDDLE))
				setMuteRg.check(R.id.mute_middle);
			else
				setMuteRg.check(R.id.mute_hight);
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int resId) {
		LogUtils.d("change REVERSE VOLUME level");
		switch (resId) {
		case R.id.mute_hight:
			Utils.putCarSettingsString(getActivity().getContentResolver(),
					SettingsProvider.REVERSE_VOLUME_LEVEL,
					F70CarSettingCommand.HIGHT);
			break;

		case R.id.mute_middle:
			Utils.putCarSettingsString(getActivity().getContentResolver(),
					SettingsProvider.REVERSE_VOLUME_LEVEL,
					F70CarSettingCommand.MIDDLE);
			break;

		case R.id.mute_low:
			Utils.putCarSettingsString(getActivity().getContentResolver(),
					SettingsProvider.REVERSE_VOLUME_LEVEL,
					F70CarSettingCommand.LOW);
			break;

		default:
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		Utils.putCarSettingsString(getActivity().getContentResolver(),
				SettingsProvider.REVERSE_VOLUME_ENABLED,
				isChecked ? F70CarSettingCommand.LOCAL_OPEN
						: F70CarSettingCommand.LOCAL_CLOSE);
		LogUtils.d("change REVERSE VOLUME status");
		setRadioGroupEnabled(isChecked);
	}
	
	private void setRadioGroupEnabled(boolean isEnabled) {
        for (int i = 0; i < setMuteRg.getChildCount(); i++) {
        	setMuteRg.getChildAt(i).setEnabled(isEnabled);
        }
	}

	@Override
	public void onClick(View v) {
		
	}
}
