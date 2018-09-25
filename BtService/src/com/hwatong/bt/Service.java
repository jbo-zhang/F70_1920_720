package com.hwatong.bt;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.media.AudioManager;
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
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.nforetek.bt.aidl.INfCallbackBluetooth;
import com.nforetek.bt.aidl.INfCommandBluetooth;
import com.nforetek.bt.res.NfDef;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import android.view.ViewGroup;
import android.widget.Toast;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.content.DialogInterface;

public class Service extends android.app.Service implements DialogInterface.OnDismissListener {
	private static final String TAG  = "BtService";
	private static final boolean DBG = true;
	private boolean isAccOffDisconnect = false;
	private boolean isAccOnRetryConnectIdle = false;
	
	private static final String BLUETOOTH_PREF = "bluetooth_pref";
	private static final String PREF_PINCODE = "pincode";
	
	private INfCommandBluetooth mCommandBluetooth;
	
	private HashMap<String, BtDevice>  mConnectedDevices = new HashMap<String, BtDevice>();
	
	private HashMap<String, String>  mDiscoveredDevices = new HashMap<String, String>();
	
	String mPinCode = null;
    boolean mConnectRequest;
    String mTargetDevice;
    String mTargetDevice4ACC;

    private final IBinder mBinder = new ServiceImpl(this);
    
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
	
	        if (className.equals(new ComponentName(NfDef.PACKAGE_NAME, NfDef.CLASS_SERVICE_BLUETOOTH))) {
	            Log.e(TAG,"ComponentName(" + NfDef.CLASS_SERVICE_BLUETOOTH + ")");
	            mCommandBluetooth = INfCommandBluetooth.Stub.asInterface(service);
	            if (mCommandBluetooth == null) {
	                Log.e(TAG,"mCommandBluetooth is null !!");
	                return;
	            }
	
	            try {
	                mCommandBluetooth.registerBtCallback(mCallbackBluetooth);
	            } catch (RemoteException e) {
	                e.printStackTrace();
	            }
	        }
	        
