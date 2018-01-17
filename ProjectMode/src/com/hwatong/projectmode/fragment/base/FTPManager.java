package com.hwatong.projectmode.fragment.base;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import android.util.Log;
/**
 * TBox FTP Manager
 * @author caochao
 */
public class FTPManager {
    private static final String TAG = "FTPManager" ; 
    /**
     * TBOX FTP 用户名
     */
    private static final String USER_NAME = "root";
    /**
     * TBOX FTP 密码
     */
    private static final String PASS_WORD = "oelinux123";
    /**
     * TBX IP地址
     */
    private static final String IP = "192.168.225.1";
    /**
     * TBOX IP端口
     */
    private static final int PORT = 21;
    
    private static final String LOCAL_LOG_FILE = "/mnt/udisk2/f70log/tbox_log.tar";
    
    public static FTPManager manager = null ;
    
    private FTPClient ftpClient = null ;
    private FTPListener mListener = null ;

    private FTPManager() {
        ftpClient = new FTPClient();
    }

    public static FTPManager getInstance() {
        if(manager==null){
            manager = new FTPManager();
        }
        return manager;
    }
    /**
     * 连接TBOX FTP服务器
     * @return
     * @throws Exception
     */
    public synchronized boolean connect() throws Exception{
        if(ftpClient.isConnected()){
            ftpClient.disconnect();
        }
        ftpClient.setDataTimeout(20000);//20s连接超时
        //ftpClient.setControlEncoding("utf-8");
        ftpClient.connect(IP, PORT);
        if(FTPReply.isPositiveCompletion(ftpClient.getReplyCode())){
            if(ftpClient.login(USER_NAME, PASS_WORD)){
                Log.d(TAG, "tbox ftpserver connnect success!");
                return true ;
            }
        }
        Log.d(TAG, "tbox ftpserver connnect failed!");
        return false ;
    }
    /**
     * 从TBOX FTP服务器下载
     * @param serverPath FTP目录文件
     * @return
     * @throws Exception
     */
    public synchronized boolean downLoad(String serverPath ) throws Exception{
        FTPFile [] files = ftpClient.listFiles(serverPath);
        /**
         * 检测文件是否存在
         *
        if(files.length == 0){
            Log.d(TAG, "the log file not exist");
            return false ;
        }*/
        /**
         * 下载log文件
         */
        if(mListener != null){
            mListener.onProcess(FTPListener.DOWNLOAD);
        }
        
        File file =new File(LOCAL_LOG_FILE);
        if(file.exists()){
            file.delete();
            file.createNewFile();
        }
        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        OutputStream outputStream = new FileOutputStream(new File(LOCAL_LOG_FILE), true);
        InputStream inputStream = ftpClient.retrieveFileStream(serverPath);
        
        byte[] b = new byte[1024];
        int length = 0 ;
        while ((length = inputStream.read(b))!=-1) {
            outputStream.write(b, 0, length);
        }
        outputStream.flush();
        outputStream.close();
        inputStream.close();
        Log.d(TAG, "dwon load finished!");
        mListener.onProcess(FTPListener.FINISH);
        return true ;
    }
    
    
    public interface FTPListener{
        public static final int DOWNLOAD = 0 ;
        public static final int FINISH = 1 ;
        public void onProcess(int status);
    }
    
    public void setListener(FTPListener ftpListener){
        mListener  = ftpListener ;
    }
}
