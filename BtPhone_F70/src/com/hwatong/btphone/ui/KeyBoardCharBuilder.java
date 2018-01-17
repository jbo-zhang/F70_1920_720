package com.hwatong.btphone.ui;

import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.hwatong.btphone.util.DailDTMF;

/**
 * 封装键盘响应数据的容器，可以绑定TextView及其子类，在view上显示点击字符
 * 
 * @author zxy time:2017年5月27日
 * 
 */
public class KeyBoardCharBuilder {
	private static final String TAG_PRE = KeyBoardCharBuilder.class.getSimpleName()+":";

	private SparseArray<Character> mLetterArray;

	private StringBuilder mSb;

//	private TextView mTv;
	private EditText mTv;

	private int maxLength = -1;

	public KeyBoardCharBuilder() {
		mLetterArray = new SparseArray<Character>();
		mSb = new StringBuilder();
	}

	public void initData(int[] keys, char[] values) {
		if (keys == null || values == null || keys.length != values.length) {
			return;
		}
		for (int i = 0; i < keys.length; i++) {
			mLetterArray.append(keys[i], values[i]);
		}
	}
	
	public char getChar(int id) {
		return mLetterArray.get(id);
	}

	public void append(int key, DailDTMF dtmf) {
		if (maxLength == -1 || mSb.length() <= maxLength) {
			mSb.append(mLetterArray.get(key));
			showNumber();
		}
	}

	public void deleteOne() {
		if (mSb.length() != 0) {
			mSb.delete(mSb.length() - 1, mSb.length());
			showNumber();
		}
	}

	public void deleteAll() {
		if (mSb.length() != 0) {
			mSb.delete(0, mSb.length());
			showNumber();
		}
	}

	public void bindTextView(EditText textView) {
		mTv = textView;
	}

	public void unBindTextView() {
		mTv = null;
	}

	private void showNumber() {
		if (mTv != null) {
			mTv.setVisibility(View.VISIBLE);
			mTv.setText(mSb.toString());
			mTv.setSelection(mSb.toString().length());
		}
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}

	@Override
	public String toString() {
		return mSb.toString();
	}

}
