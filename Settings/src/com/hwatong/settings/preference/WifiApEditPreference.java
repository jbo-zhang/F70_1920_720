package com.hwatong.settings.preference;

import com.hwatong.f70.main.LogUtils;
import com.hwatong.settings.R;

import android.content.Context;
import android.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class WifiApEditPreference extends Preference{
	private String content;
	private TextView contentTv;
	private boolean isAlreadyListener = false;

	public WifiApEditPreference(Context context) {
		this(context, null);
//		setLayoutResource(R.layout.preference_wifiap_edittext);
	}

//	public WifiApEditPreference(Context context, AttributeSet attrs, int defStyle) {
//		super(context, attrs, defStyle);
//		setLayoutResource(R.layout.preference_wifiap_edittext);
//	}

	public WifiApEditPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setLayoutResource(R.layout.preference_wifiap_edittext);	
	}
	
	@Override
	protected View onCreateView(ViewGroup parent) {
		View v = super.onCreateView(parent);
		contentTv = (TextView) v.findViewById(R.id.content);
		return v;
	}
	
	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		if(!TextUtils.isEmpty(content))
			contentTv.setText(content);
		
	}
	
	public void updateTitle(String title) {
		setTitle(title);
	}
	
	public void notifyViewChanged() {
		this.notifyChanged();
	}
	
	public void updateContent(String s) {
		this.content = s;
	}
	
	public String getContent() {
		return this.content;
	}
}
