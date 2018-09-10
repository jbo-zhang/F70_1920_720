/*
 * Copyright (C) 2012 The Android Open Source Project
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
package com.hwatong.settings;

import com.hwatong.settings.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Dialog to show WPS progress.
 */
public class InfoDialog extends AlertDialog {
    private final static String TAG = "InfoDialog";
    
	static final int BUTTON_SUBMIT = DialogInterface.BUTTON_POSITIVE;
	static final int BUTTON_NEGATIVE = DialogInterface.BUTTON_NEGATIVE;

    private View mView;

	private final View.OnClickListener mListener;
    
    public InfoDialog(Context context, View.OnClickListener listener) {
        super(context);
		mListener = listener;
    }
	@Override
	public void show() {
		super.show();
        Window win = getWindow();
        win.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        win.setContentView(R.layout.info_dialog);
        mView = win.getDecorView();
	}
	
	@Override
	public void setTitle(CharSequence title) {
		super.setTitle(title);
		TextView textview = (TextView)mView.findViewById(R.id.alertTitle);
		if (textview!=null) textview.setText(title);
	}
	@Override
	public void setTitle(int titleId) {
		// TODO Auto-generated method stub
		super.setTitle(titleId);
		TextView textview = (TextView)mView.findViewById(R.id.alertTitle);
		if (textview!=null) textview.setText(titleId);
	}
	@Override
	public void setMessage(CharSequence message) {
		super.setMessage(message);
		TextView textview = (TextView)mView.findViewById(R.id.message);
		if (textview!=null) textview.setText(message);
		mView.findViewById(R.id.contentPanel).setVisibility(View.VISIBLE);
	}
	private EditText mInputText;
	public void setInput(CharSequence title) {
		TextView textview = (TextView)mView.findViewById(R.id.message);
		if (textview!=null) textview.setText(title);
		mInputText = (EditText)mView.findViewById(R.id.inputText);
		mView.findViewById(R.id.inputPanel).setVisibility(View.VISIBLE);
	}
	public String getInput() { return (mInputText!=null)?mInputText.getText().toString():"";}
	public Button getSubmitButton() {
		return (Button)mView.findViewById(R.id.button1);
	}

	public Button getCancelButton() {
		return (Button)mView.findViewById(R.id.button2);
	}

	public void setSubmitButton(CharSequence text) {
		Button btn = (Button)mView.findViewById(R.id.button1);
		if (btn!=null) {
			btn.setText(text);
			btn.setOnClickListener((View.OnClickListener)mListener);
			btn.setVisibility(View.VISIBLE);
		}
	}

	public void setCancelButton(CharSequence text) {
		Button btn = (Button)mView.findViewById(R.id.button2);
		if (btn!=null) {
			btn.setText(text);
			btn.setOnClickListener((View.OnClickListener)mListener);
			btn.setVisibility(View.VISIBLE);
		}
	}
}
