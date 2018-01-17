package com.hwatong.projectmode.helper;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.util.Log;
/**
 * 升级服务配置
 * */
public class OTAServerConfig {
	
	private static final String default_serveraddr = "192.168.1.114";
	private static final String default_protocol = "http";
	private static final int default_port = 80;

	private static final String TAG = "OTA";
	private static final String configFile = "/system/etc/ota.conf";
	private static final String server_ip_config = "server";
	private static final String port_config_str = "port";

	private URL updatePackageURL;
	private URL buildpropURL;
	private String product;
	
	public OTAServerConfig(String productname) throws MalformedURLException {
		product = productname;
//		if (loadConfigureFromFile(configFile, productname) == false)
//			defaultConfigure(productname);
	}
	/**从文件加载配置*/
	private boolean loadConfigureFromFile(String configFile, String product) {
		try {
			final BuildPropParser parser = new BuildPropParser(new File(configFile), null);
			String server = parser.getProp(server_ip_config);
			String port_str = parser.getProp(port_config_str);
			int port = new Long(port_str).intValue();
			String fileaddr = new String(product + "/" + product + ".ota.zip");
			String buildconfigAddr = new String(product + "/" + "build.prop"); 
			updatePackageURL = new URL(default_protocol, server, port, fileaddr);
			buildpropURL = new URL(default_protocol, server, port, buildconfigAddr);
			Log.d(TAG, "create a new server config: package url " + updatePackageURL.toString() + " port:" + updatePackageURL.getPort());
			Log.d(TAG, "build.prop URL:" + buildpropURL.toString());
		} catch (Exception e) {
			Log.d(TAG, "wrong format/error of OTA configure file.");
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	private void defaultConfigure(String productname) throws MalformedURLException {
		final String fileaddr = new String(product + "/" + product + ".ota.zip");
		final String buildconfigAddr = new String(product + "/" + "build.prop"); 
		updatePackageURL = new URL(default_protocol, default_serveraddr, default_port, fileaddr );
		buildpropURL = new URL(default_protocol, default_serveraddr, default_port, buildconfigAddr);
		Log.d(TAG, "create a new server config: package url " + updatePackageURL.toString() + " port:" + updatePackageURL.getPort());
		Log.d(TAG, "build.prop URL:" + buildpropURL.toString());
	}
	
	public URL getPackageURL () {
		if (updatePackageURL == null)
			getConfig();

		return updatePackageURL;
	}

	public URL getBuildPropURL() {
		if (buildpropURL == null)
			getConfig();

		return buildpropURL;
	}
	
	private void getConfig() {
		try {
			URL url = new URL("http://192.168.1.114:80/bydstoreapi/market?type=94&signkey=bydosup");

			HttpURLConnection conn = (HttpURLConnection)url.openConnection();

			conn.setReadTimeout(10000);//设置从主机读取数据超时（单位：毫秒）

			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
	            InputStream is = conn.getInputStream();
				
				byte buf[] = new byte[1024];

				int count = is.read(buf);

				Log.d(TAG, "OTAServerConfig: " + new String(buf, 0, count));
				
				is.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
