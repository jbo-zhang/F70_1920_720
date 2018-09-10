package com.hwatong.settings.preference;


import com.hwatong.settings.R;
import com.hwatong.settings.widget.MyRadioGroup;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RadioButton;

public class SelectWifiPreference extends Preference implements OnClickListener, com.hwatong.settings.widget.MyRadioGroup.OnCheckedChangeListener{
	private Context context;
	private MyRadioGroup selectWifiRg;
	private RadioButton selectWifi, selectWifiAp;
	private int which = -1;
	public SelectWifiPreference(Context context) {
		super(context);
		this.context = context;
	}
	
	public SelectWifiPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
	}

	public SelectWifiPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}
	
	@Override
	protected View onCreateView(ViewGroup parent) {
		final LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View rootView = layoutInflater.inflate(R.layout.preference_select_wifi, parent, false);
		initWidgets(rootView);
		return rootView;
	}
	
	private void initWidgets(View rootView) {
		selectWifiRg = (MyRadioGroup) rootView.findViewById(R.id.selectwifirg);
		selectWifi = (RadioButton) rootView.findViewById(R.id.selectwifi);
		selectWifiAp = (RadioButton) rootView.findViewById(R.id.selectwifiap);
		if(which != -1)
			selectWifiRg.check(which == 0 ? R.id.selectwifiap : R.id.selectwifi);
		selectWifiRg.setOnCheckedChangeListener(this);
		
		selectWifi.setOnClickListener(this);
		selectWifiAp.setOnClickListener(this);
	}
	
	public void update(int which) {
		this.which = which;
	}
	
	private OnSelectWifiListener listener;
	public interface OnSelectWifiListener {
    	void onSearchFinish(int which);
    }
	
	public void setOnSelectWifiListener(OnSelectWifiListener onSelectWifiListener) {
		this.listener = onSelectWifiListener;
	}

	@Override
	public void onClick(View v) {
		
	}

	@Override
	public void onCheckedChanged(MyRadioGroup group, int checkedId) {
		int which = -1;
		switch (checkedId) {
		case R.id.selectwifi:
			which = 1;
			break;

		case R.id.selectwifiap:
			which = 0;
			break;
		}
		
		if(listener != null)
			listener.onSearchFinish(which);
	}

}