	        Log.e(TAG, "end onServiceConnected");
	    }
	    public void onServiceDisconnected(ComponentName className) {
	        Log.e(TAG, "ready onServiceDisconnected: " + className);          
	        if (className.equals(new ComponentName(NfDef.PACKAGE_NAME, NfDef.CLASS_SERVICE_BLUETOOTH))) {
	            mCommandBluetooth = null;
	        }            
	
	        Log.e(TAG, "end onServiceDisconnected");
	    }
    };

    /*
     * Bluetooth callback
     * 
     */
    private INfCallbackBluetooth mCallbackBluetooth = new INfCallbackBluetooth.Stub() {

        @Override
        public void onBluetoothServiceReady() throws RemoteException {
            Log.v(TAG,"onBluetoothServiceReady()");

        }
        
        @Override
        public void onAdapterStateChanged(int prevState, int newState) throws RemoteException {
            Log.v(TAG,"onAdapterStateChanged() state: " + prevState + "->" + newState);
            if(NfDef.BT_STATE_ON == newState) {
                getLocalAddress();
            }
            notifyAdaperState(newState);
        }

        @Override
        public void onAdapterDiscoverableModeChanged(int prevState, int newState)
                throws RemoteException {
            Log.v(TAG,"onAdapterDiscoverableModeChanged() state: " + prevState + "->" + newState);
            try {
                if(newState == NfDef.BT_MODE_CONNECTABLE) {
                    mCommandBluetooth.setBtDiscoverableTimeout(0);
                }
            } catch(RemoteException e) {
            }

        }

        @Override
        public void onAdapterDiscoveryStarted() throws RemoteException {
            Log.v(TAG,"onAdapterDiscoveryStarted()");
            showPrompt(1, SEARCHING_STRINGID);
        }

        @Override
        public void onAdapterDiscoveryFinished() throws RemoteException {
            Log.v(TAG,"onAdapterDiscoveryFinished()");
            notifyDiscoveryDone();
            cancleBluetoothDialog();
        }

        @Override
        public void retPairedDevices(int elements, String[] address, String[] name,
                int[] supportProfile, byte[] category) throws RemoteException {
            Log.v(TAG,"retPairedDevices() elements: " + elements);
            for(int i = 0;i < elements; i++) {
            	notifyCurrentAndPairList(String.valueOf(i), address[i], name[i]);
            }
        }

        @Override
        public void onDeviceFound(String address, String name, byte category)
                throws RemoteException {
            Log.v(TAG,"onDeviceFound() " + address + " name: " + name);
            if(mDiscoveredDevices.get(address) == null) {
            	mDiscoveredDevices.put(address, name);
            	notifyDiscovery(name, address);
            }
        }

        @Override
        public void onDeviceBondStateChanged(String address, String name, int prevState,
                int newState) throws RemoteException {
            Log.v(TAG,"onDeviceBondStateChanged() " + address + " name: " + name + " state: " + prevState + "->" + newState);
            if(newState == NfDef.BOND_BONDING) {
            	if(getConnectDevice() == null)
            		showPrompt(1, PAIRING_STRINGID);
            } else {
                cancleBluetoothDialog();
            }
        }

        @Override
        public void onDeviceUuidsUpdated(String address, String name, int supportProfile)
                throws RemoteException {
            Log.v(TAG,"onDeviceUuidsUpdated() " + address + " name: " + name + " supportProfile: " + supportProfile);

        }

        @Override
        public void onLocalAdapterNameChanged(String name) throws RemoteException {
            Log.v(TAG,"onLocalAdapterNameChanged() " + name);
            notifyLocalName(name);
        }

        @Override
        public void onDeviceOutOfRange(String address) throws RemoteException {
            Log.v(TAG,"onDeviceOutOfRange() " + address);

        }

        @Override
        public void onDeviceAclDisconnected(String address) throws RemoteException {
            Log.v(TAG,"onDeviceAclDisconnected() " + address);
            
        }

        @Override
        public void onBtRoleModeChanged(int mode) throws RemoteException {
            Log.v(TAG,"onBtRoleModeChanged() " + mode);

        }

        @Override
        public void onBtAutoConnectStateChanged(String address, int prevState, int newState) throws RemoteException {
            Log.v(TAG,"onBtAutoConnectStateChanged() prevState " + prevState + " newState " + newState);

        }

    };

	@Override
	public void onCreate() {
		super.onCreate();
		
        Log.v(TAG,"bindBluetoothService");
        SharedPreferences prefs = getSharedPreferences(BLUETOOTH_PREF, MODE_PRIVATE);
		mPinCode = prefs.getString(PREF_PINCODE, "0000");
        bindService(new Intent(NfDef.CLASS_SERVICE_BLUETOOTH), this.mConnection, BIND_AUTO_CREATE);
        IntentFilter filter = new IntentFilter();

        filter.addAction("android.bluetooth.device.action.PAIRING_REQUEST");
        filter.addAction("com.hwatong.system.CLOSE_SYSTEM_DIALOG");
        filter.addAction("com.hwatong.system.ACC_STATUS");

        registerReceiver(mReceiver, filter);
		Log.d(TAG, "onCreate");
	}

	@Override
	public void onDestroy() {
		try {
			if (mCommandBluetooth!= null) {
				mCommandBluetooth.unregisterBtCallback(mCallbackBluetooth);
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		unbindService(mConnection);
		unregisterReceiver(mReceiver);
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

    private void notifyLocalName(String name) {
        synchronized (mCallbacks) {
            int size = mCallbacks.size();
            for (int i = 0; i < size; i++) {
                Callback cb = mCallbacks.get(i);
                try {
                    cb.mCallback.onLocalName(name);
                } catch (RemoteException e) {
					e.printStackTrace();
                }
            }
        }
    }
    
    private void notifyAdaperState(int state) {
        synchronized (mCallbacks) {
            int size = mCallbacks.size();
            for (int i = 0; i < size; i++) {
                Callback cb = mCallbacks.get(i);
                try {
                    cb.mCallback.onAdapterState(state);
                } catch (RemoteException e) {
					e.printStackTrace();
                }
            }
        }
    }

    private void notifyConnected() {
        synchronized (mCallbacks) {
            int size = mCallbacks.size();
            for (int i = 0; i < size; i++) {
                Callback cb = mCallbacks.get(i);
                try {
                    cb.mCallback.onConnected();
                } catch (RemoteException e) {
					e.printStackTrace();
                }
            }
        }
    }

    private void notifyDisconnected() {
        synchronized (mCallbacks) {
            int size = mCallbacks.size();
            for (int i = 0; i < size; i++) {
                Callback cb = mCallbacks.get(i);
                try {
                    cb.mCallback.onDisconnected();
                } catch (RemoteException e) {
					e.printStackTrace();
                }
            }
        }
    }

    private void notifyDiscovery(String name, String addr) {
        synchronized (mCallbacks) {
            int size = mCallbacks.size();
            for (int i = 0; i < size; i++) {
                Callback cb = mCallbacks.get(i);
                try {
                    cb.mCallback.onDiscovery(name, addr);
                } catch (RemoteException e) {
					e.printStackTrace();
                }
            }
        }
    }

    private void notifyDiscoveryDone() {
        synchronized (mCallbacks) {
            int size = mCallbacks.size();
            for (int i = 0; i < size; i++) {
                Callback cb = mCallbacks.get(i);
                try {
                    cb.mCallback.onDiscoveryDone();
                } catch (RemoteException e) {
					e.printStackTrace();
                }
            }
        }
    }

    private void notifyCurrentAndPairList(String index, String addr, String name) {
        synchronized (mCallbacks) {
            int size = mCallbacks.size();
            for (int i = 0; i < size; i++) {
                Callback cb = mCallbacks.get(i);
                try {
                    cb.mCallback.onCurrentAndPairList(index, addr, name);
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

	private String getLocalName() {
		if(mCommandBluetooth == null) {
			return null;
		}
		String name = null;
		synchronized (this) {
			try {
				name = mCommandBluetooth.getBtLocalName();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return name;
	}

	private boolean setLocalName(String name) {
		if (name == null || name.length() == 0) {
			return false;
		}
		if(mCommandBluetooth == null) {
			return false;
		}
		boolean ret = false;
		synchronized (this) {
			try {
				ret = mCommandBluetooth.setBtLocalName(name);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ret;
	}

	private String getPinCode() {
		synchronized (this) {
			
			return mPinCode;
		}
	}

	private boolean setPinCode(String pincode) {
		if (pincode == null || pincode.length() != 4)
			return false;

		synchronized (this) {
			mPinCode = pincode;
			Editor editor = getSharedPreferences(BLUETOOTH_PREF, MODE_PRIVATE).edit();
			editor.putString(PREF_PINCODE, pincode);
			editor.commit();
		}
		return true;
	}

	private String getLocalAddress() {
		if(mCommandBluetooth == null) {
			return null;
		}
		String addr = null;
		synchronized (this) {
			try {
				addr = mCommandBluetooth.getBtLocalAddress();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
        if(addr == null || addr.isEmpty() || !stringIsMac(addr) || "00:00:00:00:00:00".equals(addr)) {
            addr = getSavedBtAddress();
        } else {
            saveBtAddress(addr);
        }
		return addr;
	}

	private String getVersion() {
		if(mCommandBluetooth == null) {
			return null;
		}
		String ver = null;
		synchronized (this) {
			try {
				ver = mCommandBluetooth.getNfServiceVersionName();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return ver;
	}

	private boolean getConnectState() {
		synchronized(mConnectedDevices) {
			return mConnectedDevices.size() > 0;
		}
	}
	
	private com.hwatong.bt.BtDevice getConnectDevice() {
		synchronized (this) {
			if(mCommandBluetooth == null) {
				return null;
			}
			BtDevice device = null;
			Iterator iter = mConnectedDevices.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				String key = (String)entry.getKey();
				device = (BtDevice)entry.getValue();
				if((device.profiles & BtDef.BT_PROFILE_HFP) != 0) {
					return new BtDevice(device);
				}
			}

			if(device != null) {
				return new BtDevice(device);
			}
			return null;
		}
	}

	private boolean deletePair(String addr) {
		if(mCommandBluetooth == null) {
			return false;
		}
		boolean ret = false;
		synchronized (this) {
			try {
				ret = mCommandBluetooth.reqBtUnpair(addr);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	private boolean startDiscovery() {
		mDiscoveredDevices.clear();
		if(mCommandBluetooth == null) {
			return false;
		}
		boolean ret = false;
        int times = 5;
		synchronized (this) {
			try {
                mCommandBluetooth.reqBtDisconnectAll();
                while(!(ret = mCommandBluetooth.startBtDiscovery()) && (times-- > 0)) {
                    Log.d(TAG, "startDiscovery failed " + times);
                    try {
                        Thread.sleep(100);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
        Log.d(TAG, "startDiscovery " + ret);
		return ret;
	}

	private boolean inquiryPairList() {
		if(mCommandBluetooth == null) {
			return false;
		}
		boolean ret = false;
		synchronized (this) {
			try {
				ret = mCommandBluetooth.reqBtPairedDevices();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	private boolean stopDiscovery() {
		if(mCommandBluetooth == null) {
			return false;
		}
		boolean ret = false;
		synchronized (this) {
			try {
				ret = mCommandBluetooth.cancelBtDiscovery();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	private boolean connectDevice(String addr) {
		if(mCommandBluetooth == null) {
			return false;
		}
		boolean ret = false;
		synchronized (this) {
			try {
                //mCommandBluetooth.reqBtDisconnectAll();
                int r = mCommandBluetooth.reqBtConnectHfpA2dp(addr);
                Log.d(TAG, "reqBtConnectHfpA2dp " + r + "addr: " + addr);
				if((r > 0) && (r & NfDef.PROFILE_HFP) != 0) {
				    ret = true;
                    mConnectRequest = true;
                    mTargetDevice = addr;
                    showPrompt(1, CONNECTING_STRINGID);
                }
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	private void disconnect() {
        Log.d(TAG, "disconnect");
        mConnectRequest = false;
        mTargetDevice = null;
		if(mCommandBluetooth == null) {
			return;
		}
		synchronized (this) {
			try {
				mCommandBluetooth.reqBtDisconnectAll();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	private int getAdapterState() {
		int ret = NfDef.BT_STATE_OFF;
		if(mCommandBluetooth == null) {
			return ret;
		}
		
		synchronized (this) {
			try {
				ret = mCommandBluetooth.getBtState();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}
	
	private boolean setEnable(boolean enable) {
		if(mCommandBluetooth == null) {
			return false;
		}
		boolean ret = false;
		synchronized (this) {
			try {
				ret = mCommandBluetooth.setBtEnable(enable);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}
	
	private void updateRemoteDevice(String addr, int profile, int state, int prestate) {
		Log.d(TAG, "updateRemoteDevice " + "addr " + addr + " profile " + profile + " state " + state + " prestate " + prestate + " mConnectRequest " + mConnectRequest + " mTargetDevice " + mTargetDevice);
		Log.d(TAG, mHandler.hasMessages(MSG_HIDESHOW_STATE) ? "have MSG_HIDESHOW_STATE msg.." : "not have MSG_HIDESHOW_STATE msg..");
		//LJW 20180509
		if(addr != null && state == BtDef.BT_STATE_CONNECTED) { //BT_STATE_CONNECTED = 3
			Log.i(TAG, "mTargetDevice4ACC set value: " + addr);		
			mTargetDevice4ACC = addr;
		}
		if(!isAccOnRetryConnectIdle && !isAccOffDisconnect && state != BtDef.BT_STATE_CONNECTED && state != BtDef.BT_STATE_STREAMING) { //BT_STATE_STREAMING = 5
			Log.i(TAG, "mTargetDevice4ACC set value: " + null);
			mTargetDevice4ACC = null;
		}
		synchronized(mConnectedDevices) {
			if(addr == null) {
				Log.d(TAG, "updateRemoteDevice here");
				BtDevice device = null;
				Iterator iter = mConnectedDevices.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry entry = (Map.Entry) iter.next();
					String key = (String)entry.getKey();
					device = (BtDevice)entry.getValue();
					if((device.profiles & profile) != 0) {
						iter.remove();
					}
				}
				return;
			}
			BtDevice cp = mConnectedDevices.get(addr);
			if(cp == null) {
				if(state == BtDef.BT_STATE_CONNECTED) {
					String name;
					try {
						name = mCommandBluetooth.getBtRemoteDeviceName(addr);
					} catch (RemoteException e) {
						e.printStackTrace();
						name = "";
					}
					mConnectedDevices.put(addr, new BtDevice(name, addr, profile));
					Log.d(TAG, "updateRemoteDevice addr " + addr);
					if(mConnectedDevices.size() == 1) {
						notifyConnected();
					}
				}
			} else {
				Log.d(TAG, "updateRemoteDevice profiles " + cp.profiles);
				if(state == BtDef.BT_STATE_CONNECTED) {
					cp.profiles |= profile;
					Log.d(TAG, "updateRemoteDevice add profiles " + cp.profiles);
				} else {
					cp.profiles &= ~profile;
					Log.d(TAG, "updateRemoteDevice remove profiles " + cp.profiles);
					if(cp.profiles == 0) {
						mConnectedDevices.remove(addr);
						Log.d(TAG, "updateRemoteDevice remove " + addr);
						Log.d(TAG, "updateRemoteDevice size " + mConnectedDevices.size());
						if(mConnectedDevices.size() == 0) {
							notifyDisconnected();
						}
					}
				}
			}
            if((profile & BtDef.BT_PROFILE_HFP) != 0) {
                if(state == BtDef.BT_STATE_READY) {
                    if(prestate == BtDef.BT_STATE_CONNECTED || prestate == BtDef.BT_STATE_DISCONNECTING) {
                        if(mTargetDevice != null && mTargetDevice.equals(addr)) {
                            cancleBluetoothDialog();
                        }
                        showPrompt(0 ,DISCONNECTED_TOAST_LAYOUTID);
                    } else if(prestate == BtDef.BT_STATE_CONNECTING) {
                        if(mConnectRequest) {
                            hideShowPromptDelay(0 ,CONNECTED_FAILED_TOAST_LAYOUTID, 15000);
                        }
                    } else {
                        if(mTargetDevice != null && mTargetDevice.equals(addr)) {
                            cancleBluetoothDialog();
                        }
                    }
                    if(mTargetDevice != null && mTargetDevice.equals(addr)) {
                        mConnectRequest = false;
                        mTargetDevice = null;
                    }
                } else if(state == BtDef.BT_STATE_CONNECTING) {
                    if(mConnectRequest) {
                        //showPrompt(1, CONNECTING_STRINGID);
                    }
                } else if(state == BtDef.BT_STATE_CONNECTED) {
                    cancleBluetoothDialog();
                    showPrompt(0 ,CONNECTED_TOAST_LAYOUTID);
                    mConnectRequest = false;
                    mTargetDevice = null;
                } else if(state == BtDef.BT_STATE_DISCONNECTING) {
                    if(mTargetDevice != null && mTargetDevice.equals(addr)) {
                        mConnectRequest = false;
                        mTargetDevice = null;
                    }
                }
            }
		}
	}
	
	private void setAutoConnect(boolean enable) {
		if(mCommandBluetooth == null) {
			return;
		}
		synchronized (this) {
			try {
                if(enable) {
				    mCommandBluetooth.setBtAutoConnect(NfDef.AUTO_CONNECT_WHEN_BT_ON | NfDef.AUTO_CONNECT_WHEN_PAIRED | NfDef.AUTO_CONNECT_WHEN_OOR, 0);
                    if(!getConnectState()) {
                        String addr = mCommandBluetooth.getBtAutoConnectingAddress();
                        Log.d(TAG, "setAutoConnect " + addr);
                        if(addr != null && !addr.isEmpty() && stringIsMac(addr) && !"00:00:00:00:00:00".equals(addr)) {
                            int r = mCommandBluetooth.reqBtConnectHfpA2dp(addr);
                            Log.d(TAG, "reqBtConnectHfpA2dp " + r);
            				if((r > 0) && (r & NfDef.PROFILE_HFP) != 0) {
                                mConnectRequest = true;
                                mTargetDevice = addr;
                                showPrompt(1, CONNECTING_STRINGID);
                            }
                        }
                    }
                } else {
                    mCommandBluetooth.setBtAutoConnect(NfDef.AUTO_CONNECT_DISABLE, 5);
                }
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	private boolean isAutoConnect() {
		if(mCommandBluetooth == null) {
			return false;
		}
		boolean ret = false;
		synchronized (this) {
			try {
				ret = (mCommandBluetooth.getBtAutoConnectCondition() != NfDef.AUTO_CONNECT_DISABLE);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}

	private static class ServiceImpl extends IService.Stub {
	    final WeakReference<Service> mService;

	    ServiceImpl(Service service) {
	        mService = new WeakReference<Service>(service);
	    }

		@Override
		public void registerCallback(ICallback callback) {
			mService.get().registerCallback(callback);
		}

		@Override
		public void unregisterCallback(ICallback callback) {
			mService.get().unregisterCallback(callback);
		}

		// setting
		@Override
		public String getLocalName() {
			return mService.get().getLocalName();
		}

		@Override
		public boolean setLocalName(String name) {
			return mService.get().setLocalName(name);
		}

		@Override
		public String getPinCode() {
			return mService.get().getPinCode();
		}

		@Override
		public boolean setPinCode(String pincode) {
			return mService.get().setPinCode(pincode);
		}

		@Override
		public String getLocalAddress() {
			return mService.get().getLocalAddress();
		}

		@Override
		public String getVersion() {
			return mService.get().getVersion();
		}

		// connect

		@Override
		public boolean getConnectState() {
			return mService.get().getConnectState();
		}

		@Override
		public BtDevice getConnectDevice() {
			return mService.get().getConnectDevice();
		}

		// devices list

		@Override
		public boolean deletePair(String addr) {
			return mService.get().deletePair(addr);
		}

		@Override
		public boolean startDiscovery() {
			return mService.get().startDiscovery();
		}

		@Override
		public boolean inquiryPairList() {
			return mService.get().inquiryPairList();
		}

		@Override
		public boolean stopDiscovery() {
			return mService.get().stopDiscovery();
		}

		@Override
		public boolean connectDevice(String addr) {
			return mService.get().connectDevice(addr);
		}

		@Override
		public void disconnect() {
			mService.get().disconnect();
		}

		@Override
		public int getAdapterState() throws RemoteException {
			// TODO Auto-generated method stub
			return mService.get().getAdapterState();
		}

		@Override
		public boolean setEnable(boolean enable) throws RemoteException {
			// TODO Auto-generated method stub
			return mService.get().setEnable(enable);
		}

		@Override
		public void updateRemoteDevice(String addr, int profile, int state, int prestate)
				throws RemoteException {
			mService.get().updateRemoteDevice(addr, profile, state, prestate);
		}

		@Override
		public void setAutoConnect(boolean enable) throws RemoteException {
			mService.get().setAutoConnect(enable);
		}

		@Override
		public boolean isAutoConnect() throws RemoteException {
			return mService.get().isAutoConnect();
		}
	}
	boolean isAutoAcceptPairingRequest = true;
	static final int ACC_OFF = 0;
	static final int ACC_ON = 1;
    BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @SuppressLint("NewApi")
		@Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction(); 
            Log.e(TAG,"Piggy Check action: " + action);
            if (action.equals("android.bluetooth.device.action.PAIRING_REQUEST")) {
                BluetoothDevice device =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int type = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT,
                        BluetoothDevice.ERROR);
                Log.e(TAG,"Piggy Check type: " + type);
                if (isAutoAcceptPairingRequest) {
                    if (type == BluetoothDevice.PAIRING_VARIANT_PASSKEY_CONFIRMATION) {
                        Log.e(TAG,"PAIRING_VARIANT_PASSKEY_CONFIRMATION");
                        try {
                        	device.setPairingConfirmation(true);
                        	//device.cancelPairingUserInput();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else if (type == BluetoothDevice.PAIRING_VARIANT_PIN){
                        Log.e(TAG,"PAIRING_VARIANT_PIN");
                        try {
                        	device.setPin(mPinCode.getBytes());
                        	device.createBond();
                        	//device.cancelPairingUserInput();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (type == BluetoothDevice.PAIRING_VARIANT_CONSENT){
                        Log.e(TAG,"PAIRING_VARIANT_CONSENT");
                        try {
                        	device.setPairingConfirmation(true);
                        	//device.cancelPairingUserInput();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.e(TAG,"Unkown paring type" + type);
                    }
                }
            } else if(action.equals("com.hwatong.system.CLOSE_SYSTEM_DIALOG")) {
                cancleBluetoothDialog();
            } else if(action.equals("com.hwatong.system.ACC_STATUS")) { //DISCONNECT BT WHEN ACCOFF, MODIFY LJW 20180504
            	Log.d(TAG, "ljw check acc status: " + intent.getIntExtra("status", -999));
            	if(intent.hasExtra("status")) {
            		int status = intent.getIntExtra("status", -999);
            		if(status == ACC_OFF) { //ACC_OFF disconnect BT delay 3000ms
            			if(!mHandler.hasMessages(MSG_ACC_STATUS_OFF))
            				mHandler.sendEmptyMessageDelayed(MSG_ACC_STATUS_OFF, 3000);
            		} else { //ACC_ON
            			if(mHandler.hasMessages(MSG_ACC_STATUS_OFF))
            				mHandler.removeMessages(MSG_ACC_STATUS_OFF);
            			if(!mHandler.hasMessages(MSG_ACC_STATUS_ON))
            				mHandler.sendEmptyMessage(MSG_ACC_STATUS_ON);
            		}
            	}
            }
        }
    };
    private static void writeFile(String path, String content) {
        if(path == null || content == null) {
            return;
        }
        try {
            final FileOutputStream os = new FileOutputStream(path);
            try {
                os.write(content.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String readFile(String path) {
        
		try {
            File file = new File(path);
            if(file == null || !file.exists() || file.length() > 32) {
                return "";
            }
			final InputStream is = new FileInputStream(path);

			final byte[] buf = new byte[(int)file.length()+1];
			int n = is.read(buf);

			is.close();
            if(n != file.length()) {
                return "";
            }

            return new String(buf, 0, n);
		} catch (java.io.FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "";
	}
    private static boolean stringIsMac(String val) {
        String trueMacAddress = "([A-Fa-f0-9]{2}:){5}[A-Fa-f0-9]{2}";  
        if (val.matches(trueMacAddress)) {
            return true;  
        } else {
            return false;  
        }  
    }

    private static void saveBtAddress(String addr) {
        if(addr == null || addr.isEmpty() || !stringIsMac(addr) || "00:00:00:00:00:00".equals(addr)) {
            return;
        }
        String old = getSavedBtAddress();
        if(!old.equals(addr)) {
            writeFile("/device/bt_mac", addr);
        }
    }
    private static String getSavedBtAddress() {
        String addr = readFile("/device/bt_mac");
        if(addr.isEmpty()) {
            return "";
        }
        if(stringIsMac(addr) && !"00:00:00:00:00:00".equals(addr)) {
            return addr;
        }
        return "";
    }

	public static final int CONNECTED_FAILED_TOAST_LAYOUTID = R.layout.f70_connect_bluetooth_failed_toast;
	public static final int CONNECTED_TOAST_LAYOUTID = R.layout.f70_connect_bluetooth_toast;
	public static final int DISCONNECTED_TOAST_LAYOUTID = R.layout.f70_disconnected_toast;

    public static final int CONNECTING_STRINGID = R.string.f70_bluetooth_connecting;
    public static final int PAIRING_STRINGID = R.string.f70_bluetooth_pairing;
    public static final int SEARCHING_STRINGID = R.string.f70_bluetooth_searching;
    Toast mToast;
    public Toast bluetoothConnectedInfoToast(Context context, int layoutId) {
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(layoutId, null);
        if(mToast == null)
		    mToast = new Toast(context);
		mToast.setGravity(Gravity.LEFT | Gravity.CENTER, 60, 0);
		mToast.setDuration(3000);
		mToast.setView(layout);
		return mToast;
	}
    public void showPrompt(int state, int id) {
        Log.d(TAG, "state:" + state + "cancleBluetoothDialog :" + "id is: " + id + ", other: CONNECTED_FAILED_TOAST_LAYOUTID: " + CONNECTED_FAILED_TOAST_LAYOUTID
        		+ ", CONNECTED_TOAST_LAYOUTID: " + CONNECTED_TOAST_LAYOUTID + ", DISCONNECTED_TOAST_LAYOUTID: " + DISCONNECTED_TOAST_LAYOUTID
        		+ ", CONNECTING_STRINGID: " + CONNECTING_STRINGID + ", PAIRING_STRINGID: " + PAIRING_STRINGID + ", SEARCHING_STRINGID: " +
        				SEARCHING_STRINGID);
        mHandler.removeMessages(MSG_SHOW_STATE);
        mHandler.removeMessages(MSG_HIDESHOW_STATE);
	    mHandler.sendMessage(mHandler.obtainMessage(MSG_SHOW_STATE, state, id));
	}
    public void hideShowPromptDelay(int state, int id, int delay) {
        Log.d(TAG, "hideShowPromptDelay");
        mHandler.removeMessages(MSG_SHOW_STATE);
        mHandler.removeMessages(MSG_HIDESHOW_STATE);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_HIDESHOW_STATE, state, id), delay);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(99999, state, id), 14000);
	}

    private BluetoothProgressDialog mProgressDialog;
    public void showBluetoothDialog(Context context, int layoutId) {
		if (mProgressDialog != null) {
			mProgressDialog.cancel();
		}
		mProgressDialog = new BluetoothProgressDialog(context,
				context.getResources().getString(layoutId), layoutId == SEARCHING_STRINGID);
		mProgressDialog.setCanceledOnTouchOutside(false);
		int offset = (int) getResources().getDimension(R.dimen.blurtooth_progress_offset);
		Window win = mProgressDialog.getWindow();
		LayoutParams params = new LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_PHONE;
		params.width = LayoutParams.WRAP_CONTENT;
		params.height = LayoutParams.WRAP_CONTENT;
		params.x = -offset;
		win.setAttributes(params);
        mProgressDialog.setOnDismissListener(this);
		mProgressDialog.show();
    }
    public void cancleBluetoothDialog() {
        Log.d(TAG, "cancleBluetoothDialog");
		mHandler.sendMessage(mHandler.obtainMessage(MSG_HIDE_STATE, 0, 0));
    }
    @Override
    public void onDismiss(DialogInterface dialog) {
        if(mProgressDialog != null) {
            if(mProgressDialog.mCanceled) {
                Log.d(TAG, "cancel");
                if(mCommandBluetooth != null) {
                    try {
                        mCommandBluetooth.cancelBtDiscovery();
                    } catch(RemoteException e) {
                    }
                }
            }
        }
    }
    static final int MSG_SHOW_STATE = 1;
    static final int MSG_HIDE_STATE = 2;
    static final int MSG_HIDESHOW_STATE = 3;
    
    static final int MSG_ACC_STATUS_OFF = 4;
    static final int MSG_ACC_STATUS_ON = 5;
    static final int MSG_RETRY_CONNECT = 6;
    Handler mHandler = new Handler() {
         @Override
         public void handleMessage(android.os.Message msg) {
             switch(msg.what) {
                 case MSG_SHOW_STATE:
                    if(msg.arg1 == 1) {
                        showBluetoothDialog(Service.this, msg.arg2);
                    } else {
                        bluetoothConnectedInfoToast(Service.this, msg.arg2).show();
                    }
                    break;

                case MSG_HIDE_STATE:
                    if (mProgressDialog != null) {
			            mProgressDialog.cancel();
		            }
                    break;
                case MSG_HIDESHOW_STATE:
                    if (mProgressDialog != null) {
			            mProgressDialog.cancel();
		            }
                    if(msg.arg1 == 1) {
                        showBluetoothDialog(Service.this, msg.arg2);
                    } else {
                        bluetoothConnectedInfoToast(Service.this, msg.arg2).show();
                    }
                    break;
                case MSG_ACC_STATUS_OFF:
                	Log.d(TAG, "MSG_ACC_STATUS_OFF mTargetDevice4ACC get value: " + mTargetDevice4ACC);
                	isAccOffDisconnect = true;
//                	if(!TextUtils.isEmpty(mTargetDevice))//bt connected before accoff
//                		mTargetDevice4ACC = mTargetDevice;
                	disconnect();
                	break;
                case MSG_ACC_STATUS_ON:
                	Log.d(TAG, "MSG_ACC_STATUS_ON mTargetDevice4ACC get value: " + mTargetDevice4ACC + "isAutoConnect:" + isAutoConnect());
                	if(!TextUtils.isEmpty(mTargetDevice4ACC) && isAutoConnect()){
                		isAccOnRetryConnectIdle = true;
                		mHandler.removeMessages(MSG_RETRY_CONNECT);
                		mHandler.sendEmptyMessageDelayed(MSG_RETRY_CONNECT, 5000);
                		
//                		mTargetDevice4ACC = null;
                		if(isAccOffDisconnect)
                			isAccOffDisconnect = false;
                	} //bt connected before accoff
                	break;
                case MSG_RETRY_CONNECT:
                	connectDevice(mTargetDevice4ACC);
                	isAccOnRetryConnectIdle = false;
                	break;
                case 99999:
                	Log.d(TAG, mHandler.hasMessages(MSG_HIDESHOW_STATE) ? "have MSG_HIDESHOW_STATE msg.." : "not have MSG_HIDESHOW_STATE msg..");
                	break;
             }
         }
    };
}
