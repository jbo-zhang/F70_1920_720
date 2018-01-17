package com.hwatong.projectmode.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hwatong.projectmode.R;
import com.hwatong.projectmode.fragment.base.BaseFragment;
import com.hwatong.projectmode.iview.ISystemUpdateView;
import com.hwatong.projectmode.presenter.SystemUpdatePresenter;
import com.hwatong.projectmode.service.UpdateService;
import com.hwatong.projectmode.ui.ConfirmDialog;
import com.hwatong.projectmode.ui.UpdateDialog;
import com.hwatong.projectmode.ui.ConfirmDialog.OnYesOnclickListener;
import com.hwatong.projectmode.utils.FileUtil;
import com.hwatong.projectmode.utils.L;

public class SystemUpdateFragment extends BaseFragment implements UpdateService.OTAUIStateChangeListener, ISystemUpdateView {

	private static final String thiz = SystemUpdateFragment.class.getSimpleName();

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
	
	private UpdateDialog copyDialog;
	
	private Object lockObject = new Object();
	
	private ListView lvList;
	private List<File> files;
	private FileAdapter fileAdapter;
	
	private Context mContext;

	private String update_file;

	private SystemUpdatePresenter systemUpdatePresenter;

	private UpdateService mService;
	private boolean mBind;
	
	private TextView tvNoFile;

	/* state change will be 0 -> Checked -> Downloading -> upgrading. */

	
	@Override
	protected int getLayoutId() {
		return R.layout.fragment_system_update;
	}
	
	@Override
	protected void initViews(View view) {
		
		mContext = getActivity();
		
		lvList = (ListView) view.findViewById(R.id.lv_list);
		
		tvNoFile = (TextView) view.findViewById(R.id.tv_no_file);
		tvNoFile.setText(getText(R.string.no_img_files));
		
		files = new ArrayList<File>();
		
		fileAdapter = new FileAdapter(getActivity(), files);
		
		lvList.setAdapter(fileAdapter);
		
		setupClickEvent();
		
		systemUpdatePresenter = new SystemUpdatePresenter(this);
		
		systemUpdatePresenter.loadFiles();
		
	}
	
