package com.hwatong.f70.bluetooth;

import java.util.ArrayList;
import java.util.List;

import com.hwatong.bt.BtDevice;
import com.hwatong.f70.baseview.BaseFragment;
import com.hwatong.f70.bluetooth.BluetoothContract.Presenter;
import com.hwatong.f70.bluetooth.SearchDevice.OnBluetoothSearchFinish;
import com.hwatong.f70.main.LogUtils;
import com.hwatong.f70.main.Utils;
import com.hwatong.settings.R;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 
 * @author ljw
 * 
 */
public class DeviceList extends BaseFragment implements BluetoothContract.View {

	private ListView mPairedListView;
	private Context context;

	private List<BtDevice> mPairedDeviceList = new ArrayList<BtDevice>();

	private int willDeleteDevice;
	private DevicesAdapter adapter;

	private BtDevice mDeviceConnected;

	private BluetoothContract.Presenter mPresenter;
	
	private static final int HIDE_INPUTMODE = 0x01;
	private static final int HIDE_INPUTMODE_DELAY = 100;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
//		mPresenter.bindService();
		context = getActivity();
	}
//
//	@Override
//	public void onDetach() {
//		super.onDetach();
//		mPresenter.unregisterBtCallback();
//		mPresenter.unbindService();
//	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.f70_devicelist, container,
				false);
		context = getActivity();
		initWidget(rootView);
		if(mPresenter != null)
			mPresenter.bindService();

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		if(mPresenter != null)
			mPresenter.isBluetoothOpen();
		changedActivityImage(this.getClass().getName());
		
		handler.sendEmptyMessageDelayed(HIDE_INPUTMODE, HIDE_INPUTMODE_DELAY);
	}

	@Override
	public void onPause() {
		super.onPause();
		LogUtils.d("DeviceList onPause");
		// TODO
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(mPresenter != null) {
			mPresenter.unregisterBtCallback();
			mPresenter.unbindService();
		}
		LogUtils.d("DeviceList onDestroy");
	}

	private void initWidget(View rootView) {
		mPairedListView = (ListView) rootView.findViewById(R.id.paired_list);
		adapter = new DevicesAdapter(getActivity(), mPairedDeviceList);
		mPairedListView.setAdapter(adapter);
						
	}
	
	private final Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			hideInputMode();
		}
		
	};
	
	private void hideInputMode() {
		hideInputMethod();
	}

	/**************************************** device list adapter ******************************************/

	class ViewHolder {
		TextView mName;
		Button connOrDisconn;
		Button deleteDevice;
	}

	class DevicesAdapter extends BaseAdapter {
		private List<BtDevice> list;
		ViewHolder viewHolder = null;
		private LayoutInflater inflater;

		public DevicesAdapter(Context context, List<BtDevice> list) {
			this.list = list;
			this.inflater = LayoutInflater.from(getActivity());
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public BtDevice getItem(int position) {
			return list.get(position);
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
				convertView = inflater.inflate(R.layout.f70_devicelist_item,
						null);
				viewHolder.mName = (TextView) convertView
						.findViewById(R.id.device_name);
				viewHolder.connOrDisconn = (Button) convertView
						.findViewById(R.id.device_disconnorconn);
				viewHolder.deleteDevice = (Button) convertView
						.findViewById(R.id.device_deleteorconn);
				convertView.setTag(viewHolder);
			} else
				viewHolder = (ViewHolder) convertView.getTag();

			synchronized (list) {
				BtDevice d = list.get(position);
				if (d.name.isEmpty()) {
					viewHolder.mName.setText(R.string.bluetooth_unknown_device);
				} else {
					viewHolder.mName.setText(d.name);
				}

				if (isDeviceConnected() && d.addr.equals(mDeviceConnected.addr)) {
					viewHolder.connOrDisconn
							.setText(R.string.bluetooth_disconnect);
				} else {
					viewHolder.connOrDisconn
							.setText(R.string.bluetooth_connect);
				}
			}

			String buttonText = getActivity().getResources().getString(
					R.string.bluetooth_connect);
//			setTextInButton(buttonText, viewHolder.connOrDisconn, viewHolder.deleteDevice);
			
			viewHolder.connOrDisconn.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							if (isDeviceConnected()
									&& mDeviceConnected.addr
											.equals(mPairedDeviceList
													.get(position).addr)) {
								mPresenter.disconnectDevice();
							} else {
								mPresenter.connectDevice(mPairedDeviceList
										.get(position));
								// showBluetoothConnectingProgress();
							}

						}

					});

			viewHolder.deleteDevice.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							willDeleteDevice = position;
							showDeleteBluetoothDialog();
						}
					});
			return convertView;
		}
	}

	/**************************************** device list adapter ******************************************/

	private void updatePairedList(BtDevice device) {
		if (!mPairedDeviceList.contains(device)) {
			synchronized (mPairedDeviceList) {
				mPairedDeviceList.add(device);
			}
			adapter.notifyDataSetChanged();
		}
	}

	/**
	 * 
	 * @param text 
	 * @param btn 
	 * @param btn2 
	 * 
	 */
	public void setTextInButton(final String text, final Button btn, final Button btn2) {
		OnGlobalLayoutListener ll = new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				btn.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				int width = btn.getWidth() - btn.getPaddingLeft()
						- btn.getPaddingRight();
				int len = text.length();
				LogUtils.d("button width: " + width + ", text length: " + len);
				// btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (width /
				// (len)) > 20 ? 20 : (float) (width / (len)));
				int buttonWidth = ((width / (len)) >= 16 && (width / (len)) <= 45) ? 140 : 95;
				btn.setWidth(buttonWidth);
				btn2.setWidth(buttonWidth);
			}
		};
		btn.getViewTreeObserver().addOnGlobalLayoutListener(ll);
	}

	private boolean isDeviceConnected() {
		if (mDeviceConnected == null) {
			LogUtils.d("none device connected");
			return false;
		} else
			return true;
	}

	// /**
	// * tips for disconnecting or disconnecting bluetooth device
	// */
	// private Toast bluetoothConnectedInfoToast(int layoutId) {
	// LayoutInflater inflater = ((Activity) context).getLayoutInflater();
	// View layout = inflater.inflate(layoutId,
	// (ViewGroup) ((Activity) context)
	// .findViewById(R.id.bluetooth_disconn));
	// Toast toast = new Toast(context);
	// toast.setGravity(Gravity.LEFT | Gravity.CENTER, 60, 0);
	// toast.setDuration(3000);
	// toast.setView(layout);
	// return toast;
	// }

	/**
	 * show the dialog if intented to delete bluetooth device
	 */
	private void showDeleteBluetoothDialog() {
		final BluetoothDeleteDialog dialog = new BluetoothDeleteDialog(
				getActivity());
		Window win = dialog.getWindow();
		LayoutParams params = new LayoutParams();
		params.width = LayoutParams.WRAP_CONTENT;
		params.height = LayoutParams.WRAP_CONTENT;
		params.x = -210;
		win.setAttributes(params);
		final BtDevice deleteDevice = mPairedDeviceList.get(willDeleteDevice);
		final String dialogTitle = deleteDevice.name;
		dialog.setTitle(dialogTitle);
		dialog.setOnPositiveListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mPairedDeviceList.remove(willDeleteDevice);
				adapter.notifyDataSetChanged();
				mPresenter.deleteDevice(deleteDevice);
				dialog.dismiss();
			}
		});
		dialog.setOnNegativeListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});

		dialog.show();
	}

	/**
	 * show progress dialog
	 */
	// private void showBluetoothConnectingProgress() {
	// if (progressDialog != null) {
	// progressDialog.cancel();
	// }
	// progressDialog = new BluetoothProgressDialog(getActivity(),
	// getActivity().getResources().getString(
	// R.string.f70_bluetooth_pairing));
	// progressDialog.setCanceledOnTouchOutside(false);
	// Window win = progressDialog.getWindow();
	// LayoutParams params = new LayoutParams();
	// params.width = LayoutParams.WRAP_CONTENT;
	// params.height = LayoutParams.WRAP_CONTENT;
	// params.x = -210;// 设置x坐标
	// win.setAttributes(params);
	// progressDialog.show();
	// }

	@Override
	public void setPresenter(Presenter presenter) {
		mPresenter = presenter;
	}

	@Override
	public void showIsBluetoothOpen(boolean isOpen) {
		if (isOpen) {
			mPresenter.getConnectedDevice();
			mPresenter.startGetPairedList();
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

	}

	@Override
	public void showUpdateDiscoveryDevice(BtDevice device) {

	}

	@Override
	public void showDisconnected() {
		LogUtils.d("ALREADY_DISCONNECTED");
		mPresenter.getConnectedDevice();
		// if (!isDeviceConnected())
		// bluetoothConnectedInfoToast(BluetoothCommonView.DISCONNECTED_SUCCESS_TOAST_LAYOUTID).show();
	}

	@Override
	public void showConnected() {
		mPresenter.getConnectedDevice();
		mPairedDeviceList.clear();
		adapter.notifyDataSetChanged();
		mPresenter.startGetPairedList();
		// 当前有设备连接时
		// if (progressDialog != null) {
		// progressDialog.cancel();
		// }
		// BluetoothCommonView.bluetoothConnectedInfoToast(getActivity(),
		// BluetoothCommonView.CONNECTED_TOAST_LAYOUTID).show();
//		Utils.startApp(getActivity(), "com.hwatong.btphone.MAIN");
//		if (getActivity() != null)
//			getActivity().finish();
	}

	@Override
	public void showUpdatePairedDevice(BtDevice device) {
		updatePairedList(device);
	}

	@Override
	public void showUpdateConnectedDeviceChanged(BtDevice device) {
		mDeviceConnected = device;
		adapter.notifyDataSetChanged();
	}

	@Override
	public void showConnectingTimeout() {
		// if (progressDialog.isShowing())
		// progressDialog.cancel();
		// bluetoothConnectedInfoToast(BluetoothCommonView.DISCONNECTED_TOAST_LAYOUTID).show();
	}

	@Override
	public void showBluetoothStatusChanged(BluetoothStatus status) {

	}
}
