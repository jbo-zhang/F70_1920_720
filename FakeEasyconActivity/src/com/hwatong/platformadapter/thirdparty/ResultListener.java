package com.hwatong.platformadapter.thirdparty;

public interface ResultListener {
	public String onResult(String result);
	public void onState(int state);
	
	//add++ 添加同步状态栏接口
	public void syncStatusBar(boolean show);
}
