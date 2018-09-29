package com.hwatong.btmusic;

import com.hwatong.btmusic.NowPlaying;

oneway interface ICallback {
	void onConnected();
	void onDisconnected();
	void nowPlayingUpdate(in NowPlaying nowPlaying);
}
