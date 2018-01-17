package com.hwatong.btphone.imodel;

import java.util.List;

import android.content.Context;

import com.hwatong.btphone.CallLog;
import com.hwatong.btphone.Contact;

public interface IBTPhoneModel {
	/**
	 * 连接蓝牙电话服务
	 */
	void link(Context context);
	
	/**
	 * 断开蓝牙电话
	 */
	void unlink(Context context);
	
	/**
	 * 蓝牙是否连接
	 * @return
	 */
	boolean isBtConnected();
	
	/**
	 * 请求同步状态
	 */
	void sync();
	
	/**
	 * 拨打电话
	 * @param number 电话号码
	 */
	void dial(String number);
	
	/**
	 * 接听
	 */
	void pickUp();
	
	/**
	 * 挂断
	 */
	void hangUp();
	
	/**
	 * 拒接
	 */
	void reject();
	
	/**
	 * 通话中输入按键
	 * @param code 按键值
	 */
	void dtmf(char code);
	
	/**
	 * 切换麦克风静音
	 */
	void toggleMic();
	
	/**
	 * 切换声音通道
	 */
	void toggleTrack();
	
	/**
	 * 加载通讯录
	 */
	void loadBooks();
	
	
	/**
	 * 加载通话记录
	 */
	void loadLogs();
	
	/**
	 * 加载未接记录
	 */
	void loadMissedLogs();
	
	/**
	 * 加载拨打记录
	 */
	void loadDialedLogs();
	
	/**
	 *加载接听记录
	 */
	void loadReceivedLogs();
	
	
	List<Contact> getBooks();
	
	List<CallLog> getLogs();
	
	List<CallLog> getMissedLogs();
	
	List<CallLog> getReceivedLogs();
	
	List<CallLog> getDialedLogs();
	
	void syncLogsStatus(int type);
	
	
}
