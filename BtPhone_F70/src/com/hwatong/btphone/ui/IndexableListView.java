/*
 * Copyright 2011 woozzu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hwatong.btphone.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListAdapter;


public class IndexableListView extends PopItemButtonListView {

	private boolean mIsFastScrollEnabled = false;
	private IndexScroller mScroller = null;

	public IndexableListView(Context context) {
		super(context);
	}

	public IndexableListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (mScroller == null)
			mScroller = new IndexScroller(getContext(), this);
	}

	public IndexableListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean isFastScrollEnabled() {
		return mIsFastScrollEnabled;
	}

	@Override
	public void setFastScrollEnabled(boolean enabled) {
		mIsFastScrollEnabled = enabled;
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);

		// Overlay index bar
		if (mScroller != null)
			mScroller.draw(canvas);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:

			if (mItemView != null && mScroller != null && mScroller.contains(ev.getX(), ev.getY())) {
				ViewHolder holder = (ViewHolder) mItemView.getTag();
				hideButton(holder);
				return true;
			}

			break;
		case MotionEvent.ACTION_MOVE:
			
			break;
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (mScroller != null && mScroller.contains(ev.getX(), ev.getY()))
			return true;

		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {

		if (mOnTouchListener != null && mOnTouchListener.onTouch(null, ev)) {
			return true;
		}
		// Intercept ListView's touch event
		if (mScroller != null && mScroller.onTouchEvent(ev))
			return true;

		return super.onTouchEvent(ev);
	}

	private OnTouchListener mOnTouchListener;

	public void setTouchEvent(OnTouchListener listener) {
		this.mOnTouchListener = listener;
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);
		if (mScroller != null)
			mScroller.setAdapter(adapter);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (mScroller != null)
			mScroller.onSizeChanged(w, h, oldw, oldh);
	}

}
