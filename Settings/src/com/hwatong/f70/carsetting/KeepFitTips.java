package com.hwatong.f70.carsetting;

import com.hwatong.bt.BtDevice;
import com.hwatong.f70.baseview.BaseFragment;
import com.hwatong.f70.bluetooth.BluetoothDeleteDialog;
import com.hwatong.f70.main.F70CanbusUtils;
import com.hwatong.f70.main.LogUtils;
import com.hwatong.settings.R;
import com.hwatong.settings.widget.SwitchButton;

import android.app.Fragment;
import android.canbus.ServiceStatus;
import android.canbus.ICanbusService;
import android.canbus.IServiceStatusListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.net.Uri;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

/**
 * 
 * @author ljw
 * 
 */
public class KeepFitTips extends BaseFragment implements OnCheckedChangeListener {

	private SwitchButton keepFitSwitch;
	private ICanbusService iCanbusService;// 远程调用接口
	private TextView distance, unit, distance2, unit2;
	private Button resetBtn;
	private static final int DIFF = -32768;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.f70_keepfitsetting,
				container, false);

		initWidget(rootView);
		initService();
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		
		changedActivityImage(this.getClass().getName());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			iCanbusService.removeServiceStatusListener(mIServiceStatusListener);
		} catch (RemoteException e) {
			LogUtils.d(e.toString());
			e.printStackTrace();
		}

		handler.removeCallbacks(null);
	}

	private void initService() {
		iCanbusService = ICanbusService.Stub.asInterface(ServiceManager
				.getService("canbus"));
		try {
			iCanbusService.addServiceStatusListener(mIServiceStatusListener);
		} catch (RemoteException e) {
			LogUtils.d(e.toString());
			e.printStackTrace();
		}

		Message m = Message.obtain(handler, 0);
		handler.sendMessage(m);
	}

	private void initWidget(View rootView) {
		keepFitSwitch = (SwitchButton) rootView
				.findViewById(R.id.switch_keepfit);
		distance = (TextView) rootView.findViewById(R.id.next_keep_instance_value);
		distance2 = (TextView) rootView.findViewById(R.id.next_keep_instance2_value);
		unit = (TextView) rootView.findViewById(R.id.keepfit_unit);
		unit2 = (TextView) rootView.findViewById(R.id.keepfit_unit2);
		
//		keepFitSwitch.setNoNeedAutoFeedback(true);
//		keepFitSwitch.setOnCheckedChangeListener(this);
		
		resetBtn = (Button)rootView.findViewById(R.id.resetdistance);
		resetBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showDeleteBluetoothDialog();
			}
		});
	}
	
//	@Override
//	public void onUserCheckedChanged(CompoundButton buttonView,
//			boolean isChecked) {
//
//	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        putCarSettingsString(this.getActivity().getContentResolver(), "maintenance_remind", isChecked ? "1" : "0");
		setTextEnabled(isChecked);
	}

	/**
	 *
	 */
	private void handleServiceStatusChanged(ServiceStatus status) {
		LogUtils.d("KeepFitSwitchStatus: " + status.getStatus3() + ", KeepFitDistance: " + status.getStatus1() + ", NextCheck: " + status.getStatus2());
		
		updateNextDistanceStatus(status.getStatus1());
		
		updateKeepFitDistance2Status(status.getStatus2());
		
		keepFitSwitch.setOnCheckedChangeListener(null);
		
//		updateKeepFitSwitchStstaus(carConfig.getStatus1());
//		keepFitSwitch.setOnCheckedChangeListener(this);
		
	}

	private void updateNextDistanceStatus(int status) {
		String nextDistance = String.valueOf(status + DIFF);
		distance.setText(nextDistance);
	}
	
	private void updateKeepFitSwitchStstaus(int status) {
		keepFitSwitch.setChecked(status == F70CarSettingCommand.OPEN ? true : false);
		setTextEnabled(status == F70CarSettingCommand.OPEN);
	}
	
	private void updateKeepFitDistance2Status(int status) {
		String nextCheckDistance = String.valueOf(status + DIFF);
		distance2.setText(nextCheckDistance);
	}
	
	private void setTextEnabled(boolean isEnabled) {
		distance.setEnabled(isEnabled);
		distance2.setEnabled(isEnabled);
		
		unit.setEnabled(isEnabled);
		unit2.setEnabled(isEnabled);
		
		resetBtn.setEnabled(isEnabled);
	}
	
	private void showDeleteBluetoothDialog() {
		final KeepFitResetDialog dialog = new KeepFitResetDialog(
				getActivity());
		Window win = dialog.getWindow();
		LayoutParams params = new LayoutParams();
		params.width = LayoutParams.WRAP_CONTENT;
		params.height = LayoutParams.WRAP_CONTENT;
		params.x = -210;// 设置x坐标
		win.setAttributes(params);
		dialog.setOnPositiveListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		dialog.setOnNegativeListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				try{
					iCanbusService.writeCarConfig(F70CarSettingCommand.TYPE_KEEPFIT_RESET,F70CarSettingCommand.OPEN);
				}catch(Exception e){
					e.printStackTrace();	
				}
				dialog.dismiss();
			}
		});

		dialog.show();
	}

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

    		ServiceStatus status = null;

    		try {
    			status = iCanbusService.getLastServiceStatus(KeepFitTips.this.getActivity().getPackageName());
    		} catch (RemoteException e) {
    			e.printStackTrace();
    			status = null;
    		}

            if (status != null)
			    handleServiceStatusChanged(status);
		}
	};

	private final IServiceStatusListener mIServiceStatusListener = new IServiceStatusListener.Stub() {
		@Override
		public void onReceived(ServiceStatus status) {
			Message m = Message.obtain(handler, 0);
			handler.sendMessage(m);
		}
	};

	public static final Uri CONTENT_URI = Uri.parse("content://car_settings/content");

	private static boolean putCarSettingsString(ContentResolver cr, String name, String value) {
		String[] select = new String[] { "value" };
		Cursor cursor = cr.query(CONTENT_URI, select, "name=?", new String[]{ name }, null);
		if (cursor != null && cursor.moveToFirst()) {
			if (cursor != null)
				cursor.close();
			ContentValues values = new ContentValues();
			values.put("value", value);
			cr.update(CONTENT_URI, values, "name=?", new String[]{ name });
		} else {
			if (cursor != null)
				cursor.close();
			ContentValues values = new ContentValues();
			values.put("name", name);
			values.put("value", value);
			cr.insert(CONTENT_URI, values);
		}
		return true;
	}

}
