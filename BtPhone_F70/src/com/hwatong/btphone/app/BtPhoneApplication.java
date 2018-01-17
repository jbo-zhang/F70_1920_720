package com.hwatong.btphone.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;

import com.hwatong.btphone.service.BtPhoneService;
import com.hwatong.btphone.util.L;

/**
 * 
 * @author zxy time:2017年6月23日
 *
 */
public class BtPhoneApplication extends Application {

	private static final String thiz = BtPhoneApplication.class.getSimpleName();
	
	private static BtPhoneApplication INSTANCE;

	private SparseArray<List<Handler>> mHandlerMap = new SparseArray<List<Handler>>();

	
	private ArrayList<Handler> mHandlers = new ArrayList<Handler>(); 
	
	private Handler mHandler ;
	
	public static BtPhoneApplication getInstance() {
		return INSTANCE;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		startBtPhoneService();
		
		INSTANCE = this;
	}
	
	private void startBtPhoneService() {
		Intent intent = new Intent(this, BtPhoneService.class);
		startService(intent);
	}
	

//	public void registerHandler(Handler handler, int[] type) {
//		if (type == null || handler == null)
//			return;
//		for (int i = 0; i < type.length; i++) {
//			List<Handler> handlers = mHandlerMap.get(type[i]);
//			if (handlers == null) {
//				handlers = new ArrayList<Handler>();
//			}
//			if (!handlers.contains(handler)) {
//				handlers.add(handler);
//				mHandlerMap.put(type[i], handlers);
//			}
//		}
//	}
	
	private List<Activity> activityList = Collections.synchronizedList(new ArrayList<Activity>());
	
	public void putActivity(Activity activity) {
		activityList.add(activity);
	}
	
	public void removeActivity(Activity activity) {
		activityList.remove(activity);
	}
	
	public void exit() {
		L.d(thiz, "exit, activityList size : " + activityList.size());
		for (Activity activity : activityList) {
			activity.finish();
		}
	}
	
	
	
	public synchronized void registerHandler(Handler handler, int[] type) {
		mHandler = handler;
	}

	public synchronized void unRegisterHandler(Handler handler, int[] type) {
		mHandler = null;
	}

//	public synchronized void notifyMsg(int what) {
//		List<Handler> mHandlers = mHandlerMap.get(what);
//		if (mHandlers != null && mHandlers.size() != 0) {
//			for (Handler handler : mHandlers) {
//				handler.sendEmptyMessage(what);
//			}
//		}
//	}
	
	public synchronized void notifyMsg(int what) {
		if(mHandler != null) {
			mHandler.sendEmptyMessage(what);
		}
	}
	
	
	public synchronized void notifyMsg(int what, int arg1) {
		Message msg = new Message();
		msg.what = what;
		msg.arg1 = arg1;
		notifyMsg(msg);
	}
	
	public synchronized void notifyMsg(int what, int arg1, int arg2) {
		Message msg = new Message();
		msg.what = what;
		msg.arg1 = arg1;
		msg.arg2 = arg2;
		notifyMsg(msg);
	}
	
	public synchronized void notifyMsg(int what, int arg1, int arg2, Object obj) {
		Message msg = new Message();
		msg.what = what;
		msg.arg1 = arg1;
		msg.arg2 = arg2;
		msg.obj = obj;
		notifyMsg(msg);
	}
	
	public synchronized void notifyMsg(int what, Object obj) {
		Message msg = new Message();
		msg.what = what;
		msg.obj = obj;
		notifyMsg(msg);
	}
	
	
	public synchronized void notifyMsgWithBundle(int what, Bundle bundle) {
		Message msg = new Message();
		msg.what = what;
		msg.setData(bundle);
		notifyMsg(msg);
	}
	
//	private void notifyMsg(Message msg) {
//		L.d(thiz, "nofifyMsg msg = " + msg);
//		List<Handler> mHandlers = mHandlerMap.get(msg.what);
//		L.d(thiz, "mHandlers.size : " + mHandlers.size());
//		if (mHandlers != null && mHandlers.size() != 0) {
//			for (Handler handler : mHandlers) {
//				handler.sendMessage(msg);
//				L.d(thiz, "handler.sendMessage");
//			}
//		}
//	}
	
	
	/**
	 * 采用循环会ANR 暂不知道原因
	 * @param msg
	 */
	
	private void notifyMsg(Message msg) {
		if(mHandler != null) {
			mHandler.sendMessage(msg);
		}
	}

}
