package com.hwatong.bt;

import android.os.Parcel;
import android.os.Parcelable;

public class BtDevice implements Parcelable {
	public final String name;
	public final String addr;
	public int profiles;

	public BtDevice(String name, String addr) {
		this.name = name;
		this.addr = addr;
	}
	
	public BtDevice(String name, String addr, int profiles) {
		this.name = name;
		this.addr = addr;
		this.profiles = profiles;
	}
	
	public BtDevice(BtDevice dev) {
		this.name = dev.name;
		this.addr = dev.addr;
		this.profiles = dev.profiles;
	}

	public static final Parcelable.Creator<BtDevice> CREATOR = new Parcelable.Creator<BtDevice>() {
		public BtDevice createFromParcel(Parcel in) {
			return new BtDevice(in);
		}

		public BtDevice[] newArray(int size) {
			return new BtDevice[size];
		}
	};

	private BtDevice(Parcel in) {
		name = in.readString();
		addr = in.readString();
		profiles = in.readInt();
	}	

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeString(name);
		dest.writeString(addr);
		dest.writeInt(profiles);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null)
			return false;
		if (getClass() != o.getClass())
			return false;
		BtDevice other = (BtDevice) o;
		if (addr != null && addr.equals(other.addr)) {
			return true;
		}

		return false;
	}
	
	@Override
	public String toString() {
		return String.format("{%s %s}", name, addr);
	}
}
