package com.hwatong.btphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Receiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("BtPhoneService", "onReceive " + intent.getAction());
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
			context.startService(new Intent(context, Service.class));
		}
	}
}
