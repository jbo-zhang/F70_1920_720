package com.hwatong.f70.baseview;

import android.app.Fragment;

public class BaseFragment extends Fragment{
	
	private OnFragmentImageChangedListener listener;
	private OnFragmentPausedListener mlFragmentPausedListener;
	
	private OnInputMethodHideListener onInputMethodHideListener;
	
	/**
	 * changed image which in the activity right
	 * display the matching image and screen
	 * @author ljw
	 *
	 */
	public interface OnFragmentImageChangedListener {
		/**
		 * 
		 * @param fragmentName
		 * send current fragment's class name
		 */
		void onFragmentChanged(String fragmentName);	
	}
	
	public interface OnFragmentPausedListener {
		void onFragmentPaused();
	}
	
	public void setOnFragmentPausedLiatener(OnFragmentPausedListener listener) {
		this.mlFragmentPausedListener = listener;
	}
	
	public void setOnFragmentImageChangedListener(OnFragmentImageChangedListener l) {
		this.listener = l;
	}
	
	public void changedActivityImage(String name) {
		if(listener != null)
			listener.onFragmentChanged(name);
	}
	
	
	
	@Override
	public void onPause() {
		super.onPause();
		if(mlFragmentPausedListener != null)
			mlFragmentPausedListener.onFragmentPaused();
	}



	/**
	 * if inputmethod show where is not allow, hide it
	 * @author ljw
	 *
	 */
	public interface OnInputMethodHideListener {
		void onHide();
	}
	
	
	public void setOnInputMethodHideListener(OnInputMethodHideListener listener) {
		this.onInputMethodHideListener = listener;
	}

	public void hideInputMethod() {
		if(onInputMethodHideListener != null) {
			onInputMethodHideListener.onHide();
		}
	}

}
