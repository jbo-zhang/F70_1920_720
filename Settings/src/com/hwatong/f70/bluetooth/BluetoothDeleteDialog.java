package com.hwatong.f70.bluetooth;

import com.hwatong.settings.R;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Administrator on 2016/9/3 0003.
 */
public class BluetoothDeleteDialog extends Dialog {
	private TextView titletip;
	private Button positiveButton; 
	private Button negativeButton; 

	/**
	 * @param context
	 */
	public BluetoothDeleteDialog(Context context) {
		super(context, R.style.CustomDialog); 
		setCustomView();
	}

	public BluetoothDeleteDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, R.style.CustomDialog);
		this.setCancelable(cancelable);
		this.setOnCancelListener(cancelListener);
		setCustomView();
	}

	public BluetoothDeleteDialog(Context context, int theme) {
		super(context, R.style.CustomDialog);
		setCustomView();
	}

	@Override
	public void setTitle(CharSequence title) {
		titletip.setText(title);
	}

	/**
	 * 
	 */
	private void setCustomView() {
		View mView = LayoutInflater.from(getContext()).inflate(
				R.layout.f70_bluetooth_delete_dialog, null);
		titletip = (TextView) mView.findViewById(R.id.delete_name);
		positiveButton = (Button) mView.findViewById(R.id.dialog_confirm);
		negativeButton = (Button) mView.findViewById(R.id.dialog_quit);
		super.setContentView(mView);
	}

	/**
	 * 
	 * 
	 * @param text
	 */
	// public void setPositiveText(CharSequence text){
	// positiveButton.setText(text);
	// }
	/**
	 * 
	 * 
	 * @param text
	 */
	// public void setNegativeText(CharSequence text){
	// negativeButton.setText(text);
	// }

	/**
	 * 
	 * 
	 * @param listener
	 */
	public void setOnPositiveListener(View.OnClickListener listener) {
		positiveButton.setOnClickListener(listener);
	}

	/**
	 * 
	 * 
	 * @param listener
	 */
	public void setOnNegativeListener(View.OnClickListener listener) {
		negativeButton.setOnClickListener(listener);
	}

	@Override
    public void show() {
    	Window window = this.getWindow();
    	WindowManager.LayoutParams lp = this.getWindow().getAttributes();
    	lp.dimAmount = 0.5f;
    	window.setAttributes(lp);
    	window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    	super.show();
    }
}
