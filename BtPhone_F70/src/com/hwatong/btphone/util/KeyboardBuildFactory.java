package com.hwatong.btphone.util;

import android.app.Activity;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.text.Editable;
import android.util.Log;
import android.widget.EditText;

import com.hwatong.btphone.ui.R;
import com.hwatong.btphone.ui.R.drawable;

public class KeyboardBuildFactory {
	private static final String TAG = KeyboardBuildFactory.class.getSimpleName();
	private Activity mActivity = null;
	private KeyboardView mKeyboardView = null;
	private EditText mEditText = null;
	private OnKeyEventCallBack mOnKeyEventCallBack = null;

	private Key mKeyConfirm;

	public static KeyboardBuildFactory bindKeyboard(Activity activity, KeyboardView keyboardView, int xmlLayoutResId,
			EditText editText) {
		return new KeyboardBuildFactory(activity, keyboardView, xmlLayoutResId, editText);
	}

	private KeyboardBuildFactory(Activity activity, KeyboardView keyboardView, int xmlLayoutResId, EditText editText) {
		this.mActivity = activity;
		this.mKeyboardView = keyboardView;
		this.mEditText = editText;
		final Keyboard keyboard = new Keyboard(mActivity, xmlLayoutResId);
		mKeyboardView.setKeyboard(keyboard);
		mKeyboardView.setPreviewEnabled(false);
		bindEdit(editText);
		for (Key key : keyboard.getKeys()) {
			if (key.codes[0] == -3) {// 确定键
				mKeyConfirm = key;
				break;
			}
		}
		OnKeyboardActionListener actionListener = new OnKeyboardActionListener() {
			@Override
			public void swipeUp() {

			}

			@Override
			public void swipeRight() {

			}

			@Override
			public void swipeLeft() {

			}

			@Override
			public void swipeDown() {

			}

			@Override
			public void onText(CharSequence text) {

			}

			@Override
			public void onRelease(int primaryCode) {
				if (primaryCode == -3) {
					mKeyConfirm.icon = mActivity.getResources().getDrawable(R.drawable.keyboard_btn_confirm_normal);
				}
			}

			@Override
			public void onPress(int primaryCode) {
				if (primaryCode == -3) {
					mKeyConfirm.icon = mActivity.getResources().getDrawable(R.drawable.keyboard_btn_confirm_pressed);
				}
			}

			@Override
			public void onKey(int primaryCode, int[] keyCodes) {
				 Log.d(TAG, "key:" + primaryCode);

				if (mOnKeyEventCallBack != null) {
					mOnKeyEventCallBack.onKeyAction(primaryCode);
				}

				if (mEditText == null) {
					return;
				}

				int start = mEditText.getSelectionStart();
				Editable editable = mEditText.getText();
				if (primaryCode == -2 && editable.length() != 0 && start != 0) {// 删除
					editable.delete(start - 1, start);
				} else if (primaryCode != -1 && primaryCode != -3 && primaryCode != -2) {
					editable.insert(start, Character.toString((char) primaryCode));
				}

			}
		};
		mKeyboardView.setOnKeyboardActionListener(actionListener);

	}

	private void bindEdit(EditText editText) {
		if (editText == null) {
			return;
		}
		// editText.setOnFocusChangeListener(new OnFocusChangeListener() {
		// @Override
		// public void onFocusChange(View v, boolean hasFocus) {
		// if (hasFocus) {
		// showKeyBoard(v);
		// } else {
		// // hideKeyBoard();
		// }
		// }
		// });
		// editText.setOnTouchListener(new OnTouchListener() {
		//
		// @Override
		// public boolean onTouch(View v, MotionEvent event) {
		// showKeyBoard(v);
		// return false;
		// }
		// });
		// editText.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// showKeyBoard(v);
		// }
		// });
	}

	/**
	 * 隐藏自定义输入法界面
	 */
	// private void hideKeyBoard() {
	// mKeyboardView.setVisibility(View.GONE);
	// mKeyboardView.setEnabled(false);
	// }

	/**
	 * 显示自定义输入法界面
	 * 
	 * @param v
	 */
	// private void showKeyBoard(View v) {
	// mKeyboardView.setVisibility(View.VISIBLE);
	// mKeyboardView.setEnabled(true);
	// if (mActivity == null) {
	// return;
	// }
	// InputMethodManager inputMethodManager = (InputMethodManager) mActivity
	// .getSystemService(Activity.INPUT_METHOD_SERVICE);
	// inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
	// }

	public void setOnKeyEventCallBack(OnKeyEventCallBack onKeyEventCallBack) {
		this.mOnKeyEventCallBack = onKeyEventCallBack;
	}

	public interface OnKeyEventCallBack {
		public void onKeyAction(int primaryCode);
	}

}
