package com.hwatong.platformadapter;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.hwatong.platformadapter.thirdparty.ThirdSpeechService;
import com.iflytek.platformservice.PlatformHelp;

public class PlatformAdapterApp extends Application{
	private PlatformAdapterClient platformClient;

	@Override
	public void onCreate(){
		super.onCreate();
		Log.d("PlatformAdapterApp", "onCreate");

		platformClient = new PlatformAdapterClient(getApplicationContext());
		PlatformHelp.getInstance().setPlatformClient(platformClient);
		startService(new Intent(getApplicationContext(), ThirdSpeechService.class));
	}

	public static PlatformAdapterClient getPlatformClientInstance(){
		return (PlatformAdapterClient) PlatformHelp.getInstance().getPlatformClient();
	}

}
