package com.hwatong.projectmode.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.os.Build;
import android.os.StatFs;
import android.util.Log;
import android.widget.Toast;

public class Util {
	private static String TAG="Util";
    public static String getExtFromFilename(String filename) {
        int dotPosition = filename.lastIndexOf('.');
        if (dotPosition != -1) {
            return filename.substring(dotPosition + 1, filename.length());
        }
        return "";
    }      

    public static String getNameFromFilename(String filename) {
        int dotPosition = filename.lastIndexOf('.');
        if (dotPosition != -1) {
            return filename.substring(0, dotPosition);
        }
        return filename;
    }	
    
    public static String getNameFromFilepath(String filepath) {
        int pos = filepath.lastIndexOf('/');
        if (pos != -1) {
            return filepath.substring(pos + 1);
        }
        return "";
    }    
    
    public static String makePath(String path1, String path2) {
        if (path1.endsWith(File.separator))
            return path1 + path2;

        return path1 + File.separator + path2;
    }    
    
    public static boolean isPathExist(String path) {
		try {
			File file = new File(path);
			StatFs stat = new StatFs(file.getPath());
			long blockSize = stat.getBlockSize();
			long totalBlocks = stat.getBlockCount();
			if(totalBlocks * blockSize > 0) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
    } 
    
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
    
	//     	常见的一些相同目录，都转换成MediaStore的目录
	public static String converPath(String path) {
		String result = path;
		if (path.startsWith("/udisk")) {
			result = path.replace("/udisk", "/mnt/udisk");
		} else if (path.startsWith("/extsd")) {
			result = path.replace("/extsd", "/mnt/extsd");
		} else {
			final String[] sdcard = { "/sdcard", "/mnt/sdcard",
					"/storage/sdcard0", "/storage/emulated/legacy" };
			for (String s : sdcard) {
				if (path.startsWith(s)) {
					result = path.replace(s, "/storage/emulated/0");
				}
			}
		}

		return result;
	}
	
	public static boolean isTheSamePath(String p1, String p2) {
		p1 = converPath(p1);
		p2 = converPath(p2);
		return p1.equals(p2);
	}
	
	/**
	 * 判断destPath 是srcPath的子目录或同�?目录
	 */
	public static boolean isSubPath(String srcPath, String destPath) {
		srcPath = converPath(srcPath);
		destPath = converPath(destPath);
		
		return destPath.startsWith(srcPath);
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
    
	public static String formatTime(long time) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date(time);
		return sdf.format(date);
	}
	
	public static void showToast(Context context, String text) {
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}
	
	public static void showToast(Context context, int textId) {
		Toast.makeText(context, textId, Toast.LENGTH_SHORT).show();
	}
	public static final int PASTE_OK = 0;
	public static final int PASTE_FAIL = 1;
	public static final int PASTE_LACKSPACE_FAIL = 2;	
	
	private static boolean copyFile(String src, String dest) {
		boolean result = true;

		FileInputStream fi = null;
		FileOutputStream fo = null;
		try {
			File destPlace = new File(dest);
			if (!destPlace.exists()) {
				if (!destPlace.mkdirs())
					return false;
			}

			File srcFile = new File(src);

			String destPath = Util.makePath(dest, srcFile.getName());
			File destFile = new File(destPath);
			if (!destFile.createNewFile())
				return false;

			fi = new FileInputStream(srcFile);
			fo = new FileOutputStream(destFile);
			int count = 102400;
			byte[] buffer = new byte[count];
			int read = 0;
			while ((read = fi.read(buffer, 0, count)) != -1) {
				fo.write(buffer, 0, read);
			}

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
	
	/**加密*/
    private static final int[] conv_table = {
	0x2d, 0xb0, 0x90, 0x9f, 0xe2, 0xd6, 0x42, 0x78,
	0xdf, 0x4f, 0x27, 0xe6, 0x87, 0xc1, 0xbd, 0xf6,
	0x9b, 0x95, 0x0b, 0x69, 0x3d, 0xc0, 0x12, 0xc2,
	0x7a, 0xc6, 0xcc, 0xa7, 0x76, 0x5c, 0x46, 0x58,
	0x32, 0x9a, 0xab, 0xd9, 0x79, 0xfa, 0x00, 0x60,
	0x81, 0x1d, 0x77, 0x5d, 0xb2, 0x82, 0xf0, 0xf3,
	0x02, 0x80, 0xb5, 0x7c, 0x23, 0xdd, 0x15, 0x10,
	0x5f, 0xbb, 0x38, 0x29, 0x39, 0x89, 0x37, 0xae,
	0x57, 0x59, 0x31, 0x49, 0x73, 0x11, 0x4b, 0xc7,
	0x3a, 0x48, 0xea, 0xf7, 0x25, 0x53, 0x0c, 0x35,
	0xb3, 0xbc, 0xeb, 0xa6, 0x24, 0x6f, 0x1f, 0x16,
	0x8b, 0x70, 0x94, 0xb9, 0x30, 0xa5, 0x05, 0x6b,
	0xb7, 0x54, 0x0a, 0x61, 0x0e, 0xff, 0xa9, 0x03,
	0xcd, 0xec, 0x0d, 0x63, 0xf9, 0xca, 0x9c, 0xb8,
	0x07, 0x84, 0x7b, 0x3b, 0xcf, 0xa2, 0x4e, 0xa1,
	0xac, 0x51, 0xc4, 0xc8, 0x66, 0xc9, 0x47, 0xa0,
	0x64, 0x72, 0x55, 0x68, 0xd2, 0x96, 0x06, 0xe4,
	0xdc, 0x2c, 0xd3, 0x26, 0x2f, 0x93, 0x65, 0x34,
	0xef, 0xf5, 0xd1, 0x9d, 0x2e, 0x4d, 0x33, 0xfb,
	0x67, 0xf2, 0x1c, 0xa3, 0xa8, 0x1e, 0xc3, 0xf1,
	0x45, 0x20, 0xaf, 0x98, 0x36, 0x97, 0xba, 0xfc,
	0x2b, 0x09, 0xf4, 0x56, 0xee, 0xe5, 0xad, 0x22,
	0x7f, 0xd5, 0x62, 0xbe, 0x3f, 0xce, 0xc5, 0x3e,
	0x17, 0xa4, 0x04, 0x28, 0x1a, 0x92, 0x5b, 0x0f,
	0x3c, 0x8a, 0x43, 0x19, 0x9e, 0x7e, 0xde, 0x44,
	0x74, 0x83, 0x91, 0x7d, 0x40, 0x8f, 0x6e, 0x8d,
	0x08, 0x4c, 0xd4, 0x01, 0x1b, 0x8e, 0x6d, 0xcb,
	0x6a, 0x5a, 0xe8, 0x75, 0x4a, 0xda, 0xe9, 0x41,
	0xe3, 0xd7, 0x13, 0x71, 0x8c, 0xaa, 0xed, 0xe1,
	0x50, 0x14, 0x88, 0x6c, 0xfe, 0xd8, 0xb4, 0x18,
	0xdb, 0xe0, 0x85, 0xe7, 0x2a, 0x99, 0x5e, 0x52,
	0xb1, 0xd0, 0xf8, 0x21, 0xbf, 0xfd, 0xb6, 0x86,
    };

    public static final int SCRAMBLER = 100;

    public static void crypt_buf(byte[] buf, int off, int count, int pos) {
        int i;
        for (i = 0; i < count; i++) {
            int j = pos ^ (pos >> 8) ^ (pos >> 16) ^ (pos >> 24);
            buf[off] ^= conv_table[(j + SCRAMBLER) & 0xff];
            off++;
            pos++;
        }
    }
    public static String getImageVersion(InputStream is) throws IOException {

		byte[] b = new byte[4096];
		int n, total = 0;
 
		while (total < 1024) {
			n = is.read(b, total, 1024 - total);
			if (n == -1)
				break;
			crypt_buf(b, total, n, total);
			total += n;
		}

		if (total < 1024) {
			Log.e(TAG, "Invalid image, total = " + total);
			return null;
		}

		if (b[0] != '[') {
			Log.e(TAG, "Invalid image, b[0] = " + b[0]);
			return null;
		}

		int end = 1;
		while (end < total) {
			if (b[end] == ']')
				break;
			end++;
		}
		if (end == total) {
			Log.e(TAG, "Invalid image, end = " + end);
			return null;
		}

		String buildNumber = new String(b, 1, end-1);
		Log.d(TAG, "BUILD_NUMBER=" + Build.VERSION.INCREMENTAL);
		Log.d(TAG, "Received BUILD_NUMBER=" + buildNumber);

//		if (buildNumber.equals(Build.VERSION.INCREMENTAL)) {
//			Log.d(TAG, "Current is up to date");
//			return version;
//		}
		return buildNumber;
    }
    
    
    /**
     * 查看是否是升级文件
     * */
    public static boolean checkUpdateFile(File file) {
		try {
			FileInputStream fis=null;
			fis = new FileInputStream(file);
			String version = Util.getImageVersion(fis);	
			fis.close();
			return (version!=null);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return false;
    }
}
	
