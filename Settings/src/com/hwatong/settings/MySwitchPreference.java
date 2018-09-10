package com.hwatong.settings;

import com.hwatong.settings.R;

import android.content.Context;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;


public class MySwitchPreference extends SwitchPreference {
	private static final String TAG = "MySwitchPreference";

	private Switch mSwitch;

	private OnMyCheckedChangedListener mOnMyCheckedChangedListener;

	public interface OnMyCheckedChangedListener {
		public void onMyCheckedChanged(CompoundButton button, boolean checked);
	}

	//褰撹缃簡鐩戝惉绔彛鏃讹紝鍒嗙switch鍜宨tem鐨勭偣鍑绘秷鎭�
	public void setOnMyCheckedChangedListener(OnMyCheckedChangedListener listener) {
		mOnMyCheckedChangedListener = listener;
	}
	//     private OnMyPreferenceClickListener mOnMyPreferenceClickListener;
	//     
	//     public interface OnMyPreferenceClickListener {
	//    	 public void OnMyPreferenceClick(View v);
	//     }
	//    
	//     public void setOnMyPreferenceClickListener(OnMyPreferenceClickListener listener) {
	//    	 mOnMyPreferenceClickListener = listener;
	//     }

	// ///////////////////////////////////////////Custom Listenr End

	public MySwitchPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public MySwitchPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		//閫氳繃璋冪敤setWidgetLayoutResource鏂规硶鏉ユ洿鏂皃reference鐨剋idgetLayout,鍗虫洿鏂版帶浠跺尯鍩�?
		setWidgetLayoutResource(R.layout.switch_pref);
	}

	public MySwitchPreference(Context context) {
		super(context);
		//閫氳繃璋冪敤setWidgetLayoutResource鏂规硶鏉ユ洿鏂皃reference鐨剋idgetLayout,鍗虫洿鏂版帶浠跺尯鍩�?
		setWidgetLayoutResource(R.layout.switch_pref);
	}

	@Override
	protected void onBindView(View view) {
		mSwitch = (Switch) view.findViewById(R.id.pref_switch);
		//view鍗虫槸浠ｈ�?�鐨刾reference鏁翠釜鍖哄煙,鍙互�?�硅view杩涜浜嬩欢鐩戝�?,涔熷氨鏄疄鐜颁簡preference鏁翠釜鍖哄煙鐨勭偣鍑讳簨浠�
		//        view.setOnClickListener(new View.OnClickListener() {
		//
		//            @Override
		//            public void onClick(View v) {
		//            	mOnMyPreferenceClickListener.OnMyPreferenceClick(v);
		//            }
		//        });

		//switch寮�鍏崇殑鐐瑰嚮浜嬩�?
		if (mSwitch != null) {
			mSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton button, boolean checked) {
					if (mOnMyCheckedChangedListener!=null) {
						mOnMyCheckedChangedListener.onMyCheckedChanged(button, checked);
					}else if (MySwitchPreference.super.isChecked() != checked) {
						setChecked(checked);
					}

					//姝ゅ璋冪敤鑷畾涔夌殑鐩戝惉鍣˙鏂规�?,璇ョ洃鍚�?櫒B鎺ュ彛搴旂敱浣跨敤GestureSwitchPreference鐨勭被鏉ュ疄鐜�,浠庤�屽疄鐜�?
					//preference鐨剆witch鐐瑰嚮浜嬩欢.娉�:鐩戝惉鍣˙鐨勫畾涔夊彲浠ュ弬鑰僌nRadioButtonCheckedListener鎺ュ彛鐨勫畾涔�
				}
			});
		}
        mSwitch.setChecked(super.isChecked());
		super.onBindView(view);
	}

	public Switch getSwitch() {
		return mSwitch;
	}
	public void setChecked(boolean bChecked) {
		Log.d(TAG, "setChecked()" + bChecked);
		if (mOnMyCheckedChangedListener==null) {
			if (mSwitch != null && mSwitch.isChecked()!=bChecked) {
				mSwitch.setChecked(bChecked);
			}
			super.setChecked(bChecked); //瀵艰嚧璋冪敤
		}
	}
}