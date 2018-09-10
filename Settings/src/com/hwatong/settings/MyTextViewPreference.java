package com.hwatong.settings;

import com.hwatong.settings.R;

import android.content.Context;
import android.graphics.Color;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class MyTextViewPreference extends Preference{
	private static final String TAG = "MyTextViewPreference";

	private Context mContext;
	private TextView mTextView;

	public MyTextViewPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}

	public MyTextViewPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public MyTextViewPreference(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		setWidgetLayoutResource(R.layout.preference_widget_textview);
		return super.onCreateView(parent);
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		mTextView = (TextView)view.findViewById(R.id.textview1);
	}

	public void setText(String text) {
		Log.d(TAG, "setText: text="+text+" , mTextView="+mTextView);
		if (mTextView!=null) {mTextView.setText(text);}
	}
	
	public void setText(int resId) {
		if (mTextView!=null) mTextView.setText(resId);
	}
	
	public String getText() { return (mTextView==null)?"":mTextView.getText().toString();}
}