package com.hwatong.btphone.ui;

import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

/**
 * 用于限制在一定时间内，重复点击不响应操作
 * 
 * @author zxy time:2017年6月1日
 * 
 */
public abstract class NoDoubleItemClickListener implements
		AdapterView.OnItemClickListener {
	private static final int DEFAULT_CLICK_INTERVAL = 250;// 时间间隔，单位毫秒
	
	private long mLastClickTime;

	private long mTimeInterval;//单位毫秒

	public NoDoubleItemClickListener() {
		super();
		mTimeInterval = DEFAULT_CLICK_INTERVAL;
	}

	public NoDoubleItemClickListener(int mTimeInterval) {
		super();
		this.mTimeInterval = mTimeInterval;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		long curTime = SystemClock.uptimeMillis();
		Log.d("AAA", "NoDoubleItemClickListener:"+(curTime - mLastClickTime));
		if (curTime - mLastClickTime < mTimeInterval) {
			Log.d("AAA", "NoDoubleItemClickListener");
			return;
		}
		mLastClickTime = curTime;

		onItemClickImp(parent, view, position, id);
	}

	public abstract void onItemClickImp(AdapterView<?> parent, View view,
			int position, long id);

}
