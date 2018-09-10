package com.hwatong.settings.wallpaper;


import com.hwatong.settings.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;



public class Utils {
	public static final String UDISK_PATH_BROADCAST = "file:///mnt/udisk";
	public static final String UDISK_PATH = "/mnt/udisk";
	public static final String SDCARD_PATH_BROADCAST = "file:///mnt/udisk2";
	public static final String SDCARD_PATH = "/mnt/udisk2";
	//	public static final String SDCARD_PATH_BROADCAST = "file:///mnt/extsd";
	//	public static final String SDCARD_PATH = "/mnt/extsd";
	public static final String LOCAL_PATH = "/mnt";

	public static final String FILE_PATH = "file://";
	
	public static final boolean DEBUG = true;
	
	public static OnListChangeListener OnListChangeClickListener;
	public interface OnListChangeListener {
		public void ListChange(int i,String s);
		public void newItentChange();
	}
	public static void setOnPictureListChangeListener(OnListChangeListener listener) {
		OnListChangeClickListener = listener;
	}

	/**
	 * 鏍煎紡鍖栨椂闂� 鏍煎紡涓�  00:00  0:00:00
	 * @param milliseconds
	 * @return
	 */
	public static String formatetime(int milliseconds)
	{
		String hour=String.valueOf(milliseconds/3600000);
		String minute=String.valueOf((milliseconds%3600000)/60000);
		String second=String.valueOf(((milliseconds%3600000)%60000)/1000);
		hour=deal(hour);
		minute=deal(minute);
		second=deal(second);
		if(hour == "00") return minute+":"+second;
		return hour+":"+minute+":"+second;
	}

	/**
	 * 杞崲鏃堕棿  1  11  涓�  01 11
	 * @param time
	 * @return
	 */
	private static String deal(String time)
	{
		if(time.length()==1)
		{
			if(time.equals("0")) time="00";
			else time="0"+time;
		}
		return time;
	}

	public static String getStringForChinese(String value) {
		if(value == null || value.equals(""))	return "";
		String newValue = value;
		try {
			if (value.equals(new String(value.getBytes("ISO-8859-1"),
					"ISO-8859-1"))) {
				newValue = new String(value.getBytes("ISO-8859-1"), "GBK");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newValue;
	}

//	public static String getComFlg(String name) {
//		if(name == null || name.equals(""))	return "";
//
//		String result = getPingYin(name.substring(0, 1));
//
//		if(!result.equals("")) {
//			result = result.substring(0, 1).toUpperCase();
//			if(!result.matches("[A-Z]")) {
//				result = "#";
//			}
//		} else {
//			result = "#";
//		}
//
//		return result;
//	}
//
//	public static String getPingYin(String src) {
//		if(src == null || src.equals(""))	return "";
//
//		char[] t1 = null;
//		t1 = src.toCharArray();
//		String[] t2 = new String[t1.length];
//		HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
//		format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
//		format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
//		format.setVCharType(HanyuPinyinVCharType.WITH_V);
//		StringBuilder result = new StringBuilder("");
//		int l = t1.length;
//		try {
//			for (int i = 0; i < l; i++) {
//
//				if (Character.toString(t1[i]).matches("[\\u4E00-\\u9FA5]+")) {
//					t2 = PinyinHelper.toHanyuPinyinStringArray(t1[i], format);
//					if(t2 != null)	result.append(t2[0]);					
//				} else {
//					result.append(Character.toString(t1[i]).toLowerCase());
//				}
//			}
//			return result.toString().replaceAll(" ", "");
//		} catch (BadHanyuPinyinOutputFormatCombination e1) {
//			e1.printStackTrace();
//		}
//		return result.toString().replaceAll(" ", "");
//	}

	/**
	 * 甯歌鐨勪竴浜涚浉鍚岀洰褰曪紝閮借浆鎹㈡垚MediaStore鐨勮矾寰�
	 */
	public static String converPath(String path) {
		if(path == null || path.equals("")) return "";
		String result = path;
		if (path.startsWith("/udisk")) {
			result = path.replace("/udisk", "/mnt/udisk");
		} else if (path.startsWith("/extsd")) {
			result = path.replace("/extsd", "/mnt/extsd");
		} else {
			final String[] sdcard = { "/sdcard", "/mnt/sdcard",
					"/storage/sdcard0", "/storage/emulated/legacy" };
			for (String s : sdcard) {
				if (path.startsWith(s)) {
					result = path.replace(s, "/storage/emulated/0");
					break;
				}
			}
		}

		return result;
	}

    /**
     *杞崲 澶у皬鏄剧ず
     * @param size
     * @return
     */
    public static String convertStorage(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else
            return String.format("%d B", size);
    }
    public static final int TYPE_FILE = 0;
	public static final int TYPE_MUSIC = 1;
	public static final int TYPE_PICTURE = 3;
	public static final int TYPE_VIDEO = 2;
	public static final int TYPE_OTHER = 4;
	public static final int TYPE_BACK = 5;

	public static boolean checkFileType(String fileName, String[] extendNames) {
		if(fileName == null) return false;
		for (String end : extendNames) {
			if(fileName.toLowerCase().endsWith(end)){
				return true;
			}
		}
		return false;
	}

	public static String getExtFromFilename(String filename) {
		if(filename == null) return "";
		int dotPosition = filename.lastIndexOf('.');
		int dotPosition2 = filename.lastIndexOf('/');
		if (dotPosition != -1 && dotPosition2 != -1 && Math.abs(dotPosition2-dotPosition) > 1) {
			return filename.substring(filename.lastIndexOf("/") + 1,filename.lastIndexOf("."));
		}
		return "";
	} 
	
	public static String getNameFromFilename(String filename) {
		if(filename == null) return "";
        int dotPosition = filename.lastIndexOf('.');
        if (dotPosition != -1) {
            return filename.substring(0, dotPosition);
        }
        return filename;
    }
	
	public static String getTypeFromFilename(String filename) {
		if(filename == null) return "";
        int dotPosition = filename.lastIndexOf('.');
        if (dotPosition != -1) {
            return filename.substring(dotPosition+1, filename.length());
        }
        return "";
    }

	//add by lcb at 20161111锛屼繚瀛樻渶鍚庣殑鐣岄潰
	private static final String PREF_FILE="usb_media_player";
	private static final String LAST_TAB="last_tab";
	public static void saveLastState(Context ctxt, String tab) {
		Log.d("USBMediaPlayer", "saveLastState: tab=" + tab);
		SharedPreferences pref = ctxt.getSharedPreferences(PREF_FILE,Context.MODE_PRIVATE);
		pref.edit().putString(LAST_TAB, tab).commit();
	}

	public static String getLastState(Context ctxt) {
		SharedPreferences pref = ctxt.getSharedPreferences(PREF_FILE,Context.MODE_PRIVATE);
		return pref.getString(LAST_TAB, "music");
	}	
	//add end
}

