package com.hwatong.f70.bluetooth;

import java.lang.ref.WeakReference;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;

import com.hwatong.bt.BtDevice;
import com.hwatong.bt.IService;
import com.hwatong.f70.main.LogUtils;
import com.nforetek.bt.res.NfDef;

/**
 * 
 * 
 * @author ljw
 * 
 */
public class NForeBluetoothPresenter implements BluetoothContract.Presenter {

	private final static int UPDATE_PAIREDLIST = 0x01;
	private final static int ALREADY_CONNECTED = 0x02;
	private final static int ALREADY_DISCONNECTED = 0x03;
	private final static int ALREADY_DISCONNECTTINGTIMEOUT = 0x04;
	private static final int UPDATE_LOCALNAMECHANGED = 0X05;
	private final static int UPDATE_DEVICELIST = 0x06;
	private final static int UPDATE_DEVICELIST_DONE = 0x07;
	private final static int BLUETOOTH_STATUS_CHANGEING = 0x08;

	private IService iService;
	private com.hwatong.btphone.IService iBtPhoneService;

	private BluetoothContract.View mView;
	private Context context;
	private BluetoothHandler handler;

	public NForeBluetoothPresenter(Context context, BluetoothContract.View view) {
		this.context = context;
		this.mView = view;
		handler = new BluetoothHandler(mView);
		mView.setPresenter(this);
	}

