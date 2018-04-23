package com.hwatong.platformadapter.handle;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.hwatong.ipod.IService;
import com.hwatong.media.MusicEntry;
import com.hwatong.platformadapter.ServiceList;
import com.hwatong.platformadapter.Tips;
import com.hwatong.platformadapter.Utils;
import android.canbus.ICanbusService;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;
/**
 * @author caochao
 */
public class HandleMusicControl {
    
    private static final String TAG = "Voice";
    /**
     * 媒体控制
     */
    private static HandleMusicControl mHandMusicControl = null;
    
    private static ServiceList mServiceList ;
    
    private Context mContext;
    
    private static ICanbusService mCanbusService;
    
    public HandleMusicControl(Context mContext) {
        super();
        this.mContext = mContext;
    }

    public static HandleMusicControl getInstance(Context context, ICanbusService canbusService , ServiceList serviceList) {
        Log.d(TAG, "HandleCarControl init");
        if (mHandMusicControl == null) {
            mHandMusicControl = new HandleMusicControl(context);
        }
        mCanbusService = canbusService;
        mServiceList = serviceList ;
        return mHandMusicControl;
    }
    
    public boolean handleMusicScence(JSONObject result) {
        // 歌名或歌手播放音乐
        String operation = "";
        String song = "";
        String artist = "";
        String album = "";
        String category = "";
        String raw ="";
        // 音源播放音乐
        String source = "";
        try {
            operation = result.getString("operation");
        } catch (JSONException e) {
        }
        try {
            song = result.getString("song");
        } catch (JSONException e) {
        }
        try {
            artist = result.getString("artist");
        } catch (JSONException e) {
        }
        try {
            album = result.getString("album");
        } catch (JSONException e) {
        }
        try {
            category = result.getString("category");
        } catch (JSONException e) {
        }
        try {
            raw = result.getString("rawText");
        } catch (JSONException e) {
        }

        try {
            source = result.getString("source");
        } catch (JSONException e) {
        }       
        /**
         * 播放iPod
         */
        if("PLAY".equals(operation) && source.equalsIgnoreCase("ipod")){
            Log.d("caochao", "play ipod , mServiceList =" +mServiceList );
            IService service = null; 
            if(mServiceList!=null){
                service = mServiceList.getIPodService();
                try {
                    Log.d("caochao", "ipod connect:"+service.isAttached());
                    if(service!=null && !service.isAttached()){
                        
                        Tips.setCustomTipUse(true);
                        Tips.setCustomTip("设备未连接");
                        return false ;
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            Intent intent = new Intent("com.hwatong.ipod.DEVICE_ATTACHED") ;
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            return true ;
        }       
        /**
         * 播放图片
         */
        if("PLAY".equals(operation) && raw.contains("图片")){
            if(mServiceList!= null){
                com.hwatong.media.IService mediaService = mServiceList.getMediaService() ;
                if(mediaService!=null){
                    try {
                        List list = mediaService.getPictureList() ;
                        if(list.size()==0){
                            Tips.setCustomTipUse(true);
                            Tips.setCustomTip("暂无本地图片");
                            return false;                          
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
            
            mContext.sendBroadcast(new Intent("com.hwatong.media.PLAY_PICTURE"));
            return true ;
        }
        /**
         * 播放音乐
         */
        Log.d(TAG, "source=" +source.isEmpty() );
        if("PLAY".equals(operation) && (source.isEmpty() || "usb".equalsIgnoreCase(source))){
            if(mServiceList!= null){
                com.hwatong.media.IService mediaService = mServiceList.getMediaService() ;
                if(mediaService!=null){
                    try {
                        List list = mediaService.getMusicList() ;
                        if(list.size()==0){
                            Tips.setCustomTipUse(true);
                            Tips.setCustomTip("暂无本地音乐");
                            return false;                          
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (!song.isEmpty() || !artist.isEmpty()) {
                if(mServiceList!= null){
                    com.hwatong.media.IService mediaService = mServiceList.getMediaService() ;
                    List<MusicEntry> list = null;
                    if(mediaService!=null){
                        try {
                            list = mediaService.getMusicList();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    if(list!=null){
                        MusicEntry musicEntry  ;
                        boolean b = false ;
                        Intent intent = new Intent("com.hwatong.voice.PLAY_MUSIC");
                        for (int i = 0; i < list.size(); i++) {
                            musicEntry = list.get(i);
                            
                            if(!song.isEmpty() && musicEntry.mFilePath.contains(song)){
                                b = true ;
                                intent.putExtra("song", song);
                            }
                            if( !artist.isEmpty() && musicEntry.mFilePath.contains(artist)){
                                b = true ;
                                intent.putExtra("artist", artist);
                            }
                            if(b){
                                mContext.sendBroadcast(intent);
                                return true ;
                            }
                        }
                    }
                }
                Tips.setCustomTipUse(true);
                Tips.setCustomTip("没有匹配的歌曲");
                return false;
            }
            mContext.sendBroadcast(new Intent("com.hwatong.voice.PLAY_MUSIC"));
            return true ;
        }        
        com.hwatong.bt.IService btService = null ;
        com.hwatong.media.IService mediaService = null ;
        if(mServiceList!=null){
            btService = mServiceList.getBtService() ;
            mediaService = mServiceList.getMediaService() ;
        }
        
        Log.d(TAG, "handleMusicScence source: " + source);
        if (!source.isEmpty()) {
            Log.d(TAG, "handleMusicScence, " + operation + " source: " + source);
            if ("蓝牙音乐".equals(source)) {
                if ("CLOSE".equals(operation)) {
                    Intent intent = new Intent("com.hwatong.voice.CLOSE_BTMUSIC");
                    mContext.sendBroadcast(intent);
                } else {
                    if(btService !=null){
                        try {
                            boolean connect = btService.getConnectState();
                            if(!connect){
                                Tips.setCustomTipUse(true);
                                Tips.setCustomTip("蓝牙未连接");
                                return true ;
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    
                    Intent intent = new Intent("com.hwatong.btmusic.CONNECTED");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        mContext.startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        return false ;
                    }
                }
                return true;
            } else if ("u盘".equalsIgnoreCase(source) || "sd".equalsIgnoreCase(source) || "本地".equals(source)) {
                
                if(mediaService!=null){
                    Log.d("TAG" , "musicService!=null");
                    try {
                        List list = mediaService.getMusicList();
                        if(list == null || list.size() ==0){
                            Tips.setCustomTipUse(true);
                            Tips.setCustomTip("没有本地音乐");
                            return true ;
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        return false ;
                    }
                }
                Utils.openMusic(mContext);
                Log.d(TAG, "handleMusicScence openApplication: " + source);
                return true;
            }
        }
        // 来首歌
        if ("PLAY".equals(operation) && album.isEmpty() && category.isEmpty()) {
            final String pkg = "com.hwatong.usbmusic";
            Utils.openMusic(mContext);
            Log.d(TAG, "handleMusicScence openApplication: " + pkg);
            return true;
        }
        return false;
    }
    
}
