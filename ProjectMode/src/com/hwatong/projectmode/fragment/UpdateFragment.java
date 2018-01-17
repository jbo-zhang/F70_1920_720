package com.hwatong.projectmode.fragment;

import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.hwatong.projectmode.R;
import com.hwatong.projectmode.fragment.base.BaseFragment;

public class UpdateFragment extends BaseFragment {

	private TextView tvSystemUpdate;
	private TextView tvTboxUpdate;
	private TextView mxUpdate;

	
	@Override
	protected int getLayoutId() {
		return R.layout.fragment_update;
	}
	
	@Override
	protected void initViews(View view) {
		tvSystemUpdate = (TextView) view.findViewById(R.id.tv_system_update);
		tvTboxUpdate = (TextView) view.findViewById(R.id.tv_tbox_update);
		mxUpdate = (TextView) view.findViewById(R.id.mx_update);
		setupClickEvent();
	}

	private void setupClickEvent() {
		tvSystemUpdate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				iActivity.toSystemUpdate();
			}
		});
		
		tvTboxUpdate.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				iActivity.toTboxUpdate();
			}
		});
		mxUpdate.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClassName("com.mxnavi.mxnaviupdate", "com.mxnavi.mxnaviupdate.MainActivity");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(intent);
            }
		    
		});
		
	}
}
