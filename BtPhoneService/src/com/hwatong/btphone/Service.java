package com.hwatong.btphone;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.hwatong.providers.carsettings.*;

import com.hwatong.bt.BtDef;
import com.hwatong.btphone.util.Utils;
import com.nforetek.bt.aidl.INfCallbackA2dp;
import com.nforetek.bt.aidl.INfCallbackHfp;
import com.nforetek.bt.aidl.INfCallbackPbap;
import com.nforetek.bt.aidl.INfCommandA2dp;
import com.nforetek.bt.aidl.INfCommandBluetooth;
import com.nforetek.bt.aidl.INfCommandHfp;
import com.nforetek.bt.aidl.INfCommandPbap;
import com.nforetek.bt.aidl.NfHfpClientCall;
import com.nforetek.bt.aidl.NfPbapContact;
import com.nforetek.bt.res.NfDef;
import android.canbus.ICanbusService;
import android.canbus.ICarStatusListener;
import android.canbus.CarStatus;
import android.os.ServiceManager;
import java.io.UnsupportedEncodingException;
import android.content.ContentProviderOperation;
import android.provider.ContactsContract;
import android.os.HandlerThread;

public class Service extends android.app.Service implements
	MediaPlayer.OnCompletionListener {

	private static final String TAG = "BtPhoneService";
	private static final boolean DBG = false;

	private AudioManager mAudioManager;
    private static ICanbusService sService;

	private boolean mHfpConnected;
	private final static String RESET_EFFECT = "com.btphoneservice.changedeffect";

    private Object mCallStateWaitor = new Object();
	private String mCallState = CallStatus.PHONE_CALL_NONE;
	private String curPhoneNumber;
    private String curPhoneName;
	private long startTalkTime = -1;

	private static final int PBAP_UPDATE_IDLE = 0;
	private static final int PBAP_UPDATE_PHONE_CONTACT = 1;
	private static final int PBAP_UPDATE_SIM_CONTACT = 2;
	private static final int PBAP_UPDATE_DIALED_CALLS = 3;
	private static final int PBAP_UPDATE_RECEIVED_CALLS = 4;
	private static final int PBAP_UPDATE_MISSED_CALLS = 5;

	private int mContactUpdateProgress = PBAP_UPDATE_IDLE;
	private int mCurrentDownload = PBAP_UPDATE_IDLE;

	private final List<Contact> mContactList = new ArrayList<Contact>();
	private final List<CallLog> mCallOutList = new ArrayList<CallLog>();
	private final List<CallLog> mCallInList = new ArrayList<CallLog>();
	private final List<CallLog> mCallMissList = new ArrayList<CallLog>();

	private MediaPlayer ringPlayer = null;

    private final IBinder mBinder = new ServiceImpl(this);
	
	private INfCommandHfp mCommandHfp;
    private INfCommandPbap mCommandPbap;
    
    private int mSignal;
    private int mBattery;
    
    private boolean mHfpAudioConnectioned;
    private boolean mInBandRing;
    private boolean mIsPhoneBookDone;
    private boolean mIsCalllogOutDone;
    private boolean mIsCalllogInDone;
    private boolean mIsCalllogMissDone;
    
    private boolean mAutoAnswer;
    HandlerThread mPhoneBookThread;
    PhoneBookHandler mPhoneBookHandler;

    HandlerThread mCanThread;
    CanHandler mCanHandler;

    boolean mVoiceMuted = false;

    static final int MSG_PHONEBOOK_UPDATE = 1;
    class PhoneBookHandler extends Handler {
        PhoneBookHandler(Looper looper) {
            super(looper);
        }
        @Override
	    public void handleMessage(Message msg) {
            final ContentResolver resolver = getContentResolver();
            
            switch(msg.what) {
            case MSG_PHONEBOOK_UPDATE:
                Log.d(TAG, "MSG_PHONEBOOK_UPDATE");
                try {
                    Cursor cursor = resolver.query(ContactsContract.RawContacts.CONTENT_URI, new String[]{"_id"}, null,null,null);
                    if(cursor != null) {
                        while(cursor.moveToNext()) {
                            int id = cursor.getInt(0);
                            resolver.delete(ContactsContract.RawContacts.CONTENT_URI, "_id=?", new String[]{id+""});
                            try {
                                synchronized (mCallStateWaitor) {
                                    if(!CallStatus.PHONE_CALL_NONE.equals(mCallState)) {
                                        Log.d(TAG, "MSG_PHONEBOOK_UPDATE delete delay");
                                        mCallStateWaitor.wait(100);
                                    }
                                }
                            } catch(Exception e) {
                            }
                        }
                        cursor.close();
                    }
                    //resolver.delete(ContactsContract.RawContacts.CONTENT_URI, "", null);
                } catch (Exception e) {
                }
                if(msg.obj == null) {
                    Log.d(TAG, "MSG_PHONEBOOK_UPDATE delete");
                } else {
                    List<Contact> list = (List<Contact>)msg.obj;
                    for (int i = 0; i < list.size(); i++) {
                        if(mPhoneBookHandler.hasMessages(MSG_PHONEBOOK_UPDATE)) {
                            break;
                        }
                        final Contact contact = list.get(i);
                        final ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
                        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, "")
                                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, "")
                                .withYieldAllowed(true)
                                .build());
                        
                        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact.name)
                                .withYieldAllowed(true)
                                .build());
                        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.number)
                                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                                .withYieldAllowed(true)
                                .build());
                        try {
                            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            synchronized (mCallStateWaitor) {
                                if(!CallStatus.PHONE_CALL_NONE.equals(mCallState)) {
                                    Log.d(TAG, "MSG_PHONEBOOK_UPDATE delay");
                                    mCallStateWaitor.wait(100);
                                }
                            }
                        } catch(Exception e) {
                        }
                    }
                }
                Log.d(TAG, "MSG_PHONEBOOK_UPDATE end");
                break;
            }
        }
    }
    
    static final int MSG_CAN_PAIRED = 1;
    static final int MSG_CAN_PHONE = 2;
    static final int MSG_CAN_TEL = 3;
    static final int MSG_CAN_CALLINFO = 4;
    class CanHandler extends Handler {
        CanHandler(Looper looper) {
            super(looper);
        }
        @Override
	    public void handleMessage(Message msg) {
            int val;
            final ICanbusService canbus = getCanbusService();
            if(canbus == null) {
                return;
            }
            try {
                switch(msg.what) {
                case MSG_CAN_PAIRED:
                    Log.d(TAG, "MSG_CAN_PAIRED");
        			canbus.writeIPCPhone(0x0F /*TEL:Invalid*/, 
                                         msg.arg1 /*BTPairing:paired or not*/, 
                                         0x03 /*Phone:Invalid*/, 
                                         0x03 /*BTWarnPage:Invalid*/, 
                                         0x03 /*CallLog:Invalid*/, 
                                         0x03 /*Call_ID:Invalid*/, 
                                         0xFF /*CallLength_STD:Invalid*/, 
                                         0x3F /*CallLength_MIN:Invalid*/, 
                                         0x3F /*CallLength_SEC:Invalid*/
                                         );
    
                    break;
                case MSG_CAN_PHONE:
                    Log.d(TAG, "MSG_CAN_PHONE");
                    
                    canbus.writeIPCPhone(0x0F /*TEL:Invalid*/, 
                                         (msg.arg2 > 0x03)?0x01:0x03 /*BTPairing:Invalid*/, 
                                         msg.arg1 /*Phone:x*/, 
                                         0x03 /*BTWarnPage:Invalid*/, 
                                         0x03 /*CallLog:Invalid*/, 
                                         0x03 /*Call_ID:Invalid*/, 
                                         0xFF /*CallLength_STD:Invalid*/, 
                                         0x3F /*CallLength_MIN:Invalid*/, 
                                         0x3F /*CallLength_SEC:Invalid*/
                                         );
                    break;
                case MSG_CAN_TEL:
                    Log.d(TAG, "MSG_CAN_TEL");
       				canbus.writeIPCPhone(msg.arg1 /*TEL:x*/, 
                                         (msg.arg2 > 0x03)?0x01:0x03 /*BTPairing:Invalid*/, 
                                         0x03 /*Phone:Invalid*/, 
                                         0x03 /*BTWarnPage:Invalid*/, 
                                         0x03 /*CallLog:Invalid*/, 
                                         0x03 /*Call_ID:Invalid*/, 
                                         0xFF /*CallLength_STD:Invalid*/, 
                                         0x3F /*CallLength_MIN:Invalid*/, 
                                         0x3F /*CallLength_SEC:Invalid*/
                                         ); 
                    break;
                case MSG_CAN_CALLINFO:
                    Log.d(TAG, "MSG_CAN_CALLINFO");
                    syncIPCCallInfo(); 
                    break;
                }
            } catch (RemoteException e) {
    			e.printStackTrace();
    		}
        }
    }

    Runnable mAutoAnswerRunnable = new Runnable() {

		@Override
		public void run() {
			if (mCommandHfp != null && mHfpConnected && CallStatus.PHONE_COMING.equals(mCallState)) {
				try {
					mCommandHfp.reqHfpAnswerCall(NfDef.CALL_ACCEPT_NONE);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
    };
    
    private ServiceConnection mConnection = new ServiceConnection() {
	    public void onServiceConnected(ComponentName className, IBinder service) {
	        Log.e(TAG, "ready onServiceConnected");
	
	        Log.v(TAG,"Piggy Check className : " + className);
	
	        Log.e(TAG,"IBinder service: " + service.hashCode());
	        try {
	            Log.v(TAG,"Piggy Check service : " + service.getInterfaceDescriptor());
	        } catch (RemoteException e1) {
	            e1.printStackTrace();
	        }
	
	        if (className.equals(new ComponentName(NfDef.PACKAGE_NAME, NfDef.CLASS_SERVICE_HFP))) {
                Log.e(TAG,"ComponentName(" + NfDef.CLASS_SERVICE_HFP + ")");
                mCommandHfp = INfCommandHfp.Stub.asInterface(service);
                if (mCommandHfp == null) {
                    Log.e(TAG,"mCommandHfp is null!!");
                    return;
                }

                try {
                    mCommandHfp.registerHfpCallback(mCallbackHfp);
                    int state = mCommandHfp.getHfpConnectionState();
                    String addr = null;
                    if(state == NfDef.STATE_CONNECTED) {
                    	addr = mCommandHfp.getHfpConnectedAddress();
                    } else {
                    }
                    
                    //mHfpHandler.removeMessages(MSG_HFP_CONNECT_RECEIVED);
                    mHfpHandler.sendMessageDelayed(mHfpHandler.obtainMessage(MSG_HFP_CONNECT_RECEIVED, state, state, addr), 100);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                
                
            } else if (className.equals(new ComponentName(NfDef.PACKAGE_NAME, NfDef.CLASS_SERVICE_PBAP))) {
                Log.e(TAG,"ComponentName(" + NfDef.CLASS_SERVICE_PBAP + ")");
                mCommandPbap = INfCommandPbap.Stub.asInterface(service);
                if (mCommandPbap == null) {
                    Log.e(TAG,"mCommandPbap is null !!");
                    return;
                }

                try {
                    mCommandPbap.registerPbapCallback(mCallbackPbap);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
	        
	        Log.e(TAG, "end onServiceConnected");
	    }
	    public void onServiceDisconnected(ComponentName className) {
	        Log.e(TAG, "ready onServiceDisconnected: " + className);
	        if (className.equals(new ComponentName(NfDef.PACKAGE_NAME, NfDef.CLASS_SERVICE_HFP))) {
	        	mCommandHfp = null;
	        } else if (className.equals(new ComponentName(NfDef.PACKAGE_NAME, NfDef.CLASS_SERVICE_PBAP))) {
	        	mCommandPbap = null;
	        }
	
	        Log.e(TAG, "end onServiceDisconnected");
	    }
    };

    /*
     * Hfp callback
     * 
     */
    private INfCallbackHfp mCallbackHfp = new INfCallbackHfp.Stub() {

        @Override
        public void onHfpServiceReady() throws RemoteException {
            Log.v(TAG,"onHfpServiceReady()");

        }

        @Override
        public void onHfpStateChanged(String address, int prevState, int newState)
                throws RemoteException {
            Log.v(TAG,"onHfpStateChanged() " + address + " state: " + prevState + "->" + newState);

            //mHfpHandler.removeMessages(MSG_HFP_CONNECT_RECEIVED);
            mHfpHandler.sendMessageDelayed(mHfpHandler.obtainMessage(MSG_HFP_CONNECT_RECEIVED, newState, prevState, address), 100);
        }

        @Override
        public void onHfpAudioStateChanged(String address, int prevState, int newState)
                throws RemoteException {
            Log.v(TAG,"onHfpAudioStateChanged() " + address + " state: " + prevState + "->" + newState);
            if(newState == NfDef.STATE_CONNECTED && mHfpAudioConnectioned == false) {
            	onHfpRemote();
            } else if(newState != NfDef.STATE_CONNECTED && mHfpAudioConnectioned == true) {
            	onHfpLocal();
            }
        }

        @Override
        public void onHfpVoiceDial(String address, boolean isVoiceDialOn) throws RemoteException {
            Log.v(TAG,"onHfpVoiceDial() " + address + " isVoiceDialOn: " + isVoiceDialOn);

        }

        @Override
        public void onHfpErrorResponse(String address, int code) throws RemoteException {
            Log.v(TAG,"onHfpErrorResponse() " + address + " code: " + code);

        }

        @Override
        public void onHfpRemoteTelecomService(String address, boolean isTelecomServiceOn)
                throws RemoteException {
            Log.v(TAG,"onHfpRemoteTelecomService() " + address + " isTelecomServiceOn: " + isTelecomServiceOn);

        }

        @Override
        public void onHfpRemoteRoamingStatus(String address, boolean isRoamingOn)
                throws RemoteException {
            Log.v(TAG,"onHfpRemoteRoamingStatus() " + address + " isRoamingOn: " + isRoamingOn);

        }

        @Override
        public void onHfpRemoteBatteryIndicator(String address, int currentValue, int maxValue,
                int minValue) throws RemoteException {
            Log.v(TAG,"onHfpRemoteBatteryIndicator() " + address + " value: " + currentValue + " (" + minValue + "-" + maxValue + ")");
            mBattery = currentValue;
            notifySignalBattery();
        }

        @Override
        public void onHfpRemoteSignalStrength(String address, int currentStrength, int maxStrength,
                int minStrength) throws RemoteException {
            Log.v(TAG,"onHfpRemoteSignalStrength() " + address + " strength: " + currentStrength + " (" + minStrength + "-" + maxStrength + ")");
            mSignal = currentStrength;
            notifySignalBattery();
        }

        @Override
        public void onHfpCallChanged(String address, NfHfpClientCall call) throws RemoteException {
            Log.v(TAG,"onHfpCallChanged() " + address + " " + call);
            if(call.getId() <= 1) {
            	processCall(call);
            } else {
            	int callstate = call.getState();
            	Log.d(TAG, "callstate " + callstate + " mCallState " + mCallState);
            	if(callstate == NfHfpClientCall.CALL_STATE_TERMINATED) {
            		processCall(call);
            	} else if(callstate == NfHfpClientCall.CALL_STATE_INCOMING) {
            		processCall(call);
            	} else if(callstate == NfHfpClientCall.CALL_STATE_ACTIVE) {
            		processCall(call);
            	}
            }
        }
    };
    
    /*
     * Pbap Callback
     * 
     */
    private INfCallbackPbap mCallbackPbap = new INfCallbackPbap.Stub() {

        @Override
        public void onPbapServiceReady() throws RemoteException {
            Log.v(TAG,"onPbapServiceReady()");

        }
        
        @Override
        public void onPbapStateChanged(String address, int prevState,
                int newState, int reason, int counts) throws RemoteException {
            Log.v(TAG,"onPbapStateChanged() " + address + " state: " + prevState + "->" + newState + " reason: " + reason + " counts: " + counts);
            if(newState == NfDef.STATE_READY) {
	            if(reason == NfDef.REASON_DOWNLOAD_FAILED) {
	            	if(mCurrentDownload == PBAP_UPDATE_PHONE_CONTACT || mCurrentDownload == PBAP_UPDATE_SIM_CONTACT) {
	            		notifyPhoneBookDone(BtPhoneDef.PBAP_DOWNLOAD_FAILED);
	            	} else if(mCurrentDownload == PBAP_UPDATE_DIALED_CALLS) {
	            		notifyCalllogDone(CallLog.TYPE_CALL_OUT, BtPhoneDef.PBAP_DOWNLOAD_FAILED);
	            	} else if(mCurrentDownload == PBAP_UPDATE_RECEIVED_CALLS) {
	            		notifyCalllogDone(CallLog.TYPE_CALL_IN, BtPhoneDef.PBAP_DOWNLOAD_FAILED);
	            	} else if(mCurrentDownload == PBAP_UPDATE_MISSED_CALLS) {
	            		notifyCalllogDone(CallLog.TYPE_CALL_MISS, BtPhoneDef.PBAP_DOWNLOAD_FAILED);
	            	}
	            	if(mContactUpdateProgress != PBAP_UPDATE_IDLE) {
	            		notifyAllDownloadDone(BtPhoneDef.PBAP_DOWNLOAD_FAILED);
	            	}
	            	mContactUpdateProgress = PBAP_UPDATE_IDLE;
	            } else if(reason == NfDef.REASON_DOWNLOAD_TIMEOUT) {
	            	if(mCurrentDownload == PBAP_UPDATE_PHONE_CONTACT || mCurrentDownload == PBAP_UPDATE_SIM_CONTACT) {
	            		notifyPhoneBookDone(BtPhoneDef.PBAP_DOWNLOAD_TIMEOUT);
	            	} else if(mCurrentDownload == PBAP_UPDATE_DIALED_CALLS) {
	            		notifyCalllogDone(CallLog.TYPE_CALL_OUT, BtPhoneDef.PBAP_DOWNLOAD_TIMEOUT);
	            	} else if(mCurrentDownload == PBAP_UPDATE_RECEIVED_CALLS) {
	            		notifyCalllogDone(CallLog.TYPE_CALL_IN, BtPhoneDef.PBAP_DOWNLOAD_TIMEOUT);
	            	} else if(mCurrentDownload == PBAP_UPDATE_MISSED_CALLS) {
	            		notifyCalllogDone(CallLog.TYPE_CALL_MISS, BtPhoneDef.PBAP_DOWNLOAD_TIMEOUT);
	            	}
	            	if(mContactUpdateProgress != PBAP_UPDATE_IDLE) {
	            		notifyAllDownloadDone(BtPhoneDef.PBAP_DOWNLOAD_TIMEOUT);
	            	}
	            	mContactUpdateProgress = PBAP_UPDATE_IDLE;
	            } else if(reason == NfDef.REASON_DOWNLOAD_USER_REJECT) {
	            	if(mCurrentDownload == PBAP_UPDATE_PHONE_CONTACT || mCurrentDownload == PBAP_UPDATE_SIM_CONTACT) {
	            		notifyPhoneBookDone(BtPhoneDef.PBAP_DOWNLOAD_REJECT);
	            	} else if(mCurrentDownload == PBAP_UPDATE_DIALED_CALLS) {
	            		notifyCalllogDone(CallLog.TYPE_CALL_OUT, BtPhoneDef.PBAP_DOWNLOAD_REJECT);
	            	} else if(mCurrentDownload == PBAP_UPDATE_RECEIVED_CALLS) {
	            		notifyCalllogDone(CallLog.TYPE_CALL_IN, BtPhoneDef.PBAP_DOWNLOAD_REJECT);
	            	} else if(mCurrentDownload == PBAP_UPDATE_MISSED_CALLS) {
	            		notifyCalllogDone(CallLog.TYPE_CALL_MISS, BtPhoneDef.PBAP_DOWNLOAD_REJECT);
	            	}
	            	if(mContactUpdateProgress != PBAP_UPDATE_IDLE) {
	            		notifyAllDownloadDone(BtPhoneDef.PBAP_DOWNLOAD_REJECT);
	            	}
	            	mContactUpdateProgress = PBAP_UPDATE_IDLE;
	            } else if(reason == NfDef.REASON_DOWNLOAD_FULL_CONTENT_COMPLETED) {
	            	if(mCurrentDownload == PBAP_UPDATE_PHONE_CONTACT) {
	            		mPbapHandler.removeMessages(MSG_PHONE_BOOK_DONE);
	            		mPbapHandler.sendEmptyMessageDelayed(MSG_PHONE_BOOK_DONE, 2000);
	            	} else if(mCurrentDownload == PBAP_UPDATE_SIM_CONTACT) {
	            		mPbapHandler.removeMessages(MSG_SIM_BOOK_DONE);
	            		mPbapHandler.sendEmptyMessageDelayed(MSG_SIM_BOOK_DONE, 2000);
	            	} else if(mCurrentDownload == PBAP_UPDATE_DIALED_CALLS) {
	            		mPbapHandler.removeMessages(MSG_CALLLOG_OUT_DONE);
	            		mPbapHandler.sendEmptyMessageDelayed(MSG_CALLLOG_OUT_DONE, 2000);
	            	} else if(mCurrentDownload == PBAP_UPDATE_RECEIVED_CALLS) {
	            		mPbapHandler.removeMessages(MSG_CALLLOG_IN_DONE);
	            		mPbapHandler.sendEmptyMessageDelayed(MSG_CALLLOG_IN_DONE, 2000);
	            	} else if(mCurrentDownload == PBAP_UPDATE_MISSED_CALLS) {
	            		mPbapHandler.removeMessages(MSG_CALLLOG_MISS_DONE);
	            		mPbapHandler.sendEmptyMessageDelayed(MSG_CALLLOG_MISS_DONE, 2000);
	            	}
	            }
	            mCurrentDownload = PBAP_UPDATE_IDLE;
            }
        }

        @Override
        public void retPbapDownloadedContact(NfPbapContact contact) throws RemoteException {
        	if (DBG) Log.v(TAG,"retPbapDownloadedContact() mCurrentDownload " + mCurrentDownload + " mContactUpdateProgress " + mContactUpdateProgress + " " + contact);
        	if(contact != null) {
        		int type = contact.getStorageType();
        		String[] numbers = contact.getNumberArray();
        		String name = contact.getLastName()+contact.getMiddleName()+contact.getFirstName();
        		if (DBG) Log.v(TAG,"retPbapDownloadedContact() type " + type + " name " + name);
        		if(numbers != null && name != null) {
	        		for(int i=0;i<numbers.length;i++) {
		        		switch(type) {
			        		case NfPbapContact.STORAGE_TYPE_SIM: {
		        				onSimBook(name, numbers[i]);
		        				break;
		        			}
		        			case NfPbapContact.STORAGE_TYPE_PHONE_MEMORY: {
		        				onPhoneBook(name, numbers[i]);
		        				break;
		        			}
		        		}
	        		}
        		}
        	}
        }

        @Override
        public void retPbapDownloadedCallLog(String address, String firstName, String middleName,
                String lastName, String number, int type, String timestamp) throws RemoteException {
            if (DBG) Log.v(TAG,"retPbapDownloadedCallLog() " + address + " lastName: " + lastName + " middleName: " + middleName + " firstName: " + firstName + " (" + type + ")");
            String name = lastName+middleName+firstName;
    		switch(type) {
				case NfPbapContact.STORAGE_TYPE_MISSED_CALLS: {
					onCalllog(CallLog.TYPE_CALL_MISS, name, number, timestamp);
					break;
				}
				case NfPbapContact.STORAGE_TYPE_RECEIVED_CALLS: {
					onCalllog(CallLog.TYPE_CALL_IN, name, number, timestamp);
					break;
				}
				case NfPbapContact.STORAGE_TYPE_DIALED_CALLS: {
					onCalllog(CallLog.TYPE_CALL_OUT, name, number, timestamp);
					break;
				}
    		}
        }

        @Override
        public void onPbapDownloadNotify(String address, int storage, int totalContacts, int downloadedContacts) throws RemoteException {
            if (DBG) Log.v(TAG, "onPbapDownloadNotify() " + address + " storage: " + storage + " downloaded: " + downloadedContacts + "/" + totalContacts);

        }

        @Override
        public void retPbapDatabaseQueryNameByNumber(String address,
                String target, String name, boolean isSuccess)
                        throws RemoteException {
            if (DBG) Log.v(TAG,"retPbapDatabaseQueryNameByNumber() " + address + " target: " + target + " name: " + name + " isSuccess: " + isSuccess);

        }

        @Override
        public void retPbapDatabaseQueryNameByPartialNumber(String address,
                String target, String[] names, String[] numbers,
                boolean isSuccess) throws RemoteException {
            if (DBG) Log.v(TAG,"retPbapDatabaseQueryNameByPartialNumber() " + address + " target: " + target + " isSuccess: " + isSuccess);
        }

        @Override
        public void retPbapDatabaseAvailable(String address)
                throws RemoteException {
            if (DBG) Log.v(TAG,"retPbapDatabaseAvailable() " + address);
        }

        @Override
        public void retPbapDeleteDatabaseByAddressCompleted(String address,
                boolean isSuccess) throws RemoteException {
            if (DBG) Log.v(TAG,"retPbapDeleteDatabaseByAddressCompleted() " + address + " isSuccess: " + isSuccess);
        }

        @Override
        public void retPbapCleanDatabaseCompleted(boolean isSuccess)
                throws RemoteException {
            if (DBG) Log.v(TAG,"retPbapCleanDatabaseCompleted() isSuccess: " + isSuccess);
        }

    };

    ICarStatusListener mICarStatusListener = new ICarStatusListener.Stub() {

		@Override
		public void onReceived(CarStatus status) throws RemoteException {
            if(status == null) return;
            mHandler.sendMessage(mHandler.obtainMessage(MSG_MANAGER_VOICE, status));
		}

    };

    private synchronized void setVoiceMuted(boolean mute) {
    	Log.d(TAG, "setVoiceMuted mute " + mute);
        if(mute != mVoiceMuted) {
            mVoiceMuted = mute;
            mAudioManager.setStreamMute(AudioManager.STREAM_VOICE_CALL, mute);
        }
    }

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate");

		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        setVoiceMuted(true);

		ringPlayer = new MediaPlayer();
		
		mAutoAnswer = Settings.getInt(getContentResolver(), SettingsProvider.BLUETOOTH_AUTO_ANSWER, 0) == 1;

		IntentFilter filter = new IntentFilter();
		filter.addAction("com.hwatong.intent.action.TALK_BUTTON");
		registerReceiver(mTelButtonListener, filter);

        filter = new IntentFilter(Intent.ACTION_SHUTDOWN);
        filter .setPriority(1000);
        registerReceiver(mShutdownIntentReceiver, filter);

        Log.v(TAG,"bindHfpService");
        bindService(new Intent(NfDef.CLASS_SERVICE_HFP), this.mConnection, BIND_AUTO_CREATE);
        Log.v(TAG,"bindPbapService");
        bindService(new Intent(NfDef.CLASS_SERVICE_PBAP), this.mConnection, BIND_AUTO_CREATE);
        Log.v(TAG,"bindBluetoothService");
		bindService(new Intent("com.hwatong.bt.service"), mServiceConnection, BIND_AUTO_CREATE);
        ICanbusService canbus = getCanbusService();
        try {
            canbus.addCarStatusListener(mICarStatusListener);
        } catch(Exception e) {
            e.printStackTrace();
        }
        mPhoneBookThread = new HandlerThread("PhoneBookThread");
        mPhoneBookThread.start();
        mPhoneBookHandler = new PhoneBookHandler(mPhoneBookThread.getLooper());

        mCanThread = new HandlerThread("CanThread");
        mCanThread.start();
        mCanHandler = new CanHandler(mCanThread.getLooper());
	}

	@Override
	public void onDestroy() {
		unbindService(mServiceConnection);

		try {
			if (mCommandHfp!= null) {
				mCommandHfp.unregisterHfpCallback(mCallbackHfp);
			}
			if (mCommandPbap!= null) {
                mCommandPbap.unregisterPbapCallback(mCallbackPbap);
            }
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		unbindService(mConnection);
		ringPlayer.release();
		ringPlayer = null;

		mAudioManager.abandonAudioFocus(null);

		unregisterReceiver(mShutdownIntentReceiver);
		unregisterReceiver(mTelButtonListener);

		mPbapHandler.removeCallbacksAndMessages(null);

		mAudioHandler.removeCallbacksAndMessages(null);

		Log.d(TAG, "onDestroy");
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		if (DBG) Log.i(TAG, "onBind: " + intent);
		return mBinder;
	}

	private List<Callback> mCallbacks = new ArrayList<Callback>();

    private final class Callback implements IBinder.DeathRecipient {
        final ICallback mCallback;

        Callback(ICallback callback) {
            mCallback = callback;
        }

        @Override
        public void binderDied() {
            if (DBG) Log.d(TAG, "callback died");

            synchronized (mCallbacks) {
                mCallbacks.remove(this);
            }
            if (mCallback != null) {
                mCallback.asBinder().unlinkToDeath(this, 0);
            }
        }
    }

    private void notifyHfpConnected() {
        Log.d(TAG, "notifyHfpConnected");
        synchronized (mCallbacks) {
            int size = mCallbacks.size();
            for (int i = 0; i < size; i++) {
                Callback cb = mCallbacks.get(i);
                try {
                    cb.mCallback.onHfpConnected();
                } catch (RemoteException e) {
					e.printStackTrace();
                }
            }
        }
    }

    private void notifyHfpDisconnected() {
        Log.d(TAG, "notifyHfpDisconnected");
        synchronized (mCallbacks) {
            int size = mCallbacks.size();
            for (int i = 0; i < size; i++) {
                Callback cb = mCallbacks.get(i);
                try {
                    cb.mCallback.onHfpDisconnected();
                } catch (RemoteException e) {
					e.printStackTrace();
                }
            }
        }
    }

    private void notifyCallStatusChanged() {
        Log.d(TAG, "notifyCallStatusChanged " + mCallState);
        synchronized (mCallbacks) {
            int size = mCallbacks.size();
            for (int i = 0; i < size; i++) {
                Callback cb = mCallbacks.get(i);
                try {
                    cb.mCallback.onCallStatusChanged();
                } catch (RemoteException e) {
					e.printStackTrace();
                }
            }
        }
    }

    private void notifyRingStart() {
        Log.d(TAG, "notifyRingStart");
        synchronized (mCallbacks) {
            int size = mCallbacks.size();
            for (int i = 0; i < size; i++) {
                Callback cb = mCallbacks.get(i);
                try {
                    cb.mCallback.onRingStart();
                } catch (RemoteException e) {
					e.printStackTrace();
                }
            }
        }
    }

    private void notifyRingStop() {
        Log.d(TAG, "notifyRingStop");
        synchronized (mCallbacks) {
            int size = mCallbacks.size();
            for (int i = 0; i < size; i++) {
                Callback cb = mCallbacks.get(i);
                try {
                    cb.mCallback.onRingStop();
                } catch (RemoteException e) {
					e.printStackTrace();
                }
            }
        }
    }

    private void notifyHfpLocal() {
        Log.d(TAG, "notifyHfpLocal");
        synchronized (mCallbacks) {
            int size = mCallbacks.size();
            for (int i = 0; i < size; i++) {
                Callback cb = mCallbacks.get(i);
                try {
                    cb.mCallback.onHfpLocal();
                } catch (RemoteException e) {
					e.printStackTrace();
                }
            }
        }
    }

    private void notifyHfpRemote() {
        Log.d(TAG, "notifyHfpRemote");
        synchronized (mCallbacks) {
            int size = mCallbacks.size();
            for (int i = 0; i < size; i++) {
                Callback cb = mCallbacks.get(i);
                try {
                    cb.mCallback.onHfpRemote();
                } catch (RemoteException e) {
					e.printStackTrace();
                }
            }
        }
    }

    private void notifyPhoneBook(String type, String name, String number) {
        synchronized (mCallbacks) {
            int size = mCallbacks.size();
            for (int i = 0; i < size; i++) {
                Callback cb = mCallbacks.get(i);
                try {
                    cb.mCallback.onPhoneBook(type, name, number);
                } catch (RemoteException e) {
					e.printStackTrace();
                }
            }
        }
    }
    
    private void notifyPhoneBookDone(int error) {
        synchronized (mCallbacks) {
            int size = mCallbacks.size();
            for (int i = 0; i < size; i++) {
                Callback cb = mCallbacks.get(i);
                try {
                    cb.mCallback.onPhoneBookDone(error);
                } catch (RemoteException e) {
					e.printStackTrace();
                }
            }
        }
    }

    private void notifyCalllog(String type, String name, String number, String date) {
        synchronized (mCallbacks) {
            int size = mCallbacks.size();
            for (int i = 0; i < size; i++) {
                Callback cb = mCallbacks.get(i);
                try {
                    cb.mCallback.onCalllog(type, name, number, date);
                } catch (RemoteException e) {
					e.printStackTrace();
                }
            }
        }
    }

    private void notifyContactsChange() {
        synchronized (mCallbacks) {
            int size = mCallbacks.size();
            for (int i = 0; i < size; i++) {
                Callback cb = mCallbacks.get(i);
                try {
                    cb.mCallback.onContactsChange();
                } catch (RemoteException e) {
					e.printStackTrace();
                }
            }
        }
    }

    private void notifyCalllogChange(String type) {
        synchronized (mCallbacks) {
            int size = mCallbacks.size();
            for (int i = 0; i < size; i++) {
                Callback cb = mCallbacks.get(i);
                try {
                    cb.mCallback.onCalllogChange(type);
                } catch (RemoteException e) {
					e.printStackTrace();
                }
            }
        }
    }

    private void notifyCalllogDone(String type, int error) {
        synchronized (mCallbacks) {
            int size = mCallbacks.size();
            for (int i = 0; i < size; i++) {
                Callback cb = mCallbacks.get(i);
                try {
                    cb.mCallback.onCalllogDone(type, error);
                } catch (RemoteException e) {
					e.printStackTrace();
                }
            }
        }
    }
    
    private void notifyAllDownloadDone(int error) {
        synchronized (mCallbacks) {
            int size = mCallbacks.size();
            for (int i = 0; i < size; i++) {
                Callback cb = mCallbacks.get(i);
                try {
                    cb.mCallback.onAllDownloadDone(error);
                } catch (RemoteException e) {
					e.printStackTrace();
                }
            }
        }
    }
    
    private void notifySignalBattery() {
        synchronized (mCallbacks) {
            int size = mCallbacks.size();
            for (int i = 0; i < size; i++) {
                Callback cb = mCallbacks.get(i);
                try {
                    cb.mCallback.onSignalBattery();
                } catch (RemoteException e) {
					e.printStackTrace();
                }
            }
        }
    }
    
	private void registerCallback(ICallback callback) {
        synchronized (mCallbacks) {
            IBinder binder = callback.asBinder();
            int size = mCallbacks.size();
            for (int i = 0; i < size; i++) {
                Callback test = mCallbacks.get(i);
                if (binder.equals(test.mCallback.asBinder())) {
                    // listener already added
                    return ;
                }
            }

            try {
            	Callback cb = new Callback(callback);
            	binder.linkToDeath(cb, 0);
            	mCallbacks.add(cb);
            } catch (RemoteException e) {
				e.printStackTrace();
			}
        }
    }

	private void unregisterCallback(ICallback callback) {
        synchronized (mCallbacks) {
            IBinder binder = callback.asBinder();
            Callback cb = null;
            int size = mCallbacks.size();
            for (int i = 0; i < size && cb == null; i++) {
                Callback test = mCallbacks.get(i);
                if (binder.equals(test.mCallback.asBinder())) {
                    cb = test;
                }
            }

            if (cb != null) {
                mCallbacks.remove(cb);
                binder.unlinkToDeath(cb, 0);
            }
        }
    }

	private boolean isHfpConnected() {
		synchronized (this) {
			return mHfpConnected;
		}
	}

	// hfp

	private void phoneAnswer() {
		if (mCommandHfp != null && mHfpConnected && CallStatus.PHONE_COMING.equals(mCallState)) {
			try {
				mCommandHfp.reqHfpAnswerCall(NfDef.CALL_ACCEPT_NONE);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	private void phoneReject() {
		if (mCommandHfp != null && mHfpConnected && CallStatus.PHONE_COMING.equals(mCallState)) {
			try {
				mCommandHfp.reqHfpRejectIncomingCall();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	private void phoneFinish() {
		if (mCommandHfp != null && mHfpConnected && !CallStatus.PHONE_CALL_NONE.equals(mCallState)) {
			try {
				mCommandHfp.reqHfpTerminateCurrentCall();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	private void phoneDial(String phonenum) {
		if (mCommandHfp != null && mHfpConnected && CallStatus.PHONE_CALL_NONE.equals(mCallState)) {
			try {
				mCommandHfp.reqHfpDialCall(phonenum);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	private void phoneTransmitDTMFCode(char code) {
		if (mCommandHfp != null && mHfpConnected && CallStatus.PHONE_TALKING.equals(mCallState)) {
			try {
				mCommandHfp.reqHfpSendDtmf(String.format("%c", code));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	private void phoneTransfer() {
		if (mCommandHfp != null && mHfpConnected && CallStatus.PHONE_TALKING.equals(mCallState)) {
			try {
				if(mHfpAudioConnectioned == true) {
					mCommandHfp.reqHfpAudioTransferToPhone();
				} else {
					mCommandHfp.reqHfpAudioTransferToCarkit();
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	private void phoneTransferBack() {
		if (mCommandHfp != null && mHfpConnected) {
			try {
				mCommandHfp.reqHfpAudioTransferToCarkit();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	private void phoneMicOpenClose() {
		if (mCommandHfp != null && mHfpConnected) {
			try {
				mCommandHfp.muteHfpMic(!mCommandHfp.isHfpMicMute());
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	private CallStatus getCallStatus() {
        Log.v(TAG, "getCallStatus begin");
		synchronized (this) {
			if (mHfpConnected) {
				long talkTime = 0;

				if (CallStatus.PHONE_TALKING.equals(mCallState) && startTalkTime != -1)
					talkTime = SystemClock.uptimeMillis() - startTalkTime;

				if (curPhoneNumber != null && !curPhoneNumber.isEmpty() && curPhoneName == null) {
                    Log.v(TAG, "getCallStatus find begin");
					synchronized (mContactList) {
                        int i;
						for (i = 0; i < mContactList.size(); i++) {
							final Contact contact = mContactList.get(i);
							final String number = contact.number.replaceAll("-|\\s+", "");
							if (number.equals(curPhoneNumber)) {
								curPhoneName = contact.name;
								break;
							}
						}
                        if(i == mContactList.size()) {
                            curPhoneName = "";
                        }
                        Log.v(TAG, "getCallStatus find end: " + curPhoneName);
					}
                    
				}
                Log.v(TAG, "getCallStatus exit");
				return new CallStatus(mCallState, curPhoneNumber, curPhoneName, talkTime);
			}
		}
        Log.v(TAG, "getCallStatus null");
		return null;
	}

	private boolean isHfpLocal() {
		synchronized (this) {
			return !mHfpAudioConnectioned;
		}
	}
	
	private String getSignalBattery() {
		synchronized (this) {
			return String.format("%02d%02d", mSignal, mBattery);
		}
	}
	
	private void setAutoAnswer(boolean enable) {
		synchronized (this) {
			mAutoAnswer = enable;
			Settings.setValue(getContentResolver(), SettingsProvider.BLUETOOTH_AUTO_ANSWER, enable?"1":"0");
		}
	}
	
	private boolean isAutoAnswer() {
		synchronized (this) {
			return mAutoAnswer;
		}
	}
	
	private boolean isMicMute() {
		if (mCommandHfp == null) {
			return false;
		}
		boolean ret = false;
		synchronized (this) {
			try {
				ret = mCommandHfp.isHfpMicMute();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ret;
	}


	// CONTACTS

	private boolean phoneBookStartUpdate() {
		if (mCommandPbap != null && mCommandHfp != null && mHfpConnected) {
			synchronized (this) {
				if (mContactUpdateProgress != PBAP_UPDATE_IDLE || mCurrentDownload != PBAP_UPDATE_IDLE) {
					Log.e(TAG, "phoneBookStartUpdate mContactUpdateProgress " + mContactUpdateProgress + " mCurrentDownload " + mCurrentDownload);
					return false;
				}
				mContactUpdateProgress = PBAP_UPDATE_PHONE_CONTACT;
				mCurrentDownload = PBAP_UPDATE_PHONE_CONTACT;
				mIsPhoneBookDone = false;
				mIsCalllogInDone = false;
				mIsCalllogOutDone = false;
				mIsCalllogMissDone = false;
			}

			boolean notifyChanged;
			
			notifyChanged = false;
			synchronized (mContactList) {
				if (mContactList.size() > 0) {
					mContactList.clear();
					notifyChanged = true;
				}
			}
			if (notifyChanged)
				notifyContactsChange();

			notifyChanged = false;
			synchronized (mCallOutList) {
				if (mCallOutList.size() > 0) {
					mCallOutList.clear();
					notifyChanged = true;
				}
			}
			if (notifyChanged)
				notifyCalllogChange(CallLog.TYPE_CALL_OUT);

			notifyChanged = false;
			synchronized (mCallInList) {
				if (mCallInList.size() > 0) {
					mCallInList.clear();
					notifyChanged = true;
				}
			}
			if (notifyChanged)
				notifyCalllogChange(CallLog.TYPE_CALL_IN);

			notifyChanged = false;
			synchronized (mCallMissList) {
				if (mCallMissList.size() > 0) {
					mCallMissList.clear();
					notifyChanged = true;
				}
			}
			if (notifyChanged)
				notifyCalllogChange(CallLog.TYPE_CALL_MISS);

			try {
				mCommandPbap.reqPbapDownload(mCommandHfp.getHfpConnectedAddress(), NfDef.PBAP_STORAGE_PHONE_MEMORY, NfDef.PBAP_PROPERTY_MASK_TEL|NfDef.PBAP_PROPERTY_MASK_FN|NfDef.PBAP_PROPERTY_MASK_N);
			} catch (RemoteException e) {
				e.printStackTrace();
			}

			return true;
		}

		return false;
	}

	private List<Contact> getContactList() {
        final List<Contact> list = new ArrayList<Contact>();

		synchronized (mContactList) {
			list.addAll(mContactList);
	    }

	    return list;
	}

	private boolean callLogStartUpdate(String type) {
		Log.d(TAG, "callLogStartUpdate type " + type);
		synchronized (this) {
			if (mContactUpdateProgress != PBAP_UPDATE_IDLE || mCurrentDownload != PBAP_UPDATE_IDLE) {
				Log.e(TAG, "callLogStartUpdate mContactUpdateProgress " + mContactUpdateProgress + " mCurrentDownload " + mCurrentDownload);
				return false;
			}

			if (mCommandPbap != null && mHfpConnected) {
				if (CallLog.TYPE_CALL_OUT.equals(type)) {
					mIsCalllogOutDone = false;
					boolean notifyChanged = false;
					synchronized (mCallOutList) {
						if (mCallOutList.size() > 0) {
							mCallOutList.clear();
							notifyChanged = true;
						}
					}
					if (notifyChanged)
						notifyCalllogChange(CallLog.TYPE_CALL_OUT);
	
					try {
						mCommandPbap.reqPbapDownload(mCommandHfp.getHfpConnectedAddress(), NfDef.PBAP_STORAGE_DIALED_CALLS, NfDef.PBAP_PROPERTY_MASK_TEL|NfDef.PBAP_PROPERTY_MASK_FN|NfDef.PBAP_PROPERTY_MASK_N);
						mCurrentDownload = PBAP_UPDATE_DIALED_CALLS;
					} catch (RemoteException e) {
						e.printStackTrace();
						return false;
					}
				} else if (CallLog.TYPE_CALL_IN.equals(type)) {
					mIsCalllogInDone = false;
					boolean notifyChanged = false;
					synchronized (mCallInList) {
						if (mCallInList.size() > 0) {
							mCallInList.clear();
							notifyChanged = true;
						}
					}
					if (notifyChanged)
						notifyCalllogChange(CallLog.TYPE_CALL_IN);
	
					try {
						mCommandPbap.reqPbapDownload(mCommandHfp.getHfpConnectedAddress(), NfDef.PBAP_STORAGE_RECEIVED_CALLS, NfDef.PBAP_PROPERTY_MASK_TEL|NfDef.PBAP_PROPERTY_MASK_FN|NfDef.PBAP_PROPERTY_MASK_N);
						mCurrentDownload = PBAP_UPDATE_RECEIVED_CALLS;
					} catch (RemoteException e) {
						e.printStackTrace();
						return false;
					}
				} else if (CallLog.TYPE_CALL_MISS.equals(type)) {
					mIsCalllogMissDone = false;
					boolean notifyChanged = false;
					synchronized (mCallMissList) {
						if (mCallMissList.size() > 0) {
							mCallMissList.clear();
							notifyChanged = true;
						}
					}
					if (notifyChanged)
						notifyCalllogChange(CallLog.TYPE_CALL_MISS);
	
					try {
						mCommandPbap.reqPbapDownload(mCommandHfp.getHfpConnectedAddress(), NfDef.PBAP_STORAGE_MISSED_CALLS, NfDef.PBAP_PROPERTY_MASK_TEL|NfDef.PBAP_PROPERTY_MASK_FN|NfDef.PBAP_PROPERTY_MASK_N);
						mCurrentDownload = PBAP_UPDATE_MISSED_CALLS;
					} catch (RemoteException e) {
						e.printStackTrace();
						return false;
					}
				}
			}
		}
		return true;
	}

	private List<CallLog> getCalllogList(String type) {
        final List<CallLog> list = new ArrayList<CallLog>();

		if (CallLog.TYPE_CALL_OUT.equals(type)) {
			synchronized (mCallOutList) {
				list.addAll(mCallOutList);
		    }
		} else if (CallLog.TYPE_CALL_IN.equals(type)) {
			synchronized (mCallInList) {
				list.addAll(mCallInList);
		    }
		} else if (CallLog.TYPE_CALL_MISS.equals(type)) {
			synchronized (mCallMissList) {
				list.addAll(mCallMissList);
		    }
		}

	    return list;
	}

	private boolean removeCalllog(String token) {
		if (token == null)
			return false;

		synchronized (mCallOutList) {
			for (int i = 0; i < mCallOutList.size(); i++) {
				final CallLog e = mCallOutList.get(i);
				if (token.equals(e.token)) {
					mCallOutList.remove(i);
					return true;
				}
			}
	    }

		synchronized (mCallInList) {
			for (int i = 0; i < mCallInList.size(); i++) {
				final CallLog e = mCallInList.get(i);
				if (token.equals(e.token)) {
					mCallInList.remove(i);
					return true;
				}
			}
	    }

		synchronized (mCallMissList) {
			for (int i = 0; i < mCallMissList.size(); i++) {
				final CallLog e = mCallMissList.get(i);
				if (token.equals(e.token)) {
					mCallMissList.remove(i);
					return true;
				}
			}
	    }

	    return false;
	}
	
	private boolean isPhoneBookDone() {
		synchronized (this) {
			return mIsPhoneBookDone;
		}
	}
	
	private boolean isCalllogDone(String type) {
		synchronized (this) {
			if(CallLog.TYPE_CALL_IN.equals(type)) {
				return mIsCalllogInDone;
			} else if(CallLog.TYPE_CALL_OUT.equals(type)) {
				return mIsCalllogOutDone;
			} else if(CallLog.TYPE_CALL_MISS.equals(type)) {
				return mIsCalllogMissDone;
			}
			return false;
		}
	}

	public static class ServiceImpl extends IService.Stub {
	    final WeakReference<Service> mService;

		private static class AsyncOp {
			private boolean done;

			public synchronized void exec(Handler handler, int what) {
				done = false;

	        	Message m = Message.obtain(handler, what, this);
	        	handler.sendMessage(m);

				while (!done) {
					try {
						wait();
					} catch (InterruptedException e){
						e.printStackTrace();
					}
				}
			}

			public synchronized void complete() {
				done = true;
				notifyAll();
			}
		}

		private static class HfpDial extends AsyncOp {
			public final String phonenum;

			public HfpDial(String phonenum) {
				this.phonenum = phonenum;
			}
		}

		private static class HfpTransmitDTMFCode extends AsyncOp {
			public final char code;

			public HfpTransmitDTMFCode(char code) {
				this.code = code;
			}
		}

		private static class PhoneBookStartUpdateOp extends AsyncOp {
			public boolean result;
		}

		private static class HfpCalllogStartUpdate extends AsyncOp {
			public final String type;
			public boolean result;

			public HfpCalllogStartUpdate(String type) {
				this.type = type;
			}
		}

		private static final int MSG_HFP_ANSWER = 1;
		private static final int MSG_HFP_REJECT = 2;
		private static final int MSG_HFP_FINISH = 3;
		private static final int MSG_HFP_DAIL = 4;
		private static final int MSG_HFP_TRANSMIT_DTMF_CODE = 5;
		private static final int MSG_HFP_TRANSFER = 6;
		private static final int MSG_HFP_TRANSFER_BACK = 7;
		private static final int MSG_HFP_MIC_OPEN_CLOSE = 8;
		private static final int MSG_HFP_BOOK_START_UPDATE = 9;
		private static final int MSG_HFP_CALLLOG_START_UPDATE = 10;

		private final Handler mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				final AsyncOp op = (AsyncOp)msg.obj;

				switch (msg.what) {

				case MSG_HFP_ANSWER:
	        		mService.get().phoneAnswer();
					break;
				case MSG_HFP_REJECT:
	        		mService.get().phoneReject();
					break;
				case MSG_HFP_FINISH:
	        		mService.get().phoneFinish();
					break;
				case MSG_HFP_DAIL:
	        		mService.get().phoneDial(((HfpDial)op).phonenum);
					break;
				case MSG_HFP_TRANSMIT_DTMF_CODE:
	        		mService.get().phoneTransmitDTMFCode(((HfpTransmitDTMFCode)op).code);
					break;
				case MSG_HFP_TRANSFER:
	        		mService.get().phoneTransfer();
					break;
				case MSG_HFP_TRANSFER_BACK:
	        		mService.get().phoneTransferBack();
					break;
				case MSG_HFP_MIC_OPEN_CLOSE:
	        		mService.get().phoneMicOpenClose();
					break;
				case MSG_HFP_BOOK_START_UPDATE:
	        		((PhoneBookStartUpdateOp)op).result = mService.get().phoneBookStartUpdate();
					break;
				case MSG_HFP_CALLLOG_START_UPDATE:
					((HfpCalllogStartUpdate)op).result = mService.get().callLogStartUpdate(((HfpCalllogStartUpdate)op).type);
					break;
				}

	        	op.complete();
			}
		};

		public ServiceImpl(Service service) {
	        mService = new WeakReference<Service>(service);
		}

		@Override
		public void registerCallback(ICallback cb) {
        	mService.get().registerCallback(cb);
		}

		@Override
		public void unregisterCallback(ICallback cb) {
        	mService.get().unregisterCallback(cb);
		}

		@Override
		public boolean isHfpConnected() {
        	return mService.get().isHfpConnected();
		}

		// hfp

		@Override
		public void phoneAnswer() {
			new AsyncOp().exec(mHandler, MSG_HFP_ANSWER);
		}

		@Override
		public void phoneReject() {
			new AsyncOp().exec(mHandler, MSG_HFP_REJECT);
		}

		@Override
		public void phoneFinish() {
			new AsyncOp().exec(mHandler, MSG_HFP_FINISH);
		}

		@Override
		public void phoneDial(String phonenum) {
			new HfpDial(phonenum).exec(mHandler, MSG_HFP_DAIL);
		}

		@Override
		public void phoneTransmitDTMFCode(char code) {
			new HfpTransmitDTMFCode(code).exec(mHandler, MSG_HFP_TRANSMIT_DTMF_CODE);
		}

		@Override
		public void phoneTransfer() {
			new AsyncOp().exec(mHandler, MSG_HFP_TRANSFER);
		}

		@Override
		public void phoneTransferBack() {
			new AsyncOp().exec(mHandler, MSG_HFP_TRANSFER_BACK);
		}

		@Override
		public void phoneMicOpenClose() {
			new AsyncOp().exec(mHandler, MSG_HFP_MIC_OPEN_CLOSE);
		}

		@Override
		public CallStatus getCallStatus() {
			return mService.get().getCallStatus();
		}
		
		@Override
		public boolean isHfpLocal() {
			return mService.get().isHfpLocal();
		}

		// CONTACTS

		@Override
		public boolean phoneBookStartUpdate() {
			final PhoneBookStartUpdateOp op = new PhoneBookStartUpdateOp();
			op.exec(mHandler, MSG_HFP_BOOK_START_UPDATE);
			return op.result;
		}

		@Override
		public List<Contact> getContactList() {
			return mService.get().getContactList();
		}

		@Override
		public boolean callLogStartUpdate(String type) {
			final HfpCalllogStartUpdate op = new HfpCalllogStartUpdate(type);;
			op.exec(mHandler, MSG_HFP_CALLLOG_START_UPDATE);
			return op.result;
		}

		@Override
		public List<CallLog> getCalllogList(String type) {
			return mService.get().getCalllogList(type);
		}

		@Override
		public boolean removeCalllog(String token) {
			return mService.get().removeCalllog(token);
		}

		@Override
		public boolean isPhoneBookDone() throws RemoteException {
			return mService.get().isPhoneBookDone();
		}

		@Override
		public boolean isCalllogDone(String type) throws RemoteException {
			return mService.get().isCalllogDone(type);
		}

		@Override
		public String getSignalBattery() throws RemoteException {
			return mService.get().getSignalBattery();
		}

		@Override
		public void setAutoAnswer(boolean enable) throws RemoteException {
			mService.get().setAutoAnswer(enable);
		}

		@Override
		public boolean isAutoAnswer() throws RemoteException {
			return mService.get().isAutoAnswer();
		}

		@Override
		public boolean isMicMute() throws RemoteException {
			return mService.get().isMicMute();
		}
	}

	private com.hwatong.bt.IService mService = null;

	private final ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			if (DBG) Log.i(TAG, "onServiceConnected");

			mService = com.hwatong.bt.IService.Stub.asInterface(service);

			try {
				String addr = null;
                int state = BtDef.BT_STATE_INVALID;
				if(mCommandHfp != null) {
                    state = mCommandHfp.getHfpConnectionState();
					addr = mCommandHfp.getHfpConnectedAddress();
				}
				mService.updateRemoteDevice(addr, BtDef.BT_PROFILE_HFP, mapState(state), mapState(state));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			if (DBG) Log.i(TAG, "onServiceDisconnected");

			if (mService != null) {
				mService = null;
			}
		}
	};

	private void onHfpConnectChanged(int newstate, int prestate, String addr) {
		Log.d(TAG, "onHfpConnectChanged newstate " + newstate + " mHfpConnected " + mHfpConnected);
        boolean state = (newstate == NfDef.STATE_CONNECTED);
		if (state != mHfpConnected) {
			synchronized (this) {
				mHfpConnected = state;
				if (!mHfpConnected && !CallStatus.PHONE_CALL_NONE.equals(mCallState)) {
                    onHangUp();
				}

				mContactUpdateProgress = PBAP_UPDATE_IDLE;
				mCurrentDownload = PBAP_UPDATE_IDLE;
				mPbapHandler.removeMessages(MSG_PHONE_BOOK_DONE);
				mPbapHandler.removeMessages(MSG_SIM_BOOK_DONE);
				mPbapHandler.removeMessages(MSG_CALLLOG_OUT_DONE);
				mPbapHandler.removeMessages(MSG_CALLLOG_IN_DONE);
				mPbapHandler.removeMessages(MSG_CALLLOG_MISS_DONE);
			}
			
			if (state) {
				try {
					if(mCommandHfp != null) {
						mHfpAudioConnectioned = mCommandHfp.getHfpAudioConnectionState() == NfDef.STATE_CONNECTED;
						List<NfHfpClientCall> list = mCommandHfp.getHfpCallList();
						if(list != null && list.size() > 0) {
					        for(int i=0;i < list.size();i++) {
                                final NfHfpClientCall call = list.get(i);
                                Log.d(TAG, "onHfpConnectChanged has call " + i + " " + call);
                                processCall(call);
                            }
						}
						mBattery = mCommandHfp.getHfpRemoteBatteryIndicator();
						mSignal = mCommandHfp.getHfpRemoteSignalStrength();
                        mCommandHfp.muteHfpMic(false);
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}

				notifyHfpConnected();
			} else {
				boolean notifyChanged;

				notifyChanged = false;
				synchronized (mContactList) {
                    deleteAllContacts();
					if (mContactList.size() > 0) {
						mContactList.clear();
						notifyChanged = true;
					}
				}
				if (notifyChanged)
					notifyContactsChange();

				notifyChanged = false;
				synchronized (mCallOutList) {
					if (mCallOutList.size() > 0) {
						mCallOutList.clear();
						notifyChanged = true;
					}
				}
				if (notifyChanged)
					notifyCalllogChange(CallLog.TYPE_CALL_OUT);

				notifyChanged = false;
				synchronized (mCallInList) {
					if (mCallInList.size() > 0) {
						mCallInList.clear();
						notifyChanged = true;
					}
				}
				if (notifyChanged)
					notifyCalllogChange(CallLog.TYPE_CALL_IN);

				notifyChanged = false;
				synchronized (mCallMissList) {
					if (mCallMissList.size() > 0) {
						mCallMissList.clear();
						notifyChanged = true;
					}
				}
				if (notifyChanged)
					notifyCalllogChange(CallLog.TYPE_CALL_MISS);

				notifyHfpDisconnected();
			}
            mCanHandler.sendMessage(mCanHandler.obtainMessage(MSG_CAN_PAIRED, (state?0x01:0x00)/*BTPairing:paired or not*/, 0));
		}

        if(mService != null && mCommandHfp != null) {
			try {
				mService.updateRemoteDevice(addr, BtDef.BT_PROFILE_HFP, mapState(newstate), mapState(prestate));
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

    static int mapState(int state) {
        int ret = BtDef.BT_STATE_INVALID;
        switch(state) {
            case NfDef.STATE_READY:
                ret = BtDef.BT_STATE_READY;
                break;
            case NfDef.STATE_CONNECTING:
                ret = BtDef.BT_STATE_CONNECTING;
                break;
            case NfDef.STATE_CONNECTED:
                ret = BtDef.BT_STATE_CONNECTED;
                break;
            case NfDef.STATE_DISCONNECTING:
                ret = BtDef.BT_STATE_DISCONNECTING;
                break;
        }
        return ret;
    }

	public void onIncoming(String number) {
		boolean notify = false;
        Log.d(TAG, "onIncoming enter");
		synchronized (this) {
			if (mHfpConnected && !CallStatus.PHONE_COMING.equals(mCallState)) {
				Intent intent = new Intent("com.hwatong.phone.PHONE_STATUS");
				intent.putExtra("status", "ring");
				sendBroadcast(intent);
                synchronized (mCallStateWaitor) {
				    mCallState = CallStatus.PHONE_COMING;
                    mCallStateWaitor.notifyAll();
                }
				curPhoneNumber = number;
                curPhoneName = null;
				notify = true;
				/*
				if(mCommandHfp != null) {
					try {
						mCommandHfp.startHfpRender();
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
				*/
				mHfpHandler.removeCallbacks(mAutoAnswerRunnable);
                if(mAutoAnswer) {
				    mHfpHandler.postDelayed(mAutoAnswerRunnable, 6000);
                }
                mCanHandler.sendMessage(mCanHandler.obtainMessage(MSG_CAN_PHONE, 0x00/*Phone:Incoming*/, 0));
                mCanHandler.sendEmptyMessage(MSG_CAN_CALLINFO);
			}
		}

		if (notify) {
			requestAudioFocus();
			notifyCallStatusChanged();
			Log.d("F70CompleteReceive", "notify setting onIncoming");
			Intent intent = new Intent(RESET_EFFECT);
			intent.putExtra("status", 1); //taking start:1 idle:0
			sendBroadcast(intent);
			//startPhoneCallActivity();
		}
        /*
        if(mCommandHfp != null) {
            try {
                mInBandRing = mCommandHfp.isHfpInBandRingtoneSupport();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "mInBandRing " + mInBandRing);
        */
    	if(/*!mInBandRing*/true) {
            mAudioHandler.removeMessages(RING_STOP);
        	mAudioHandler.sendEmptyMessage(RING_START);
    	}
        Log.d(TAG, "onIncoming exit");
	}

	public void onOutGoingOrTalkingNumber(String number) {
		boolean notify = false;
        Log.d(TAG, "onOutGoingOrTalkingNumber enter");
		synchronized (this) {
            setVoiceMuted(false);
			if (mHfpConnected && !CallStatus.PHONE_CALLING.equals(mCallState)) {
                synchronized (mCallStateWaitor) {
				    mCallState = CallStatus.PHONE_CALLING;
                    mCallStateWaitor.notifyAll();
                }
				Intent intent = new Intent("com.hwatong.phone.PHONE_STATUS");
				intent.putExtra("status", "talk");
				sendBroadcast(intent);
                String name = "";
				if (number != null && !number.isEmpty()) {
                    Log.d(TAG, "onOutGoingOrTalkingNumber find begin");
					synchronized (mContactList) {
						for (int i = 0; i < mContactList.size(); i++) {
							final Contact contact = mContactList.get(i);
							final String number2 = contact.number;;
							if (number2.equals(number)) {
								name = contact.name;
								break;
							}
						}
					}
                    Log.d(TAG, "onOutGoingOrTalkingNumber find end: " + name);
                    /*do not record
					synchronized (mCallOutList) {
						if (mCallOutList.size() < 10000) {
							SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
							Date date = new Date();
							String time = sdf.format(date);
							CallLog callLog = new CallLog(CallLog.TYPE_CALL_OUT, name, number, time);
							mCallOutList.add(0, callLog);
						}
					}
					notifyCalllogChange(CallLog.TYPE_CALL_OUT);
                    */
				}

				curPhoneNumber = number;
                curPhoneName = name;

				notify = true;
				/*
				if(mCommandHfp != null) {
					try {
						mCommandHfp.startHfpRender();
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
				*/
                mCanHandler.sendMessage(mCanHandler.obtainMessage(MSG_CAN_PHONE, 0x01 /*Phone:Outgoing*/, 0));
                mCanHandler.sendEmptyMessage(MSG_CAN_CALLINFO);
			}
		}

		if (notify) {
			requestAudioFocus();
			notifyCallStatusChanged();
			Log.d("F70CompleteReceive", "notify setting onOutGoingOrTalkingNumber");
			Intent intent = new Intent(RESET_EFFECT);
			intent.putExtra("status", 1); //taking start:1 idle:0
			sendBroadcast(intent);
			//startPhoneCallActivity();
		}
        mAudioHandler.removeMessages(RING_START);
        mAudioHandler.sendEmptyMessage(RING_STOP);
        Log.d(TAG, "onOutGoingOrTalkingNumber exit");
	}

	public void onTalking(String number) {
		boolean notify = false;
        Log.d(TAG, "onTalking enter");
		synchronized (this) {
            setVoiceMuted(false);
			mHfpHandler.removeCallbacks(mAutoAnswerRunnable);
			if (mHfpConnected /* && (CallStatus.PHONE_CALLING.equals(mCallState) || CallStatus.PHONE_COMING.equals(mCallState) || CallStatus.PHONE_CALL_NONE.equals(mCallState)) */) {
                if (CallStatus.PHONE_CALL_NONE.equals(mCallState)) {
                    //sometimes no CALL_STATE_ALERTING msg
                    Log.d(TAG, "no CALL_STATE_ALERTING received " + number);
                    curPhoneNumber = number;
                    curPhoneName = null;
                    mCanHandler.sendEmptyMessage(MSG_CAN_CALLINFO);
				}
				if (!CallStatus.PHONE_CALLING.equals(mCallState)) {
					Intent intent = new Intent("com.hwatong.phone.PHONE_STATUS");
					intent.putExtra("status", "talk");
					sendBroadcast(intent);
				} 
				//if (CallStatus.PHONE_COMING.equals(mCallState)) {
					curPhoneNumber = number;
					curPhoneName = null;
					if (curPhoneNumber != null && !curPhoneNumber.isEmpty()) {
						String name = "";
                        if(curPhoneName == null) {
                            Log.d(TAG, "onTalking find begin");
    						synchronized (mContactList) {
    							for (int i = 0; i < mContactList.size(); i++) {
    								final Contact contact = mContactList.get(i);
    								final String number2 = contact.number;
    								if (number2.equals(curPhoneNumber)) {
    									name = contact.name;
    									break;
    								}
    							}
                                curPhoneName = name;
                                Log.d(TAG, "onTalking find end: " + name);
                            }
						} else {
                            name = curPhoneName;
                        }
                        /*
						synchronized (mCallInList) {
							if (mCallInList.size() < 10000) {
								SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
								Date date = new Date();
								String time = sdf.format(date);
								CallLog callLog = new CallLog(CallLog.TYPE_CALL_IN, name, curPhoneNumber, time);
								mCallInList.add(0, callLog);
							}
						}

						notifyCalllogChange(CallLog.TYPE_CALL_IN);
                        */
					}
				//}

                synchronized (mCallStateWaitor) {
				    mCallState = CallStatus.PHONE_TALKING;
                    mCallStateWaitor.notifyAll();
                }
				startTalkTime = SystemClock.uptimeMillis();

				notify = true;
                mCanHandler.sendMessage(mCanHandler.obtainMessage(MSG_CAN_TEL, 0x04 /*TEL:Connect*/, 0));
			}
		}

		if (notify) {
			requestAudioFocus();

			notifyCallStatusChanged();

			//startPhoneCallActivity();
		}
        mAudioHandler.removeMessages(RING_START);
        mAudioHandler.sendEmptyMessage(RING_STOP);
        Log.d(TAG, "onTalking exit");
	}

	public void onHangUp() {
		boolean notify = false;
        Log.d(TAG, "onHangUp enter");
        if(mCommandHfp != null && mHfpConnected) {
            try {
                boolean hascall = false;
                List<NfHfpClientCall> list = mCommandHfp.getHfpCallList();
    		    if(list != null && list.size() > 0) {
                    for(int i=0;i < list.size();i++) {
                        final NfHfpClientCall call = list.get(i);
                        Log.d(TAG, "onHangUp has call " + i + " " + call);
                        if(call.getState() != NfHfpClientCall.CALL_STATE_TERMINATED) {
                            hascall = true;
                        }
                    }
                    if(hascall) {
                        return;
                    }
    		    }
            } catch(RemoteException e) {
            }
        }
        if(mCommandHfp != null) {
            try {
                mCommandHfp.muteHfpMic(false);
            } catch(RemoteException e) {
            }
        }
		synchronized (this) {
			if (!CallStatus.PHONE_CALL_NONE.equals(mCallState)) {
                /*do not add new record
				if (CallStatus.PHONE_COMING.equals(mCallState)) {
					if (curPhoneNumber != null && !curPhoneNumber.isEmpty()) {
						String name = "";
                        if(curPhoneName == null) {
                            Log.d(TAG, "onHangUp find begin");
    						synchronized (mContactList) {
    							for (int i = 0; i < mContactList.size(); i++) {
    								final Contact contact = mContactList.get(i);
    								final String number = contact.number;
    								if (number.equals(curPhoneNumber)) {
    									name = contact.name;
    									break;
    								}
    							}
                                curPhoneName = name;
                                Log.d(TAG, "onHangUp find end: " + name);
    						}
                        } else {
                            name = curPhoneName;
                        }

						synchronized (mCallMissList) {
							if (mCallMissList.size() < 10000) {
								SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
								Date date = new Date();
								String time = sdf.format(date);
								CallLog callLog = new CallLog(CallLog.TYPE_CALL_MISS, name, curPhoneNumber, time);
								mCallMissList.add(0, callLog);
							}
						}

						notifyCalllogChange(CallLog.TYPE_CALL_MISS);
					}
				}
                */

                synchronized (mCallStateWaitor) {
				    mCallState = CallStatus.PHONE_CALL_NONE;
                    mCallStateWaitor.notifyAll();
                }
				curPhoneNumber = null;
                curPhoneName = null;
				startTalkTime = -1;

				notify = true;
                mCanHandler.sendMessage(mCanHandler.obtainMessage(MSG_CAN_TEL, 0x05 /*TEL:End*/, 0));
			}
		}

		if (notify) {
			notifyCallStatusChanged();
			abandonAudioFocus();
			Intent intent = new Intent("com.hwatong.phone.PHONE_STATUS");
			intent.putExtra("status", "release");
			sendBroadcast(intent);
			Log.d("F70CompleteReceive", "notify setting onHangUp");
			Intent intent2 = new Intent(RESET_EFFECT);
			intent2.putExtra("status", 0); //taking start:1 idle:0
			sendBroadcast(intent2);
		}
        mAudioHandler.removeMessages(RING_START);
        mAudioHandler.sendEmptyMessage(RING_STOP);

        setVoiceMuted(true);
        Log.d(TAG, "onHangUp exit");
	}

	public void onHfpLocal() {
		synchronized (this) {
			mHfpAudioConnectioned = false;
		}

		notifyHfpLocal();
	}

	public void onHfpRemote() {
		synchronized (this) {
			mHfpAudioConnectioned = true;
		}

		notifyHfpRemote();
	}

	public void onPhoneBook(String name, String number) {
		if (DBG) Log.v(TAG,"onPhoneBook name " + name + " number " + number);
		if (number.isEmpty())
			return;
		synchronized (this) {
			if (mCurrentDownload != PBAP_UPDATE_PHONE_CONTACT) {
				Log.e(TAG,"onPhoneBook err mCurrentDownload " + mCurrentDownload);
				return;
			}
		}

		synchronized (mContactList) {
			if (mContactList.size() < 10000) {
				mContactList.add(new Contact(Contact.TYPE_PHONE, name, number, Utils.getComFlg(name)));
			}
		}

		notifyContactsChange();

		notifyPhoneBook(Contact.TYPE_PHONE, name, number);
        try {
            synchronized (mCallStateWaitor) {
                if(!CallStatus.PHONE_CALL_NONE.equals(mCallState)) {
                    Log.v(TAG,"onPhoneBook name " + name + " number " + number + " delay");
                    mCallStateWaitor.wait(100);
                }
            }
        } catch(Exception e) {
        }
	}
	
	public void onSimBook(String name, String number) {
		if (DBG) Log.v(TAG,"onSimBook name " + name + " number " + number);
		if (number.isEmpty())
			return;
		synchronized (this) {
			if (mCurrentDownload != PBAP_UPDATE_SIM_CONTACT) {
				Log.e(TAG,"onSimBook err mCurrentDownload " + mCurrentDownload);
				return;
			}
		}

		synchronized (mContactList) {
			if (mContactList.size() < 10000) {
				mContactList.add(new Contact(Contact.TYPE_SIM, name, number, Utils.getComFlg(name)));
			}
		}

		notifyContactsChange();

		notifyPhoneBook(Contact.TYPE_SIM, name, number);
        try {
            synchronized (mCallStateWaitor) {
                if(!CallStatus.PHONE_CALL_NONE.equals(mCallState)) {
                    Log.v(TAG,"onSimBook name " + name + " number " + number + " delay");
                    mCallStateWaitor.wait(100);
                }
            }
        } catch(Exception e) {
        }
	}

	private void onPhoneBookDone() {
		synchronized (this) {
			if(mContactUpdateProgress == PBAP_UPDATE_PHONE_CONTACT) {
				if (mCommandPbap != null) {
					try {
						mCommandPbap.reqPbapDownload(mCommandHfp.getHfpConnectedAddress(), NfDef.PBAP_STORAGE_SIM, NfDef.PBAP_PROPERTY_MASK_TEL|NfDef.PBAP_PROPERTY_MASK_FN|NfDef.PBAP_PROPERTY_MASK_N);
						mContactUpdateProgress = PBAP_UPDATE_SIM_CONTACT;
						mCurrentDownload = PBAP_UPDATE_SIM_CONTACT;
					} catch (RemoteException e) {
						e.printStackTrace();
						mContactUpdateProgress = PBAP_UPDATE_IDLE;
						notifyPhoneBookDone(BtPhoneDef.PBAP_DOWNLOAD_FAILED);
					}
				}
			}
		}
	}

	private void onSimDone() {
		synchronized (this) {
			if(mContactUpdateProgress == PBAP_UPDATE_SIM_CONTACT) {
				mIsPhoneBookDone = true;
				if (mCommandPbap != null) {
					try {
						mCommandPbap.reqPbapDownload(mCommandHfp.getHfpConnectedAddress(), NfDef.PBAP_STORAGE_DIALED_CALLS, NfDef.PBAP_PROPERTY_MASK_TEL|NfDef.PBAP_PROPERTY_MASK_FN|NfDef.PBAP_PROPERTY_MASK_N);
						mContactUpdateProgress = PBAP_UPDATE_DIALED_CALLS;
						mCurrentDownload = PBAP_UPDATE_DIALED_CALLS;
					} catch (RemoteException e) {
						e.printStackTrace();
						mContactUpdateProgress = PBAP_UPDATE_IDLE;
						notifyPhoneBookDone(BtPhoneDef.PBAP_DOWNLOAD_FAILED);
					}
				}
			}
            syncContactsToProvider();
			notifyPhoneBookDone(BtPhoneDef.PBAP_DOWNLOAD_SUCCESS);
		}
	}
	
	private void onCalllogOutDone() {
		synchronized (this) {
			mIsCalllogOutDone = true;
			if(mContactUpdateProgress == PBAP_UPDATE_DIALED_CALLS) {
				if (mCommandPbap != null) {
					try {
						mCommandPbap.reqPbapDownload(mCommandHfp.getHfpConnectedAddress(), NfDef.PBAP_STORAGE_RECEIVED_CALLS, NfDef.PBAP_PROPERTY_MASK_TEL|NfDef.PBAP_PROPERTY_MASK_FN|NfDef.PBAP_PROPERTY_MASK_N);
						mContactUpdateProgress = PBAP_UPDATE_RECEIVED_CALLS;
						mCurrentDownload = PBAP_UPDATE_RECEIVED_CALLS;
					} catch (RemoteException e) {
						e.printStackTrace();
						mContactUpdateProgress = PBAP_UPDATE_IDLE;
						notifyCalllogDone(CallLog.TYPE_CALL_IN, BtPhoneDef.PBAP_DOWNLOAD_FAILED);
					}
				}
			}
			notifyCalllogDone(CallLog.TYPE_CALL_OUT, BtPhoneDef.PBAP_DOWNLOAD_SUCCESS);
		}
	}

	private void onCalllogInDone() {
		synchronized (this) {
			mIsCalllogInDone = true;
			if(mContactUpdateProgress == PBAP_UPDATE_RECEIVED_CALLS) {
				if (mCommandPbap != null) {
					try {
						mCommandPbap.reqPbapDownload(mCommandHfp.getHfpConnectedAddress(), NfDef.PBAP_STORAGE_MISSED_CALLS, NfDef.PBAP_PROPERTY_MASK_TEL|NfDef.PBAP_PROPERTY_MASK_FN|NfDef.PBAP_PROPERTY_MASK_N);
						mContactUpdateProgress = PBAP_UPDATE_MISSED_CALLS;
						mCurrentDownload = PBAP_UPDATE_MISSED_CALLS;
					} catch (RemoteException e) {
						e.printStackTrace();
						mContactUpdateProgress = PBAP_UPDATE_IDLE;
						notifyCalllogDone(CallLog.TYPE_CALL_MISS, BtPhoneDef.PBAP_DOWNLOAD_FAILED);
					}
				}
			}
			notifyCalllogDone(CallLog.TYPE_CALL_IN, BtPhoneDef.PBAP_DOWNLOAD_SUCCESS);
		}
	}

	private void onCalllogMissDone() {
		synchronized (this) {
			mIsCalllogMissDone = true;
			if(mContactUpdateProgress == PBAP_UPDATE_MISSED_CALLS) {
				mContactUpdateProgress = PBAP_UPDATE_IDLE;
				notifyAllDownloadDone(BtPhoneDef.PBAP_DOWNLOAD_SUCCESS);
			}
			notifyCalllogDone(CallLog.TYPE_CALL_MISS, BtPhoneDef.PBAP_DOWNLOAD_SUCCESS);
		}
	}
	
    private void onCalllog(String type, String name, String number, String date) {
		if (CallLog.TYPE_CALL_OUT.equals(type)) {
			synchronized (this) {
				if (mCurrentDownload != PBAP_UPDATE_DIALED_CALLS) {
					Log.e(TAG, "onCalllog err mCurrentDownload " + mCurrentDownload + " type " + type);
					return;
				}
			}
			synchronized (mCallOutList) {
				if (mCallOutList.size() < 10000) {
					CallLog callLog = new CallLog(type, name, number, date);
					mCallOutList.add(callLog);
				}
			}
		} else if (CallLog.TYPE_CALL_IN.equals(type)) {
			synchronized (this) {
				if (mCurrentDownload != PBAP_UPDATE_RECEIVED_CALLS) {
					Log.e(TAG, "onCalllog err mCurrentDownload " + mCurrentDownload + " type " + type);
					return;
				}
			}
			synchronized (mCallInList) {
				if (mCallInList.size() < 10000) {
					CallLog callLog = new CallLog(type, name, number, date);
					mCallInList.add(callLog);
				}
			}
		} else if (CallLog.TYPE_CALL_MISS.equals(type)) {
			synchronized (this) {
				if (mCurrentDownload != PBAP_UPDATE_MISSED_CALLS) {
					Log.e(TAG, "onCalllog err mCurrentDownload " + mCurrentDownload + " type " + type);
					return;
				}
			}
			synchronized (mCallMissList) {
				if (mCallMissList.size() < 10000) {
					CallLog callLog = new CallLog(type, name, number, date);
					mCallMissList.add(callLog);
				}
			}
		}

		notifyCalllogChange(type);

		notifyCalllog(type, name, number, date);
        try {
            synchronized (mCallStateWaitor) {
                if(!CallStatus.PHONE_CALL_NONE.equals(mCallState)) {
                    Log.v(TAG,"onCalllog name " + name + " number " + number + " delay");
                    mCallStateWaitor.wait(100);
                }
            }
        } catch(Exception e) {
        }
    }

	private static final int MSG_PHONE_BOOK_DONE = 1;
	private static final int MSG_SIM_BOOK_DONE = 2;
	private static final int MSG_CALLLOG_OUT_DONE = 3;
	private static final int MSG_CALLLOG_IN_DONE = 4;
	private static final int MSG_CALLLOG_MISS_DONE = 5;
	
	private final Handler mPbapHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == MSG_PHONE_BOOK_DONE) {
				onPhoneBookDone();

			} else if (msg.what == MSG_SIM_BOOK_DONE) {
				onSimDone();

			} else if (msg.what == MSG_CALLLOG_OUT_DONE) {
				onCalllogOutDone();

			} else if (msg.what == MSG_CALLLOG_IN_DONE) {
				onCalllogInDone();

			} else if (msg.what == MSG_CALLLOG_MISS_DONE) {
				onCalllogMissDone();

			}

		}
	};

	private final BroadcastReceiver mTelButtonListener = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			synchronized (Service.this) {
				Log.d(TAG, "onReceive: " + action + ", mCallState " + mCallState);

				if (mHfpConnected) {
					if (CallStatus.PHONE_COMING.equals(mCallState)) {
						if ("com.hwatong.intent.action.TALK_BUTTON".equals(action)) {
							try {
								mCommandHfp.reqHfpAnswerCall(NfDef.CALL_ACCEPT_NONE);
							} catch (RemoteException e) {
								e.printStackTrace();
							}
						}
					} else if (CallStatus.PHONE_CALLING.equals(mCallState) || CallStatus.PHONE_TALKING.equals(mCallState)) {
						if ("com.hwatong.intent.action.TALK_BUTTON".equals(action)) {
							try {
								mCommandHfp.reqHfpTerminateCurrentCall();
							} catch (RemoteException e) {
								e.printStackTrace();
							}
						}
					} else {
						if ("com.hwatong.intent.action.TALK_BUTTON".equals(action)) {
							final Intent i = new Intent("com.hwatong.phone.BTPHONE_UI");
							i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					        try {
								startActivity(i);
					        } catch (ActivityNotFoundException e) {
								e.printStackTrace();
					        }
						}
					}
				} else {
					if ("com.hwatong.intent.action.TALK_BUTTON".equals(action)) {
						final Intent i = new Intent("com.hwatong.phone.BTPHONE_UI");
						i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						try {
							startActivity(i);
						} catch (ActivityNotFoundException e) {
							e.printStackTrace();
						}
					}
				}

			}
		}
	};

	private static final int RING_START = 1;
	private static final int RING_STOP = 2;
	private static final int MANAGER_AUDIO_FOCUS = 3;
	private static final int ABANDON_AUDIO_FOCUS = 4;
	private final Handler mAudioHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			if (msg.what == RING_START) {
				if (!mRinging) {
					mRinging = true;
					notifyRingStart();
					ringStart();
				}
			} else if (msg.what == RING_STOP) {
				mRinging = false;
				ringStop();
				notifyRingStop();
			} else if (msg.what == MANAGER_AUDIO_FOCUS) {
				if (mHfpConnected && !CallStatus.PHONE_CALL_NONE.equals(mCallState)) {
					requestAudioFocus();
				} else {
					abandonAudioFocus();
				}
			} else if (msg.what == ABANDON_AUDIO_FOCUS) {
				abandonAudioFocusDelayed();
			}
		}
	};

	private boolean isPhoneAudio;

	public void requestAudioFocus() {
		mAudioHandler.removeMessages(ABANDON_AUDIO_FOCUS);
		if(!isPhoneAudio) {
			isPhoneAudio = true;
			mAudioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL,
					AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
	
			mAudioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
			mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
		}
	}

	public void abandonAudioFocus() {
		mAudioHandler.sendEmptyMessageDelayed(ABANDON_AUDIO_FOCUS, 1800);
	}

	private void abandonAudioFocusDelayed() {
		if (isPhoneAudio) {
			isPhoneAudio = false;
			mAudioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
			mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
			mAudioManager.abandonAudioFocus(null);
		}
	}

	private boolean mRinging = false;

	private void ringStart() {
		String path = "/system/ring.mp3";
		if (path == null || !new File(path).exists()) {
			Log.e(TAG, "cannot find ring file");
			return;
		}
		
		try {
			ringPlayer.reset();
			ringPlayer.setDataSource(path);
			ringPlayer.setAudioStreamType(AudioManager.STREAM_RING);
			ringPlayer.prepare();
			ringPlayer.start();
			ringPlayer.setOnCompletionListener(this);
			Log.d(TAG, "playing ring " + path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void ringStop() {
		try {
			ringPlayer.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if (mRinging)
			ringStart();
	}

	private void startPhoneCallActivity() {
		/*if (!isECActive())*/ {
			Intent intent = new Intent("com.hwatong.phone.BTPHONE_UI");
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			try {
				startActivity(intent);
			} catch (ActivityNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	private final BroadcastReceiver mShutdownIntentReceiver = new BroadcastReceiver() {
		@Override
        public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (Intent.ACTION_SHUTDOWN.equals(action)) {
				if (mHfpConnected && !CallStatus.PHONE_CALL_NONE.equals(mCallState)) {
					if (mHfpAudioConnectioned) {
						try {
							mCommandHfp.reqHfpAudioTransferToPhone();
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
    };
    
    private static final int MSG_HFP_CONNECT_RECEIVED = 1;
    private static final int MSG_HFP_INCOMING = 2;
    private static final int MSG_HFP_OUTGOING = 3;
    private static final int MSG_HFP_TALKING = 4;
    private static final int MSG_HFP_HANGUP = 5;

	private final Handler mHfpHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			
			if (msg.what == MSG_HFP_CONNECT_RECEIVED) {
				String addr = (String)msg.obj;
				onHfpConnectChanged(msg.arg1, msg.arg2, addr);
			} else if(msg.what == MSG_HFP_INCOMING) {
				String num = (String)msg.obj;
				onIncoming(num);
			} else if(msg.what == MSG_HFP_OUTGOING) {
				String num = (String)msg.obj;
				onOutGoingOrTalkingNumber(num);
			} else if(msg.what == MSG_HFP_TALKING) {
				String num = (String)msg.obj;
				onTalking(num);
			} else if(msg.what == MSG_HFP_HANGUP) {
				onHangUp();
			}
		}
	};
	
	void processCall(NfHfpClientCall call) {
        if(call != null) {
        	int callstate = call.getState();
        	Log.d(TAG, "callstate " + callstate + " mCallState " + mCallState);
        	if(callstate == NfHfpClientCall.CALL_STATE_INCOMING) {
        		mHfpHandler.sendMessage(mHfpHandler.obtainMessage(MSG_HFP_INCOMING, call.getNumber()));
        	} else if(callstate == NfHfpClientCall.CALL_STATE_TERMINATED) {
        		mHfpHandler.sendEmptyMessage(MSG_HFP_HANGUP);
        	} else if(callstate == NfHfpClientCall.CALL_STATE_ACTIVE) {
        		mHfpHandler.sendMessage(mHfpHandler.obtainMessage(MSG_HFP_TALKING, call.getNumber()));
        	} else if(callstate == NfHfpClientCall.CALL_STATE_DIALING || callstate == NfHfpClientCall.CALL_STATE_ALERTING) {
        		mHfpHandler.sendMessage(mHfpHandler.obtainMessage(MSG_HFP_OUTGOING, call.getNumber()));
        	}
        	mAudioHandler.sendEmptyMessage(MANAGER_AUDIO_FOCUS);
        }
	}
    private static class Settings {
        private static final Uri CONTENT_URI = Uri.parse("content://car_settings/content");

        private static String getString(ContentResolver cr, String name) {
            String[] select = new String[] { "value" };
            Cursor cursor = cr.query(CONTENT_URI, select, "name=?", new String[]{ name }, null);
            if (cursor == null)
                return null;
            String value = null;
            if (cursor.moveToFirst()) {
                value = cursor.getString(0);
            }
            cursor.close();
            return value;
        }

        public static int getInt(ContentResolver cr, String name, int def) {
            String v = getString(cr, name);
            try {
                return v != null ? Integer.parseInt(v) : def;
            } catch (NumberFormatException e) {
                return def;
            }
        }

        public static Uri getUriFor(String name) {
            return Uri.withAppendedPath(CONTENT_URI, name);
        }

        public static boolean setValue(ContentResolver cr, String name, String value) {
            try {
                String[] select = new String[] { "value" };
                Cursor cursor = cr.query(CONTENT_URI, select, "name=?",
                        new String[] { name }, null);
                if (cursor != null && cursor.moveToFirst()) {
                    if (cursor != null)
                        cursor.close();
                    ContentValues values = new ContentValues();
                    values.put("value", value);
                    cr.update(CONTENT_URI, values, "name=?", new String[] { name });
                } else {
                    if (cursor != null)
                        cursor.close();
                    ContentValues values = new ContentValues();
                    values.put("name", name);
                    values.put("value", value);
                    cr.insert(CONTENT_URI, values);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }        
    }

    private static ICanbusService getCanbusService() {
        if (sService != null) {
            return sService;
        }
        IBinder b = ServiceManager.getService("canbus");
        sService = ICanbusService.Stub.asInterface(b);
        return sService;
    }

    void reqHfpAnswerCall() {
		if (mCommandHfp != null) {
			try {
				mCommandHfp.reqHfpAnswerCall(NfDef.CALL_ACCEPT_NONE);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
        mCanHandler.sendMessage(mCanHandler.obtainMessage(MSG_CAN_TEL, 0x01 /*TEL:Accept*/, 0));
    }

    void reqHfpRejectIncomingCall() {
		if (mCommandHfp != null) {
			try {
				mCommandHfp.reqHfpRejectIncomingCall();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
        mCanHandler.sendMessage(mCanHandler.obtainMessage(MSG_CAN_TEL, 0x02 /*TEL:Decline*/, 0));
    }

    void reqHfpTerminateCurrentCall() {
		if (mCommandHfp != null) {
			try {
				mCommandHfp.reqHfpTerminateCurrentCall();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
    }

    void reqHfpDialCall(String number) {
		if (mCommandHfp != null) {
			try {
				mCommandHfp.reqHfpDialCall(number);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
    }

    static String split(String orignal, int count) {
        
        StringBuilder sb = new StringBuilder(count);
        if(orignal != null) {
            char[] b = orignal.toCharArray();
            for (int i = 0; i < b.length; i++) {
                if (count <= 0) {
                    break;
                }
        
                String temp = String.valueOf(b[i]);
        
                // Chinese character
                if (temp.getBytes().length > 1) {
                    count -= 2;
                    if (count < 0) {
                        break;
                    }
                } else {
                    count--;
                }
        
                sb.append(temp);
            }
        }
    
        return sb.toString();
    }

    void syncIPCCallInfo() {
        String number = null;
        String name = "";
        Log.d(TAG, "syncIPCCallInfo begin");
        synchronized (this) {
            number = curPhoneNumber;
            if(number == null || number.isEmpty()) {
                return;
            }
    
            if(number != null) {
                number = split(number, 16);
            }
            
            if(curPhoneName == null) {
                Log.d(TAG, "syncIPCCallInfo find begin");
        		synchronized (mContactList) {
        			for (int i = 0; i < mContactList.size(); i++) {
        				final Contact contact = mContactList.get(i);
        				final String number1 = contact.number.replaceAll("-|\\s+", "");
        				if (number1.equals(number)) {
        					name = contact.name;
        					break;
        				}
        			}
                    curPhoneName = name;
                    Log.d(TAG, "syncIPCCallInfo find end: " + name);
        		}
            } else {
                name = curPhoneName;
            }
        }
        
        int name_len = 0;
        int number_len = 0;
        byte name_buf[] = null;
        byte number_buf[] = null;
        if(name != null && !name.isEmpty()) {
            name = split(name, 16);
            try {
                name_buf = name.getBytes("GB2312");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            try {
                number_buf = number.getBytes("GB2312");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        if((name_buf == null || name_buf.length == 0) && (number_buf == null || number_buf.length == 0)) {
            return;
        }
        ICanbusService canbus = getCanbusService();
        if(canbus == null) {
            return;
        }
        if(name_buf != null) {
            name_len = name_buf.length;
        }
        if(number_buf != null) {
            number_len = number_buf.length;
        }
        byte data[] = new byte[1+name_len+number_len];
        if(name_len > 0) {
            data[0] = (byte)name_len;
            System.arraycopy(name_buf, 0, data, 1, name_len);
        } else if(number_len > 0){
            data[0] = (byte)number_len;
            System.arraycopy(number_buf, 0, data, 1, number_len);
        }

        int total = data.length/6 + ((data.length%6 > 0)?1:0);
        if(total > 0) {
            for(int j = 0;j < total;j++) {
                int start = 6*j;
                int end = 6*(j+1);
                int len = data.length;
                end = (len < end)?len:end;
                byte once[] = new byte[end-start];
                System.arraycopy(data, start, once, 0, once.length);
                Log.d(TAG, "start " + start + " end " + end + " once " + once.length);
                try {
                    canbus.writeCallInfo(total /*Total_Messages*/, 
                                                 j /*MessageNumber*/, 
                                                 0x0E /*CallLogNum*/, 
                                                 0x03 /*CallLog*/, 
                                                 0x00 /*PhoneAuth*/, 
                                                 once /*Characters*/
                                                 );
                } catch (RemoteException e) {
			        e.printStackTrace();
		        }
                try {
                    Thread.sleep(100);
                } catch(Exception e) {
                }
            }
        }
    }
    
    //modify LJW delay after accoff tranfer bt to carkit
    private static final int BT_VOICE_ACCOFF_CHANGED_DELAY = 0x88;
    private static final int BT_VOICE_ACCOFF_CHANGED_DELAY_TIME = 1000;
    private Handler btVoiceDelayHandler  = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			try {
				if (!mHfpAudioConnectioned) {
					if (mSavedHfpAudioConnectioned) {
						mCommandHfp.reqHfpAudioTransferToCarkit();
					}
				}
				mSavedHfpAudioConnectioned = false;
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
    	
    };

	private static final int MSG_MANAGER_VOICE = 1;
    boolean mSavedHfpAudioConnectioned;
    CarStatus mCarStatus;

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case MSG_MANAGER_VOICE:
                if(msg.obj != null) {
                    mCarStatus = (CarStatus)msg.obj;
                }
                try {
                    if(mCarStatus != null && mCommandHfp != null && mHfpConnected) {
            		    int acc = mCarStatus.getStatus1();
                        if(CallStatus.PHONE_TALKING.equals(mCallState)) {
                            if(acc == 0) {
                                if(mHfpAudioConnectioned) {
                                    mSavedHfpAudioConnectioned = true;
                                    mCommandHfp.reqHfpAudioTransferToPhone();
                                }
                            } else if(acc > 0) {
//                                if(!mHfpAudioConnectioned) {
//                                    if(mSavedHfpAudioConnectioned) {
//                                        mCommandHfp.reqHfpAudioTransferToCarkit();
//                                    }
//                                }
//                                mSavedHfpAudioConnectioned = false;
                            	//ljw fast change accoff -> accon
                            	btVoiceDelayHandler.removeMessages(BT_VOICE_ACCOFF_CHANGED_DELAY);
                            	btVoiceDelayHandler.sendEmptyMessageDelayed(BT_VOICE_ACCOFF_CHANGED_DELAY, BT_VOICE_ACCOFF_CHANGED_DELAY_TIME);
                            	
                                mCanHandler.sendMessage(mCanHandler.obtainMessage(MSG_CAN_TEL, 0x04 /*TEL:Connect*/, 0x04));
                                mCanHandler.sendEmptyMessage(MSG_CAN_CALLINFO);
                            }
                        } else if(CallStatus.PHONE_COMING.equals(mCallState)) {
                            if(acc > 0) {
                                mCanHandler.sendMessage(mCanHandler.obtainMessage(MSG_CAN_PHONE, 0x00/*Phone:Incoming*/, 0x04));
                            }
                            mCanHandler.sendEmptyMessage(MSG_CAN_CALLINFO);
                        } else if(CallStatus.PHONE_CALLING.equals(mCallState)) {
                            if(acc > 0) {
                                mCanHandler.sendMessage(mCanHandler.obtainMessage(MSG_CAN_PHONE, 0x01 /*Phone:Outgoing*/, 0x04));
                                mCanHandler.sendEmptyMessage(MSG_CAN_CALLINFO);
                            }
                        }
                    }
                } catch(Exception e) {
                }
				break;

			}
		}
	};
    void deleteAllContacts() {
        //final ContentResolver resolver = getContentResolver();
        //resolver.delete(ContactsContract.RawContacts.CONTENT_URI, null, null);
        mPhoneBookHandler.removeMessages(MSG_PHONEBOOK_UPDATE);
        mPhoneBookHandler.sendEmptyMessage(MSG_PHONEBOOK_UPDATE);
    }
    void syncContactsToProvider() {
        Log.d(TAG, "syncContactsToProvider enter " + mContactList.size());
		synchronized (mContactList) {
            final List<Contact> list = new ArrayList<Contact>();
            list.addAll(mContactList);
            mPhoneBookHandler.removeMessages(MSG_PHONEBOOK_UPDATE);
            mPhoneBookHandler.sendMessage(mPhoneBookHandler.obtainMessage(MSG_PHONEBOOK_UPDATE, list));
            /*
            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
			for (int i = 0; i < mContactList.size(); i++) {
                int size = ops.size();
				final Contact contact = mContactList.get(i);

                ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, "")
                        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, "")
                        .withYieldAllowed(true)
                        .build());
                
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, size)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact.name)
                        .withYieldAllowed(true)
                        .build());
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, size)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.number)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                        .withYieldAllowed(true)
                        .build());
            }
            try {
                getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            } catch (Exception e) {
                e.printStackTrace();
            }
            */
		}
        Log.d(TAG, "syncContactsToProvider end");
    }
}
