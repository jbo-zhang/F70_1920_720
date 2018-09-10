package com.hwatong.f70.main;

import android.app.ActivityThread;
import android.canbus.ICanbusService;
import android.os.Build;
import android.os.RemoteException;
import android.os.ServiceManager;

public class ConfigrationVersion {

	private ICanbusService iCanbusService;

	//
	// private static final String LOW = "F70_L";
	// private static final String MIDDLE = "F70_M";
	// private static final String HIGHT = "F70_H";

	private static final int LOW = 1;
	private static final int MIDDLE_ELITE = 2;
	private static final int MIDDLE_LUXURY = 3;
	private static final int HIGHT = 4;

	private int currentConfig = -1;

	private ConfigrationVersion() {
		init();
	}

	private static class F70ConfigrationVersion {
		private static final ConfigrationVersion instance = new ConfigrationVersion();
	}

	public static ConfigrationVersion getInstance() {
		return F70ConfigrationVersion.instance;
	}

	private void init() {
		this.iCanbusService = ICanbusService.Stub.asInterface(ServiceManager
				.getService("canbus"));
		try {
			currentConfig = iCanbusService.getCarConfigType();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		LogUtils.d("init over currentConfig is " + currentConfig);
	}

	public boolean isHight() {
		return currentConfig == HIGHT;
	}

	public boolean isMiddleElite() {
		return currentConfig == MIDDLE_ELITE;
	}

	public boolean isMiddleLuxury() {
		return currentConfig == MIDDLE_LUXURY;
	}

	public boolean isLow() {
		return currentConfig == LOW;
	}
}
