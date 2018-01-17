package com.hwatong.settings.ota;

import java.io.*;
import java.util.*;

import android.content.Context;
import android.util.Log;


/**
 * 构建支持解析器
 * */
public class BuildPropParser {
    private static final String TAG = "OTA";

    private HashMap<String, String> propHM = null;
    private File tmpFile;
    private Context mContext;
    
    BuildPropParser(ByteArrayOutputStream out, Context context) {//ByteArrayOutputStream:可以捕获内存缓冲区的数据，转换成字节数组。

    	mContext = context;
        propHM = new HashMap<String, String>();
        setByteArrayStream(out);
    }

    BuildPropParser(File file, Context context) throws IOException {
    	mContext = context;
        propHM = new HashMap<String, String>();
        setFile(file);
    }

    public HashMap<String, String> getPropMap() { return propHM;};
    
    
    /**从propHM.get(propname)取propname对应的值*/	
    public String getProp(String propname) { 
    	if (propHM != null)
    		return (String) propHM.get(propname); 
    	else 
    		return null;
    }
    
    /**设置字节数组流。根据传进来的字节数组out，mcongtext取文件目录，设置临时文件*/	
    private void setByteArrayStream(ByteArrayOutputStream out) {
        try {
        	File tmpDir = null;
        	if (mContext != null)
        		tmpDir = mContext.getFilesDir();
        	Log.d(TAG, "tmpDir:"  + tmpDir.toString() +  "\n");
            tmpFile = File.createTempFile("buildprop", "ss", tmpDir);
            
            tmpFile.deleteOnExit();//在虚拟机终止时，请求删除此抽象路径名表示的文件或目录
            FileOutputStream o2 = new FileOutputStream(tmpFile);
            out.writeTo(o2);
            o2.close();
            setFile(tmpFile);
            tmpFile.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 给propHM传值
     * */
    private void setFile(File file) throws IOException {
        try {
            FileReader reader = new FileReader(file);
            BufferedReader in = new BufferedReader(reader);
            String string;
            while ((string = in.readLine()) != null) {
                Scanner scan = new Scanner(string);
                scan.useDelimiter("=");//以=为分隔符
                try {
                    propHM.put(scan.next(), scan.next());
                } catch (NoSuchElementException e) {
                    continue;
                }
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }
    
    public String getRelease() { 
    	if (propHM != null) 
    		return propHM.get("ro.build.version.release"); 
    	else 
    		return null;
    }

    public String getNumRelease()  {
    	if (propHM != null) 
    		return propHM.get("ro.build.version.incremental");
    	else
    		return null;
    }

}
