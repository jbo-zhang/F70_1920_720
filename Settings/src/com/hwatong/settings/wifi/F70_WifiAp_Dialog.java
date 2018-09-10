package com.hwatong.settings.wifi;

import static android.net.wifi.WifiConfiguration.INVALID_NETWORK_ID;

import com.hwatong.f70.main.CHNUtils;
import com.hwatong.f70.main.LogUtils;
import com.hwatong.settings.R;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.InputFilter;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class F70_WifiAp_Dialog extends Dialog implements
		android.view.View.OnClickListener, TextWatcher, OnFocusChangeListener{

	private String title;
	private OnCustomDialogListener customDialogListener;
	EditText contentEdt;
	private TextView titleTv;
	private Button confirmBt, cancelBt;
	private CheckBox showPassword;
	private boolean isPassword;
	private View mView;
	private LinearLayout showPasswordLayout;
	private final Handler mTextViewChangedHandler;

	private String editContent;
	
	private static final int SSID_MIN = 3;
	private static final int PASSWORD_MIN = 8;
	
	// 定义回调事件，用于dialog的点击事件
	public interface OnCustomDialogListener {
		public void back(String name);
	}

	public F70_WifiAp_Dialog(Context context, String title, boolean isPassword,
			OnCustomDialogListener customDialogListener) {
		super(context, R.style.CustomDialog);
		this.title = title;
		this.customDialogListener = customDialogListener;
		this.isPassword = isPassword;
		mTextViewChangedHandler = new Handler();
	}

	@Override
	public void show() {
		Window window = this.getWindow();
		WindowManager.LayoutParams lp = this.getWindow().getAttributes();
		lp.dimAmount = 0.5f;
		window.setAttributes(lp);
		window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		super.show();
		/*
		 * During creation, the submit button can be unavailable to determine
		 * visibility. Right after creation, update button visibility
		 */
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
						| WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		setContentView(R.layout.f70_wifiapdialog);
		// 设置标题
		titleTv = (TextView) findViewById(R.id.wifi_title);
		contentEdt = (EditText) findViewById(R.id.password);
		CHNUtils.filterChinese(contentEdt);
		confirmBt = (Button) findViewById(R.id.button1);
		cancelBt = (Button) findViewById(R.id.button2);
		showPassword = (CheckBox) findViewById(R.id.show_password);
		showPasswordLayout = (LinearLayout) findViewById(R.id.show_password_layout);

		contentEdt.addTextChangedListener(this);
		if (isPassword) {
			contentEdt.setHint(R.string.wifi_ap_passwordtip);
			showPasswordLayout.setVisibility(View.VISIBLE);
		}
			enableSubmitIfAppropriate(isPassword ? PASSWORD_MIN : SSID_MIN);

		confirmBt.setOnClickListener(this);
		cancelBt.setOnClickListener(this);
		showPassword.setOnClickListener(this);

		titleTv.setText(title);
		
		contentEdt.setOnFocusChangeListener(this);
		
	}

	@Override
	public void onClick(View v) {
		int resId = v.getId();
		switch (resId) {
		case R.id.button1:
			if (customDialogListener != null)
				customDialogListener.back(contentEdt.getText().toString()
						.trim());
			this.dismiss();
			break;
		case R.id.button2:
			this.dismiss();
			break;

		case R.id.show_password:
			int pos = contentEdt.getSelectionEnd();
			contentEdt
					.setInputType(InputType.TYPE_CLASS_TEXT
							| (((CheckBox) v).isChecked() ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
									: InputType.TYPE_TEXT_VARIATION_PASSWORD));
			if (pos >= 0) {
				((EditText) contentEdt).setSelection(pos);
			}
			break;
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {

	}

	@Override
	public void afterTextChanged(Editable s) {
		LogUtils.d("afterTextChanged");
		mTextViewChangedHandler.post(new Runnable() {
			public void run() {
				enableSubmitIfAppropriate(isPassword ? PASSWORD_MIN : SSID_MIN);
			}
		});
	}

	void enableSubmitIfAppropriate(int type) {
		if (confirmBt == null)
			return;

		boolean invalid = false;

		if (contentEdt != null && contentEdt.getText().toString().length() >= type) {
			invalid = true;
		}
		LogUtils.d("passwordInvalid: " + invalid + "hint: " + contentEdt.getText().toString().length());
		// LJW F70
//		confirmBt.setTextColor(passwordInvalid ? Color.WHITE : Color.BLACK);
		confirmBt.setEnabled(invalid);
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		LogUtils.d("F70_WifiAp_Dialog focus is changed!");
		if(!hasFocus)
			this.dismiss();
	}
	
	public void clearFocus() {
		this.contentEdt.clearFocus();
	}
	
	public void setEditContent(String content) {
		if(!TextUtils.isEmpty(content))
			this.contentEdt.setText(content);
	}

}
