package com.hwatong.platformadapter.thirdparty;

import com.hwatong.platformadapter.thirdparty.CallBack;

interface IService {
    /**
     *注册语音事件回调
     */
	void registCallBack(CallBack callBack);
	/**
	 *反注册语音事件回调
	 */
	void unregistCallBack(CallBack callBack);
	/**
	 *打开语音界面
	 */
    void openVoiceHelp();
    /**
     *切换MIC
     * 1：语音占有MIC
     * 0：语音释放MIC
     */
    void switchSpeechMic(int state);	
}
