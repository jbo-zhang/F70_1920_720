package com.hwatong.projectmode.helper;

import java.net.*;
import java.security.GeneralSecurityException;
import java.io.*;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.*;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.RecoverySystem;
import android.util.Log;

public class OTAServerManager {

	private static final String TAG = "OTA";

	private OTAServerConfig mConfig;
	private long mCacheProgress = -1;
	private Context mContext;
	private Handler mSelfHandler;

	public OTAServerManager(Context context) throws MalformedURLException {
		mContext = context;
		mConfig = new OTAServerConfig(Build.PRODUCT);
	}
	
	/**检查网络在线工作*/
	public static boolean checkNetworkOnline(Context context) {
		ConnectivityManager conMgr =  (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);//ConnectivityManager主要管理和网络连接相关的操作
		if ((conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI)).isConnectedOrConnecting()) {
			return true;
		} else {
			return false;
		}
	}
	
	/** return true if needs to upgrade*/
	public static boolean compareLocalVersionToServer(JSONObject parser) {
		if (parser == null) {
			Log.d(TAG, "compareLocalVersion Without fetch remote prop list.");
			return false;
		}
/*		String localNumVersion = Build.VERSION.INCREMENTAL;
		Long buildutc = Build.TIME;
		Long remoteBuildUTC = (Long.parseLong(parser.getProp("ro.build.date.utc"))) * 1000;
		// *1000 because Build.java also *1000, align with it.
		Log.d(TAG, "Local Version:" + Build.VERSION.INCREMENTAL + "server Version:" + parser.getNumRelease());
		boolean upgrade = false;
		upgrade = remoteBuildUTC > buildutc;
		// here only check build time, in your case, you may also check build id, etc.
		Log.d(TAG, "remote BUILD TIME: " + remoteBuildUTC + " local build rtc:" + buildutc);
		return upgrade;
*/
		try {
			final int status = parser.getInt("status");
			Log.d(TAG, "status = " + status);
			if (status != 1)
				return false;

			final JSONObject data = parser.getJSONObject("data");
			if (data == null)
				return false;

			final String localBuildID = Build.ID;
			final String remoteBuildID = data.getString("version");
			Log.d(TAG, "Local BuildID: " + localBuildID + " server Version: " + remoteBuildID);
			return !localBuildID.equals(remoteBuildID);
		} catch (org.json.JSONException e) {
			e.printStackTrace();
			return false;
		}
	}
	/**取更新包的大小*/
	public long getUpgradePackageSize() {
		if (checkURLOK(mConfig.getPackageURL()) == false) {
			Log.d(TAG, "getUpgradePckageSize Failed");
			return -1;
		}
		
		URL url = mConfig.getPackageURL();
		URLConnection con;
		try {
			con = url.openConnection();
			return con.getContentLength();
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	private boolean checkURLOK(URL url) {
		Log.d(TAG, "checkURLOK: " + url);
		try {
			HttpURLConnection.setFollowRedirects(false);//连接是否遵循远程服务器返回的重定向的集合标识
			
			HttpURLConnection con =  (HttpURLConnection) url.openConnection();
			
			con.setRequestMethod("HEAD");
			
			Log.d(TAG, "checkURLOK: getResponseCode() " + con.getResponseCode());
			return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	/**去目标包的属性*/
	public static JSONObject getTargetPackageProperty() {
		try {
//			URL url = new URL("http://223.4.25.90:8080/bydstoreapi/market?type=94&signkey=bydosup");
			URL url = new URL("http://192.168.1.114:80/bydstoreapi/market?type=94&signkey=bydosup");

			HttpURLConnection conn = (HttpURLConnection)url.openConnection();

			conn.setReadTimeout(10000);

			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
	            InputStream is = conn.getInputStream();
				
				byte buf[] = new byte[1024];
				int count = is.read(buf);

				is.close();

				String desc = new String(buf, 0, count);
				Log.d(TAG, "getTargetPackageProperty: " + desc);
				final int index = desc.indexOf("\\");
				if (index != -1) {
					desc = desc.substring(0, index) + "/" + desc.substring(index + 1);
					Log.d(TAG, "getTargetPackageProperty: " + desc);
				}

				try {
					JSONObject jsonObject = new JSONObject(desc);
					Log.d(TAG, "getTargetPackageProperty: " + jsonObject);
					return jsonObject;
				} catch (org.json.JSONException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	// function: 
	// download the property list from remote site, and parse it to peroerty list.//从远程站点下载属性列表,解析它peroerty列表
	// the caller can parser this list and get information.
	 */
	BuildPropParser getTargetPackagePropertyList(URL configURL) {
		
		// first try to download the property list file. the build.prop of target image.
		try {
			URL url =  configURL;
			url.openConnection();//返回一个新的连接到资源指向这个URL
			InputStream reader = url.openStream();
			ByteArrayOutputStream writer = new ByteArrayOutputStream();
			byte[] buffer = new byte[153600];
			int totalBufRead = 0;
			int bytesRead;
			
			Log.d(TAG, "start download: " + url.toString() + "to buffer");
		
			while ((bytesRead = reader.read(buffer)) > 0) {
				writer.write(buffer, 0, bytesRead);
				buffer = new byte[153600];
				totalBufRead += bytesRead;
			}
			
		
		Log.d(TAG, "download finish:" + (new Integer(totalBufRead).toString()) + "bytes download");
		reader.close();
		
		BuildPropParser parser = new BuildPropParser(writer, mContext);
		
		return parser;
		
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean handleMessage(Message arg0) {
		// TODO Auto-generated method stub
		return false;
	}

}
