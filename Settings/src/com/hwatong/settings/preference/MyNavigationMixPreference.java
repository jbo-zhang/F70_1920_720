package com.hwatong.settings.preference;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.hwatong.settings.R;
import com.hwatong.settings.Utils;
import com.hwatong.providers.carsettings.SettingsProvider;


public class MyNavigationMixPreference extends Preference implements android.widget.RadioGroup.OnCheckedChangeListener {
	private static final String TAG = "MyNavigationMixPreference";

	private Context mContext;
	private RadioGroup mNaviMix;
	private int RES_ID_RADIO[] = new int [] {R.id.rb_navi_mix_close, R.id.rb_navi_mix_open, R.id.rb_navi_mix_voice};
	public MyNavigationMixPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}

	public MyNavigationMixPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public MyNavigationMixPreference(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		final LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View layout = layoutInflater.inflate(R.layout.preference_navigation_mix, parent, false);
		return layout;
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		mNaviMix = (RadioGroup)view.findViewById(R.id.rg_navi_mix);
		mNaviMix.setOnCheckedChangeListener(this);
		updateData();
	}

	private boolean fromTouch=true;
	private void updateData() {
		fromTouch=false;
		String select = Utils.getCarSettingsString(getContext().getContentResolver(), SettingsProvider.SOUND_NAVI_MIX, SettingsProvider.DEFAULT_NAVI_MIX);
		mNaviMix.check(RES_ID_RADIO[Integer.valueOf(select)]);
		fromTouch=true;
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if (!fromTouch) return;
		switch(checkedId) {
		case R.id.rb_navi_mix_close:
			Utils.putCarSettingsString(getContext().getContentResolver(), SettingsProvider.SOUND_NAVI_MIX, "0");
			break;
		case R.id.rb_navi_mix_open:
			Utils.putCarSettingsString(getContext().getContentResolver(), SettingsProvider.SOUND_NAVI_MIX, "1");
			break;
		case R.id.rb_navi_mix_voice:
			Utils.putCarSettingsString(getContext().getContentResolver(), SettingsProvider.SOUND_NAVI_MIX, "2");
			break;
		}
	}
}
