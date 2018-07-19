package com.hwatong.btphone;

oneway interface ICallback {
	void onHfpConnected();
	void onHfpDisconnected();

	void onCallStatusChanged();

	void onRingStart();
	void onRingStop();

	void onHfpLocal();
	void onHfpRemote();

	void onPhoneBook(String type, String name, String number);
	void onContactsChange();
	void onPhoneBookDone(int error);

	void onCalllog(String type, String name, String number, String date);
	void onCalllogChange(String type);
	void onCalllogDone(String type, int error);
	
	void onAllDownloadDone(int error);
	
	void onSignalBattery();
}
