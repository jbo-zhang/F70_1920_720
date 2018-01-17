package com.hwatong.btphone.activity.base;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;

import com.hwatong.btphone.CallLog;
import com.hwatong.btphone.Contact;
import com.hwatong.btphone.app.BtPhoneApplication;
import com.hwatong.btphone.bean.UICallLog;
import com.hwatong.btphone.constants.Constant;
import com.hwatong.btphone.iview.IUIView;
import com.hwatong.btphone.service.BtPhoneService;
import com.hwatong.btphone.service.BtPhoneService.BtPhoneBinder;
import com.hwatong.btphone.util.L;
import com.hwatong.statusbarinfo.aidl.IStatusBarInfo;

/**
 * 
 * @author zhangjinbo 
 * 
 */
public abstract class BaseActivity extends Activity implements OnClickListener,
		OnLongClickListener, OnTouchListener, IUIView{

	private static final String thiz = BaseActivity.class.getSimpleName();

	protected BtPhoneBinder mService;

	protected IStatusBarInfo iStatusBarInfo;

	private int[] types = { Constant.MSG_SHOW_CONNECTED, Constant.MSG_SHOW_DISCONNECTED,
			Constant.MSG_SHOW_COMING, Constant.MSG_SHOW_CALLING,
			Constant.MSG_SHOW_TALKING, Constant.MSG_SHOW_HANG_UP,
			Constant.MSG_SHOW_REJECT, Constant.MSG_UPDATE_BOOKS,
			Constant.MSG_UPDATE_MISSED_LOGS, Constant.MSG_UPDATE_DIALED_LOGS,
			Constant.MSG_UPDATE_RECEIVED_LOGS, Constant.MSG_UPDATE_ALL_LOGS,
			Constant.MSG_SHOW_BOOKS_LOAD_START,Constant.MSG_SHOW_BOOKS_LOADING, 
			Constant.MSG_SHOW_BOOKS_LOADED,Constant.MSG_SHOW_LOGS_LOAD_START, 
			Constant.MSG_SHOW_LOGS_LOADING,Constant.MSG_SHOW_LOGS_LOADED, 
			Constant.MSG_SHOW_MIC_MUTE, Constant.MSG_SHOW_SOUND_TRACK,
			Constant.MSG_CLOSE, Constant.MSG_SHOW_IDEL, Constant.MSG_SHOW_DTMF_INPUT, 
			Constant.MSG_OPEN_MISSED_CALLS};

	@SuppressLint("HandlerLeak")
	protected Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case Constant.MSG_SHOW_CONNECTED:			//来电
				L.d(thiz, "Constant.MSG_SHOW_CONNECTED");
				showConnected();
				break;
				
			case Constant.MSG_SHOW_DISCONNECTED:		//来电
				L.d(thiz, "Constant.MSG_SHOW_DISCONNECTED");
				showDisconnected();
				break;
				
			case Constant.MSG_SHOW_COMING:				//来电
				L.d(thiz, "onHfpCallChanged Constant.MSG_SHOW_COMING");
				showComing((UICallLog)msg.obj);
				break;
				
			case Constant.MSG_SHOW_CALLING:				//来电
				L.d(thiz, "onHfpCallChanged Constant.MSG_SHOW_CALLING");
				showCalling((UICallLog)msg.obj);
				break;
				
			case Constant.MSG_SHOW_TALKING:				//通话中
				L.d(thiz, "onHfpCallChanged Constant.MSG_SHOW_TALKING");
				showTalking((UICallLog)msg.obj);
				break;
				
			case Constant.MSG_SHOW_HANG_UP:				//挂断
				L.d(thiz, "onHfpCallChanged Constant.MSG_SHOW_HANG_UP");
				showHangUp((UICallLog)msg.obj);
				break;
				
			case Constant.MSG_SHOW_REJECT:				//拒接
				L.d(thiz, "onHfpCallChanged Constant.MSG_SHOW_REJECT");
				showReject((UICallLog)msg.obj);
				break;
				
			case Constant.MSG_SHOW_IDEL:
				L.d(thiz, "onHfpCallChanged Constant.MSG_SHOW_IDEL");
				showIdel();
				break;
				
			case Constant.MSG_UPDATE_BOOKS:				//更新通讯录			
				L.d(thiz, "Constant.MSG_UPDATE_BOOKS");
				if(mService != null) {
					List<Contact> contacts = mService.getBooks();
					updateBooks(contacts);
				}
				
				break;
				
			case Constant.MSG_UPDATE_MISSED_LOGS:		//更新未接列表
				L.d(thiz, "Constant.MSG_UPDATE_MISSED_LOGS");
				if(mService != null) {
					List<CallLog> missedLogs = mService.getMissedLogs();
					updateMissedLogs(missedLogs);
				}
				break;
				
			case Constant.MSG_UPDATE_DIALED_LOGS:		//更新拨出列表
				L.d(thiz, "Constant.MSG_UPDATE_DIALED_LOGS");
				if(mService != null) {
					List<CallLog> dialedLogs = mService.getDialedLogs();
					updateDialedLogs(dialedLogs);
				}
				break;
				
			case Constant.MSG_UPDATE_RECEIVED_LOGS:		//更新接听列表
				L.d(thiz, "Constant.MSG_UPDATE_RECEIVED_LOGS");
				if(mService != null) {
					List<CallLog> receivedLogs = mService.getReceivedLogs();
					updateReceivedLogs(receivedLogs);
				}
				break;
				
			case Constant.MSG_UPDATE_ALL_LOGS:			//更新全部通话列表
				L.d(thiz, "Constant.MSG_UPDATE_ALL_LOGS");
				if(mService != null) {
					List<CallLog> logs = mService.getLogs();
					updateAllLogs(logs);
				}
				break;
				
			case Constant.MSG_SHOW_BOOKS_LOAD_START:	//显示通讯录开始更新
				L.d(thiz, "Constant.MSG_SHOW_BOOKS_LOAD_START");
				showBooksLoadStart();
				break;
				
			case Constant.MSG_SHOW_BOOKS_LOADING:		//显示通讯录更新中
				L.d(thiz, "Constant.MSG_SHOW_BOOKS_LOADING");
				showBooksLoading();
				break;
				
			case Constant.MSG_SHOW_BOOKS_LOADED:		//显示通讯录更新完成
				L.d(thiz, "Constant.MSG_SHOW_BOOKS_LOADED");
				boolean succeed_1 = (msg.arg1 == 1);
				int reason_2 = msg.arg2;
				showBooksLoaded(succeed_1, reason_2);
				break;
				
			case Constant.MSG_SHOW_LOGS_LOAD_START:		//显示通话记录开始更新
				L.d(thiz, "Constant.MSG_SHOW_LOGS_LOAD_START type: " + msg.arg1);
				showLogsLoadStart(msg.arg1);
				break;
				
			case Constant.MSG_SHOW_LOGS_LOADING:		//显示通话记录更新中
				L.d(thiz, "Constant.MSG_SHOW_LOGS_LOADING type: " + msg.arg1);
				showLogsLoading(msg.arg1);
				break;
				
			case Constant.MSG_SHOW_LOGS_LOADED:			//显示通话记录更新完成
				L.d(thiz, "Constant.MSG_SHOW_LOGS_LOADED type: " + msg.arg1 + " result: " + msg.arg2);
				showLogsLoaded(msg.arg1, msg.arg2);
				break;
				
			case Constant.MSG_SHOW_MIC_MUTE:			//麦克风静音状态
				L.d(thiz, "Constant.MSG_SHOW_MIC_MUTE");
				boolean isMute = (msg.arg1 == 1);
				showMicMute(isMute);
				break;
				
			case Constant.MSG_SHOW_SOUND_TRACK:			//声音通道状态
				L.d(thiz, "Constant.MSG_SHOW_SOUND_TRACK");
				boolean isCar = (msg.arg1==1);
				showSoundTrack(isCar);
				break;

			case Constant.MSG_SYNC_BOOKS_ALREADY_LOAD:
				L.d(thiz, "Constant.MSG_SYNC_BOOKS_ALREADY_LOAD");
				syncBooksAlreadyLoad();
				break;
				
			case Constant.MSG_SYNC_LOGS_ALREADY_LOAD:
				L.d(thiz, "Constant.MSG_SYNC_LOGS_ALREADY_LOAD type: " + msg.arg1);
				syncLogsAlreadyLoad(msg.arg1);
				break;
				
			case Constant.MSG_CLOSE:
				L.d(thiz, "Constant.MSG_CLOSE");
				finish();
				break;
				
			case Constant.MSG_SHOW_DTMF_INPUT:			//dtmf输入
				L.d(thiz, "Constant.MSG_SHOW_DTMF_INPUT");
				showDTMFInput((UICallLog)msg.obj);
				break;
				
			case Constant.MSG_OPEN_MISSED_CALLS:		//打开未接来电
				L.d(thiz, "Constant.MSG_OPEN_MISSED_CALLS");
				toMissedCalls();
				break;
				
			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(getLayoutId());

		initView();

		bindService(new Intent(this, BtPhoneService.class), mConn, Context.BIND_AUTO_CREATE);
		
		BtPhoneApplication.getInstance().putActivity(this);
		
	}

	@Override
	protected void onResume() {
		L.d(thiz, "onResume");
		super.onResume();
		
		BtPhoneApplication.getInstance().registerHandler(mHandler, types);
		
		sync();
		
		if (iStatusBarInfo != null) {
			try {
				iStatusBarInfo.setCurrentPageName(getPageName());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else {
			Intent intent = new Intent();
			intent.setAction("com.remote.hwatong.statusinfoservice");
			bindService(intent, mConn2, BIND_AUTO_CREATE);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onPause() {
		L.d(thiz, "onPause");
		BtPhoneApplication.getInstance().unRegisterHandler(mHandler, types);
		super.onPause();
	}
	
	
	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {

		unbindService(mConn);
		unbindService(mConn2);
		
		mService = null;
		iStatusBarInfo = null;
		
		BtPhoneApplication.getInstance().removeActivity(this);
		super.onDestroy();
	}

	protected ServiceConnection mConn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = (BtPhoneBinder) service;
			sync();
			serviceConnected();
		}

	};

	private void sync() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				if(mService != null) {
					mService.sync();
				}
			}
		}).start();
	}
	
	
	
	protected ServiceConnection mConn2 = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			iStatusBarInfo = null;
		}

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder binder) {
			iStatusBarInfo = IStatusBarInfo.Stub.asInterface(binder);
			try {
				if (iStatusBarInfo != null) {
					iStatusBarInfo.setCurrentPageName(getPageName());
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	};

	public void onClick(android.view.View view) {
		doClick(view);
	};

	@Override
	public boolean onLongClick(View view) {
		return doLongClick(view);
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		return doTouch(view, event);
	}

	protected abstract int getLayoutId();

	protected abstract String getPageName();

	protected void initView() {}

	protected void serviceConnected() {}

	protected void doClick(View v) {}

	protected boolean doLongClick(View v) {
		return false;
	}

	protected boolean doTouch(View view, MotionEvent event) {
		return false;
	}
	

	@Override
	public void showConnected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showDisconnected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showComing(UICallLog callLog) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showCalling(UICallLog callLog) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showTalking(UICallLog callLog) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showHangUp(UICallLog callLog) {
		// TODO Auto-generated method stub
		L.d(thiz, "showHangUp");
	}

	@Override
	public void showReject(UICallLog callLog) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateBooks(List<Contact> list) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateMissedLogs(List<CallLog> list) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateDialedLogs(List<CallLog> list) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateReceivedLogs(List<CallLog> list) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateAllLogs(List<CallLog> list) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showBooksLoadStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showBooksLoading() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showBooksLoaded(boolean succeed, int reason) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void syncBooksAlreadyLoad() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showLogsLoadStart(int type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showLogsLoading(int type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showLogsLoaded(int type, int result) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void syncLogsAlreadyLoad(int type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showMicMute(boolean isMute) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void showSoundTrack(boolean isCar) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void showIdel() {
		// TODO Auto-generated method stub
		
	}
	
	
	@Override
	public void showDTMFInput(UICallLog callLog) {
		// TODO Auto-generated method stub
	}
	
	
	@Override
	public void toMissedCalls() {
		
	}
	

}
