package com.hwatong.btphone.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hwatong.btphone.service.BtPhoneService;

public class BootCompleteReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent arg1) {
		Intent intent = new Intent(arg0, BtPhoneService.class);
		arg0.startService(intent);
	}
}
