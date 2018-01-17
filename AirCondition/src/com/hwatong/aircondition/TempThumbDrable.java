package com.hwatong.aircondition;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class TempThumbDrable extends Drawable{
	
	private static final String TAG = "TempThumbDrable" ;
	
	
	RectF rectF = null; 
	
	int left;
	int top;
	int right;
	int bottom ;
	
	/**
	 * 默认温度
	 */
	private String temp= "0" ;
	
    public void setTemp(String temp) {
		this.temp = temp;
		invalidateSelf() ;
	}
   
	private Paint mPaint;  
    private Bitmap mBitmap;  
 
  
    public TempThumbDrable(Bitmap bitmap) {  
        mBitmap = bitmap;    
        mPaint = new Paint();  
        mPaint.setAntiAlias(true);   
    }  
  
    @Override  
    public void setBounds(int left, int top, int right, int bottom) {  
    	Log.d(TAG, "left="+left +"top="+top +"right="+right +"bottom="+bottom);
    	rectF = new RectF(left, top, right, bottom);
    	this.left = left ; 
    	this.top = top ;
    	this.right = right ;
    	this.bottom = bottom ;
        super.setBounds(left, top, right, bottom);
        
    }  
  
    @Override  
    public void draw(Canvas canvas) {  
        canvas.drawBitmap(mBitmap, null, rectF, mPaint);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(25.0f);
        canvas.drawText(temp, getBounds().centerX()-35, getBounds().centerY()+10, paint);
    }  
  
    @Override  
    public int getIntrinsicWidth() {  
        return mBitmap.getWidth();  
    }  
  
    @Override  
    public int getIntrinsicHeight() {  
        return mBitmap.getHeight();  
    }  
  
    @Override  
    public void setAlpha(int alpha) {  
        mPaint.setAlpha(alpha);  
    }  
  
    @Override  
    public void setColorFilter(ColorFilter cf) {  
        mPaint.setColorFilter(cf);  
    }  
  
    @Override  
    public int getOpacity() {  
        return PixelFormat.TRANSLUCENT;  
    } 


}
