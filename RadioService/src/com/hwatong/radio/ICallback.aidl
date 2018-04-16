package com.hwatong.radio;

oneway interface ICallback {
	void onStatusChanged();

	void onChannelListChanged(int band);
	void onFavorChannelListChanged();

	void onChannelChanged();

	void onDisplayChanged(int band, int frequence);
}
