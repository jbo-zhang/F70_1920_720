package com.hwatong.sysupdate;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hwatong.settings.ota.UpdateService;
import com.hwatong.sysupdate.R;

/**
 * 升级的交互碎片
 * */
public class SystemUpdate extends Fragment implements UpdateService.OTAUIStateChangeListener, OpenFileDialog.OpenDialogListener {

	private static final String TAG = "OTA";

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				doUIStateChanged(msg.arg1, msg.arg2, msg.obj);
				break;
			}
		}
	};

	private Button mUpgradeButton;// 更新按钮
	private Button mQuitButton; //退出按钮
	private TextView mMessageTextView;// 检查更新
	private TextView mVersionTextView;// 版本号
	private ImageView mSpinner;// ProgressBar
	private ProgressBar mDownloadProgress;// 检查，矫正的进度条
	private OpenFileDialog openFileDlg;

	private Context mContext;

	private UpdateService mService;
	private boolean mBind;

	/* state change will be 0 -> Checked -> Downloading -> upgrading. */

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);
	}

	/**
	 * 初始化升级界面
	 * */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		final View view = inflater.inflate(R.xml.system_update_fragment, null);

		mUpgradeButton = (Button) view.findViewById(R.id.upgrade_button);
		mUpgradeButton.setOnClickListener(mUpgradeListener);
		
		mQuitButton = (Button) view.findViewById(R.id.bt_quit);
		mQuitButton.setOnClickListener(mUpgradeListener);
		
		mMessageTextView = (TextView) view.findViewById(R.id.message_text_view);
		mVersionTextView = ((TextView) view.findViewById(R.id.version_text_view));
		
		mSpinner = (ImageView) view.findViewById(R.id.spinner);
		mSpinner.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.rotate));

		mDownloadProgress = (ProgressBar) view.findViewById(R.id.download_progress_bar);

		mVersionTextView.setVisibility(View.INVISIBLE);
		mDownloadProgress.setVisibility(View.INVISIBLE);
		mUpgradeButton.setVisibility(View.INVISIBLE);
		mQuitButton.setVisibility(View.INVISIBLE);

		mContext = getActivity();
		openFileDlg = new OpenFileDialog(mContext);
		OpenFileDialog.ok_clicj_flag = false;
		
		
		openFileDlg.setFilter("^.*\\.img$");
		
		openFileDlg.setOpenDialogListener(this);

		// hlw 点击返回键直接退出
		openFileDlg.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface arg0) {
				// TODO Auto-generated method stub

				Log.e("1111", "" + OpenFileDialog.ok_clicj_flag);
				if (!OpenFileDialog.ok_clicj_flag)
					OnCancel();
			}

		});
		openFileDlg.show();

		return view;
	}

	@Override
	public void onStart() {
		Log.d(TAG, "onStart");
		super.onStart();

		if (mContext.bindService(new Intent(mContext, UpdateService.class), mConnection, Context.BIND_AUTO_CREATE)) {
			mBind = true;
		} else {
			mBind = false;
		}
	}

	@Override
	public void onStop() {
		Log.d(TAG, "onStop");
		if (mService != null)
			mService.setmListener(null);
		if (mBind) {
			mContext.unbindService(mConnection);
		}
		mHandler.removeMessages(0);
		super.onStop();
	}

	@Override
	public void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();
	}

	/**
	 * 绑定是异步的bindService()会立即返回，它不会返回IBinder给客户端。
	 * 要接收IBinder，客户端必须创建一个ServiceConnection的实例并传给bindService()。
	 * ServiceConnection包含一个回调方法，系统调用这个方法来传递要返回的IBinder
	 * */
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = ((UpdateService.ServiceBinder) service).getService();
			mService.setmListener(SystemUpdate.this);
			if (update_file != null) {
				mService.setUpdateFile(update_file);
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			mService = null;
		}
	};

	/** 监听升级按钮 */
	private View.OnClickListener mUpgradeListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.bt_quit:
				getActivity().finish();
				break;
			case R.id.upgrade_button:
				Log.v(TAG, "upgrade button clicked.");
				new Thread(new Runnable() {
					public void run() {
						if (mService != null)
							mService.startCopyUpgradePackage();
					}
				}).start();
				break;
			default:
				break;
			}
		}
	};

	/**
	 * 根据全进去的参数，执行UI的更改
	 * */
	public void onUIStateChanged(int state, int error, Object info) {
		mHandler.removeMessages(0);
		mHandler.obtainMessage(0, state, error, info).sendToTarget();
	}

	/**
	 * 根据全进去的state，执行UI的更改
	 * */
	private void doUIStateChanged(int state, int error, Object info) {
		Log.v(TAG, "doUIStateChanged: " + "state: " + state + " error: " + error + " info: " + info);

		switch (state) {
		case STATE_IN_IDLE:
			mVersionTextView.setVisibility(View.INVISIBLE);
			mDownloadProgress.setVisibility(View.INVISIBLE);
			mUpgradeButton.setVisibility(View.INVISIBLE);
			mQuitButton.setVisibility(View.INVISIBLE);
			break;

		case STATE_IN_CHECKED:
			onStateInChecked(error, info);
			break;

		case STATE_IN_DOWNLOADING:
			onStateDownload(error, info);
			break;

		case STATE_IN_COPY:
			onStateDownload(error, info);
			break;

		case STATE_IN_UPGRADING:
			onStateUpgrade(error, info);
			break;

		case MESSAGE_DOWNLOAD_PROGRESS:
		case MESSAGE_VERIFY_PROGRESS:
			onProgress(state, error, info);
			break;
		}
	}

	/**
	 * 检查更新包，给mVersionTextView赋值
	 * */
	private void onStateInChecked(int error, Object info) {
		Log.v(TAG, "onStateInChecked: " + "error:" + error + " info: " + info);

		mSpinner.clearAnimation();
		mSpinner.setVisibility(View.INVISIBLE);

		if (error == 0) {
			if (info instanceof UpdateService.UpdateFileInfo) {

				final String version = ((UpdateService.UpdateFileInfo) info).getVersion();
				final long bytes = ((UpdateService.UpdateFileInfo) info).getSize();

				mMessageTextView.setText(getText(R.string.have_new));

				String length = (String) getText(R.string.length_unknown);
				if (bytes > 0)
					length = byteCountToDisplaySize(bytes, false);

				mVersionTextView.setText(getText(R.string.version) + ":" + version + "\n" + getText(R.string.size) + ":" + length);

				mVersionTextView.setVisibility(View.VISIBLE);
				mUpgradeButton.setVisibility(View.VISIBLE);
				mQuitButton.setVisibility(View.VISIBLE);

			}
		} else if (error == ERROR_WIFI_NOT_AVALIBLE) {
			mMessageTextView.setText(getText(R.string.error_needs_wifi));
			mQuitButton.setVisibility(View.VISIBLE);
		} else if (error == ERROR_CANNOT_FIND_SERVER) {
			mMessageTextView.setText(getText(R.string.error_cannot_connect_server));
			mQuitButton.setVisibility(View.VISIBLE);
		} else if (error == ERROR_WRITE_FILE_ERROR) {
			mMessageTextView.setText(getText(R.string.error_write_file));
			mQuitButton.setVisibility(View.VISIBLE);
		} else if (error == ERROR_IMAGE_FILE_ERROR) {
			mMessageTextView.setText(getText(R.string.error_image_file));
			mQuitButton.setVisibility(View.VISIBLE);
		
		//添加mmc版本不符合错误
		} else if(error == ERROR_MMC_SIZE_NOT_MATCH) {
			mMessageTextView.setText(getText(R.string.error_mmc_size_not_match));
			mQuitButton.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 加载更新包，给mMessageTextView赋值
	 * */
	private void onStateDownload(int error, Object info) {
		// from start download, it start hide the version again.
		mVersionTextView.setVisibility(View.INVISIBLE);
		mUpgradeButton.setVisibility(View.INVISIBLE);
		mQuitButton.setVisibility(View.INVISIBLE);
		mSpinner.setVisibility(View.INVISIBLE);
		mDownloadProgress.setVisibility(View.VISIBLE);

		if (error == ERROR_CANNOT_FIND_SERVER) {// 服务器上没有升级包
			// in this case, the build.prop already found but the server don't
			// have upgrade package
			// report as "Server Error: Not have upgrade package";
			mMessageTextView.setText(getText(R.string.error_server_no_package));
			mQuitButton.setVisibility(View.VISIBLE);
		} else if (error == ERROR_WRITE_FILE_ERROR) {// 写入文件错误
			mMessageTextView.setText(getText(R.string.error_write_file));
			mVersionTextView.setVisibility(View.VISIBLE);
			mUpgradeButton.setVisibility(View.VISIBLE);
			mQuitButton.setVisibility(View.VISIBLE);
		} else if (error == ERROR_IMAGE_FILE_ERROR) {// 错误的升级包
			mMessageTextView.setText(getText(R.string.error_image_file));
			mVersionTextView.setVisibility(View.VISIBLE);
			mUpgradeButton.setVisibility(View.VISIBLE);
			mQuitButton.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 更新时，给mMessageTextView赋值
	 * */
	private void onStateUpgrade(int error, Object info) {
		if (error == ERROR_PACKAGE_VERIFY_FAILED) {// 升级包校验失败
			mMessageTextView.setText(getText(R.string.error_package_verify_failed));
			mQuitButton.setVisibility(View.VISIBLE);
			// meet error in Verify, fall back to check.
			// TODO which state should ?
		} else if (error == ERROR_PACKAGE_INSTALL_FAILED) {// 升级包安装失败
			mMessageTextView.setText(getText(R.string.error_package_install_failed));
			mQuitButton.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 设置任务进度，根据参数给mMessageTextView，mVersionTextView赋值
	 * */
	private void onProgress(int message, int error, Object info) {
		final Long progress = (Long) info;

		mDownloadProgress.setProgress(progress.intValue());// 设置任务的进度

		Log.v(TAG, "progress : " + progress);

		if (message == MESSAGE_DOWNLOAD_PROGRESS) {
			mMessageTextView.setText(getText(R.string.download_upgrade_package));
			mVersionTextView.setVisibility(View.INVISIBLE);
			mUpgradeButton.setVisibility(View.INVISIBLE);
			mQuitButton.setVisibility(View.INVISIBLE);
			mSpinner.setVisibility(View.INVISIBLE);
			mDownloadProgress.setVisibility(View.VISIBLE);
		} else if (message == MESSAGE_VERIFY_PROGRESS) {
			mMessageTextView.setText(getText(R.string.verify_package));
			mVersionTextView.setVisibility(View.INVISIBLE);
			mUpgradeButton.setVisibility(View.INVISIBLE);
			mQuitButton.setVisibility(View.INVISIBLE);
			mSpinner.setVisibility(View.INVISIBLE);
			mDownloadProgress.setVisibility(View.VISIBLE);
		} else if (message == MESSAGE_COPY_PROGRESS) {
			mMessageTextView.setText(getText(R.string.copy_upgrade_package));
			mVersionTextView.setVisibility(View.INVISIBLE);
			mUpgradeButton.setVisibility(View.INVISIBLE);
			mQuitButton.setVisibility(View.INVISIBLE);
			mSpinner.setVisibility(View.INVISIBLE);
			mDownloadProgress.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 转换B为k,m,g
	 * */
	public static String byteCountToDisplaySize(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit)
			return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "KMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	private String update_file;

	/**
	 * 选中更新包
	 * */
	@Override
	public void OnSelectedFile(String fileName) {
		update_file = fileName;
		if (mService != null) {
			mService.setUpdateFile(update_file);
		}
	}

	@Override
	public void OnCancel() {
		getActivity().onBackPressed();
	}
}
