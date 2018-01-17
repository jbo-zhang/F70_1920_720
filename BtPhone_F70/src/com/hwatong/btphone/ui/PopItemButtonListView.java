package com.hwatong.btphone.ui;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.hwatong.btphone.CallLog;
import com.hwatong.btphone.Contact;
import com.hwatong.btphone.util.L;

/**
 * 特用于本应用需要点击listVew item弹出拨号按钮 注意:不要在外部调用 setOnItemClickListener
 * 方法，否则原来的点击事件会被覆盖
 * 
 * @author zxy time:2017年6月20日
 * 
 */
public class PopItemButtonListView extends ListView {

	private static final String thiz = PopItemButtonListView.class.getSimpleName();
	
	protected View mItemView;// 被点击的ItemView
	protected boolean mItemClickEnable = true;// 设置Item view点击是否有效

	public PopItemButtonListView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		setOnItemClickListener(mItemClickListener);
	}

	public PopItemButtonListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOnItemClickListener(mItemClickListener);
	}

	public PopItemButtonListView(Context context) {
		super(context);
		setOnItemClickListener(mItemClickListener);
	}

	private NoDoubleItemClickListener mItemClickListener = new NoDoubleItemClickListener() {

		@Override
		public void onItemClickImp(AdapterView<?> parent, View view,
				int position, long id) {
			if (!mItemClickEnable) {
				return;
			}

			Object object = getItemAtPosition(position);
			String number = null;
			if (object instanceof Contact) {
				number = ((Contact) object).number;
			} else if (object instanceof CallLog) {
				number = ((CallLog) object).number;
			}
			if (TextUtils.isEmpty(number)) {
				return;
			}

			ViewHolder holder = (ViewHolder) view.getTag();
			if (holder == null) {
				return;
			}

			if (mItemView == null) {
				showButton(holder, view);
			} else if (mItemView == view) {
				hideButton(holder);
			} else {
				//这个就是不等于null也不等于当前View，表明打开的时候点击到另一个Item
				//hideCurrentItemBtn();
				showButton(holder, view);
			}

		}
	};

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (mItemView != null) {
				if (ev.getY() > mItemView.getY() && ev.getY() < (mItemView.getY() + mItemView.getHeight())) {
					// 表示触摸在当前View内, 让onClick收起
					return true;
				} else {
					hideCurrentItemBtn();
					return true;
				}
			}
			break;

		case MotionEvent.ACTION_MOVE:
			// 触摸到当前View也不可以滑动
			if (mItemView != null) {
				//hideCurrentItemBtn();
				return true;
			}
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:

			break;
		}
		return super.onTouchEvent(ev);
	}

	public void setItemClickEnable(boolean itemClickEnable) {
		this.mItemClickEnable = itemClickEnable;
		hideCurrentItemBtn();
	}

	public synchronized void hideCurrentItemBtn() {
		if (mItemView != null) {
			hideButton((ViewHolder) mItemView.getTag());
		}
	}

	protected synchronized void showButton(ViewHolder holder, View view) {
		L.d(thiz, "show! holder: " + holder.hashCode() + " view: " + view.hashCode());
		if (holder == null || holder.mTvNumber == null 
				|| holder.mBtnDial == null || mItemView != null)
			return;
		holder.mBtnDial.setVisibility(View.VISIBLE);
		float orignalX = holder.mTvNumber.getX();
		float endX = orignalX - holder.mBtnDial.getMeasuredWidth() + 10;
		
		holder.mTvNumber.setX(endX);
		
		holder.mBtnDial.setX(endX + holder.mTvNumber.getWidth() + 10);
		
		mItemView = view;
	}

	protected synchronized void hideButton(ViewHolder holder) {
		L.d(thiz, "hide! holder: " + holder.hashCode());
		if (holder == null || holder.mTvNumber == null 
				|| holder.mBtnDial == null || mItemView == null)
			return;
		float orignalX = holder.mTvNumber.getX();
		float endX = orignalX + holder.mBtnDial.getMeasuredWidth() - 10;
		
		holder.mTvNumber.setX(endX);
		
		holder.mBtnDial.setX(endX + holder.mTvNumber.getWidth() + 30);
		
		mItemView = null;
	}
	
}
