/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hwatong.settings.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hwatong.settings.R;
import com.hwatong.settings.SettingsPreferenceFragment;

/**
 * Base class for Settings fragments, with some helper functions and dialog management.
 */
public abstract class MySettingsPreferenceFragment extends SettingsPreferenceFragment implements OnClickListener, OnTouchListener{

	private static final String TAG = "MySettingsPreferenceFragment";
	private int IMAGE_RES_ID[] = new int[] {R.id.button1, R.id.button2, R.id.button3, R.id.button4, R.id.button5, R.id.button6};
	private int TEXT_RES_ID[] = new int[] {R.id.button_text1, R.id.button_text2, R.id.button_text3, R.id.button_text4, R.id.button_text5, R.id.button_text6};
	private int STRING_RES_ID[] = new int[] {R.string.header_time, R.string.header_screen, R.string.header_language, R.string.header_video, R.string.header_temperature, R.string.header_version};

	private ImageView mImageViews[] = new ImageView[IMAGE_RES_ID.length];
	private TextView mTextViews[] = new TextView[TEXT_RES_ID.length];

    @Override
    public void onDestroyView() {
		for (int i = 0; i < IMAGE_RES_ID.length; i++) {
			mImageViews[i].setOnClickListener(null);
			mImageViews[i].setOnTouchListener(null);
		}
        super.onDestroyView();
    }

	protected abstract int getCurrentId();

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		final View view = getView();

		for(int i=0; i<TEXT_RES_ID.length; i++) {
			mTextViews[i] = (TextView)view.findViewById(TEXT_RES_ID[i]);
			mTextViews[i].setText(STRING_RES_ID[i]);
		}
		for(int i=0; i<IMAGE_RES_ID.length; i++) {
			mImageViews[i] = (ImageView)view.findViewById(IMAGE_RES_ID[i]);
			mImageViews[i].setOnClickListener(this);
			mImageViews[i].setOnTouchListener(this);
		}
		select();
	}

	private void select() {
		for(int i=0; i<mTextViews.length; i++) {
			if (i==getCurrentId()) {
				mTextViews[i].setTextColor(Color.rgb(0, 0, 0));
				mImageViews[i].setSelected(true);
			}else {
				mTextViews[i].setTextColor(Color.rgb(255, 255, 255));
				mImageViews[i].setSelected(false);
			}
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId()==IMAGE_RES_ID[getCurrentId()])
			return;
		
		switch(v.getId()){
		case R.id.button1:
			startFragment(this, MyDateTimeSettings.class.getCanonicalName(), -1, null);
			break;
		case R.id.button2:
			startFragment(this, MyScreenSettings.class.getCanonicalName(), -1, null);
			break;
		case R.id.button3:
			startFragment(this, MyLanguageSettings.class.getCanonicalName(), -1, null);
			break;
		case R.id.button4:
			startFragment(this, MyVideoSettings.class.getCanonicalName(), -1, null);
			break;
		case R.id.button5:
			startFragment(this, MyTemperatureSettings.class.getCanonicalName(), -1, null);
			break;
		case R.id.button6:
			startFragment(this, MyVersionSettings.class.getCanonicalName(), -1, null);
			break;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_DOWN) 
        { 
        	for(int i=0; i<IMAGE_RES_ID.length; i++) {
        		if (v.getId()==IMAGE_RES_ID[i]) {
    				mTextViews[i].setTextColor(Color.rgb(0, 0, 0));
        		}
        	}
        } 
        else if(event.getAction()==MotionEvent.ACTION_MOVE) {
        	for(int i=0; i<IMAGE_RES_ID.length; i++) {
        		if (v.getId()==IMAGE_RES_ID[i]) {
        			if (v.isPressed())
        				mTextViews[i].setTextColor(Color.rgb(0, 0, 0));
        			else if (!v.isSelected())
        				mTextViews[i].setTextColor(Color.rgb(255, 255, 255));
        		}
        	}
        }
        else if(event.getAction()==MotionEvent.ACTION_UP) {
        }
        return false; 
	}
	
	
}
