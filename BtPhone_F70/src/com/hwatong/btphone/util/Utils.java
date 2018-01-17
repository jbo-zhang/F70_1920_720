package com.hwatong.btphone.util;

import java.util.List;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.hwatong.btphone.CallLog;
import com.hwatong.btphone.Contact;
import com.hwatong.btphone.activity.CallLogActivity;
import com.hwatong.btphone.activity.DialActivity;
import com.hwatong.btphone.bean.UICallLog;
import com.hwatong.btphone.ui.DrawableTextView;

/**
 * 
 * @author zxy time:2017年5月27日
 * 
 */
public class Utils {

	public static void gotoActivity(Context context, Class<?> cls) {
		Intent intent = new Intent(context, cls);
		context.startActivity(intent);
	}

	public static void gotoDialActivity(Context context, UICallLog callLog) {
		Intent intent = new Intent(context, DialActivity.class);
		intent.putExtra("call_log", callLog);
		context.startActivity(intent);
	}
	
	public static void gotoDialActivityInService(Context context, UICallLog callLog) {
		Intent intent = new Intent(context, DialActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("call_log", callLog);
		context.startActivity(intent);
	}
	
	public static void gotoCallLogActivityInService(Context context) {
		Intent intent = new Intent(context, CallLogActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("type", 0);
		context.startActivity(intent);
	}
	

	@SuppressLint("DefaultLocale")
	public static String getComFlg(String name) {
		if (TextUtils.isEmpty(name))
			return "";

		String result = getPingYin(name.trim());

		if (!result.equals("")) {
			String first = result.substring(0, 1).toUpperCase();
			if (!first.matches("[A-Z]")) {
				result = "#" + result.substring(1);
			}
		} else {
			result = "#";
		}

		return result;
	}
	
	
	public static String[] getPinyinAndFirstLetter(String hanzi) {
		if(TextUtils.isEmpty(hanzi)) {
			return new String[] {"",""};
		} 
		char[] hanziChars = hanzi.toCharArray();
		HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
		format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
		format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		format.setVCharType(HanyuPinyinVCharType.WITH_V);
		
		StringBuilder pinyin = new StringBuilder();
		StringBuilder firstLetters = new StringBuilder();
		
		for(int i = 0; i < hanziChars.length; i++) {
			//判断是否为汉字
			if(Character.toString(hanziChars[i]).matches("[\\u4E00-\\u9FA5]+")) {
				try {
					String[] temp = PinyinHelper.toHanyuPinyinStringArray(hanziChars[i], format);
					if(temp != null) {
						pinyin.append(temp[0]);
						firstLetters.append(temp[0].charAt(0));
					} 					
				} catch (BadHanyuPinyinOutputFormatCombination e) {
					e.printStackTrace();
				}
			} else {
				pinyin.append(Character.toString(hanziChars[i]).toUpperCase());
				firstLetters.append(Character.toString(hanziChars[i]).toUpperCase().charAt(0));
			}
		}
		
		return new String[]{pinyin.toString(), firstLetters.toString()};
	}
	
	

	@SuppressLint("DefaultLocale")
	public static String getPingYin(String src) {
		if (src == null || src.equals(""))
			return "";

		char[] t1 = src.toCharArray();
		String[] t2 = new String[t1.length];
		HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
		format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
		format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		format.setVCharType(HanyuPinyinVCharType.WITH_V);
		StringBuilder result = new StringBuilder("");
		int l = t1.length;
		try {
			for (int i = 0; i < l; i++) {
				//如果是汉字
				if (Character.toString(t1[i]).matches("[\\u4E00-\\u9FA5]+")) {
					t2 = PinyinHelper.toHanyuPinyinStringArray(t1[i], format);
					if (t2 != null)
						result.append(t2[0]);
				} else {
					result.append(Character.toString(t1[i]).toLowerCase());
				}
			}
			return result.toString().replaceAll(" ", "");
		} catch (BadHanyuPinyinOutputFormatCombination e1) {
			e1.printStackTrace();
		}
		return result.toString().replaceAll(" ", "");
	}

	/**
	 * 获取汉语字符串拼音首字母
	 * 
	 * @param chinese
	 * @return
	 */
	public static String getFirstSpell(String chinese) {
		StringBuffer pybf = new StringBuffer();
		char[] arr = chinese.toCharArray();
		HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
		defaultFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
		defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

		for (char curchar : arr) {
			if (curchar > 128) {
				try {
					String[] temp = PinyinHelper.toHanyuPinyinStringArray(
							curchar, defaultFormat);
					if (temp != null) {
						pybf.append(temp[0].charAt(0));
					}
				} catch (BadHanyuPinyinOutputFormatCombination e) {
					e.printStackTrace();
				}
			} else {
				pybf.append(curchar);
			}
		}
		return pybf.toString().replaceAll("\\W", "").trim();

	}

	public static void setTextViewGray(DrawableTextView view, boolean enable,
			Drawable[] newDrawables, int color) {
		view.setEnabled(enable);
//		view.setEnabled(true);
		view.setDrawablesSize(newDrawables);
		view.setTextColor(color);
	}

	public static String getlocalip(Context context) {
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		if (ipAddress == 0)
			return "未连接wifi";
		return ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "."
				+ (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));
	}

	public static int getPositionForSection(List<Contact> contacts, char c) {
		// return getPositionByBinnary(contacts, c, 0, contacts.size() - 1);
		return getPosition(contacts, c);
		// return findFirstEqual(contacts, c);
	}

	// 二分查找法 查找第一个相等的元素
	static int findFirstEqual(List<Contact> contacts, char c) {
		int left = 0;
		int right = contacts.size() - 1;

		// 这里必须是 <=
		while (left <= right) {
			int mid = (left + right) / 2;
			if (contacts.get(mid).comFlg.charAt(0) - c >= 0) {
				right = mid - 1;
			} else {
				left = mid + 1;
			}
		}
		if (left < contacts.size()
				&& contacts.get(left).comFlg.charAt(0) - c == 0) {
			return left;
		}

		return -1;
	}

	/**
	 * 普通查找法
	 * 
	 * @param contacts
	 * @param c
	 * @return
	 */
	private static int getPosition(List<Contact> contacts, char c) {
		// Log.d("AAA", "start=" + start + "  end=" + end);

		for (int i = 0; i < contacts.size(); i++) {
			if (contacts.get(i).comFlg.startsWith(String.valueOf(c))) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 二分查找法，查找 以 字符 c 开头的Contact 所在的位置
	 * 
	 * @param contacts
	 * @param c
	 * @return
	 */
	private static int getPositionByBinnary(List<Contact> contacts, char c,
			int start, int end) {
		// Log.d("AAA", "start=" + start + "  end=" + end);
		if (start > end) {
			return -1;
		}
		if (start == end || (end - start) == 1) {
			int index = contacts.get(start).comFlg
					.startsWith(String.valueOf(c)) ? start : -1;
			index = index == -1
					&& contacts.get(end).comFlg.startsWith(String.valueOf(c)) ? end
					: -1;
			return index;
		}

		int middle = (start + end) / 2;

		if (middle >= 0 && middle <= end) {
			char a = contacts.get(middle).comFlg.charAt(0);
			int result = a - c;
			if (result >= 0) {
				return getPositionByBinnary(contacts, c, start, middle);
			} else {
				return getPositionByBinnary(contacts, c, middle, end);
			}
		}

		return -1;
	}
	
	
	private static Toast mToast;
	public static void showToast(Context context, String msg) {
		if (mToast != null) {
			mToast.setText(msg);
			mToast.setDuration(Toast.LENGTH_SHORT);
		} else {
			mToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
		}
		mToast.show();
	}
	
	

}
