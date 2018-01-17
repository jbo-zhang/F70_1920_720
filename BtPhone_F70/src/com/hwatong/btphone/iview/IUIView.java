package com.hwatong.btphone.iview;

import java.util.List;

import com.hwatong.btphone.CallLog;
import com.hwatong.btphone.Contact;
import com.hwatong.btphone.bean.UICallLog;

public interface IUIView {
	
	/**
	 * 蓝牙已连接
	 */
	void showConnected();
	
	/**
	 * 蓝牙未连接
	 */
	void showDisconnected();
	
	/**
	 * 来电
	 */
	void showComing(UICallLog callLog);
	
	/**
	 * 去电
	 */
	void showCalling(UICallLog callLog);
	
	/**
	 * 通话
	 */
	void showTalking(UICallLog callLog);
	
	
	void showDTMFInput(UICallLog callLog);
	
	
	/**
	 * 
	 */
	void showIdel();
	
	/**
	 * 挂断
	 */
	void showHangUp(UICallLog callLog);
	
	/**
	 * 拒接
	 */
	void showReject(UICallLog callLog);
	
	/**
	 * 更新通信录
	 * @param list
	 */
	void updateBooks(List<Contact> list);
	
	/**
	 * 更新未接记录
	 * @param list
	 */
	void updateMissedLogs(List<CallLog> list);
	
	/**
	 * 更新拨打记录
	 * @param list
	 */
	void updateDialedLogs(List<CallLog> list);
	
	/**
	 * 更新接听记录
	 * @param list
	 */
	void updateReceivedLogs(List<CallLog> list);
	
	/**
	 * 更新全部记录
	 * @param list
	 */
	void updateAllLogs(List<CallLog> list);
	
	
	/**
	 * 通讯录开始加载
	 */
	void showBooksLoadStart();
	
	/**
	 * 通讯录加载中
	 */
	void showBooksLoading();
	
	/**
	 * 通讯录加载结束
	 * @param result
	 * @param reason
	 */
	void showBooksLoaded(boolean succeed, int reason);
	
	/**
	 * 同步通讯录已经加载完成
	 */
	void syncBooksAlreadyLoad();
	
	
	/**
	 * 通话记录开始加载
	 */
	void showLogsLoadStart(int type);
	
	/**
	 * 通话记录加载中
	 */
	void showLogsLoading(int type);
	
	/**
	 * 通话记录加载结束
	 * @param succeed
	 * @param reason
	 */
	void showLogsLoaded(int type, int result);
	
	/**
	 * 同步通话记录已经加载完成
	 */
	void syncLogsAlreadyLoad(int type);	
	
	/**
	 * 麦克风静音
	 * @param isMute
	 */
	void showMicMute(boolean isMute);
	
	/**
	 * 声音通道
	 * @param isCar
	 */
	void showSoundTrack(boolean isCar);
	
	/**
	 * 打开未接来电
	 */
	void toMissedCalls();
	
}
