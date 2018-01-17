package com.hwatong.btphone.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.hwatong.btphone.activity.base.BaseActivity;
import com.hwatong.btphone.bean.UICallLog;
import com.hwatong.btphone.ui.DrawableTextView;
import com.hwatong.btphone.ui.R;
import com.hwatong.btphone.util.L;
import com.hwatong.btphone.util.Utils;

/**
 * 主界面
 * 
 * @author zxy zjb time:2017年5月25日
 * 
 */
public class PhoneActivity extends BaseActivity{

	private static final String thiz = PhoneActivity.class.getSimpleName();

	private DrawableTextView mTvDial;

	private DrawableTextView mTvContacts;

	private DrawableTextView mTvCallLog;

	private ImageButton mBtnReturn;

	private TextView tvTip;

	int totalY;

	@Override
	protected void initView() {
		mTvDial = (DrawableTextView) findViewById(R.id.dtv_dial);
		mTvDial.setOnClickListener(this);

		mTvContacts = (DrawableTextView) findViewById(R.id.dtv_contacts);
		mTvContacts.setOnClickListener(this);

		mTvCallLog = (DrawableTextView) findViewById(R.id.dtv_call_log);
		mTvCallLog.setOnClickListener(this);

		mBtnReturn = (ImageButton) findViewById(R.id.btn_return);
		mBtnReturn.setOnClickListener(this);

		tvTip = (TextView) findViewById(R.id.tv_tip);

		// 默认为false
		showBtConnected(false);

	}

	/**
	 * 四指手势
	 */
	private boolean sended = false;

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			// 手势启动升级界面
			case 456:
				L.d(thiz, "msg 456");
//				Intent intent = new Intent();
//				intent.setAction("android.intent.action.SYSTEM_UPDATE_SETTINGS");
//				if (intent.resolveActivity(getPackageManager()) != null) {
//					startActivity(intent);
//				} else {
//					Toast.makeText(PhoneActivity.this, "没有升级应用", Toast.LENGTH_SHORT).show();
//				}
				break;

			case 321:
//				startActivity(new Intent(PhoneActivity.this, TboxUpdateActivity.class));
				break;
			default:
				break;
			}
		};
	};

	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// 系统升级
		if (event.getPointerCount() >= 3) {
			if (!sended) {
				handler.sendEmptyMessageDelayed(456, 5000);
				sended = true;
			}
		} else {
			handler.removeMessages(456);
			sended = false;
		}

		// tbox升级
		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			if (event.getX() > 1000) {
				totalY += event.getY();
				L.d(thiz, "totalY : " + totalY);
				if (totalY > (200 * 100)) {
					handler.sendEmptyMessage(321);
					totalY = 0;
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			handler.removeMessages(321);
			totalY = 0;
			break;
		default:
			break;
		}

		return super.onTouchEvent(event);
	}

	private void showBtConnected(boolean connected) {
		L.d(thiz, "onViewStateChange enabled : " + connected);

		if (mTvDial.isEnabled() != connected) {
			// 字体颜色
			int colorId = connected ? R.color.activity_tab_textcolor : R.color.activity_tab_textcolor_gray;
			int color = getResources().getColor(colorId);

			// 拨号
			Drawable drawable = getResources()
					.getDrawable(connected ? R.drawable.icon_dial : R.drawable.icon_dial_gray);
			Utils.setTextViewGray(mTvDial, connected, new Drawable[] { null, drawable, null, null }, color);

			// 通讯录
			Drawable drawable2 = getResources().getDrawable(
					connected ? R.drawable.icon_calllog : R.drawable.icon_calllog_gray);
			Utils.setTextViewGray(mTvCallLog, connected, new Drawable[] { null, drawable2, null, null }, color);

			// 通话记录
			Drawable drawable3 = getResources().getDrawable(
					connected ? R.drawable.icon_contacts : R.drawable.icon_contacts_gray);
			Utils.setTextViewGray(mTvContacts, connected, new Drawable[] { null, drawable3, null, null }, color);

			// 连接提示
			// tvTip.setText(connected ? "" :
			// getString(R.string.bt_not_connect));
		}
	}

	@Override
	protected void doClick(View v) {
		switch (v.getId()) {
		case R.id.dtv_dial:
			L.d(thiz, "onclick dial!");
			Utils.gotoActivity(this, DialActivity.class);
			break;
		case R.id.dtv_contacts:
			L.d(thiz, "onclick contacts!");
			Utils.gotoActivity(this, ContactsListActivity.class);
			break;
		case R.id.dtv_call_log:
			L.d(thiz, "onclick calllog!");
			Utils.gotoActivity(this, CallLogActivity.class);
			break;
		case R.id.btn_return:
			L.d(thiz, "onclick finish!");
			toHome();
			finish();
			break;

		default:
			break;
		}
	}

	/**
	 * to home!
	 */
	private void toHome() {
		Intent intent = new Intent("com.hwatong.launcher.MAIN");
		try {
			startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected int getLayoutId() {
		return R.layout.activity_btphone;
	}

	@Override
	protected String getPageName() {
		return "btphone_home";
	}

	@Override
	public void showConnected() {
		showBtConnected(true);
	}

	@Override
	public void showDisconnected() {
		showBtConnected(false);
	}

	@Override
	public void showComing(UICallLog callLog) {
		if(callLog.shouldJump == 1) {
			Utils.gotoDialActivity(this, callLog);
		}
	}

	@Override
	public void showCalling(UICallLog callLog) {
		if(callLog.shouldJump == 1) {
			Utils.gotoDialActivity(this, callLog);
		}
	}
	
	@Override
	public void showTalking(UICallLog callLog) {
		if(callLog.shouldJump == 1) {
			Utils.gotoDialActivity(this, callLog);
		}
	}

	@Override
	public void showHangUp(UICallLog callLog) {
		L.d(thiz, "showHangUp");
	}

}
