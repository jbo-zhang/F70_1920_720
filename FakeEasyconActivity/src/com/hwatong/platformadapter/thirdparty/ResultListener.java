package com.hwatong.platformadapter.thirdparty;

public interface ResultListener {
	/**
	 * 传递语义给第三方，得到第三方的处理结果
	 * @param result 要给第三方的语义
	 * @return  第三方处理之后的返回结果
	 */
	public String onResult(String result);
	
	/**
	 * 语音状态回调，对应于client的onDoAction， 用于通知第三方给第三方
	 * @param state
	 */
	public void onState(int state);
	
	/**
	 * 同步状态栏
	 * @param show
	 */
	public void syncStatusBar(boolean show);
}
