package com.hwatong.btphone.presenter;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;

import com.hwatong.btphone.CallLog;
import com.hwatong.btphone.Contact;
import com.hwatong.btphone.app.BtPhoneApplication;
import com.hwatong.btphone.bean.UICallLog;
import com.hwatong.btphone.constants.Constant;
import com.hwatong.btphone.imodel.IBTPhoneModel;
import com.hwatong.btphone.iview.IServiceView;
import com.hwatong.btphone.iview.IUIView;
import com.hwatong.btphone.model.HwatongModel;
import com.hwatong.btphone.util.L;

public class ServicePresenter implements IUIView, IBTPhoneModel{

	private static final String thiz = ServicePresenter.class.getSimpleName();
	
	private IServiceView iServiceView;
	
	private IBTPhoneModel iModel = new HwatongModel(this);

	public ServicePresenter(IServiceView iServiceView) {
		this.iServiceView = iServiceView;
	}
	
	//---------------------------------华丽的分割线----------------------------------------
	
	//---------------------------------IUIView----------------------------------------
	@Override
	public void showConnected() {
		BtPhoneApplication.getInstance().notifyMsg(Constant.MSG_SHOW_CONNECTED);
	}


	@Override
	public void showDisconnected() {
		iServiceView.hideWindow();
		BtPhoneApplication.getInstance().notifyMsg(Constant.MSG_SHOW_DISCONNECTED);
	}


	@Override
	public void showComing(UICallLog callLog) {
		L.d(thiz, "showComing");
		long start = System.currentTimeMillis();
		if(callLog == null) {
			return;
		}
		
		if(!isDialForground()) {
			iServiceView.showWindow(callLog);
		} else {
			BtPhoneApplication.getInstance().notifyMsg(Constant.MSG_SHOW_COMING, callLog);
		}
		L.d(thiz, "showComing cost : " + (System.currentTimeMillis() - start));
	}


	@Override
	public void showCalling(UICallLog callLog) {
		if(callLog == null) {
			return;
		}
		if(!isDialForground()) {
			iServiceView.gotoDialActivity(callLog);
		} else {
			BtPhoneApplication.getInstance().notifyMsg(Constant.MSG_SHOW_CALLING, callLog);
		}
	}


	@Override
	public void showTalking(UICallLog callLog) {
		if(callLog == null) {
			return;
		}
		if(!isDialForground()) {
			iServiceView.showTalking(callLog);
		} else {
			BtPhoneApplication.getInstance().notifyMsg(Constant.MSG_SHOW_TALKING, callLog);
		}
	}


	@Override
	public void showHangUp(UICallLog callLog) {
		if(callLog == null) {
			return;
		}
		BtPhoneApplication.getInstance().notifyMsg(Constant.MSG_SHOW_HANG_UP, callLog);
	}


	@Override
	public void showReject(UICallLog callLog) {
		if(callLog == null) {
			return;
		}
		if(!isDialForground()) {
			iServiceView.hideWindow();
		} else {
			BtPhoneApplication.getInstance().notifyMsg(Constant.MSG_SHOW_REJECT, callLog);
		}
	}


	@Override
	public void updateBooks(List<Contact> list) {
		BtPhoneApplication.getInstance().notifyMsg(Constant.MSG_UPDATE_BOOKS);
	}


	@Override
	public void updateMissedLogs(List<CallLog> list) {
		BtPhoneApplication.getInstance().notifyMsg(Constant.MSG_UPDATE_MISSED_LOGS);
	}


	@Override
	public void updateDialedLogs(List<CallLog> list) {
		BtPhoneApplication.getInstance().notifyMsg(Constant.MSG_UPDATE_DIALED_LOGS);
	}


	@Override
	public void updateReceivedLogs(List<CallLog> list) {
		BtPhoneApplication.getInstance().notifyMsg(Constant.MSG_UPDATE_RECEIVED_LOGS);
	}


	@Override
	public void updateAllLogs(List<CallLog> list) {
//		Bundle bundle = new Bundle();
//		bundle.putParcelableArrayList("logs", list);
		BtPhoneApplication.getInstance().notifyMsg(Constant.MSG_UPDATE_ALL_LOGS);
	}


	@Override
	public void showBooksLoadStart() {
		BtPhoneApplication.getInstance().notifyMsg(Constant.MSG_SHOW_BOOKS_LOAD_START);
	}


	@Override
	public void showBooksLoading() {
		BtPhoneApplication.getInstance().notifyMsg(Constant.MSG_SHOW_BOOKS_LOADING);		
	}


	@Override
	public void showBooksLoaded(boolean succeed, int reason) {
		BtPhoneApplication.getInstance().notifyMsg(Constant.MSG_SHOW_BOOKS_LOADED, succeed ? 1 : 0 , reason);		
	}


