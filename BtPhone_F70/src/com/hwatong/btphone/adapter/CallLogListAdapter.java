package com.hwatong.btphone.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.hwatong.btphone.CallLog;
import com.hwatong.btphone.ui.DrawableTextView;
import com.hwatong.btphone.ui.R;
import com.hwatong.btphone.ui.ViewHolder;
import com.hwatong.btphone.util.L;


public class CallLogListAdapter extends BaseAdapter {

	private static final String thiz = CallLogListAdapter.class.getSimpleName();
	
	private List<CallLog> mDataList = new ArrayList<CallLog>();
	private Activity mContext;

	private int mLayoutResId;
	
	private ButtonOnClick mButtonOnClick;//响应拨号按钮被点击事件

	private int width1 = 150, width2 = 170;
	
	public CallLogListAdapter(Activity context, int layoutResId, List<CallLog> list) {
		super();
		mDataList.addAll(list);
		Log.d("9095", "mDataList size " + mDataList.size());
		this.mLayoutResId = layoutResId;
		this.mContext = context;
	}
	
	public void setmBtnOnClickListener(ButtonOnClick buttonOnClick) {
		this.mButtonOnClick = buttonOnClick;
	}
	
	public void refresh(List<CallLog> logs) {
		mDataList.clear();
		mDataList.addAll(logs);
		L.d(thiz, "mDataList.size : " + mDataList.size());
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return mDataList.size();
	}

	@Override
	public CallLog getItem(int position) {
		if(mDataList.size() > position) {
			return mDataList.get(position);
		} else {
			return null;
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();

			convertView = mContext.getLayoutInflater().inflate(mLayoutResId, null);
			holder.mDtvName = (DrawableTextView) convertView.findViewById(R.id.dtv_name);
			holder.mTvNumber = (TextView) convertView.findViewById(R.id.tv_phone_number);
			if (mLayoutResId == R.layout.item_contacts_btn) {
				holder.mBtnDial = (ImageButton) convertView.findViewById(R.id.btn_dial);
				width1= 310;
				width2= 260;
			} else {
				width1= 150;
				width2= 170;
			}

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		CallLog callLog = getItem(position);
		if(callLog == null) {
			return convertView;
		}
		Drawable drawableLeft = null;
		
		if(CallLog.TYPE_CALL_IN.equals(callLog.type)) {
			drawableLeft = mContext.getResources().getDrawable(R.drawable.icon_log_in);
		} else if(CallLog.TYPE_CALL_MISS.equals(callLog.type)) {
			drawableLeft = mContext.getResources().getDrawable(R.drawable.icon_log_miss);
		} else if(CallLog.TYPE_CALL_OUT.equals(callLog.type)) {
			drawableLeft = mContext.getResources().getDrawable(R.drawable.icon_log_out);
		}
		
//		switch (callLog.type) {
//		case CallLog.TYPE_CALL_IN:
//			drawableLeft = mContext.getResources().getDrawable(R.drawable.icon_log_in);
//			break;
//		case CallLog.TYPE_CALL_MISS:
//			drawableLeft = mContext.getResources().getDrawable(R.drawable.icon_log_miss);
//			break;
//		case CallLog.TYPE_CALL_OUT:
//			drawableLeft = mContext.getResources().getDrawable(R.drawable.icon_log_out);
//			break;
//		default:
//			break;
//		}
		
		holder.mDtvName.setDrawables(drawableLeft, null, null, null);
		holder.mDtvName.setText(TextUtils.ellipsize(callLog.name, holder.mDtvName.getPaint(), width1, TextUtils.TruncateAt.END));
		holder.mTvNumber.setText(TextUtils.ellipsize(callLog.number,holder.mTvNumber.getPaint(), width2, TextUtils.TruncateAt.END));
		
		if (holder.mBtnDial != null) {
			holder.mBtnDial.setFocusable(false);
			holder.mBtnDial.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					CallLog callLog = mDataList.get(position);
					if(mButtonOnClick != null){
						mButtonOnClick.clickButton(callLog);
					}
				}
			});
		}
		return convertView;
	}
	
	public interface ButtonOnClick{
		void clickButton(CallLog callLog);
	}

	
	public static String dealDetailString(TextView v ,String content,float show_len){
		TextPaint tpaint =v.getPaint();
		//tpaint.setTextSize(21);
		String temp="";
		if(content!=null){
			temp=content.replaceAll("\n", " ").replaceAll("\b", " ");
		}
		String str_content=(content==null?"":temp);
		float len=0;
		int s_len=0;
		if(str_content != null && str_content != ""){
			len = tpaint.measureText(str_content);
			s_len=str_content.length();
		}
		int i=0;
		for(; i < s_len && len > show_len; i++){
			str_content=str_content.substring(0, str_content.length()-1);
			len = tpaint.measureText(str_content);
		}
		if(i>0){
			return str_content+"...";
		}else{
		   return content;
		}
	}
	
}
