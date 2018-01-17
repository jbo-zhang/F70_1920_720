package com.hwatong.projectmode.iview;

import java.io.File;
import java.util.List;

public interface ITboxUpdateView {
	
	void showFiles(List<File> files);
	
	void showConfirmDialog(File file);
	
	void showCopyProgress(long percent);
	
	void showUpdateResult(int result, String info);
	
	void showNoFiles();
	
	void copyEnd();
	
	void showUpdateStart();
	
	void showUpdateProgress(String fileName, int step);
	
	void ftpCreatFailed();
	
	void showNoDevices();
}
