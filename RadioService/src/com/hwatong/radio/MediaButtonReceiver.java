package com.hwatong.radio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

public class MediaButtonReceiver extends BroadcastReceiver {
	private static final String TAG = "RadioService";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive: " + intent);

		String intentAction = intent.getAction();

		if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
			KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

			if (event == null) {
				return;
			}

			int keyCode = event.getKeyCode();
			int action = event.getAction();
			long eventTime = event.getEventTime();

			switch (keyCode) {

			case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
			case KeyEvent.KEYCODE_MEDIA_NEXT: 
			case KeyEvent.KEYCODE_MEDIA_PAUSE: 
			case KeyEvent.KEYCODE_MEDIA_PLAY: {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (event.getRepeatCount() == 0) {
						Intent i = new Intent();
						i.setAction("com.hwatong.radio.service");

						if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
							i.putExtra("command", "play");
						} else if (keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
							i.putExtra("command", "pause");
						} else if (keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
							i.putExtra("command", "previous");
						} else if (keyCode == KeyEvent.KEYCODE_MEDIA_NEXT) {
							i.putExtra("command", "next");
						}

						context.startService(i);
					}
				}

				if (isOrderedBroadcast()) {
					abortBroadcast();
				}
				break;
			}
			}
		}
	}
}
