package com.hwatong.platformadapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class FakeEasyconActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startApp("net.easyconn");
		finish();
	}

	private void startApp(String app){
		Intent intent = getApplicationContext().getPackageManager().getLaunchIntentForPackage(app);
		if(intent != null) {
			try{
				startActivity(intent);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}
}
