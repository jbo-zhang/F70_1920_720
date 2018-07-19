package com.hwatong.btphone;

import android.os.Parcel;
import android.os.Parcelable;

public class CallStatus implements Parcelable {
	public static final String PHONE_CALL_NONE = "IDLE";
	public static final String PHONE_CALLING = "CALLING";
	public static final String PHONE_COMING = "COMING";
	public static final String PHONE_TALKING = "TALKING";

	public final String status;
	public final String number;
	public final String name;
	public final long duration;

	public CallStatus(String status, String number, String name, long duration) {
		this.status = status;
		this.number = number;
		this.name = name;
		this.duration = duration;
	}
	
	public static final Parcelable.Creator<CallStatus> CREATOR = new Parcelable.Creator<CallStatus>() {
		public CallStatus createFromParcel(Parcel in) {
			return new CallStatus(in);
		}

		public CallStatus[] newArray(int size) {
			return new CallStatus[size];
		}
	};

	private CallStatus(Parcel in) {
		status = in.readString();
		number = in.readString();
		name = in.readString();
		duration = in.readLong();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeString(status);
		dest.writeString(number);
		dest.writeString(name);
		dest.writeLong(duration);
	}

	public String toString() {
		return "status: " + status + ", number: " + number + ", name: " + name + ", duration: " + duration;
	}
}
