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


public class MyWidgetTextPreference extends Preference implements View.OnClickListener {
	private static final String TAG = "MyWidgetTextPreference";

	private Context mContext;
	private ViewGroup mWidgetFrame;
	private TextView mTextView; 
	
	private OnMyWidgetTextPreferenceClickListener mOnMyWidgetTextPreferenceClickListener;

	public interface OnMyWidgetTextPreferenceClickListener {
		public void OnItemClick(View v);
	}

	public void setOnMyWidgetTextPreferenceClickListener(OnMyWidgetTextPreferenceClickListener listener) {
		mOnMyWidgetTextPreferenceClickListener = listener;
	}

	public MyWidgetTextPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}

	public MyWidgetTextPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public MyWidgetTextPreference(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		final LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View layout = layoutInflater.inflate(R.layout.preference_widget_widgettext, parent, false);
		mTextView = (TextView)layout.findViewById(com.android.internal.R.id.summary);
        mWidgetFrame = (ViewGroup) layout.findViewById(com.android.internal.R.id.widget_frame);
        if (mWidgetFrame != null) {
            if (getWidgetLayoutResource() != 0) {
                layoutInflater.inflate(getWidgetLayoutResource(), mWidgetFrame);
            } else {
                mWidgetFrame.setVisibility(View.GONE);
            }
        }
        updateWidgetFrame();
		return layout;
	}

	private void updateWidgetFrame() {
		if (mWidgetFrame==null) return;
		
		String summary = (String) getSummary();
		if (summary==null || "".equals(summary)) {
			mWidgetFrame.setVisibility(View.VISIBLE);
			mTextView.setVisibility(View.GONE);
		}else {
			mWidgetFrame.setVisibility(View.GONE);
			mTextView.setVisibility(View.VISIBLE);
		}
	}
	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
	}

	public View findWidgetById(int resid) {
		return (mWidgetFrame==null)?null:mWidgetFrame.findViewById(resid);
	}

	@Override
	public void setSummary(CharSequence summary) {
		super.setSummary(summary);
        updateWidgetFrame();
	}

	@Override
	public void setSummary(int summaryResId) {
		super.setSummary(summaryResId);
        updateWidgetFrame();
	}

	@Override
	public void onClick(View v) {
		mOnMyWidgetTextPreferenceClickListener.OnItemClick(v);
	}
}