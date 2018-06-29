package com.hwatong.platformadapter;

import android.app.Application;
import android.content.Intent;

import com.hwatong.platformadapter.thirdparty.ThirdSpeechService;
import com.hwatong.platformadapter.utils.L;
import com.iflytek.platformservice.PlatformHelp;

public class PlatformAdapterApp extends Application{
	private static final String thiz = PlatformAdapterApp.class.getSimpleName();
	
	
	private PlatformAdapterClient platformClient;

	@Override
	public void onCreate(){
		super.onCreate();

		L.d(thiz, "onCreate!");
		
		platformClient = new PlatformAdapterClient(getApplicationContext());
		PlatformHelp.getInstance().setPlatformClient(platformClient);
		startService(new Intent(getApplicationContext(), ThirdSpeechService.class));
	}

	public static PlatformAdapterClient getPlatformClientInstance(){
		return (PlatformAdapterClient) PlatformHelp.getInstance().getPlatformClient();
	}

}
