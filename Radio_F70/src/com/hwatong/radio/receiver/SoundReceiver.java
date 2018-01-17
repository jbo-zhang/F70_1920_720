package com.hwatong.radio.receiver;

import com.hwatong.radio.ui.Radio;
import com.hwatong.utils.L;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SoundReceiver extends BroadcastReceiver{
	
	private final static String thiz = SoundReceiver.class.getSimpleName();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		L.d(thiz, "onReceive action : " + intent.getAction());
		if(intent.getAction().equals("com.hwatong.voice.OPEN_AM")) {
			Intent activityIntent = new Intent(context, Radio.class);
			activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			activityIntent.putExtra("type", 1);
			context.startActivity(activityIntent);
		}
	}
}
