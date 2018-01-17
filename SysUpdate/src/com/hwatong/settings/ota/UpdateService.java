package com.hwatong.settings.ota;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;

import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.FileUtils;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RecoverySystem;
import android.util.Log;

import com.hwatong.sysupdate.R;
import com.hwatong.sysupdate.Util;

public class UpdateService extends Service {
    private static final String TAG = "UpdateService";
    private static final boolean DBG = true;
    
    private static final String MMC_PATH = "/sys/devices/platform/sdhci-esdhc-imx.3/mmc_host/mmc0/mmc0:0001/name"; 

    public class ServiceBinder extends Binder {
        public UpdateService getService() {
            return UpdateService.this;
        }
    }

    private final IBinder mBinder = new ServiceBinder();

	private OTAServerManager mOTAManager;
	private PowerManager.WakeLock mWakelock;

	private final String mUpdatePackageLocation = "/cache/update.zip";

	private JSONObject parser = null;

	private int mState = 0x01;
	private int mError;
	private Object mInfo;
	private String mSrcUpdateFile;

	private OTAUIStateChangeListener mListener;	

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");

        try {
			mOTAManager = new OTAServerManager(this);
		} catch (MalformedURLException e) {
			mOTAManager = null;
            e.printStackTrace();
		}

		PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
		mWakelock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "OTA Wakelock");
		startCheckingVersion();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        if (intent != null) {
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mBinder;
    }

	public void setmListener(OTAUIStateChangeListener mListener) {
		synchronized(this) {
			this.mListener = mListener;
			if (this.mListener != null)
				this.mListener.onUIStateChanged(mState, mError, mInfo);
		}
	}

	/**报告状态的改变，选择执行UI的改变*/
	private void reportStateChanged(int state, int error, Object info) {
		synchronized(this) {
			mState = state;
			mError = error;
			mInfo = info;
			if (this.mListener != null)
				this.mListener.onUIStateChanged(mState, mError, mInfo);
		}
	}

	public void setUpdateFile(String fileName) {
		synchronized(this) {
			mSrcUpdateFile = fileName;
		}
	}
	
	/**开始检查版本*/
	public void startCheckingVersion() {
		Log.d(TAG, "startCheckingVersion");
    	new Thread(new Runnable() {
    		public void run() {
    			for (;;) {
    				if (checkingVersion())
    					break;
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
    			}
    		}
    	}).start();
	}
	
	/**更新文件的文件信息类*/
	public class UpdateFileInfo {
		
		public UpdateFileInfo(String file, String version, int size) {
			this.version=version;
			this.size=size;
			this.filename=file;
		}
		public String getVersion(){return version;}
		public int getSize() {return size;}
		public String getFileName() {return filename;}
		private String version;
		private int size;
		private String filename;
	};
	
	/**判定是否有外部存储设备*/
	private boolean hasSDCard() {
		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());	
	}

	private String sdcardFileName="/mnt/extsd/update.zip";
	private String usbFileName="/mnt/udisk/update.zip";
	
	private boolean checkUpdateFile(String filename) {
		if (!hasSDCard())
			return false;

		File file = new File(filename); 
		if (file == null || !file.exists()) {
			return false;
		}		
		return true;
	}
	
	/**根据文件名称，取更新文件的详细信息*/
	public UpdateFileInfo getUpdateFileInfo(String filename) {
		File file = new File(filename);
		int size;
		String version;
		try {
			FileInputStream fis=null;
			fis = new FileInputStream(file);
			size = fis.available();
			version = Util.getImageVersion(fis);	
			fis.close();
			return new UpdateFileInfo(filename, version, size);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**检查版本号*/
	private boolean checkingVersion() {
		
		//Log.d(TAG, "checkingVersion检查版本号");

		if (mSrcUpdateFile!=null) {
			UpdateFileInfo info = getUpdateFileInfo(mSrcUpdateFile);
			if (info==null) {
				reportStateChanged(OTAUIStateChangeListener.STATE_IN_CHECKED, OTAUIStateChangeListener.ERROR_IMAGE_FILE_ERROR, null);
			}else {
				
				//adb by zjb ++ 
				//升级包MMC大小
				String updateImgMMCSize = "";
				String systemMMCSize = getMMCSize();
				
				String[] split = info.version.split("\\.");
				if(split.length >= 2) {
					updateImgMMCSize = split[1];
				}
				
				Log.d("9095", "updateImgMMCSize: " + updateImgMMCSize + " systemMMCSize: " + systemMMCSize);
				
				if(updateImgMMCSize.equals("16") || updateImgMMCSize.equals("32")) {
					if(!updateImgMMCSize.equals(systemMMCSize)) {
						Log.d("9095", "mmc size not match!!!");
						reportStateChanged(OTAUIStateChangeListener.STATE_IN_CHECKED, OTAUIStateChangeListener.ERROR_MMC_SIZE_NOT_MATCH, info);
						
					} else {
						Log.d("9095", "mmc size match!!!");
						reportStateChanged(OTAUIStateChangeListener.STATE_IN_CHECKED, OTAUIStateChangeListener.NO_ERROR, info);
					}
				} else {
					Log.d("9095", "update package mmc is not 16 or 32!");
					reportStateChanged(OTAUIStateChangeListener.STATE_IN_CHECKED, OTAUIStateChangeListener.NO_ERROR, info);
				}
				//adb by zjb -- 
			}
			return true;
		}
		return false;
	}
	/**开始加载更新包*/
	public void startDownloadUpgradePackage() {
		Log.d(TAG, "startDownloadUpgradePackage()开始加载更新包");
		new Thread(new Runnable() {
			public void run() {
				downloadUpgradePackage();
			}
		}).start();
	}
	
	/**复制更新包*/
	public void startCopyUpgradePackage() {
		Log.d(TAG, "startCopyUpgradePackage()复制更新包");
		new Thread(new Runnable() {
			public void run() {
				copyUpgradePackage();
			}
		}).start();
	}
	
	/**加载更新包*/
	private void downloadUpgradePackage() {
		
		Log.d(TAG, "downloadUpgradePackage()加载更新包");

		reportStateChanged(OTAUIStateChangeListener.STATE_IN_DOWNLOADING, 0, null);

		URL url;
		try {
			final JSONObject data = parser.getJSONObject("data");
			if (data != null) {
				final String server_url = data.getString("server_url");
				final String file_url = data.getString("url");
				url = new URL(server_url + file_url);
			} else {
				url = null;
			}
		} catch (org.json.JSONException e) {
			e.printStackTrace();
			url = null;
    	} catch (MalformedURLException e) {
    		e.printStackTrace();
    		url = null;
		}

		if (url == null) {
			reportStateChanged(OTAUIStateChangeListener.STATE_IN_DOWNLOADING, OTAUIStateChangeListener.ERROR_CANNOT_FIND_SERVER, null);

			return;
		}

		File targetFile = new File(mUpdatePackageLocation);
		try {
			targetFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();

			reportStateChanged(OTAUIStateChangeListener.STATE_IN_DOWNLOADING, OTAUIStateChangeListener.ERROR_WRITE_FILE_ERROR, null);

			return;
		}

		try {
			mWakelock.acquire();
			
//			URL url = mConfig.getPackageURL();
			Log.d(TAG, "start downoading package:" + url.toString());
			URLConnection conexion = url.openConnection();
			conexion.setReadTimeout(10000);
			// this will be useful so that you can show a topical 0-100% progress bar

			int lengthOfFile = 96038693;
			lengthOfFile = conexion.getContentLength();			
			// download the file
			InputStream input = new BufferedInputStream(url.openStream());
			OutputStream output = new FileOutputStream(targetFile);
			
			Log.d(TAG, "file size:" + lengthOfFile);
			byte data[] = new byte[100 * 1024];
			long total = 0, count;
			while ((count = input.read(data)) >= 0) {
				total += count;
				
				// publishing the progress....

				Long progress = new Long(total * 100 / lengthOfFile);

				reportStateChanged(OTAUIStateChangeListener.MESSAGE_DOWNLOAD_PROGRESS, 0, progress);

				output.write(data, 0, (int)count);
			}
			
			output.flush();
			output.close();
			input.close();

			// success download, let try to start with install package...
			// we should already in another thread, no needs to create a thread.
			startInstallUpgradePackage();
		} catch (IOException e) {
			e.printStackTrace();

			reportStateChanged(OTAUIStateChangeListener.STATE_IN_DOWNLOADING, OTAUIStateChangeListener.ERROR_WRITE_FILE_ERROR, null);
		} finally {
			mWakelock.release();
			mWakelock.acquire(2);
		}
	}
	
	
	/**复制文件*/
	public boolean copyfile(File fromFile, File toFile,Boolean rewrite ) {
		if (!fromFile.exists()) {
			return false;
		}
		if (!fromFile.isFile()) {
			return false;
		}
		if (!fromFile.canRead()) {
			return false;
		}
	
		if (!toFile.getParentFile().exists()) {
			toFile.getParentFile().mkdirs();
		}
		if (toFile.exists() && rewrite) {
			toFile.delete();
		}
		try {
			java.io.FileInputStream fosfrom = new java.io.FileInputStream(fromFile);
			java.io.FileOutputStream fosto = new FileOutputStream(toFile);
			int size = fosfrom.available();

			byte bt[] = new byte[1024];
			int c;
			float total=0;
			while ((c = fosfrom.read(bt)) > 0) {
				fosto.write(bt, 0, c);
				total+=c;
				Long progress = new Long((long)((total/size)*100));
				reportStateChanged(OTAUIStateChangeListener.MESSAGE_DOWNLOAD_PROGRESS, 0, progress);
			}
			fosfrom.close();
			fosto.close();
			return true;
		} catch (Exception ex) {
			Log.d("readfile", ex.getMessage());
		}
		return false;
	}
	
	/**复制更新包*/
	private void copyUpgradePackage() {
		
		Log.d(TAG, "copyUpgradePackage()复制更新包");

		try{
			mWakelock.acquire();
			
			reportStateChanged(OTAUIStateChangeListener.STATE_IN_COPY, 0, null);
			File fromFile=new File(mSrcUpdateFile);
			FileInputStream is = new FileInputStream(fromFile);
			
//			File targetFile = new File(mUpdatePackageLocation);
			if (handleImageFile(is)) {
				startInstallUpgradePackage();
			}else {
				reportStateChanged(OTAUIStateChangeListener.STATE_IN_COPY, OTAUIStateChangeListener.ERROR_IMAGE_FILE_ERROR, null);
			}
//			if (copyfile(fromFile, targetFile, true)) {
//				startInstallUpgradePackage();
//			}
		} catch (Exception e) {
			e.printStackTrace();

			reportStateChanged(OTAUIStateChangeListener.STATE_IN_COPY, OTAUIStateChangeListener.ERROR_WRITE_FILE_ERROR, null);
		} finally {
			mWakelock.release();
			mWakelock.acquire(2);
		}
	}
	
	/*判断是不是错误的升级包**/
    private boolean handleImageFile(InputStream is) throws IOException {
		byte[] b = new byte[4096];
		int n, total = 0;
 
		while (total < 1024) {
			n = is.read(b, total, 1024 - total);
			if (n == -1)
				break;
			Util.crypt_buf(b, total, n, total);
			total += n;
		}

		if (total < 1024) {
			Log.d(TAG, "Invalid image, total = " + total);
			return false;
		}

		if (b[0] != '[') {
			Log.d(TAG, "Invalid image, b[0] = " + b[0]);
			return false;
		}

		int end = 1;
		while (end < total) {
			if (b[end] == ']')
				break;
			end++;
		}
		if (end == total) {
			Log.d(TAG, "Invalid image, end = " + end);
			return false;
		}

		String buildNumber = new String(b, 1, end-1);
		Log.d(TAG, "BUILD_NUMBER=" + Build.VERSION.INCREMENTAL);//当前的版本
		Log.d(TAG, "Received BUILD_NUMBER=" + buildNumber);//升级到的版本

//		if (buildNumber.equals(Build.VERSION.INCREMENTAL)) {
//			Log.d(TAG, "Current is up to date");
//			return true;
//		}

		File pkg = new File(mUpdatePackageLocation);//("/cache/t.zip");

		if (pkg.exists()) {
			pkg.delete();
		}
		int size = is.available();
		
		try {
			FileOutputStream os = new FileOutputStream(pkg);
			try {
				os.write(b, end + 1, total - end - 1);
				for (;;) {
					n = is.read(b);
					if (n == -1)
						break;
					Util.crypt_buf(b, 0, n, total);
					os.write(b, 0, n);
					total += n;
					Long progress = new Long((long)(((float)total/size)*100));
					reportStateChanged(OTAUIStateChangeListener.MESSAGE_DOWNLOAD_PROGRESS, 0, progress);
				}
			} finally {
				os.flush();
				try {
					os.getFD().sync();
				} catch (IOException e) {
            		e.printStackTrace();
				}
				os.close();
			}
			FileUtils.setPermissions(pkg.getPath(), FileUtils.S_IRUSR|FileUtils.S_IWUSR|FileUtils.S_IRGRP|FileUtils.S_IWGRP|FileUtils.S_IROTH, -1, -1);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
			return false;
		}

		Log.d(TAG, total + " bytes download completed");//现在完成的更新包的大小
		return true;
    }

	public void startInstallUpgradePackage() {
		File recoveryFile = new File(mUpdatePackageLocation);
		
		// first verify package
        try {
        	 mWakelock.acquire();
        	 RecoverySystem.verifyPackage(recoveryFile, recoveryVerifyListener, null);//想验证升级包的正确性，那就要在RecoverySystem.installPackage()
        	 																			//之前调用一下RecoverySystem.verifyPackage()这个函数
        	 																			//百分比
        } catch (IOException e1) {
			reportStateChanged(OTAUIStateChangeListener.STATE_IN_UPGRADING, OTAUIStateChangeListener.ERROR_PACKAGE_VERIFY_FALIED, null);
        	e1.printStackTrace();
        	return;
        } catch (GeneralSecurityException e1) {
			reportStateChanged(OTAUIStateChangeListener.STATE_IN_UPGRADING, OTAUIStateChangeListener.ERROR_PACKAGE_VERIFY_FALIED, null);
        	e1.printStackTrace();
        	return;
        } finally {
        	mWakelock.release();
        }

        // then install package
        try {
        	mWakelock.acquire();
			RecoverySystem.installPackage(this, recoveryFile);//installPackage升级
        } catch (IOException e) {
        	e.printStackTrace();
			reportStateChanged(OTAUIStateChangeListener.STATE_IN_UPGRADING, OTAUIStateChangeListener.ERROR_PACKAGE_INSTALL_FAILED, null);
        	return;
        } catch (SecurityException e){
        	e.printStackTrace();
			reportStateChanged(OTAUIStateChangeListener.STATE_IN_UPGRADING, OTAUIStateChangeListener.ERROR_PACKAGE_INSTALL_FAILED, null);
        	return;
        } finally {
        	mWakelock.release();
        }
        // cannot reach here...

	}

	RecoverySystem.ProgressListener recoveryVerifyListener = new RecoverySystem.ProgressListener() {
		public void onProgress(int progress) {
			Log.d(TAG, "verify progress" + progress);
			reportStateChanged(OTAUIStateChangeListener.MESSAGE_VERIFY_PROGRESS, 0, new Long(progress));
		}
	};

    private boolean mNotificationShown;

	private void setNotificationVisible(boolean visible) {
        if (!visible && !mNotificationShown) {
            return;
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        final int id = android.R.drawable.stat_sys_download_done;

        if (visible) {
	        Notification notif = new Notification.Builder(this)
		        .setContentTitle(getResources().getString(R.string.have_new))
		        .setSmallIcon(android.R.drawable.stat_sys_download_done)
		        .setOngoing(true)
		        .setAutoCancel(false)
		        .setPriority(Notification.PRIORITY_MAX)
		        .setWhen(0)
		        .build();
	        notif.contentIntent = PendingIntent.getActivity(this, id, new Intent("android.settings.SYSTEM_UPDATE"), 0);

	        notificationManager.notify(id, notif);
	    } else {
            notificationManager.cancel(id);
	    }

        mNotificationShown = visible;
    }

	public interface OTAUIStateChangeListener {
		
		final int STATE_IN_IDLE = 0x01;//空闲
		final int STATE_IN_CHECKED = 0x02; // state in checking whether new available.在检查新是否可用。
		final int STATE_IN_DOWNLOADING = 0x03; // state in download upgrade package在下载升级包
		final int STATE_IN_UPGRADING = 0x04;  // In upgrade state在升级状态
		final int STATE_IN_COPY = 0x05; //in copy upgrade package在副本升级包
		
		final int MESSAGE_DOWNLOAD_PROGRESS = 0x11;//消息下载进度
		final int MESSAGE_COPY_PROGRESS = 0x12;//消息副本进度
		final int MESSAGE_VERIFY_PROGRESS = 0x13;//消息验证进度
		final int MESSAGE_STATE_CHANGE = 0x14;//消息状态变化
		final int MESSAGE_ERROR = 0x15;//消息错误
		
		// should be raise exception ? but how to do exception in async mode ?应该提高异常?但在异步模式下异常怎么办?
		final int NO_ERROR = 0;
		final int ERROR_WIFI_NOT_AVALIBLE = 1;  // require wifi network, for OTA app.升级应用请求无线网络
		final int ERROR_CANNOT_FIND_SERVER = 2;//错误找不到服务器
		final int ERROR_PACKAGE_VERIFY_FALIED = 3;//错误包验证失败
		final int ERROR_WRITE_FILE_ERROR = 4;//错误写文件错误
		final int ERROR_NETWORK_ERROR = 5;//错误的网络错误
		final int ERROR_PACKAGE_INSTALL_FAILED = 6;//错误包安装失败
		final int ERROR_PACKAGE_VERIFY_FAILED = 7;//错误包验证失败
		final int ERROR_IMAGE_FILE_ERROR = 8;// 错误图像文件错误
		
		final int ERROR_MMC_SIZE_NOT_MATCH = 9; //MMC大小不符合
		
		// results
		final int RESULTS_ALREADY_LATEST = 1;//最新结果已经

		public void onUIStateChanged(int state, int error, Object info);
		
	}
	
	
	private String getMMCSize() {
		
		String mmcVersion = getMMCVersion();
		
		if(mmcVersion.length() >= 6 && mmcVersion.substring(0, 6).equals("R1J57L")) {
			return "32";
		} else {
			return "16";
		}
	}
	
	
	/**
	 * 获取MCU版本内容
	 * @return
	 */
	public static String getMMCVersion() {
        try {
            FileReader fr = new FileReader(MMC_PATH);
            BufferedReader br = new BufferedReader(fr);
            String tmp = br.readLine();
            Log.d("9095", "mmc: " + tmp);
            br.close();
            fr.close();
            return tmp;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i("TAG", "file not found");
        } catch (IOException e) {
            e.printStackTrace();
        }
		return "";
	}
	
	
	
	

}
