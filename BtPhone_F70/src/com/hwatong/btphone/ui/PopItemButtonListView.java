package com.hwatong.btphone.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hwatong.btphone.CallLog;
import com.hwatong.btphone.Contact;
import com.hwatong.btphone.util.DensityUtils;
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

	/**
	 * mLabelB不带联系人信息
	 * mLabelT带联系人信息
	 */
	private View mLabelB, mLabelT;  
	
	/**
	 * 姓名， 号码，属于mLabelT，
	 */
	private TextView tvName, tvNumber;
	
	
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

	/**
	 * hideCurrentItem与hideButton的区别在于，hideButton是打开之后点击同一个条目关闭，hideCurrentItem是打开一个条目后点击了其他条目或者滑动了列表之后关闭。
	 */
	public synchronized void hideCurrentItemBtn() {
		if (mItemView != null) {
			hideButton((ViewHolder) mItemView.getTag());
			
			//点击其他条目关闭需要延时，保证切换不闪
			hideRightMessage(200);
		}
	}

	protected synchronized void showButton(ViewHolder holder, View view) {
		L.d(thiz, "show! holder: " + holder.hashCode() + " view: " + view.hashCode());
		if (holder == null || holder.mTvNumber == null 
				|| holder.mBtnDial == null || mItemView != null)
			return;
		holder.mBtnDial.setVisibility(View.VISIBLE);
		float orignalX = holder.mTvNumber.getX();
		float endX = orignalX - holder.mBtnDial.getMeasuredWidth() + DensityUtils.dp2px(getContext(), 10);
		
		holder.mTvNumber.setX(endX);
		
		holder.mBtnDial.setX(endX + holder.mTvNumber.getWidth() + DensityUtils.dp2px(getContext(), 10));
		
		mItemView = view;
		
		
		showRightMessage(holder, 0);
	}

	protected synchronized void hideButton(ViewHolder holder) {
		L.d(thiz, "hide! holder: " + holder.hashCode());
		if (holder == null || holder.mTvNumber == null 
				|| holder.mBtnDial == null || mItemView == null)
			return;
		float orignalX = holder.mTvNumber.getX();
		float endX = orignalX + holder.mBtnDial.getMeasuredWidth() - DensityUtils.dp2px(getContext(), 10);
		
		holder.mTvNumber.setX(endX);
		
		holder.mBtnDial.setX(endX + holder.mTvNumber.getWidth() + DensityUtils.dp2px(getContext(), 30));
		
		mItemView = null;
		
		//点击当前条目关闭不延时，保证立即隐藏
		hideRightMessage(0);
	}
	
	/**
	 * 增加一个显示右边信息的方法
	 * @param holder
	 * @param delay
	 */
	private void showRightMessage(ViewHolder holder, long delay) {
		Message obtainMessage = handler.obtainMessage();
		obtainMessage.obj = holder;
		obtainMessage.what = 0;
		handler.removeCallbacksAndMessages(null);
		handler.sendMessageDelayed(obtainMessage, delay);
	}
	
	/**
	 * 增加一个隐藏右边信息的方法
	 * @param delay
	 */
	private void hideRightMessage(long delay) {
		handler.removeCallbacksAndMessages(null);
		handler.sendEmptyMessageDelayed(1, delay);
	}
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0:
				if(mLabelB != null && mLabelT != null && tvName != null && tvNumber != null) {
					mLabelB.setVisibility(View.INVISIBLE);
					mLabelT.setVisibility(View.VISIBLE);
					
					ViewHolder holder = (ViewHolder) msg.obj;
					if(holder != null) {
						tvName.setText(holder.mDtvName.getText());
						tvNumber.setText(holder.mTvNumber.getText());
					}
				}
				break;
			case 1:
				if(mLabelB != null && mLabelT != null) {
					mLabelB.setVisibility(View.VISIBLE);
					mLabelT.setVisibility(View.INVISIBLE);
				}
				break;
			default:
				break;
			}
		};
	};
	
	/**
	 * 需要在Activity获取到View对象后设置进来
	 * @param labelB
	 * @param labelT
	 * @param tvName
	 * @param tvNumber
	 */
	public void attachLabel(View labelB, View labelT, TextView tvName, TextView tvNumber) {
		this.mLabelB = labelB;
		this.mLabelT = labelT;
		this.tvName = tvName; 
		this.tvNumber = tvNumber;
	}
	
	
}
