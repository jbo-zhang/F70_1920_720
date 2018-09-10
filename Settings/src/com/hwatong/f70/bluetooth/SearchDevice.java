package com.hwatong.f70.bluetooth;

import java.util.ArrayList;
import java.util.List;



import com.hwatong.bt.BtDevice;
import com.hwatong.f70.baseview.BaseFragment;
import com.hwatong.f70.bluetooth.BluetoothContract.Presenter;
import com.hwatong.f70.main.LogUtils;
import com.hwatong.f70.main.Utils;
import com.hwatong.settings.R;


import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 
 * @author PC
 * 
 */
public class SearchDevice extends BaseFragment implements BluetoothContract.View{

	private SearchedDevicesAdapter searchAdapter;
	private ListView searchListView;
	private List<BtDevice> mDiscoveryDeviceList = new ArrayList<BtDevice>();
	private Context context;
	
	private BluetoothContract.Presenter mPresenter;
	private BtDevice mDeviceConnected;
	private int currentConnectedDevice;
	
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
//		mPresenter.bindService();
		context = getActivity();
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.f70_searchlist, container,
				false);
		LogUtils.d("SearchDeviceis onCreateView");
		initWidget(rootView);
		if(mPresenter != null)
			mPresenter.bindService();
		return rootView;
	}
		
	@Override
	public void onResume() {
		LogUtils.d("onResume");
		currentConnectedDevice = -1;
		super.onResume();
		initReceiver();
		changedActivityImage(this.getClass().getName());
	}
	
	@Override
	public void onPause() {
		LogUtils.d("onPause");
		super.onPause();
		if(context != null)
			context.unregisterReceiver(receiver);
	}
	
	@Override
	public void onStop() {
		LogUtils.d("onStop");
		super.onStop();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mDiscoveryDeviceList.clear();
		searchAdapter.notifyDataSetChanged();
		LogUtils.d("DeviceList onDestroy");
		handler.removeCallbacksAndMessages(null);
		if(mPresenter != null) {
			mPresenter.stopDiscovery();
			mPresenter.unregisterBtCallback();
			mPresenter.unbindService();
		}
		if(searchFinish != null)
			searchFinish = null;
	}

	/**
	 *
	 */
	@Override
	public void onHiddenChanged(boolean hidden) {
		LogUtils.d("onHiddenChanged");
		if (hidden) {
			mDiscoveryDeviceList.clear();
		} else {
		}
		super.onHiddenChanged(hidden);
	}

	private void initWidget(View rootView) {
		searchListView = (ListView) rootView.findViewById(R.id.searched_list);
		searchAdapter = new SearchedDevicesAdapter(mDiscoveryDeviceList);
		searchListView.setAdapter(searchAdapter);
	}
	
	private void initReceiver() {
		IntentFilter intentFilter = new IntentFilter("com.hwatong.gotosearch.uha");
		intentFilter.addAction("com.hwatong.voice.SELECT_BT");
		if(context != null)
			context.registerReceiver(receiver, intentFilter);
	}
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if(action.equals("com.hwatong.voice.SELECT_BT")) {
				
				int posion = intent.getIntExtra("select", -1) - 1;
				LogUtils.d("ifly connect bt: " + posion);
				if(!mDiscoveryDeviceList.isEmpty() && (mDiscoveryDeviceList.get(posion) != null)) {
					if(!isDeviceConnected() || (isDeviceConnected() && mDeviceConnected.addr.equals(mDiscoveryDeviceList.get(posion))))
						mPresenter.connectDevice(mDiscoveryDeviceList.get(posion));
				}
					mPresenter.connectDevice(mDiscoveryDeviceList.get(posion));
			} else if(action.equals("com.hwatong.gotosearch.uha")) {
				LogUtils.d("SearchDevice receive");
				mDiscoveryDeviceList.clear();
				searchAdapter.notifyDataSetChanged();
				mPresenter.startDiscovery();
			}
		}
		
	};


	/************************************** Searched device list adapter ******************************/

	class ViewHolder {
		TextView mName;
		Button connDevice;
	}

	class SearchedDevicesAdapter extends BaseAdapter {
		private List<BtDevice> deviceList;
		ViewHolder viewHolder = null;
		private LayoutInflater inflater;

		private int expandPosition = -1;

		public SearchedDevicesAdapter(List<BtDevice> list) {
			this.deviceList = list;
			this.inflater = LayoutInflater.from(getActivity());
		}

		@Override
		public int getCount() {
			return deviceList.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = inflater.inflate(R.layout.f70_searchdevice_item,
						null);
				viewHolder.mName = (TextView) convertView
						.findViewById(R.id.searchdevice_name);
				viewHolder.connDevice = (Button) convertView
						.findViewById(R.id.device_conn);

				convertView.setTag(viewHolder);
			} else
				viewHolder = (ViewHolder) convertView.getTag();
			
			synchronized (deviceList) {
				BtDevice d = deviceList.get(position);
				if (d.name.isEmpty()) {
					viewHolder.mName.setText(R.string.bluetooth_unknown_device);
				} else {
					LogUtils.d("update searchlist: " + d.name);
					viewHolder.mName.setText(d.name);
				}

			}

			convertView
					.setBackgroundResource(R.drawable.system_setting_selector);
			if (expandPosition == position) {
				viewHolder.connDevice.setVisibility(View.VISIBLE);
			} else {
				viewHolder.connDevice.setVisibility(View.GONE);
			}
			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					  
		            if(expandPosition == position){
		                expandPosition = -1;
		            }else{
		                expandPosition = position;
		            }
		            notifyDataSetChanged();
				}
			});
			
			viewHolder.connDevice.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					mPresenter.connectDevice(mDiscoveryDeviceList.get(position));
					currentConnectedDevice = position;
				}
			});
			return convertView;
		}

	}
	
	private void updateSearchList(BtDevice device) {
		if (!mDiscoveryDeviceList.contains(device)) {
			synchronized (mDiscoveryDeviceList) {
				mDiscoveryDeviceList.add(device);
			}
			searchAdapter.notifyDataSetChanged();
		}
	}
	
