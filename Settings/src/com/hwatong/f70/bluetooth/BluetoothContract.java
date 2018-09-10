package com.hwatong.f70.bluetooth;

import com.hwatong.bt.BtDevice;
/**
 * @author ljw
 *
 */
public interface BluetoothContract {
	
	interface View extends BaseView<Presenter> {
		/**
		 * 
		 * @param isOpen
		 */
		void showIsBluetoothOpen(boolean isOpen);
		
		/**
		 * 
		 */
		void showIsAutoconnectedOpen(boolean isOpen);
		
		/**
		 * 
		 */
		void showGetBluetoothName(String name);
		
		/**
		 * 
		 */
		void showIsAutoAnswerOpen(boolean isOpen);
		
		/**
		 * 
		 */
		void showDiscoveryDone();
		
		/**
		 *
		 */
		void showUpdateDiscoveryDevice(BtDevice device);
		
		/**
		 * 
		 */
		void showDisconnected();
		
		/**
		 * 
		 */
		void showConnected();
		
		/**
		 * 
		 */
		void showUpdatePairedDevice(BtDevice device);
		
		/**
		 * 
		 */
		void showUpdateConnectedDeviceChanged(BtDevice device);
		
		/**
		 * 
		 */
		void showConnectingTimeout();
		
		/**
		 * 
		 */
		void showBluetoothStatusChanged(BluetoothStatus status);
		
	}
	
	interface Presenter {
		/**
		 */
		void enabledBluetooth(boolean option);
		
		/**

		 */
		void enabledAutoConnect(boolean option);
		
		/**

		 */
		void enabledAutoAnswer(boolean option);
		
		/**

		 */
		void setBtName(String name);
		
		/**

		 */
		void connectDevice(BtDevice device);
		
		/**

		 */
		void disconnectDevice();
		
		/**

		 */
		void startDiscovery();
		
		/**

		 */
		void stopDiscovery();
		
		/**

		 */
		void startGetPairedList();
		
		/**

		 */
		void deleteDevice(BtDevice device);
		
		/**

		 */
		void isBluetoothOpen();
		
		/**

		 */
		void isAutoConnectedOpen();
		
		/**

		 */
		void isAutoAnswerOpen();
		
		/**

		 */
		void getBluetoothName();
		
		/**

		 */
		void bindService();
		
		/**

		 */
		void unbindService();
		
		/**

		 */
		void registerBtCallback();
		
		/**

		 */
		void unregisterBtCallback();
		
		/**

		 */
		void getConnectedDevice();
		
	}

}
