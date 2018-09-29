package com.hwatong.btmusic;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.os.Parcel;
import android.os.Parcelable;

public class NowPlaying implements Parcelable {
    private final Map<String, String> map = new HashMap<String, String>();

	public NowPlaying() {
	}
	
    public String set(String key, String value) {
        return map.put(key, value);
    }

    public String get(String key) {
        return map.get(key);
    }

	public static final Parcelable.Creator<NowPlaying> CREATOR = new Parcelable.Creator<NowPlaying>() {
		public NowPlaying createFromParcel(Parcel in) {
			return new NowPlaying(in);
		}

		public NowPlaying[] newArray(int size) {
			return new NowPlaying[size];
		}
	};

	private NowPlaying(Parcel in) {
		in.readMap(map, HashMap.class.getClassLoader());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeMap(map);
	}
	
	@Override
	public String toString() {
		  java.util.Map.Entry entry;  
		  StringBuffer sb = new StringBuffer();  
		  for(Iterator iterator = map.entrySet().iterator(); iterator.hasNext();)  
		  {  
		    entry = (java.util.Map.Entry)iterator.next();  
		      sb.append(entry.getKey().toString()).append( "'" ).append(null==entry.getValue()?"":  
		      entry.getValue().toString()).append (iterator.hasNext() ? "^" : "");  
		  }  
		  return sb.toString();
	}
}
