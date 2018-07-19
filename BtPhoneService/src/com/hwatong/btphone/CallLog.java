package com.hwatong.btphone;

import android.os.Parcel;
import android.os.Parcelable;

public class CallLog implements Parcelable {
	//type是从蓝牙读出的值
	public static final String TYPE_CALL_OUT = "4";
	public static final String TYPE_CALL_IN = "5";
	public static final String TYPE_CALL_MISS = "6";

	public final String type;
	public final String name;
	public final String number;
	public final String date;
	public final String token;

	public CallLog(String type, String name, String number, String date) {
		this.type = type;
		this.name = name;
		this.number = number;
		this.date = date;
		this.token = type + this.toString();
	}

	public static class Comparator implements java.util.Comparator<CallLog> {
		@Override
		public int compare(CallLog arg0, CallLog arg1) {
			return arg1.date.compareToIgnoreCase(arg0.date);
		}
	}

	public static final Parcelable.Creator<CallLog> CREATOR = new Parcelable.Creator<CallLog>() {
		public CallLog createFromParcel(Parcel in) {
			return new CallLog(in);
		}

		public CallLog[] newArray(int size) {
			return new CallLog[size];
		}
	};

	private CallLog(Parcel in) {
		type = in.readString();
		name = in.readString();
		number = in.readString();
		date = in.readString();
		token = in.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(type);
		dest.writeString(name);
		dest.writeString(number);
		dest.writeString(date);
		dest.writeString(token);
	}
}
