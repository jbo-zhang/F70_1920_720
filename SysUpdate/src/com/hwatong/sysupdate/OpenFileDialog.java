package com.hwatong.sysupdate;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 升级包列表对话框
 * */
public class OpenFileDialog extends AlertDialog.Builder {

	private String currentPath = "/mnt";//Environment.getExternalStorageDirectory().getPath();
    private List<File> files = new ArrayList<File>();
    private TextView title;
    private ListView listView;
    private FilenameFilter filenameFilter;
    private int selectedIndex = -1;
    private OpenDialogListener listener;
    private Drawable folderIcon;
    private Drawable fileIcon;
    private String accessDeniedMessage;
    private View mView;
    public static boolean ok_clicj_flag = false;//按返回键时背后动画是否直接退出

    public interface OpenDialogListener {
        public void OnSelectedFile(String fileName);
        public void OnCancel();
    }
    /**
     * 升级包列表适配器，给列表设置属性
     * */
    private class FileAdapter extends ArrayAdapter<File> {

        public FileAdapter(Context context, List<File> files) {
            super(context, android.R.layout.simple_list_item_1, files);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView) super.getView(position, convertView, parent);
            File file = getItem(position);
            if (view != null) {
                view.setText(file.getName());
                view.setTextColor(Color.rgb(255, 255, 255));
                if (file.isDirectory()) {
                    setDrawable(view, folderIcon);
                } else {
                    setDrawable(view, fileIcon);
                    if (selectedIndex == position)
                        view.setBackgroundColor(getContext().getResources().getColor(android.R.color.holo_blue_dark));
                    else
                        view.setBackgroundColor(getContext().getResources().getColor(android.R.color.transparent));
                }
            }
            return view;
        }

