package com.hwatong.platformadapter.handle;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.canbus.ICanbusService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.text.TextUtils;

import com.hwatong.platformadapter.ServiceList;
import com.hwatong.platformadapter.Tips;
import com.hwatong.platformadapter.utils.L;
import com.hwatong.platformadapter.utils.Utils;

public class HandleAppControl {
    private static final String thiz = HandleAppControl.class.getSimpleName();
    /**
     * 本地App控制
     */
    private static HandleAppControl mHandleAppControl = null;

    private Context mContext;
    private static ICanbusService mCanbusService;
    private static ServiceList mServiceList ;
    public HandleAppControl(Context mContext) {
        super();
        this.mContext = mContext;
    }

    public static HandleAppControl getInstance(Context context, ICanbusService canbusService , ServiceList serviceList) {
        L.d(thiz, "HandleAppControl init");
        if (mHandleAppControl == null) {
            mHandleAppControl = new HandleAppControl(context);
        }
        mCanbusService = canbusService;
        mServiceList = serviceList ;
        return mHandleAppControl;
    }
    
    public boolean handleAppScence(JSONObject result) {
        String operation = null;
        String name = "";
        String raw = "";
        try {
            operation = result.getString("operation");
        } catch (JSONException e) {
        }
        try {
            name = result.getString("name");
        } catch (JSONException e) {
        }
        try {
            raw = result.getString("rawText");
        } catch (JSONException e) {
        }
        com.hwatong.media.IService mediaService = mServiceList.getMediaService();
        com.hwatong.bt.IService btService = mServiceList.getBtService();
        com.hwatong.ipod.IService ipodService = mServiceList.getIPodService();
        if ("LAUNCH".equals(operation) && name.equalsIgnoreCase("ipod")) {
            try {
                if( ipodService !=null && !ipodService.isAttached() ){
                    Tips.setCustomTipUse(true);
                    Tips.setCustomTip("设备未连接");
                    return false ;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent("com.hwatong.ipod.DEVICE_ATTACHED") ;
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            return true ;                    
        }
        if ("EXIT".equals(operation) && name.equalsIgnoreCase("ipod")) {
        	L.d(thiz, "close ipod");
            Intent intent = new Intent("com.hwatong.voice.CLOSE_IPOD");
            mContext.sendBroadcast(intent);     
            return true ;                    
        }
        /**
         * 手机互联操作
         */
        if("LAUNCH".equals(operation)){
            
            if("手机里的导航".equals(name)){
                String action = "com.hwatong.voice.PHONE_LINK_NAVIGATION" ;
                Intent intent = new Intent(action);
                mContext.sendBroadcast(intent);
                return true ;
            }
            if("手机里的天气".equals(name)){
                String action = "com.hwatong.voice.PHONE_LINK_WEATHER" ;
                Intent intent = new Intent(action);
                mContext.sendBroadcast(intent);
                return true ;
            }
            if("手机里的新闻".equals(name)){
                String action = "com.hwatong.voice.PHONE_LINK_NEWS" ;
                Intent intent = new Intent(action);
                mContext.sendBroadcast(intent); 
                return true ;
            }
            if("手机里的喜马拉雅".equals(name)){
                String action = "com.hwatong.voice.PHONE_LINK_FM" ;
                Intent intent = new Intent(action);
                mContext.sendBroadcast(intent);  
                return true ;
            }
            if("手机里的音乐".equals(name)){
                String action = "com.hwatong.voice.PHONE_LINK_MUSIC" ;
                Intent intent = new Intent(action);
                mContext.sendBroadcast(intent);  
                return true ;
            }
        }
        
        /**
         * 手机互联操作关闭
         */
        if("EXIT".equals(operation)){
            // {"name":"手机互联","operation":"EXIT","focus":"app","rawText":"关闭手机互联"}
        	
            if("手机里的导航".equals(name)){
                String action = "com.hwatong.voice.PHONE_LINK_NAVIGATION_EXIT" ;
                Intent intent = new Intent(action);
                mContext.sendBroadcast(intent);
                return true ;
            }
            if("手机里的天气".equals(name)){
                String action = "com.hwatong.voice.PHONE_LINK_WEATHER_EXIT" ;
                Intent intent = new Intent(action);
                mContext.sendBroadcast(intent);
                return true ;
            }
            if("手机里的新闻".equals(name)){
                String action = "com.hwatong.voice.PHONE_LINK_NEWS_EXIT" ;
                Intent intent = new Intent(action);
                mContext.sendBroadcast(intent); 
                return true ;
            }
            if("手机里的喜马拉雅".equals(name)){
                String action = "com.hwatong.voice.PHONE_LINK_FM_EXIT" ;
                Intent intent = new Intent(action);
                mContext.sendBroadcast(intent);  
                return true ;
            }
            if("手机里的音乐".equals(name)){
                String action = "com.hwatong.voice.PHONE_LINK_MUSIC_EXIT" ;
                Intent intent = new Intent(action);
                mContext.sendBroadcast(intent);  
                return true ;
            }
            //关闭手机互联
            if(!TextUtils.isEmpty(name) && name.contains("互联")) {
        		L.d(thiz, "exit vlink !");
        		Utils.closeApplication(mContext, "com.eryanet.vlink");
        		return true;
        	}
        }
        
        
        
        
        //add++ 添加处理{"name":"音乐","operation":"","focus":"app","rawText":"音乐"}情况
        if ("LAUNCH".equals(operation) && name.contains("音乐") || ("".equals(operation) && name.equals("音乐"))) {
            if(mediaService!=null){
                try {
                    List list = mediaService.getMusicList();
                        if(list==null || list.size() == 0){
                            Tips.setCustomTipUse(true);
                            Tips.setCustomTip("暂无本地音乐");
                            return false;
                        }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        
        //add ++ 添加处理 {"name":"视频","operation":"","focus":"app","rawText":"视频"} 情况
        if ("LAUNCH".equals(operation) && name.contains("视频") || ("".equals(operation) && name.equals("视频"))) {
            if(mediaService!=null){
                try {
                    List list = mediaService.getVideoList();
                    if(list==null || list.size() == 0){
                        Tips.setCustomTipUse(true);
                        Tips.setCustomTip("暂无本地视频");
                        return false;
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        
        ////{"name":"图片","operation":"LAUNCH","focus":"app","rawText":"播放图片"}
        //add++ 添加处理 {"name":"图片","operation":"","focus":"app","rawText":"图片"} 情况
        if ("LAUNCH".equals(operation) && name.contains("图片") || ("".equals(operation) && name.equals("图片"))) {
            if(mediaService!=null){
                try {
                    List list = mediaService.getPictureList();
                    if(list==null || list.size() == 0){
                        Tips.setCustomTipUse(true);
                        Tips.setCustomTip("暂无本地图片");
                        return false;
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
        if("LAUNCH".equals(operation) && (name.contains("在线娱乐") || name.contains("娱乐云"))){
            Intent intent = mContext.getPackageManager().getLaunchIntentForPackage("com.xiaoma.launcher");
            if (intent != null) {
                try {
                    mContext.startActivity(intent);
                    return true ;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return false ;
        }
        if("LAUNCH".equals(operation) && (name.contains("智能互联") || name.contains("手机互联"))){
            try {
            	L.d(thiz, "打开智能互联");
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setComponent(new ComponentName("com.eryanet.vlink", "com.eryanet.vlink.ConnectActivity"));
                mContext.startActivity(intent);
                return true ;
            } catch (Exception e) {
                L.d(thiz , e.toString());
                e.printStackTrace();
            }
            return false ;
        }
        
        //delete--
//        if("LAUNCH".equals(operation) && "ipod".equalsIgnoreCase(name)){
//        	L.d(thiz," LAUNCH ipod !!!!!!!!!!!!!!");
//            return true ;
//        }
        
        if (operation != null && !name.isEmpty()) {
            PackageManager pManager = mContext.getPackageManager();
            List<PackageInfo> paklist = pManager.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
            int i = 0;
            for (i = 0; i < paklist.size(); i++) {
                PackageInfo pInfo = paklist.get(i);
                String packageName = pInfo.packageName;
                String appName = pInfo.applicationInfo.loadLabel(pManager).toString();

                L.d(thiz, "packageName : " + packageName + " appName : " + appName);
                
                if (appName.equalsIgnoreCase(name)) {
                    if ("com.hwatong.ipod".equals(packageName))
                        packageName = "com.hwatong.ipod.ui";

                    if ("LAUNCH".equals(operation) || operation.isEmpty()) {
                        if ("com.hwatong.usbmusic".equals(packageName)) {
                            Utils.openMusic(mContext);
                        } else if ("com.hwatong.usbvideo".equals(packageName)) {
                            Utils.openVideo(mContext);
                        } else if ("com.hwatong.usbpicture".equals(packageName)) {
                            Utils.openPicture(mContext);
                        }
                        else {
                            Utils.openApplication(mContext, packageName);
                        }
                        return true;
                    } else if ("EXIT".equals(operation)) {
                        if ("com.hwatong.radio.ui".equals(packageName)) {
                            Intent intent = new Intent("com.hwatong.voice.CLOSE_FM");
                            mContext.sendBroadcast(intent);
                        } else if ("com.hwatong.btphone.ui".equals(packageName)) {
                            Intent intent = new Intent("com.hwatong.voice.CLOSE_BTPHONE");
                            mContext.sendBroadcast(intent);
                        } else if ("com.hwatong.btmusic.ui".equals(packageName)) {
                            Intent intent = new Intent("com.hwatong.voice.CLOSE_BTMUSIC");
                            mContext.sendBroadcast(intent);
                        } else if ("com.hwatong.usbmusic".equals(packageName)) {
                            Intent intent = new Intent("com.hwatong.voice.CLOSE_MUSIC");
                            mContext.sendBroadcast(intent);
                        } else if (Utils.getMapPackage()/*"com.mxnavi.mxnavi"*/.equals(packageName)) {
                            Utils.closeMap(mContext);
                        } else if ("com.hwatong.usbvideo".equals(packageName)) {
                            Intent intent = new Intent("com.hwatong.voice.CLOSE_VIDEO");
                            mContext.sendBroadcast(intent);
                        } else if ("com.hwatong.usbpicture".equals(packageName)) {
                            Intent intent = new Intent("com.hwatong.voice.CLOSE_PICTURE");
                            mContext.sendBroadcast(intent);
                        } else {
                            Utils.closeApplication(mContext, packageName);
                        }
                        return true;
                    }
                    break;
                }
            }

            if (i >= paklist.size()) {
                if ("LAUNCH".equals(operation) || operation.isEmpty()) {
                    if (Utils.launchMatchApp(mContext, name, raw)) {
                        return true;
                    }
                } else if ("EXIT".equals(operation)) {
                    String pkgName = Utils.getMatchAppPkgName(name);
                    if (pkgName != null) {
                        if ("com.hwatong.radio.ui".equals(pkgName)) {
                            Intent intent = new Intent("com.hwatong.voice.CLOSE_FM");
                            mContext.sendBroadcast(intent);
                        } else if ("com.hwatong.btphone.ui".equals(pkgName)) {
                            Intent intent = new Intent("com.hwatong.voice.CLOSE_BTPHONE");
                            mContext.sendBroadcast(intent);
                        } else if ("com.hwatong.btmusic.ui".equals(pkgName)) {

                            Intent intent = new Intent("com.hwatong.voice.CLOSE_BTMUSIC");
                            mContext.sendBroadcast(intent);
                        } else if ("com.hwatong.usbmusic".equals(pkgName)) {
                            Intent intent = new Intent("com.hwatong.voice.CLOSE_MUSIC");
                            mContext.sendBroadcast(intent);
                        } else if (Utils.getMapPackage()/*"com.mxnavi.mxnavi"*/.equals(pkgName)) {
                            Utils.closeMap(mContext);
                        } else if ("com.hwatong.usbvideo".equals(pkgName)) {
                            Intent intent = new Intent("com.hwatong.voice.CLOSE_VIDEO");
                            mContext.sendBroadcast(intent);
                        } else if ("com.hwatong.usbpicture".equals(pkgName)) {
                            Intent intent = new Intent("com.hwatong.voice.CLOSE_PICTURE");
                            mContext.sendBroadcast(intent);
                        } else {
                            Utils.closeApplication(mContext, pkgName);
                        }
                        return true;
                    }
                }
                L.d(thiz, "======================" + name);
                if ("LAUNCH".equals(operation) && "蓝牙".equals(name)) {
                    try {
                    	Intent intent = new Intent();
                        intent.setClassName("com.hwatong.settings","com.hwatong.f70.bluetooth.BaseBluetoothSettingActivity");
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                        btService.setEnable(true);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        return false ;
		            }
                    return true;
                }
                
                /**
                 *日历操作
                 */
                if("LAUNCH".equals(operation) && "日历".equals(name)){
                    Intent intent = new Intent();
                    intent.setClassName("com.hwatong.calendar","com.hwatong.calendar.CalendarActivity");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                    return true;
                }
                if("EXIT".equals(operation) && "日历".equals(name)){
                    mContext.sendBroadcast(new Intent("com.hwatong.voice.CLOSE_CALENDAR"));
                    return true;
                }
            }
        }

        return false;
    }
}
