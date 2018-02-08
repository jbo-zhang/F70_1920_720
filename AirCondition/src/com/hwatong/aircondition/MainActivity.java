package com.hwatong.aircondition;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.canbus.ACStatus;
import android.canbus.CarStatus;
import android.canbus.IACStatusListener;
import android.canbus.ICanbusService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.hwatong.statusbarinfo.aidl.IStatusBarInfo;

public class MainActivity extends Activity implements OnClickListener, IVoiceView {

	private static final String TAG = "kongtiao";
	protected static final boolean DBG = false;

	private ICanbusService mCanbusService;

	private ImageButton mBtnReturn;

	private com.hwatong.aircondition.VerticalSeekBar mLeftTempSeekBar;

	// --吹风模式
	private ImageView mIvBlowerUp;

	private ImageView mIvBlowerFront;

	private ImageView mIvBlowerDown;
	// --吹风模式

	// ---风速加减
	private ImageView[] mWindLevels;

	private TextView mTvWindSwitch;

	private ImageView mIvWindLevel1;

	private ImageView mIvWindLevel2;

	private ImageView mIvWindLevel3;

	private ImageView mIvWindLevel4;

	private ImageView mIvWindLevel5;

	private ImageView mIvWindLevel6;

	private ImageView mIvWindLevel7;
	// ---风速加减

	private TextView mTvFrontDefrost;

	private TextView mTvRearDefrost;

	private TextView mTvAc;

	private TextView mTvLoop;

	private TextView mTvRearSwitch;
	
	private static final int DISAPPEAR_DELAY = 10000;

	private IStatusBarInfo mStatusBarInfo; // 状态栏左上角信息
	
	private String H_V_STR = "F70_H_V";
	private String L_V_STR = "F70_L_V";
	
	private int[] drawableIds = new int[] { R.drawable.wind_level_1,
			R.drawable.wind_level_2, R.drawable.wind_level_3,
			R.drawable.wind_level_4, R.drawable.wind_level_5,
			R.drawable.wind_level_6, R.drawable.wind_level_7 };


	private static final int MSG_AC_STATUS_RECEIVED = 0;

