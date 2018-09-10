package com.hwatong.settings.wallpaper;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;

import com.hwatong.settings.R;

public class WallpaperActivity extends Activity{
//	protected static final String TAG = "Wallpaper";
//
//	private static final int[] LOCAL_RESOURCE = {
//		R.drawable.wallpaper_img1, R.drawable.wallpaper_img2, R.drawable.wallpaper_img3, 
//		R.drawable.wallpaper_img4, R.drawable.wallpaper_img5, R.drawable.wallpaper_img6,
//		R.drawable.wallpaper_img7, R.drawable.wallpaper_img8, R.drawable.wallpaper_img9,
//		R.drawable.wallpaper_img10, R.drawable.wallpaper_img11, 
//	};
//	private final List<com.hwatong.media.PictureEntry> mPicListInUSB = new ArrayList<com.hwatong.media.PictureEntry>();
//
//	private ViewPager mViewPager;
//
//	private boolean mPictureInUSB;
//	private String mFilePath;
//	private int mOffset;
//
//	private Dialog mConCameraDialog;
//
//	private PictureLoader mPictureLoader;
//	private WallpaperWorker mWallpaperWorker;
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState){
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_wallpaper);
//
//		Intent intent = getIntent();
//		Bundle bundle = intent.getExtras();
//		mPictureInUSB = bundle.getBoolean("picture_in_USB");
//		if (mPictureInUSB) {
//			mFilePath = bundle.getString("path");
//		}
//
//		mConCameraDialog = new Dialog(this, R.style.Wallpaper_mydialog);
//		View view = getLayoutInflater().inflate(R.layout.wallpaper_being_progress, null);
//		mConCameraDialog.setContentView(view);
//		mConCameraDialog.setCanceledOnTouchOutside(false);
//		mConCameraDialog.setOnKeyListener(new OnKeyListener() {
//			@Override
//			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//				if (keyCode == KeyEvent.KEYCODE_BACK){
//					mConCameraDialog.dismiss();
//					finish();
//					return true;
//				}
//				return false;
//			}
//		});
//
//		ImageButton btSure =(ImageButton) findViewById(R.id.sure_bt);
//		btSure.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				mConCameraDialog.show();
//
//				int position = mViewPager.getCurrentItem();
//				
//				if (mPictureInUSB) {
//					position = (position + mOffset) % mPicListInUSB.size();
//					mWallpaperWorker.start(mPicListInUSB.get(position).mFilePath);
//				} else {
//					position = (position + mOffset) % LOCAL_RESOURCE.length;
//					mWallpaperWorker.start(LOCAL_RESOURCE[position]);
//				}
//			}
//		});
//		
//		ImageButton btCancel = (ImageButton) findViewById(R.id.cancel_bt);
//		btCancel.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				if (mConCameraDialog.isShowing())
//					mConCameraDialog.dismiss();
//				finish();
//			}
//		});
//
//		mViewPager = (ViewPager) findViewById(R.id.id_viewPager);
//		mViewPager.setPageMargin((int) (getResources().getDisplayMetrics().density * 15));
//		mViewPager.setPageTransformer(true, new MyPageTransformer());
//
//		mViewPager.setOnPageChangeListener(mOnPageChangeListener);
//
//		mPictureLoader = new PictureLoader();
//		mWallpaperWorker = new WallpaperWorker();
//
//		mViewPager.setAdapter(mPagerAdapter);
//
//		if (mPictureInUSB) {
////			mViewPager.setCurrentItem(0);
//			mOffset = 0;
//			bindService(new Intent("com.hwatong.media.MediaScannerService"), mConnection, 0);
//		} else {
//			int res = bundle.getInt("res");
//			int position = 0;
//			for (int i = 0; i < LOCAL_RESOURCE.length; i++) {
//				if (LOCAL_RESOURCE[i] == res) {
//					position = i;
//					break;
//				}
//			}
////			mViewPager.setCurrentItem(position);
//			mOffset = position;
//		}
//
//	}
//
//	@Override
//	protected void onDestroy() {
//		Log.w(TAG, "onDestroy");
//
//		if (mPictureInUSB) {
//			if (mService != null) {
//				try {
//					mService.unregisterCallback(mUpdateListener);
//				} catch (RemoteException e) {
//					e.printStackTrace();
//				}
//			}
//
//			mService = null;
//			unbindService(mConnection);
//		}
//
//		mWallpaperWorker.waitComplete();
//		mWallpaperWorker = null;
//
//		mPictureLoader.waitComplete();
//		mPictureLoader = null;
//
//		super.onDestroy();
//	}
//
//	@Override
//	protected void onPause() {
//		super.onPause();
//	}
//
//	private com.hwatong.media.IService mService;
//
//	private final android.content.ServiceConnection mConnection = new android.content.ServiceConnection() {
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
//
//	        Message m = Message.obtain(mUsbStateChangeHandler, 0, new String[] { path, oldState, newState } );
//	        mUsbStateChangeHandler.sendMessage(m);
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
//	private final Handler mUsbStateChangeHandler = new Handler() {
//		public void handleMessage(Message msg) {
//            String path = ((String[]) msg.obj)[0];
//            String oldState = ((String[]) msg.obj)[1];
//            String newState = ((String[]) msg.obj)[2];
//
//			if (Environment.MEDIA_UNMOUNTED.equals(newState) && !Environment.MEDIA_REMOVED.equals(oldState)) {
//				if (Utils.DEBUG) Log.i(TAG, "USBSTATE_UNMOUNTED:");
//				if (mPictureInUSB){
//					finish();
//				}
//			}
//		}
//	};
//
//	private final Handler mPictureListChangedHandler = new Handler() {
//		public void handleMessage(Message msg) {
//			handlePictureListChanged();
//		}
//	};
//
//	private void handlePictureListChanged() {
//		mPicListInUSB.clear();
//
//		if (mService != null) {
//			try {
//        		final List<com.hwatong.media.PictureEntry> list = mService.getPictureList();
//        		mPicListInUSB.addAll(list);
//			} catch (RemoteException e) {
//				e.printStackTrace();
//			}
//		}
//
//		if (mPictureInUSB) {
//			mPagerAdapter.notifyDataSetChanged();
//
//			int position = 0;
//			for (int i = 0; i < mPicListInUSB.size(); i++) {
//				if (mFilePath != null && mFilePath.equals(mPicListInUSB.get(i).mFilePath)) {
//					position = i;
//					break;
//				}
//			}
////			mViewPager.setCurrentItem(position);
//			mOffset = position;
//		}
//	}
//
//	private final OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {
//		@Override
//		public void onPageSelected(int arg0) {
//			int position = arg0;
//
//			if (mPictureInUSB)
//				position = (position + mOffset) % mPicListInUSB.size();
//			else
//				position = (position + mOffset) % LOCAL_RESOURCE.length;
//			WallpaperActivity.this.setResult(1000, new Intent().putExtra("resultPosition", position));
//		}
//
//		@Override
//		public void onPageScrolled(int arg0, float arg1, int arg2) {
//
//		}
//
//		@Override
//		public void onPageScrollStateChanged(int arg0) {
//
//		}
//	};
//
//	private final PagerAdapter mPagerAdapter = new PagerAdapter() {
//		@Override
//		public int getCount(){
//			return mPictureInUSB ? mPicListInUSB.size() : LOCAL_RESOURCE.length;
//		}
//
//		@Override
//		public boolean isViewFromObject(View arg0, Object arg1){
//			return arg0 == arg1;
//		}
//
//		@Override
//		public void destroyItem(ViewGroup container, int position, Object object){
//			container.removeView((View) object);
//		}
//
//		@Override
//		public Object instantiateItem(ViewGroup container, int position){
//			if (Utils.DEBUG) Log.i(TAG, "mPager instantiateItem " + position);
//			ImageView imageView = new ImageView(WallpaperActivity.this);
//
//			if (mPictureInUSB) {
//				position = (position + mOffset) % mPicListInUSB.size();
////				imageView.setImageResource(R.drawable.wallpaper_file_picture);  
//				mPictureLoader.load(imageView, mPicListInUSB.get(position).mFilePath);
//			} else {
//				position = (position + mOffset) % LOCAL_RESOURCE.length;
////				mPictureLoader.load(imageView, LOCAL_RESOURCE[position]);
//				imageView.setImageResource(LOCAL_RESOURCE[position]);
//			}
//
//			imageView.setScaleType(ScaleType.FIT_CENTER);
//			container.addView(imageView);
//
//			return imageView;
//		}
//
//		@Override
//		public void setPrimaryItem(ViewGroup container, int position, Object object) {
//			super.setPrimaryItem(container, position, object);
//			if (Utils.DEBUG) Log.i(TAG, "mPager setPrimaryItem " + position);
//		}
//
//	};
//
//	private class PictureLoader extends Handler implements Runnable {
//		private static final int WIDTH = 960;
//		private static final int HEIGHT = 840;
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
//		public ImageView mImageView;
//
//		public PictureLoader() {
//		}
//
//		public void load(ImageView imageView, String filePath) {
//			waitComplete();
//
//			if (mLoader == null && filePath != null) {
//				mImageView = imageView;
//				mFilePath = filePath;
//				exit = false;
//				done = false;
//				mLoader = new Thread(this);
//				mLoader.start();
//			}
//		}
//
//		public void load(ImageView imageView, int res) {
//			waitComplete();
//
//			if (mLoader == null) {
//				mImageView = imageView;
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
//				if (mBitmap != null)
//					mImageView.setImageBitmap(mBitmap);
//			}
//			mLoader = null;
//			mBitmap = null;
//			mFilePath = null;
//			mImageView = null;
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
//				opt.inSampleSize = 1;
//				if (opt.outWidth > WIDTH || opt.outHeight > HEIGHT) {
//					int widthRatio = Math.round((float)opt.outWidth / (float)WIDTH);
//					int heightRatio = Math.round((float)opt.outHeight / (float)HEIGHT);
//					opt.inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
//				}
//
//				opt.inJustDecodeBounds = false;
//				bm = BitmapFactory.decodeFile(mFilePath, opt);
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
//            if (bm == null) {
//		        final BitmapFactory.Options opt = new BitmapFactory.Options();
//            	bm = BitmapFactory.decodeResource(getResources(), R.drawable.wallpaper_file_picture, opt);
//            }
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
//			synchronized (this) {
//				if (mBitmap != null) {
//					mImageView.setImageBitmap(mBitmap);
//					mBitmap = null;
//					notifyAll();
//				}
//			}
//		}
//
//	}
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
//			screenWidth = getWindowManager().getDefaultDisplay().getWidth();
//			screenHeight = getWindowManager().getDefaultDisplay().getHeight();
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
//				exit = false;
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
//					if (mPictureInUSB) {
//						final WallpaperManager wallpaperManager = WallpaperManager.getInstance(WallpaperActivity.this);
//
//						int bitmapWidth = mBitmap.getWidth();
//						int bitmapHeight = mBitmap.getHeight();
//
//						try {
//							wallpaperManager.suggestDesiredDimensions(bitmapWidth, bitmapHeight);
//							wallpaperManager.setBitmap(mBitmap);
//
//							for (int i = 0; i < mPicListInUSB.size(); i++) {
//								if (mFilePath != null && mFilePath.equals(mPicListInUSB.get(i).mFilePath)) {
//									SpTools.putInt(WallpaperActivity.this, "settingUsbPosition", i);
//									SpTools.putInt(WallpaperActivity.this, "settingLocalPosition", -1);
//									break;
//								}
//							}
//
//							success = true;
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
//					} else {
//						try {
//							setWallpaper(mBitmap);
//
//							for (int i = 0; i < LOCAL_RESOURCE.length; i++) {
//								if (LOCAL_RESOURCE[i] == mRes) {
//									SpTools.putInt(WallpaperActivity.this, "settingLocalPosition", i);
//									SpTools.putInt(WallpaperActivity.this, "settingUsbPosition", -1);
//									break;
//								}
//							}
//
//							success = true;
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
//					}
//
//					mBitmap = null;
//					notifyAll();
//				}
//			}
//
//			Toast.makeText(getApplicationContext(), 
//				success ? R.string.wallpaper_succed : R.string.wallpaper_imagedamage,
//				Toast.LENGTH_SHORT).show();
//
//			if (mConCameraDialog.isShowing())
//				mConCameraDialog.dismiss();
//			finish();
//		}
//
//	}
//
//	public class MyPageTransformer implements ViewPager.PageTransformer  
//	{  
//	    public void transformPage(View view, float position)  
//	    {  
//	            //宸﹁竟0~-90搴�,鍙宠竟90~0搴�,  
//	            //宸﹁竟x 0~-width锛屽彸杈箈 width~0锛�  
//	        if (position < -1)  
//	        {  
//	  
//	        } else if (position <= 1) // a椤垫粦鍔ㄨ嚦b椤� 锛� a椤典粠 0.0 ~ -1 锛沚椤典粠1 ~ 0.0  
//	        { // [-1,1]  
//	            if (position < 0)//婊戝姩涓乏杈归〉闈�  
//	            {  
//	                view.setPivotX(view.getMeasuredWidth());  
//	                view.setRotationY(position*45);  
//	            } else//婊戝姩涓彸杈归〉闈�  
//	            {  
//	                view.setPivotX(0);  
//	                view.setRotationY(position*45);  
//	            }  
//	  
//	        } else  
//	        { // (1,+Infinity]  
//	              
//	        }  
//	    }  
//	}  
//	
//	public class DepthPageTransformer implements ViewPager.PageTransformer {
//		private static final float MIN_SCALE = 0.75f;
//
//		public void transformPage(View view, float position) {
//			int pageWidth = view.getWidth();
//
//			if (position < -1) { // [-Infinity,-1)
//				// This page is way off-screen to the left.
//				view.setAlpha(0);
//
//			} else if (position <= 0) { // [-1,0]
//				// Use the default slide transition when moving to the left page
//				view.setAlpha(1);
//				view.setTranslationX(0);
//				view.setScaleX(1);
//				view.setScaleY(1);
//
//			} else if (position <= 1) { // (0,1]
//				// Fade the page out.
//				view.setAlpha(1 - position);
//
//				// Counteract the default slide transition
//				view.setTranslationX(pageWidth * -position);
//
//				// Scale the page down (between MIN_SCALE and 1)
//				float scaleFactor = MIN_SCALE
//						+ (1 - MIN_SCALE) * (1 - Math.abs(position));
//				view.setScaleX(scaleFactor);
//				view.setScaleY(scaleFactor);
//
//			} else { // (1,+Infinity]
//				// This page is way off-screen to the right.
//				view.setAlpha(0);
//			}
//		}
//	}
//
//	public static Bitmap scaleBitmap(Bitmap srcBitmap, int targetWidth, int targetHeight) {
//		int srcWidth = srcBitmap.getWidth();         
//		int srcHeight = srcBitmap.getHeight();
//		float scaleWidth = ((float) targetWidth) / srcWidth;
//		float scaleHeight = ((float) targetHeight) / srcHeight;
//		Matrix matrix = new Matrix();
//		matrix.postScale(scaleWidth, scaleHeight);          
//		return Bitmap.createBitmap(srcBitmap, 0, 0, srcWidth, srcHeight, matrix, true);
//	}
//
//	public static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
//		int initialSize = computeInitialSampleSize(options, minSideLength,maxNumOfPixels);
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
//		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
//				.sqrt(w * h / maxNumOfPixels));
//		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math
//				.floor(w / minSideLength), Math.floor(h / minSideLength));
//
//		if (upperBound < lowerBound) {
//			// return the larger one when there is no overlapping zone.
//					return lowerBound;
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

}
