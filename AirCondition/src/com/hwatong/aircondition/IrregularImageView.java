package com.hwatong.aircondition;

import java.text.BreakIterator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewDebug.IntToString;
import android.widget.ImageView;

/**
 * 
 * @author zhangjinbo
 *	一个点击区域不规则的ImageView，同时实现INVISIBLE状态下可以点击。
 */
public class IrregularImageView extends ImageView {
	private static final String thiz = IrregularImageView.class.getSimpleName();

	/**
	 * 透明图片，在INVISIBLE时设置透明背景，让点击事件有效。
	 */
	private Bitmap transparentBmp;
	private Drawable transparentDrawable;
	
	/**
	 * 原背景图片，用于VISIBLE时设回，以及获取点击坐标点的像素
	 */
	private Drawable srcDrawable;
	private Bitmap srcBitmap;
	
	private int mVisibility  = -1;
	
	public IrregularImageView(Context context) {
		super(context);
	}

	public IrregularImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public IrregularImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		transparentBmp = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
		transparentDrawable = new BitmapDrawable(transparentBmp);
		
		srcDrawable = getBackground();
		srcBitmap = drawableToBitmap(srcDrawable);
		
		//xml文件中设置的visibility不调用重写的setVisibility，需要转化一下。
		if(mVisibility == -1) {
			mVisibility = super.getVisibility();
		}
		super.setVisibility(View.VISIBLE);
		setVisibility(mVisibility);
		
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);
	}
	
	public static Bitmap drawableToBitmap(Drawable drawable) {

        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap.Config config =
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                        : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        //注意，下面三行代码要用到，否则在View或者SurfaceView里的canvas.drawBitmap会看不到图
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);

        return bitmap;
    }

	@Override
	public void setVisibility(int visibility) {
		Log.d("kongtiao", "setVisibility : " + visibility);
		
		if(visibility == View.INVISIBLE) {
			if(transparentBmp != null) {
				setBackground(transparentDrawable);
			}
			mVisibility = View.INVISIBLE;
			return;
		} else if(visibility == View.VISIBLE) {
			if(srcDrawable != null) {
				setBackground(srcDrawable);
			}
			mVisibility = View.VISIBLE;
			return;
		}
		super.setVisibility(visibility);
	}
	
	@Override
	@ExportedProperty(mapping = { @IntToString(from = 0, to = "VISIBLE"),
			@IntToString(from = 4, to = "INVISIBLE"),
			@IntToString(from = 8, to = "GONE") })
	public int getVisibility() {
		if(mVisibility == View.INVISIBLE || mVisibility == View.VISIBLE) {
			return mVisibility;
		} else {
			return super.getVisibility();
		}
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			pressed();
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			unPressed();
			break;
		default:
			break;
		}
		
		//Bitmap bitmap = bitmaps[Integer.parseInt((String)getTag())];
		if(event.getX() < 0 || event.getX() > getWidth() || event.getY() < 0 || event.getY() > getHeight()) {
			unPressed();
			return false;
		}
		if (srcBitmap.getPixel((int) (event.getX()), ((int) event.getY())) == 0) {
			Log.d(thiz, " 透明区域");
			unPressed();
			return false;
		}
		Log.d(thiz," 非透明区域");
		return super.onTouchEvent(event);
	}
	
	private boolean isPressed = false;
	private synchronized void pressed() {
		if(getVisibility() != View.VISIBLE && !isPressed && isEnabled()) {
			setVisibility(View.VISIBLE);
			isPressed = true;
		}
	}
	
	private synchronized void unPressed() {
		if(isPressed && isEnabled()) {
			setVisibility(View.INVISIBLE);
			isPressed = false;
		}
	}

}
