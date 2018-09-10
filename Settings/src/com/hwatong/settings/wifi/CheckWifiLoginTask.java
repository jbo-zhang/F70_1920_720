package com.hwatong.settings.wifi;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.AsyncTask;

public class CheckWifiLoginTask extends AsyncTask<Integer,Integer,Boolean> {


    private ICheckWifiCallBack mCallBack;


    public CheckWifiLoginTask (ICheckWifiCallBack mCallBack){
        super();
        this.mCallBack=mCallBack;
    }


    @Override
    protected Boolean doInBackground(Integer... params) {
        return isWifiSetPortal();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (mCallBack != null) {
            mCallBack.portalNetWork(result);
        }
    }

    /**
     * 验证当前wifi是否需要Portal验证
     * @return
     */
    private boolean isWifiSetPortal() {
        String mWalledGardenUrl = "http://g.cn/generate_204";
        // 设置请求超时
        int WALLED_GARDEN_SOCKET_TIMEOUT_MS = 10000;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(mWalledGardenUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setInstanceFollowRedirects(false);
            urlConnection.setConnectTimeout(WALLED_GARDEN_SOCKET_TIMEOUT_MS);
            urlConnection.setReadTimeout(WALLED_GARDEN_SOCKET_TIMEOUT_MS);
            urlConnection.setUseCaches(false);
            urlConnection.getInputStream();
            // 判断返回状态码是否是204
            return urlConnection.getResponseCode()!=204;
        } catch (IOException e) {
            //   e.printStackTrace();
            return false;
        } finally {
            if (urlConnection != null) {
                //释放资源
                urlConnection.disconnect();
            }
        }
    }

    /**
     * 检测Wifi 是否需要portal 认证
     * @param callBack
     */
    public static void checkWifi(ICheckWifiCallBack callBack){
        new CheckWifiLoginTask(callBack).execute();
    }

    public interface ICheckWifiCallBack{
        void portalNetWork(boolean isLogin);
    }

}
