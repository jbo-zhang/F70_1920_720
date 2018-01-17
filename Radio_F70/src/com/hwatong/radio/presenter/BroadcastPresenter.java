package com.hwatong.radio.presenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.hwatong.radio.ui.Radio;
import com.hwatong.radio.ui.iview.IReceiverView;
import com.hwatong.utils.L;

public class BroadcastPresenter {
	private VoiceBroadcast voiceBroadcast;
	private static final String CLOSE_ACTION = "com.hwatong.voice.CLOSE_FM";
	private static final String FM_CMD_ACTION = "com.hwatong.voice.FM_CMD";
	private static final String AM_CMD_ACITON = "com.hwatong.voice.AM_CMD";
	private static final String FM_COLLECTION = "com.hwatong.voice.FM_COLLECTION";
	private static final String SELECT_CHANNEL = "com.hwatong.voice.SELECT_CHANNEL";
	
	private static final String thiz = BroadcastPresenter.class.getSimpleName();
	
	private IReceiverView iView;
	
	public BroadcastPresenter(IReceiverView view) {
		this.iView = view;
	}

	public void regVoiceBroadcast(Context context) {
		voiceBroadcast = new VoiceBroadcast();
		IntentFilter filter = new IntentFilter();
		filter.addAction(CLOSE_ACTION);
		filter.addAction(FM_CMD_ACTION);
		filter.addAction(AM_CMD_ACITON);
		filter.addAction(FM_COLLECTION);
		filter.addAction(SELECT_CHANNEL);
		context.registerReceiver(voiceBroadcast, filter);
	}
	
	public void unregVoiceBroadcast(Context context) {
		context.unregisterReceiver(voiceBroadcast);
	}
	
	private class VoiceBroadcast extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			L.d(thiz, "onReceive ! action: " + intent.getAction());
			//关闭收音机
			if(CLOSE_ACTION.equals(intent.getAction())) {
				iView.close();
			
			//播放FM频率
			} else if(FM_CMD_ACTION.equals(intent.getAction())) {
				//playFm(intent);
			
			//播放AM频率
			} else if(AM_CMD_ACITON.equals(intent.getAction())) {
				//playAm(intent);
			
			//收藏电台
			} else if(FM_COLLECTION.equals(intent.getAction())) {
				iView.collect();
				
			//播放收藏频道
			} else if(SELECT_CHANNEL.equals(intent.getAction())) {
				playPosition(intent);
				
			}
		}
		
	}
	
	private void playFm(Intent intent) {
		float freq = Float.parseFloat(intent.getStringExtra("frequency"));
		L.d(thiz, "receive freq : " + freq);
		if(freq < Radio.MIN_FREQUENCE_FM_FLOAT || freq > Radio.MAX_FREQUENCE_FM_FLOAT) {
			return;
		}
		iView.playChannel((int) (freq * 100));
	}
	
	private void playAm(Intent intent) {
		float freq = Float.parseFloat(intent.getStringExtra("frequency"));
		L.d(thiz, "receive freq : " + freq);
		if(freq < Radio.MIN_FREQUENCE_AM || freq > Radio.MAX_FREQUENCE_AM) {
			return;
		}
		iView.playChannel((int) freq);
	}
	
	private void playPosition(Intent intent) {
		int pos = Integer.parseInt(intent.getStringExtra("channel"));
		iView.playPosition(pos);
	}
	
	
	
}
