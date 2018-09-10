package com.hwatong.f70.commonsetting;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.hwatong.f70.baseview.BaseFragment;
import com.hwatong.f70.main.LogUtils;
import com.hwatong.settings.R;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class VersionInfo extends BaseFragment{
	

	private final static String VERSION_URI = "/sys/devices/platform/imx-i2c.1/i2c-1/1-0019/version";
	private TextView mcuVersion;
	private TextView softwareVersion;	
	private TextView systemVersion;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.f70_versioninfo, container, false);
		
		initWidget(rootView);
		initContent();
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		changedActivityImage(this.getClass().getName());
	}
	
	private void initWidget(View rootView) {
		mcuVersion = (TextView) rootView.findViewById(R.id.tv_hardware_version);
		systemVersion = (TextView)rootView.findViewById(R.id.tv_system_version);
		softwareVersion = (TextView)rootView.findViewById(R.id.tv_software_version);		
	}
	
	private void initContent() {
		String mcuVersionValue = getMcuVersionInfo();
		String softwareVersionValue = getSoftwareVersion();
		if(!TextUtils.isEmpty(mcuVersionValue))
			mcuVersion.setText(mcuVersionValue);
		
		softwareVersion.setText(softwareVersionValue);
	}

	/**
	 *
	 * @return
	 */
	private String getMcuVersionInfo() {
        try {
            FileReader fr = new FileReader(VERSION_URI);
            BufferedReader br = new BufferedReader(fr);
            String tmp = br.readLine();
            LogUtils.d("version: " + tmp);
            br.close();
            fr.close();
            return tmp;
//            while (tmp != null) {
//                String[] strs = tmp.split(",");
//                Log.i("ljwtest:", strs[0] + "," + strs[1] + "," + str);
//                if (strs.length == 2 && strs[0].equals(str))
//                    return strs[1];
//                tmp = br.readLine();
//            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i("TAG", "file not found");
        } catch (IOException e) {
            e.printStackTrace();
        }
		return null;
	}
	
	/**
	 *
	 */
	private String getSoftwareVersion() {
		return Build.ID;
	}
}
