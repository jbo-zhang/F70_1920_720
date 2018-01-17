package com.hwatong.btphone.constants;

import android.os.Message;


public class Constant {

	public static final int RESULT_FINISH_ACTIVITY = 10;//返回DialActivity时是否结束activity
	
	public static final int MSG_CLOSE = 109;
	public static final int MSG_OPEN_MISSED_CALLS = 110; 
	
	public static final int MSG_SHOW_CONNECTED = 999;
	public static final int MSG_SHOW_DISCONNECTED = 1000;
	public static final int MSG_SHOW_COMING = 1001;
	public static final int MSG_SHOW_CALLING = 998;
	public static final int MSG_SHOW_TALKING = 1002;
	public static final int MSG_SHOW_HANG_UP = 1003;
	public static final int MSG_SHOW_REJECT = 1004;
	public static final int MSG_SHOW_IDEL = 104;
	public static final int MSG_UPDATE_BOOKS = 1005;
	public static final int MSG_UPDATE_MISSED_LOGS = 1006;
	public static final int MSG_UPDATE_DIALED_LOGS = 1007;
	public static final int MSG_UPDATE_RECEIVED_LOGS = 1008;
	public static final int MSG_UPDATE_ALL_LOGS = 1009;
	public static final int MSG_SHOW_BOOKS_LOAD_START = 1010;
	public static final int MSG_SHOW_BOOKS_LOADING = 1011;
	public static final int MSG_SHOW_BOOKS_LOADED = 1012;
	public static final int MSG_SHOW_LOGS_LOAD_START = 1013;
	public static final int MSG_SHOW_LOGS_LOADING = 1014;
	public static final int MSG_SHOW_LOGS_LOADED = 1015;
	public static final int MSG_SHOW_MIC_MUTE = 101;
	public static final int MSG_SHOW_SOUND_TRACK = 1017;
	public static final int MSG_SYNC_BOOKS_ALREADY_LOAD = 1018;
	public static final int MSG_SYNC_LOGS_ALREADY_LOAD = 1019;
	
	public static final int MSG_SHOW_DTMF_INPUT = 1020;
	
}
