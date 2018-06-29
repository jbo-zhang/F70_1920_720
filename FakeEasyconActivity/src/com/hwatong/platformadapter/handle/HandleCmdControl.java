package com.hwatong.platformadapter.handle;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Instrumentation;
import android.canbus.ICanbusService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.hwatong.bt.BtDef;
import com.hwatong.platformadapter.ServiceList;
import com.hwatong.platformadapter.Tips;
import com.hwatong.platformadapter.utils.L;
import com.iflytek.platform.type.PlatformCode;
import com.iflytek.platformservice.PlatformService;
/**
 * @author caochao
 */
public class HandleCmdControl {
    private static final String thiz = HandleCmdControl.class.getSimpleName();
    /**
     * 处理系统命令
     */
    private Instrumentation mInst ;
    
    private static HandleCmdControl mHandleCmdControl = null;
    
    private static AudioManager mAudioManager ;

    private Context mContext;
    private static ICanbusService mCanbusService;
    private static ServiceList mServiceList ;
    
    public HandleCmdControl(Context mContext) {
        super();
        this.mContext = mContext;
        mInst = new Instrumentation();
    }

    public static HandleCmdControl getInstance(Context context, ICanbusService canbusService , AudioManager audioManager,
            ServiceList serviceList) {
        L.d(thiz, "HandleCmdControl init");
        if (mHandleCmdControl == null) {
            mHandleCmdControl = new HandleCmdControl(context);
        }
        mAudioManager = audioManager ;
        mCanbusService = canbusService;
        mServiceList = serviceList ;
        return mHandleCmdControl;
    }
    
