package com.hwatong.bt;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;
import android.view.View;
import android.view.View.OnClickListener;

public class BluetoothProgressDialog extends Dialog implements OnClickListener {
    public boolean mCanceled = false;
    public BluetoothProgressDialog(Context context, String strMessage, boolean showcancel) {
        this(context, R.style.CustomDialog, strMessage, showcancel);  
    }

    public BluetoothProgressDialog(Context context, int theme, String strMessage, boolean showcancel) {
        super(context, theme);
        this.setContentView(R.layout.f70_bluetooth_progressdialog);
        TextView tvMsg = (TextView) this.findViewById(R.id.bluetooth_progress_title);
        if (tvMsg != null) {
            tvMsg.setText(strMessage);
        }
        if(showcancel) {
            View cancel = findViewById(R.id.cancel_search_btn);
            if(cancel != null) {
                cancel.setVisibility(View.VISIBLE);
                cancel.setOnClickListener(this);
            }
        }
    }
  
    @Override  
    public void onWindowFocusChanged(boolean hasFocus) {  
  
        if (!hasFocus) {  
            dismiss();  
        }  
    }

    @Override
	public void onClick(View v) {
        mCanceled = true;
        cancel();
    }
}  