//	/**
//	 * tips for disconnecting or disconnecting bluetooth device
//	 */
//	private Toast bluetoothConnectedInfoToast(int layoutId) {
//		LayoutInflater inflater = ((Activity) context).getLayoutInflater();
//		View layout = inflater.inflate(layoutId,
//				(ViewGroup) ((Activity) context)
//						.findViewById(R.id.bluetooth_disconn));
//		Toast toast = new Toast(context);
//		toast.setGravity(Gravity.LEFT | Gravity.CENTER, 60, 0);
//		toast.setDuration(3000);
//		toast.setView(layout);
//		return toast;
//	}
//	
//	/**
//	 * show progress dialog
//	 */
//	private void showBluetoothSearchingProgress() {
//		if(getActivity() == null) {
//			handler.removeMessages(ERROR_ACTIVITY);
//			handler.sendEmptyMessageDelayed(ERROR_ACTIVITY, 100);
//			return;
//		} 
//		searchProgressDialog = new BluetoothProgressDialog(getActivity(),
//				getActivity().getResources().getString(R.string.bluetooth_srarching));
//		searchProgressDialog.setCanceledOnTouchOutside(true);
//		Window win = searchProgressDialog.getWindow();
//		LayoutParams params = new LayoutParams();
//		params.width = LayoutParams.WRAP_CONTENT;
//		params.height = LayoutParams.WRAP_CONTENT;
//		params.x = -210;
//		win.setAttributes(params);
//		searchProgressDialog.show();
//	}
//	
//	/**
//	 * show progress dialog
//	 */
//	private void showBluetoothConnectingProgress() {
//		if (progressDialog != null) {
//			progressDialog.cancel();
//		}
//		progressDialog = new BluetoothProgressDialog(getActivity(),
//				getActivity().getResources().getString(
//						R.string.f70_bluetooth_pairing));
//		Window win = progressDialog.getWindow();
//		LayoutParams params = new LayoutParams();
//		params.width = LayoutParams.WRAP_CONTENT;
//		params.height = LayoutParams.WRAP_CONTENT;
//		params.x = -210;
//		win.setAttributes(params);
//		progressDialog.show();
//	}
	
	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
		}
		
	};
	
	private boolean isDeviceConnected() {
		if (mDeviceConnected == null) {
			LogUtils.d("none device connected");
			return false;
		} else
			return true;
	}

	@Override
	public void setPresenter(Presenter presenter) {
		mPresenter = presenter;
	}

	@Override
	public void showIsBluetoothOpen(boolean isOpen) {
		
		if(isOpen) {
			mPresenter.getConnectedDevice();
			mPresenter.startDiscovery();
		}
			
	}

	@Override
	public void showIsAutoconnectedOpen(boolean isOpen) {
		
	}

	@Override
	public void showGetBluetoothName(String name) {
		
	}

	@Override
	public void showIsAutoAnswerOpen(boolean isOpen) {
		
	}

	@Override
	public void showDiscoveryDone() {
		if(searchFinish != null)
			searchFinish.onSearchFinish();
	}

	@Override
	public void showUpdateDiscoveryDevice(BtDevice device) {
		updateSearchList(device);
	}

	@Override
	public void showDisconnected() {
		
	}

	@Override
	public void showConnected() {
//		BluetoothCommonView.bluetoothConnectedInfoToast(getActivity(), BluetoothCommonView.CONNECTED_TOAST_LAYOUTID).show();
		Utils.startApp(getActivity(), "com.hwatong.btphone.MAIN");
		if(getActivity() != null)
			getActivity().finish();
//		mDiscoveryDeviceList.remove(currentConnectedDevice);
//		searchAdapter.notifyDataSetChanged();
	}

	@Override
	public void showUpdatePairedDevice(BtDevice device) {
		
	}

	@Override
	public void showUpdateConnectedDeviceChanged(BtDevice device) {
		mDeviceConnected = device;
	}

	@Override
	public void showConnectingTimeout() {
	}

	@Override
	public void showBluetoothStatusChanged(BluetoothStatus status) {
		
		if(status == BluetoothStatus.ON) {
			mPresenter.startDiscovery();
		}
	}
	
	private OnBluetoothSearchFinish searchFinish;
	public interface OnBluetoothSearchFinish {
    	void onSearchFinish();
    }
	
	public void setOnBluetoothSearchFinish(OnBluetoothSearchFinish onBluetoothSearchFinish) {
		this.searchFinish = onBluetoothSearchFinish;
	}
	
}