	@Override
	public void syncBooksAlreadyLoad() {
		BtPhoneApplication.getInstance().notifyMsg(Constant.MSG_SYNC_BOOKS_ALREADY_LOAD);
	}


	@Override
	public void showLogsLoadStart(int type) {
		BtPhoneApplication.getInstance().notifyMsg(Constant.MSG_SHOW_LOGS_LOAD_START, type);
	}


	@Override
	public void showLogsLoading(int type) {
		BtPhoneApplication.getInstance().notifyMsg(Constant.MSG_SHOW_LOGS_LOADING, type);		
	}


	@Override
	public void showLogsLoaded(int type, int result) {
		BtPhoneApplication.getInstance().notifyMsg(Constant.MSG_SHOW_LOGS_LOADED, type , result);		
	}


	@Override
	public void syncLogsAlreadyLoad(int type) {
		BtPhoneApplication.getInstance().notifyMsg(Constant.MSG_SYNC_LOGS_ALREADY_LOAD, type);		
	}


	@Override
	public void showMicMute(boolean isMute) {
		BtPhoneApplication.getInstance().notifyMsg(Constant.MSG_SHOW_MIC_MUTE, isMute ? 1 : 0);
	}


	@Override
	public void showSoundTrack(boolean isCar) {
		BtPhoneApplication.getInstance().notifyMsg(Constant.MSG_SHOW_SOUND_TRACK, isCar ? 1 : 0);
	}
	
	@Override
	public void showIdel() {
		BtPhoneApplication.getInstance().notifyMsg(Constant.MSG_SHOW_IDEL);
	}
	
	@Override
	public void showDTMFInput(UICallLog callLog) {
		BtPhoneApplication.getInstance().notifyMsg(Constant.MSG_SHOW_DTMF_INPUT, callLog);
	}
	
	@Override
	public void toMissedCalls() {
		//在BTPhoneService中处理了
	}

	
	//---------------------------------华丽的分割线----------------------------------------

	//---------------------------------IModel----------------------------------------
	
	@Override
	public void link(Context context) {
		iModel.link(context);
	}


	@Override
	public void unlink(Context context) {
		iModel.unlink(context);
	}
	
	@Override
	public void sync() {
		iModel.sync();
	}
	

	@Override
	public void dial(String number) {
		iModel.dial(number);
	}


	@Override
	public void pickUp() {
		iModel.pickUp();
	}


	@Override
	public void hangUp() {
		iModel.hangUp();
	}


	@Override
	public void reject() {
		iModel.reject();
	}


	@Override
	public void dtmf(char code) {
		iModel.dtmf(code);
	}


	@Override
	public void toggleMic() {
		iModel.toggleMic();
	}


	@Override
	public void toggleTrack() {
		iModel.toggleTrack();
	}


	@Override
	public void loadBooks() {
		iModel.loadBooks();
	}
	
	@Override
	public void loadLogs() {
		iModel.loadLogs();
	}


	@Override
	public void loadMissedLogs() {
		iModel.loadMissedLogs();
	}


	@Override
	public void loadDialedLogs() {
		iModel.loadDialedLogs();
	}

	@Override
	public void loadReceivedLogs() {
		iModel.loadReceivedLogs();
	}
	

	@Override
	public List<Contact> getBooks() {
		return iModel.getBooks();
	}

	@Override
	public List<CallLog> getLogs() {
		return iModel.getLogs();
	}

	@Override
	public List<CallLog> getMissedLogs() {
		return iModel.getMissedLogs();
	}

	@Override
	public List<CallLog> getReceivedLogs() {
		return iModel.getReceivedLogs();
	}

	@Override
	public List<CallLog> getDialedLogs() {
		return iModel.getDialedLogs();
	}

	

	@Override
	public boolean isBtConnected() {
		return iModel.isBtConnected();
	}
	
	@Override
	public void syncLogsStatus(int type) {
		iModel.syncLogsStatus(type);
	}
	
	//---------------------------------华丽的分割线----------------------------------------
	
	//---------------------------------方法----------------------------------------
	
	protected boolean isDialForground() {
		ActivityManager am = (ActivityManager) BtPhoneApplication.getInstance().getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> cn = am.getRunningTasks(1);
		RunningTaskInfo taskInfo = cn.get(0);
		ComponentName name = taskInfo.topActivity;
		if ("com.hwatong.btphone.activity.DialActivity".equals(name.getClassName())
				|| "com.hwatong.btphone.activity.CallLogActivity".equals(name.getClassName())
				|| "com.hwatong.btphone.activity.ContactsListActivity".equals(name.getClassName())
				|| "com.hwatong.btphone.activity.PhoneActivity".equals(name.getClassName())) {
			return true;
		}
		return false;
	}

	
}
