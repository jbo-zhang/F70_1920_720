package com.hwatong.radio.ui.iview;

import java.util.ArrayList;
import java.util.List;

import com.hwatong.radio.Frequence;

public interface IRadioView {
	void refreshView(int band, int freq, ArrayList<Frequence> list);
	void refreshView(int band, int freq);
	void showLoading();
	void hideLoading();
	void refreshChannelList(int freq, List<Frequence> list);
	void showFirstScan();
	void showPreview();
	void hidePreview();
}
