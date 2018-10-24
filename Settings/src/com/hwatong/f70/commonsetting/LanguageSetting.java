package com.hwatong.f70.commonsetting;

import java.util.Locale;

import android.canbus.ICanbusService;
import android.os.Bundle;
import android.os.ServiceManager;
import android.os.FileUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.android.internal.app.LocalePicker;
import com.hwatong.f70.baseview.BaseFragment;
import com.hwatong.f70.carsetting.F70CarSettingCommand;
import com.hwatong.f70.main.F70CanbusUtils;
import com.hwatong.f70.main.LogUtils;
import com.hwatong.settings.R;

public class LanguageSetting extends BaseFragment implements
		OnCheckedChangeListener, OnClickListener {
	private Locale locale;
	private RadioGroup languageSelect;
	private RadioButton chnRb, engRb;

	private ICanbusService iCanbusService;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LogUtils.d("onCreateView");
		View rootView = inflater.inflate(R.layout.f70_languagesetting,
				container, false);
		initService();
		initWidget(rootView);

		initCurrentLanguage();

		return rootView;
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		LogUtils.d("onHiddenChanged");
	}

	@Override
	public void onResume() {
		super.onResume();
		LogUtils.d("onResume");
		changedActivityImage(this.getClass().getName());
	}

	@Override
	public void onStop() {
		super.onStop();
		LogUtils.d("onStop");
	}

	@Override
	public void onPause() {
		super.onPause();
		LogUtils.d("onPause");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		LogUtils.d("onDestroy");
	}

	private void initWidget(View rootView) {
		languageSelect = (RadioGroup) rootView
				.findViewById(R.id.language_select);
		chnRb = (RadioButton) rootView.findViewById(R.id.chinese);
		engRb = (RadioButton) rootView.findViewById(R.id.english);

		chnRb.setOnClickListener(this);
		engRb.setOnClickListener(this);
//		languageSelect.setOnCheckedChangeListener(this);
	}

	private void initService() {
		iCanbusService = ICanbusService.Stub.asInterface(ServiceManager
				.getService("canbus"));
	}

	
	private void initCurrentLanguage() {
		if (isCurrentLanguageEnglish())
			languageSelect.check(R.id.english);
		else
			languageSelect.check(R.id.chinese);
		languageSelect.setOnCheckedChangeListener(this);
	}

	
	private boolean isCurrentLanguageEnglish() {
		locale = getResources().getConfiguration().locale;
		return Locale.US.getLanguage().equals(locale.getLanguage());
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int resId) {
		switch (resId) {
		case R.id.chinese:
			Log.d("LanguageSetting", "onCheckedChanged Chinese!");
		    try{
			    FileUtils.stringToFile("/device/local", String.valueOf(F70CarSettingCommand.CHINESE));//save language
                FileUtils.setPermissions("/device/local", 0666, -1, -1); // -rw-rw-rw-
		    }catch(Exception e){
		        Log.e("LanguageSetting","Faild to savd local:"+e.getMessage());
		    }
			F70CanbusUtils.getInstance().writeCarConfig(iCanbusService,
					F70CarSettingCommand.TYPE_LANGUAGE,
					F70CarSettingCommand.CHINESE);
			LocalePicker.updateLocale(Locale.CHINA);
			break;
		case R.id.english:
			Log.d("LanguageSetting", "onCheckedChanged English!");
		    try{
			    FileUtils.stringToFile("/device/local", String.valueOf(F70CarSettingCommand.ENGLISH));//save language
                FileUtils.setPermissions("/device/local", 0666, -1, -1); // -rw-rw-rw-
		    }catch(Exception e){
		        Log.e("LanguageSetting","Faild to savd local:"+e.getMessage());
		    }
			F70CanbusUtils.getInstance().writeCarConfig(iCanbusService,
					F70CarSettingCommand.TYPE_LANGUAGE,
					F70CarSettingCommand.ENGLISH);
			LocalePicker.updateLocale(Locale.US);
			break;
		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {

	}
}
