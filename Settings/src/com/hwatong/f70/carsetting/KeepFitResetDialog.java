package com.hwatong.f70.carsetting;

import com.hwatong.settings.R;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class KeepFitResetDialog extends Dialog{

	private Button positiveButton; // 确定按钮
	private Button negativeButton; // 取消按钮

	/**
	 * @param context
	 */
	public KeepFitResetDialog(Context context) {
		super(context, R.style.CustomDialog); // 自定义style主要去掉标题，标题将在setCustomView中自定义设置
		setCustomView();
	}

	public KeepFitResetDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, R.style.CustomDialog);
		this.setCancelable(cancelable);
		this.setOnCancelListener(cancelListener);
		setCustomView();
	}

	public KeepFitResetDialog(Context context, int theme) {
		super(context, R.style.CustomDialog);
		setCustomView();
	}

	/**
	 * 设置整个弹出框的视图
	 */
	private void setCustomView() {
		View mView = LayoutInflater.from(getContext()).inflate(
				R.layout.f70_keepfit_confirm_dialog, null);
		positiveButton = (Button) mView.findViewById(R.id.dialog_confirm);
		negativeButton = (Button) mView.findViewById(R.id.dialog_quit);
		super.setContentView(mView);
	}

	/**
	 * 设置确定键文�?
	 * 
	 * @param text
	 */
	// public void setPositiveText(CharSequence text){
	// positiveButton.setText(text);
	// }
	/**
	 * 设置取消键文�?
	 * 
	 * @param text
	 */
	// public void setNegativeText(CharSequence text){
	// negativeButton.setText(text);
	// }

	/**
	 * 确定键监听器
	 * 
	 * @param listener
	 */
	public void setOnPositiveListener(View.OnClickListener listener) {
		positiveButton.setOnClickListener(listener);
	}

	/**
	 * 取消键监听器
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
