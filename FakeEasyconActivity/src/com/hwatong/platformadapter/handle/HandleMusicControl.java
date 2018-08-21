package com.hwatong.platformadapter.handle;
import java.util.List;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.canbus.ICanbusService;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.os.SystemClock;

import com.hwatong.ipod.IService;
import com.hwatong.media.MusicEntry;
import com.hwatong.platformadapter.ServiceList;
import com.hwatong.platformadapter.Tips;
import com.hwatong.platformadapter.utils.L;
import com.hwatong.platformadapter.utils.Utils;
import com.iflytek.platform.type.PlatformCode;
import com.iflytek.platformservice.PlatformService;
/**
 * @author caochao
 */
public class HandleMusicControl {
    
    private static final String thiz = HandleMusicControl.class.getSimpleName();
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
        L.d(thiz, "HandleMusicControl init");
        if (mHandMusicControl == null) {
            mHandMusicControl = new HandleMusicControl(context);
        }
        mCanbusService = canbusService;
        mServiceList = serviceList ;
        return mHandMusicControl;
    }
    
    
//    public void addToIfly() {
//		try {
//			String[] songs = new String[] {"白昼之月", "宠儿", "飞", "坏与更坏", "让世界毁灭", "热血无赖", "天真有邪", "我梦见你梦见我", "我已经敢想妳", "勿忘你", "一点点", "我们不一样"};
//			for (int i = 0; i < songs.length; i++) {
//				L.d(thiz, songs[i]);
//			}
//			PlatformService.platformCallback.uploadCustomData(PlatformCode.UPLOADTYPE_SONGS, songs);
//		} catch (RemoteException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    }
    
    
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
            L.d(thiz, "play ipod , mServiceList =" +mServiceList );
            IService service = null; 
            if(mServiceList!=null){
                service = mServiceList.getIPodService();
                try {
                	L.d(thiz, "ipod connect:"+service.isAttached());
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
                       // List list = mediaService.getPictureList() ;
                    	L.d(thiz , "musicService!=null picture file size " + mediaService.pictureFileSize());
                        if(mediaService.pictureFileSize()==0){
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
         * 播放音乐 {"operation":"PLAY","source":"手机","focus":"music","rawText":"打开手机里的歌曲"} 
         */
        L.d(thiz, "source=" +source.isEmpty() );
        if("PLAY".equals(operation) && (source.isEmpty() || "usb".equalsIgnoreCase(source))){
            if(mServiceList!= null){
                com.hwatong.media.IService mediaService = mServiceList.getMediaService() ;
                if(mediaService!=null){
                    try {
                       // List list = mediaService.getMusicList() ;
                    	L.d(thiz, "musicService music file size : " + mediaService.musicFileSize());
                        if(mediaService.musicFileSize()==0){
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
                    List<MusicEntry> list = new ArrayList<MusicEntry>();
                    if(mediaService!=null){
					 try {
					  for(int i=0;i<=mediaService.musicFileSize()/2000;i++){
						  List<MusicEntry> temp = mediaService.getMusicList(i);
						  L.d(thiz, "getMusicList temp size : " + temp.size());
						  list.addAll(temp);
					  	}
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
                                
                                //add++ 解决闪主界面
                                SystemClock.sleep(1500);
                                
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
        
        L.d(thiz, "handleMusicScence source: " + source);
        if (!source.isEmpty()) {
            L.d(thiz, "handleMusicScence, " + operation + " source: " + source);
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
                    try {
                    	L.d(thiz , "musicService!=null mediaService music file size " + mediaService.musicFileSize());
                      //  List list = mediaService.getMusicList();
                        if(mediaService.musicFileSize() ==0){
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
                L.d(thiz, "handleMusicScence openApplication: " + source);
                return true;
            }
        }
        
        // 来首歌  {"operation":"PLAY","source":"手机","focus":"music","rawText":"打开手机里的歌曲"}
        if ("PLAY".equals(operation) && album.isEmpty() && category.isEmpty()) {
        	
        	 try {
				if(mediaService.musicFileSize()==0){
				     Tips.setCustomTipUse(true);
				     Tips.setCustomTip("暂无本地音乐");
				     return false;                          
				 }
			} catch (RemoteException e) {
				e.printStackTrace();
			}
        	
        	
            final String pkg = "com.hwatong.usbmusic";
            Utils.openMusic(mContext);
            L.d(thiz, "handleMusicScence openApplication: " + pkg);
            return true;
        }
        return false;
    }
    
}
