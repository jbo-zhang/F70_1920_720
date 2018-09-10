package com.hwatong.f70.main;

import com.hwatong.settings.R;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SettingBackUpDialog extends Dialog{
	

    private TextView titletip;//需要删除的设备名
    private Button positiveButton;   //确定按钮
    private Button negativeButton;   //取消按钮
    /**
     * @param context
     */
    public SettingBackUpDialog(Context context) {
        super(context,R.style.CustomDialog);    //自定义style主要去掉标题，标题将在setCustomView中自定义设置
        setCustomView();
    }

    public SettingBackUpDialog(Context context, boolean cancelable,
                                OnCancelListener cancelListener) {
        super(context, R.style.CustomDialog);
        this.setCancelable(cancelable);
        this.setOnCancelListener(cancelListener);
        setCustomView();
    }

    public SettingBackUpDialog(Context context, int theme) {
        super(context, R.style.CustomDialog);
        setCustomView();
    }

    @Override
    public void setTitle(CharSequence title) {
        titletip.setText(title);
    }

    /**
     * 设置整个弹出框的视图
     */
    private void setCustomView(){
        View mView = LayoutInflater.from(getContext()).inflate(R.layout.f70_backup_dialog, null);
        titletip = (TextView)mView.findViewById(R.id.title_name);
        positiveButton = (Button) mView.findViewById(R.id.backupdialog_confirm);
        negativeButton = (Button) mView.findViewById(R.id.backupdialog_quit);
        super.setContentView(mView);
    }
    
    

    /**
     * 
     * @param text
     */
//    public void setPositiveText(CharSequence text){
//        positiveButton.setText(text);
//    }
    /**
     * 
     * @param text
     */
//    public void setNegativeText(CharSequence text){
//        negativeButton.setText(text);
//    }

    /**
     * 
     * @param listener
     */
    public void setOnPositiveListener(View.OnClickListener listener){
        positiveButton.setOnClickListener(listener);
    }
    /**
     * 
     * @param listener
     */
    public void setOnNegativeListener(View.OnClickListener listener){
        negativeButton.setOnClickListener(listener);
    }


}
