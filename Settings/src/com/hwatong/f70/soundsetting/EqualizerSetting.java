package com.hwatong.f70.soundsetting;

import com.hwatong.f70.baseview.BaseFragment;
import com.hwatong.f70.main.LogUtils;
import com.hwatong.f70.observable.Function;
import com.hwatong.f70.observable.ObservableManager;
import com.hwatong.settings.R;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class EqualizerSetting extends BaseFragment implements
		OnCheckedChangeListener, OnClickListener, Function<Object, Object> {

	private RadioGroup effectSelectRg;
	private RadioButton pop_Rb, jazz_Rb, classical_Rb, rock_Rb, human_Rb,
			flat_Rb, customer_Rb;
	private boolean fromUser;
	
	private static final int QE_EFFECT_DIFF = 6;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.f70_equalizer_setting,
				container, false);

		initWidget(rootView);
		return rootView;
	}

	private void initWidget(View rootView) {
		fromUser = false;
		effectSelectRg = (RadioGroup) rootView.findViewById(R.id.fx_select);
		effectSelectRg.setOnCheckedChangeListener(this);

		pop_Rb = (RadioButton) rootView.findViewById(R.id.fx_pop);
		jazz_Rb = (RadioButton) rootView.findViewById(R.id.fx_jazz);
		classical_Rb = (RadioButton) rootView.findViewById(R.id.fx_classical);
		rock_Rb = (RadioButton) rootView.findViewById(R.id.fx_rock);
		human_Rb = (RadioButton) rootView.findViewById(R.id.fx_human);
		flat_Rb = (RadioButton) rootView.findViewById(R.id.fx_flat);
		customer_Rb = (RadioButton) rootView.findViewById(R.id.fx_none);

		pop_Rb.setOnClickListener(this);
		jazz_Rb.setOnClickListener(this);
		classical_Rb.setOnClickListener(this);
		rock_Rb.setOnClickListener(this);
		human_Rb.setOnClickListener(this);
		flat_Rb.setOnClickListener(this);
		customer_Rb.setOnClickListener(this);

	}

	@Override
	public void onResume() {
		super.onResume();
		changedActivityImage(this.getClass().getName());
//		initEffectRadioGroup();
		handler.sendEmptyMessageDelayed(0x01, DELAY_INIT_TIME);
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		if (!hidden) {
		} else {
		}
		super.onHiddenChanged(hidden);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	
	private void initEffectRadioGroup() {
		Activity activity = getActivity();
		String currentModeName = "";
		if(activity != null) {			
			currentModeName = EffectUtils.getCarSettingsString(activity
					.getContentResolver(), EffectUtils.EQUALIZER_MODE);
		}
		LogUtils.d("initEffectRadioGroup: " + currentModeName);
		if (!TextUtils.isEmpty(currentModeName)) {
			if (currentModeName.equals(EffectUtils.POP))
				effectSelectRg.check(R.id.fx_pop);
			else if (currentModeName.equals(EffectUtils.JAZZ))
				effectSelectRg.check(R.id.fx_jazz);
			else if (currentModeName.equals(EffectUtils.CLASSICAL))
				effectSelectRg.check(R.id.fx_classical);
			else if (currentModeName.equals(EffectUtils.ROCK))
				effectSelectRg.check(R.id.fx_rock);
			else if (currentModeName.equals(EffectUtils.HUMAN))
				effectSelectRg.check(R.id.fx_human);
			else if (currentModeName.equals(EffectUtils.FLAT))
				effectSelectRg.check(R.id.fx_flat);
			else if (currentModeName.equals(EffectUtils.CUSTOMER))
				effectSelectRg.check(R.id.fx_none);
		} else {
			currentModeName = EffectUtils.FLAT;
			effectSelectRg.check(R.id.fx_flat);
		}
		syncActivityData(currentModeName);
		fromUser = true;
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if (!fromUser)
			return;
		LogUtils.d("onCheckedChanged");
		String type = "";
		switch (checkedId) {
		case R.id.fx_pop:
			type = EffectUtils.POP;
			break;

		case R.id.fx_jazz:
			type = EffectUtils.JAZZ;
			break;

		case R.id.fx_classical:
			type = EffectUtils.CLASSICAL;
			break;

		case R.id.fx_rock:
			type = EffectUtils.ROCK;
			break;

		case R.id.fx_human:
			type = EffectUtils.HUMAN;
			break;

		case R.id.fx_flat:
			type = EffectUtils.FLAT;

			break;

		case R.id.fx_none:
			type = EffectUtils.CUSTOMER;
			break;

		default:
			break;
		}
		setEQ(type);
	}

	private void setEQ(String type) {
		LogUtils.d("custom bass: " + EffectUtils.getCustomBass()
				+ ", custom mid: " + EffectUtils.getCustomMid()
				+ ", custom treble: "
				+ EffectUtils.getCustomTreble());
		
		Activity activity = getActivity();
		
		if(activity == null) {
			LogUtils.d("activity is null !");
			return;
		}
		
		EffectUtils.putCarSettingsString(activity.getContentResolver(),
				EffectUtils.EQUALIZER_MODE, type);
		LogUtils.d("type: " + type);
		if (type.equals(EffectUtils.CUSTOMER)) {
			int[] customer = {0, 0, 0, 0, 0};
			if(activity != null) {
				customer[0] = EffectUtils.getBand1(activity);
				customer[1] = EffectUtils.getBand2(activity);
				customer[2] = EffectUtils.getBand3(activity);
				customer[3] = EffectUtils.getBand4(activity);
				customer[4] = EffectUtils.getBand5(activity);
				
				for(int i : customer)
					LogUtils.d("customer get: " + i);
				EffectUtils.setBand1ToFile(customer[0]);
				EffectUtils.setBand2ToFile(customer[1]);
				EffectUtils.setBand3ToFile(customer[2]);
				EffectUtils.setBand4ToFile(customer[3]);
				EffectUtils.setBand5ToFile(customer[4]);
			}
		} else {
			EffectUtils.setEqAnother(type); 
		}
//			EffectUtils.setBassValue(EffectUtils.getCustomBass());
//			EffectUtils.setMidValue(EffectUtils.getCustomMid());
//			EffectUtils.setTrebleValue(EffectUtils
//					.getCustomTreble());
//			saveCurrentEffectValueAfterExit(
//					EffectUtils.getCustomBass(),
//					EffectUtils.getCustomMid(),
//					EffectUtils.getCustomTreble());
//		} else {
//			EffectUtils.setBassValue(0);
//			EffectUtils.setMidValue(0);
//			EffectUtils.setTrebleValue(0);
//		}
//		else { 
//			int[] settingMode = EffectUtils.getEffect(type);
//			EffectUtils.setBassValue(settingMode[EffectUtils.BASS_POSITON]);
//			EffectUtils.setMidValue(settingMode[EffectUtils.MID_POSITON]);
//			EffectUtils.setTrebleValue(settingMode[EffectUtils.TREBLE_POSITON]);
//			saveCurrentEffectValueAfterExit(
//					settingMode[EffectUtils.BASS_POSITON],
//					settingMode[EffectUtils.MID_POSITON],
//					settingMode[EffectUtils.TREBLE_POSITON]);
			
//		}
		syncActivityData(type);
		
//		saveCurrentEqModeAfterExit(type);//D��?o��?
	}
	
	@Override
	public Object function(Object... data) {
		return null;
	}

	@Override
	public void onClick(View v) {
		int resId = v.getId();
		switch (resId) {
		case R.id.fx_pop:

			break;

		case R.id.fx_jazz:

			break;

		case R.id.fx_classical:

			break;

		case R.id.fx_rock:

			break;

		case R.id.fx_human:

			break;

		case R.id.fx_flat:

			break;

		case R.id.fx_none:

			break;

		default:
			break;
		}
	}

	/**
	 *
	 */
	private void saveCurrentEffectValueAfterExit(int bass, int mid, int treble) {
		LogUtils.d("save bass: " + bass + ", save mid: " + mid
				+ ", save treble: " + treble);
		EffectUtils.setCurrentBass(bass);
		EffectUtils.setCurrentMid(mid);
		EffectUtils.setCurrentTreble(treble);
	}
	
	/**
	 *
	 */
	private void saveCurrentEqModeAfterExit(String type) {
		LogUtils.d("save mode: " + type);
		EffectUtils.setCurrentEqAnother(type);
	}
	
	private void syncActivityData(String type) {
	    Object notify = ObservableManager.newInstance()
	            .notify(FuntionCommon.CURRENT_SOUND_SETTING_RESULT_ACTIVITY, type);
	}
	
	static final int DELAY_INIT_TIME = 50;
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			initEffectRadioGroup();
		}
		
	};

}
