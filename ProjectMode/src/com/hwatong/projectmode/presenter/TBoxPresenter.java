package com.hwatong.projectmode.presenter;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.hwatong.projectmode.iview.ITboxUpdateView;
import com.hwatong.projectmode.utils.FileUtil;
import com.hwatong.projectmode.utils.L;
import com.hwatong.projectmode.utils.TimerTaskUtil;
import com.tbox.service.FlowInfo;
import com.tbox.service.ITboxService;
import com.tbox.service.NetworkStatus;
import com.tbox.service.UpdateStep;

public class TBoxPresenter {

	private static final String thiz = TBoxPresenter.class.getSimpleName();

	private static final String path = "/sdcard/ftp/";
	
	private static final String ftpPath = "/sdcard/ftp";
	
	private final static String USB_PATH="/mnt/udisk";
	private final static String USB_PATH2="/mnt/udisk2";
	private final static String TFCARD_PATH="/mnt/extsd";
	
	private Context context;
	
	private ITboxUpdateView tboxView;
	
	// TBOX服务
	private ITboxService mTboxService;
	
	private FilenameFilter filenameFilter;
	
	private int i;
	

	public TBoxPresenter(ITboxUpdateView tboxView) {
		this.tboxView = tboxView;
	}
	
	/**
	 * U盘文件路径
	 */
	private String src;
	
	/**
	 * ftp文件路径
	 */
	private String des;
	
	/**
	 * 升级方法参数
	 */
	private String updateName;
	
	/**
	 * 
	 */
	private String fileName;
	
	/**
	 * 初始化TBOX服务
	 */
	public void initTboxService(Context context) {
		this.context = context;
		context.bindService(new Intent("com.tbox.service.TboxService"), tboxConnection, Context.BIND_AUTO_CREATE);
		setFilter("^.*\\.tarbz2$");
	}
	
