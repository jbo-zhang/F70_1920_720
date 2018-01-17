package com.hwatong.settings.ota;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class UpdateReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent arg1) {
		context.startService(new Intent(context, UpdateService.class));
	}

}
