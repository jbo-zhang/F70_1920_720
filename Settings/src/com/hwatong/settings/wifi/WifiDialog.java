/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hwatong.settings.wifi;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.hwatong.settings.R;

class WifiDialog extends AlertDialog implements WifiConfigUiBase {
	static final int BUTTON_SUBMIT = DialogInterface.BUTTON_POSITIVE;
	static final int BUTTON_FORGET = DialogInterface.BUTTON_NEUTRAL;

	private final boolean mEdit;
	private final View.OnClickListener mListener;
	private final AccessPoint mAccessPoint;

	private View mView;
	private WifiConfigController mController;

	public WifiDialog(Context context, View.OnClickListener listener,
			AccessPoint accessPoint, boolean edit) {
		super(context, R.style.Theme_WifiDialog);
		mEdit = edit;
		mListener = listener;
		mAccessPoint = accessPoint;
	}

	@Override
	public WifiConfigController getController() {
		return mController;
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		super.show();
		Window w = getWindow();
		w.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		w.setContentView(R.layout.wifi_dialog);
		w.setLayout(685,319);
		mView = w.getDecorView();
		mView.getBackground().setAlpha(0);
		setInverseBackgroundForced(true);
		mController = new WifiConfigController(this, mView, mAccessPoint, mEdit);
		/* During creation, the submit button can be unavailable to determine
		 * visibility. Right after creation, update button visibility */
		mController.enableSubmitIfAppropriate();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//        mView = getLayoutInflater().inflate(R.layout.wifi_dialog, null);
		//        setView(mView);
		//        setInverseBackgroundForced(true);
		//        mController = new WifiConfigController(this, mView, mAccessPoint, mEdit);
		super.onCreate(savedInstanceState);
		/* During creation, the submit button can be unavailable to determine
		 * visibility. Right after creation, update button visibility */
		//        mController.enableSubmitIfAppropriate();
	}

	@Override
	public boolean isEdit() {
		return mEdit;
	}

	@Override
	public Button getSubmitButton() {
//		return getButton(BUTTON_SUBMIT);
		return (Button)mView.findViewById(R.id.button1);
	}

	@Override
	public Button getForgetButton() {
//		return getButton(BUTTON_FORGET);
		return (Button)mView.findViewById(R.id.button3);
	}

	@Override
	public Button getCancelButton() {
//		return getButton(BUTTON_NEGATIVE);
		return (Button)mView.findViewById(R.id.button2);
	}

	@Override
	public void setSubmitButton(CharSequence text) {
		Button btn = (Button)mView.findViewById(R.id.button1);
		if (btn!=null) {
			btn.setText(text);
			btn.setOnClickListener((View.OnClickListener)mListener);
			btn.setVisibility(View.VISIBLE);
		}
//		setButton(BUTTON_SUBMIT, text, mListener);
	}

	@Override
	public void setForgetButton(CharSequence text) {
		Button btn = (Button)mView.findViewById(R.id.button3);
		if (btn!=null) {
			btn.setText(text);
			btn.setOnClickListener((View.OnClickListener)mListener);
			btn.setVisibility(View.VISIBLE);
		}
//		setButton(BUTTON_FORGET, text, mListener);
	}

	@Override
	public void setCancelButton(CharSequence text) {
		Button btn = (Button)mView.findViewById(R.id.button2);
		if (btn!=null) {
			btn.setText(text);
			btn.setOnClickListener((View.OnClickListener)mListener);
			btn.setVisibility(View.VISIBLE);
		}
//		setButton(BUTTON_NEGATIVE, text, mListener);
	}
}
