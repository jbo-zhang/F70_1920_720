package com.hwatong.bt;

oneway interface ICallback {
	void onLocalName(String name);
	
	void onAdapterState(int state);

	void onConnected();
	void onDisconnected();

	void onDiscovery(String name, String addr);
	void onDiscoveryDone();
	void onCurrentAndPairList(String index, String addr, String name);
}
