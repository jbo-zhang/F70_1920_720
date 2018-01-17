package com.hwatong.aircondition;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.SeekBar;

public class VerticalSeekBar extends SeekBar {
	private boolean mIsDragging;
	private float mTouchDownY;
	private int mScaledTouchSlop;
	private boolean isInScrollingContainer = false;

	public boolean isInScrollingContainer() {
		return isInScrollingContainer;
	}

	public void setInScrollingContainer(boolean isInScrollingContainer) {
		this.isInScrollingContainer = isInScrollingContainer;
	}

	float mTouchProgressOffset;

	public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
	}

	public VerticalSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public VerticalSeekBar(Context context) {
		super(context);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(h, w, oldh, oldw);

	}

	@Override
	protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(heightMeasureSpec, widthMeasureSpec);
		setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	protected synchronized void onDraw(Canvas canvas) {
		
		canvas.rotate(-90);
		canvas.translate(-getHeight(), 0); 
		
		super.onDraw(canvas);
		
	} 

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!isEnabled()) {
			return false;
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_MOVE:
			setProgress(getCurProgress(event));
			//setProgress2(getCurProgress(event), true);
			onSizeChanged(getWidth(), getHeight(), 0, 0);
			break;
		case MotionEvent.ACTION_UP:
			int progress = getCurProgress(event);
			setProgress(progress);
			//setProgress2(progress, true);
			onSizeChanged(getWidth(), getHeight(), 0, 0);
			if(mListener != null){
				mListener.onTouch(progress);
			}
			break;

		case MotionEvent.ACTION_CANCEL:
			break;
		}

		return true;
	}

	private int getCurProgress(MotionEvent event) {
		float y = event.getY();
		y = y < 0 ? 0 : y > getHeight() ? getHeight() : y;
		return getMax() - (int)(getMax() * y/getHeight());
	}


	UpDownListener mListener;

	public void setUpDownListener(UpDownListener l) {
		mListener = l;
	}

	public interface UpDownListener {
		public void onTouch(int progress);
	};


	/**
	 * This is called when the user has started touching this widget.
	 */
	void onStartTrackingTouch() {
		mIsDragging = true;
	}

	/**
	 * This is called when the user either releases his touch or the touch is
	 * canceled.
	 */
	void onStopTrackingTouch() {
		mIsDragging = false;
	}


	@Override
	public synchronized void setProgress(int progress) {

		super.setProgress(progress);
		onSizeChanged(getWidth(), getHeight(), 0, 0);

	}
	
	public synchronized void setProgress2(int progress, boolean fromUser) {
		try {
			Method setProgress = getClass().getMethod("setProgress", int.class, boolean.class);
			setProgress.invoke(this, progress, fromUser);
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		onSizeChanged(getWidth(), getHeight(), 0, 0);
	}

}
