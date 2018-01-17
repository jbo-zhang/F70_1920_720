package com.hwatong.projectmode.fragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.widget.TextView;
import com.hwatong.projectmode.R;
import com.hwatong.projectmode.fragment.base.BaseFragment;
import com.hwatong.projectmode.utils.SystemUtil;
import com.tbox.service.FlowInfo;
import com.tbox.service.ITboxCallback;
import com.tbox.service.NetworkStatus;
import com.tbox.service.UpdateStep;
import android.util.Log;
/**
 * 系统参数
 * @author caochao
 */
public class SystemProfileFragment extends BaseFragment{
    
    private Context mContext ;
    
	private TextView tvCpuTemerature;
	private TextView tvRam;
	private TextView tvRom;
	private TextView tvBluetoothAddress;
	private TextView tvWlanAddress;
	private TextView tvDeviceSeries;
	private TextView tvNetSignal;
	private TextView tvTboxIccid;
	
	private static final int BT_ADDRESS = 1;
	private static final int TBOX_ICCID = 2;
	private static final int TBOX_ICCID_DISPLAY = 4;
	private static final int TBOX_RSSI = 3 ;
	private Handler handler = new Handler(){
	    @Override
	    public void handleMessage(Message msg) {
	        super.handleMessage(msg);
	        switch (msg.what) {
            case BT_ADDRESS:
                if(btService!=null){
                    try {
                        setFormatText(tvBluetoothAddress, btService.getLocalAddress());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case TBOX_ICCID:
                if(tboxService!=null){
                    
                    try {
                        String iccid = Integer.toString(tboxService.getIccid());
                        if(iccid!=null){
                            //setFormatText(tvTboxIccid, iccid);
                        }
                             
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case TBOX_ICCID_DISPLAY:
                if(tboxService!=null){
                    byte[] data= (byte[])msg.obj;
                    setFormatText(tvTboxIccid, new String(data));
                }
                break;
            case TBOX_RSSI:
                if(tboxService!=null){
                    
                    try {
                        String rssi = Integer.toString(tboxService.getNetworkStatus().RSSI) ;
                        if(rssi!=null){
                            setFormatText(tvNetSignal, rssi); 
                        }
                        
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }                
                }
            }
	        
	            
	    }
	};
	/**
	 * 蓝牙参数
	 */
    private com.hwatong.bt.IService btService = null ; 
	private ServiceConnection btConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            btService = null ;
        }
        
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            btService = com.hwatong.bt.IService.Stub.asInterface(binder);
            handler.sendEmptyMessage(BT_ADDRESS);
        }
    };
    
    /**
     * TBox参数
     */
    private com.tbox.service.ITboxService tboxService;
    private ServiceConnection tboxConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            tboxService = null ;
        }
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            tboxService = com.tbox.service.ITboxService.Stub.asInterface(binder);
            try {
		       if(tboxService!=null && tboxService.getTboxStatus() == 1){
					tboxService.getIccid();
				}
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
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
                            handler.sendEmptyMessage(TBOX_ICCID);
                            Log.d("caochao" , "onTboxStatusChanged:"+arg0);
                        }
                        
                        @Override
                        public void onNetworkStatusChanged(NetworkStatus arg0)
                                throws RemoteException {
                            handler.sendEmptyMessage(TBOX_RSSI);
                        }
                        
                        @Override
                        public void onLog(byte[] arg0) throws RemoteException {
                            
                        }
                        
                        @Override
                        public void onIccid(byte[] arg0) throws RemoteException {
                            Log.d("caochao" , "onIccid:"+new String(arg0));
                            handler.obtainMessage(TBOX_ICCID_DISPLAY, arg0).sendToTarget();
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
		return R.layout.fragment_system_profile;
	}
	
	@Override
	protected void initViews(View view) {
	    mContext = getActivity() ;
	    mContext.bindService(new Intent("com.hwatong.bt.service"), btConnection, Context.BIND_AUTO_CREATE);
	    mContext.bindService(new Intent("com.tbox.service.TboxService"), tboxConnection, Context.BIND_AUTO_CREATE);
		tvCpuTemerature = (TextView) view.findViewById(R.id.tv_cpu_temperature);
		tvRam = (TextView) view.findViewById(R.id.tv_ram);
		tvRom = (TextView) view.findViewById(R.id.tv_rom);
		tvBluetoothAddress = (TextView) view.findViewById(R.id.tv_bluetooth_address);
		tvWlanAddress = (TextView) view.findViewById(R.id.tv_wlan_address);
		tvDeviceSeries = (TextView) view.findViewById(R.id.tv_device_series);
		tvNetSignal = (TextView) view.findViewById(R.id.tv_net_signal);
		tvTboxIccid = (TextView) view.findViewById(R.id.tv_tbox_iccid);
		setViewsData();
		
	}
	@Override
	public void onDestroy() {
	    super.onDestroy();
	    handler.removeCallbacks(runnable);
	    mContext.unbindService(btConnection);
	    mContext.unbindService(tboxConnection);
	}

	private void setViewsData() {
		setCpuTemerature();
		setRam();
		setRom();
		setWlanAddress();
		setDeviceSeries();		
	}
        /**
         *
         */
        private static final String SERIAL = "/sys/devices/platform/sdhci-esdhc-imx.3/mmc_host/mmc0/mmc0:0001/serial";
	private String getSerial() {
		String temp ;
		File file = new File(SERIAL);
		BufferedReader reader = null ;
		try {
			reader = new BufferedReader(new FileReader(file));
			temp = reader.readLine() ;
		} catch (IOException e) {
			e.printStackTrace();
			temp = "" ;
		} finally{
			if(reader!=null){
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return temp;
	}
	/**
	 * CPU温度节点
	 */
	private static final String CPU_TEMP = "/sys/devices/virtual/thermal/thermal_zone0/temp" ; 
	/**
	 * 获取CPU温度
	 * @return
	 */
	private String getCpuTemp() {
		String temp ;
		File file = new File(CPU_TEMP);
		BufferedReader reader = null ;
		try {
			reader = new BufferedReader(new FileReader(file));
			temp = reader.readLine() ;
		} catch (IOException e) {
			e.printStackTrace();
			temp = "" ;
		} finally{
			if(reader!=null){
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return temp;
	}
	/**
	 * 每隔5秒获取CPU温度
	 */
	private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            setFormatText(tvCpuTemerature , getCpuTemp());
            handler.postDelayed(this, 5000);
        }
    };
	private void setCpuTemerature() {
	    handler.postDelayed(runnable, 0);
	}
	
	private void setRam() {
		setFormatText(tvRam, SystemUtil.getTotalRam(getActivity()));		
	}

	private void setRom() {
		setFormatText(tvRom, SystemUtil.getRomTotalSize(getActivity()));		
	}

	private void setWlanAddress() {
	    WifiManager mManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
	    WifiInfo  info = mManager.getConnectionInfo();
		setFormatText(tvWlanAddress, "00:50:43:02:fe:01");		
	}

	private void setDeviceSeries() {
		setFormatText(tvDeviceSeries, getSerial());		
	}

	private void setFormatText(TextView tv, String str) {
		tv.setText(String.format((String)tv.getText(), str));
	}
	
	
	
	
}
