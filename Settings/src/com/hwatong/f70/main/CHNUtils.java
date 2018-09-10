package com.hwatong.f70.main;

import android.text.InputFilter;
import android.text.Spanned;
import android.widget.TextView;

public class CHNUtils {
	
	public static void filterChinese(TextView v) {
	    v.setFilters(new InputFilter[]{new InputFilter() {
	        @Override
	        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
	            if (null != source && isChinese(source.toString())) return "";
	            return source;
	        }
	    }});
	}

	// 完整的判断中文汉字和符号
	@SuppressWarnings("unused")
	private static boolean isChinese(String strName) {
	    char[] ch = strName.toCharArray();
	    for (char c : ch) {
	        if (isChinese(c)) {
	            return true;
	        }
	    }
	    return false;
	}

	// 根据Unicode编码完美的判断中文汉字和符号
	private static boolean isChinese(char c) {
	    Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
	    return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
	            || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
	            || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
	            || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
	            || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
	            || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
	            || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION;
	}
}
