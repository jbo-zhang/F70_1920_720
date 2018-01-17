package com.hwatong.projectmode.receiver;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.provider.Settings;

public class DebugReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent arg1) {
        boolean isDebug = Settings.Global.getInt(context.getContentResolver(), Settings.Global.ADB_ENABLED, 0)==1 ? true:false ;
        if(isDebug){
            new Handler().postDelayed(new Runnable(){

                @Override
                public void run() {
                    writeAdbFile(1);
                }
                
            }, 5000) ;
            
            
        }
        
    }
    private static final String ADB_DEBUG_FILE = "/sys/devices/platform/imx-i2c.1/i2c-1/1-0019/usb_switch" ;
    private boolean writeAdbFile(int i){
        FileOutputStream os = null ;
        try {
            os = new FileOutputStream(ADB_DEBUG_FILE);
            os.write(Integer.toString(i).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            return false ;
        } finally{
            if(os!=null){
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true ;
    }

}
