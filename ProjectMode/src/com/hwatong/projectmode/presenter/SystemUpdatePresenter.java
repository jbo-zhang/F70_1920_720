package com.hwatong.projectmode.presenter;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hwatong.projectmode.R;
import com.hwatong.projectmode.iview.ISystemUpdateView;
import com.hwatong.projectmode.utils.L;
import com.hwatong.projectmode.utils.Util;

public class SystemUpdatePresenter {
	private final static String thiz = SystemUpdatePresenter.class.getSimpleName();
	
	private ISystemUpdateView iView;
	
	private List<File> files = new ArrayList<File>();
	private FilenameFilter filenameFilter;
	
	
	private final static String USB_PATH="/mnt/udisk";
    private final static String USB_PATH2="/mnt/udisk2";
    private final static String TFCARD_PATH="/mnt/extsd";
	
	public SystemUpdatePresenter(ISystemUpdateView iView) {
		this.iView = iView;
		filenameFilter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String fileName) {
                File tempFile = new File(dir.getPath(), fileName);
            	L.d(thiz, "tempFile: " + tempFile.getAbsolutePath());
                if (tempFile.isFile())
                	return tempFile.getName().endsWith(".img");
                return false;
            }
        };
		
	}
	public void loadFiles() {
		new ScanTask().execute();
	}
	
	
	/**
     * 异步加载，解决加载文件可能ANR问题
     * @author zjb time:2017年12月25日
     *
     */
    private class ScanTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			Log.d("9095", "doInBackground");
			List<File> usbFiles1 = getFiles(USB_PATH);
	    	if (usbFiles1!=null)
	    		files.addAll(usbFiles1);
	    	List<File> usbFiles2 = getFiles(USB_PATH2);
	    	if (usbFiles2!=null)
	    		files.addAll(usbFiles2);
	    	List<File> tfcardFiles = getFiles(TFCARD_PATH);
	    	if (tfcardFiles!=null)
	    		files.addAll(tfcardFiles);
	    	return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			Log.d("9095", "onPostExecute files size: " + files.size());
			
			iView.showFiles(files);
			
		}
    	
    }
    
    /**
     * 根据目录取升级文件，返回文件列表
     * */
    private List<File> getFiles(String directoryPath) {
    	List<File> fileList= new ArrayList<File>();
    	try {
            File directory = new File(directoryPath);
            
            File[] listFiles = directory.listFiles(filenameFilter);
            
            if(listFiles != null && listFiles.length > 0) {
            	 List<File> fileList1 = Arrays.asList(listFiles);
            	
            	for(File f:fileList1) {
                	if (f.isFile() && Util.checkUpdateFile(f)) {
                		fileList.add(f);
                	}
                }
                Collections.sort(fileList, new Comparator<File>() {
                    @Override
                    public int compare(File file, File file2) {
                        if (file.isDirectory() && file2.isFile())
                            return -1;
                        else if (file.isFile() && file2.isDirectory())
                            return 1;
                        else
                            return file.getPath().compareTo(file2.getPath());
                    }
                });
            }
    	}catch (Exception e) {
    		e.printStackTrace();
    	}
    	
        return fileList;
    }
    
	
	
	
	
}
