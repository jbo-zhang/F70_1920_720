package com.hwatong.platformadapter.handle;

import org.json.JSONException;
import org.json.JSONObject;
import android.canbus.ICanbusService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
/**
 * @author caochao
 */
public class HandlerRadioControl {
    private static final String TAG = "Voice";
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
        Log.d(TAG, "HandleCarControl init");
        if (mHandlerRadioControl == null) {
            mHandlerRadioControl = new HandlerRadioControl(context);
        }
        mCanbusService = canbusService;
        return mHandlerRadioControl;
    }
    
    private String mHandleMessage = null;
    
    public boolean handleRadioScence(JSONObject result) {
        
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
            }       	
        }
        if ("收藏电台".equals(rawText) || "保存电台".equals(rawText)) {
            Log.d(TAG, "rawText: " + rawText);
            Intent intent = new Intent("com.hwatong.voice.FM_COLLECTION");
            mContext.sendBroadcast(intent);
            return true;
        }
        if (!waveband.isEmpty() && !code.isEmpty()) {
            double frequency = Double.parseDouble(code);
            Log.d(TAG, "frequency: " + frequency);

            if (waveband.equals("fm")) {
                if (frequency > 108 || frequency < 87.5) {
                    mHandleMessage = "对不起，请说正确的FM频段";
                } else {
                    Intent intent = new Intent("com.hwatong.voice.FM_CMD");
                    intent.putExtra("frequency", code);
                    mContext.sendBroadcast(intent);
                    
                    Log.d(TAG, "handleRadioScence send intent: " + intent);
                    return true;
                }
            } else if (waveband.equals("am")) {
                if (frequency > 1605 || frequency < 535) {
                    mHandleMessage = "对不起，请说正确的AM频段";
                } else {
                    Intent intent = new Intent("com.hwatong.voice.AM_CMD");
                    intent.putExtra("frequency", code);
                    mContext.sendBroadcast(intent);
                    Log.d(TAG, "handleRadioScence send intent: " + intent);
                    return true;
                }
            }
        }

        return false;
    }
}
