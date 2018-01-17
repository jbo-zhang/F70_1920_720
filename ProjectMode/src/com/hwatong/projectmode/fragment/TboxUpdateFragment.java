package com.hwatong.projectmode.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
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
import com.hwatong.projectmode.iview.ITboxUpdateView;
import com.hwatong.projectmode.presenter.TBoxPresenter;
import com.hwatong.projectmode.ui.ConfirmDialog;
import com.hwatong.projectmode.ui.ConfirmDialog.OnYesOnclickListener;
import com.hwatong.projectmode.ui.UpdateDialog;
import com.hwatong.projectmode.utils.FileUtil;
import com.hwatong.projectmode.utils.L;

public class TboxUpdateFragment extends BaseFragment implements ITboxUpdateView {

	private final static String thiz = TboxUpdateFragment.class.getSimpleName();

	private TBoxPresenter tBoxPresenter;
	private ListView lvList;

	private List<File> files;
	private FileAdapter2 fileAdapter2;

	private UpdateDialog copyDialog;
	
	private UpdateDialog updateDialog;

	private Object lockObject = new Object();
	
	private Object lockObject2 = new Object();
	
	private TextView tvNoFile;

	
	@Override
	protected int getLayoutId() {
		return R.layout.fragment_update_tbox;
	}
	
	@Override
	protected void initViews(View view) {
		lvList = (ListView) view.findViewById(R.id.lv_list);
		
		tvNoFile = (TextView) view.findViewById(R.id.tv_no_file);
		tvNoFile.setText(getText(R.string.no_tbox_files));
		
		files = new ArrayList<File>();
		
		fileAdapter2 = new FileAdapter2(getActivity(), files);
		
		lvList.setAdapter(fileAdapter2);
		
		setupClickEvent();
		
		tBoxPresenter = new TBoxPresenter(this);
		tBoxPresenter.initTboxService(getActivity());

		tBoxPresenter.loadFiles();

	}
	
	
	@Override
	public void onDestroy() {
		L.d(thiz,"TboxUpdateFragment onDestroy !");
		tBoxPresenter.unbindTbox(getActivity());
		super.onDestroy();
	}
	

