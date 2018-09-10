package com.hwatong.settings.preference;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.hwatong.settings.R;


public class MyWifiSearch extends Preference implements View.OnClickListener {
	private static final String TAG = "MyWifiSearch";

	private Context mContext;
	private Button mButton;
	private TextView mTextView; 

	private boolean searching=false;
	
	private OnSearchClickListener mOnSearchClickListener;

	public interface OnSearchClickListener {
		public void OnClick(View v);
	}

	public void setOnSearchClickListener(OnSearchClickListener listener) {
		mOnSearchClickListener = listener;
	}

	public MyWifiSearch(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}

	public MyWifiSearch(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public MyWifiSearch(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		final LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View layout = layoutInflater.inflate(R.layout.preference_wifi_search, parent, false);
		mTextView = (TextView)layout.findViewById(R.id.tv_search);
//        mButton = (Button) layout.findViewById(R.id.btn_search);
//        mButton.setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				Log.d(TAG, "Button Frame onClick: "+v.getId());
//				mOnSearchClickListener.OnClick(v);
//			}
//		});
		Log.d(TAG, "onCreateView: button " + isEnabled());
//        mButton.setEnabled(isEnabled());
//        updateWidgetFrame();
		return layout;
	}

	private void updateWidgetFrame() {
		if (searching) {
			if (mButton!=null) mButton.setVisibility(View.GONE);
			if (mTextView!=null) mTextView.setVisibility(View.VISIBLE);
		}else {
			if (mButton!=null) mButton.setVisibility(View.VISIBLE);
			if (mTextView!=null) mTextView.setVisibility(View.GONE);
		}
	}
	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
	}

	public void setSearching(boolean searching) {
		this.searching=searching;
        updateWidgetFrame();
	}

	
	@Override
	public void setEnabled(boolean enabled) {
		Log.d(TAG, "setEnabled: button " + mButton + " enabled " + enabled);
		super.setEnabled(enabled);
		if (mButton != null) 
			mButton.setEnabled(enabled);
	}

	@Override
	public void onClick(View v) {
	}
}
