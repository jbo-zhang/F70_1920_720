package com.hwatong.btmusic;

import com.hwatong.btmusic.ICallback;
import com.hwatong.btmusic.NowPlaying;

interface IService {
	void registerCallback(ICallback callback);
	void unregisterCallback(ICallback callback);	

	boolean isBtMusicConnected();
	
    NowPlaying getNowPlaying();
	
	void playPause();
	void play();
	void pause();
	void stop();
	void previous();
	void next();

    void startIEAP();
    void stopIEAP();
}
