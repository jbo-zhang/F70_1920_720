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

package com.hwatong.settings;

import java.util.ArrayList;
import java.util.List;

import com.hwatong.settings.R;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;

public class NavigationSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, OnPreferenceClickListener {
    private static final String TAG = "NavigationSettings";

    private static final String KEY_NAVIGATION = "navigation";
    private ListPreference mNaviApps;
	private List<AppBean> apps = new ArrayList<AppBean>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.navigation_settings);

        mNaviApps = (ListPreference)findPreference(KEY_NAVIGATION);
        mNaviApps.setOnPreferenceChangeListener(this);
        mNaviApps.setOnPreferenceClickListener(this);
        init();
    }
    private String getApplicationName(String packageName) {
		try {
			ApplicationInfo info = getPackageManager().getApplicationInfo(packageName, 0);
			return info.loadLabel(getPackageManager()).toString(); 
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		 return null;
    }

    private void init() {
    	if (mNaviApps==null)
    		return;
    	
	    String packageName = Utils.getCarSettingsString(getContentResolver(), "navigation_software", "");
	    if (packageName!=null) {
	    	String appName = getApplicationName(packageName);
	    	mNaviApps.setSummary(appName);
	    }
	    
		apps.clear();
		List<PackageInfo> applist = getPackageManager().getInstalledPackages(0);
		for(PackageInfo app:applist) {
			if(app.packageName.equals("com.autonavi.amapauto") || (app.applicationInfo.flags&ApplicationInfo.FLAG_SYSTEM) == 0) {
				String appName = app.applicationInfo.loadLabel(getPackageManager()).toString();
				Drawable appIcon = app.applicationInfo.loadIcon(getPackageManager());
				apps.add(new AppBean(appName,app.packageName, app.versionName, appIcon));
			}
		}
		if (apps.size()>0) {
	        CharSequence[] entries = new CharSequence[apps.size()];
	        CharSequence[] entryValues = new CharSequence[apps.size()];
			for(int i=0; i<apps.size();i++) {
				entries[i]=apps.get(i).appName;
				entryValues[i]=apps.get(i).packName;
			}
	        mNaviApps.setEntries(entries);
	        mNaviApps.setEntryValues(entryValues);
//			empty.setVisibility(View.GONE);
		}else {
	        CharSequence[] entries = new CharSequence[1];
	        CharSequence[] entryValues = new CharSequence[1];
			entries[0]="";
			entryValues[0]="";
	        mNaviApps.setEntries(entries);
	        mNaviApps.setEntryValues(entryValues);
//			removePreference(KEY_NAVIGATION); 
//			empty.setVisibility(View.VISIBLE);
		}
    	
    }
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
    	if (apps.size()>0)
    		return super.onPreferenceTreeClick(preferenceScreen, preference);
    	else 
    		return false;
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
    	String packageName = (String) objValue;
    	Utils.putCarSettingsString(getContentResolver(), "navigation_software", packageName);
	    if (packageName!=null) {
	    	String appName = getApplicationName(packageName);
	    	mNaviApps.setSummary(appName);
	    }
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return false;
    }
    public class AppBean{

        private String appName; //搴旂敤鍚�? 
        private String packName; //鍖呭�?  
        private String version; //鐗堟湰鍚�?  
        private Drawable appIcon; //搴旂敤鍥炬爣  

        public AppBean(String appName,String packageName, String version, Drawable appIcon) {
        	this.appName = appName;
        	this.packName = packageName;
        	this.version = version;
        	this.appIcon = appIcon;
        }
        public String getAppName() {  
            return appName;  
        }  
          
        public void setAppName(String appName) {  
            this.appName = appName;  
        }  
         
        public String getPackageName() {  
            return packName;  
        }  
          
        public void setPackageName(String packageName) {  
            this.packName = packageName;  
        }  
        
        public String getVersion() {  
            return version;  
        }  

        public void setVersion(String version) {  
            this.version = version;  
        }  

        public Drawable getAppIcon() {  
            return appIcon;  
        }  

        public void setAppIcon(Drawable appIcon) {  
            this.appIcon = appIcon;  
        }
    }    
}
