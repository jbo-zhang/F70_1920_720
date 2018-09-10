package com.hwatong.settings.preference;

import android.content.Context;
import android.graphics.Color;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.hwatong.settings.R;


public class MyDropDownPreference extends Preference implements View.OnClickListener {
	private static final String TAG = "MyDropDownPreference";

	private Context mContext;
	private ViewGroup mWidgetFrame;
	
	private OnMyDropDownPreferenceClickListener mOnMyDropDownPreferenceClickListener;

	public interface OnMyDropDownPreferenceClickListener {
		public void OnItemClick(View v);
	}

	public void setOnMyDropDownPreferenceClickListener(OnMyDropDownPreferenceClickListener listener) {
		mOnMyDropDownPreferenceClickListener = listener;
	}

	public MyDropDownPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}

	public MyDropDownPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public MyDropDownPreference(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		final LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View layout = layoutInflater.inflate(R.layout.preference_widget_dropdown, parent, false);
        mWidgetFrame = (ViewGroup) layout.findViewById(com.android.internal.R.id.widget_frame);
        if (mWidgetFrame != null) {
            if (getWidgetLayoutResource() != 0) {
                layoutInflater.inflate(getWidgetLayoutResource(), mWidgetFrame);
            } else {
                mWidgetFrame.setVisibility(View.GONE);
            }
        }
		return layout;
	}
	public View findWidgetById(int resid) {
		return (mWidgetFrame==null)?null:mWidgetFrame.findViewById(resid);
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);

		//switch寮�鍏崇殑鐐瑰嚮浜嬩欢
//		if (mSwitch != null) {
//			mSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//			}		
//		mTextView = (TextView)view.findViewById(R.id.textview1);
	}


	@Override
	public void onClick(View v) {
		mOnMyDropDownPreferenceClickListener.OnItemClick(v);
	}
}