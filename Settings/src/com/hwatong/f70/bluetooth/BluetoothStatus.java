package com.hwatong.f70.bluetooth;

public enum BluetoothStatus {
	ON, TURN_ON, OFF, TRUN_OFF;
	
	BluetoothStatus status;

	public BluetoothStatus getBluetoothStatus() {
		return status;
	}
	
	public void setBluetoothStatus(BluetoothStatus status) {
		this.status = status;
	}
}
