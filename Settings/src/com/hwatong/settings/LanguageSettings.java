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

import com.hwatong.settings.R;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;

public class LanguageSettings extends SettingsPreferenceFragment{
    private static final String TAG = "LanguageSettings";

	private static final String KEY_CUSTOM = "custom_pref";
    private static final String KEY_PHONE_LANGUAGE = "phone_language";
    private Preference mLanguagePref;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.language_settings);

        if (getActivity().getAssets().getLocales().length == 1) {
            // No "Select language" pref if there's only one system locale available.
            getPreferenceScreen().removePreference(findPreference(KEY_PHONE_LANGUAGE));
        } else {
            mLanguagePref = findPreference(KEY_PHONE_LANGUAGE);
        }

    }
    @Override
    public void onResume() {
        super.onResume();
        if (mLanguagePref != null) {
            Configuration conf = getResources().getConfiguration();
            String language = conf.locale.getLanguage();
            String localeString;
            // TODO: This is not an accurate way to display the locale, as it is
            // just working around the fact that we support limited dialects
            // and want to pretend that the language is valid for all locales.
            // We need a way to support languages that aren't tied to a particular
            // locale instead of hiding the locale qualifier.
            if (hasOnlyOneLanguageInstance(language,
                    Resources.getSystem().getAssets().getLocales())) {
                localeString = conf.locale.getDisplayLanguage(conf.locale);
            } else {
                localeString = conf.locale.getDisplayName(conf.locale);
            }
            if (localeString.length() > 1) {
                localeString = Character.toUpperCase(localeString.charAt(0)) + localeString.substring(1);
                mLanguagePref.setSummary(localeString);
            }
        }
    }

    private boolean hasOnlyOneLanguageInstance(String languageCode, String[] locales) {
        int count = 0;
        for (String localeCode : locales) {
            if (localeCode.length() > 2
                    && localeCode.startsWith(languageCode)) {
                count++;
                if (count > 1) {
                    return false;
                }
            }
        }
        return count == 1;
    }
    
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
    	return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	public boolean onPreferenceChange(Preference preference, Object objValue) {
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
