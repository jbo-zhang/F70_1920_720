package com.hwatong.btphone;

import android.os.Parcel;
import android.os.Parcelable;

public class Contact implements Parcelable {
	public static final String TYPE_SIM = "SIM";
	public static final String TYPE_PHONE = "PHONE";

	public final String type;
	public final String name;
	public final String number;
	/**
	 * full pinyin of name
	 */
	public final String comFlg;

	public Contact(String type, String name, String number, String comFlg) {
		this.type = type;
		this.name = name;
		this.number = number;
		this.comFlg = comFlg;
	}

	public static final Parcelable.Creator<Contact> CREATOR = new Parcelable.Creator<Contact>() {
		public Contact createFromParcel(Parcel in) {
			return new Contact(in);
		}

		public Contact[] newArray(int size) {
			return new Contact[size];
		}
	};

	private Contact(Parcel in) {
		type = in.readString();
		name = in.readString();
		number = in.readString();
		comFlg = in.readString();
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
		dest.writeString(comFlg);
	}

	public static class Comparator implements java.util.Comparator<Contact> {
		@Override
		public int compare(Contact lhs, Contact rhs) {
			if ( (lhs.comFlg.startsWith("#") && !rhs.comFlg.startsWith("#"))
					|| (!lhs.comFlg.startsWith("#") && rhs.comFlg.startsWith("#")) )
				return rhs.comFlg.compareToIgnoreCase(lhs.comFlg);
			return lhs.comFlg.compareToIgnoreCase(rhs.comFlg);
		}
	}

}
