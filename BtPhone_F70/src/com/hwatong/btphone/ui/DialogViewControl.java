package com.hwatong.btphone.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hwatong.btphone.util.L;

/**
 * 用于更新提示信息的切换和显示
 * 
 * @author zxy time:2017年6月2日
 * 
 */
public class DialogViewControl {

	private static final String thiz = DialogViewControl.class.getSimpleName();
	
	private ProgressBar mProgressBar;

	private TextView mTvDialogMsg;

	private Dialog mDialog;

	private boolean isShow;

	private Context mContext;

	private View rootView;

	public DialogViewControl(Context context) {
		this.mContext = context;

		rootView = View.inflate(context, R.layout.dialog_update, null);
		mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
		mTvDialogMsg = (TextView) rootView.findViewById(R.id.tv_dialog_msg);

		createDialog(context, rootView);
	}

	private void createDialog(Context context, View view) {
		mDialog = new Dialog(context, R.style.style_mydialog);
		// mDialog.setCancelable(false);
		// mDialog.setCanceledOnTouchOutside(false);
		mDialog.setOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface arg0) {
				isShow = false;
			}
		});
		mDialog.setContentView(view);
	}

	public synchronized void showOnlyText(String text) {
		if (mDialog == null) {
			createDialog(mContext, rootView);
		}
		mProgressBar.setVisibility(View.GONE);
		mTvDialogMsg.setText(text);

		showDialog();
	}

	public synchronized void showProgressWithText(String text) {
		if (mDialog == null) {
			createDialog(mContext, rootView);
		}
		mProgressBar.setVisibility(View.VISIBLE);
		mTvDialogMsg.setText(text);

		showDialog();
	}

	private void showDialog() {
		L.d(thiz, "before if show Dialog() !");
		if (!isShow && !((Activity) mContext).isFinishing()) {
			L.d(thiz, "show Dialog() !");
			isShow = true;
			mDialog.show();
			// 一定得在show完dialog后来set属性
			WindowManager.LayoutParams lp = mDialog.getWindow().getAttributes();
//			lp.width = (int) (800 * mContext.getResources().getDisplayMetrics().density);
			lp.width = 740;
			lp.height = 120;
			// lp.height = (int) (300 * mContext.getResources().getDisplayMetrics().density);
			lp.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
			lp.x = 55;
			mDialog.getWindow().setAttributes(lp);
		}
	}

	public void dismiss() {
		mProgressBar = null;
		mTvDialogMsg = null;

		mDialog.dismiss();
		mDialog = null;

		isShow = false;
	}

}
