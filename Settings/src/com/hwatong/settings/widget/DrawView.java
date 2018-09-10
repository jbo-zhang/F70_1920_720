package com.hwatong.settings.widget;

import com.hwatong.settings.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;


public class DrawView extends ImageView {
	private static String TAG = "DrawView";
	private Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(),
			R.drawable.f70_sound_thumb_normal);
	 private Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(),
	 R.drawable.f70_sound_thumb_press);
	private Bitmap bitmap = bitmap1;

	private Context mContext;

	private int radius = bitmap.getHeight() > bitmap.getWidth() ? bitmap
			.getHeight() / 2 + 5 : bitmap.getWidth() / 2 + 5;
	private static int maxSzie = 14;
	private float currentX = radius; // 手指X当前位置
	private float currentY = radius;// 手指Y当前位置
	private float X, Y; // 中间小球位置
	private int moveX, moveY; // 当前 X Y的值

	private boolean mNeedmoving = false; // 判断是否可以移动

	private boolean mLineIsAlignMoving = true; // 4根线是否自动跟随跑
	private boolean mValueIsAlignMoving = true; // 4个数字是否自动更新
	private boolean mIsAlign = true; // 滑动之后是否自动对齐

	// 定义、并创建画笔
	Paint p = new Paint();

	public DrawView(Context context) {
		super(context);
	}

	public DrawView(Context context, AttributeSet set) {
		super(context, set);
		mContext = context;
		// p.setColor(Color.argb(188,98, 198, 199));
//		p.setColor(context.getResources().getColor(R.color.line_color));
		p.setStrokeWidth(4);
		setXY(0, 0);
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (!mNeedmoving || mLineIsAlignMoving) {
			X = currentX;
			Y = currentY;
		}

		/*
		 * p.setStrokeWidth(20); PathEffect effect = new DashPathEffect(new
		 * float[]{30, 10}, 1); p.setAntiAlias(true);
		 * p.setStyle(Paint.Style.STROKE); p.setPathEffect(effect); Path path =
		 * new Path(); path.moveTo(20, 20); //画断断续续的线 path.lineTo(150,150);
		 * canvas.drawPath(path, p);
		 */

//		canvas.drawLine(0, Y, X - radius / 2, Y, p);
//		canvas.drawLine(X + radius / 2, Y, getMeasuredWidth(), Y, p);
//		canvas.drawLine(X, 2, X, Y - radius / 2, p);
//		canvas.drawLine(X, Y + radius / 2, X, getMeasuredHeight(), p);
		// canvas.drawCircle(currentX,currentY, radius, p); // 绘制一个小圆（作为小球）
		canvas.drawBitmap(bitmap, currentX - bitmap.getWidth() / 2, currentY
				- bitmap.getHeight() / 2, p);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int action = event.getAction();
			
		currentX = event.getX();  
	    currentY = event.getY();    
	    if(currentX >=getMeasuredWidth() - radius) currentX =getMeasuredWidth() - radius;
	    if(currentX <=radius) currentX =radius;
	    if(currentY >= getMeasuredHeight() - radius) currentY = getMeasuredHeight() - radius;
	    if(currentY <=radius) currentY =radius;
	    mValueIsAlignMoving = true;
//	    if (mOnXYLChangeListener != null && mValueIsAlignMoving)
//	    	mOnXYLChangeListener.onListChange(getx(), gety());
	    //重绘小球  
	    invalidate(); 
	    
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			int i = (int) Math.sqrt(Math.pow(event.getX() - currentX, 2)
					+ Math.pow(event.getY() - currentY, 2));
			if (i <= radius) {
				mNeedmoving = true;
				 bitmap = bitmap2;
				// p.setColor(mContext.getResources().getColor(R.color.blue1));
				invalidate();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (!mNeedmoving)
				break;
			// if(event.getX()+radius > getMeasuredWidth() ||
			// event.getY()+radius > getMeasuredHeight()
			// || event.getX() < radius || event.getY() < radius){
			if (event.getX() + radius > getMeasuredWidth())
				currentX = getMeasuredWidth() - radius;
			else if (event.getX() < radius)
				currentX = radius;
			else
				currentX = event.getX();
			if (event.getY() + radius > getMeasuredHeight())
				currentY = getMeasuredHeight() - radius;
			else if (event.getY() < radius)
				currentY = radius;
			else
				currentY = event.getY();
			// }else{
			// currentX = event.getX();
			// currentY = event.getY();
			// }
			invalidate();
			if (mOnXYLChangeListener != null && mValueIsAlignMoving)
				mOnXYLChangeListener.onListChange(getx(), gety(), false);
			break;
		case MotionEvent.ACTION_UP:
			if (!mNeedmoving)
				break;
			if (event.getX() + radius <= getMeasuredWidth()
					&& event.getY() + radius <= getMeasuredHeight()
					&& event.getX() >= radius && event.getY() >= radius) {
				currentX = event.getX();
				currentY = event.getY();
			} else {
				if (event.getX() + radius > getMeasuredWidth())
					currentX = getMeasuredWidth() - radius;
				else if (event.getX() < radius)
					currentX = radius;
				if (event.getY() + radius > getMeasuredHeight())
					currentY = getMeasuredHeight() - radius;
				else if (event.getY() < radius)
					currentY = radius;
			}
			mNeedmoving = false;
			bitmap = bitmap1;
//			p.setColor(mContext.getResources().getColor(R.color.line_color));
			invalidate();
			if (mOnXYLChangeListener != null)
				mOnXYLChangeListener.onListChange(getx(), gety(), true);
			if (mIsAlign)
				setXY(getx(), gety());
			else {
				moveX = getx();
				moveY = gety();
			}
			break;
		}
		return true; // 返回true表明该处理方法已经处理该事件
	}

	// public void setAddX(boolean x){
	//
	// int a = (int) Math.ceil(((double)getMeasuredWidth()-2*radius)/maxSzie);
	// if(x) currentX += a ;
	// else currentX -= a;
	// if(currentX-radius < 0) currentX = radius;
	// if(currentX+radius > getMeasuredWidth()) currentX =
	// getMeasuredWidth()-radius;
	// invalidate();
	// if(mOnXYLChangeListener != null)
	// mOnXYLChangeListener.onListChange(getx(),gety());
	// }
	//
	// public void setAddY(boolean y){
	// int a = (int) Math.ceil(((double)getMeasuredHeight()-2*radius)/maxSzie);
	//
	// if(y) currentY += a;
	// else currentY -= a;
	// if(currentY-radius < 0) currentY = radius;
	// if(currentY+radius > getMeasuredHeight()) currentY =
	// getMeasuredHeight()-radius;
	// invalidate();
	// if(mOnXYLChangeListener != null)
	// mOnXYLChangeListener.onListChange(getx(),gety());
	// }
	//
	// private int getx(){
	// return
	// Math.round(((float)currentX-radius)/(getMeasuredWidth()-2*radius)*maxSzie)
	// - maxSzie/2;
	// }
	//
	// private int gety(){
	// return
	// Math.round(((float)currentY-radius)/(getMeasuredHeight()-2*radius)*maxSzie)
	// - maxSzie/2;
	// }

	public void setAddX(boolean x) {
		moveX = moveX + (x ? 1 : -1);
		if (moveX + maxSzie / 2 < 0)
			moveX = -maxSzie / 2;
		if (moveX + maxSzie / 2 > maxSzie)
			moveX = maxSzie / 2;
		if (moveX + maxSzie / 2 == 0)
			currentX = radius;
		else if (moveX + maxSzie / 2 == maxSzie)
			currentX = getMeasuredWidth() - radius;
		else {
			currentX = ((float) getMeasuredWidth() - 2 * radius)
					* (moveX + maxSzie / 2) / maxSzie + radius;
		}
		invalidate();
		if (mOnXYLChangeListener != null)
			mOnXYLChangeListener.onListChange(moveX, moveY, false);
	}

	public void setAddY(boolean y) {
		moveY = moveY + (y ? 1 : -1);
		if (moveY + maxSzie / 2 < 0)
			moveY = -maxSzie / 2;
		if (moveY + maxSzie / 2 > maxSzie)
			moveY = maxSzie / 2;
		if (moveY + maxSzie / 2 == 0)
			currentY = radius;
		else if (moveY + maxSzie / 2 == maxSzie)
			currentY = getMeasuredHeight() - radius;
		else {
			currentY = ((float) getMeasuredHeight() - 2 * radius)
					* (moveY + maxSzie / 2) / maxSzie + radius;
		}
		invalidate();
		if (mOnXYLChangeListener != null)
			mOnXYLChangeListener.onListChange(moveX, moveY, false);
	}

	private int gety() {
		if (mIsAlign)
			return Math.round(((float) currentY - radius)/ (getMeasuredHeight() - 2 * radius) * maxSzie)- maxSzie / 2;
		if (currentY * 2 >= getMeasuredHeight())
			return (int) (Math.floor((Double.parseDouble(String.valueOf(currentY)) - radius)/ (getMeasuredHeight() - 2 * radius) * maxSzie) - maxSzie / 2);
		else
			return (int) (Math.ceil((Double.parseDouble(String.valueOf(currentY)) - radius)/ (getMeasuredHeight() - 2 * radius) * maxSzie) - maxSzie / 2);
	}

	private int getx() {
		if (mIsAlign)
			return Math.round(((float) currentX - radius)/ (getMeasuredWidth() - 2 * radius) * maxSzie)- maxSzie / 2;
		if (currentX * 2 >= getMeasuredWidth())
			return (int) (Math.floor(((float) currentX - radius)/ (getMeasuredWidth() - 2 * radius) * maxSzie) - maxSzie / 2);
		else
			return (int) (Math.ceil(((float) currentX - radius)/ (getMeasuredWidth() - 2 * radius) * maxSzie) - maxSzie / 2);
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int a) {
		radius = a;
		invalidate();
	}

	public int getMaxSzie() {
		return maxSzie;
	}

	public void setmMaxSzie(int a) {
		if (a <= 0 || a > 40)
			return;
		maxSzie = a;
		invalidate();
	}

	public void setmIsAlign(boolean a) {
		mIsAlign = a;
	}

	public void setXY(int x, int y) {
		Log.d("ljwtestfuntion", "setXY: " + x + ", " + y);
		if (Math.abs(x) > maxSzie / 2 || Math.abs(y) > maxSzie / 2)
			return;
		currentX = ((float) x + maxSzie / 2) / maxSzie
				* (329 - 2 * radius) + radius;
		currentY = ((float) y + maxSzie / 2) / maxSzie
				* (329 - 2 * radius) + radius;
		invalidate();
		moveX = x;
		moveY = y;
//		if (mOnXYLChangeListener != null)
//			mOnXYLChangeListener.onListChange(getx(), gety(), false);
	}
	
	public static int getMax() {
		return maxSzie;
	}

	private OnXYChangeListener mOnXYLChangeListener;

	public interface OnXYChangeListener {
		public void onListChange(int x, int y, boolean fromUser);
	}

	public void setOnXYChangeListener(OnXYChangeListener listener) {
		mOnXYLChangeListener = listener;
	}
}
