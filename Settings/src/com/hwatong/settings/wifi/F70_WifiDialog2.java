package com.hwatong.settings.wifi;

import com.hwatong.settings.R;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;

public class F70_WifiDialog2 extends AlertDialog implements WifiConfigUiBase {

	private final boolean mEdit;
	private final View.OnClickListener mListener;
	private final AccessPoint mAccessPoint;

	private View mView;
	private WifiConfigController mController;
	private Context context;

	public F70_WifiDialog2(Context context, View.OnClickListener listener,
			AccessPoint accessPoint, boolean edit) {
		super(context, R.style.CustomDialog);
		mEdit = edit;
		mListener = listener;
		mAccessPoint = accessPoint;
		this.context = context;
	}

	@Override
	public void show() {
		super.show();
		int wifidialogWidth = (int) context.getResources().getDimension(R.dimen.wifidialog_width);
		int wifidialogheight = (int) context.getResources().getDimension(R.dimen.wifidialog_height);
		int wifidialogOffset = -(int) context.getResources().getDimension(R.dimen.wifidialog_offset);
		
		Window w = getWindow();
		w.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);		
		w.setContentView(R.layout.f70_wifidialog);
		w.setLayout(wifidialogWidth, wifidialogheight);
		LayoutParams params = new LayoutParams();
		params.width = LayoutParams.WRAP_CONTENT;
		params.height = LayoutParams.WRAP_CONTENT;
		params.x = wifidialogOffset;// ÉèÖÃx×ø±ê
		w.setAttributes(params);
		mView = w.getDecorView();
		mView.getBackground().setAlpha(0);
		setInverseBackgroundForced(true);
		mController = new WifiConfigController(this, mView, mAccessPoint, mEdit);
		/*
		 * During creation, the submit button can be unavailable to determine
		 * visibility. Right after creation, update button visibility
		 */
		mController.enableSubmitIfAppropriate();
	}

	@Override
	public WifiConfigController getController() {
		return mController;
	}

	@Override
	public boolean isEdit() {
		return mEdit;
	}

	@Override
	public void setSubmitButton(CharSequence text) {
		Button btn = (Button) mView.findViewById(R.id.button1);
		if (btn != null) {
			btn.setText(text);
			btn.setOnClickListener((View.OnClickListener) mListener);
			btn.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void setForgetButton(CharSequence text) {
		Button btn = (Button) mView.findViewById(R.id.button3);
		if (btn != null) {
			btn.setText(text);
			Drawable back = context
					.getResources()
					.getDrawable(
							isSubmitButtonVisible() ? R.drawable.setting_wifi_m_selector
									: R.drawable.setting_wifi_r_selector);
			btn.setBackground(back);
			btn.setOnClickListener((View.OnClickListener) mListener);
			btn.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void setCancelButton(CharSequence text) {
		Button btn = (Button) mView.findViewById(R.id.button2);
		if (btn != null) {
			btn.setText(text);
			btn.setOnClickListener((View.OnClickListener) mListener);
			btn.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public Button getSubmitButton() {
		return (Button) mView.findViewById(R.id.button1);
	}

	@Override
	public Button getForgetButton() {
		return (Button) mView.findViewById(R.id.button3);
	}

	@Override
	public Button getCancelButton() {
		return (Button) mView.findViewById(R.id.button2);
	}

	private boolean isSubmitButtonVisible() {
		return getSubmitButton().getVisibility() == View.VISIBLE;
	}
}
