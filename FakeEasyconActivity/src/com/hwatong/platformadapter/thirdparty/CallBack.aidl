package com.hwatong.platformadapter.thirdparty;

interface CallBack {
    /**
	 * 提供语义给第三方
	 */
	String onResult(String result);
    /**
	 *语音状态
	 */
    oneway void onStatus(int status);
}
