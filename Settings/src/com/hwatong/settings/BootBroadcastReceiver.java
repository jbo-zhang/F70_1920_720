/*
 * Copyright (C) 2008 The Android Open Source Project
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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

import com.hwatong.providers.carsettings.SettingsProvider;

public class BootBroadcastReceiver extends BroadcastReceiver {  
	private AudioManager mAudioManager;
	
    // 系统启动完成  
    static final String ACTION = "android.intent.action.BOOT_COMPLETED";  
  
    @Override  
    public void onReceive(Context context, Intent intent) {  
        // 当收听到的事件是“BOOT_COMPLETED”时，就创建并启动相应的Activity和Service  
        if (intent.getAction().equals(ACTION)) {  
    		mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        	String v = Utils.getCarSettingsString(context.getContentResolver(), "boot_volume", SettingsProvider.DEFAULT_BOOT_VOLUME);
        	int volume = Integer.valueOf(v);
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
        }  
    }  
}  
