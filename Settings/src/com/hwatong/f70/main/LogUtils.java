package com.hwatong.f70.main;

import android.text.TextUtils;
import android.util.Log;

/**
 * LogUtils����˵��: 
 * 1 ֻ����ȼ����ڵ���LEVEL����־ 
 *   �����ڿ����Ͳ�Ʒ������ͨ���޸�LEVEL��ѡ���������־.
 *   ��LEVEL=NOTHING�����������е���־. 
 * 2 v,d,i,w,e����Ӧ��������. 
 *   ��������TAG����TAGΪ����Ϊ����Ĭ��TAG
 * 
 */
public class LogUtils {
	public static final int VERBOSE = 1;
	public static final int DEBUG = 2;
	public static final int INFO = 3;
	public static final int WARN = 4;
	public static final int ERROR = 5;
	public static final int NOTHING = 6;
	public static final int LEVEL = VERBOSE;
	public static final String SEPARATOR = ",";

	public static void v(String message) {
		if (LEVEL <= VERBOSE) {
			StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
			String tag = getDefaultTag(stackTraceElement);
			Log.v(tag, getLogInfo(stackTraceElement) + message);
		}
	}

	public static void v(String tag, String message) {
		if (LEVEL <= VERBOSE) {
			StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
			if (TextUtils.isEmpty(tag)) {
				tag = getDefaultTag(stackTraceElement);
			}
			Log.v(tag, getLogInfo(stackTraceElement) + message);
		}
	}

	public static void d(String message) {
		if (LEVEL <= DEBUG) {
			StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
			String tag = getDefaultTag(stackTraceElement);
			Log.d(tag, getLogInfo(stackTraceElement) + message);
		}
	}

	public static void d(String tag, String message) {
		if (LEVEL <= DEBUG) {
			StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
			if (TextUtils.isEmpty(tag)) {
				tag = getDefaultTag(stackTraceElement);
			}
			Log.d(tag, getLogInfo(stackTraceElement) + message);
		}
	}

	public static void i(String message) {
		if (LEVEL <= INFO) {
			StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
			String tag = getDefaultTag(stackTraceElement);
			Log.i(tag, getLogInfo(stackTraceElement) + message);
		}
	}

	public static void i(String tag, String message) {
		if (LEVEL <= INFO) {
			StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
			if (TextUtils.isEmpty(tag)) {
				tag = getDefaultTag(stackTraceElement);
			}
			Log.i(tag, getLogInfo(stackTraceElement) + message);
		}
	}

	public static void w(String message) {
		if (LEVEL <= WARN) {
			StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
			String tag = getDefaultTag(stackTraceElement);
			Log.w(tag, getLogInfo(stackTraceElement) + message);
		}
	}

	public static void w(String tag, String message) {
		if (LEVEL <= WARN) {
			StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
			if (TextUtils.isEmpty(tag)) {
				tag = getDefaultTag(stackTraceElement);
			}
			Log.w(tag, getLogInfo(stackTraceElement) + message);
		}
	}

	public static void e(String tag, String message) {
		if (LEVEL <= ERROR) {
			StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
			if (TextUtils.isEmpty(tag)) {
				tag = getDefaultTag(stackTraceElement);
			}
			Log.e(tag, getLogInfo(stackTraceElement) + message);
		}
	}

	/**
	 * ��ȡĬ�ϵ�TAG����. 
	 * ������MainActivity.java�е�������־���. 
	 * ��TAGΪMainActivity
	 */
	public static String getDefaultTag(StackTraceElement stackTraceElement) {
		String fileName = stackTraceElement.getFileName();
		String stringArray[] = fileName.split("\\.");
		String tag = stringArray[0];
		return tag;
	}

	/**
	 * �����־����������Ϣ
	 */
	public static String getLogInfo(StackTraceElement stackTraceElement) {
		StringBuilder logInfoStringBuilder = new StringBuilder();
		// ��ȡ�߳���
		String threadName = Thread.currentThread().getName();
		// ��ȡ�߳�ID
		long threadID = Thread.currentThread().getId();
		// ��ȡ�ļ���.��xxx.java
		String fileName = stackTraceElement.getFileName();
		// ��ȡ����.������+����
		String className = stackTraceElement.getClassName();
		// ��ȡ��������
		String methodName = stackTraceElement.getMethodName();
		// ��ȡ�����������
		int lineNumber = stackTraceElement.getLineNumber();

		logInfoStringBuilder.append("[ ");
		logInfoStringBuilder.append("threadID=" + threadID).append(SEPARATOR);
		logInfoStringBuilder.append("threadName=" + threadName).append(SEPARATOR);
		logInfoStringBuilder.append("fileName=" + fileName).append(SEPARATOR);
		logInfoStringBuilder.append("className=" + className).append(SEPARATOR);
		logInfoStringBuilder.append("methodName=" + methodName).append(SEPARATOR);
		logInfoStringBuilder.append("lineNumber=" + lineNumber);
		logInfoStringBuilder.append(" ] ");
		return logInfoStringBuilder.toString();
	}

}