    com.hwatong.bt.IService btService = null;    
    public boolean handleCmdScence(JSONObject result) {
        String category = "";
        String name = "";
        int nameVaule = 0;
        int offset = -1 ;
        String operation = "" ;
        try {
            category = result.getString("category");
        } catch (JSONException e) {
        }
        try {
            name = result.getString("name");
        } catch (JSONException e) {
        }
        try {
            nameVaule = result.getInt("nameValue");
        } catch (JSONException e) {
        }
        try {
            offset = result.getInt("offset");
        } catch (JSONException e) {
        }
        try {
            operation = result.getString("operation");
        } catch (JSONException e) {
        }
        
        if(mServiceList != null){
            btService = mServiceList.getBtService();
        }
        if("返回".equals(name)){
            try {
                PlatformService.platformCallback.systemStateChange(PlatformCode.STATE_VIEWOFF);
                return true ;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        /**
         * 修改bug，关闭不起作用
         */
        if("关闭".equals(name)){
            try {
                PlatformService.platformCallback.systemStateChange(PlatformCode.STATE_VIEWOFF);
                return true ;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        if (!category.isEmpty()) {
            if ("音量控制".equals(category)) {
                if (!TextUtils.isEmpty(name) && name.contains("音量+")) {
                    setMute(false);
                    mAudioManager.adjustSuggestedStreamVolume(AudioManager.ADJUST_RAISE, AudioManager.STREAM_MUSIC, AudioManager.FLAG_SHOW_UI);
                    return true;
                } else if (!TextUtils.isEmpty(name) && name.contains("音量-")) {
                    setMute(false);
                    mAudioManager.adjustSuggestedStreamVolume(AudioManager.ADJUST_LOWER, AudioManager.STREAM_MUSIC, AudioManager.FLAG_SHOW_UI);
                    return true;
                } else if ("静音".equals(name)) {
                    setMute(true);
                    return true;
                } else if ("打开音量".equals(name)) {
                    setMute(false);
                    return true;
                } else if ("导航音量调节".equals(name)) {
                    setMute(false);
                    mAudioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, nameVaule, AudioManager.FLAG_SHOW_UI);
                    return true;
                } else if ("电话音量调节".equals(name)) {
                    setMute(false);
                    mAudioManager.setStreamVolume(AudioManager.STREAM_RING, nameVaule, AudioManager.FLAG_SHOW_UI);
                    return true;
                } else if ("媒体音量调节".equals(name) || "蓝牙音乐音量调节".equals(name) || "音乐音量调节".equals(name) || "收音机音量调节".equals(name)) {
                    setMute(false);
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, nameVaule, AudioManager.FLAG_SHOW_UI);
                    return true;
                }
            } else if ("播放模式".equals(category)) {
                if ("顺序循环".equals(name)) {
                    Intent intent = new Intent("com.hwatong.voice.PLAY_MODE");
                    intent.putExtra("mode", "loop");
                    mContext.sendBroadcast(intent);
                    return true;
                } else if ("单曲循环".equals(name)) {
                    Intent intent = new Intent("com.hwatong.voice.PLAY_MODE");
                    intent.putExtra("mode", "single_loop");
                    mContext.sendBroadcast(intent);
                    return true;
                } else if ("随机播放".equals(name)) {
                    Intent intent = new Intent("com.hwatong.voice.PLAY_MODE");
                    intent.putExtra("mode", "random");
                    mContext.sendBroadcast(intent);
                    return true;
                }
            } else if("切换播放模式".equals(name)){
                Intent intent = new Intent("com.hwatong.voice.PLAY_MODE");
                mContext.sendBroadcast(intent);
                return true;            
            }else if ("曲目控制".equals(category)) {
                if ("上一首".equals(name) || "上一频道".equals(name) || "上一台".equals(name)) {
                    sendMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
                    return true;
                } else if ("下一首".equals(name) || "下一频道".equals(name) || "下一台".equals(name)) {
                    sendMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_NEXT);
                    return true;
                } else if ("暂停".equals(name)) {
                    sendMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PAUSE);
                    return true;
                } else if ("播放".equals(name)) {
                    sendMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY);
                    return true;
                }
            } else if ("收音机控制".equals(category)) {
                if ("上一频道".equals(name)) {
                    sendMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
                    return true;
                } else if ("下一频道".equals(name)) {
                    sendMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_NEXT);
                    return true;
                }
            } else if ("频道选择".equals(category)) {
                String str = name.substring(2, 3);
                Intent intent = new Intent("com.hwatong.voice.SELECT_CHANNEL");
                mContext.sendBroadcast(intent);
                return true;
            } else if ("总体控制".equals(category)) {
                if ("返回".equals(name)) {
                    sendMediaKeyEvent(KeyEvent.KEYCODE_BACK);
                    return true;
                }
            } else if ("蓝牙控制".equals(category)) {
                if(!isBtOpen()){
                    Tips.setCustomTipUse(true);
                    Tips.setCustomTip("蓝牙未打开");
                    return false ;
                }
                if ("蓝牙搜索".equals(name) || "蓝牙连接".equals(name)) {
                    Intent intent = new Intent("com.hwatong.voice.SEARCH_BT");
                    mContext.sendBroadcast(intent);
                    
                    //add++ 解决跳主界面问题
                    SystemClock.sleep(1500);
                    
                    return true;
                }
                L.d(thiz, "operation:"+operation+";offset:"+offset);
                if ("SELECT".equals(operation) && offset != -1) {
                    Intent intent = new Intent("com.hwatong.voice.SELECT_BT");
                    intent.putExtra("select", offset);
                    mContext.sendBroadcast(intent);
                    return true;
                }
            } else if("电台控制".equals(category)){
                if("后台播放".equals(name)){
                    Intent intent = new Intent("com.hwatong.voice.CLOSE_MUSIC"); 
                    mContext.sendBroadcast(intent); 
                }
            }
        }
        if("黑屏".equals(name)||"关闭车机".equals(name) || "关闭屏幕".equals(name)){
            Intent intent = new Intent("com.hwatong.system.SCREEN_OFF");
            mContext.sendBroadcast(intent);
            return true;
        } else if("解除黑屏".equals(name)){
            Intent intent = new Intent("com.hwatong.system.UNLOCK");
            mContext.sendBroadcast(intent); 
            return true;     
        //onNLPResult(): {"name":"下一个","focus":"cmd","rawText":"下一个。"}
        //onNLPResult(): {"name":"下一首","focus":"cmd","rawText":"下一个"}   
        } else if(!TextUtils.isEmpty(name) && (name.contains("下一个") || name.contains("下一首"))){
            sendMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_NEXT);
            return true ;
        } else if(!TextUtils.isEmpty(name) && (name.contains("上一个") || name.contains("上一首"))){
            sendMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
            return true ;
        }
        return false;
    }
    /**
     * 静音
     * @param state
     */
    private void setMute(boolean state) {
        try {
            mCanbusService.setMute(state);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    /**
     * 发送系统媒体事件
     * @param keyCode
     */
    private void sendMediaKeyEvent(final int keyCode) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    L.d(thiz, "sendKeyDownUpSync" + keyCode);
                    mInst.sendKeyDownUpSync(keyCode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 500);
    }
    /**
     * 蓝牙是否打开
     * @return
     */
    private boolean isBtOpen(){
        if(btService == null){
            return false;
        }
        try {
            if(btService.getAdapterState()==BtDef.BT_STATE_OFF){
                return false ;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return true ;
    }
}
