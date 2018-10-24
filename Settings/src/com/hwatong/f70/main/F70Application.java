package com.hwatong.f70.main;


import com.hwatong.statusbarinfo.aidl.IStatusBarInfo;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

public class F70Application extends Application {
	private IStatusBarInfo iStatusBarInfo;
	private static F70Application f70Application;
	public static int isShowProgram = 0;
	public static boolean isShowWifi = false;

	@Override
	public void onCreate() {
		super.onCreate();
		f70Application = this;
		bindStatusBarService();
	}

	public static F70Application getInstance() {
		return f70Application;
	}

	private ServiceConnection StatusBarConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			iStatusBarInfo = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			iStatusBarInfo = IStatusBarInfo.Stub.asInterface(service);
			//sendCurrentPageName("setting_main");
		}
	};

	//绑定systemui服务
	private void bindStatusBarService() {
		Intent intent = new Intent();
		intent.setAction("com.remote.hwatong.statusinfoservice");
		bindService(intent, StatusBarConnection, BIND_AUTO_CREATE);
	}

	//发送当前页面信息
	public void sendCurrentPageName(String name) {
		try {
			if (iStatusBarInfo != null)
				iStatusBarInfo.setCurrentPageName(name);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
