package com.hwatong.radio;

import com.hwatong.radio.ICallback;
import com.hwatong.radio.Channel;

interface IService {  
	void registerCallback(ICallback cb);  
	void unregisterCallback(ICallback cb);  

	int[] getStatus();
	
    List<Channel> getChannelList(int band);
    int addChannel(int band, int freq);
    void removeChannel(int band, int freq);

    List<Channel> getFavorChannelList();
    void addFavorChannel(int freq);
    void removeFavorChannel(int freq);

	int getCurrentBand();
	int getCurrentChannel(int band);

	void band();
	void tuneTo(int frequence, boolean add);
	void tuneDown();
	void tuneUp();
	void seekDown();
	void seekUp();
	void scan();
	void pause();
	void play();
	void playPause();
}
