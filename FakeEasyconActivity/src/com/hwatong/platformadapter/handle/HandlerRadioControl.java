package com.hwatong.platformadapter.handle;

import org.json.JSONException;
import org.json.JSONObject;

import utils.L;
import android.canbus.ICanbusService;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.text.TextUtils;
/**
 * @author caochao
 */
public class HandlerRadioControl {
    private static final String thiz = HandlerRadioControl.class.getSimpleName();
    /**
     * FM控制
     */
    private static HandlerRadioControl mHandlerRadioControl = null;

    private Context mContext;
    private static ICanbusService mCanbusService;
    
    public HandlerRadioControl(Context mContext) {
        super();
        this.mContext = mContext;
    }
    /**
     * 获取FM控制把柄
     * @param context
     * @param canbusService
     * @return
     */
    public static HandlerRadioControl getInstance(Context context, ICanbusService canbusService) {
        L.d(thiz, "HandlerRadioControl init");
        if (mHandlerRadioControl == null) {
            mHandlerRadioControl = new HandlerRadioControl(context);
        }
        mCanbusService = canbusService;
        return mHandlerRadioControl;
    }
    
    private String mHandleMessage = null;
    
    public boolean handleRadioScence(JSONObject result) {
    	L.d(thiz, "handleRadioScence!");
        
        String waveband = "";
        
        String code = "";
        
        String rawText = "";
        
        try {
            waveband = result.getString("waveband");
        } catch (JSONException e) {
        
        }
        
        try {
            code = result.getString("code");
        } catch (JSONException e) {
        
        }
        try {
            rawText = result.getString("rawText");
        } catch (JSONException e) {
        
        }
        
        if(!waveband.isEmpty() && code.isEmpty()){
            if (waveband.equals("am")) {
                Intent intent = new Intent("com.hwatong.voice.OPEN_AM");
                mContext.sendBroadcast(intent);       
            	return true;
            } else if(waveband.equals("fm")) {
            	Intent intent = new Intent("com.hwatong.voice.OPEN_FM");
                mContext.sendBroadcast(intent);   
                return true;
            }
            
        }
        // {"operation":"SAVE","focus":"radio","rawText":"保存电台。"}
        // {"operation":"SAVE","focus":"radio","rawText":"收藏电台。"}
        //所以不能用equals
        if (!TextUtils.isEmpty(rawText) && (rawText.contains("收藏电台") || rawText.contains("保存电台"))) {
            L.d(thiz, "rawText: " + rawText);
            Intent intent = new Intent("com.hwatong.voice.FM_COLLECTION");
            mContext.sendBroadcast(intent);
            return true;
        }
        if (!waveband.isEmpty() && !code.isEmpty()) {
            double frequency = Double.parseDouble(code);
            L.d(thiz, "frequency: " + frequency);

            if (waveband.equals("fm")) {
                if (frequency > 108 || frequency < 87.5) {
                    mHandleMessage = "对不起，请说正确的FM频段";
                } else {
                    Intent intent = new Intent("com.hwatong.voice.FM_CMD");
                    intent.putExtra("frequency", code);
                    mContext.sendBroadcast(intent);
                    L.d(thiz, "handleRadioScence send intent: " + intent);
                    
                    //add++ 解决手动执行语音“FM87.8”跳主界面问题，延时，让界面先跳转语音界面再消失
                    SystemClock.sleep(1500);
                    
                    return true;
                }
            } else if (waveband.equals("am")) {
                if (frequency > 1629 || frequency < 531) {
                    mHandleMessage = "对不起，请说正确的AM频段";
                } else {
                    Intent intent = new Intent("com.hwatong.voice.AM_CMD");
                    intent.putExtra("frequency", code);
                    mContext.sendBroadcast(intent);
                    L.d(thiz, "handleRadioScence send intent: " + intent);
                    
                    //add++解决手动执行语音“AM531”跳主界面问题，延时，让界面先跳转语音界面再消失
                    SystemClock.sleep(1500);
                    
                    return true;
                }
            }
        }

        return false;
    }
}