	private void setupClickEvent() {
		lvList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
				final FileAdapter adapter = (FileAdapter) adapterView.getAdapter();
				File file = adapter.getItem(index);
				if (file.isFile()) {
					if (index != fileAdapter.getSelectedIndex()) {
						fileAdapter.setSelectedIndex(index);
					} else {
						fileAdapter.setSelectedIndex(-1);
					}
					fileAdapter.notifyDataSetChanged();
				}
			}
		});
	}

	
	@Override
	public void onStart() {
		super.onStart();

		if (mContext.bindService(new Intent(mContext, UpdateService.class), mConnection, Context.BIND_AUTO_CREATE)) {
			mBind = true;
		} else {
			mBind = false;
		}
	}

	@Override
	public void onStop() {
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
		L.d(thiz, "onPause");
		super.onPause();
	}
	
	
	/**
	 * 升级包列表适配器，给列表设置属性
	 * */
	private class FileAdapter extends BaseAdapter{

		private int selectedIndex = -1;

		private List<File> list;
		private Context context;
		
		public FileAdapter(Context context, List<File> files) {
			this.context = context;
			this.list = files;
		}
		
		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public File getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if(convertView == null) {
				convertView = View.inflate(context, R.layout.lv_item_file, null);
				holder = new ViewHolder();
				holder.tvName = (TextView) convertView.findViewById(R.id.tv_item_name);
				holder.btUpdate = (Button) convertView.findViewById(R.id.bt_item_update);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.tvName.setText(getItem(position).getName());
			
			if (selectedIndex == position) {
				holder.btUpdate.setVisibility(View.VISIBLE);
				convertView.setBackgroundColor(Color.parseColor("#22625e5e"));
			} else {
				holder.btUpdate.setVisibility(View.INVISIBLE);
				convertView.setBackgroundColor(Color.TRANSPARENT);
			}
			
			holder.btUpdate.setFocusable(false);
			
			holder.btUpdate.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					update_file = getItem(position).getPath();
					L.d(thiz, "update_file : " + update_file);
					if (mService != null && update_file != null) {
						mService.setUpdateFile(update_file);
					}
				}
			});
			
			return convertView;
		}
		
		
		public void setSelectedIndex(int index) {
			selectedIndex = index;
		}

		public int getSelectedIndex() {
			return selectedIndex;
		}
		
		
		class ViewHolder {
			public TextView tvName;
			public Button btUpdate;
		}
	}

	
	/**
	 * 绑定是异步的bindService()会立即返回，它不会返回IBinder给客户端。
	 * 要接收IBinder，客户端必须创建一个ServiceConnection的实例并传给bindService()。
	 * ServiceConnection包含一个回调方法，系统调用这个方法来传递要返回的IBinder
	 * */
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = ((UpdateService.ServiceBinder) service).getService();
			mService.setmListener(SystemUpdateFragment.this);
		}

		public void onServiceDisconnected(ComponentName className) {
			mService = null;
		}
	};

	/**
	 * 根据全进去的参数，执行UI的更改
	 * */
	@Override
	public void onUIStateChanged(int state, int error, Object info) {
		mHandler.removeMessages(0);
		mHandler.obtainMessage(0, state, error, info).sendToTarget();
	}

	/**
	 * onUIStateChanged转UI线程到此方法，根据全进去的state，执行UI的更改
	 * */
	private void doUIStateChanged(int state, int error, Object info) {
		L.d(thiz, "doUIStateChanged: " + "state: " + state + " error: " + error + " info: " + info);

		switch (state) {
		case STATE_IN_IDLE:					//初始态
			
			break;

		case STATE_IN_CHECKED:				//检查后反馈
			onStateInChecked(error, info);
			break;

		case STATE_IN_DOWNLOADING:			//下载
			onStateDownload(error, info);
			break;

		case STATE_IN_COPY:					//复制
			onStateDownload(error, info);
			break;

		case STATE_IN_UPGRADING:			//升级
			onStateUpgrade(error, info);
			break;

		case MESSAGE_DOWNLOAD_PROGRESS:		//进度
		case MESSAGE_VERIFY_PROGRESS:
			onProgress(state, error, info);
			break;
		}
	}

	/**
	 * 检查更新包，给mVersionTextView赋值
	 * */
	private void onStateInChecked(int error, Object info) {
		L.d(thiz, "onStateInChecked: " + "error:" + error + " info: " + info);

		if (error == NO_ERROR) {
			if (info instanceof UpdateService.UpdateFileInfo) {

				final String version = ((UpdateService.UpdateFileInfo) info).getVersion();
				final long bytes = ((UpdateService.UpdateFileInfo) info).getSize();

				String length = (String) getText(R.string.length_unknown);
				if (bytes > 0)
					length = byteCountToDisplaySize(bytes, true);

				L.d(thiz, getText(R.string.version) + ":" + version + "\n" + getText(R.string.size) + ":" + length);

				showConfirmDialog(version, length);
				
			}
		} else if (error == ERROR_WIFI_NOT_AVALIBLE) {
			L.d(thiz, getText(R.string.error_needs_wifi) + "");
			
			showErrorDialog("", getText(R.string.error_needs_wifi).toString(), "");
			
		} else if (error == ERROR_CANNOT_FIND_SERVER) {
			L.d(thiz, getText(R.string.error_cannot_connect_server) + "");
			
			showErrorDialog("", getText(R.string.error_cannot_connect_server).toString(), "");
			
		} else if (error == ERROR_WRITE_FILE_ERROR) {
			L.d(thiz, getText(R.string.error_write_file) + "");
			showErrorDialog("", getText(R.string.error_write_file).toString(), "");
			
		} else if (error == ERROR_IMAGE_FILE_ERROR) {
			L.d(thiz, getText(R.string.error_image_file) + "");
			showErrorDialog("", getText(R.string.error_image_file).toString(), "");
			
		} else if(error == ERROR_MMC_SIZE_NOT_MATCH) {			//添加mmc版本不符合错误
			L.d(thiz, getText(R.string.error_mmc_size_not_match) + "");
			showErrorDialog("", getText(R.string.error_mmc_size_not_match).toString(), "");
			
		}
	}

	/**
	 * 加载更新包，给mMessageTextView赋值
	 * */
	private void onStateDownload(int error, Object info) {
		// from start download, it start hide the version again.

		if (error == ERROR_CANNOT_FIND_SERVER) {// 服务器上没有升级包
			
			L.d(thiz, getText(R.string.error_server_no_package).toString());
			showErrorDialog("", getText(R.string.error_server_no_package).toString(), "");
			
		} else if (error == ERROR_WRITE_FILE_ERROR) {// 写入文件错误
			
			L.d(thiz, getText(R.string.error_write_file).toString());
			showErrorDialog("", getText(R.string.error_write_file).toString(), "");
			
		} else if (error == ERROR_IMAGE_FILE_ERROR) {// 错误的升级包
			
			L.d(thiz, getText(R.string.error_image_file).toString());
			showErrorDialog("", getText(R.string.error_image_file).toString(), "");
			
		}
	}

	/**
	 * 更新时，给mMessageTextView赋值
	 * */
	private void onStateUpgrade(int error, Object info) {
		if (error == ERROR_PACKAGE_VERIFY_FAILED) {// 升级包校验失败
			L.d(thiz, getText(R.string.error_package_verify_failed).toString());
			showErrorDialog("", getText(R.string.error_package_verify_failed).toString(), "");
			// meet error in Verify, fall back to check.
			
		} else if (error == ERROR_PACKAGE_INSTALL_FAILED) {// 升级包安装失败
			L.d(thiz, getText(R.string.error_package_install_failed).toString());
			showErrorDialog("", getText(R.string.error_package_install_failed).toString(), "");
			
		}
	}

	/**
	 * 设置任务进度，根据参数给mMessageTextView，mVersionTextView赋值
	 * */
	private void onProgress(int message, int error, Object info) {
		final Long progress = (Long) info;
		L.d(thiz, "progress : " + progress);
		
		if (message == MESSAGE_DOWNLOAD_PROGRESS) {
			showProgress(progress, getText(R.string.copying).toString());
			
		} else if (message == MESSAGE_VERIFY_PROGRESS) {
			showProgress(progress, getText(R.string.verify_package) + "…");
			
		} else if (message == MESSAGE_COPY_PROGRESS) {
			showProgress(progress, getText(R.string.copying).toString());
		}
	}
	
	
	/**
	 * 显示复制进度
	 */
	public void showProgress(final long percent, String title) {
		L.d(thiz, "updateCopyProgress percent: " + percent);
		synchronized (lockObject) {
			if (copyDialog == null) {
				copyDialog = new UpdateDialog(getActivity(), UpdateDialog.STYLE_COPY);
				Window window = copyDialog.getWindow();
				window.setGravity(Gravity.LEFT|Gravity.TOP);
				window.setLayout(571, 250);
				LayoutParams attributes = window.getAttributes();
				attributes.x = 145;
				attributes.y = 60;
				window.setAttributes(attributes);
				copyDialog.show();
				copyDialog.setTitle(title);
				
			} else {
				if (!copyDialog.isShowing()) {
					copyDialog.show();
					copyDialog.setTitle(title);
				}
				
				copyDialog.setTitle(title);
				copyDialog.setProgress((int) percent);
			}
			
		}
	}
	

	private void showConfirmDialog(String version, String length) {
		ConfirmDialog confirmDialog = new ConfirmDialog(getActivity());
		
		confirmDialog.setYesOnclickListener(new OnYesOnclickListener() {
			
			@Override
			public void onYesClick() {
				if (mService != null) {
					mService.startCopyUpgradePackage();
				}
			}
		});
		
		Window window = confirmDialog.getWindow();
		window.setGravity(Gravity.LEFT | Gravity.TOP);
		LayoutParams attributes = window.getAttributes();
		attributes.x = 1280/3 - 190;
		attributes.y = 80;
		window.setAttributes(attributes);
		
		confirmDialog.show();
		
		confirmDialog.setMessage(version, "文件大小: " + length, "确定升级吗?");
	}
	
	
	private void showErrorDialog(String msg1, String msg2, String msg3) {
		if(copyDialog != null && copyDialog.isShowing()) {
			copyDialog.dismiss();
		}
		
		ConfirmDialog confirmDialog = new ConfirmDialog(getActivity());
		
		Window window = confirmDialog.getWindow();
		window.setGravity(Gravity.LEFT | Gravity.TOP);
		LayoutParams attributes = window.getAttributes();
		attributes.x = 1280/3 - 190;
		attributes.y = 80;
		window.setAttributes(attributes);
		
		confirmDialog.show();
		
		confirmDialog.setTitle("出错啦！");
		confirmDialog.setMessage(msg1, msg2, msg3);
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


	@Override
	public void showFiles(List<File> files) {
		if(files == null || files.size() == 0) {
//			Toast makeText = Toast.makeText(getActivity(), "没有找到系统升级文件", Toast.LENGTH_SHORT);
//			makeText.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, -220, 250);
//			makeText.show();
			
			tvNoFile.setVisibility(View.VISIBLE);
			lvList.setVisibility(View.INVISIBLE);
			return;
		}
		
		tvNoFile.setVisibility(View.INVISIBLE);
		lvList.setVisibility(View.VISIBLE);
		
		this.files.clear();
		this.files.addAll(files);

		fileAdapter.notifyDataSetChanged();
	}
}
