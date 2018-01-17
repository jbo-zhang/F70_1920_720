package com.hwatong.projectmode.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hwatong.projectmode.R;

public class ConfirmDialog extends Dialog {  
  
    private OnNoOnclickListener noOnclickListener;//取消按钮被点击了的监听器  
    private OnYesOnclickListener yesOnclickListener;//确定按钮被点击了的监听器  
    
	private Button btYes, btNo;
	private TextView tvMessage1, tvMessage2, tvMessage3, tvTitle;
	
    /** 
     * 设置取消按钮的显示内容和监听 
     * 
     * @param str 
     * @param onNoOnclickListener 
     */  
    public void setNoOnclickListener(OnNoOnclickListener onNoOnclickListener) {  
        this.noOnclickListener = onNoOnclickListener;  
    }  
  
    /** 
     * 设置确定按钮的显示内容和监听 
     * 
     * @param str 
     * @param onYesOnclickListener 
     */  
    public void setYesOnclickListener(OnYesOnclickListener onYesOnclickListener) {  
        this.yesOnclickListener = onYesOnclickListener;  
    }  
  
    public ConfirmDialog(Context context) {  
        super(context, R.style.my_dialog);  
    }  
  
    @Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.dialog_confirm);  
        //按空白处不能取消动画  
        setCanceledOnTouchOutside(false);  
  
        //初始化界面控件  
        initView();  
        
        //初始化界面控件的事件  
        initEvent();  
          
    }  
  
    /** 
     * 初始化界面的确定和取消监听器 
     */  
    private void initEvent() {  
        //设置确定按钮被点击后，向外界提供监听  
    	btYes.setOnClickListener(new View.OnClickListener() {  
            @Override  
            public void onClick(View v) {  
                if (yesOnclickListener != null) {  
                    yesOnclickListener.onYesClick();  
                }  
                dismiss();
            }  
        });  
        
        //设置取消按钮被点击后，向外界提供监听  
    	btNo.setOnClickListener(new View.OnClickListener() {  
            @Override  
            public void onClick(View v) {  
                if (noOnclickListener != null) {  
                    noOnclickListener.onNoClick();  
                }
                dismiss();
            }  
        });  
    }  
  
    /** 
     * 初始化界面控件 
     */  
    private void initView() {  
    	tvTitle = (TextView) findViewById(R.id.tv_title);
    	tvMessage1 = (TextView) findViewById(R.id.tv_message_1);
    	tvMessage2 = (TextView) findViewById(R.id.tv_message_2);
    	tvMessage3 = (TextView) findViewById(R.id.tv_message_3);
    	btYes = (Button) findViewById(R.id.bt_confirm);
    	btNo = (Button) findViewById(R.id.bt_cancel);
    }  
    
    
    public ConfirmDialog setTitle(String title) {
    	tvTitle.setText(title);
    	return this;
    }
    
    
    public ConfirmDialog setMessage(String msg1, String msg2, String msg3) {
    	tvMessage1.setText(msg1);
    	tvMessage2.setText(msg2);
    	tvMessage3.setText(msg3);
    	return this;
    }
    
  
    /** 
     * 设置确定按钮和取消被点击的接口 
     */  
    public interface OnYesOnclickListener {  
        public void onYesClick();  
    }  
  
    public interface OnNoOnclickListener {  
        public void onNoClick();  
    }  
}  