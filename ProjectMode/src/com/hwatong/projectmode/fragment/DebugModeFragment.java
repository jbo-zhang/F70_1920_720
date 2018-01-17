package com.hwatong.projectmode.fragment;

import java.io.FileOutputStream;
import java.io.IOException;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import java.util.Arrays;

import com.hwatong.projectmode.R;
import com.hwatong.projectmode.fragment.base.BaseFragment;
import com.hwatong.projectmode.fragment.base.FTPManager;
import com.hwatong.projectmode.fragment.base.FTPManager.FTPListener;
import com.hwatong.projectmode.ui.SwitchButton;
import com.hwatong.projectmode.utils.L;
import com.tbox.service.FlowInfo;
import com.tbox.service.ITboxCallback;
import com.tbox.service.NetworkStatus;
import com.tbox.service.UpdateStep;

/**
 * 	属性persist.sys.log.config和sys.log.config控制系统日志打印，persist.sys.log.config属性重启后仍生效sys.log.config只在本次开机时间内有效
 *  属性persist.sys.gps.log和sys.gps.log控制gps日志打印，persist.sys.gps.log属性重启后仍生效sys.gps.log只在本次开机时间内有效
 * @author zxy time:2017年11月21日
 *
 */

public class DebugModeFragment extends BaseFragment{
	
	private static final String TAG = DebugModeFragment.class.getSimpleName();
	
	private Context mContext ;
	
	private SwitchButton sbAdbDebug;
	private SwitchButton sbSystemLogs;
	private Button sbTboxLogs;
	private SwitchButton sbGpsLogs;
	
	private static final String ADB_DEBUG_FILE = "/sys/devices/platform/imx-i2c.1/i2c-1/1-0019/usb_switch" ;
	
	private static final int DEBUG_ENABLE = 2;
	
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
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case FTPListener.DOWNLOAD:
                sbTboxLogs.setText(R.string.saving);
                break;
            case FTPListener.FINISH:
                sbTboxLogs.setText(R.string.save);
                break;
            case DEBUG_ENABLE:
                sbAdbDebug.setEnabled(true);
                break;
            }
        }
    };
	private com.tbox.service.ITboxService tboxService;
    private ServiceConnection tboxConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            tboxService = null ;
        }
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            tboxService = com.tbox.service.ITboxService.Stub.asInterface(binder);
            if(tboxService!=null){
                try {
                    tboxService.registerTboxCallback(new ITboxCallback.Stub() {
                     
                        @Override
                        public void onVin(byte[] arg0) throws RemoteException {
                            
                        }
                        
                        @Override
                        public void onVersion(byte[] arg0) throws RemoteException {
                            
                        }
                        
                        @Override
                        public void onUpdateStep(UpdateStep arg0) throws RemoteException {
                            
                        }
                        
                        @Override
                        public void onUpdateRsp(int arg0) throws RemoteException {
                            
                        }
                        
                        @Override
                        public void onUpdateCheckResult(int arg0) throws RemoteException {
                            
                        }
                        
                        @Override
                        public void onTboxStatusChanged(int arg0) throws RemoteException {
                            
                        }
                        
                        @Override
                        public void onNetworkStatusChanged(NetworkStatus arg0)
                                throws RemoteException {
                        }
                        
                        @Override
                        public void onLog(byte[] path) throws RemoteException {
                            path = Arrays.copyOfRange(path, 0, path.length-1);
                            final String logPath = new String(path);
                            Log.d("FTPManager" , logPath);
                            new Thread(new Runnable(){
                                @Override
                                public void run() {
                                    
                                    FTPManager ftpManager = FTPManager.getInstance();
                                    ftpManager.setListener(new FTPListener(){

                                        @Override
                                        public void onProcess(int status) {
                                            handler.sendEmptyMessage(status);
                                        }
                                        
                                    });
                                    try {
                                        if(ftpManager.connect()){
                                            ftpManager.downLoad(logPath);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Log.d("ftpManager",e.getMessage()+";"+e.toString());
                                    }
                                }
                                
                            }).start();
                            
                        }
                        
                        @Override
                        public void onIccid(byte[] arg0) throws RemoteException {
                            
                        }
                        
                        @Override
                        public void onIMEI(byte[] arg0) throws RemoteException {
                            
                        }
                        
                        @Override
                        public void onFlow(FlowInfo arg0) throws RemoteException {
                            
                        }
                    });
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    };    	
    
	@Override
	protected int getLayoutId() {
		return R.layout.fragment_debug_mode;
	}
	
	@Override
	protected void initViews(View view) {
	    mContext = getActivity();
	    mContext.bindService(new Intent("com.tbox.service.TboxService"), tboxConnection, Context.BIND_AUTO_CREATE);
        sbAdbDebug = (SwitchButton) view.findViewById(R.id.switch_adb_debug);
        sbSystemLogs = (SwitchButton) view.findViewById(R.id.switch_system_logs);
        sbTboxLogs = (Button) view.findViewById(R.id.switch_tbox_logs);
        sbGpsLogs = (SwitchButton) view.findViewById(R.id.switch_gps_logs);
        
        sbAdbDebug.setChecked(Settings.Global.getInt(mContext.getContentResolver(), Settings.Global.ADB_ENABLED, 0)==1 ? true:false);
        //sbAdbDebug.setEnabled(false);
        sbSystemLogs.setChecked(SystemProperties.getInt("persist.sys.log.config", 0)==1? true:false);
        
        sbGpsLogs.setChecked(SystemProperties.getInt("persist.sys.gps.log", 0)==1? true:false );
        
        
        sbAdbDebug.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    
                    
                    Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.ADB_ENABLED, 1);

                    Log.d(TAG , "ADB SET TRUE");
                    sbAdbDebug.setEnabled(false);
                    handler.postDelayed(new Runnable(){

                        @Override
                        public void run() {
                            writeAdbFile(1);
                            Log.d(TAG , " SET 1");
                        }
                        
                    }, 5000);
                } else {
                    /**
                    writeAdbFile(0);
                    sbAdbDebug.setEnabled(false);
                    handler.postDelayed(new Runnable(){

                        @Override
                        public void run() {
                            handler.sendEmptyMessage(DEBUG_ENABLE);
                        }
                        
                    }, 5000);*/
                }
            }
        });
        
        sbSystemLogs.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				L.d(TAG, "persist.sys.log.config : " + isChecked);
				SystemProperties.set("persist.sys.log.config", isChecked?"1":"0");
			}
		});
        
        sbGpsLogs.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				L.d(TAG, "persist.sys.gps.log : " + isChecked);
				SystemProperties.set("persist.sys.gps.log", isChecked?"1":"0");			
			}
		});
        
        sbTboxLogs.setOnClickListener(new OnClickListener() {        
            @Override
            public void onClick(View v) {
                if(tboxService!=null){
                    try {
                        tboxService.getLog();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }    
                }
            }
        });    
	}
	
	@Override
	public void onDestroy() {
            super.onDestroy() ;
	    mContext.unbindService(tboxConnection);
	}	
}
