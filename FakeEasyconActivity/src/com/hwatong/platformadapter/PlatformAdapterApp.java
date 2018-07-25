package com.hwatong.platformadapter;

import android.app.Application;
import android.util.Log;

import com.iflytek.platformservice.PlatformHelp;

public class PlatformAdapterApp extends Application {
	@Override
	public void onCreate(){
		super.onCreate();

		Log.d("PlatformAdapterApp", "onCreate!");
		
		PlatformAdapterClient platformClient = new PlatformAdapterClient(getApplicationContext());
		PlatformHelp.getInstance().setPlatformClient(platformClient);
	}

	public static PlatformAdapterClient getPlatformClientInstance(){
		return (PlatformAdapterClient) PlatformHelp.getInstance().getPlatformClient();
	}

}
