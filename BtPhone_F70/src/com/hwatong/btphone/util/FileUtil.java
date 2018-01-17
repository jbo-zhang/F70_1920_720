package com.hwatong.btphone.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {
	
	public static long getFileSize(File file) {
		if (!file.exists()) {
			return 0;
		}

		long size = 0;
		if (file.isDirectory()) {
			File[] list = file.listFiles();
			if(list == null) {
				return size;
			}
			for (File f : list) {
				size += getFileSize(f);
			}
		} else {
			size = file.length();
		}

		return size;
	}    
    
	
    // storage, G M K B
    public static String convertStorage(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else
            return String.format("%d B", size);
    }
    

	private static final String thiz = FileUtil.class.getSimpleName();
	
	public static boolean copyFile(String src, String dest) {
		L.d(thiz, "copyFile src: " + src + " dest: " + dest);
		boolean result = true;

		FileInputStream fi = null;
		FileOutputStream fo = null;
		try {
			File destFile = new File(dest);
			if (!destFile.exists()) {
				if (!destFile.createNewFile())
					return false;
			}

			File srcFile = new File(src);

			fi = new FileInputStream(srcFile);
			fo = new FileOutputStream(destFile);
			int count = 102400;
			byte[] buffer = new byte[count];
			int read = 0;
			int i =0;
			L.d(thiz, "before while");
			while ((read = fi.read(buffer, 0, count)) != -1) {
				L.d(thiz, "in while");
				if((i++) == 10) {
					i=0;
					long percent = (destFile.length() * 100)/srcFile.length();
				}
				fo.write(buffer, 0, read);
			}
			L.d(thiz, "after while");
			
		} catch (FileNotFoundException e) {
			result = false;
			e.printStackTrace();
		} catch (IOException e) {
			result = false;
			e.printStackTrace();
		} catch (Exception e) {
			result = false;
			e.printStackTrace();
		} finally {
			try {
				if (fi != null)
					fi.close();
				if (fo != null)
					fo.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return result;
	}

	public static void deleteFile(String path) {
		File file = new File(path);
		boolean directory = file.isDirectory();
		if (directory) {
			File[] list = file.listFiles();
			for (File f : list) {
				deleteFile(f.getAbsolutePath());							
			}
		}
		
		boolean result = file.delete();					
		if (!directory && result) {
		}
	}

    
    /**
     * 查看是否是升级文件
     * */
    public static boolean checkUpdateFile(File file) {
		if(file.getName().endsWith(".tarbz2")) {
			return true;
		}
    	return false;
    }
}
	
