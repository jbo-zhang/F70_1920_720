package com.hwatong.radio.receiver;

import com.hwatong.radio.ui.Radio;
import com.hwatong.utils.L;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

public class SoundReceiver extends BroadcastReceiver{
	
	private final static String thiz = SoundReceiver.class.getSimpleName();
	
	private static final String OPEN_AM = "com.hwatong.voice.OPEN_AM";
	private static final String FM_CMD_ACTION = "com.hwatong.voice.FM_CMD";
	private static final String AM_CMD_ACITON = "com.hwatong.voice.AM_CMD";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		L.d(thiz, "onReceive action : " + intent.getAction());
		if(OPEN_AM.equals(intent.getAction())) {
			Intent activityIntent = new Intent(context, Radio.class);
			activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			activityIntent.putExtra("type", 1);
			context.startActivity(activityIntent);
		} else if(FM_CMD_ACTION.equals(intent.getAction()) || AM_CMD_ACITON.equals(intent.getAction())){
			L.d(thiz, "CMD_ACTION");
			SystemClock.sleep(500);
			Intent activityIntent = new Intent(context, Radio.class);
			activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(activityIntent);
		}
	}
}