	@Override
	public void enabledBluetooth(boolean option) {
		LogUtils.d("turnBluetooth:");
		if (iService != null)
			try {
				iService.setEnable(option);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		else
			LogUtils.d("turnBluetooth:iService is null");
	}

	@Override
	public void enabledAutoConnect(boolean option) {
		LogUtils.d("turnAutoConnect: " + option);
		if (iService != null) {
			try {
				iService.setAutoConnect(option);
				LogUtils.d("openAutoConnect: " + option);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else
			LogUtils.d("turnAutoConnect:iService is null");
	}

	@Override
	public void enabledAutoAnswer(boolean option) {
		LogUtils.d("callAutoAnswer");
		if (iBtPhoneService != null) {
			try {
				iBtPhoneService.setAutoAnswer(option);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else
			LogUtils.d("turnAutoAnswer: iBtPhoneService is null");
	}

	@Override
	public void setBtName(String name) {
		LogUtils.d("setBtLocalName:");
		if (iService != null) {
			try {
				disconnectDevice();
				iService.setLocalName(name);
				LogUtils.d("setBtLocalName: " + name);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else
			LogUtils.d("setBtLocalName:iService is null");
	}

	@Override
	public void connectDevice(BtDevice device) {
		LogUtils.d("connect:");
		if (iService != null) { 
			try {
				iService.disconnect();
				iService.connectDevice(device.addr);
				handler.removeMessages(ALREADY_DISCONNECTTINGTIMEOUT);
				handler.sendEmptyMessageDelayed(ALREADY_DISCONNECTTINGTIMEOUT,
						20000);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else
			LogUtils.d("connect:iService is null");
	}

	@Override
	public void disconnectDevice() {
		LogUtils.d("disconnect:");
		if (iService != null) {
			try {
				iService.disconnect();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else
			LogUtils.d("disconnect:iService is null");
	}

	@Override
	public void startDiscovery() {
		LogUtils.d("startSearch:");
		if (iService != null) {
			try {
				stopDiscovery();
				boolean s = iService.startDiscovery();
				if(!s)
					iService.startDiscovery();
				LogUtils.d("startSearch ret :" + s);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void stopDiscovery() {
		LogUtils.d("startSearch:");
		if (iService != null) {
			try {
				iService.stopDiscovery();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else
			LogUtils.d("stopSearch:iService is null");
	}

	@Override
	public void startGetPairedList() {
		LogUtils.d("startGetPairedList");
		if (iService != null) {
			try {
				iService.inquiryPairList();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else
			LogUtils.d("startGetPairedList:iService is null");
	}

	@Override
	public void deleteDevice(BtDevice device) {
		LogUtils.d("deletePaired:");
		if (iService != null) {
			try {
				iService.deletePair(device.addr);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else
			LogUtils.d("deletePaired:iService is null");
	}

	@Override
	public void isBluetoothOpen() {
		LogUtils.d("getBluetoothStatus:");
		if (iService != null) {
			try {
				int state = iService.getAdapterState();
				LogUtils.d("getBluetoothStatus: " + state);
				mView.showIsBluetoothOpen(state == NfDef.BT_STATE_ON || state == NfDef.BT_STATE_TURNING_ON);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else
			LogUtils.d("getBluetoothStatus:iService is null");
	}

	@Override
	public void isAutoConnectedOpen() {
		LogUtils.d("getAutoConnectedStatus:");
		if (iService != null) {
			try {
				boolean autoConnectStatus = iService.isAutoConnect();
				LogUtils.d("autoConnectStatus: " + autoConnectStatus);
				mView.showIsAutoconnectedOpen(autoConnectStatus);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else
			LogUtils.d("getAutoConnectedStatus:iService is null");
	}

	@Override
	public void isAutoAnswerOpen() {
		LogUtils.d("getAutoAnswerStatus:");
		if (iBtPhoneService != null) {
			try {
				long start = System.currentTimeMillis();
				LogUtils.d("start get autoAnswerStatus time: " + start);
				boolean autoAnswerStatus = iBtPhoneService.isAutoAnswer();
				LogUtils.d("autoAnswerStatus: " + autoAnswerStatus + "cost time: " + (System.currentTimeMillis() - start));
				mView.showIsAutoAnswerOpen(autoAnswerStatus);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else
			LogUtils.d("getAutoAnswerStatus:iService is null");
	}

	@Override
	public void getBluetoothName() {
		LogUtils.d("getBtLocaLName:");
		String name = "";
		if (iService != null) {
			try {
				name = iService.getLocalName();
				LogUtils.d("getBtLocaLName: " + name);
				mView.showGetBluetoothName(name);
			} catch (RemoteException e) {
				LogUtils.d("getBtLocaLName error: " + e.toString());
			}
		}
	}

	@Override
	public void bindService() {
//		if (mView instanceof BluetoothSwitchSetting)
			context.bindService(new Intent("com.hwatong.btphone.service"),
					btPhoneServiceConnection, Context.BIND_AUTO_CREATE);

		context.bindService(new Intent("com.hwatong.bt.service"),
				btServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void unbindService() {
//		if (mView instanceof BluetoothSwitchSetting)
		if(iService == null && iBtPhoneService == null)
			return;
		context.unbindService(btPhoneServiceConnection);
		context.unbindService(btServiceConnection);
	}

	@Override
	public void registerBtCallback() {

	}

	@Override
	public void unregisterBtCallback() {
		try {
			if (iService != null)
				iService.unregisterCallback(btCallBack);
			else
				LogUtils.d("iService is null");
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		handler.removeCallbacksAndMessages(null);
	}

	@Override
	public void getConnectedDevice() {
		LogUtils.d("getConnectedDevice:");
		if (iService != null) {
			try {
				BtDevice device = iService.getConnectDevice();
				LogUtils.d("getConnectedDevice: " + device);
				mView.showUpdateConnectedDeviceChanged(device);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else
			LogUtils.d("getConnectedDevice iService is null");
	}

	private static class BluetoothHandler extends Handler {
		WeakReference<BluetoothContract.View> mVReference;

		public BluetoothHandler(BluetoothContract.View view) {
			mVReference = new WeakReference<BluetoothContract.View>(view);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			final BluetoothContract.View mView = mVReference.get();

			if (mView == null)
				return;

			switch (msg.what) {
			case UPDATE_PAIREDLIST:
				mView.showUpdatePairedDevice((BtDevice) msg.obj);
				break;
			case ALREADY_CONNECTED:
				mView.showConnected();
				break;
			case ALREADY_DISCONNECTED:
				mView.showDisconnected();
				break;
			case UPDATE_LOCALNAMECHANGED:
				mView.showGetBluetoothName((String) msg.obj);
				break;
			case UPDATE_DEVICELIST:
				mView.showUpdateDiscoveryDevice((BtDevice) msg.obj);
				break;
			case UPDATE_DEVICELIST_DONE:
				mView.showDiscoveryDone();
				break;
			case ALREADY_DISCONNECTTINGTIMEOUT:
				mView.showConnectingTimeout();
				break;
			case BLUETOOTH_STATUS_CHANGEING:
				int status = (Integer) msg.obj;
				BluetoothStatus bluetoothStatus = null;
				switch (status) {
				case NfDef.BT_STATE_ON:
					bluetoothStatus = BluetoothStatus.ON;
					break;
				case NfDef.BT_STATE_OFF:
					bluetoothStatus = BluetoothStatus.OFF;
					break;
				case NfDef.BT_STATE_TURNING_ON:
					bluetoothStatus = BluetoothStatus.TURN_ON;
					break;
				case NfDef.BT_STATE_TURNING_OFF:
					bluetoothStatus = BluetoothStatus.TRUN_OFF;
					break;
				default:
					break;
				}
				mView.showBluetoothStatusChanged(bluetoothStatus);
				break;
			}
		}
	}

	private ServiceConnection btPhoneServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			LogUtils.d("btPhoneServiceConnection onServiceDisconnected:");
			iBtPhoneService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LogUtils.d("btServiceConnection onServiceConnected:");
			iBtPhoneService = com.hwatong.btphone.IService.Stub
					.asInterface(service);
			isAutoAnswerOpen();
		}
	};

	private ServiceConnection btServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder serv) {
			LogUtils.d("btServiceConnection onServiceConnected:");
			iService = IService.Stub.asInterface(serv);
			try {
				iService.registerCallback(btCallBack);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			isBluetoothOpen();
			isAutoConnectedOpen();
			getBluetoothName();
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			LogUtils.d("btServiceConnection onServiceDisconnected:");
			iService = null;
		}
	};

	private final com.hwatong.bt.ICallback btCallBack = new com.hwatong.bt.ICallback.Stub() {

		@Override
		public void onLocalName(String name) throws RemoteException {
			LogUtils.d("f70 bt onLocalName: " + name);
			Message message = Message.obtain();
			message.what = UPDATE_LOCALNAMECHANGED;
			message.obj = name;
			handler.sendMessage(message);
		}

		@Override
		public void onDiscoveryDone() throws RemoteException {
			LogUtils.d("f70 bt onDiscoveryDone");
			handler.sendEmptyMessage(UPDATE_DEVICELIST_DONE);
		}

		@Override
		public void onDiscovery(String name, String addr)
				throws RemoteException {
			LogUtils.d("f70 bt onDiscovery: " + name + ", " + addr);
			Message message = Message.obtain();
			message.what = UPDATE_DEVICELIST;
			message.obj = new BtDevice(name, addr);
			handler.sendMessage(message);
		}

		@Override
		public void onDisconnected() throws RemoteException {
			LogUtils.d("f70 bt onConnected");
			handler.sendEmptyMessage(ALREADY_DISCONNECTED);
		}

		@Override
		public void onCurrentAndPairList(String index, String addr, String name)
				throws RemoteException {
			LogUtils.d("f70 bt onCurrentAndPairList: " + ", index: " + index
					+ ", addr: " + addr + ", name: " + name);
			Message message = Message.obtain();
			message.what = UPDATE_PAIREDLIST;
			message.obj = new BtDevice(name, addr);
			handler.sendMessage(message);
		}

		@Override
		public void onConnected() throws RemoteException {
			LogUtils.d("f70 bt onConnected");
			handler.removeMessages(ALREADY_DISCONNECTTINGTIMEOUT);
			handler.sendEmptyMessage(ALREADY_CONNECTED);
		}

		@Override
		public void onAdapterState(int state) throws RemoteException {
			LogUtils.d("f70 bt onAdapterState: " + state);
//			boolean isChanging = (state == NfDef.BT_STATE_TURNING_ON || state == NfDef.BT_STATE_TURNING_OFF);
			Message msg = Message.obtain();
			msg.what = BLUETOOTH_STATUS_CHANGEING;
			msg.obj = state;
			handler.sendMessage(msg);
		}
	};

}
