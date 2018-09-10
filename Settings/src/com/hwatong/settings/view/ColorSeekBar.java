package com.hwatong.settings.view;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.hwatong.settings.R;


public class ColorSeekBar extends View  {
	private static final String TAG = "ColorSeekBar";
	
	public static final int[] COLORS = new int[]{0xFFFF00FF,0xFF0000FF,
		0xFF00FFFF, 0xFF00FF00,
		0xffff7400,
		0xFFFF0000};


	private int barHeight = 0;

	private int barWidth;


	private int thumbRadius = 14;


	private int currentThumbOffsetX = 0;


	private int barStartX, barStartY;

	Paint thumbPaint = new Paint();

	private int currentColor;

	public void setHeight(int h) {
		thumbRadius = barHeight = h / 2;
	}

	public void setCurrentThumbOffset(int position) {
		currentThumbOffsetX =position;
		invalidate();
	}

	private int mSetProgress=-1;
	public void setCurrentProgress(int progress) {
		if (barWidth>0) {
			currentThumbOffsetX = ((barWidth-thumbRadius*2)*progress/255);
			if (currentThumbOffsetX >= barWidth - thumbRadius*2)
				currentThumbOffsetX = barWidth - thumbRadius*2;              
			invalidate();
		}else {
			mSetProgress=progress;
		}
		Log.d(TAG, "setCurrentProgress: progress="+progress +", barWidth="+barWidth + "currentThumbOffsetX="+currentThumbOffsetX);
	}

	public interface ColorChangeListener {
		void colorChange(int color, float rate);
	}

	ColorChangeListener colorChangeListener;

	public ColorSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		currentColor = COLORS[0];
		invalidate();
	}


	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		barHeight = 8;
		barWidth = w;
		barStartX = 0;
		barStartY = h/2 - barHeight / 2; 
		super.onSizeChanged(w, h, oldw, oldh);
		if (mSetProgress>0) {
			setCurrentProgress(mSetProgress);
			mSetProgress=-1;
		}
	}

	public void setOnColorChangerListener(ColorChangeListener colorChangerListener) {
		this.colorChangeListener = colorChangerListener;
	}

	private void drawBar(Canvas canvas) {
		Paint barPaint = new Paint();
		barPaint.setShader(
				new LinearGradient(barStartX, barStartY + barHeight / 2,
						barStartX + barWidth, barStartY + barHeight / 2,
						COLORS, null, Shader.TileMode.CLAMP));
		canvas.drawRoundRect(
				new RectF(barStartX, barStartY,
						barStartX + barWidth, barStartY + barHeight), 10, 10, barPaint);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		/*if(StaticSetFragment.huadong){
    		return true;
    	}*/
		switch (event.getAction()) {

		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_MOVE:
			currentThumbOffsetX = (int) event.getX();
			if (currentThumbOffsetX <= 0) 
				currentThumbOffsetX = 0;
			if (currentThumbOffsetX >= barWidth - thumbRadius*2)
				currentThumbOffsetX = barWidth - thumbRadius*2;              
			break;
		}

		invalidate();

		return true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		drawBar(canvas);
		currentColor = getCurrentColor();
		drawThumb(canvas);
		if (colorChangeListener != null)
			colorChangeListener.colorChange(currentColor, (float)(currentThumbOffsetX)/(float)(barWidth-thumbRadius*2));

		super.onDraw(canvas);
	}

	private int ave(int s, int t, int unit, int step) {
		return s + (t - s) * step / unit;
	}


	private int getCurrentColor() {
		int unit = barWidth / (COLORS.length - 1);
		int position = currentThumbOffsetX - thumbRadius;
		int i = position / unit;
		int step = position % unit;
		if (i >= COLORS.length - 1) 
			return COLORS[COLORS.length - 1];
		int c0 = COLORS[i];
		int c1 = COLORS[i + 1];

		int a = ave(Color.alpha(c0), Color.alpha(c1), unit, step);
		int r = ave(Color.red(c0), Color.red(c1), unit, step);
		int g = ave(Color.green(c0), Color.green(c1), unit, step);
		int b = ave(Color.blue(c0), Color.blue(c1), unit, step);

		return Color.argb(a, r, g, b);
	}

	private void drawThumb(Canvas canvas) {
		//		thumbPaint.setColor(currentColor);

		//        Path mPath=new Path();
		//        
		//        mPath.moveTo(currentThumbOffsetX - 10, barStartY-30); 
		//        mPath.lineTo(currentThumbOffsetX + 10, barStartY-30); 
		//        mPath.lineTo(currentThumbOffsetX + 10, barStartY-15); 
		//        mPath.lineTo(currentThumbOffsetX, barStartY); 
		//        mPath.lineTo(currentThumbOffsetX - 10, barStartY-15); 
		//       mPath.close(); 
		//       canvas.drawPath(mPath, thumbPaint);
		Paint p = new Paint();  
		p.setColor(Color.GREEN);
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.colorseekbar_thumb);
		
		int left = barStartX+currentThumbOffsetX;
		int top = barStartY+barHeight / 2-bitmap.getHeight()/2;
		
		canvas.drawBitmap(bitmap, left, top, p);  
		Log.d(TAG, "left="+left+", top="+top+", currentThumbOffsetX="+currentThumbOffsetX+",thumbRadius="+thumbRadius+", bitmapWidth="+bitmap.getWidth()+", bitmapHeidht="+bitmap.getHeight());
	}


	private RectF getThumbRect() {
		return new RectF(currentThumbOffsetX - thumbRadius, barStartY + barHeight / 2 - thumbRadius,
				currentThumbOffsetX + thumbRadius, barStartY + barHeight / 2 + thumbRadius);
	}



	public int fahuiweizhi()
	{
		return  currentThumbOffsetX;
	}



}