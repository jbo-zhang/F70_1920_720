package com.hwatong.projectmode.fragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;
import android.widget.TextView;
import com.hwatong.projectmode.R;
import com.hwatong.projectmode.fragment.base.BaseFragment;
import com.hwatong.projectmode.utils.SystemUtil;


public class VersionInfoFragment extends BaseFragment{
    
    private static final String MAP_VERSION_COMMAND = "cat /mxmap/MXNavi/mxversion.dat";
    
	private TextView tvProductModel;
	private TextView tvAndroidVersion;
	private TextView tvMcuVersion;
	private TextView tvArmVersion;
	private TextView tvMapVersion;
	private TextView tvBluetoothVersion;
	private TextView tvSoundVersion;
	
	private Context mContext ;

    private com.hwatong.bt.IService btService = null ; 
    private ServiceConnection btConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            btService = null ;
        }
        
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            btService = com.hwatong.bt.IService.Stub.asInterface(binder);
            handler.sendEmptyMessage(BT_VERSION);
        }
    };
    
    private static final int BT_VERSION = 1 ;
    private Handler handler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
            case BT_VERSION:
                if(btService!=null){
                    try {
                        setFormatText(tvBluetoothVersion,  btService.getVersion());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
        };
    } ;
    
    
	@Override
	protected int getLayoutId() {
		return R.layout.fragment_version_info;
	}
	
	@Override
	protected void initViews(View view) {
	    mContext = getActivity() ;
	    mContext.bindService(new Intent("com.hwatong.bt.service"), btConnection, Context.BIND_AUTO_CREATE);
		tvProductModel = (TextView) view.findViewById(R.id.tv_product_model);
		tvAndroidVersion = (TextView) view.findViewById(R.id.tv_android_version);
		tvMcuVersion = (TextView) view.findViewById(R.id.tv_mcu_version);
		tvArmVersion = (TextView) view.findViewById(R.id.tv_arm_version);
		tvMapVersion = (TextView) view.findViewById(R.id.tv_map_version);
		tvBluetoothVersion = (TextView) view.findViewById(R.id.tv_bluetooth_version);
		tvSoundVersion = (TextView) view.findViewById(R.id.tv_sound_version);
		
		setViewsData();
		
		if(Build.ID.contains("F70_L")){
		    tvMapVersion.setVisibility(View.GONE);
		    tvSoundVersion.setVisibility(View.GONE);
		}
	}
	
	@Override
	public void onDestroy() {
            super.onDestroy();
	    mContext.unbindService(btConnection);
	}

	private void setViewsData() {
		setProductModel();
		setAndroidVersion();
		setMcuVersion();
		setArmVersion();
		setMapVersion();
		setSoundVersion();
		
	}

	private void setProductModel() {
		setFormatText(tvProductModel, SystemUtil.getSoftwareVersion());
	}

	private void setAndroidVersion() {
		setFormatText(tvAndroidVersion, SystemUtil.getSystemVersion());
	}

	private void setMcuVersion() {
		setFormatText(tvMcuVersion, SystemUtil.getMcuVersionInfo());
	}

	private void setArmVersion() {
		setFormatText(tvArmVersion, SystemUtil.getArmVersion());
	}

	private void setMapVersion() {
	    String version = "" ;
	    Runtime runtime = Runtime.getRuntime();
	    BufferedReader reader = null ;
	    try {
            Process process = runtime.exec(MAP_VERSION_COMMAND);
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuffer stringBuffer = new StringBuffer();
            String line ;
            while ((line = reader.readLine()) != null) {
                stringBuffer.append(line);
            }
            String[] strs = stringBuffer.toString().split(":");
            if(strs.length == 3){
            	version = strs[2] ;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if(reader!=null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
		setFormatText(tvMapVersion, version);
	}

	private void setSoundVersion() {
		setFormatText(tvSoundVersion, "语音版本号");
	}
	
	private void setFormatText(TextView tv, String str) {
		tv.setText(String.format((String)tv.getText(), str));
	}
	

}
