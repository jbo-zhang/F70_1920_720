package com.hwatong.btphone.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.TextView;


/**
 * 自定义TextView 可以设置TextView drawable的宽高
 * 
 * @author leaves
 * 
 */
public class DrawableTextView extends TextView {

	private int mDrawableWidth;
	private int mDrawableHeight;

	public DrawableTextView(Context context) {
		this(context, null);
	}

	public DrawableTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DrawableTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		/**
		 * 取得自定义属性值
		 */
		TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.DrawableTextView);
		mDrawableWidth = ta.getDimensionPixelSize(R.styleable.DrawableTextView_drawableWidth, -1);
		mDrawableHeight = ta.getDimensionPixelSize(R.styleable.DrawableTextView_drawableHeight, -1);

		setDrawablesSize(getCompoundDrawables());

		ta.recycle();
	}

	public void setDrawablesSize(Drawable[] drawables) {
		for (Drawable drawable : drawables) {
			if (drawable == null) {
				continue;
			}

			/**
			 * 设置宽高
			 */
			Rect rect = new Rect(drawable.getBounds());
//			if (mDrawableWidth != -1)
				rect.right = mDrawableWidth == -1?drawable.getMinimumWidth():mDrawableWidth;
//			if (mDrawableHeight != -1)
				rect.bottom = mDrawableHeight == -1?drawable.getMinimumHeight():mDrawableHeight;
			drawable.setBounds(0, 0, rect.right, rect.bottom);

		}

		/**
		 * 设置给TextView
		 */
		setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
	}

	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
	}

	public void setDrawables(Drawable left, Drawable top, Drawable right, Drawable botton) {
		Drawable[] drawables = { left, top, right, botton };
		setDrawablesSize(drawables);
	}

	public void setWidthHeightRate(float wrate, float hrate) {
		Drawable[] drawables = getCompoundDrawables();
		Drawable textDrawable = null;
		for (Drawable drawable : drawables) {
			if (drawable != null) {
				textDrawable = drawable;
			}
		}

		if (textDrawable != null) {
			Rect rect = new Rect(textDrawable.getBounds());
			if (wrate != -1)
				rect.right = (int) ((float) rect.right * wrate);
			if (hrate != -1)
				rect.bottom = (int) ((float) rect.bottom * hrate);
			textDrawable.setBounds(0, 0, rect.right, rect.bottom);
		}

		setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
	}
}