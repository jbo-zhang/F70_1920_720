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
     * ��֤��ǰwifi�Ƿ���ҪPortal��֤
     * @return
     */
    private boolean isWifiSetPortal() {
        String mWalledGardenUrl = "http://g.cn/generate_204";
        // ��������ʱ
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
            // �жϷ���״̬���Ƿ���204
            return urlConnection.getResponseCode()!=204;
        } catch (IOException e) {
            //   e.printStackTrace();
            return false;
        } finally {
            if (urlConnection != null) {
                //�ͷ���Դ
                urlConnection.disconnect();
            }
        }
    }

    /**
     * ���Wifi �Ƿ���Ҫportal ��֤
     * @param callBack
     */
    public static void checkWifi(ICheckWifiCallBack callBack){
        new CheckWifiLoginTask(callBack).execute();
    }

    public interface ICheckWifiCallBack{
        void portalNetWork(boolean isLogin);
    }

}
