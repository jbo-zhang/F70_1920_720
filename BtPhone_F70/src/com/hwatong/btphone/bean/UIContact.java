package com.hwatong.btphone.bean;

import java.util.Comparator;

import android.os.Parcel;
import android.os.Parcelable;

import com.hwatong.btphone.Contact;
import com.hwatong.btphone.util.L;

public class UIContact implements Parcelable, Comparable<UIContact> {
	public String name = "";
	public String number = "";
	/**
	 * full pinyin of name
	 */
	public String comFlg = "";
	public String firstLetters = "";

	public UIContact() {}

	public UIContact(String name, String number, String comFlg, String firstLetters) {
		this.name = name;
		this.number = number;
		this.comFlg = comFlg;
		this.firstLetters = firstLetters;
		L.dRoll("[UIContact]", "name: " + name + " number: " + number + " comFlg: " + comFlg + " firstLetters: " + firstLetters);
	}

	public static class ContactComparator implements Comparator<Contact> {
		@Override
		public int compare(Contact lhs, Contact rhs) {
			int nameCompare = 1;
			nameCompare = lhs.comFlg.compareToIgnoreCase(rhs.comFlg);
			if(nameCompare == 0) {
				return lhs.number.compareToIgnoreCase(rhs.number);
			} else {
				return nameCompare;
			}
		}
	}


	@Override
	public int compareTo(UIContact another) {
		int nameCompare = 1;
		if (this.comFlg.startsWith("#") || another.comFlg.startsWith("#")) {
			nameCompare = another.comFlg.compareToIgnoreCase(this.comFlg);
			if(nameCompare == 0) {
				return another.number.compareToIgnoreCase(this.number);
			} else {
				return nameCompare;
			}
		}
		nameCompare = this.comFlg.compareToIgnoreCase(another.comFlg);
		if(nameCompare == 0 ) {
			return this.number.compareToIgnoreCase(another.number);
		} else {
			return nameCompare;
		}
		
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof UIContact){
			UIContact UIContact = (UIContact) o;
			return name.equals(UIContact.name) && number.equals(UIContact.number);
		}
		return super.equals(o);
	}
	

	
	public static final Parcelable.Creator<UIContact> CREATOR = new Parcelable.Creator<UIContact>() {
		public UIContact createFromParcel(Parcel in) {
			return new UIContact(in);
		}

		public UIContact[] newArray(int size) {
			return new UIContact[size];
		}
	};

	private UIContact(Parcel in) {
		readFromParcel(in);
	}

	public void readFromParcel(Parcel in) {
		name = in.readString();
		number = in.readString();
		comFlg = in.readString();
		firstLetters = in.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(number);
		dest.writeString(comFlg);
		dest.writeString(firstLetters);
	}
	
	
	
}
