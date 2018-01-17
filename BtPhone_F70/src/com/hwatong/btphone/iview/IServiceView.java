package com.hwatong.btphone.iview;

import com.hwatong.btphone.bean.UICallLog;

public interface IServiceView {
	void showWindow(UICallLog callLog);
	void hideWindow();
	void showTalking(UICallLog callLog);
	
	void gotoDialActivity(UICallLog callLog);
}
