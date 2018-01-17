package com.hwatong.projectmode.ui;

import android.app.Dialog;
import android.content.Context;
import android.opengl.Visibility;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hwatong.projectmode.R;

public class UpdateDialog extends Dialog {  
  
    private OnNoOnclickListener noOnclickListener;//取消按钮被点击了的监听器  
    private OnYesOnclickListener yesOnclickListener;//确定按钮被点击了的监听器  
    
	private ProgressBar pbUpdate;
	private Button btUpdate;
	private TextView tvTitle;
	
	private TextView tvProgressLeft, tvProgressRight;
	
	public static final int STYLE_COPY = 1;
	
	public static final int STYLE_UPDATE = 2;
  
	
	private int mStyle = STYLE_UPDATE;
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
  
    public UpdateDialog(Context context, int style) {  
        super(context, R.style.my_dialog);  
        
        mStyle = style;
    }  
    
    
  
    @Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.dialog_tbox_update);  
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
        btUpdate.setOnClickListener(new View.OnClickListener() {  
            @Override  
            public void onClick(View v) {  
                if (yesOnclickListener != null) {  
                    yesOnclickListener.onYesClick();  
                }  
            }  
        });  
        
//        //设置取消按钮被点击后，向外界提供监听  
//        no.setOnClickListener(new View.OnClickListener() {  
//            @Override  
//            public void onClick(View v) {  
//                if (noOnclickListener != null) {  
//                    noOnclickListener.onNoClick();  
//                }  
//            }  
//        });  
    }  
  
    /** 
     * 初始化界面控件 
     */  
    private void initView() {  
    	tvTitle = (TextView) findViewById(R.id.tv_dialog_title);
    	pbUpdate = (ProgressBar) findViewById(R.id.pb_update_progress);
    	btUpdate = (Button) findViewById(R.id.bt_update);
    	tvProgressLeft = (TextView) findViewById(R.id.tv_progress_left);
    	tvProgressRight = (TextView) findViewById(R.id.tv_progress_right);
    	
    	switch (mStyle) {
		case STYLE_COPY:
			tvTitle.setText(R.string.copying);
			btUpdate.setVisibility(View.INVISIBLE);
			tvProgressLeft.setVisibility(View.INVISIBLE);
			tvProgressRight.setVisibility(View.INVISIBLE);
			
			break;
		case STYLE_UPDATE:
			tvTitle.setText(R.string.version_no);
			btUpdate.setVisibility(View.INVISIBLE);
			tvProgressLeft.setVisibility(View.VISIBLE);
			tvProgressRight.setVisibility(View.VISIBLE);
			tvProgressRight.setText("升级中…");
			break;
    	}
    	
    }  
    
    public void setTitle(String versionCode) {
    	tvTitle.setText(versionCode);
    }
    
    
    public void setProgress(int progress) {
    	pbUpdate.setProgress(progress);
    	tvProgressLeft.setText(progress + "%");
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