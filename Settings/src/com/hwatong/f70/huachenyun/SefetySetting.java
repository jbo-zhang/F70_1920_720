package com.hwatong.f70.huachenyun;

import com.hwatong.f70.baseview.BaseFragment;
import com.hwatong.f70.main.LogUtils;
import com.hwatong.settings.R;
import com.hwatong.settings.widget.SwitchButton;
import com.tricheer.remoteservice.IRemoteService;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SefetySetting extends BaseFragment {

	private SwitchButton dangerousWarnSwitch;
	private TextView warnContactName;
	private IRemoteService mService;

	private static final int UPDATE_CONTACT = 0X01;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.f70_safetysetting, container,
				false);

		initWidget(rootView);
		initService();
		return rootView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		changedActivityImage(this.getClass().getName());
	}
	
	private void initService() {
		getActivity().bindService(new Intent("com.tricheer.remoteservice"),
				mServiceConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mService != null) {
			try {
				mService.unregisterCallback(mCallback);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		getActivity().unbindService(mServiceConnection);

	}

	private void initWidget(View rootView) {
		dangerousWarnSwitch = (SwitchButton) rootView
				.findViewById(R.id.switch_dangerous_warn);
		warnContactName = (TextView) rootView
				.findViewById(R.id.warn_contact_name);
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			updateContact((String) msg.obj);
		}
	};

	private void updateContact(String contactName) {
		String name = TextUtils.isEmpty(contactName) ? "--" : contactName;
		warnContactName.setText(name);
	}

	private final ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LogUtils.d("iRemoteservice onServiceConnected");
			mService = com.tricheer.remoteservice.IRemoteService.Stub
					.asInterface(service);
			if (mService != null) {
				try {
					mService.registerCallback(mCallback);
					mService.getContacts();
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
			} else
				LogUtils.d("iRemoteService is null");

		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			if (mService != null) {
				mService = null;
			}
		}
	};

	private final com.tricheer.remoteservice.IRemoteCallback.Stub mCallback = new com.tricheer.remoteservice.IRemoteCallback.Stub() {
		@Override
		public void onLogin() throws RemoteException {
		}

		@Override
		public void onLogout() throws RemoteException {
		}

		@Override
		public void onContacts(String name, String phone)
				throws RemoteException {
			LogUtils.d("onContacts name : " + name + ", phone: " + phone);
			Message msg = Message.obtain();
			msg.what = UPDATE_CONTACT;
			msg.obj = name + " : " + phone;
			handler.sendMessage(msg);
		}

		@Override
		public void onFlow(int arg0, int arg1, int arg2) throws RemoteException {
			
		}

	};

}