	@SuppressLint("HandlerLeak")
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_AC_STATUS_RECEIVED:
				handleACStatusReceived();
				break;
			case 20:
				sendCloseBroadcast();
				MainActivity.this.finish();
				break;
			}
		}
	};

	/**
	 * 
	 * @param img
	 * @return
	 */
	public Bitmap toturn(Bitmap bitmap) {
		Matrix matrix = new Matrix();
		// 设置旋转角度
		matrix.setRotate(90);
		// 重新绘制Bitmap
		bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), matrix, true);
		return bitmap;
	}

	private TempThumbDrable tempThumbDrable;

	public static Bitmap drawableToBitmap(Drawable drawable) {

		Bitmap bitmap = Bitmap.createBitmap(

		drawable.getIntrinsicWidth(),

		drawable.getIntrinsicHeight(),

		drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888

		: Bitmap.Config.RGB_565);

		Canvas canvas = new Canvas(bitmap);

		// canvas.setBitmap(bitmap);

		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight());

		drawable.draw(canvas);

		return bitmap;

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mCanbusService = ICanbusService.Stub.asInterface(ServiceManager.getService("canbus"));
		
		
//		getAccStatus();
//		
//		if(acc != 2) {
//			accNotOn();
//			finish();
//			return;
//		}
		
		setContentView(R.layout.activity_main);
		
		
		initView();

		mWindLevels = new ImageView[] { mIvWindLevel1, mIvWindLevel2,
				mIvWindLevel3, mIvWindLevel4, mIvWindLevel5, mIvWindLevel6,
				mIvWindLevel7 };

		mLeftTempSeekBar.setEnabled(true);
		/* 设置32是为了除了二可以得到float型 才会有0.5的出现 */
		mLeftTempSeekBar.setMax(32);

		tempThumbDrable = new TempThumbDrable(BitmapFactory.decodeResource(
				getResources(), R.drawable.seekbar_pointer));
		tempThumbDrable
				.setTemp(mLeftTempSeekBar.getProgress() / 2.0 + 16 + "℃");
		mLeftTempSeekBar.setThumb(new BitmapDrawable(
				toturn(drawableToBitmap(tempThumbDrable))));

		mLeftTempSeekBar
				.setUpDownListener(new com.hwatong.aircondition.VerticalSeekBar.UpDownListener() {

					@Override
					public void onTouch(int progress) {
						sendACKeyEvent("温度", 10, (int) (progress + 32));
					}
				});

		mLeftTempSeekBar
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					@Override
					public void onStopTrackingTouch(SeekBar arg0) {

					}

					@Override
					public void onStartTrackingTouch(SeekBar arg0) {

					}

					@Override
					public void onProgressChanged(SeekBar arg0, int progress,
							boolean fromUser) {
						
						refreshTimer();
						
						tempThumbDrable = new TempThumbDrable(BitmapFactory
								.decodeResource(getResources(),
										R.drawable.seekbar_pointer));
						tempThumbDrable.setTemp(progress / 2.0 + 16 + "℃");
						mLeftTempSeekBar.setThumb(new BitmapDrawable(
								toturn(drawableToBitmap(tempThumbDrable))));
						Log.d(TAG, "fromUser = " + fromUser);
					}
				});
		
		voicePresenter = new VoicePresenter(this, this);

	}

	private void initView() {
		mBtnReturn = (ImageButton) findViewById(R.id.btn_return);
		mBtnReturn.setOnClickListener(this);

		mLeftTempSeekBar = (VerticalSeekBar) findViewById(R.id.seekbar_left_temp);

		// 除雾（向上吹风）,平行吹风，向下吹风
		mIvBlowerUp = (ImageView) findViewById(R.id.iv_blower_up);
		mIvBlowerUp.setOnClickListener(this);

		mIvBlowerFront = (ImageView) findViewById(R.id.iv_blower_front);
		mIvBlowerFront.setOnClickListener(this);

		mIvBlowerDown = (ImageView) findViewById(R.id.iv_blower_down);
		mIvBlowerDown.setOnClickListener(this);
		
		// 风速 1~7
		mTvWindSwitch = (TextView) findViewById(R.id.btn_wind_switch);
		mTvWindSwitch.setOnClickListener(this);

		mIvWindLevel1 = (ImageView) findViewById(R.id.btn_wind_level_1);
		mIvWindLevel1.setOnClickListener(this);

		mIvWindLevel2 = (ImageView) findViewById(R.id.btn_wind_level_2);
		mIvWindLevel2.setOnClickListener(this);

		mIvWindLevel3 = (ImageView) findViewById(R.id.btn_wind_level_3);
		mIvWindLevel3.setOnClickListener(this);

		mIvWindLevel4 = (ImageView) findViewById(R.id.btn_wind_level_4);
		mIvWindLevel4.setOnClickListener(this);

		mIvWindLevel5 = (ImageView) findViewById(R.id.btn_wind_level_5);
		mIvWindLevel5.setOnClickListener(this);

		mIvWindLevel6 = (ImageView) findViewById(R.id.btn_wind_level_6);
		mIvWindLevel6.setOnClickListener(this);

		mIvWindLevel7 = (ImageView) findViewById(R.id.btn_wind_level_7);
		mIvWindLevel7.setOnClickListener(this);

		// 模式：前部，后部，空调，内循环，后空调
		mTvFrontDefrost = (TextView) findViewById(R.id.btn_front_defrost);
		mTvFrontDefrost.setOnClickListener(this);

		mTvRearDefrost = (TextView) findViewById(R.id.btn_rear_defrost);
		mTvRearDefrost.setOnClickListener(this);

		mTvAc = (TextView) findViewById(R.id.btn_ac);
		mTvAc.setOnClickListener(this);

		mTvLoop = (TextView) findViewById(R.id.btn_loop);
		mTvLoop.setOnClickListener(this);

		mTvRearSwitch = (TextView) findViewById(R.id.btn_rear);
		mTvRearSwitch.setOnClickListener(this);
		
	
		
		//去掉之前根据版本号判断高低配的操作
//		if(getSoftwareVersion().contains(H_V_STR)) {
//			mTvRearSwitch.setVisibility(View.VISIBLE);
//		} else if(getSoftwareVersion().contains(L_V_STR)) {
//			mTvRearSwitch.setVisibility(View.GONE);
//		}
		
		//中配豪华和高配旗舰有后空调，其他没有
//		if(getCarType() == 3 || getCarType() == 4) {	
//			mTvRearSwitch.setVisibility(View.VISIBLE);
//		} else if(getCarType() == 1 || getCarType() == 2){
//			mTvRearSwitch.setVisibility(View.GONE);
//		}
		
	}

	boolean mStoped = false;
	boolean mPopMode = false;

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Log.d(TAG, "onNewIntent " + mStoped);
		if (mStoped) {
			setIntent(intent);
		}
	};

	@Override
	protected void onStart() {
		super.onStart();
		mStoped = false;
		Log.d(TAG, "onStart");
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		refreshTimer();
		
		// 绑定状态栏服务
		bindService(new Intent("com.remote.hwatong.statusinfoservice"),
				mConn2, BIND_AUTO_CREATE);
		
		syncStatusBar();
		
		//中配豪华和高配旗舰有后空调，其他没有
		if(getCarType() == 3 || getCarType() == 4) {	
			mTvRearSwitch.setVisibility(View.VISIBLE);
		} else if(getCarType() == 1 || getCarType() == 2){
			mTvRearSwitch.setVisibility(View.GONE);
		} else {
			mTvRearSwitch.setVisibility(View.GONE);
		}
		
		
		if(mCanbusService != null) {
			try {
				mCanbusService.addACStatusListener(mACStatusListener);
			} catch (RemoteException e) {
				e.printStackTrace();
			}

			handleACStatusReceived();
		}
		
	};
	
	protected IStatusBarInfo iStatusBarInfo;
	private void syncStatusBar() {
		if(iStatusBarInfo != null) {
			try {
				iStatusBarInfo.setCurrentPageName("air_conditioning");
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else {
			Intent intent = new Intent();
			intent.setAction("com.remote.hwatong.statusinfoservice");
			bindService(intent, mConn2, BIND_AUTO_CREATE);
		}
	}
	
	protected ServiceConnection mConn2 = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			iStatusBarInfo = null;
		}

		@Override
		public void onServiceConnected(ComponentName arg0, IBinder binder) {
			iStatusBarInfo = IStatusBarInfo.Stub.asInterface(binder);
			try {
				if (iStatusBarInfo != null)
					Log.d(TAG, "setCurrentPageName air_conditioning");
					iStatusBarInfo.setCurrentPageName("air_conditioning");
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	};
	
	

	@Override
	public void onClick(View v) {
		refreshTimer();

		switch (v.getId()) {

		case R.id.iv_blower_up:
			Log.d(TAG, "blower_up");
			sendACKeyEvent_2("向上吹风", 4, v.getVisibility() == View.VISIBLE ? 0 : 1);
			break;
			
		case R.id.iv_blower_front:
			Log.d(TAG, "blower_front");
			sendACKeyEvent_2("平行吹风", 5, v.getVisibility() == View.VISIBLE ? 0 : 1);
			break;
			
		case R.id.iv_blower_down:
			Log.d(TAG, "blower_down");
			sendACKeyEvent_2("向下吹风", 6, v.getVisibility() == View.VISIBLE ? 0 : 1);
			break;

		case R.id.btn_return:
			sendCloseBroadcast();
			finish();
			break;

		case R.id.btn_wind_level_1:
		case R.id.btn_wind_level_2:
		case R.id.btn_wind_level_3:
		case R.id.btn_wind_level_4:
		case R.id.btn_wind_level_5:
		case R.id.btn_wind_level_6:
		case R.id.btn_wind_level_7:
			int level = Integer.parseInt((String) v.getTag());
			sendACKeyEvent("风速", 3, level);
			break;
			
		case R.id.btn_ac:
			sendACKeyEvent_2("ac", 1, v.isSelected() ? 0 : 1);
			break;
			
		case R.id.btn_front_defrost:
			sendACKeyEvent_2("前除霜", 0, v.isSelected() ? 0 : 1);
			break;
			
		case R.id.btn_rear_defrost:
			sendACKeyEvent_2("后除霜", 2, v.isSelected() ? 0 : 1);
			break;
			
		case R.id.btn_wind_switch:
			//空调总开关
			sendACKeyEvent_2("空调开关", 8, 0);
			break;
			
		case R.id.btn_rear:
			sendACKeyEvent_2("后空调开关按下", 7, v.isSelected() ? 0 : 1);
			break;
			
		case R.id.btn_loop:
			sendACKeyEvent_2("内外循环", 9, v.isSelected() ? 0 : 1);
			break;

		default:
			break;
		}
	}
	

	/* 摄氏度和华氏度转换 */
	public float tempExchange(float temp) {
		float left = 0;
		if (temp >= 16 && temp <= 32) {
			left = temp * 9 / 5 + 32;
		} else {
			left = (temp - 32) * 5 / 9;
		}
		return left;
	}

	private void sendACKeyEvent(String tag, int code, int status) {
		Log.d(TAG, tag + ":(" + code + "," + status + ")");
		sendACKeyEvent(code, status);
	}
	
	private synchronized void sendACKeyEvent_2(String tag, int code, int status) {
		Log.d(TAG, tag + ":(" + code + "," + status + ")");
		sendACKeyEvent(code, 1);
		sendACKeyEvent(code, 0);
	}

	/**
	 * 发送到 CanBus
	 * @param code
	 * @param status
	 */
	private void sendACKeyEvent(int code, int status) {
		try {
			Log.d(TAG, "before write code = " + code + " status = " + status);
			if(mCanbusService != null) {
				mCanbusService.writeACControl(code, status);
			}
			Log.d(TAG, "after write code = " + code + " status = " + status);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 数据反馈
	 */
	private final IACStatusListener mACStatusListener = new IACStatusListener.Stub() {

		@Override
		public void onReceived(ACStatus arg0) throws RemoteException {
			Log.d(TAG, "onReceived: " + arg0);
			mHandler.removeMessages(MSG_AC_STATUS_RECEIVED);
			mHandler.sendEmptyMessage(MSG_AC_STATUS_RECEIVED);
		}
	};
	
	private ACStatus mACStatus;

	/**
	 * 获取最新状态，设置界面
	 */
	private void handleACStatusReceived() {

		ACStatus status = null;
		try {
			if(mCanbusService != null) {
				status = mCanbusService.getLastACStatus(getPackageName());
			}
			Log.e(TAG, "status : " + status);
		} catch (RemoteException e) {
			e.printStackTrace();
			status = null;
		}

		if (status != null) {
			if (mACStatus == null || !mACStatus.equals(status)) { // 如果按钮状态为空,切按钮状态改变

				if (mACStatus == null)
					mACStatus = new ACStatus();
				mACStatus.set(status);

				Log.e(TAG, "handleACStatusReceived: mAPLStatus = " + mACStatus);
				
				// 首先判断空调开关状态     0x0: Off 按钮灰    0x1: On 按钮点亮
				int windOnOff = status.getStatus7() & 0x0F;
				//空调关
				if(windOnOff == 0x01) {		
					turnOffViews();
					// 后空调开关
					int rear = status.getStatus3() & 0x03;
					setRear(rear == 0x00);
					
					// 后除霜
					int rearDefrost = status.getStatus12() & 0x03;
					setRearDefrost(rearDefrost == 0x01);
					return;
				//空调开
				} else {
					turnOnViews();
				}

				// 温度
				int value = status.getStatus1() & 0xFF;
				setTemperature(value);
				
				//吹风模式
				
				//吹头
				setBlowerUp((status.getStatus8() & 0x03) == 0x01);
				
				//吹胸
				setBlowerMiddle((status.getStatus9() & 0x03) == 0x01);
				
				//吹脚
				setBlowerDown((status.getStatus10() & 0x03) == 0x01);
				
				
				// 风速
				int valuewind = status.getStatus2() & 0x0F;
				setWindLevel(valuewind);
				
				
				// 前除霜
				int frontDefrost = status.getStatus6() & 0x03;
				setFrontDefrost(frontDefrost == 0x01);

				
				// 后除霜
				int rearDefrost = status.getStatus12() & 0x03;
				setRearDefrost(rearDefrost == 0x01);
				
				
				// AC
				int ac = status.getStatus5() & 0x03;
				setAC(ac == 0x01);
				
				// 循环模式 
				int loop = status.getStatus4() & 0x03;
				setLoop(loop);
				
				int rear = status.getStatus3() & 0x03;
				setRear(rear == 0x00);
				
			}
		}
	}


	/**
	 * 空调关闭，所有状态清零
	 */
	private void turnOffViews() {
		Log.d(TAG, "turn off Views !");
		
		setSwitch(false);
		
		//置灰
		setTemperature(32);
		setBlowerUp(false);
		setBlowerMiddle(false);
		setBlowerDown(false);
		setWindLevel(0);
		setFrontDefrost(false);
		setRearDefrost(false);
		setAC(false);
		setLoop(-1);
		setRear(false);
		
		//使能
		setSeekBarEnabled(false);
		setBlowerUpEnabled(false);
		setBlowerMiddleEnabled(false);
		setBlowerDownEnabled(false);
		//setWindLevelEnabled(false);
		//setFrontDefrostEnabled(false);
		setACEnabled(false);
		setLoopEnabled(false);
	}
	
	/**
	 * 空调开启，使能各个按钮
	 */
	private void turnOnViews() {
		setSwitch(true);
		
		setSeekBarEnabled(true);
		setSeekBarEnabled(true);
		setBlowerUpEnabled(true);
		setBlowerMiddleEnabled(true);
		setBlowerDownEnabled(true);
		//setWindLevelEnabled(true);
		//setFrontDefrostEnabled(true);
		setACEnabled(true);
		setLoopEnabled(true);
		
	}
	
	
	
	/**
	 * 空调开关
	 * @param isOn
	 */
	private void setSwitch(boolean isOn) {
		mTvWindSwitch.setSelected(!isOn);
	}
	
	
	
	/**
	 * 设置温度
	 * @param value 32 ~ 64 
	 */
	private void setTemperature(int value) {
		value = value < 32 ? 32 : value > 64 ? 64 : value;
		mLeftTempSeekBar.setProgress(value - 32);

		float temp = (float) value * 0.5f;
		tempThumbDrable.setTemp(temp + "℃");
		Log.e("temp", "" + temp);
	}
	
	private void setSeekBarEnabled(boolean enabled) {
		mLeftTempSeekBar.setEnabled(enabled);
	}
	
	
	/**
	 * 吹头
	 * @param isOn
	 */
	private void setBlowerUp(boolean isOn) {
		mIvBlowerUp.setVisibility(isOn ? View.VISIBLE : View.INVISIBLE);
	}
	
	private void setBlowerUpEnabled(boolean enabled) {
		mIvBlowerUp.setEnabled(enabled);
	}
	
	/**
	 * 吹胸
	 * @param isOn
	 */
	private void setBlowerMiddle(boolean isOn) {
		mIvBlowerFront.setVisibility(isOn ? View.VISIBLE : View.INVISIBLE);
	}
	
	private void setBlowerMiddleEnabled(boolean enabled) {
		mIvBlowerFront.setEnabled(enabled);
	}
	
	
	/**
	 * 吹脚
	 * @param isOn
	 */
	private void setBlowerDown(boolean isOn) {
		mIvBlowerDown.setVisibility(isOn ? View.VISIBLE : View.INVISIBLE);
	}
	
	private void setBlowerDownEnabled(boolean enabled) {
		mIvBlowerDown.setEnabled(enabled);
	}
	
	
	/**
	 * 风速
	 * @param level
	 */
	private void setWindLevel(int level) {
		if (level > mWindLevels.length)
			level = mWindLevels.length;

		for (int i = 0; i < level; i++) {
			mWindLevels[i].setImageAlpha(255);
		}
		for (int i = level; i < mWindLevels.length; i++) {
			mWindLevels[i].setImageAlpha(0);
		}
	}
	
	private void setWindLevelEnabled(boolean enabled) {
		for(int i = 0; i < mWindLevels.length; i++) {
			mWindLevels[i].setEnabled(enabled);
		}
	}
	
	
	
	/**
	 * 前除霜
	 * @param isOn
	 */
	private void setFrontDefrost(boolean isOn) {
		mTvFrontDefrost.setSelected(isOn);
	}
	
	private void setFrontDefrostEnabled(boolean enabled) {
		mTvFrontDefrost.setEnabled(enabled);
	}
	
	
	/**
	 * 后除霜
	 * @param isOn
	 */
	private void setRearDefrost(boolean isOn) {
		mTvRearDefrost.setSelected(isOn);
	}
	
	/**
	 * AC
	 * @param isOn
	 */
	private void setAC(boolean isOn) {
		mTvAc.setSelected(isOn);
	}
	
	private void setACEnabled(boolean enabled) {
		mTvAc.setEnabled(enabled);;
	}
	
	

	/**
	 * 循环模式
	 * @param loop
	 */
	private void setLoop(int loop) {
		//0x0: 外循环模式      0x1: 内循环模式      0x2: 自动循环模式
		if(loop == 0x00) {
			//外循环
			setLoopView(R.drawable.icon_loop_out, R.string.text_loop_out);
		} else if(loop == 0x01) {
			//内循环
			setLoopView(R.drawable.icon_loop_in, R.string.text_loop_in);
		} else if(loop == 0x02) {
			//自动循环
			
			if(getCarType() == 4) {	
				setLoopView(R.drawable.icon_loop_auto, R.string.text_loop_auto);
			} else if(getCarType() == 1 || getCarType() == 2 || getCarType() == 3){
				mTvLoop.setSelected(false);
			} else {
				mTvLoop.setSelected(false);
			}
			
			//去掉之前根据版本号判断高低配
//			if(getSoftwareVersion().contains(H_V_STR)) {
//				setLoopView(R.drawable.icon_loop_auto, R.string.text_loop_auto);
//			} else {
//				mTvLoop.setSelected(false);
//			}
			
		} else if(loop == -1){
			mTvLoop.setSelected(false);
		}
	}
	
	private void setLoopEnabled(boolean enabled) {
		mTvLoop.setEnabled(enabled);
	}
	
	/**
	 * 设置循环模式按钮
	 * @param iconId
	 * @param textId
	 */
	private void setLoopView(int iconId, int textId) {
		Drawable drawable = getResources().getDrawable(iconId);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		mTvLoop.setCompoundDrawables(null, drawable, null, null);
		mTvLoop.setText(textId);
		mTvLoop.setSelected(true);
	}
	

	/**
	 * 后空调
	 * @param isOn
	 */
	private void setRear(boolean isOn) {
		mTvRearSwitch.setSelected(isOn);
	}
	

	@Override
	protected void onPause() {
		Log.d(TAG, "onPause");
		mStatusBarInfo = null;
		unbindService(mConn2);
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mStoped = true;
		Log.d(TAG, "onStop");
	}

	@Override
	protected void onDestroy() {
		try {
			if(mCanbusService != null) {
				mCanbusService.removeACStatusListener(mACStatusListener);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		mHandler.removeMessages(MSG_AC_STATUS_RECEIVED);
		mHandler.removeCallbacksAndMessages(null);
		
		sendCloseBroadcast();
		Log.d(TAG, "onDestroy sendBroadcast!");
		
		voicePresenter.unregisterBroadcast(this);
		
		super.onDestroy();
	}
	
	private synchronized void refreshTimer() {
		mHandler.removeMessages(20);
		mHandler.sendEmptyMessageDelayed(20, DISAPPEAR_DELAY);
	}
	
	private void sendCloseBroadcast() {
		Intent intent = new Intent("com.hwatong.action.AIR_CONDITION_CLOSE");
		sendBroadcast(intent);
		Log.d(TAG, "after send broadcast !");
	}
	
	
	private int acc = 0;
	private VoicePresenter voicePresenter;
	/**
	 * acc提示
	 */
	private void getAccStatus() {
		try {
			if (mCanbusService != null) {
				CarStatus carStatus = mCanbusService.getLastCarStatus(getPackageName());
				acc = carStatus.getStatus1();
				Log.d(TAG, carStatus.toString() + " ; " + acc);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * acc提示弹框
	 */
	private void accNotOn() {
		Intent intent = new Intent("com.hwatong.backservice.BackService");
		intent.putExtra("cmd", "alarm");
		intent.putExtra("type", "noacc");
		intent.putExtra("run", "true");
		startService(intent);
	}

	@Override
	public void close() {
		finish();
	}
	
	/**
	 * 获取软件版本内容
	 */
    private String getSoftwareVersion() {
    	//return "F70_L_Vxxxx";
		return android.os.Build.ID;
	}
    
    /**
     * 返回值int：  1，表示低配；
				2，表示中配精英型；
				3，表示中配豪华型；
				4，表示高配；
				0，表示未知【由于精英和豪华是通过CAN消息判断，可能存在延时，在未收到CAN消息之前，默认为0】
     * @return
     */
    private int getCarType() {
    	int carType = 0;
    	if(mCanbusService != null) {
    		try {
				carType = mCanbusService.getCarConfigType();
				Log.d(TAG, "get carType : " + carType);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
    	}
    	
    	return carType;
    }
    
    
    
    
    
	
	
}
