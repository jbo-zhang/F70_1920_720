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



import com.hwatong.btphone.ui.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.widget.Adapter;
import android.widget.ListView;
import android.widget.SectionIndexer;

public class IndexScroller {

	private float mIndexbarWidth;
	private float mIndexbarMargin;
	private float mPreviewPadding;
	private float mDensity;
	private float mScaledDensity;
	private float mAlphaRate = 1.0f;
	private float mBigTextSize;
	private float mTextSize;

	private int mListViewWidth;
	private int mListViewHeight;
	private int mCurrentSection = -1;
	private boolean mIsIndexing = false;
	private ListView mListView = null;
	private SectionIndexer mIndexer = null;
	private String[] mSections = null;
	private RectF mIndexbarRect;
	private int mColorTxtHl;

	public IndexScroller(Context context, ListView lv) {
		mDensity = context.getResources().getDisplayMetrics().density;
		mScaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
		mListView = lv;
		setAdapter(mListView.getAdapter());

		mIndexbarWidth = 40 * mDensity;
		mIndexbarMargin = 5 * mDensity;
		mPreviewPadding = 5 * mDensity;
		mBigTextSize = 50 * mScaledDensity;
		mTextSize = 12 * mScaledDensity;

		mColorTxtHl = context.getResources().getColor(R.color.txt_hl);
	}

	public void draw(Canvas canvas) {
		if (mSections != null && mSections.length > 0) {
			// Preview is shown when mCurrentSection is set
			if (mCurrentSection >= 0) {
				Paint indexbarPaint = new Paint();
				indexbarPaint.setColor(Color.GRAY);
				indexbarPaint.setAlpha((int) (64 * mAlphaRate));
				indexbarPaint.setAntiAlias(true);
				canvas.drawRoundRect(mIndexbarRect, 5 * mDensity, 5 * mDensity, indexbarPaint);

				// Paint previewPaint = new Paint();
				// previewPaint.setColor(Color.GRAY);
				// previewPaint.setAlpha(96);
				// previewPaint.setAntiAlias(true);
				// previewPaint.setShadowLayer(3, 0, 0, Color.argb(64, 0, 0,
				// 0));

				Paint previewTextPaint = new Paint();
				previewTextPaint.setColor(mColorTxtHl);
				previewTextPaint.setAntiAlias(true);
				previewTextPaint.setTextSize(mBigTextSize * mScaledDensity);
				previewTextPaint.setTypeface(Typeface.DEFAULT_BOLD);

				float previewTextWidth = previewTextPaint.measureText(mSections[mCurrentSection]);
				float previewSize = 2 * mPreviewPadding + previewTextPaint.descent() - previewTextPaint.ascent();
				RectF previewRect = new RectF((mListViewWidth - mIndexbarWidth - mIndexbarMargin - previewSize),
						(mListViewHeight - previewSize) / 2,
						(mListViewWidth - mIndexbarWidth - mIndexbarMargin - previewSize) + previewSize,
						(mListViewHeight - previewSize) / 2 + previewSize);
				// canvas.drawRoundRect(previewRect, 5 * mDensity, 5 * mDensity,
				// previewPaint);
				canvas.drawText(mSections[mCurrentSection],
						previewRect.left + (previewSize - previewTextWidth) / 2 - 1, previewRect.top + mPreviewPadding
								- previewTextPaint.ascent() + 1, previewTextPaint);
			}

			Paint indexPaint = new Paint();
			indexPaint.setColor(Color.WHITE);
			indexPaint.setAlpha((int) (255 * mAlphaRate));
			indexPaint.setAntiAlias(true);
			indexPaint.setTextSize(mTextSize * mScaledDensity);

			float sectionHeight = (mIndexbarRect.height() - 2 * mIndexbarMargin) / mSections.length;
			float paddingTop = (sectionHeight - (indexPaint.descent() - indexPaint.ascent())) / 2;
			for (int i = 0; i < mSections.length; i++) {
				if (i == mCurrentSection)
					indexPaint.setColor(mColorTxtHl);
				else
					indexPaint.setColor(Color.LTGRAY);

				float paddingLeft = (mIndexbarWidth - indexPaint.measureText(mSections[i])) / 2;
				canvas.drawText(mSections[i], mIndexbarRect.left + paddingLeft, mIndexbarRect.top + mIndexbarMargin
						+ sectionHeight * i + paddingTop - indexPaint.ascent(), indexPaint);
			}
		}
	}

	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// If down event occurs inside index bar region, start indexing
			if (contains(ev.getX(), ev.getY())) {
				// It demonstrates that the motion event started from index bar
				mIsIndexing = true;
				// Determine which section the point is in, and move the list to
				// that section
				mCurrentSection = getSectionByPoint(ev.getY());
				mListView.setSelection(mIndexer.getPositionForSection(mCurrentSection));
				return true;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (mIsIndexing) {
				// If this event moves inside index bar
				if (contains(ev.getX(), ev.getY())) {
					// Determine which section the point is in, and move the
					// list to that section
					mCurrentSection = getSectionByPoint(ev.getY());
					mListView.setSelection(mIndexer.getPositionForSection(mCurrentSection));
				}
				return true;
			}
			break;
		case MotionEvent.ACTION_UP:
			if (mIsIndexing) {
				mIsIndexing = false;
				mCurrentSection = -1;
			}
			break;
		}
		return false;
	}

	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		mListViewWidth = w;
		mListViewHeight = h;
		mIndexbarRect = new RectF(w - mIndexbarMargin - mIndexbarWidth, mIndexbarMargin, w - mIndexbarMargin, h
				- mIndexbarMargin);
	}

	public void setAdapter(Adapter adapter) {
		if (adapter instanceof SectionIndexer) {
			mIndexer = (SectionIndexer) adapter;
			mSections = (String[]) mIndexer.getSections();
		}
	}

	public boolean contains(float x, float y) {
		// Determine if the point is in index bar region, which includes the
		// right margin of the bar
		return (x >= mIndexbarRect.left && y >= mIndexbarRect.top && y <= mIndexbarRect.top + mIndexbarRect.height());
	}

	private int getSectionByPoint(float y) {
		if (mSections == null || mSections.length == 0)
			return 0;
		if (y < mIndexbarRect.top + mIndexbarMargin)
			return 0;
		if (y >= mIndexbarRect.top + mIndexbarRect.height() - mIndexbarMargin)
			return mSections.length - 1;
		return (int) ((y - mIndexbarRect.top - mIndexbarMargin) / ((mIndexbarRect.height() - 2 * mIndexbarMargin) / mSections.length));
	}
}
