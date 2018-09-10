package com.hwatong.settings.wallpaper;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.hwatong.settings.R;

import android.app.Dialog;
import android.app.Fragment;
import android.app.Service;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.DialogInterface.OnKeyListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.StatFs;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class WallpaperSettings extends Fragment {
//
//	private static final String TAG = "Wallpaper";
//
//	private GridviewAdapter mGridviewAdapter;
//	private GridView mGridView;
//	private View vLocal;
//	private View vUSB;
//	private TextView txLocal;
//	private TextView txUSB;
//
//	private WallpaperWorker mWallpaperWorker;
//
//	private int clickCount = 0;
//	private int clickPosition = -1;
//
//	// private View emptyView;
//	private Dialog usbNullDialog;
//	private Dialog mConCameraDialog;
//	private LinearLayout usbNullLinearLayout;
//	
//	private com.hwatong.media.IService mService;
//
//	private boolean mIsUsbTab = false;
//
//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container,
//			Bundle savedInstanceState) {
//		View view = inflater.inflate(R.layout.fragment_wallpaper, container, false);
//		return view;
//	}
//
//	@Override
//	public void onActivityCreated(Bundle savedInstanceState) {
//		super.onActivityCreated(savedInstanceState);
//
//		usbNullDialog = new Dialog(getActivity(), R.style.Wallpaper_nulldialog);
//		View view = getActivity().getLayoutInflater().inflate(
//				R.layout.wallpaper_being_empty, null);
//		usbNullLinearLayout = (LinearLayout) view
//				.findViewById(R.id.ll_usbisnull);
//		usbNullDialog.setContentView(view);
//		usbNullDialog.setCanceledOnTouchOutside(true);
//		usbNullDialog.setOnKeyListener(new OnKeyListener() {
//			@Override
//			public boolean onKey(DialogInterface dialog, int keyCode,
//					KeyEvent event) {
//				if (keyCode == KeyEvent.KEYCODE_BACK) {
//					usbNullDialog.dismiss();
//					return true;
//				}
//				return false;
//			}
//		});
//		usbNullLinearLayout.setOnClickListener(new View.OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				usbNullDialog.dismiss();
//			}
//		});
//
//		mConCameraDialog = new Dialog(getActivity(), R.style.Wallpaper_mydialog);
//		View view_succed = getActivity().getLayoutInflater().inflate(
//				R.layout.wallpaper_being_progress, null);
//		mConCameraDialog.setContentView(view_succed);
//		mConCameraDialog.setCanceledOnTouchOutside(false);
//		mConCameraDialog.setOnKeyListener(new OnKeyListener() {
//			@Override
//			public boolean onKey(DialogInterface dialog, int keyCode,
//					KeyEvent event) {
//				if (keyCode == KeyEvent.KEYCODE_BACK) {
//					mConCameraDialog.dismiss();
//					return true;
//				}
//				return false;
//			}
//		});
//
//		// emptyView = getActivity().findViewById(R.id.empty_view);
//		vLocal = getActivity().findViewById(R.id.local);
//		vUSB = getActivity().findViewById(R.id.usb);
//		txLocal = (TextView) getActivity().findViewById(R.id.local_tx);
//		txUSB = (TextView) getActivity().findViewById(R.id.usb_tx);
//
//		setChoose(1);
//		vLocal.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if (!mIsUsbTab)
//					return;
//
//				if (mService != null) {
//					try {
//						mService.unregisterCallback(mUpdateListener);
//					} catch (RemoteException e) {
//						e.printStackTrace();
//					}
//				}
//
//				mService = null;
//				getActivity().unbindService(mConnection);
//
//				setChoose(1);
//				mIsUsbTab = false;
//
//				// emptyView.setVisibility(View.GONE);
//				mGridView.setVisibility(View.VISIBLE);
//				
//				mGridView.setHorizontalSpacing(47);
//				mGridView.setVerticalSpacing(33);
//				
//				mGridviewAdapter = new GridviewAdapter(getActivity().getApplicationContext());
//				mGridView.setAdapter(mGridviewAdapter);
//
//			}
//		});
//		vUSB.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				if (mIsUsbTab)
//					return;
//
//				setChoose(2);
//				mIsUsbTab = true;
//
//				mGridviewAdapter = new GridviewAdapter(getActivity().getApplicationContext());
//				mGridView.setAdapter(mGridviewAdapter);
//
//				getActivity().bindService(new Intent("com.hwatong.media.MediaScannerService"), mConnection, 0);
//			}
//		});
//
//		mGridView = (GridView) getActivity().findViewById(R.id.gridview);
//		mGridviewAdapter = new GridviewAdapter(getActivity().getApplicationContext());
//		mGridView.setAdapter(mGridviewAdapter);
//
//		mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view,
//					int position, long id) {
//
//				if (clickPosition != position) {
//					clickPosition = position;
//					clickCount = 0;
//				}
//					
//				clickCount += 1;
//
//				if (clickCount == 1) {
//					mClickHandler.removeMessages(0);
//					mClickHandler.sendEmptyMessageDelayed(0, 300);
//				}
//			}
//		});
//
//		mWallpaperWorker = new WallpaperWorker();
//	}
//
//	@Override
//	public void onDestroy() {
//		Log.w(TAG, "onDestroy");
//
//		if (mIsUsbTab) {
//			if (mService != null) {
//				try {
//					mService.unregisterCallback(mUpdateListener);
//				} catch (RemoteException e) {
//					e.printStackTrace();
//				}
//			}
//
//			mService = null;
//			getActivity().unbindService(mConnection);
//		}
//
//		mWallpaperWorker.waitComplete();
//		mWallpaperWorker = null;
//
//		mClickHandler.removeCallbacksAndMessages(null);
//
//		super.onDestroy();
//	}
//
//	@Override
//	public void onResume() {
//		Log.w(TAG, "onResume");
//
//		clickCount = 0;
//		clickPosition = -1;
//
//		mGridviewAdapter.notifyDataSetChanged();
//
//		super.onResume();
//	}
//
//	@Override
//	public void onPause() {
//		Log.w(TAG, "onPause");
//		super.onPause();
//	}
//
//	@Override
//	public void onActivityResult(int requestCode, int resultCode, Intent data) {
//		// TODO Auto-generated method stub
//		super.onActivityResult(requestCode, resultCode, data);
//
//		if (Utils.DEBUG) Log.i(TAG, "onActivityResult " + requestCode + ", " + resultCode);
//		if (requestCode == 1000 && resultCode == 1000) {
//			if (Utils.DEBUG) Log.i(TAG, "onActivityResult " + data.getIntExtra("resultPosition", 0));
//			mGridView.requestFocus();
//			mGridView.smoothScrollToPosition(data.getIntExtra("resultPosition", 0));
//		}
//	}
//
//	private final ServiceConnection mConnection = new ServiceConnection() {
//		@Override
//		public void onServiceConnected(ComponentName name, IBinder service) {
//			if (Utils.DEBUG) Log.i(TAG, "onServiceConnected");
//
//			mService = com.hwatong.media.IService.Stub.asInterface(service);
//
//			try {
//				mService.registerCallback(mUpdateListener);
//
//				handlePictureListChanged();
//
//			} catch (RemoteException e) {
//				e.printStackTrace();
//			}
//		}
//
//		@Override
//		public void onServiceDisconnected(ComponentName name) {
//			if (Utils.DEBUG) Log.i(TAG, "onServiceDisconnected");
//			mService = null;
//		}
//	};
//
//	private final com.hwatong.media.ICallback mUpdateListener = new com.hwatong.media.ICallback.Stub() {
//
//		@Override
//		public void onMusicListChanged() {
//			if (Utils.DEBUG) Log.i(TAG, "onMusicListChanged");
//		}
//
//		@Override
//		public void onVideoListChanged() {
//			if (Utils.DEBUG) Log.i(TAG, "onVideoListChanged");
//		}
//
//		@Override
//		public void onPictureListChanged() {
//			if (Utils.DEBUG) Log.i(TAG, "onPictureListChanged");
//
//			mPictureListChangedHandler.removeMessages(0);
//			mPictureListChangedHandler.sendEmptyMessage(0);
//		}
//
//		@Override
//		public void onUsbStateChanged(String path, String oldState, String newState) {
//			if (Utils.DEBUG) Log.i(TAG, "onUsbStateChanged " + path + " " + oldState + " -> " + newState);
//		}
//
//		@Override
//		public void onUsbScanChanged(String path, String oldState, String newState) {
//			if (Utils.DEBUG) Log.i(TAG, "onUsbScanChanged " + path + " " + oldState + " -> " + newState);
//		}
//
//		@Override
//		public void onUsbPrescanChanged(String path, String oldState, String newState) {
//			if (Utils.DEBUG) Log.i(TAG, "onUsbPrescanChanged " + path + " " + oldState + " -> " + newState);
//		}
//
//	};
//
//	private final Handler mPictureListChangedHandler = new Handler() {
//		public void handleMessage(Message msg) {
//			handlePictureListChanged();
//		}
//	};
//
//	private void handlePictureListChanged() {
//		if (mIsUsbTab) {
//			mGridviewAdapter.notifyData();
//
//			if (0 < mGridviewAdapter.getCount()) {
//				mGridView.setVisibility(View.VISIBLE);
//				// emptyView.setVisibility(View.GONE);
//				mGridView.setHorizontalSpacing(70);
//				mGridView.setVerticalSpacing(35);
//			} else {
//				SpTools.putInt(getActivity(), "settingUsbPosition", -1);
//				vLocal.callOnClick();
//				usbNullDialog.show();
//			}
//		}
//	}
//	
//	public static int computeSampleSize(BitmapFactory.Options options,
//			int minSideLength, int maxNumOfPixels) {
//
//		int initialSize = computeInitialSampleSize(options,
//				minSideLength, maxNumOfPixels);
//
//		int roundedSize;
//		if (initialSize <= 8) {
//			roundedSize = 1;
//			while (roundedSize < initialSize) {
//				roundedSize <<= 1;
//			}
//		} else {
//			roundedSize = (initialSize + 7) / 8 * 8;
//		}
//
//		return roundedSize;
//	}
//
//	private static int computeInitialSampleSize(BitmapFactory.Options options,
//			int minSideLength, int maxNumOfPixels) {
//		double w = options.outWidth;
//		double h = options.outHeight;
//
//		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(
//				Math.sqrt(w * h / maxNumOfPixels));
//		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
//				Math.floor(w / minSideLength), Math.floor(h / minSideLength));
//
//		if (upperBound < lowerBound) {
//			// return the larger one when there is no overlapping zone.
//			return lowerBound;
//		}
//
//		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
//			return 1;
//		} else if (minSideLength == -1) {
//			return lowerBound;
//		} else {
//			return upperBound;
//		}
//	}
//
//	public static Bitmap scaleBitmap(Bitmap srcBitmap, int targetWidth,
//			int targetHeight) {
//		int srcWidth = srcBitmap.getWidth();
//		int srcHeight = srcBitmap.getHeight();
//		float scaleWidth = ((float) targetWidth) / srcWidth;
//		float scaleHeight = ((float) targetHeight) / srcHeight;
//		Matrix matrix = new Matrix();
//		matrix.postScale(scaleWidth, scaleHeight);
//		return Bitmap.createBitmap(srcBitmap, 0, 0, srcWidth, srcHeight,
//				matrix, true);
//	}
//
//	private void setChoose(int what) {
//		if (1 == what) {
//			txLocal.setSelected(true);
//			txUSB.setSelected(false);
////			txLocal.setTextColor(getResources().getColor(R.color.solid_black));
////			txLocal.setBackgroundResource(R.drawable.buttonleft_selected);
////			txUSB.setTextColor(getResources().getColor(R.color.white));
////			txUSB.setBackgroundResource(R.drawable.buttonright_normal);
//		} else if (2 == what) {
//			txLocal.setSelected(false);
//			txUSB.setSelected(true);
////			txLocal.setTextColor(getResources().getColor(R.color.white));
////			txLocal.setBackgroundResource(R.drawable.buttonleft_normal);
////			txUSB.setTextColor(getResources().getColor(R.color.solid_black));
////			txUSB.setBackgroundResource(R.drawable.buttonright_selected);
//		}
//	}
//
//	private final Handler mClickHandler = new Handler() {
//		@Override
//		public void handleMessage(Message msg) {
//			if (clickCount == 1 && clickPosition != -1) {
//				Intent intent = new Intent();
//				intent.setClass(getActivity(), WallpaperActivity.class);
//				Bundle bundle = new Bundle();
//				bundle.putBoolean("picture_in_USB", mIsUsbTab);
//				if (mIsUsbTab)
//					bundle.putString("path", ((GridviewAdapter.Entry)mGridviewAdapter.getItem(clickPosition)).mFilePath);
//				else
//					bundle.putInt("res", ((GridviewAdapter.Entry)mGridviewAdapter.getItem(clickPosition)).mRes);
//				intent.putExtras(bundle);
//				startActivityForResult(intent, 1000);
//				clickPosition = -1;
//
//			} else if (clickCount == 2 && clickPosition != -1) {
//
//				mConCameraDialog.show();
//
//				if (mIsUsbTab)
//					mWallpaperWorker.start(((GridviewAdapter.Entry)mGridviewAdapter.getItem(clickPosition)).mFilePath);
//				else
//					mWallpaperWorker.start(((GridviewAdapter.Entry)mGridviewAdapter.getItem(clickPosition)).mRes);
//			}
//		}
//	};
//	
//	private class WallpaperWorker extends Handler implements Runnable {
//		private final int screenWidth;
//		private final int screenHeight;
//		
//		private boolean exit;
//		private boolean done;
//
//		private String mFilePath;
//		private int mRes;
//
//		private Thread mLoader;
//
//		private Bitmap mBitmap;
//
//		public WallpaperWorker() {
//			screenWidth = getActivity().getWindowManager().getDefaultDisplay().getWidth();
//			screenHeight = getActivity().getWindowManager().getDefaultDisplay().getHeight();
//		}
//
//		public void start(String filePath) {
//			if (mLoader != null && filePath != null && !filePath.equals(mFilePath)) {
//				waitComplete();
//			}
//
//			if (mLoader == null && filePath != null) {
//				mFilePath = filePath;
//				exit = false;
//				done = false;
//				mLoader = new Thread(this);
//				mLoader.start();
//			}
//		}
//
//		public void start(int res) {
//			if (mLoader != null && (mFilePath != null || mRes != res)) {
//				waitComplete();
//			}
//
//			if (mLoader == null) {
//				mFilePath = null;
//				mRes = res;
//				exit = false;
//				done = false;
//				mLoader = new Thread(this);
//				mLoader.start();
//			}
//		}
//
//		public synchronized void waitComplete() {
//			if (mLoader != null) {
//				exit = true;
//				notifyAll();
//				while (!done) {
//					try {
//						wait();
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//			mLoader = null;
//			mBitmap = null;
//			mFilePath = null;
//        	removeMessages(0);
//		}
//
//		@Override
//		public void run() {
//			Bitmap bm = null;
//			
//			if (mFilePath != null) {
//				BitmapFactory.Options opt = new BitmapFactory.Options();
//				opt.inJustDecodeBounds = true;
//				BitmapFactory.decodeFile(mFilePath, opt);
//
//				final int minSideLength = Math.min(screenWidth, screenHeight);
//				opt.inSampleSize = computeSampleSize(opt, minSideLength, screenWidth * screenHeight);
//				opt.inJustDecodeBounds = false;
//				opt.inInputShareable = true;
//				opt.inPurgeable = true;
//				Bitmap bmp = BitmapFactory.decodeFile(mFilePath, opt);
//				if (bmp != null) {
//					bm = scaleBitmap(bmp, screenWidth, screenHeight);
//				}
//			} else {
//				InputStream is = getResources().openRawResource(mRes);
//
//				if (is != null) {
//					try {
//						BitmapFactory.Options opt = new BitmapFactory.Options();
//						bm = BitmapFactory.decodeStream(is, null, opt);
//	                } catch (OutOfMemoryError e) {
//	                    Log.e(TAG, "Can't decode stream, ", e);
//						e.printStackTrace();
//	                    bm = null;
//		            } finally {
//	                	if (is != null) {
//		                    try {
//		                        is.close();
//		                    } catch (java.io.IOException e) {
//								e.printStackTrace();
//		                    }
//		                }
//			        }
//			    }
//			}
//
//			synchronized (this) {
//				mBitmap = bm;
//				removeMessages(0);
//				sendEmptyMessage(0);
//				done = true;
//				notifyAll();
//			}
//		}
//
//		@Override
//		public void handleMessage(Message msg) {
//			boolean success = false;
//			
//			synchronized (this) {
//				if (mBitmap != null) {
//					if (mIsUsbTab) {
//						final WallpaperManager wallpaperManager = WallpaperManager.getInstance(getActivity());
//
//						int bitmapWidth = mBitmap.getWidth();
//						int bitmapHeight = mBitmap.getHeight();
//
//						try {
//							wallpaperManager.suggestDesiredDimensions(bitmapWidth, bitmapHeight);
//							wallpaperManager.setBitmap(mBitmap);
//
//							SpTools.putInt(getActivity(), "settingUsbPosition", clickPosition);
//							SpTools.putInt(getActivity(), "settingLocalPosition", -1);
//
//							success = true;
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
//					} else {
//						try {
//							getActivity().setWallpaper(mBitmap);
//							SpTools.putInt(getActivity(),"settingLocalPosition",clickPosition);
//							SpTools.putInt(getActivity(),"settingUsbPosition", -1);
//
//							success = true;
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
//					}
//				}
//
//				mBitmap = null;
//				notifyAll();
//			}
//
//			clickCount = 0;
//			clickPosition = -1;
//
//			Toast.makeText(getActivity().getApplicationContext(),
//					success ? R.string.wallpaper_succed : R.string.wallpaper_imagedamage,
//					Toast.LENGTH_SHORT).show();
//
//			if (mConCameraDialog.isShowing()) {
//				mConCameraDialog.dismiss();
//				mGridviewAdapter.notifyDataSetChanged();
//			}
//		}
//
//	}
//
//	public class GridviewAdapter extends BaseAdapter {
//		private final int[] LOCAL_RESOURCE = {
//			R.drawable.wallpaper_img1, R.drawable.wallpaper_img2, R.drawable.wallpaper_img3, 
//			R.drawable.wallpaper_img4, R.drawable.wallpaper_img5, R.drawable.wallpaper_img6,
//			R.drawable.wallpaper_img7, R.drawable.wallpaper_img8, R.drawable.wallpaper_img9,
//			R.drawable.wallpaper_img10, R.drawable.wallpaper_img11, 
//		};
//		private final List<GridviewAdapter.Entry> mPicList = new ArrayList<GridviewAdapter.Entry>();
//		private final Context mContext;
//
//		public GridviewAdapter(Context context) {
//			mContext = context;
//
//			fillList();
//		}
//
//		public void notifyData() {
//			fillList();
//			notifyDataSetChanged();
//		}
//
//		private void fillList() {
//			Log.d(TAG, "fillList in usb " + mIsUsbTab);
//			mPicList.clear();
//
//			if (mIsUsbTab) {
//				if (mService != null) {
//					try {
//		        		final List<com.hwatong.media.PictureEntry> list = mService.getPictureList();
//						Log.d(TAG, "fillList list size " + list.size());
//			    		for (int i = 0; i < list.size(); i++) {
//							String path = list.get(i).mFilePath;
//							String title = Utils.getExtFromFilename(path);
//
//							mPicList.add(new GridviewAdapter.Entry(path, title));
//			    		}
//					} catch (RemoteException e) {
//						e.printStackTrace();
//					}
//				}
//			} else {
//	    		for (int i = 0; i < LOCAL_RESOURCE.length; i++)
//					mPicList.add(new GridviewAdapter.Entry(LOCAL_RESOURCE[i]));
//			}
//			Log.d(TAG, "fillList list size " + mPicList.size());
//		}
//
//		@Override
//		public int getCount() {
//			return mPicList.size();
//		}
//
//		@Override
//		public Object getItem(int position) {
//			if (position >= 0 && position < mPicList.size())
//				return mPicList.get(position);
//
//			return null;
//		}
//
//		@Override
//		public long getItemId(int position) {
//			return position;
//		}
//
//		@Override
//		public View getView(int position, View convertView, ViewGroup parent) {
//			ViewHolder myViewHolder = null;
//			if (convertView == null) {
//				convertView = View.inflate(mContext, R.layout.wallpaper_gridview_item, null);
//				myViewHolder = new ViewHolder(mContext,
//					(ImageView) convertView.findViewById(R.id.image),
//					(TextView) convertView.findViewById(R.id.tv_hassetting),
//					(ImageView) convertView.findViewById(R.id.iv_image));
//				convertView.setTag(myViewHolder);
//			} else {
//				myViewHolder = (ViewHolder) convertView.getTag();
//			}
//
//			if (mIsUsbTab) {
//				myViewHolder.mImage_bg.setBackgroundResource(0);
//				myViewHolder.load(mPicList.get(position).mFilePath);
//				
//				if (SpTools.getInt(mContext, "settingUsbPosition", -1) != -1 && SpTools.getInt(mContext, "settingUsbPosition", -1) == position) {
//					myViewHolder.mTextView.setVisibility(View.VISIBLE);
//				}else {
//					myViewHolder.mTextView.setVisibility(View.GONE);
//				}
//			} else {
////				myViewHolder.load(LOCAL_RESOURCE[position]);
//				myViewHolder.mImage_bg.setBackgroundResource(LOCAL_RESOURCE[position]);
//
//				if (SpTools.getInt(mContext, "settingLocalPosition", -1) != -1 && SpTools.getInt(mContext, "settingLocalPosition", -1) == position) {
//					myViewHolder.mTextView.setVisibility(View.VISIBLE);
//				} else {
//					myViewHolder.mTextView.setVisibility(View.GONE);
//				}
//			}
//
//			return convertView;
//		}
//
//		private class Entry implements java.io.Serializable {
//
//			public final String mFilePath;
//			public final String mTitle;
//			public final int mRes;
//
//			public Entry(String path, String title) {
//				mFilePath = path;
//				mTitle = title;
//				mRes = 0;
//			}
//
//			public Entry(int res) {
//				mFilePath = null;
//				mTitle = null;
//				mRes = res;
//			}
//			
//			@Override
//			public boolean equals(Object o) {
//		        if (o == this) {
//		            return true;
//		        }
//				if (o instanceof Entry) {
//					return mFilePath != null && mFilePath.equals(((Entry)o).mFilePath);
//				}
//				return false;
//			}
//			
//			public class Comparator implements java.util.Comparator<Entry> {
//				@Override
//				public int compare(Entry arg0, Entry arg1) {
//					return arg0.mTitle.compareToIgnoreCase(arg1.mTitle);
//				}
//			}	
//		}
//
//		private class ViewHolder extends Handler implements Runnable {
//			private static final int PICTURE_WIDTH = 208;
//			private static final int PICTURE_HEIGHT = 122;
//
//			public final Context mContext;
//			public final ImageView mImageView;
//			public final TextView mTextView;
//			public final ImageView mImage_bg;
//			public String mFilePath;
//			public int mRes;
//
//			private Bitmap bitmap;
//			private Thread loader;
//			private boolean exit;
//			private boolean done;
//
//			public ViewHolder(Context context, ImageView imageView, TextView textView, ImageView imageBg) {
//				mContext = context;
//				mImageView = imageView;
//				mTextView = textView;
//				mImage_bg = imageBg;
//			}
//
//			public void load(String filePath) {
//				if (loader != null && filePath != null && !filePath.equals(mFilePath)) {
//					mImageView.setImageResource(R.drawable.wallpaper_file_picture);
//					waitComplete();
//				}
//
//				if (loader == null && filePath != null) {
//					mFilePath = filePath;
//					exit = false;
//					done = false;
//					loader = new Thread(this);
//					loader.start();
//				}
//			}
//
//			public void load(int res) {
//				if (loader != null && (mFilePath != null || mRes != res)) {
//					mImageView.setImageResource(R.drawable.wallpaper_file_picture);
//					waitComplete();
//				}
//
//				if (loader == null) {
//					mFilePath = null;
//					mRes = res;
//					exit = false;
//					done = false;
//					loader = new Thread(this);
//					loader.start();
//				}
//			}
//
//			public synchronized void waitComplete() {
//				if (loader != null) {
//					exit = true;
//					notifyAll();
//					while (!done) {
//						try {
//							wait();
//						} catch (InterruptedException e){
//							e.printStackTrace();
//						}
//					}
//					if (bitmap != null)
//						mImageView.setImageBitmap(bitmap);
//				}
//				loader = null;
//				bitmap = null;
//				mFilePath = null;
//        		removeMessages(0);
//			}
//
//			@Override
//    		public void run() {
//				Bitmap bm = null;
//
//				if (mFilePath != null) {
//			        final BitmapFactory.Options opt = new BitmapFactory.Options();
//
//			        opt.inJustDecodeBounds = true;
//					BitmapFactory.decodeFile(mFilePath, opt);
//
//			        opt.inSampleSize = 1;
//			    	while (opt.outWidth / opt.inSampleSize > PICTURE_WIDTH
//			    			&& opt.outHeight / opt.inSampleSize > PICTURE_HEIGHT) {
//			    		opt.inSampleSize *= 2;
//			    	}
//			        opt.inJustDecodeBounds = false;
//					bm = BitmapFactory.decodeFile(mFilePath, opt);
//	            } else {
//					InputStream is = getResources().openRawResource(mRes);
//
//					if (is != null) {
//						try {
//							final BitmapFactory.Options opt = new BitmapFactory.Options();
//							bm = BitmapFactory.decodeStream(is, null, opt);
//		                } catch (OutOfMemoryError e) {
//		                    Log.e(TAG, "Can't decode stream, ", e);
//							e.printStackTrace();
//		                    bm = null;
//			            } finally {
//		                	if (is != null) {
//			                    try {
//			                        is.close();
//			                    } catch (java.io.IOException e) {
//									e.printStackTrace();
//			                    }
//			                }
//				        }
//				    }
//				}
//
//	            if (bm == null) {
//			        final BitmapFactory.Options opt = new BitmapFactory.Options();
//	            	bm = BitmapFactory.decodeResource(getResources(), R.drawable.wallpaper_file_picture, opt);
//	            }
//
//				synchronized (this) {
//					bitmap = bm;
//    				removeMessages(0);
//					sendEmptyMessage(0);
//					while (!exit && bitmap != null) {
//						try {
//							wait();
//						} catch (InterruptedException e){
//							e.printStackTrace();
//						}
//					}
//					done = true;
//					notifyAll();
//				}
//			}
//
//			@Override
//			public void handleMessage(Message msg) {
//				synchronized (this) {
//					if (bitmap != null) {
//						mImageView.setImageBitmap(bitmap);
//						bitmap = null;
//						notifyAll();
//					}
//				}
//			}
//			
//		}
//	}
//
}
