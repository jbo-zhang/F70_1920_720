package com.hwatong.btmusic;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.hwatong.bt.BtDef;
import com.nforetek.bt.aidl.INfCallbackA2dp;
import com.nforetek.bt.aidl.INfCallbackAvrcp;
import com.nforetek.bt.aidl.INfCommandA2dp;
import com.nforetek.bt.aidl.INfCommandAvrcp;
import com.nforetek.bt.res.NfDef;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.os.UserHandle;
import android.os.SystemClock;

public class Service extends android.app.Service implements
	AudioManager.OnAudioFocusChangeListener {

	private static final String TAG = "BtMusicService";
	private static final boolean DBG = true;

	private NowPlaying mNowPlaying = new NowPlaying();;
	private final Object mNowPlayingLock = new Object();

	private ComponentName mComponentName;
	private AudioManager mAudioManager;

	private boolean mRequestPlay;
	private boolean mRequestPauseByAF;
    private boolean mBeforeVoice;

	private INfCommandA2dp mCommandA2dp;
	private INfCommandAvrcp mCommandAvrcp;
	
	private boolean mA2dpConnected;
	private boolean mAvrcpConnected;

    private long mCurrentTrack = -1;
    private boolean mStreaming;
    private boolean mPlayStatus;

    private final IBinder mBinder = new ServiceImpl(this);

    private ServiceConnection mConnection = new ServiceConnection() {
	    public void onServiceConnected(ComponentName className, IBinder service) {
	        Log.e(TAG, "ready onServiceConnected");
	
	        Log.v(TAG,"Piggy Check className : " + className);
	
	        Log.e(TAG,"IBinder service: " + service.hashCode());
	        try {
	            Log.v(TAG,"Piggy Check service : " + service.getInterfaceDescriptor());
	        } catch (RemoteException e1) {
	            e1.printStackTrace();
	        }
	
	        if (className.equals(new ComponentName(NfDef.PACKAGE_NAME, NfDef.CLASS_SERVICE_A2DP))) {
                Log.e(TAG,"ComponentName(" + NfDef.CLASS_SERVICE_A2DP +")");
                mCommandA2dp = INfCommandA2dp.Stub.asInterface(service);
                if (mCommandA2dp == null) {
                    Log.e(TAG,"mCommandA2dp is null !!");
                    return;
                }

                try {
                    mCommandA2dp.registerA2dpCallback(mCallbackA2dp);
                    int state = mCommandA2dp.getA2dpConnectionState();
                    String addr = null;
                    if(mCommandA2dp.isA2dpConnected()) {
                    	addr = mCommandA2dp.getA2dpConnectedAddress();
                    } else {
                    }
                    //mBtMusicHandler.removeMessages(MSG_A2DP_CONNECT_CHANGED);
                    mBtMusicHandler.sendMessageDelayed(mBtMusicHandler.obtainMessage(MSG_A2DP_CONNECT_CHANGED, state, state, addr), 100);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else if (className.equals(new ComponentName(NfDef.PACKAGE_NAME, NfDef.CLASS_SERVICE_AVRCP))) {
                Log.e(TAG,"ComponentName(" + NfDef.CLASS_SERVICE_AVRCP + ")");
                mCommandAvrcp = INfCommandAvrcp.Stub.asInterface(service);
                if (mCommandAvrcp == null) {
                    Log.e(TAG,"mCommandAvrcp is null !!");
                    return;
                }

                try {
                    mCommandAvrcp.registerAvrcpCallback(mCallbackAvrcp);
                    int state = 0;
                    String addr = null;
                    if(mCommandAvrcp.isAvrcpConnected()) {
                    	state = 1;
                    	addr = mCommandAvrcp.getAvrcpConnectedAddress();
                    } else {
                    	state = 0;
                    }
                    mBtMusicHandler.removeMessages(MSG_AVRCP_CONNECT_CHANGED);
                    mBtMusicHandler.sendMessageDelayed(mBtMusicHandler.obtainMessage(MSG_AVRCP_CONNECT_CHANGED, state, 0, addr), 1000);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
	        
	        Log.e(TAG, "end onServiceConnected");
	    }
	    public void onServiceDisconnected(ComponentName className) {
	        Log.e(TAG, "ready onServiceDisconnected: " + className);          
	        if (className.equals(new ComponentName(NfDef.PACKAGE_NAME, NfDef.CLASS_SERVICE_A2DP))) {
	        	mCommandA2dp = null;
	        } else if (className.equals(new ComponentName(NfDef.PACKAGE_NAME, NfDef.CLASS_SERVICE_AVRCP))) {
	        	mCommandAvrcp = null;
	        }
	
	        Log.e(TAG, "end onServiceDisconnected");
	    }
    };
    
    /*
     * A2dp Callback
     * 
     */

    private INfCallbackA2dp mCallbackA2dp = new INfCallbackA2dp.Stub() {

        @Override
        public void onA2dpServiceReady() throws RemoteException {
            Log.v(TAG,"onA2dpServiceReady()");
        }
        
        @Override
        public void onA2dpStateChanged(String address, int prevState, int newState)
                throws RemoteException {
            Log.v(TAG,"onA2dpStateChanged() " + address + " state: " + prevState + "->" + newState);
            //mBtMusicHandler.removeMessages(MSG_A2DP_CONNECT_CHANGED);
            boolean status = mStreaming;
            status = (newState == NfDef.STATE_STREAMING);
            Log.d(TAG, "onA2dpStateChanged mStreaming: " + mStreaming);
            if(status != mStreaming) {
                mStreaming = status;
                processStatusId();
            }
            mBtMusicHandler.sendMessageDelayed(mBtMusicHandler.obtainMessage(MSG_A2DP_CONNECT_CHANGED, newState, prevState, address), 100);
        }
    };

    void processStatusId() {
        synchronized (mNowPlayingLock) {
    	    mNowPlaying.setStatus(mStreaming?mPlayStatus:false);
    	    Log.d(TAG, "ljw processStatusId: " + mPlayStatus);
    		onNowPlayingUpdate();
    	}
    }
    
    private INfCallbackAvrcp mCallbackAvrcp = new INfCallbackAvrcp.Stub() {

        @Override
        public void onAvrcpServiceReady() throws RemoteException {
            Log.v(TAG,"onAvrcpServiceReady()");

        }
        
        @Override
        public void onAvrcpStateChanged(String address, int prevState, int newState)
                throws RemoteException {
            Log.v(TAG,"onAvrcpStateChanged() " + address + " state: " + prevState + "->" + newState);
            /*
            if (newState >= NfDef.STATE_CONNECTED && prevState < NfDef.STATE_CONNECTED) {
                Log.e(TAG,"reqAvrcpCtRegisterEventWatcher");
                mCommandAvrcp.reqAvrcpRegisterEventWatcher(NfDef.AVRCP_EVENT_ID_TRACK_CHANGED, 0);
                mCommandAvrcp.reqAvrcpRegisterEventWatcher(NfDef.AVRCP_EVENT_ID_PLAYBACK_STATUS_CHANGED, 0);
                mCommandAvrcp.reqAvrcpRegisterEventWatcher(NfDef.AVRCP_EVENT_ID_PLAYBACK_POS_CHANGED, 0);
            }
            */
            mBtMusicHandler.removeMessages(MSG_AVRCP_CONNECT_CHANGED);
            mBtMusicHandler.sendMessageDelayed(mBtMusicHandler.obtainMessage(MSG_AVRCP_CONNECT_CHANGED, newState, prevState, address), 1000);
        }

        @Override
        public void retAvrcp13CapabilitiesSupportEvent(byte[] eventIds)
                throws RemoteException {
            Log.v(TAG,"retAvrcp13CapabilitiesSupportEvent()");
 
        }

        @Override
        public void retAvrcp13PlayerSettingAttributesList(byte[] attributeIds)
                throws RemoteException {
            Log.v(TAG,"retAvrcp13PlayerSettingAttributesList()");

        }

        @Override
        public void retAvrcp13PlayerSettingValuesList(byte attributeId,
                byte[] valueIds) throws RemoteException {
            Log.v(TAG,"retAvrcp13PlayerSettingValuesList() attributeId: " + attributeId);

        }

        @Override
        public void retAvrcp13PlayerSettingCurrentValues(byte[] attributeIds,
                byte[] valueIds) throws RemoteException {
            Log.v(TAG,"retAvrcp13PlayerSettingCurrentValues()");

        }

        @Override
        public void retAvrcp13SetPlayerSettingValueSuccess() throws RemoteException {
            Log.v(TAG,"retAvrcp13SetPlayerSettingValueSuccess()");

        }

        @Override
        public void retAvrcp13ElementAttributesPlaying(int[] metadataAtrributeIds,
                String[] texts) throws RemoteException {
            Log.v(TAG,"retAvrcp13ElementAttributesPlaying()");

            String title = "";
            String album = "";
            String artist = "";
            synchronized (mNowPlayingLock) {
	            for (int i = 0 ; i < metadataAtrributeIds.length ; i++ ) 
	            {
	                if (metadataAtrributeIds[i] == NfDef.AVRCP_META_ATTRIBUTE_ID_TITLE) {
	                    title = new String(texts[i]);
	                    Log.v(TAG,"retAvrcp13ElementAttributesPlaying() title: " + title);
	                    mNowPlaying.setTitle(title);
	                }
	                else if (metadataAtrributeIds[i] == NfDef.AVRCP_META_ATTRIBUTE_ID_ALBUM) {
	                    album = new String(texts[i]);
	                    Log.v(TAG,"retAvrcp13ElementAttributesPlaying() album: " + album);
	                    
	                }
	                else if (metadataAtrributeIds[i] == NfDef.AVRCP_META_ATTRIBUTE_ID_ARTIST) {
	                    artist = new String(texts[i]);
	                    Log.v(TAG,"retAvrcp13ElementAttributesPlaying() artist: " + artist);
	                    mNowPlaying.setArtist(artist);
	                } else if(metadataAtrributeIds[i] == NfDef.AVRCP_META_ATTRIBUTE_ID_NUMBER_OF_MEDIA) {
	                	int total = 0;
	                	try {
	                		total = Integer.parseInt(texts[i]);
	                	} catch(Exception e) {
	                		
	                	}
	                	mNowPlaying.setTotal(total);
	                } else if(metadataAtrributeIds[i] == NfDef.AVRCP_META_ATTRIBUTE_ID_SONG_LENGTH) {
	                	int dutarion = 0;
	                	try {
	                		dutarion = Integer.parseInt(texts[i]);
	                	} catch(Exception e) {
	                		
	                	}
	                	mNowPlaying.setDuration(dutarion);
	                }
	            }

				onNowPlayingUpdate();
			}
        }

        @Override
        public void retAvrcp13PlayStatus(long songLen, long songPos, byte statusId)
                throws RemoteException {
            Log.v(TAG,"retAvrcp13PlayStatus() songLen: " + songLen + " songPos: " + songPos + " statusId: " + statusId);
            boolean status = mPlayStatus;
            if(statusId == NfDef.AVRCP_PLAYING_STATUS_ID_STOPPED || statusId == NfDef.AVRCP_PLAYING_STATUS_ID_PAUSED) {
                status = false;
            } else if(statusId == NfDef.AVRCP_PLAYING_STATUS_ID_PLAYING) {
                status = true;
            }
            if(status != mPlayStatus) {
                mPlayStatus = status;
                processStatusId();
            }
        }

        @Override
        public void onAvrcp13EventPlaybackStatusChanged(byte statusId)
                throws RemoteException {
            Log.v(TAG,"onAvrcp13EventPlaybackStatusChanged() statusId: " + statusId);
            boolean status = mPlayStatus;
            if(statusId == NfDef.AVRCP_PLAYING_STATUS_ID_STOPPED || statusId == NfDef.AVRCP_PLAYING_STATUS_ID_PAUSED) {
                status = false;
            } else if(statusId == NfDef.AVRCP_PLAYING_STATUS_ID_PLAYING) {
                status = true;
            }
            if(status != mPlayStatus) {
                mPlayStatus = status;
                processStatusId();
            }
        }

        @Override
        public void onAvrcp13EventTrackChanged(long elementId)
                throws RemoteException {
            Log.v(TAG,"onAvrcp13EventTrackChanged() elemendId: " + elementId);
            mCurrentTrack = elementId;
            mCommandAvrcp.reqAvrcp13GetElementAttributesPlaying();

        }

        @Override
        public void onAvrcp13EventTrackReachedEnd() throws RemoteException {
            Log.v(TAG,"onAvrcp13EventTrackReachedEnd()");

        }

        @Override
        public void onAvrcp13EventTrackReachedStart() throws RemoteException {
            Log.v(TAG,"onAvrcp13EventTrackReachedStart()");

        }

        @Override
        public void onAvrcp13EventPlaybackPosChanged(long songPos)
                throws RemoteException {
            Log.v(TAG,"onAvrcp13EventPlaybackPosChanged() songPos: " + songPos);
            synchronized (mNowPlayingLock) {
				if (mNowPlaying == null || mNowPlaying.playbackStatus) {
					mNowPlaying.setPosition((int)songPos);
					onNowPlayingUpdate();
				}
			}
        }

        @Override
        public void onAvrcp13EventBatteryStatusChanged(byte statusId)
                throws RemoteException {
            Log.v(TAG,"onAvrcp13EventBatteryStatusChanged() statusId: " + statusId);

        }

        @Override
        public void onAvrcp13EventSystemStatusChanged(byte statusId)
                throws RemoteException {
            Log.v(TAG,"onAvrcp13EventSystemStatusChanged() statusId: " + statusId);

        }

        @Override
        public void onAvrcp13EventPlayerSettingChanged(byte[] attributeIds,
                byte[] valueIds) throws RemoteException {
            Log.v(TAG,"onAvrcp13EventPlayerSettingChanged()");

        }

        @Override
        public void onAvrcp14EventNowPlayingContentChanged() throws RemoteException {
            Log.v(TAG,"onAvrcp14EventNowPlayingContentChanged()");

        }

        @Override
        public void onAvrcp14EventAvailablePlayerChanged() throws RemoteException {
            Log.v(TAG,"onAvrcp14EventAvailablePlayerChanged()");

        }

        @Override
        public void onAvrcp14EventAddressedPlayerChanged(int playerId,
                int uidCounter) throws RemoteException {
            Log.v(TAG,"onAvrcp14EventAddressedPlayerChanged() playerId: " + playerId + " uidCounter: " + uidCounter);

        }

        @Override
        public void onAvrcp14EventUidsChanged(int uidCounter)
                throws RemoteException {
            Log.v(TAG,"onAvrcp14EventUidsChanged() uidCounter: " + uidCounter);

        }

        @Override
        public void onAvrcp14EventVolumeChanged(byte volume) throws RemoteException {
            Log.v(TAG,"onAvrcp14EventVolumeChanged() volume: " + volume);

        }

        @Override
        public void retAvrcp14SetAddressedPlayerSuccess() throws RemoteException {
            Log.v(TAG,"retAvrcp14SetAddressedPlayerSuccess()");

        }

        @Override
        public void retAvrcp14SetBrowsedPlayerSuccess(String[] path,
                int uidCounter, long itemCount) throws RemoteException {
            String p = "";
            for (int i=0;i<path.length;i++) {
                p += path[i];
            }
            Log.v(TAG,"retAvrcp14SetBrowsedPlayerSuccess() path: " + p + " uidCounter: " + uidCounter + " itemCount: " + itemCount);

        }

        @Override
        public void retAvrcp14FolderItems(int uidCounter, long itemCount)
                throws RemoteException {
            Log.v(TAG,"retAvrcp14FolderItems() uidCounter: " + uidCounter + " itemCount: " + itemCount);

        }
        
        @Override
        public void retAvrcp14MediaItems(int uidCounter, long itemCount)
                throws RemoteException {
            Log.v(TAG,"retAvrcp14MediaItems() uidCounter: " + uidCounter + " itemCount: " + itemCount);

        }

        @Override
        public void retAvrcp14ChangePathSuccess(long itemCount)
                throws RemoteException {
            Log.v(TAG,"retAvrcp14ChangePathSuccess() itemCount: " + itemCount);

        }

        @Override
        public void retAvrcp14ItemAttributes(int[] metadataAtrributeIds,
                String[] texts) throws RemoteException {
            Log.v(TAG,"retAvrcp14ItemAttributes()");

        }

        @Override
        public void retAvrcp14PlaySelectedItemSuccess() throws RemoteException {
            Log.v(TAG,"retAvrcp14PlaySelectedItemSuccess()");

        }

        @Override
        public void retAvrcp14SearchResult(int uidCounter, long itemCount)
                throws RemoteException {
            Log.v(TAG,"retAvrcp14SearchResult() uidCounter: " + uidCounter + " itemCount: " + itemCount);

        }

        @Override
        public void retAvrcp14AddToNowPlayingSuccess() throws RemoteException {
            Log.v(TAG,"retAvrcp14AddToNowPlayingSuccess()");

        }

        @Override
        public void retAvrcp14SetAbsoluteVolumeSuccess(byte volume)
                throws RemoteException {
            Log.v(TAG,"retAvrcp14SetAbsoluteVolumeSuccess() volume: " + volume);

        }

        @Override
        public void onAvrcpErrorResponse(int opId, int reason, byte eventId)
                throws RemoteException {
            Log.v(TAG,"onAvrcpErrorResponse() opId: " + opId + " reason: " + reason + " eventId: " + eventId);

        }

        @Override
        public void onAvrcp13RegisterEventWatcherSuccess(byte eventId) throws RemoteException {
            Log.v(TAG,"onAvrcp13RegisterEventWatcherSuccess() eventId: " + eventId);
      
        }

        @Override
        public void onAvrcp13RegisterEventWatcherFail(byte eventId) throws RemoteException {
            Log.v(TAG,"onAvrcp13RegisterEventWatcherFail() eventId: " + eventId);
  
        }
    };

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate");

		mComponentName = new ComponentName(getPackageName(), MediaButtonReceiver.class.getName());
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		IntentFilter filter = new IntentFilter();
		filter.addAction("com.hwatong.voice.CLOSE_BTMUSIC");
		registerReceiver(mReceiver, filter);
		regisIpodFocusReceiver();

		Log.v(TAG,"bindA2dpService");
		bindService(new Intent(NfDef.CLASS_SERVICE_A2DP), this.mConnection, BIND_AUTO_CREATE);
		Log.v(TAG,"bindAvrcpService");
		bindService(new Intent(NfDef.CLASS_SERVICE_AVRCP), this.mConnection, BIND_AUTO_CREATE);
		Log.v(TAG,"bindBluetoothService");
		bindService(new Intent("com.hwatong.bt.service"), mServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onDestroy() {
		mRequestPlay = false;
		pausePlayback();

		mAudioManager.unregisterMediaButtonEventReceiver(mComponentName);
		mAudioManager.abandonAudioFocus(this);

		unbindService(mServiceConnection);
		
		try {
			if (mCommandA2dp != null) {
	            mCommandA2dp.unregisterA2dpCallback(mCallbackA2dp);
	        }
	        if (mCommandAvrcp!= null) {
	            mCommandAvrcp.unregisterAvrcpCallback(mCallbackAvrcp);
	        }
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		unbindService(mConnection);

		unregisterReceiver(mReceiver);
		
		//LJW 20180508
		unRegisIpodFocusReceiver();

		Log.d(TAG, "onDestroy");
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		if (DBG) Log.i(TAG, "onBind: " + intent);
		return mBinder;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
            String action = intent.getAction();
            String cmd = intent.getStringExtra("command");
            if (DBG) Log.i(TAG, "onStartCommand " + action + " / " + cmd);

			if ("play".equals(cmd)) {
				play();
			} else if ("pause".equals(cmd)) {
				pause();
			} else if ("previous".equals(cmd)) {
				previous();
			} else if ("next".equals(cmd)) {
				next();
			}
		}

		return START_STICKY;
	}

	private List<Callback> mCallbacks = new ArrayList<Callback>();

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

    private void notifyConnected() {
        synchronized (mCallbacks) {
            int size = mCallbacks.size();
            for (int i = 0; i < size; i++) {
                Callback cb = mCallbacks.get(i);
                try {
                    cb.mCallback.onConnected();
                } catch (RemoteException e) {
					e.printStackTrace();
                }
            }
        }
    }

    private void notifyDisconnected() {
        synchronized (mCallbacks) {
            int size = mCallbacks.size();
            for (int i = 0; i < size; i++) {
                Callback cb = mCallbacks.get(i);
                try {
                    cb.mCallback.onDisconnected();
                } catch (RemoteException e) {
					e.printStackTrace();
                }
            }
        }
    }

    private void notifyNowPlayingUpdate(com.hwatong.btmusic.NowPlaying nowPlaying) {
        synchronized (mCallbacks) {
            int size = mCallbacks.size();
            for (int i = 0; i < size; i++) {
                Callback cb = mCallbacks.get(i);
                try {
                    cb.mCallback.nowPlayingUpdate(nowPlaying);
                } catch (RemoteException e) {
					e.printStackTrace();
                }
            }
        }
    }

	private void registerCallback(ICallback callback) {
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

	private void unregisterCallback(ICallback callback) {
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

	private boolean isBtMusicConnected() {
		synchronized (this) {
			return mA2dpConnected && mAvrcpConnected;
		}
	}

    private com.hwatong.btmusic.NowPlaying getNowPlaying() {
		com.hwatong.btmusic.NowPlaying nowPlaying = null;

		synchronized (mNowPlayingLock) {
			if (mNowPlaying != null) {
				nowPlaying = new com.hwatong.btmusic.NowPlaying();
				nowPlaying.set("title", mNowPlaying.title);
				nowPlaying.set("artist", mNowPlaying.artist);
				nowPlaying.set("duration", Integer.toString(mNowPlaying.duration));
				nowPlaying.set("position", Integer.toString(mNowPlaying.position));
				nowPlaying.set("total", Integer.toString(mNowPlaying.total));
				nowPlaying.set("playbackStatus", Boolean.toString(mNowPlaying.playbackStatus));
			}
		}

		return nowPlaying;
	}

	public void playPause() {
		if (DBG) Log.d(TAG, "playPause connected " + mAvrcpConnected);

		if (mCommandAvrcp != null && mAvrcpConnected) {
            serialPlayPause();
            /*
			try {
				if(mNowPlaying != null) {
					if(mNowPlaying.playbackStatus) {
						mCommandAvrcp.reqAvrcpPause();
					} else {
						mCommandAvrcp.reqAvrcpPlay();
					}
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
            */
		}
	}
	
	public void play() {
		if (DBG) Log.d(TAG, "play connected " + mAvrcpConnected);

		if (mCommandAvrcp != null && mCommandA2dp != null && mAvrcpConnected && mA2dpConnected) {
			mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
			mAudioManager.registerMediaButtonEventReceiver(mComponentName);

			mRequestPlay = true;
            mRequestPauseByAF = false;
            
            
//			resumePlayback();
            playDelayHandler.removeMessages(MSG_PLAY_DELEY);
            playDelayHandler.sendEmptyMessageDelayed(MSG_PLAY_DELEY, MSG_PLAY_DELEY_TIME);

			updateDispInfo();
		}
	}
	
	public void pause() {
		if (DBG) Log.d(TAG, "pause");

		if (mCommandAvrcp != null) {
            serialPause();
            
		}
	}
	
	public void stop() {
		if (mCommandAvrcp != null && mAvrcpConnected) {
			try {
				mCommandAvrcp.reqAvrcpStop();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	public void previous() {
        if (DBG) Log.d(TAG, "previous " + mAvrcpConnected);
		if (mCommandAvrcp != null && mAvrcpConnected) {
            serialPre();
            /*
			try {
				mCommandAvrcp.reqAvrcpBackward();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
            */
		}
	}

	public void next() {
        if (DBG) Log.d(TAG, "next " + mAvrcpConnected);
		if (mCommandAvrcp != null && mAvrcpConnected) {
            serialNext();
            /*
			try {
				mCommandAvrcp.reqAvrcpForward();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
            */
		}
	}

    public void startIEAP() {
        audioOpenIndex(0x02);
    }

    public void stopIEAP() {
        audioCloseIndex(0x02);
    }
	
	private static class ServiceImpl extends IService.Stub {
	    final WeakReference<Service> mService;

		private static class AsyncOp {
			private boolean done;

			public synchronized void exec(Handler handler, int what) {
				done = false;
                Log.d(TAG, "AsyncOp exec " + this);
	        	Message m = Message.obtain(handler, what, this);
	        	handler.sendMessage(m);

				while (!done) {
					try {
						wait();
					} catch (InterruptedException e){
						e.printStackTrace();
					}
				}
                Log.d(TAG, "AsyncOp exec done" + this);
			}

			public synchronized void complete() {
				done = true;
				notifyAll();
			}
		}

		private static final int MSG_PLAYPAUSE = 1;
		private static final int MSG_PLAY = 2;
		private static final int MSG_PAUSE = 3;
		private static final int MSG_STOP = 4;
		private static final int MSG_PREVIOUS = 5;
		private static final int MSG_NEXT = 6;
        private static final int MSG_STARTIEAP = 7;
        private static final int MSG_STOPIEAP = 8;

		private final Handler mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				final AsyncOp op = (AsyncOp)msg.obj;
                long now =  SystemClock.uptimeMillis();
                Log.d(TAG, "handleMessage begin " + op);
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
				case MSG_STOP:
	        		mService.get().stop();
					break;
				case MSG_PREVIOUS:
	        		mService.get().previous();
					break;
				case MSG_NEXT:
	        		mService.get().next();
					break;
                case MSG_STARTIEAP:
	        		mService.get().startIEAP();
					break;
				case MSG_STOPIEAP:
	        		mService.get().stopIEAP();
					break;
				}
				Log.d(TAG, "handleMessage end " + op);
	        	op.complete();
			}
		};

	    ServiceImpl(Service service) {
	        mService = new WeakReference<Service>(service);
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
		public boolean isBtMusicConnected() {
			return mService.get().isBtMusicConnected();
		}
		
		@Override
		public com.hwatong.btmusic.NowPlaying getNowPlaying() {
			return mService.get().getNowPlaying();
		}

		@Override
		public void playPause() {
			new AsyncOp().exec(mHandler, MSG_PLAYPAUSE);
		}
		
		@Override
		public void play() {
			Log.d(TAG, "play");
			new AsyncOp().exec(mHandler, MSG_PLAY);
		}
		
		@Override
		public void pause() {
			new AsyncOp().exec(mHandler, MSG_PAUSE);
		}
		
		@Override
		public void stop() {
			new AsyncOp().exec(mHandler, MSG_STOP);
		}

		@Override
		public void previous() {
			new AsyncOp().exec(mHandler, MSG_PREVIOUS);
		}

		@Override
		public void next() {
			new AsyncOp().exec(mHandler, MSG_NEXT);
		}

        @Override
		public void startIEAP() {
			new AsyncOp().exec(mHandler, MSG_STARTIEAP);
		}

		@Override
		public void stopIEAP() {
			new AsyncOp().exec(mHandler, MSG_STOPIEAP);
		}

	};

	private void resumePlayback() {
		audioOpenIndex(0x01);
		if (mCommandAvrcp != null && mAvrcpConnected) {
            serialPlay();
		}
	}
	
	private void pausePlayback() {
		audioCloseIndex(0x01);
		if (mCommandAvrcp != null) {
            serialPause();
		}
	}
    boolean audioOpen() {
		if(mCommandA2dp != null && mA2dpConnected) {
			try {
				Log.d(TAG, "startA2dpRender");
				mCommandA2dp.startA2dpRender();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
        return true;
    }

    void audioClose() {
		if (mCommandA2dp != null) {
			try {
				Log.d(TAG, "pauseA2dpRender");
				mCommandA2dp.pauseA2dpRender();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
    }

    int mAudioIndexMask;
    private void audioOpenIndex(int index) {
        Log.d(TAG, "audioOpenIndex index " + index + " mAudioIndexMask " + mAudioIndexMask);
        synchronized (this) {
            mAudioIndexMask |= index;
            audioOpen();
        }
    }

    private void audioCloseIndex(int index) {
        Log.d(TAG, "audioCloseIndex index " + index + " mAudioIndexMask " + mAudioIndexMask);
        synchronized (this) {
            mAudioIndexMask &= ~index;
            if(mAudioIndexMask == 0) {
                audioClose();
            }
        }
    }

	@Override
	public void onAudioFocusChange(int focusChange) {
		if (DBG) Log.d(TAG, "onAudioFocusChange: " + focusChange + ", mRequestPlay " + mRequestPlay);

		switch (focusChange) {

		case AudioManager.AUDIOFOCUS_GAIN:
			if (mRequestPlay) {
                if(mRequestPauseByAF == false || mBeforeVoice == true) {
				    resumePlayback();
                } else {
                    audioOpenIndex(0x01);
                }
			}
            mRequestPauseByAF = false;
			break;

		case AudioManager.AUDIOFOCUS_LOSS:
			mRequestPlay = false;
            mRequestPauseByAF = false;
            playDelayHandler.removeCallbacksAndMessages(null);
			pausePlayback();
			//LJW 20180508
			sendIpodFocusLoss();
			break;

		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            mRequestPauseByAF = true;
            if(mNowPlaying != null) {
                mBeforeVoice = mNowPlaying.playbackStatus;
            }
			pausePlayback();
			break;

		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:

			break;
		}
	}
	
	private void sendIpodFocusLoss() {
		Intent intent = new Intent("com.hwatong.btmusic.FOCUS_LOSS");
		sendBroadcast(intent);
	}
	
	private void regisIpodFocusReceiver() {
		IntentFilter intentFilter = new IntentFilter("com.hwatong.ipod.FOCUS_LOSS");
		registerReceiver(ipodFocusReceiver, intentFilter);
	}
	
	private void unRegisIpodFocusReceiver() {
		unregisterReceiver(ipodFocusReceiver);
	}
	
	private BroadcastReceiver ipodFocusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "receive com.hwatong.ipod.FOCUS_LOSS");
			if("com.hwatong.btmusic.ui.BluetoothMusicActivity".equals(com.hwatong.btmusic.util.Utils.getTopActivityName(context)))
//				play();
				playDelayHandler.sendEmptyMessageDelayed(MSG_FOCUSLOSS_PLAY_DELEY, MSG_PLAY_DELEY_TIME);
		}
		
	};

	private void updateDispInfo() {
		if (mRequestPlay && mNowPlaying != null) {
			Intent intent = new Intent("com.hwatong.player.info");
			intent.putExtra("type", "BT_MUSIC");
			intent.putExtra("status", mNowPlaying.playbackStatus);
			intent.putExtra("title", mNowPlaying.title);
			sendBroadcastAsUser(intent, UserHandle.ALL);
		} else {
			Intent intent = new Intent("com.hwatong.player.info");
			intent.putExtra("type", "BT_MUSIC");
			intent.putExtra("status", false);
			intent.putExtra("title", "");
			sendBroadcastAsUser(intent, UserHandle.ALL);
        }
	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			final String action = arg1.getAction();
			Log.d(TAG, "onReceive: " + action);

			if ("com.hwatong.voice.CLOSE_BTMUSIC".equals(action)) {
				mRequestPlay = false;
				pause();
			}
		}
	};
	private com.hwatong.bt.IService mService = null;

	private final ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			if (DBG) Log.i(TAG, "onServiceConnected");

			mService = com.hwatong.bt.IService.Stub.asInterface(service);

			try {
				String addr = null;
                int state = BtDef.BT_STATE_INVALID;
				if(mCommandA2dp != null) {
                    state = mCommandA2dp.getA2dpConnectionState();
					addr = mCommandA2dp.getA2dpConnectedAddress();
				}
				mService.updateRemoteDevice(addr, BtDef.BT_PROFILE_A2DP, mapState(state), mapState(state));
				
				addr = null;
                state = BtDef.BT_STATE_INVALID;
				if(mCommandAvrcp != null) {
                    state = mCommandAvrcp.getAvrcpConnectionState();
					addr = mCommandAvrcp.getAvrcpConnectedAddress();
				}
				mService.updateRemoteDevice(addr, BtDef.BT_PROFILE_AVRCP, mapState(state), mapState(state));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			if (DBG) Log.i(TAG, "onServiceDisconnected");

			if (mService != null) {
				mService = null;
			}
		}
	};

	private void onConnectChanged(boolean a2dpstate, boolean avrcpstate) {
		if ((a2dpstate && avrcpstate) != (mA2dpConnected && mAvrcpConnected)) {
			Log.e(TAG,"onConnectChanged a2dpstate " + a2dpstate + " avrcpstate " + avrcpstate);
			mA2dpConnected = a2dpstate;
			mAvrcpConnected = avrcpstate;
			if(mA2dpConnected && mAvrcpConnected) {
				try {
					Log.e(TAG,"reqAvrcpCtRegisterEventWatcher");
	                mCommandAvrcp.reqAvrcpRegisterEventWatcher(NfDef.AVRCP_EVENT_ID_TRACK_CHANGED, 1);
	                mCommandAvrcp.reqAvrcpRegisterEventWatcher(NfDef.AVRCP_EVENT_ID_PLAYBACK_STATUS_CHANGED, 1);
	                mCommandAvrcp.reqAvrcpRegisterEventWatcher(NfDef.AVRCP_EVENT_ID_PLAYBACK_POS_CHANGED, 1);
					mCommandAvrcp.reqAvrcp13GetPlayStatus();
					mCommandAvrcp.reqAvrcp13GetElementAttributesPlaying();
				} catch (RemoteException e) {
					e.printStackTrace();
				}

				notifyConnected();
			} else {
				if (mNowPlaying != null && mNowPlaying.playbackStatus) {
					gotoRadio();
				}

				notifyDisconnected();
			}
		} else {
			mA2dpConnected = a2dpstate;
			mAvrcpConnected = avrcpstate;
		}
	}

	private void onNowPlayingUpdate() {
		if (mNowPlaying != null) {
			updateDispInfo();
			
			final com.hwatong.btmusic.NowPlaying nowPlaying = getNowPlaying();
			
			if (nowPlaying != null) {
				Log.d(TAG, "NowPlaying: " + nowPlaying.get("title"));

				notifyNowPlayingUpdate(nowPlaying);
			}
		}
	}

	private void gotoRadio() {
		final Intent intent = new Intent("com.hwatong.media.RADIO_UI");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static class NowPlaying {
		String title;
		String artist;
		int duration;
		int position;
		int total;
		boolean playbackStatus;
        private boolean mTargetPlayStatus;

        public void setTitle(String value) {
        	title = value;
        }
        public void setArtist(String value) {
        	artist = value;
        }
        public void setDuration(int value) {
        	duration = value;
        }
        public void setPosition(int value) {
        	position = value;
        }
        public void setTotal(int value) {
        	total = value;
        }
        public void setStatus(boolean value) {
            Log.d(TAG, "playbackStatus " + value);
        	playbackStatus = value;
            mTargetPlayStatus = value;
        }
        
	};
	//MODIFY LJW 20180425
	
	private static final int MSG_PLAY_DELEY_TIME = 500;
	private static final int MSG_PLAY_DELEY = 999;
	private static final int MSG_FOCUSLOSS_PLAY_DELEY = 998;
	
    private final Handler playDelayHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(msg.what == MSG_FOCUSLOSS_PLAY_DELEY)
				play();
			else
				resumePlayback();
		}
    	
    };
	
    private static final int MSG_A2DP_CONNECT_CHANGED = 1;
    private static final int MSG_AVRCP_CONNECT_CHANGED = 2;

	private final Handler mBtMusicHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			
			if (msg.what == MSG_A2DP_CONNECT_CHANGED) {
				Boolean a2dpstate = (msg.arg1 == NfDef.STATE_CONNECTED || msg.arg1 == NfDef.STATE_STREAMING);
				String addr = (String) msg.obj;
				Log.d(TAG,"MSG_A2DP_CONNECT_CHANGED a2dpstate " + a2dpstate + " mA2dpConnected " + mA2dpConnected + " mRequestPlay " + mRequestPlay);
				try {
					if(mService != null) mService.updateRemoteDevice(addr, BtDef.BT_PROFILE_A2DP, mapState(msg.arg1), mapState(msg.arg2));
				} catch (RemoteException e) {
					e.printStackTrace();
				}
                if(a2dpstate != mA2dpConnected) {
                    if(!a2dpstate) {
                        mRequestPlay = false;
                        mRequestPauseByAF = false;
                    } else {
                        if(!mRequestPlay) {
                            pausePlayback();
                        }
                    }
                }
				onConnectChanged(a2dpstate, mAvrcpConnected);
			} else if (msg.what == MSG_AVRCP_CONNECT_CHANGED) {
				Boolean avrcpstate = ((msg.arg1 == NfDef.STATE_CONNECTED || msg.arg1 == NfDef.STATE_BROWSING));
				String addr = (String) msg.obj;
				Log.d(TAG,"MSG_AVRCP_CONNECT_CHANGED avrcpstate " + avrcpstate + " mAvrcpConnected " + mAvrcpConnected);
				try {
					if(mService != null) mService.updateRemoteDevice(addr, BtDef.BT_PROFILE_AVRCP, mapState(msg.arg1), mapState(msg.arg2));
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				onConnectChanged(mA2dpConnected, avrcpstate);
			}
		}
	};
    static int mapState(int state) {
        int ret = BtDef.BT_STATE_INVALID;
        switch(state) {
            case NfDef.STATE_READY:
                ret = BtDef.BT_STATE_READY;
                break;
            case NfDef.STATE_CONNECTING:
                ret = BtDef.BT_STATE_CONNECTING;
                break;
            case NfDef.STATE_CONNECTED:
                ret = BtDef.BT_STATE_CONNECTED;
                break;
            case NfDef.STATE_DISCONNECTING:
                ret = BtDef.BT_STATE_DISCONNECTING;
                break;
            case NfDef.STATE_STREAMING:
                ret = BtDef.BT_STATE_STREAMING;
                break;
            case NfDef.STATE_BROWSING:
                ret = BtDef.BT_STATE_BROWSING;
        }
        return ret;
    }

    static final int MSG_CMD_PLAYPAUSE = 1;
    static final int MSG_CMD_PRENEXT = 2;
    static long mLastCmdTime;
    static final long MAX_CMD_INTERVAL = 1000;
    static final int AUDIO_OPEN_DELAY = 9997;
    static final int AUDIO_OPEN_DELAY_TIME = 700;
    Handler mCommandHandler = new Handler() {
        @Override
			public void handleMessage(Message msg) {
                long now =  SystemClock.uptimeMillis();
                long interval = now - mLastCmdTime;
                Log.d(TAG, "now " + now);
                if (mCommandAvrcp == null || !mAvrcpConnected) {
                    Log.d(TAG, "not ready");
                    return;
                }
                if(interval < MAX_CMD_INTERVAL) {
                    Message newmsg = new Message();
                    newmsg.what = msg.what;
                    newmsg.arg1 = msg.arg1;
                    mCommandHandler.sendMessageDelayed(newmsg, interval);
                    Log.d(TAG, "interval not ready " + now);
                    return;
                }
                switch(msg.what) {
                    case MSG_CMD_PLAYPAUSE:
                    Log.d(TAG, "pause " + msg.arg1 + " playbackStatus " + mNowPlaying.playbackStatus + " mTargetPlayStatus " + mNowPlaying.mTargetPlayStatus);
        			try {
    					if((msg.arg1 == 0 || msg.arg1 == 2) && !mNowPlaying.mTargetPlayStatus) {
                            Log.d(TAG, "serial play");
                            mNowPlaying.mTargetPlayStatus = true;
    						mCommandAvrcp.reqAvrcpPlay();
    						if(mCommandHandler.hasMessages(AUDIO_OPEN_DELAY))
    							return;
    						audioOpenIndex(0x01);
    						mCommandHandler.sendEmptyMessageDelayed(AUDIO_OPEN_DELAY, AUDIO_OPEN_DELAY_TIME);
    					} else if((msg.arg1 == 1 || msg.arg1 == 2) && mNowPlaying.mTargetPlayStatus) {
                            Log.d(TAG, "serial pause");
                            mNowPlaying.mTargetPlayStatus = false;
    						mCommandAvrcp.reqAvrcpPause();
    					}
        			} catch (RemoteException e) {
        				e.printStackTrace();
        			}
                    break;
                    case MSG_CMD_PRENEXT:
                    boolean next = (msg.arg1 == 1);
                    try {
                        if(next) {
                            Log.d(TAG, "serial next");
                            mCommandAvrcp.reqAvrcpForward();
                        } else {
                            Log.d(TAG, "serial pre");
                            mCommandAvrcp.reqAvrcpBackward();
                        }
                    } catch (RemoteException e) {
        				e.printStackTrace();
        			}
                    break;
                    case AUDIO_OPEN_DELAY_TIME:
                    	Log.d(TAG, "AUDIO_OPEN_DELAY_TIME is out");
                    	break;
                }
                mLastCmdTime = SystemClock.uptimeMillis();
                Log.d(TAG, "end " + now);
            }
    };

    void serialPlayPause() {
        mCommandHandler.removeMessages(MSG_CMD_PLAYPAUSE);
        mCommandHandler.sendMessage(mCommandHandler.obtainMessage(MSG_CMD_PLAYPAUSE, 2, 0));
    }
    void serialPause() {
        mCommandHandler.removeMessages(MSG_CMD_PLAYPAUSE);
        mCommandHandler.sendMessage(mCommandHandler.obtainMessage(MSG_CMD_PLAYPAUSE, 1, 0));
    }
    void serialPlay() {
        mCommandHandler.removeMessages(MSG_CMD_PLAYPAUSE);
        mCommandHandler.sendMessage(mCommandHandler.obtainMessage(MSG_CMD_PLAYPAUSE, 0, 0));
    }
    void serialPre() {
        if(!mCommandHandler.hasMessages(MSG_CMD_PRENEXT)) {
            mCommandHandler.sendMessage(mCommandHandler.obtainMessage(MSG_CMD_PRENEXT, 0, 0));
        }
    }
    void serialNext() {
        if(!mCommandHandler.hasMessages(MSG_CMD_PRENEXT)) {
            mCommandHandler.sendMessage(mCommandHandler.obtainMessage(MSG_CMD_PRENEXT, 1, 0));
        }
    }
}
