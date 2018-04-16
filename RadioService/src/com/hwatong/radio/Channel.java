package com.hwatong.radio;

import android.os.Parcel;
import android.os.Parcelable;

public class Channel implements Parcelable {
	public final int frequence;
	public final int from;

	public Channel(int freq, int from){
		this.frequence = freq;
		this.from = from;
	}

	public static final Parcelable.Creator<Channel> CREATOR = new Parcelable.Creator<Channel>() {
		public Channel createFromParcel(Parcel in) {
			return new Channel(in);
		}

		public Channel[] newArray(int size) {
			return new Channel[size];
		}
	};

	private Channel(Parcel in) {
		frequence = in.readInt();
		from = in.readInt();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(frequence);
		dest.writeInt(from);
	}

	@Override
	public int describeContents() {
		return 0;
	}
}
