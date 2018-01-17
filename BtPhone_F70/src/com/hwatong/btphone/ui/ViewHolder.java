package com.hwatong.btphone.ui;

import com.hwatong.btphone.activity.ContactsListActivity;
import com.hwatong.btphone.adapter.CallLogListAdapter;

import android.widget.ImageButton;
import android.widget.TextView;

/**
 * 用于{@link CallLogListAdapter} 和 {@link ContactsListActivity#ContactsAdapter}
 * 
 * @author zxy time:2017年6月20日
 * 
 */
public class ViewHolder {
	public DrawableTextView mDtvName;
	public TextView mTvNumber;
	public ImageButton mBtnDial;

}