	private void setupClickEvent() {

		lvList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
				final FileAdapter2 adapter = (FileAdapter2) adapterView.getAdapter();
				File file = adapter.getItem(index);
				if (file.isFile()) {
					if (index != fileAdapter2.getSelectedIndex()) {
						fileAdapter2.setSelectedIndex(index);
					} else {
						fileAdapter2.setSelectedIndex(-1);
					}
					fileAdapter2.notifyDataSetChanged();
				}
			}
		});

	}
	
	/**
	 * 升级包列表适配器，给列表设置属性
	 * */
	private class FileAdapter2 extends BaseAdapter{

		private int selectedIndex = -1;

		private List<File> list;
		private Context context;
		
		public FileAdapter2(Context context, List<File> files) {
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
					tBoxPresenter.updateTbox(getItem(position));
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
	 * 显示文件列表
	 */
	@Override
	public void showFiles(final List<File> files) {
		getActivity().runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				tvNoFile.setVisibility(View.INVISIBLE);
				lvList.setVisibility(View.VISIBLE);
				
				TboxUpdateFragment.this.files.clear();
				TboxUpdateFragment.this.files.addAll(files);

				fileAdapter2.notifyDataSetChanged();
			}
		});
	}
	

	/**
	 * 显示确认升级弹窗
	 */
	@Override
	public void showConfirmDialog(File file) {
		showTboxUpdateDialog(file);
	}

	private void showTboxUpdateDialog(File file) {
		String fileSize = FileUtil.convertStorage(file.length());
		ConfirmDialog confirmDialog = new ConfirmDialog(getActivity());
		
		confirmDialog.setYesOnclickListener(new OnYesOnclickListener() {
			
			@Override
			public void onYesClick() {
				tBoxPresenter.confirmUpdate();
			}
		});
		
		Window window = confirmDialog.getWindow();
		window.setGravity(Gravity.LEFT | Gravity.TOP);
		LayoutParams attributes = window.getAttributes();
		attributes.x = 1280/3 - 190;
		attributes.y = 80;
		window.setAttributes(attributes);
		
		confirmDialog.show();
		
		confirmDialog.setMessage(file.getName(), "文件大小: " + fileSize, "确定升级TBOX吗?");
	}
	
	/**
	 * 显示复制进度
	 */
	@Override
	public void showCopyProgress(final long percent) {
		L.d(thiz, "updateCopyProgress percent: " + percent);
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
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
					} else {
						if (!copyDialog.isShowing()) {
							copyDialog.show();
						}
						copyDialog.setProgress((int) percent);
					}
					
				}
			}
		});
	}

	/**
	 * 升级结果
	 */
	@Override
	public void showUpdateResult(int result, final String info) {
		
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				
				synchronized (lockObject2) {
					if (updateDialog != null) {
						updateDialog.setProgress(100);
						if (updateDialog.isShowing()) {
							updateDialog.dismiss();
						}
					}
				}
				
				Toast makeText = Toast.makeText(getActivity(), info, Toast.LENGTH_SHORT);
				makeText.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, -220, 250);
				makeText.show();
			}
		});
	}


	/**
	 * 复制完成
	 */
	@Override
	public void copyEnd() {
		L.d(thiz, "copyEnd()");
		synchronized (lockObject) {
			if (copyDialog != null) {
				copyDialog.setProgress(100);
				if (copyDialog.isShowing()) {
					L.d(thiz, "copydialog dismiss");
					copyDialog.dismiss();
				}
			}
		}
		tBoxPresenter.startUpdate();
	}

	/**
	 * 没有找到文件
	 */
	@Override
	public void showNoFiles() {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
//				Toast makeText = Toast.makeText(getActivity(), "没有找到TBOX升级文件", Toast.LENGTH_SHORT);
//				makeText.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, -220, 250);
//				makeText.show();
				
				tvNoFile.setVisibility(View.VISIBLE);
				lvList.setVisibility(View.INVISIBLE);
			}
		});
	}

	/**
	 * 升级开始
	 */
	@Override
	public void showUpdateStart() {
		L.d(thiz, "showUpdateStart 开始升级！");
	}
	
	/**
	 * 沒有連接設備
	 */
	@Override
	public void showNoDevices() {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast makeText = Toast.makeText(getActivity(), getText(R.string.tbox_dev_not_connected), Toast.LENGTH_SHORT);
				makeText.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, -220, 250);
				makeText.show();
			}
		});
	}



	/**
	 * 显示升级进度
	 */
	@Override
	public void showUpdateProgress(final String fileName, final int step) {
		L.d(thiz, "showUpdateProgress step: " + step);
		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				synchronized (lockObject2) {
					if (updateDialog == null) {
						updateDialog = new UpdateDialog(getActivity(), UpdateDialog.STYLE_UPDATE);
						Window window = updateDialog.getWindow();
						window.setGravity(Gravity.LEFT|Gravity.TOP);
						window.setLayout(571, 250);
						LayoutParams attributes = window.getAttributes();
						attributes.x = 145;
						attributes.y = 60;
						window.setAttributes(attributes);
						updateDialog.show();
						updateDialog.setTitle(fileName);
					} else {
						if (!updateDialog.isShowing()) {
							updateDialog.show();
							updateDialog.setTitle(fileName);
						}
						updateDialog.setProgress((int) step);
					}
				}
			}
		});
	}

	@Override
	public void ftpCreatFailed() {
		Toast makeText = Toast.makeText(getActivity(), getText(R.string.ftp_dir_create_failed), Toast.LENGTH_SHORT);
		makeText.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, -220, 250);
		makeText.show();
	}

}