        private void setDrawable(TextView view, Drawable drawable) {
            if (view != null) {
                if (drawable != null) {
                    drawable.setBounds(0, 0, 60, 60);
                    view.setCompoundDrawables(drawable, null, null, null);
                } else {
                    view.setCompoundDrawables(null, null, null, null);
                }
            }
        }
    }
    public OpenFileDialog(Context context) {
        super(context);
    }
    
    private final static String USB_PATH="/mnt/udisk";
    private final static String USB_PATH2="/mnt/udisk2";
    private final static String TFCARD_PATH="/mnt/extsd";
    
    private Button mPositiveButton;
    private Button mNegativeButton;
    private AlertDialog mDialog;
    
    @Override
    public AlertDialog show() {
    	new ScanTask().execute();
    	//如果扫描文件非常快，加上100ms延时可以防止加载中闪动
    	SystemClock.sleep(100);
        return mDialog;
    }
    /**
     * 更改选中文件，设置确定按钮是否可用
     * */
    private void changeSelectedFile() {
    	if (mPositiveButton!=null) {
    		mPositiveButton.setEnabled(selectedIndex>=0);
    	}
    }
    /**
     * 设置过滤字符串
     * */
    public OpenFileDialog setFilter(final String filter) {
        filenameFilter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String fileName) {
                File tempFile = new File(dir.getPath(), fileName);
            	Log.d("9095", "tempFile: " + tempFile.getAbsolutePath());
                if (tempFile.isFile())
                	return tempFile.getName().endsWith(".img");
                return true;
            }
        };
        return this;
    }
    
    /**
     *设置打开列表窗口的监听实例 
     * */
    public OpenFileDialog setOpenDialogListener(OpenDialogListener listener) {
        this.listener = listener;
        return this;
    }
    /**
     *设置文件夹图标的实例 
     * */
    public OpenFileDialog setFolderIcon(Drawable drawable) {
        this.folderIcon = drawable;
        return this;
    }
    /**
     *设置文件图标的实例 
     * */
    public OpenFileDialog setFileIcon(Drawable drawable) {
        this.fileIcon = drawable;
        return this;
    }
    /**
     *设置拒绝访问的消息的实例 
     * */
    public OpenFileDialog setAccessDeniedMessage(String message) {
        this.accessDeniedMessage = message;
        return this;
    }
    
    /**得到默认显示*/
    private static Display getDefaultDisplay(Context context) {
        return ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    }
    /**
     * 	取屏幕四个点边距
     * */
    private static Point getScreenSize(Context context) {
        Point screeSize = new Point();
        getDefaultDisplay(context).getSize(screeSize);
        return screeSize;
    }
    /**
     * 取屏幕高边距
     * */
    private static int getLinearLayoutMinHeight(Context context) {
        return getScreenSize(context).y;
    }
    /**
     * 取每个item的高度
     * */
    private int getItemHeight(Context context) {
        TypedValue value = new TypedValue();
        DisplayMetrics metrics = new DisplayMetrics();
        context.getTheme().resolveAttribute(android.R.attr.listPreferredItemHeightSmall, value, true);
        getDefaultDisplay(context).getMetrics(metrics);
        return (int) TypedValue.complexToDimension(value.data, metrics);
    }
    /**
     * 取text的宽
     * */
    public int getTextWidth(String text, Paint paint) {
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return bounds.left + bounds.width() + 80;
    }
    
    /**
     * 给title赋值
     * */
    private void changeTitle() {
        String titleText = getContext().getString(R.string.select_file); //currentPath;选择一个升级包
        int screenWidth = getScreenSize(getContext()).x;
        int maxWidth = (int) (screenWidth * 0.99);
        if (getTextWidth(titleText, title.getPaint()) > maxWidth) {
            while (getTextWidth("..." + titleText, title.getPaint()) > maxWidth) {
                int start = titleText.indexOf("/", 2);
                if (start > 0)
                    titleText = titleText.substring(start);
                else
                    titleText = titleText.substring(2);
            }
            title.setText("..." + titleText);
        } else {
            title.setText(titleText);
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
    
    /**
     * 重新根据当前路径设置adapter，给列表传值
     * */
    private void RebuildFiles(ArrayAdapter<File> adapter) {
        try {
            List<File> fileList = getFiles(currentPath);
            files.clear();
            selectedIndex = -1;
            files.addAll(fileList);
            adapter.notifyDataSetChanged();
            changeTitle();
        } catch (NullPointerException e) {
            String message = getContext().getResources().getString(android.R.string.unknownName);
            if (accessDeniedMessage!=null && !accessDeniedMessage.equals(""))
                message = accessDeniedMessage;
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
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
			
			if (files==null || files.size()==0) {
				Toast.makeText(getContext(), R.string.notfound, Toast.LENGTH_LONG).show();
	    	}	
			
			/**
			 * 只有一个升级包直接选择
			 * */
	    	if (files.size()==1) {
	            listener.OnSelectedFile(files.get(0).getPath());
	    		return ;
	    	}
	    		
	        mDialog = OpenFileDialog.super.show();
	        Window win = mDialog.getWindow();
	        
	        win.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
	        win.setContentView(R.layout.dialog_openfile);
	        mView = win.getDecorView();
	       
	        mPositiveButton = (Button)mView.findViewById(R.id.button1);
	        mNegativeButton = (Button)mView.findViewById(R.id.button2);
	        mPositiveButton.setText(android.R.string.ok);
	        mPositiveButton.setVisibility(View.VISIBLE);
	        mNegativeButton.setText(android.R.string.cancel);
	        mNegativeButton.setVisibility(View.VISIBLE);
	        //确定按钮
	        mPositiveButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
	                if (selectedIndex > -1 && listener != null) {
	                	
	                	ok_clicj_flag = true;
	                    listener.OnSelectedFile(listView.getItemAtPosition(selectedIndex).toString());
	                    mDialog.dismiss();
	                }
					
				}
	        });
	        
	        //取消按钮
	        mNegativeButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					
					ok_clicj_flag = false;
	            	listener.OnCancel();
	                mDialog.dismiss();
				}
	        });
	        
	        listView = (ListView)mView.findViewById(R.id.list);
	        listView.setMinimumHeight(getLinearLayoutMinHeight(getContext())); 
	        
	        //listview item的点击事件
	        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

	            @Override
	            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
	                final ArrayAdapter<File> adapter = (FileAdapter) adapterView.getAdapter();
	                File file = adapter.getItem(index);
	                if (file.isDirectory()) {
	                    currentPath = file.getPath();
	                    RebuildFiles(adapter);
	                } else {
	                    if (index != selectedIndex)
	                        selectedIndex = index;
	                    else
	                        selectedIndex = -1;
	                    adapter.notifyDataSetChanged();
	                    changeSelectedFile();
	                }
	            }
	        });
	        listView.setAdapter(new FileAdapter(getContext(), files));
	        title = (TextView)mView.findViewById(R.id.title);
	        changeTitle();
	        changeSelectedFile();
			
		}
    	
    }

    
    
    

}
