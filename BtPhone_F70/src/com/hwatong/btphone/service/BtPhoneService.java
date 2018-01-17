package com.hwatong.btphone.service;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.hwatong.btphone.CallLog;
import com.hwatong.btphone.Contact;
import com.hwatong.btphone.app.BtPhoneApplication;
import com.hwatong.btphone.bean.UICallLog;
import com.hwatong.btphone.constants.Constant;
import com.hwatong.btphone.iview.IReceiverView;
import com.hwatong.btphone.iview.IServiceView;
import com.hwatong.btphone.presenter.BroadcastPresenter;
import com.hwatong.btphone.presenter.ServicePresenter;
import com.hwatong.btphone.ui.R;
import com.hwatong.btphone.util.L;
import com.hwatong.btphone.util.Utils;

/**
 * 
 * @author zxy time:2017年5月26日
 * 
 */
public class BtPhoneService extends Service implements IReceiverView, IServiceView{

	private static final String thiz = BtPhoneService.class.getSimpleName();

	public static final int READ_CONTACTS = 0;
	public static final int READ_CALL_OUT = 1;
	public static final int READ_CALL_MISS = 2;
	public static final int READ_CALL_IN = 3;
	
	
	private WindowManager wmManager;
	private View comingWindow;
	private boolean viewAdded = false;
	private WindowManager.LayoutParams params;
	private TextView tvName;
	private ImageView iv_talking;
	private FrameLayout flComing;
	
	private TextView tvCalling, tvAccept, tvReject;
	
	
	private BroadcastPresenter broadcastPresenter;
	private ServicePresenter servicePresenter;
	
	private UICallLog currentCall;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		initView();

		broadcastPresenter = new BroadcastPresenter(this);
		broadcastPresenter.regVoiceBroadcast(this);
		
		servicePresenter = new ServicePresenter(this);
		servicePresenter.link(this);
		
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return new BtPhoneBinder();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy() {
		servicePresenter.unlink(this);
		broadcastPresenter.unregVoiceBroadcast(this);
		super.onDestroy();
	}

	private void initView() {
		initComingWindow();
	}

