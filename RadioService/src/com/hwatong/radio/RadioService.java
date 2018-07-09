package com.hwatong.radio;

import java.lang.ref.WeakReference;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.IAudioFocusDispatcher;
import android.media.IAudioService;
import android.media.MediaRecorder;
import android.media.AudioSystem;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.os.SystemProperties;

public class RadioService extends Service {//implements AudioManager.OnAudioFocusChangeListener {

	private static final String TAG = "RadioService";
	private static final boolean DBG = true;

	// 收音机的一些固定频率
	private static final int MIN_FREQUENCE_FM = 8750;
	private static final int MAX_FREQUENCE_FM = 10800;
	private static final int DEF_FREQUENCE_FM = 8750;
	private static final int MIN_FREQUENCE_AM = 531;
	private static final int MAX_FREQUENCE_AM = 1629;
	private static final int DEF_FREQUENCE_AM = 612;

	private static final String SharedPreferences_Current_Channel = "spf_current_channel";
	private static final String SharedPreferences_Radio_AM = "spf_radio_am";
	private static final String SharedPreferences_Radio_FM = "spf_radio_fm";
	private static final String SharedPreferences_Radio_Collection = "spf_radio_collection";
	private static final String Num_Current_Band = "Num_Current_Band";
	private static final String Num_Current_Channel_Of_AM = "Num_Current_Channel_Of_AM";
	private static final String Num_Current_Channel = "Num_Current_Channel";
	private static final String Num_Current_Channel_Of_FM = "Num_Current_Channel_Of_FM";
	private static final String AM_Data = "am_";
	private static final String FM_Data = "fm_";
	private static final String Collection_Data = "collection_";

	static {
		System.loadLibrary("fmradio_jni");
	}

	private int mCurrentBand = -1;
	private int[] mCurrentChannel = { -1, -1 };

	private final List<Channel> mFMRadioList = new ArrayList<Channel>();
	private final List<Channel> mAMRadioList = new ArrayList<Channel>();
	private final List<Channel> mCollectionList = new ArrayList<Channel>();

	private ComponentName mComponentName;
	private AudioManager mAudioManager;
	private IAudioService mAudioService;

	private SimpleDateFormat formatter;

	private final ServiceImpl mBinder = new ServiceImpl(this);

	@SuppressLint("SimpleDateFormat")
		@Override
		public void onCreate() {
			super.onCreate();
			if (DBG) Log.d(TAG, "onCreate");

			mComponentName = new ComponentName(getPackageName(), MediaButtonReceiver.class.getName());
			mAudioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
			mAudioService = IAudioService.Stub.asInterface(ServiceManager.getService(Context.AUDIO_SERVICE));

			formatter = new SimpleDateFormat("yyyy年MM月dd日   HH:mm:ss");

			//		radioSetVolume(0);
			//		radioSetMute(true);

			readRadioList("collection");

			if (!initCheck(0)) {
				if (mRadioThread == null) {
					mRadioThread = new RadioThread(OP_INIT);
					mRadioThread.start();
				}
			} else {
				doInit2();
				mRadioStatus = OP_IDLE;
			}

			IntentFilter filter = new IntentFilter("com.hwatong.voice.CLOSE_FM");
			filter.addAction("com.hwatong.voice.FM_CMD");
			filter.addAction("com.hwatong.voice.AM_CMD");
			filter.addAction("com.hwatong.voice.BACK_Radio");
			//filter.addAction(AudioManager.MASTER_MUTE_CHANGED_ACTION);
			registerReceiver(mVoiceCmdListener, filter);
		}

	@Override
		public void onDestroy() {
			unregisterReceiver(mVoiceCmdListener);

			if (mRadioThread != null) {
				mRadioThread.requestExitAndWait();
				mRadioThread = null;
			}

			mRequestPlay = false;
			//		mhardwareHandle.sendMessageDelayed(mhardwareHandle.obtainMessage(MSG_MUTE, 1, 0) , 0);
			pausePlayback();

			//		mAudioManager.abandonAudioFocus(this);
			abandonAudioFocus();
			mAudioManager.unregisterMediaButtonEventReceiver(mComponentName);

			mVolumeHandler.removeCallbacksAndMessages(null);
			mHandler.removeCallbacksAndMessages(null);

			if (DBG) Log.d(TAG, "onDestroy");
			super.onDestroy();
		}

	@Override
		public IBinder onBind(Intent intent) {
			if (DBG) Log.d(TAG, "onBind");
			return mBinder;
		}

	@Override
		public void onRebind(Intent intent) {
			if (DBG) Log.d(TAG, "onRebind");
		}

	@Override
		public boolean onUnbind(Intent intent) {
			if (DBG) Log.d(TAG, "onUnbind");
			return super.onUnbind(intent);
		}

