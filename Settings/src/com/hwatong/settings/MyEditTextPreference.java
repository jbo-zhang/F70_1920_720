package com.hwatong.settings;

import com.hwatong.settings.R;

import android.content.Context;
import android.graphics.Color;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class MyEditTextPreference extends EditTextPreference{
	private static final String TAG = "MyEditTextPreference";

	private Context mContext;
	private TextView mTextView;

	public MyEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}

	public MyEditTextPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public MyEditTextPreference(Context context) {
		super(context);
		mContext = context;
	}


	@Override
	protected View onCreateView(ViewGroup parent) {
		// TODO Auto-generated method stub
		setWidgetLayoutResource(R.layout.preference_widget_textview);
		return super.onCreateView(parent);
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		mTextView = (TextView)view.findViewById(R.id.textview1);
		if (mTextView!=null) mTextView.setText(getText());
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		// TODO Auto-generated method stub
		super.onDialogClosed(positiveResult);
		if (mTextView!=null) mTextView.setText(getText());
	}

	@Override
	public void setText(String text) {
		// TODO Auto-generated method stub
		super.setText(text);
		if (mTextView!=null) mTextView.setText(text);
	}
}