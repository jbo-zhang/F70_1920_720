package com.hwatong.f70.bluetooth;

import com.hwatong.settings.R;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;

public class BluetoothProgressDialog extends Dialog {  
    public BluetoothProgressDialog(Context context, String strMessage) {  
        this(context, R.style.CustomDialog, strMessage);  
    }  
  
    public BluetoothProgressDialog(Context context, int theme, String strMessage) {  
        super(context, theme);  
        this.setContentView(R.layout.f70_bluetooth_progressdialog);  
//        this.getWindow().getAttributes().gravity = Gravity.CENTER;  
        TextView tvMsg = (TextView) this.findViewById(R.id.bluetooth_progress_title);  
        if (tvMsg != null) {  
            tvMsg.setText(strMessage);  
        }  
    }  
  
    @Override  
    public void onWindowFocusChanged(boolean hasFocus) {  
  
        if (!hasFocus) {  
            dismiss();  
        }  
    }  
}  