	@Override
		public int onStartCommand(Intent intent, int flags, int startId) {
			if (DBG) Log.d(TAG, "onStartCommand " + intent);

			if (intent != null) {
				String action = intent.getAction();
				String cmd = intent.getStringExtra("command");
				if (DBG) Log.i(TAG, "onStartCommand " + action + " / " + cmd);

				if ("info".equals(cmd)) {
					if (mCurrentBand != -1)
						sendInfoUpdate(mCurrentBand, mCurrentChannel[mCurrentBand], mRequestPlay);

				} else if ("play".equals(cmd)) {
					play();

				} else if ("pause".equals(cmd)) {
					pause();

				} else if ("previous".equals(cmd)) {
					
					//为了停止预览，因为预览状态服务是不知道的。
					sendButtonBroadcast(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
					
					//点击seek键后暂停搜索
					if (mRadioThread != null) {
						mRadioThread.requestExitAndWait();
						mRadioThread = null;
						notifyStatusChanged();
						return START_STICKY;
					}
					
					
					if (mRadioThread == null && mCurrentBand != -1) {
						if ((mCurrentBand == 0 && mFMRadioList.size() == 0)
								|| (mCurrentBand == 1 && mAMRadioList.size() == 0)) {

							mRadioThread = new RadioThread(OP_SEEK_DOWN);
							mRadioThread.start();

						} else {
							int freq = selectPreChannel();
							if (freq != -1) {
								synchronized (mOpLock) {
									tuneTo(mCurrentBand, freq);
								}
							}
						}
					}
				} else if ("next".equals(cmd)) {

					//为了停止预览，因为预览状态服务是不知道的。
					sendButtonBroadcast(KeyEvent.KEYCODE_MEDIA_NEXT);
					//点击seek键后暂停搜索
					if (mRadioThread != null) {
						mRadioThread.requestExitAndWait();
						mRadioThread = null;
						notifyStatusChanged();
						return START_STICKY;
					}
					
					
					if (mRadioThread == null && mCurrentBand != -1) {
						if ((mCurrentBand == 0 && mFMRadioList.size() == 0)
								|| (mCurrentBand == 1 && mAMRadioList.size() == 0)) {

							mRadioThread = new RadioThread(OP_SEEK_UP);
							mRadioThread.start();

						} else {
							int freq = selectNextChannel();
							if (freq != -1) {
								synchronized (mOpLock) {
									tuneTo(mCurrentBand, freq);
								}
							}
						}
					}

				} else if ("tune_down".equals(cmd)) {
					tuneDown();
					if (!mRequestPlay) {
						play();
					}
				} else if ("tune_up".equals(cmd)) {
					tuneUp();
					if (!mRequestPlay) {
						play();
					}
				} else if ("seek_down".equals(cmd)) {
					seekDown();
					if (!mRequestPlay) {
						play();
					}
				} else if ("seek_up".equals(cmd)) {
					seekUp();
					if (!mRequestPlay) {
						play();
					}
				}
			}

			return START_STICKY;
		}
	
	/**
	 * 用于通知客户端停止电台预览
	 * @param key
	 */
	private void sendButtonBroadcast(int key) {
		Intent intent = new Intent("android.intent.action.MEDIA_BUTTON_F70");
		intent.putExtra("key", key);
		sendBroadcast(intent);
	}
	

	private final List<Callback> mCallbacks = new ArrayList<Callback>();

	private final class Callback implements IBinder.DeathRecipient {
		final ICallback mCallback;

		Callback(ICallback callback) {
			mCallback = callback;
		}

		@Override
			public void binderDied() {
				if (DBG) Log.d(TAG, "callback died");

				synchronized (mCallbacks) {
					mCallbacks.remove(this);
				}
				if (mCallback != null) {
					mCallback.asBinder().unlinkToDeath(this, 0);
				}
			}
	}

	private void notifyStatusChanged() {
		synchronized(mCallbacks) {
			int size = mCallbacks.size();
			for (int i = 0; i < size; i++) {
				Callback cb = mCallbacks.get(i);
				try {
					cb.mCallback.onStatusChanged();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void notifyChannelListChanged(int band) {
		synchronized(mCallbacks) {
			int size = mCallbacks.size();
			for (int i = 0; i < size; i++) {
				Callback cb = mCallbacks.get(i);
				try {
					cb.mCallback.onChannelListChanged(band);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void notifyFavorChannelListChanged() {
		synchronized(mCallbacks) {
			int size = mCallbacks.size();
			for (int i = 0; i < size; i++) {
				Callback cb = mCallbacks.get(i);
				try {
					cb.mCallback.onFavorChannelListChanged();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void notifyChannelChanged() {
		synchronized(mCallbacks) {
			int size = mCallbacks.size();
			for (int i = 0; i < size; i++) {
				Callback cb = mCallbacks.get(i);
				try {
					cb.mCallback.onChannelChanged();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void notifyDisplayChanged(int band, int freq) {
		synchronized(mCallbacks) {
			int size = mCallbacks.size();
			for (int i = 0; i < size; i++) {
				Callback cb = mCallbacks.get(i);
				try {
					cb.mCallback.onDisplayChanged(band, freq);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void registerCallback(ICallback callback) {
		synchronized (mCallbacks) {
			IBinder binder = callback.asBinder();
			int size = mCallbacks.size();
			for (int i = 0; i < size; i++) {
				Callback test = mCallbacks.get(i);
				if (binder.equals(test.mCallback.asBinder())) {
					// listener already added
					return ;
				}
			}

			try {
				Callback cb = new Callback(callback);
				binder.linkToDeath(cb, 0);
				mCallbacks.add(cb);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	public void unregisterCallback(ICallback callback) {
		synchronized (mCallbacks) {
			IBinder binder = callback.asBinder();
			Callback cb = null;
			int size = mCallbacks.size();
			for (int i = 0; i < size && cb == null; i++) {
				Callback test = mCallbacks.get(i);
				if (binder.equals(test.mCallback.asBinder())) {
					cb = test;
				}
			}

			if (cb != null) {
				mCallbacks.remove(cb);
				binder.unlinkToDeath(cb, 0);
			}
		}
	}

	public int[] getStatus() {
		synchronized (mLock) {
			return new int[] { mRadioStatus, mRequestPlay ? 1 : 0 };
		}
	}

	public List<Channel> getChannelList(int band) {
		List<Channel> list = new ArrayList<Channel>();

		if (band == 0) {
			synchronized (mFMRadioList) {
				for (int i = 0; i < mFMRadioList.size(); i++) {
					list.add(new Channel(
								mFMRadioList.get(i).frequence,
								mFMRadioList.get(i).from));
				}
			}
		} else {
			synchronized (mAMRadioList) {
				for (int i = 0; i < mAMRadioList.size(); i++) {
					list.add(new Channel(
								mAMRadioList.get(i).frequence,
								mAMRadioList.get(i).from));
				}
			}
		}

		return list;
	}

	public int addChannel(int band, int freq) {
		if (band == 0) {
			synchronized (mFMRadioList) {
				int i = 0;
				for (; i < mFMRadioList.size(); i++) {
					if (mFMRadioList.get(i).frequence == freq)
						break;
					if (mFMRadioList.get(i).frequence > freq)
						break;
				}
				if (i == mFMRadioList.size()) {
					mFMRadioList.add(new Channel(freq, 0));
				} else {
					if (mFMRadioList.get(i).frequence == freq) {
						if (mFMRadioList.get(i).from == 0)
							return -2;
						mFMRadioList.remove(i);
					}
					mFMRadioList.add(i, new Channel(freq, 0));
				}

				final Editor editor = getSharedPreferences(
						SharedPreferences_Radio_FM, MODE_PRIVATE).edit();
				editor.clear();
				editor.commit();

				i = 0;
				for (; i < mFMRadioList.size(); i++) {
					editor.putInt(FM_Data + i, mFMRadioList.get(i).frequence);
				}
				editor.putInt(FM_Data + i, 0);
				editor.commit();
			}

			readRadioList("FM");
		} else {
			synchronized (mAMRadioList) {
				int i = 0;
				for (; i < mAMRadioList.size(); i++) {
					if (mAMRadioList.get(i).frequence == freq)
						break;
					if (mAMRadioList.get(i).frequence > freq)
						break;
				}
				if (i == mAMRadioList.size()) {
					mAMRadioList.add(new Channel(freq, 0));
				} else {
					if (mAMRadioList.get(i).frequence == freq) {
						if (mAMRadioList.get(i).from == 0)
							return -2;
						mAMRadioList.remove(i);
					}
					mAMRadioList.add(i, new Channel(freq, 0));
				}

				final Editor editor = getSharedPreferences(
						SharedPreferences_Radio_AM, MODE_PRIVATE).edit();
				editor.clear();
				editor.commit();

				i = 0;
				for (; i < mAMRadioList.size(); i++) {
					editor.putInt(AM_Data + i, mAMRadioList.get(i).frequence);
				}
				editor.putInt(AM_Data + i, 0);
				editor.commit();
			}

			readRadioList("AM");
		}

		return 0;
	}

	public void removeChannel(int band, int freq) {
	}

	public List<Channel> getFavorChannelList() {
		List<Channel> list = new ArrayList<Channel>();

		synchronized (mCollectionList) {
			for (int i = 0; i < mCollectionList.size(); i++) {
				list.add(new Channel(
							mCollectionList.get(i).frequence,
							mCollectionList.get(i).from));
			}
		}

		return list;
	}

	public void addFavorChannel(int freq) {
		synchronized (mCollectionList) {
			int i = 0;
			for (; i < mCollectionList.size(); i++) {
				if (mCollectionList.get(i).frequence == freq)
					return;
				if (mCollectionList.get(i).frequence > freq)
					break;
			}
			if (i == mCollectionList.size())
				mCollectionList.add(new Channel(freq, 0));
			else
				mCollectionList.add(i, new Channel(freq, 0));

			Editor editor = getSharedPreferences(
					SharedPreferences_Radio_Collection, MODE_PRIVATE).edit();
			editor.clear();
			editor.commit();

			i = 0;
			for (; i < mCollectionList.size(); i++) {
				editor.putInt(Collection_Data + i, mCollectionList.get(i).frequence);
			}
			editor.putInt(Collection_Data + i, 0);
			editor.commit();
			editor = null;
		}

		readRadioList("collection");
	}

	public void removeFavorChannel(int freq) {
		synchronized (mCollectionList) {
			int i = 0;
			for (; i < mCollectionList.size(); i++) {
				if (mCollectionList.get(i).frequence == freq)
					break;
			}
			if (i < mCollectionList.size()) {
				mCollectionList.remove(i);

				Editor editor = getSharedPreferences(
						SharedPreferences_Radio_Collection, MODE_PRIVATE).edit();
				editor.clear();
				editor.commit();

				i = 0;
				for (; i < mCollectionList.size(); i++) {
					editor.putInt(Collection_Data + i, mCollectionList.get(i).frequence);
				}
				editor.putInt(Collection_Data + i, 0);
				editor.commit();
				editor = null;
			}
		}

		readRadioList("collection");
	}

	public int getCurrentBand() {
		return mCurrentBand;
	}

	public int getCurrentChannel(int band) {
		if (band != -1)
			return mCurrentChannel[band];
		return -1;
	}

	public void band() {
		if (DBG) Log.d(TAG, "band");
		if (mRadioThread != null) {
//			if (mRadioThread.getStatus() == OP_INIT)
//				return;
			mRadioThread.requestExitAndWait();
			mRadioThread = null;
		}

		int band;
		if (mCurrentBand == 0)
			band = 1;
		else
			band = 0;
		if (band == 1 && !initCheck(1)) {
			mCurrentBand = 1;
			scan();
		} else {
			synchronized (mOpLock) {
				tuneTo(band, mCurrentChannel[band]);
			}
		}
	}

	public void tuneTo(int frequence, boolean add) {
		if (DBG) Log.d(TAG, "tuneTo " + frequence + ", add " + add + ", current" + mCurrentChannel[0] + "/" + mCurrentChannel[1]);

		if (mRadioThread != null) {
//			if (mRadioThread.getStatus() == OP_INIT)
//				return;
			mRadioThread.requestExitAndWait();
			mRadioThread = null;
		}

		if(frequence == -1) {
			return;
		}
		
		
		int band = MIN_FREQUENCE_FM <= frequence && frequence <= MAX_FREQUENCE_FM ? 0 : 1;
		if (add)
			addChannelInListSingle(band, frequence, 2);

		synchronized (mOpLock) {
			tuneTo(band, frequence);
		}
	}

	public void tuneDown() {
		if (DBG) Log.d(TAG, "tuneDown");

		if (mRadioThread != null) {
//			if (mRadioThread.getStatus() == OP_INIT)
//				return;
			mRadioThread.requestExitAndWait();
			mRadioThread = null;
			return;
		}

		synchronized (mOpLock) {
			if (mCurrentBand == 0) {
				int freq = mCurrentChannel[0];
				freq -= 10;
				if (freq < MIN_FREQUENCE_FM)
					freq = MAX_FREQUENCE_FM;
				addChannelInListSingle(mCurrentBand, freq, 2);
				tuneTo(mCurrentBand, freq);
			} else if (mCurrentBand == 1) {
				int freq = mCurrentChannel[1];
				freq -= 9;
				if (freq < MIN_FREQUENCE_AM)
					freq = MAX_FREQUENCE_AM;
				addChannelInListSingle(mCurrentBand, freq, 2);
				tuneTo(mCurrentBand, freq);
			}
		}
	}

	public void tuneUp() {
		if (DBG) Log.d(TAG, "tuneUp");

		if (mRadioThread != null) {
//			if (mRadioThread.getStatus() == OP_INIT)
//				return;
			mRadioThread.requestExitAndWait();
			mRadioThread = null;
			return;
		}

		synchronized (mOpLock) {
			if (mCurrentBand == 0) {
				int freq = mCurrentChannel[0];
				freq += 10;
				if (freq > MAX_FREQUENCE_FM)
					freq = MIN_FREQUENCE_FM;
				addChannelInListSingle(mCurrentBand, freq, 2);
				tuneTo(mCurrentBand, freq);
			} else if (mCurrentBand == 1) {
				int freq = mCurrentChannel[1];
				freq += 9;
				if (freq > MAX_FREQUENCE_AM)
					freq = MIN_FREQUENCE_AM;
				addChannelInListSingle(mCurrentBand, freq, 2);
				tuneTo(mCurrentBand, freq);
			}
		}
	}

	public void seekDown() {
		if (DBG) Log.d(TAG, "seekDown");

		if (mRadioThread != null) {
			if (DBG) Log.d(TAG, "seekDown 2");
//			if (mRadioThread.getStatus() == OP_INIT)
//				return;
			int status = mRadioThread.getStatus();
			mRadioThread.requestExitAndWait();
			mRadioThread = null;
			if (status == OP_SEEK_DOWN)
				return;
		}

		if (mCurrentBand != -1) {
			mRadioThread = new RadioThread(OP_SEEK_DOWN);
			mRadioThread.start();
		}
	}

	public void seekUp() {
		if (DBG) Log.d(TAG, "seekUp");

		if (mRadioThread != null) {
//			if (mRadioThread.getStatus() == OP_INIT)
//				return;
			int status = mRadioThread.getStatus();
			mRadioThread.requestExitAndWait();
			mRadioThread = null;
			if (status == OP_SEEK_UP)
				return;
		}

		if (mCurrentBand != -1) {
			mRadioThread = new RadioThread(OP_SEEK_UP);
			mRadioThread.start();
		}
	}

	public void scan() {
		if (DBG) Log.d(TAG, "scan");

		if (mRadioThread != null) {
//			if (mRadioThread.getStatus() == OP_INIT)
//				return;
			int status = mRadioThread.getStatus();
			mRadioThread.requestExitAndWait();
			mRadioThread = null;
			if (status == OP_SCAN || status == OP_INIT)
				return;
		}

		if (mCurrentBand != -1) {
			mRadioThread = new RadioThread(OP_SCAN);
			mRadioThread.start();
		}
	}

	public void pause() {
		if (DBG) Log.d(TAG, "pause");

		synchronized (mLock) {
			mRequestPlay = false;
			pausePlayback();
		}
		notifyStatusChanged();
		if (mCurrentBand != -1)
			sendInfoUpdate(mCurrentBand, mCurrentChannel[mCurrentBand], mRequestPlay);
	}

	public void play() {
		if (DBG) Log.d(TAG, "play");

		mAudioManager.registerMediaButtonEventReceiver(mComponentName);
		//		mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		requestAudioFocus(AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

		synchronized (mLock) {
			mRequestPauseByAF = false;
			mRequestPlay = true;
			if (mRadioStatus == OP_IDLE)
				resumePlayback();
		}
		notifyStatusChanged();

		if (mCurrentBand != -1)
			sendInfoUpdate(mCurrentBand, mCurrentChannel[mCurrentBand], mRequestPlay);
	}

	public void playPause() {
		if (DBG) Log.d(TAG, "playPause");

		if (mRequestPlay)
			pause();
		else
			play();
	}

	private static class ServiceImpl extends IService.Stub {
		final WeakReference<RadioService> mService;

		private static class AsyncOp {
			private boolean done;

			public synchronized void exec(Handler handler, int what) {
				done = false;

				Message m = Message.obtain(handler, what, this);
				handler.sendMessage(m);

				while (!done) {
					try {
						wait();
					} catch (InterruptedException e){
						e.printStackTrace();
					}
				}
			}

			public synchronized void complete() {
				done = true;
				notifyAll();
			}
		}

		private static class TuneToOp extends AsyncOp {
			public final int frequence;
			public final boolean add;
			public TuneToOp(int frequence, boolean add) {
				this.frequence = frequence;
				this.add = add;
			}
		}

		private static final int MSG_PLAYPAUSE = 1;
		private static final int MSG_PLAY = 2;
		private static final int MSG_PAUSE = 3;
		private static final int MSG_SCAN = 4;
		private static final int MSG_SEEK_UP= 5;
		private static final int MSG_SEEK_DOWN = 6;
		private static final int MSG_TUNE_UP = 7;
		private static final int MSG_TUNE_DOWN = 8;
		private static final int MSG_TUNE_TO = 9;
		private static final int MSG_BAND = 10;

		private final Handler mHandler = new Handler() {
			@Override
				public void handleMessage(Message msg) {
					final AsyncOp op = (AsyncOp)msg.obj;

					switch (msg.what) {

						case MSG_PLAYPAUSE:
							mService.get().playPause();
							break;
						case MSG_PLAY:
							mService.get().play();
							break;
						case MSG_PAUSE:
							mService.get().pause();
							break;
						case MSG_SCAN:
							mService.get().scan();
							break;
						case MSG_SEEK_UP:
							mService.get().seekUp();
							break;
						case MSG_SEEK_DOWN:
							mService.get().seekDown();
							break;
						case MSG_TUNE_UP:
							mService.get().tuneUp();
							break;
						case MSG_TUNE_DOWN:
							mService.get().tuneDown();
							break;
						case MSG_TUNE_TO:
							mService.get().tuneTo(((TuneToOp)op).frequence, ((TuneToOp)op).add);
							break;
						case MSG_BAND:
							mService.get().band();
							break;
					}

					op.complete();
				}
		};

		ServiceImpl(RadioService service) {
			mService = new WeakReference<RadioService>(service);
		}

		@Override
			public void registerCallback(ICallback cb) {
				mService.get().registerCallback(cb);
			}

		@Override
			public void unregisterCallback(ICallback cb) {
				mService.get().unregisterCallback(cb);
			}

		@Override
			public int[] getStatus() {
				return mService.get().getStatus();
			}

		@Override
			public List<Channel> getChannelList(int band) {
				return mService.get().getChannelList(band);
			}

		@Override
			public int addChannel(int band, int freq) {
				return mService.get().addChannel(band, freq);
			}

		@Override
			public void removeChannel(int band, int freq) {
				mService.get().removeChannel(band, freq);
			}

		@Override
			public List<Channel> getFavorChannelList() {
				return mService.get().getFavorChannelList();
			}

		@Override
			public void addFavorChannel(int freq) {
				mService.get().addFavorChannel(freq);
			}

		@Override
			public void removeFavorChannel(int freq) {
				mService.get().removeFavorChannel(freq);
			}

		@Override
			public int getCurrentBand() {
				return mService.get().getCurrentBand();
			}

		@Override
			public int getCurrentChannel(int band) {
				return mService.get().getCurrentChannel(band);
			}

		@Override
			public void band() {
				new AsyncOp().exec(mHandler, MSG_BAND);
			}

		@Override
			public void tuneTo(int frequence, boolean add) {
				new TuneToOp(frequence, add).exec(mHandler, MSG_TUNE_TO);
			}

		@Override
			public void tuneDown() {
				new AsyncOp().exec(mHandler, MSG_TUNE_DOWN);
			}

		@Override
			public void tuneUp() {
				new AsyncOp().exec(mHandler, MSG_TUNE_UP);
			}

		@Override
			public void seekDown() {
				new AsyncOp().exec(mHandler, MSG_SEEK_DOWN);
			}

		@Override
			public void seekUp() {
				new AsyncOp().exec(mHandler, MSG_SEEK_UP);
			}

		@Override
			public void scan() {
				new AsyncOp().exec(mHandler, MSG_SCAN);
			}

		@Override
			public void pause() {
				new AsyncOp().exec(mHandler, MSG_PAUSE);
			}

		@Override
			public void play() {
				new AsyncOp().exec(mHandler, MSG_PLAY);
			}

		@Override
			public void playPause() {
				new AsyncOp().exec(mHandler, MSG_PLAYPAUSE);
			}
	};

	private int requestAudioFocus(int streamType, int durationHint) {
		int status = AudioManager.AUDIOFOCUS_REQUEST_FAILED;

		try {
			status = mAudioService.requestAudioFocus(streamType, durationHint, mBinder,
					mAudioFocusDispatcher, new String(this.toString()),
					getPackageName());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return status;
	}

	private int abandonAudioFocus() {
		int status = AudioManager.AUDIOFOCUS_REQUEST_FAILED;

		try {
			status = mAudioService.abandonAudioFocus(mAudioFocusDispatcher,
					new String(this.toString()));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return status;
	}

	private final IAudioFocusDispatcher mAudioFocusDispatcher = new AudioFocusDispatcher();

	private class AudioFocusDispatcher extends IAudioFocusDispatcher.Stub implements Runnable {
		private final Handler mHandler = new Handler();

		private int focusChange;
		private boolean done;

		public void dispatchAudioFocusChange(int focusChange, String id) {
			synchronized (this) {
				done = false;

				this.focusChange = focusChange;

				if (mHandler.post(this)) {
					while (!done) {
						try {
							wait();
						} catch (InterruptedException e){
							e.printStackTrace();
						}
					}
				}
			}
		}

		@Override
			public void run() {
				onAudioFocusChange(focusChange);

				synchronized (this) {
					done = true;
					notifyAll();
				}
			}
	}

	private static void writeFile(String path, String content) {
		if(path == null || content == null) {
			return;
		}
		try {
			final FileOutputStream os = new FileOutputStream(path);
			try {
				os.write(content.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean mRequestPlay;
	private boolean mRequestPauseByAF;
	private AudioThread mAudioThread;

	private void setRadioVolume() {//zhangzhitong20180426
		int vol = AudioSystem.getStreamVolumeIndex(AudioSystem.STREAM_MUSIC, AudioSystem.DEVICE_OUT_SPEAKER);//mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		int v = 16;
		switch(vol) {
			case 0:
				v = 0;
				break;
			case 1:
				v = 5;
				break;
			case 2:
				v = 8;
				break;
			case 3:
				v = 11;
				break;
			case 4:
				v = 14;
				break;
			case 5:
				v = 18;
				break;
			case 6:
				v = 21;
				break;
			case 7:
				v = 23;
				break;
			case 8:
				v = 25;
				break;
			case 9:
				v = 27;
				break;
			case 10:
				v = 29;
				break;
			case 11:
				v = 31;
				break;
			case 12:
				v = 33;
				break;
			case 13:
				v = 34;
				break;
			case 14:
				v = 37;
				break;
			case 15:
				v = 39;
				break;
			case 16:
				v = 41;
				break;
			case 17:
				v = 43;
				break;
			case 18:
				v = 46;
				break;
			case 19:
				v = 47;
				break;
			case 20:
				v = 48;
				break;
			case 21:
				v = 49;
				break;
			case 22:
				v = 50;
				break;
			case 23:
				v = 52;
				break;
			case 24:
				v = 53;
				break;
			case 25:
				v = 54;
				break;
			case 26:
				v = 55;
				break;
			case 27:
				v = 56;
				break;
			case 28:
				v = 57;
				break;
			case 29:
				v = 58;
				break;
			case 30:
				v = 60;
				break;
			case 31:
				v = 61;
				break;
		}
		Log.d(TAG, "vol " + vol + " v " + v);
		writeFile("/sys/devices/platform/imx-i2c.0/i2c-0/0-0063/volume_func", String.valueOf(v));
	}

	private void resumePlayback() {
		writeSubwoofer(false);
		radioSetMute(false);
		boolean mode = SystemProperties.getBoolean("radio.mode", true);
		if(!mode) {
			if (mAudioThread == null) {
				mAudioThread = new AudioThread();
				mAudioThread.start();
			}
		} else {

			audioOpen();
			//setRadioVolume();

			tinymix(32, 0);
			tinymix(33, 0);

			tinymix(48, 0);
			tinymix(49, 0);
			tinymix(37, 0);
			tinymix(39, 0);
			tinymix(43, 0);
			tinymix(46, 0);
			tinymix(34, 5);
			tinymix(35, 5);
			setMute(false);
		}
		//		mhardwareHandle.sendMessageDelayed(mhardwareHandle.obtainMessage(MSG_subwoofer, 0, 0) , 0);
		//		mhardwareHandle.removeMessages(MSG_TINYMIX);
		//		mhardwareHandle.sendMessageDelayed(mhardwareHandle.obtainMessage(MSG_TINYMIX, mCurrentBand, 0) , 1000);
	}

	private void pausePlayback() {
		if (mAudioThread != null) {
			mAudioThread.requestExitAndWait();
			mAudioThread = null;
		}
		boolean mode = SystemProperties.getBoolean("radio.mode", true);
		if(mode){
			setMute(true);
			tinymix(34, 0);
			tinymix(35, 0);
			audioClose();
		}
		radioSetMute(true);
		writeSubwoofer(true);
	}

	//	@SuppressWarnings("deprecation")
	//	@Override
	public void onAudioFocusChange(int focusChange) {
		if (DBG) Log.d(TAG, "onAudioFocusChange: focusChange " + focusChange);

		if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {

			synchronized (mLock) {
				mRequestPauseByAF = false;
				if (mRequestPlay && mRadioStatus == OP_IDLE) {
					resumePlayback();

					setMaxVolume(1.0f);
				}
			}

		} else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {

			pause();

		} else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {

			synchronized (mLock) {
				mRequestPauseByAF = true;
				pausePlayback();
			}
			//			mhardwareHandle.sendMessageDelayed(mhardwareHandle.obtainMessage(MSG_MUTE, 1, 0) , 0);
			//			mhardwareHandle.removeMessages(MSG_TINYMIX);

			//			tinymix(5, 124);			

		} else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {

			//setMaxVolume(0.1f);

		}
	}

	private AudioTrack audioTrack;
	private int mAudioSessionId;

	private float mFadeVolume = 0.0f;
	private float mReqVolume = 1.0f;
	private float mMaxVolume = 1.0f;
	private boolean mMute;
	private final Object mAudioLock = new Object();

	private void setMaxVolume(float volume) {
		synchronized (mAudioLock) {
			mMaxVolume = volume;
			applyVolume();
		}
	}

	private void setVolume(float volume) {
		synchronized (mAudioLock) {
			mReqVolume = volume;
			applyVolume();
		}
	}

	private void setMute(boolean mute) {
		synchronized (mAudioLock) {
			mMute = mute;
			applyVolume();
		}
	}

	private void applyVolume() {
		if (!mVolumeHandler.hasMessages(MSG_FADE_UP))
			mVolumeHandler.sendEmptyMessage(MSG_FADE_UP);
	}

	private static final int MSG_FADE_UP = 1;

	private final Handler mVolumeHandler = new Handler() {
		@Override
			public void handleMessage(Message msg) {
				if (msg.what == MSG_FADE_UP) {
					synchronized (mAudioLock) {
						mFadeVolume += 0.05f;
						if(mMute) {
							mFadeVolume = 0.0f;
						} else if (mFadeVolume < Math.min(mMaxVolume, mReqVolume)) {
							sendEmptyMessageDelayed(MSG_FADE_UP, 100);
						} else {
							mFadeVolume = Math.min(mMaxVolume, mReqVolume);
						}
						audioSetVolume(mFadeVolume, mFadeVolume);
					}
				}
			}
	};

	private final Object mOpLock = new Object();

	public int addChannelInListSingle(int band, int freq, int from) {
		/*
		   if (band == 0) {
		   synchronized (mFMRadioList) {
		   boolean changed = false;
		   int i;
		   for (i = mFMRadioList.size() - 1; i >= 0; i--) {
		   if (mFMRadioList.get(i).from == from) {
		   mFMRadioList.remove(i);
		   changed = true;
		   }
		   }
		   i = 0;
		   for (; i < mFMRadioList.size(); i++) {
		   if (mFMRadioList.get(i).frequence == freq)
		   break;
		   if (mFMRadioList.get(i).frequence > freq)
		   break;
		   }
		   if (i == mFMRadioList.size()) {
		   mFMRadioList.add(new Channel(freq, from));
		   } else {
		   if (mFMRadioList.get(i).frequence == freq) {
		   if (changed)
		   notifyChannelListChanged(band);
		   return -2;
		   } else {
		   mFMRadioList.add(i, new Channel(freq, from));
		   }
		   }
		   }

		   notifyChannelListChanged(band);
		   } else if (band == 1) {
		   synchronized (mAMRadioList) {
		   boolean changed = false;
		   int i;
		   for (i = mAMRadioList.size() - 1; i >= 0; i--) {
		   if (mAMRadioList.get(i).from == from) {
		   mAMRadioList.remove(i);
		   changed = true;
		   }
		   }
		   i = 0;
		   for (; i < mAMRadioList.size(); i++) {
		   if (mAMRadioList.get(i).frequence == freq)
		   break;
		   if (mAMRadioList.get(i).frequence > freq)
		   break;
		   }
		   if (i == mAMRadioList.size()) {
		   mAMRadioList.add(new Channel(freq, from));
		   } else {
		   if (mAMRadioList.get(i).frequence == freq) {
		   if (changed)
		   notifyChannelListChanged(band);
		   return -2;
		   } else {
		   mAMRadioList.add(i, new Channel(freq, from));
		   }
		   }
		   }

		   notifyChannelListChanged(band);
		   }
		 */
		return 0;
	}

	public int addChannelInList(int band, int freq, int from) {
		/*
		   if (band == 0) {
		   synchronized (mFMRadioList) {
		   int i = 0;
		   for (; i < mFMRadioList.size(); i++) {
		   if (mFMRadioList.get(i).frequence == freq)
		   break;
		   if (mFMRadioList.get(i).frequence > freq)
		   break;
		   }
		   if (i == mFMRadioList.size()) {
		   mFMRadioList.add(new Channel(freq, from));
		   } else {
		   if (mFMRadioList.get(i).frequence == freq) {
		   if (mFMRadioList.get(i).from == 0 || mFMRadioList.get(i).from == from)
		   return -2;
		   mFMRadioList.remove(i);
		   }
		   mFMRadioList.add(i, new Channel(freq, from));
		   }
		   }

		   notifyChannelListChanged(band);
		   } else {
		   synchronized (mAMRadioList) {
		   int i = 0;
		   for (; i < mAMRadioList.size(); i++) {
		   if (mAMRadioList.get(i).frequence == freq)
		   break;
		   if (mAMRadioList.get(i).frequence > freq)
		   break;
		   }
		   if (i == mAMRadioList.size()) {
		   mAMRadioList.add(new Channel(freq, from));
		   } else {
		   if (mAMRadioList.get(i).frequence == freq) {
		   if (mAMRadioList.get(i).from == 0 || mAMRadioList.get(i).from == from)
		   return -2;
		   mAMRadioList.remove(i);
		   }
		   mAMRadioList.add(i, new Channel(freq, from));
		   }
		   }

		   notifyChannelListChanged(band);
		   }
		 */
		return 0;
	}

	private boolean tuneToAndSave(int band, int freq) {
		if (DBG) Log.d(TAG, "tuneToAndSave band " + band + ", freq " + freq);

		radioTurnToFreq(band == 0, freq);

		mCurrentBand = band;
		mCurrentChannel[band] = freq;

		saveCurrentChannel();

		notifyChannelChanged();

		return true;
	}
		
	private boolean tuneTo(int band, int freq) {
		if (DBG) Log.d(TAG, "tuneTo band " + band + ", freq " + freq);

		if (mCurrentBand == band && mCurrentChannel[band] == freq) {
			return false;
		}

		if (DBG) Log.d(TAG, "turn to band " + band + ", freq " + freq);
		radioTurnToFreq(band == 0, freq);

		mCurrentBand = band;
		mCurrentChannel[band] = freq;

		saveCurrentChannel();

		notifyChannelChanged();

		if (mCurrentBand != -1)
			sendInfoUpdate(mCurrentBand, mCurrentChannel[mCurrentBand], mRequestPlay);

		return true;
	}

	private void sendInfoUpdate(int band, int freq, boolean status) {
		if (freq != -1) {
			Intent intent = new Intent("com.hwatong.player.info");
			intent.putExtra("type", "FM");
			intent.putExtra("status", status);
			if (band == 0) {
				intent.putExtra("title1", changeToString(freq));
				intent.putExtra("title", "" + changeToString(freq) + " MHz");
				intent.putExtra("title2", "FM");
				intent.putExtra("title3", "MHz");
			} else if (band == 1) {
				intent.putExtra("title", "" + freq + " KHz");
				intent.putExtra("title1", "" + freq);
				intent.putExtra("title2", "AM");
				intent.putExtra("title3", "KHz");
			}
			sendBroadcast(intent);
		}
	}

	private boolean readRadioList(String what) {
		if ("".equals(what) || null == what)
			return false;

		boolean ret = false;

		if ("collection".equals(what)) {
			synchronized (mCollectionList) {
				mCollectionList.clear();

				int i = 0;
				SharedPreferences mSharedPreferencesCollection = getSharedPreferences(
						SharedPreferences_Radio_Collection, MODE_PRIVATE);
				while (mSharedPreferencesCollection.contains(Collection_Data + i)) {
					ret = true;
					if (mSharedPreferencesCollection.getInt(Collection_Data + i, 0) == 0)
						break;

					mCollectionList.add(new Channel(
								mSharedPreferencesCollection.getInt(Collection_Data + i, 0),
								0));
					i++;
				}
			}

			notifyFavorChannelListChanged();
		} else if ("AM".equals(what)) {
			synchronized (mAMRadioList) {
				mAMRadioList.clear();

				SharedPreferences mSharedPreferencesAM = getSharedPreferences(
						SharedPreferences_Radio_AM, MODE_PRIVATE);
				int i = 0;
				while (mSharedPreferencesAM.contains(AM_Data + i)) {
					ret = true;
					if (mSharedPreferencesAM.getInt(AM_Data + i, 0) == 0)
						break;

					int freq = mSharedPreferencesAM.getInt(AM_Data + i, 0);
					if (MIN_FREQUENCE_AM <= freq && freq < MAX_FREQUENCE_AM) {
						mAMRadioList.add(new Channel(freq, 0));
					}
					i++;
				}
				mSharedPreferencesAM = null;
			}

			notifyChannelListChanged(1);
		} else if ("FM".equals(what)) {
			synchronized (mFMRadioList) {
				mFMRadioList.clear();

				int i = 0;
				SharedPreferences mSharedPreferencesFM = getSharedPreferences(
						SharedPreferences_Radio_FM, MODE_PRIVATE);
				while (mSharedPreferencesFM.contains(FM_Data + i)) {
					ret = true;
					if (mSharedPreferencesFM.getInt(FM_Data + i, 0) == 0)
						break;

					int freq = mSharedPreferencesFM.getInt(FM_Data + i, 0);
					if (MIN_FREQUENCE_FM < freq && freq <= MAX_FREQUENCE_FM) {
						mFMRadioList.add(new Channel(freq, 0));
					}
					i++;
				}
				mSharedPreferencesFM = null;

			}

			notifyChannelListChanged(0);
		}

		return ret;
	}

	private void applyRadioList(String what) {
		if ("collection".equals(what)) {

		} else if ("AM".equals(what)) {
			synchronized (mAMRadioList) {
				if (mCurrentBand == 1 && mAMRadioList.size() > 0) {
//					int i = 0;
//					for (; i < mAMRadioList.size(); i++) {
//						if (mAMRadioList.get(i).frequence == mCurrentChannel[1])
//							break;
//					}
//					if (i == mAMRadioList.size()) {
//						synchronized (mOpLock) {
//							tuneTo(mCurrentBand, mAMRadioList.get(0).frequence);
//						}
//					}
					
					synchronized (mOpLock) {
						tuneTo(mCurrentBand, mAMRadioList.get(0).frequence);
					}
					
				}
			}
		} else if ("FM".equals(what)) {
			synchronized (mFMRadioList) {
				if (mCurrentBand == 0 && mFMRadioList.size() > 0) {
//					int i = 0;
//					for (; i < mFMRadioList.size(); i++) {
//						if (mFMRadioList.get(i).frequence == mCurrentChannel[0])
//							break;
//					}
//					if (i == mFMRadioList.size()) {
//						synchronized (mOpLock) {
//							tuneTo(mCurrentBand, mFMRadioList.get(0).frequence);
//						}
//					}
					synchronized (mOpLock) {
						tuneTo(mCurrentBand, mFMRadioList.get(0).frequence);
					}
				}
			}
		}
	}

	/** 选择前一个频道 */
	private int selectPreChannel() {
		int freq = -1;

		if (mCurrentBand == 0) {
			synchronized (mFMRadioList) {
				if (mFMRadioList.size() > 0) {
					for (int i = mFMRadioList.size() - 1; i >= 0; i--) {
						if (mFMRadioList.get(i).frequence < mCurrentChannel[mCurrentBand]) {
							freq = mFMRadioList.get(i).frequence;
							break;
						}
					}

					if (freq == -1) {
						freq = mFMRadioList.get(mFMRadioList.size() - 1).frequence;
					}
				}
			}
		} else if (mCurrentBand == 1) {
			synchronized (mAMRadioList) {
				if (mAMRadioList.size() > 0) {
					for (int i = mAMRadioList.size() - 1; i >= 0; i--) {
						if (mAMRadioList.get(i).frequence < mCurrentChannel[mCurrentBand]) {
							freq = mAMRadioList.get(i).frequence;
							break;
						}
					}

					if (freq == -1) {
						freq = mAMRadioList.get(mAMRadioList.size() - 1).frequence;
					}
				}
			}
		}

		return freq;
	}

	/** 选择下一个频道 */
	private int selectNextChannel() {
		int freq = -1;

		if (mCurrentBand == 0) {
			synchronized (mFMRadioList) {
				if (mFMRadioList.size() > 0) {
					for (int i = 0; i < mFMRadioList.size(); i++) {
						if (mFMRadioList.get(i).frequence > mCurrentChannel[mCurrentBand]) {
							freq = mFMRadioList.get(i).frequence;
							break;
						}
					}
					if (freq == -1) {
						freq = mFMRadioList.get(0).frequence;
					}
				}
			}
		} else if (mCurrentBand == 1) {
			synchronized (mAMRadioList) {
				if (mAMRadioList.size() > 0) {
					for (int i = 0; i < mAMRadioList.size(); i++) {
						if (mAMRadioList.get(i).frequence > mCurrentChannel[mCurrentBand]) {
							freq = mAMRadioList.get(i).frequence;
							break;
						}
					}
				}
				if (freq == -1) {
					freq = mAMRadioList.get(0).frequence;
				}
			}
		}

		return freq;
	}

	private static final int MSG_OP_COMPLETED = 101;

	private final Handler mHandler = new Handler() {
		@Override
			public void handleMessage(Message msg) {

				switch (msg.what) {

					case MSG_OP_COMPLETED: {
								       if (DBG) Log.d(TAG, "MSG_OP_COMPLETED");

								       if (mRadioThread != null) {
									       int status = mRadioThread.getStatus();

									       mRadioThread.requestExitAndWait();
									       mRadioThread = null;
								       }

								       break;
							       }

				}
			}
	};

	private static final int MSG_subwoofer = 201;
	private static final int MSG_MUTE = 202;
	//	private static final int MSG_TINYMIX = 203;

	@SuppressLint("HandlerLeak") 
		private final Handler mhardwareHandle = new Handler(){

			@Override
				public void handleMessage(Message msg) {
					switch (msg.what) {
						case MSG_MUTE:
							if (msg.arg1 == 1) {
								writeAmpMute(true);
								mhardwareHandle.sendMessageDelayed(mhardwareHandle.obtainMessage(MSG_subwoofer, 1, 0) , 20);
							} else {
								writeAmpMute(false);
								//					writeMainVol("0x0f");
							}
							break;
						case MSG_subwoofer:
							if (msg.arg1 == 1) {
								writeSubwoofer(true);
								mhardwareHandle.sendMessageDelayed(mhardwareHandle.obtainMessage(MSG_MUTE, 0, 0) , 20);
							} else {
								writeSubwoofer(false);
								//					writeMainVol("0x08");
							}
							break;
							//			case MSG_TINYMIX:
							//				tinymix(5, 127);
							//				break;
						default:
							break;
					}
				}

		};


	//	还未得到硬件确认的代码，用于控制硬件上电断电以维持搜台质量与播放清晰度
	private static void writeAmpMute(boolean mute) {
		try {
			final FileOutputStream os = new FileOutputStream(
					"/sys/devices/platform/imx-i2c.0/i2c-0/0-0044/main_mute");
			try {
				os.write((mute ? "1" : "0").getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void writeSubwoofer(boolean enable) {
		try {
			final FileOutputStream os = new FileOutputStream(
					"/sys/devices/platform/imx-i2c.1/i2c-1/1-0019/subwoofer_enable");
			try {
				os.write((enable ? "1" : "0").getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void writeMainVol(String value) {
		try {
			final FileOutputStream os = new FileOutputStream(
					"/sys/devices/platform/imx-i2c.0/i2c-0/0-0044/main_vol");
			try {
				os.write(value.getBytes());
				Log.d(TAG, "write main_vol: "+value);
			} catch (IOException e) {
				e.printStackTrace();
			}
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean initCheck(int band) {
		boolean status = false;
		if(band == 0) {
			status= readRadioList("FM");
		} else {
			status = readRadioList("AM");
		}

		if (!status) {
			return false;
		}

		return true;
	}

	/**
	 * 获得当前FM与AM频率
	 */
	private void doInit2() {
		int band;
		final int[] freqInBand = new int[2];

		SharedPreferences sp = getSharedPreferences(SharedPreferences_Current_Channel, MODE_PRIVATE);
		band = sp.getInt(Num_Current_Band, -1);
        if(band == 1){
            readRadioList("AM");
        }
		freqInBand[0] = sp.getInt(Num_Current_Channel_Of_FM, -1);
		freqInBand[1] = sp.getInt(Num_Current_Channel_Of_AM, -1);
		sp = null;

		if (DBG) Log.d(TAG, "Band " + band + ", FM " + freqInBand[0] + ", AM " + freqInBand[1]);

		synchronized (mOpLock) {
			if (band != 0 && freqInBand[0] != -1)
				mCurrentChannel[0] = freqInBand[0];
			if (band != 1 && freqInBand[1] != -1)
				mCurrentChannel[1] = freqInBand[1];

			if (band != -1 && freqInBand[band] != -1) {
				addChannelInList(band, freqInBand[band], 1);
				tuneTo(band, freqInBand[band]);
			}
		}

		if (DBG) Log.d(TAG, "Band " + mCurrentBand + ", CurrentChannel " + mCurrentChannel[0] + ", " + mCurrentChannel[1]);

		synchronized (mOpLock) {
			band = 0;
			freqInBand[0] = DEF_FREQUENCE_FM;
			freqInBand[1] = DEF_FREQUENCE_AM;

			synchronized (mFMRadioList) {
				if (mFMRadioList.size() > 0)
					freqInBand[0] = mFMRadioList.get(0).frequence;
			}
			synchronized (mAMRadioList) {
				if (mAMRadioList.size() > 0)
					freqInBand[1] = mAMRadioList.get(0).frequence;
			}

			if (band != 0 && mCurrentChannel[0] == -1)
				mCurrentChannel[0] = freqInBand[0];
			if (band != 1 && mCurrentChannel[1] == -1)
				mCurrentChannel[1] = freqInBand[1];

			if (mCurrentBand == -1 || mCurrentChannel[mCurrentBand] == -1) {
				addChannelInList(band, freqInBand[band], 1);
				tuneTo(band, freqInBand[band]);
			}
		}

		if (DBG) Log.d(TAG, "Band " + mCurrentBand + ", CurrentChannel " + mCurrentChannel[0] + ", " + mCurrentChannel[1]);
	}

	private final Object mLock = new Object();
	private int mRadioStatus = OP_IDLE;

	private RadioThread mRadioThread;

	private static final int OP_INIT = -1;
	private static final int OP_IDLE = 0;
	private static final int OP_SCAN = 1;
	private static final int OP_SEEK_UP = 2;
	private static final int OP_SEEK_DOWN = 3;

	private class RadioThread extends Thread {
		private final int op;

		private boolean exit = false;
		private boolean done = false;

		public RadioThread(int op) {
			this.op = op;
		}

		public void requestExitAndWait() {
			synchronized (this) {
				exit = true;
				notifyAll();

				while (!done) {
					try {
						wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}



		@Override
			public void run() {
				setPriority(Thread.MAX_PRIORITY);

				if (DBG) Log.d(TAG, "RadioThread Start, op " + op);

				//			radioSetVolume(0);

				synchronized (mLock) {
					mRadioStatus = op;
					pausePlayback();
				}
				notifyStatusChanged();

				switch (op) {

					case OP_INIT: {
							      doInit();
							      doInit2();
							      break;
						      }

					case OP_SCAN: {
							      if (mCurrentBand != -1 && scanInBand(mCurrentBand)) {
								      readRadioList(mCurrentBand == 0 ? "FM" : "AM");
								      applyRadioList(mCurrentBand == 0 ? "FM" : "AM");
							      }

							      break;
						      }
					case OP_SEEK_UP: {
								 synchronized (mOpLock) {
									 if (mCurrentBand != -1) {
										 int freq = doSeekUp();
										 if (freq != -1) {
											 addChannelInList(mCurrentBand, freq, 1);
											 tuneTo(mCurrentBand, freq);
										 }
									 }
								 }

								 break;
							 }
					case OP_SEEK_DOWN: {
								   synchronized (mOpLock) {
									   if (mCurrentBand != -1) {
										   int freq = doSeekDown();
										   if (freq != -1) {
											   addChannelInList(mCurrentBand, freq, 1);
											   tuneTo(mCurrentBand, freq);
										   }
									   }
								   }

								   break;
							   }

				}

				//			radioSetVolume(0);

				synchronized (mLock) {
					mRadioStatus = OP_IDLE;
					if (mRequestPlay && !mRequestPauseByAF)
						resumePlayback();
				}
				notifyStatusChanged();

				if (!exit) {
					mHandler.removeMessages(MSG_OP_COMPLETED);
					mHandler.sendEmptyMessage(MSG_OP_COMPLETED);
				}

				if (DBG) Log.d(TAG, "RadioThread End");

				synchronized (this) {
					done = true;
					notifyAll();
				}
			}

		public synchronized int getStatus() {
			return op;
		}

		private void doInit() {
			if (scanInBand(0)) {
				readRadioList("FM");
			}
			/*
			   if (scanInBand(1)) {
			   readRadioList("AM");
			   }
			 */
		}

		private boolean scanInBand(int band) {
			final short[] radioList = new short[300];
			int count = 0;

			int resumeFreq = mCurrentChannel[band];
			
			synchronized (mOpLock) {
				radioOpen();
				radioStartSeek(band == 0, band == 0 ? MIN_FREQUENCE_FM : MIN_FREQUENCE_AM);

				short freq = (short) (band == 0 ? MIN_FREQUENCE_FM : MIN_FREQUENCE_AM);
				for (; freq < (band == 0 ? MAX_FREQUENCE_FM : MAX_FREQUENCE_AM); freq += (band == 0 ? 10 : 9)) {

					
					//add，保证停止时候停在当前频率
					mCurrentChannel[band] = freq;
					Log.d("RadioService", "mCurrentChannel " + mCurrentChannel[band]);
					
					notifyDisplayChanged(band, freq);
					sendInfoUpdate(band, freq, mRequestPlay);

					if (radioScanFreq(band == 0, freq) == 0) {
						if (DBG) Log.d(TAG, "scanInBand Is OK | " + freq);
						radioList[count] = freq;
						count++;
						if (count >= radioList.length) {
							break;
						}
					}

					synchronized (this) {
						if (/*op != OP_INIT && */ exit) {
							if (DBG) Log.d(TAG, "scanInBand Is break " + ", current" + mCurrentChannel[0] + "/" + mCurrentChannel[1]);
							break;
						}
					}

				}
				
				//退出停在当前频率，不是退出则回到之前频率
				if(exit) {
					mCurrentChannel[band] = freq;
					mCurrentBand = band;
				} else {
					mCurrentChannel[band] = resumeFreq;
				}
				
				radioEndSeek(band == 0, mCurrentChannel[band]);
				radioClose();

				if (mCurrentBand != -1 && mCurrentChannel[mCurrentBand] != -1) {
					tuneToAndSave(mCurrentBand, mCurrentChannel[mCurrentBand]);
					//radioTurnToFreq(mCurrentBand == 0, mCurrentChannel[mCurrentBand]);
					notifyDisplayChanged(mCurrentBand, mCurrentChannel[mCurrentBand]);
					sendInfoUpdate(mCurrentBand, mCurrentChannel[mCurrentBand], mRequestPlay);
				}

//				//该代码表示如果搜索到一半退出，则恢复原来的列表，不更新列表
//				synchronized (this) {
//					if (/*op != OP_INIT && */exit) {
//						
//						if(band == 0) {
//							int i = 0;
//							SharedPreferences mSharedPreferencesFM = getSharedPreferences(
//									SharedPreferences_Radio_FM, MODE_PRIVATE);
//							if(mSharedPreferencesFM.contains(FM_Data + 0)) {
//								
//							} else {
//								Editor mEditorFM = mSharedPreferencesFM.edit();
//								mEditorFM.putInt(FM_Data + 0, 0);
//								mEditorFM.commit();
//								mEditorFM = null;
//							}
//							
//							mSharedPreferencesFM = null;
//							
//						} else {
//							SharedPreferences mSharedPreferencesAM = getSharedPreferences(
//									SharedPreferences_Radio_AM, MODE_PRIVATE);
//							if(mSharedPreferencesAM.contains(AM_Data + 0)) {
//								
//							} else {
//								Editor mEditorAM = mSharedPreferencesAM.edit();
//								mEditorAM.putInt(AM_Data + 0, 0);
//								mEditorAM.commit();
//								mEditorAM = null;
//							}
//							mSharedPreferencesAM = null;
//						}
//						
//						return false;
//					}
//				}
			}

			try {
				if (band == 0) {
					Editor mEditorFM = getSharedPreferences(
							SharedPreferences_Radio_FM, MODE_PRIVATE).edit();
					mEditorFM.clear();
					mEditorFM.commit();

					int i = 0;
					for (; i < count; i++) {
						mEditorFM.putInt(FM_Data + i, radioList[i]);
					}
					mEditorFM.putInt(FM_Data + i, 0);
					mEditorFM.commit();
					mEditorFM = null;

				} else {
					Editor mEditorAM = getSharedPreferences(
							SharedPreferences_Radio_AM, MODE_PRIVATE).edit();
					mEditorAM.clear();
					mEditorAM.commit();

					int i = 0;
					for (; i < count; i++) {
						mEditorAM.putInt(AM_Data + i, radioList[i]);
					}
					mEditorAM.putInt(AM_Data + i, 0);
					mEditorAM.commit();
					mEditorAM = null;
				}
			} catch (Exception e) {
				// TODO: handle exception
			}

			return true;
		}

		private int doSeekUp() {
			if (DBG) Log.d(TAG, "doSeekUp");

			int band = mCurrentBand;
			int freq0 = mCurrentChannel[band];
			int freq = freq0;

			if (band == 0) {
				freq += 10;
				if (freq > MAX_FREQUENCE_FM)
					freq = MIN_FREQUENCE_FM;
			} else {
				freq += 9;
				if (freq > MAX_FREQUENCE_AM)
					freq = MIN_FREQUENCE_AM;
			}

			int ret = -1;

			radioOpen();
			radioStartSeek(band == 0, freq);

			for (;;) {

				if (freq == freq0)
					break;

				notifyDisplayChanged(band, freq);
				sendInfoUpdate(band, freq, mRequestPlay);

				if (radioScanFreq(band == 0, freq) == 0) {
					ret = freq;
					break;
				}

				synchronized (this) {
					if (exit) {
						if (DBG) Log.d(TAG, "SEEK_UP is break " + ", current" + mCurrentChannel[0] + "/" + mCurrentChannel[1]);
						ret = freq;
						break;
					}
				}

				if (band == 0) {
					freq += 10;
					if (freq > MAX_FREQUENCE_FM)
						freq = MIN_FREQUENCE_FM;
				} else {
					freq += 9;
					if (freq > MAX_FREQUENCE_AM)
						freq = MIN_FREQUENCE_AM;
				}
			}

			radioEndSeek(band == 0, freq);
			radioClose();

			if (ret == -1) {
				if (mCurrentBand != -1 && mCurrentChannel[mCurrentBand] != -1) {
					tuneToAndSave(mCurrentBand, mCurrentChannel[mCurrentBand]);
					//radioTurnToFreq(mCurrentBand == 0, mCurrentChannel[mCurrentBand]);
					notifyDisplayChanged(mCurrentBand, mCurrentChannel[mCurrentBand]);
					sendInfoUpdate(mCurrentBand, mCurrentChannel[mCurrentBand], mRequestPlay);
				}
			}

			return ret;
		}

		private int doSeekDown() {
			if (DBG) Log.d(TAG, "doSeekDown");

			int band = mCurrentBand;
			int freq0 = mCurrentChannel[band];
			int freq = freq0;

			if (band == 0) {
				freq -= 10;
				if (freq < MIN_FREQUENCE_FM)
					freq = MAX_FREQUENCE_FM;
			} else {
				freq -= 9;
				if (freq < MIN_FREQUENCE_AM)
					freq = MAX_FREQUENCE_AM;
			}

			int ret = -1;

			radioOpen();
			radioStartSeek(band == 0, freq);

			for (;;) {

				if (freq == freq0)
					break;

				notifyDisplayChanged(band, freq);
				sendInfoUpdate(band, freq, mRequestPlay);

				if (radioScanFreq(band == 0, freq) == 0) {
					ret = freq;
					break;
				}

				synchronized (this) {
					if (exit) {
						if (DBG) Log.d(TAG, "SEEK_DOWN is break " + ", current" + mCurrentChannel[0] + "/" + mCurrentChannel[1]);
						ret = freq;
						break;
					}
				}

				if (band == 0) {
					freq -= 10;
					if (freq < MIN_FREQUENCE_FM)
						freq = MAX_FREQUENCE_FM;
				} else {
					freq -= 9;
					if (freq < MIN_FREQUENCE_AM)
						freq = MAX_FREQUENCE_AM;
				}

			}

			radioEndSeek(true, freq);
			radioClose();

			if (ret == -1) {
				if (mCurrentBand != -1 && mCurrentChannel[mCurrentBand] != -1) {
					tuneToAndSave(mCurrentBand, mCurrentChannel[mCurrentBand]);
					//radioTurnToFreq(mCurrentBand == 0, mCurrentChannel[mCurrentBand]);
					notifyDisplayChanged(mCurrentBand, mCurrentChannel[mCurrentBand]);
					sendInfoUpdate(mCurrentBand, mCurrentChannel[mCurrentBand], mRequestPlay);
				}
			}

			return ret;
		}

	}

	private class AudioThread extends Thread {
		private boolean exit;
		private boolean done;

		public synchronized void requestExitAndWait() {
			exit = true;
			notifyAll();

			while (!done) {
				try {
					wait();
				} catch (InterruptedException e){
					e.printStackTrace();
				}
			}
		}

		@Override
			public synchronized void start() {
				done = false;
				exit = false;
				super.start();
			}

		@Override
			public void run() {
				setPriority(Thread.MAX_PRIORITY);

				final int sampleRateInHz = 44100;
				final int encodingBitrate = AudioFormat.ENCODING_PCM_16BIT;
				final int recBufSize = AudioRecord.getMinBufferSize(sampleRateInHz,
						AudioFormat.CHANNEL_IN_STEREO, encodingBitrate);
				final int playBufSize = AudioTrack.getMinBufferSize(sampleRateInHz,
						AudioFormat.CHANNEL_OUT_STEREO, encodingBitrate);

				if (DBG) Log.d(TAG, "recBufSize " + recBufSize + ", playBufSize " + playBufSize);

				synchronized (mAudioLock) {
					audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
							sampleRateInHz, AudioFormat.CHANNEL_OUT_STEREO,
							encodingBitrate, playBufSize * 2, AudioTrack.MODE_STREAM);
					audioTrack.setPlaybackRate(sampleRateInHz);
					mAudioSessionId = audioTrack.getAudioSessionId();

					try {
						audioTrack.play();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.CAMCORDER,
						sampleRateInHz, AudioFormat.CHANNEL_IN_STEREO,
						encodingBitrate, recBufSize * 2);

				while (!exit) {
					try {
						audioRecord.startRecording();
					} catch (Exception e) {
						e.printStackTrace();
					}

					if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING)
						break;

					try {
						Thread.sleep(100);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				setMute(false);

				final byte[] bsBuffer = new byte[4096];

				while (!exit) {
					int n = audioRecord.read(bsBuffer, 0, bsBuffer.length);
					boolean enable = SystemProperties.getBoolean("radio.enable", true);
					Log.d(TAG, "enable " + enable + " n " + n);
					if(n > 2)
						Log.d(TAG, "read " + bsBuffer[0] + " " + bsBuffer[1] + " " + bsBuffer[2] + " " + bsBuffer[3]);

					if (n > 0&&enable) {
						synchronized (mAudioLock) {
							if (audioTrack != null) {
								audioTrack.write(bsBuffer, 0, n);
							}
						}
					}
				}

				synchronized (mAudioLock) {
					try {
						audioTrack.stop();
						audioRecord.stop();
					} catch (Exception e) {
						e.printStackTrace();
					}

					audioTrack.release();
					audioTrack = null;

					audioRecord.release();
				}

				synchronized (this) {
					done = true;
					notifyAll();
				}
			}
	}

	private void readCurrentChannel() {
		SharedPreferences sp = getSharedPreferences(
				SharedPreferences_Current_Channel, MODE_PRIVATE);
		mCurrentBand = sp.getInt(
				Num_Current_Band, 0);
		mCurrentChannel[0] = sp.getInt(
				Num_Current_Channel_Of_FM, DEF_FREQUENCE_FM);
		mCurrentChannel[1] = sp.getInt(
				Num_Current_Channel_Of_AM, DEF_FREQUENCE_AM);
		sp = null;
	}

	private void saveCurrentChannel() {
		final Editor editor = getSharedPreferences(
				SharedPreferences_Current_Channel, MODE_PRIVATE).edit();

		editor.putInt(Num_Current_Band, mCurrentBand);
		editor.putInt(Num_Current_Channel_Of_FM, mCurrentChannel[0]);
		editor.putInt(Num_Current_Channel_Of_AM, mCurrentChannel[1]);

		editor.commit();

		sync();
	}

	public String changeToString(int frequence) {
		double f = frequence / 100.0;
		DecimalFormat df = new DecimalFormat("#.0");
		return df.format(f);
	}


	private BroadcastReceiver mVoiceCmdListener = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				final String action = intent.getAction();
				Log.d(TAG, "action " + action);
				if (action.equals("com.hwatong.voice.CLOSE_FM")) {
					pause();
				} else if (action.equals("com.hwatong.voice.FM_CMD")) {
					String freq = intent.getStringExtra("frequency");
					if (freq == null || freq.isEmpty()) {
						return;
					}
					int f = (int) (Double.parseDouble(freq) * (double) 100);
					if (f <= MAX_FREQUENCE_FM && f >= MIN_FREQUENCE_FM) {
						//去掉sync是因为不去掉的话需要等到搜索状态停止才能获取到锁
						//synchronized (mOpLock) {
							if (mCurrentBand != -1) {
								addChannelInListSingle(0, f, 2);
								
								//采用这个tuneto可以停止搜索状态并跳转到指定频率，解决搜台过程语音打开指定频率无用问题
								tuneTo(f, false);
								//停止预览电台用
								sendButtonBroadcast(133);
								
								//tuneTo(0, f);
							}
						//}
					}
				} else if (action.equals("com.hwatong.voice.AM_CMD")) {
					String freq = intent.getStringExtra("frequency");
					if (freq == null || freq.isEmpty()) {
						return;
					}
					int f = ((int) (Double.parseDouble(freq)));
					if (f <= MAX_FREQUENCE_AM && f >= MIN_FREQUENCE_AM) {
						//synchronized (mOpLock) {
							if (mCurrentBand != -1) {
								addChannelInListSingle(1, f, 2);
								tuneTo(f, false);
								
								//停止预览电台用
								sendButtonBroadcast(133);
								
								//tuneTo(1, f);
							}
						//}
					}
				} else if (action.equals("com.hwatong.voice.BACK_Radio")) {
					readCurrentChannel();
					//synchronized (mOpLock) {
						if (mCurrentBand != -1)
							tuneTo(mCurrentChannel[mCurrentBand], false);
						
							//停止预览电台用
							sendButtonBroadcast(133);
							//tuneTo(mCurrentBand, mCurrentChannel[mCurrentBand]);
					//}
				} else if (action.equals("com.hwatong.voice.search_Radio")) {

				}/* else if (action.equals(AudioManager.MASTER_MUTE_CHANGED_ACTION)) {
					if(mRequestPlay) {
						setRadioVolume();
					}
				}*/
			}
	};

	// native API
	public native int radioOpen();
	public native int radioClose();

	public native int radioTurnToFreq(boolean fm, int frequency);
	public native int radioScanFreq(boolean fm, int frequency);
	public native int radioStartSeek(boolean fm, int frequency);
	public native int radioEndSeek(boolean fm, int frequency);
	//public native int radioGetQuality(boolean fm, short[] arr);
	//public native int radioSetVolume(int volume);
	public native int radioSetMute(boolean muted);

	public native void sync();
	public native void tinymix(int id, int value);

	public native boolean audioOpen();
	public native void audioClose();
	public native void audioSetVolume(float leftVol, float rightVol);

}