	private void initComingWindow() {
		params = new WindowManager.LayoutParams();
		params.width = LayoutParams.WRAP_CONTENT;
		params.height = LayoutParams.WRAP_CONTENT;
		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
		params.format = PixelFormat.TRANSLUCENT;
		params.flags = LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		
		params.y = 130;
		params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;

		wmManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		comingWindow = LayoutInflater.from(this).inflate(R.layout.coming_window, null);
		tvName = (TextView) comingWindow.findViewById(R.id.tv_name);
		iv_talking = (ImageView) comingWindow.findViewById(R.id.iv_talking);
		flComing = (FrameLayout) comingWindow.findViewById(R.id.fl_coming);

		tvCalling = (TextView) comingWindow.findViewById(R.id.tv_calling);
		tvAccept = (TextView) comingWindow.findViewById(R.id.tv_accept);
		tvReject = (TextView) comingWindow.findViewById(R.id.tv_reject);
		
		
		OnClickListener clickListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.fl_accept:
					L.d(thiz, "accept!");
					servicePresenter.pickUp();
					
					break;
				case R.id.fl_reject:
					L.d(thiz, "handup!");
					servicePresenter.reject();
					hidePhoneComingWindow();
					break;
				case R.id.iv_talking:
					L.d(thiz, "click iv_talking!!!");
					
					break;
				default:
					break;
				}
			}
		};
		
		comingWindow.findViewById(R.id.fl_accept).setOnClickListener(clickListener);

		comingWindow.findViewById(R.id.fl_reject).setOnClickListener(clickListener);

		iv_talking.setOnClickListener(clickListener);
		iv_talking.setOnTouchListener(new OnTouchListener() {
			private int lastX;
			private int lastY;
			private int downX;
			private int downY;

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:// 按下 事件
					downX = lastX = (int) event.getRawX();
					downY = lastY = (int) event.getRawY();
					break;
				case MotionEvent.ACTION_MOVE:// 移动 事件
					int disX = (int) (event.getRawX() - lastX);
					int disY = (int) (event.getRawY() - lastY);
					params.x += disX;
					params.y += disY;
					wmManager.updateViewLayout(comingWindow, params);
					lastX = (int) event.getRawX();
					lastY = (int) event.getRawY();

					break;
				case MotionEvent.ACTION_UP:// 抬起 事件
					int x = (int) event.getRawX();
					int y = (int) event.getRawY();
					int upX = x - downX;
					int upY = y - downY;
					upX = Math.abs(upX);
					upY = Math.abs(upY);

					if (upX < 5 && upY < 5) {
						// 点击进入指定页面
						
					}
					break;
				}
				return true;
			}
		});
	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}
	
	private Handler windowHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				hidePhoneComingWindow();
				break;
			case 1:
				showPhoneComingWindow();
				break;
			case 2: // 来电或者呼叫
				flComing.setVisibility(View.VISIBLE);
				iv_talking.setVisibility(View.GONE);
				break;
			case 3: // 通话中
				iv_talking.setVisibility(View.VISIBLE);
				flComing.setVisibility(View.GONE);
				break;
			default:
				break;
			}
		};
	};

	/**
	 * 其他界面显示弹窗
	 * 
	 * @param number
	 */
	private synchronized void showPhoneComingWindow() {
		L.d(thiz, "showPhoneComingWindow");
		long start = System.currentTimeMillis();
		if (viewAdded) {
			return;
		}

		tvName.setText(currentCall.number + " " + currentCall.name);

		tvCalling.setText(getString(R.string.coming_now));
		tvAccept.setText(getString(R.string.accept));
		tvReject.setText(getString(R.string.reject));
		
		params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
		params.y = 130;

		wmManager.addView(comingWindow, params);
		viewAdded = true;
		
		L.d(thiz, "showPhoneComingWindow cost: " + (System.currentTimeMillis() - start));
		
	}

	private synchronized void hidePhoneComingWindow() {
		if (!viewAdded) {
			return;
		}
		wmManager.removeView(comingWindow);
		viewAdded = false;
	}

	/**
	 * 发送指令到服务端更新通讯录或者通话记录
	 * 
	 * @param read
	 */
	private void sendBTCommand(int read) {
		switch (read) {
		case READ_CALL_OUT:
			servicePresenter.loadDialedLogs();

			break;
		case READ_CALL_MISS:
			servicePresenter.loadMissedLogs();
			
			break;
		case READ_CALL_IN:
			servicePresenter.loadReceivedLogs();

			break;
		case READ_CONTACTS:
			servicePresenter.loadBooks();
			
			break;
		}
	}


	/**
	 * 供客户端调用的类
	 */
	public class BtPhoneBinder extends Binder {

		private BtPhoneBinder() {
			super();
		}		

		public void sync() {
			servicePresenter.sync();
		}
		
		public void dial(String number) {
			servicePresenter.dial(number);
		}

		public void pickUp() {
			servicePresenter.pickUp();
		}

		public void hangUp() {
			servicePresenter.hangUp();
		}

		public void reject() {
			servicePresenter.reject();
		}

		public void dtmf(char code) {
			servicePresenter.dtmf(code);
		}

		public void toggleMic() {
			servicePresenter.toggleMic();
		}

		public void toggleTrack() {
			servicePresenter.toggleTrack();
		}

		public void loadBooks() {
			servicePresenter.loadBooks();
		}

		public void loadLogs() {
			servicePresenter.loadLogs();
		}

		public void loadMissedLogs() {
			servicePresenter.loadMissedLogs();
		}

		public void loadDialedLogs() {
			servicePresenter.loadDialedLogs();
		}

		public void loadReceivedLogs() {
			servicePresenter.loadReceivedLogs();
		}
		
		public List<Contact> getBooks() {
			return servicePresenter.getBooks();
		}
		
		public List<CallLog> getLogs() {
			return servicePresenter.getLogs();
		}
		
		public List<CallLog> getMissedLogs() {
			return servicePresenter.getMissedLogs();
		}
		
		public List<CallLog> getDialedLogs() {
			return servicePresenter.getDialedLogs();
		}
		
		public List<CallLog> getReceivedLogs() {
			return servicePresenter.getReceivedLogs();
		}

		/**
		 * 切换不同类型通话记录时用到
		 */
		public void syncLogsStatus(int type) {
			servicePresenter.syncLogsStatus(type);
		}
		
	}

	@Override
	public void close() {
		//BtPhoneApplication.getInstance().notifyMsg(Constant.MSG_CLOSE);
		BtPhoneApplication.getInstance().exit();
	}
	
	@Override
	public void toMissedCalls() {
		L.d(thiz, "toMissedCalls");
		if(!servicePresenter.isBtConnected()) {
			return ;
		}
		
		if(isCallLogForground()) {
			BtPhoneApplication.getInstance().notifyMsg(Constant.MSG_OPEN_MISSED_CALLS);
		} else {
			Utils.gotoCallLogActivityInService(this);
			BtPhoneApplication.getInstance().notifyMsg(Constant.MSG_OPEN_MISSED_CALLS);
		}
	}

	@Override
	public void showWindow(UICallLog callLog) {
		L.d(thiz, "showWindow");
		currentCall = callLog;
		windowHandler.sendEmptyMessage(1);
	}

	@Override
	public void hideWindow() {
		windowHandler.sendEmptyMessage(0);
	}
	
	@Override
	public void showTalking(UICallLog callLog) {
		hideWindow();
		gotoDialActivity(callLog);
	}

	@Override
	public void gotoDialActivity(UICallLog callLog) {
		L.d(thiz, "goto dial activity in service");
		Utils.gotoDialActivityInService(this, callLog);
	}
	
	protected boolean isCallLogForground() {
		ActivityManager am = (ActivityManager) BtPhoneApplication.getInstance().getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> cn = am.getRunningTasks(1);
		RunningTaskInfo taskInfo = cn.get(0);
		ComponentName name = taskInfo.topActivity;
		if ("com.hwatong.btphone.activity.CallLogActivity".equals(name.getClassName())) {
			return true;
		}
		return false;
	}

}
