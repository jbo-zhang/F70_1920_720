package com.hwatong.projectmode.fragment.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hwatong.projectmode.iview.IActivity;

public abstract class BaseFragment extends Fragment {
	protected IActivity iActivity;
	
	public void setIActivity(IActivity iActivity) {
		this.iActivity = iActivity;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			
		View view = inflater.inflate(getLayoutId(), container, false);
		
		initViews(view);
		
		return view;
	}


	protected abstract int getLayoutId();
	
	protected void initViews(View view){
		
	}
	
}
