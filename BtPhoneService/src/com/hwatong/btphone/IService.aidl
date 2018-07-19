package com.hwatong.btphone;

import com.hwatong.btphone.CallStatus;
import com.hwatong.btphone.CallLog;
import com.hwatong.btphone.Contact;
import com.hwatong.btphone.ICallback;

interface IService {
	void registerCallback(ICallback callback);
	void unregisterCallback(ICallback callback);

	boolean isHfpConnected();
	
	// hfp

	void phoneAnswer();

	void phoneReject();
	
	void phoneFinish();

	void phoneDial(String phonenum);

	void phoneTransmitDTMFCode(char code);
	
	void phoneTransfer();

	void phoneTransferBack();

	void phoneMicOpenClose();
	
	CallStatus getCallStatus();

	boolean isHfpLocal();
	
	String getSignalBattery();
	
	void setAutoAnswer(boolean enable);
	
	boolean isAutoAnswer();
	
	boolean isMicMute();

	// contacts

	boolean phoneBookStartUpdate();
	List<Contact> getContactList();
	boolean isPhoneBookDone();

	boolean callLogStartUpdate(String type);
	List<CallLog> getCalllogList(String type);
	boolean removeCalllog(String token);
	boolean isCalllogDone(String type);
}
