package com.hwatong.bt;

import com.hwatong.bt.ICallback;
import com.hwatong.bt.BtDevice;

interface IService {
	void registerCallback(ICallback callback);
	void unregisterCallback(ICallback callback);	
	
	// setting
	int getAdapterState();
	
	boolean setEnable(boolean enable);

	String getLocalName();

	boolean setLocalName(String name);

	String getPinCode();

	boolean setPinCode(String pincode);
	
	String getLocalAddress();
	
	String getVersion();
	
	void setAutoConnect(boolean enable);
	
	boolean isAutoConnect();
	
	//connect info

	boolean getConnectState();

	BtDevice getConnectDevice();

	// devices list

	boolean deletePair(String addr);

	boolean startDiscovery();

	boolean inquiryPairList();

	boolean stopDiscovery();
	
	boolean connectDevice(String addr);
	
	void disconnect();
	void updateRemoteDevice(String addr, int profile, int state, int prestate);
}
