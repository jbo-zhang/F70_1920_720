package com.hwatong.btphone.bean;

import java.util.Comparator;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.hwatong.btphone.CallLog;

/**
 * 一条通话记录
 * @author zhangjinbo
 *
 */
public class UICallLog implements Parcelable {
	public static final int TYPE_CALL_OUT = 4;
	public static final int TYPE_CALL_IN = 5;
	public static final int TYPE_CALL_MISS = 6;

	public int type = TYPE_CALL_OUT;
	public String name = "";
	public String number = "";
	public String date = "";
//	public String comFlg = "";
//	public String firstLetters = "";
	public long duration ;
	public String dtmfStr = "";
	
	public byte shouldJump;

	public UICallLog() {}

	public UICallLog(int type, String name, String number, String date) {
		this.type = type;
		this.name = name;
		this.number = number;
		this.date = date;
//		String[] str = Utils.getPinyinAndFirstLetter(name);
//		this.comFlg = str[0];
//		this.firstLetters = str[1];
//		comFlg = "".equals(comFlg) ? "#" : comFlg;
	}

	public static class UICallLogComparator implements Comparator<UICallLog> {
		@Override
		public int compare(UICallLog arg0, UICallLog arg1) {
			Log.d("BTPhone", "compare : " + arg0.date + " " + arg1.date);
			return arg1.date.compareToIgnoreCase(arg0.date);
		}
	}
	
	public static class CallLogComparator implements Comparator<CallLog> {
		@Override
		public int compare(CallLog arg0, CallLog arg1) {
			Log.d("BTPhone", "compare : " + arg0.date + " " + arg1.date);
			return arg1.date.compareToIgnoreCase(arg0.date);
		}
	}
	

//	@Override
//	public int compareTo(UICallLog another) {
//		if (this.comFlg.startsWith("#") || another.comFlg.startsWith("#")) {
//			return another.comFlg.compareToIgnoreCase(this.comFlg);
//		}
//		return this.comFlg.compareToIgnoreCase(another.comFlg);
//	}
	

	public static final Parcelable.Creator<UICallLog> CREATOR = new Parcelable.Creator<UICallLog>() {
		public UICallLog createFromParcel(Parcel in) {
			return new UICallLog(in);
		}

		public UICallLog[] newArray(int size) {
			return new UICallLog[size];
		}
	};

	
	@Override
	public int describeContents() {
		return 0;
	}

	private UICallLog(Parcel in) {
		readFromParcel(in);
	}

	public void readFromParcel(Parcel in) {
		type = in.readInt();
		name = in.readString();
		number = in.readString();
		date = in.readString();
//		comFlg = in.readString();
//		firstLetters = in.readString();
		duration = in.readLong();
		dtmfStr = in.readString();
		shouldJump = in.readByte();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(type);
		dest.writeString(name);
		dest.writeString(number);
		dest.writeString(date);
//		dest.writeString(comFlg);
//		dest.writeString(firstLetters);
		dest.writeLong(duration);
		dest.writeString(dtmfStr);
		dest.writeByte(shouldJump);
	}
	
	
}
