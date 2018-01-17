package com.hwatong.radio;


import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.WindowManager.LayoutParams;

import com.hwatong.radio.ui.R;


public class CustomDialog extends Dialog {
	public CustomDialog(Context context, String strMessage) {
		this(context, R.style.CustomDialog, strMessage);
	}

	public CustomDialog(Context context, int theme, String strMessage) {
		super(context, theme);
		this.setContentView(R.layout.update_progress);
		this.getWindow().setFlags(LayoutParams.FLAG_NOT_TOUCH_MODAL, LayoutParams.FLAG_NOT_TOUCH_MODAL);
		this.getWindow().getAttributes().gravity = Gravity.RIGHT | Gravity.TOP;
		// TextView tvMsg = (TextView) this.findViewById(R.id.txt_radio_search);
		// if (tvMsg != null) {
		// tvMsg.setText(strMessage);
		// }
	}

//	private Handler handler = new Handler();
//	private Runnable dismissDialog = new Runnable() {
//
//		@Override
//		public void run() {
//			// TODO Auto-generated method stub
//			dismiss();
//		}
//	};

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {

		if (!hasFocus) {
			dismiss();
		}
	}
}
