package com.hwatong.utils;

import java.text.DecimalFormat;

import com.hwatong.radio.ui.Radio;

public class Utils {
	public static String numberToString(int frequence) {
		if(frequence < Radio.MIN_FREQUENCE_FM) {
			frequence = Radio.MIN_FREQUENCE_FM;
		} else if(frequence > Radio.MAX_FREQUENCE_FM) {
			frequence = Radio.MAX_FREQUENCE_FM;
		}
		
		double f = frequence / 100.0;
		DecimalFormat df = new DecimalFormat("#.0");
		return df.format(f);
	}

	public static String getBandText(int band) {
		String bandText = "";
		switch (band) {
		case 1:
			bandText = "FM1";
			break;
		case 2:
			bandText = "FM2";
			break;
		case 3:
			bandText = "FM3";
			break;
		case 4:
			bandText = "AM1";
			break;
		case 5:
			bandText = "AM2";
			break;
		}
		return bandText;
	}
}