	public void unbindTbox(Context context) {
		if(mTboxService != null) {
			try {
				L.d(thiz, "unregisterTboxCallback");
				mTboxService.unregisterTboxCallback(mTboxCallback);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		context.unbindService(tboxConnection);
		
		tboxView = null;
	}
	
	/**
	 * 加载可选文件
	 */
	public void loadFiles() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				List<File> files = getFiles(USB_PATH);
				if(files == null || files.size() == 0) {
					files = getFiles(USB_PATH2);
				} 
				
				L.d(thiz, "" + files);
				
				if(files != null && files.size() > 0) {
					if(tboxView != null) {
						tboxView.showFiles(files);
					}
				} else {
					if(tboxView != null) {
						tboxView.showNoFiles();
					}
				}
			}
		}).start();
		
		
	}
	
	/**
	 * 初始化数据，显示确认弹窗
	 * @param file
	 */
	public void updateTbox(File file) {
		if(file == null) {
			return;
		}
		
		src = file.getAbsolutePath();
		des = path + file.getName();
		updateName = "/" + file.getName();
		
		fileName = file.getName();
		
		fileName = fileName.substring(0, fileName.lastIndexOf("."));
		
		File ftpDirectory = new File(ftpPath);
		if (!ftpDirectory.exists()) {
			if(!ftpDirectory.mkdirs() && tboxView != null) {
				tboxView.ftpCreatFailed();
				return ;
			}
		}
		
		if(tboxView != null) {
			tboxView.showConfirmDialog(file);
		}
	}
	
	/**
	 * 确认升级，拷贝文件
	 */
	public void confirmUpdate() {
		L.d(thiz, "src: " + src + " des: " + des) ;
		new Thread(new Runnable() {
			@Override
			public void run() {
				FileUtil.copyFile(src, des, tboxView);
			}
		}).start();
	}
	
	/**
	 * 拷贝文件完之后调用，开始升级
	 */
	public void startUpdate() {
		L.d(thiz, "start update filename : " + updateName);
		if(mTboxService != null) {
			try {
				
				if(mTboxService.getTboxStatus() == 0) {
					tboxView.showNoDevices();
				}
				
				mTboxService.update(updateName);
				
				if(tboxView != null) {
					tboxView.showUpdateStart();
				}
				
				L.d(thiz, "after start update filename : " + updateName);
				
				//for test
//				TimerTaskUtil.startTimer("update_progress", 0, 100, new TimerTask() {
//					
//					@Override
//					public void run() {
//						if(tboxView != null) {
//							tboxView.showUpdateProgress(fileName, i++);
//						}
//						if(i >= 88) {
//							TimerTaskUtil.cancelTimer("update_progress");
//							if(tboxView != null) {
//								tboxView.showUpdateResult(0, "升级成功！");
//							}
//							i = 0;
//						}
//					}
//				});
				
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	private ServiceConnection tboxConnection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			try {
				if(mTboxService != null) {
					mTboxService.unregisterTboxCallback(mTboxCallback);
					L.d(thiz, "mTboxService already unregister");
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder serv) {
			L.d(thiz, "ITboxService onServiceConnected");
			mTboxService = com.tbox.service.ITboxService.Stub.asInterface(serv);
			if (mTboxService != null) {
				try {
					int status = mTboxService.getTboxStatus();
					mTboxService.registerTboxCallback(mTboxCallback);
					L.d(thiz, "ServiceConnected getTboxStatus: " + status + "\n" + "mTboxService already register");
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			} else {
				L.d(thiz, "mTboxService is null");
			}
		}
	};

	private final com.tbox.service.ITboxCallback mTboxCallback = new com.tbox.service.ITboxCallback.Stub() {

		@Override
		public void onFlow(FlowInfo arg0) throws RemoteException {

		}

		@Override
		public void onIMEI(byte[] arg0) throws RemoteException {

		}

		@Override
		public void onIccid(byte[] arg0) throws RemoteException {

		}

		@Override
		public void onLog(byte[] arg0) throws RemoteException {

		}

		@Override
		public void onNetworkStatusChanged(NetworkStatus arg0) throws RemoteException {

		}

		@Override
		public void onTboxStatusChanged(int arg0) throws RemoteException {
			L.d(thiz, "onTboxStatusChanged arg0 : " + arg0);
		}

		/**
		 *  0：成功
			1：失败
		 */
		@Override
		public void onUpdateCheckResult(int result) throws RemoteException {
			L.d(thiz, "onUpdateCheckResult result : " + result);
			TimerTaskUtil.cancelTimer("update_progress");
			i = 0;
			if(tboxView != null) {
				switch (result) {
				case 0:
					tboxView.showUpdateResult(0, "升级成功！");
					break;
				case 1:
					tboxView.showUpdateResult(0, "升級失败！");
					break;
				default:
					break;
				}
			}
		}

		/**
		 *  0：成功
			1:文件拆包解压错误
			2:文件路径错误
			3:文件MD5校验错误
			4:文件名错误
			5:MCU升级失败
			6:SOC升级失败
			7:升级文件下载失败
		 */
		@Override
		public void onUpdateRsp(int result) throws RemoteException {
			L.d(thiz, "onUpdateRsp result : " + result);
			if(tboxView != null) {
				switch (result) {
				case 0:
					tboxView.showUpdateResult(0, "成功！");
					break; 
				case 1:
					tboxView.showUpdateResult(0, "文件拆包解压错误！");
					break;
				case 2:
					tboxView.showUpdateResult(0, "文件路径错误！");
					break;
				case 3:
					tboxView.showUpdateResult(0, "文件MD5校验错误！");
					break;
				case 4:
					tboxView.showUpdateResult(0, "文件名错误！");
					break;
				case 5:
					tboxView.showUpdateResult(0, "MCU升级失败！");
					break;
				case 6:
					tboxView.showUpdateResult(0, "SOC升级失败！");
					break;
				case 7:
					tboxView.showUpdateResult(0, "升级文件下载失败！");
				break;
				default:
					break;
				}
			}
		}

		@Override
		public void onUpdateStep(UpdateStep step) throws RemoteException {
			L.d(thiz,"onUpdateStep step " + step);
			TimerTaskUtil.startTimer("update_progress", 0, step.mUnitStep, new TimerTask() {
				
				@Override
				public void run() {
					if(tboxView != null) {
						tboxView.showUpdateProgress(fileName, i++);
					}
					if(i >= 100) {
						TimerTaskUtil.cancelTimer("update_progress");
						i = 0;
					}
				}
			});
		}

		@Override
		public void onVersion(byte[] arg0) throws RemoteException {
			L.d(thiz, "onVersion arg0 : " + arg0);
		}

		@Override
		public void onVin(byte[] arg0) throws RemoteException {
			L.d(thiz, "onVersion onVin : " + arg0);
		}

	};
	
	
	 /**
     * 设置过滤字符串
     * */
    public void setFilter(final String filter) {
        filenameFilter = new FilenameFilter() {

            @Override
            public boolean accept(File file, String fileName) {
                File tempFile = new File(String.format("%s/%s", file.getPath(), fileName));
                if (tempFile.isFile())
                    return tempFile.getName().matches(filter);
                return true;
            }
        };
    }
	
	/**
     * 根据目录取升级文件，返回文件列表
     * */
    private List<File> getFiles(String directoryPath) {
    	List<File> fileList= new ArrayList<File>();
    	try {
            File directory = new File(directoryPath);
            List<File> fileList1 = Arrays.asList(directory.listFiles(filenameFilter));
            L.d(thiz,"fileList1 : " + fileList1);
            for(File f:fileList1) {
            	if (f.isFile() && FileUtil.checkUpdateFile(f)) {
            		fileList.add(f);
            	}
            }
    	}catch (Exception e) {
    		e.printStackTrace();
    	}
        return fileList;
    }

}
