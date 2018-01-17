package com.hwatong.radio.ui.iview;

public interface IReceiverView {
	void close();

	void playChannel(int freq);

	void collect();

	void playPosition(int pos);
}
