package com.hwatong.f70.commonsetting;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.hwatong.f70.baseview.BaseFragment;
import com.hwatong.f70.carsetting.F70CarSettingCommand;
import com.hwatong.f70.main.ConfigrationVersion;
import com.hwatong.f70.main.F70CanbusUtils;
import com.hwatong.f70.main.LogUtils;
import com.hwatong.providers.carsettings.SettingsProvider;
import com.hwatong.settings.R;
import com.hwatong.settings.Utils;

import android.app.Fragment;
import android.canbus.CarConfig;
import android.canbus.CarStatus;
import android.canbus.ICanbusService;
import android.canbus.ICarConfigListener;
import android.canbus.ICarStatusListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class DisplaySetting extends BaseFragment implements OnTouchListener,
		OnClickListener {

	private SeekBar wholeCarLight, mediaLight, dashboardLight;
	private RelativeLayout mediaLightAdd, mediaLightDrcre, dashboardLightAdd,
			dashboardLightDecre;
	private TextView wholeCarLightText, mediaLightText, dashboardLightText;
	private LinearLayout IPCLayout;
	private ImageView IPCIntervel;

	private ScheduledExecutorService scheduledExecutor;

	private static final int WHOLE_CAR_LIGHT_MAX = 15;
	private static final int MEDIA_LIGHT_MAX = 15;
	private static final int DASHBOARD_LIGHT_MAX = 15;
	// private static final int DASHBOARD_LIGHT_DIFF = 7;
	// private static final int DASHBOARD_LIGHT_DIFF_SEEKBAR = 4;
	// private static final int MEDIALIGHT_STEP = 1;
	// private static final int MEDIA_LIGHT_MIN = 10;
	public static final int MEDIA_LIGHTESSS = 3;
	public static final int DASHBOARD_LIGHTESSS = 4;

	private ICanbusService iCanbusService;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.f70_displaysetting,
				container, false);

		initWidget(rootView);
		initSeekBarListener();
		initService();
		return rootView;
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
	}

	@Override
	public void onResume() {
		super.onResume();
		initWholeCarLight();
		initMediaLight();
		initDashboardLight();
		// getActivity().getApplicationContext().getContentResolver().registerContentObserver(Uri.withAppendedPath(Utils.CONTENT_URI,
		// SettingsProvider.SCREEN_BRIGHTNESS), true,mBrightnessObserver);
		// getActivity().getApplicationContext().getContentResolver().registerContentObserver(Uri.withAppendedPath(Utils.CONTENT_URI,
		// SettingsProvider.HUD_BRIGHTNESS), true,mHudBrightnessObserver);
		
		changedActivityImage(this.getClass().getName());
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			iCanbusService.removeCarConfigListener(iCarConfigListener);
			iCanbusService.removeCarStatusListener(iCarStatusListener);
		} catch (RemoteException e) {
			LogUtils.d(e.toString());
			e.printStackTrace();
		}
		handler.removeCallbacksAndMessages(null);
		wholeCarLightHandler.removeCallbacksAndMessages(null);
	}

	// private ContentObserver mBrightnessObserver = new ContentObserver(
	// new Handler()) {
	// @Override
	// public void onChange(boolean selfChange) {
	// }
	// };
	// private ContentObserver mHudBrightnessObserver = new ContentObserver(
	// new Handler()) {
	// @Override
	// public void onChange(boolean selfChange) {
	// }
	// };

	
	private void initWidget(View rootView) {
		wholeCarLight = (SeekBar) rootView
				.findViewById(R.id.whole_car_light_seekbar);
		mediaLight = (SeekBar) rootView.findViewById(R.id.media_light_seekbar);
		dashboardLight = (SeekBar) rootView
				.findViewById(R.id.instrument_light_seekbar);

		// wholeCarLightAdd = (ImageButton) rootView
		// .findViewById(R.id.whole_car_light_add);
		// wholeCarLightDecre = (ImageButton) rootView
		// .findViewById(R.id.whole_car_light_decre);
		mediaLightAdd = (RelativeLayout) rootView
				.findViewById(R.id.media_light_add);
		mediaLightDrcre = (RelativeLayout) rootView
				.findViewById(R.id.media_light_decre);
		dashboardLightAdd = (RelativeLayout) rootView
				.findViewById(R.id.instrument_light_add);
		dashboardLightDecre = (RelativeLayout) rootView
				.findViewById(R.id.instrument_light_decre);

		wholeCarLightText = (TextView) rootView
				.findViewById(R.id.whole_car_text);
		mediaLightText = (TextView) rootView
				.findViewById(R.id.media_light_text);
		dashboardLightText = (TextView) rootView
				.findViewById(R.id.instrument_light_text);

//		wholeCarLightAdd.setOnTouchListener(this);
//		wholeCarLightDecre.setOnTouchListener(this);
		mediaLightAdd.setOnTouchListener(this);
		mediaLightDrcre.setOnTouchListener(this);
		dashboardLightAdd.setOnTouchListener(this);
		dashboardLightDecre.setOnTouchListener(this);

//		wholeCarLightAdd.setOnClickListener(this);
//		wholeCarLightDecre.setOnClickListener(this);
		mediaLightAdd.setOnClickListener(this);
		mediaLightDrcre.setOnClickListener(this);
		dashboardLightAdd.setOnClickListener(this);
		dashboardLightDecre.setOnClickListener(this);

		
		if (ConfigrationVersion.getInstance().isHight() || ConfigrationVersion.getInstance().isMiddleLuxury()) {
			IPCLayout = (LinearLayout) rootView.findViewById(R.id.ipc_layout);
			IPCIntervel = (ImageView) rootView.findViewById(R.id.ipc_intervel);
			IPCLayout.setVisibility(View.VISIBLE);
			IPCIntervel.setVisibility(View.VISIBLE);
		}
	}

	private void initService() {
		iCanbusService = ICanbusService.Stub.asInterface(ServiceManager
				.getService("canbus"));
		try {
			iCanbusService.addCarConfigListener(iCarConfigListener);
			iCanbusService.addCarStatusListener(iCarStatusListener);
		} catch (RemoteException e) {
			LogUtils.d(e.toString());
			e.printStackTrace();
		}
	}

	
	private void initWholeCarLight() {
		try {
			CarConfig initWholeCar = iCanbusService
					.getLastCarConfig(getActivity().getPackageName());
			handleWholeCarLight(initWholeCar);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	
	private void initMediaLight() {
		String matchLight = Utils.getCarSettingsString(
				getActivity().getContentResolver(),
				getCurrentMediaType());
		LogUtils.d("settingprovider: " + matchLight);
		int mediaLightValue = Integer.parseInt(matchLight == null ? "7" : matchLight);
		LogUtils.d("mediaLightValue: " + mediaLightValue);
		mediaLight.setProgress(mediaLightValue);
	}

	
	private void initDashboardLight() {
		try {
			CarConfig initDashboard = iCanbusService
					.getLastCarConfig(getActivity().getPackageName());
			handleDashboardLight(initDashboard);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	
	private void initSeekBarListener() {
		wholeCarLight.setMax(WHOLE_CAR_LIGHT_MAX);
		mediaLight.setMax(MEDIA_LIGHT_MAX);
		dashboardLight.setMax(DASHBOARD_LIGHT_MAX);

		wholeCarLight.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				setWholeCarLight(seekBar.getProgress());
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				wholeCarLightText.setText("" + progress);
			}
		});

		mediaLight.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				setMediaLightBySeekBar(seekBar);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				mediaLightText.setText("" + progress);
			}
		});

		dashboardLight
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						setDashboardLight(seekBar.getProgress());
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {

					}

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						dashboardLightText.setText("" + progress);
					}
				});
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			responseButtonEvent(v.getId(), true);
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			responseButtonEvent(v.getId(), false);
		}
		return false;
	}

	@Override
	public void onClick(View v) {

	}

	/**
	 *
	 * 
	 * @param buttonId
	 */
	private void responseButtonEvent(int buttonId, boolean isStart) {

		if (isStart)
			updateAddOrSubtract(buttonId);
		else
			stopAddOrSubtract();
	}

	/**
	 *
	 * 
	 * @param viewId
	 */
	private void updateAddOrSubtract(int viewId) {
		final int vid = viewId;
		if (scheduledExecutor == null)
			scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
		scheduledExecutor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				Message msg = Message.obtain();
				msg.what = vid;
				handler.sendMessage(msg);
			}
		}, 0, 200, TimeUnit.MILLISECONDS); 
	}

	/**
	 *
	 */
	private void stopAddOrSubtract() {
		if (scheduledExecutor != null) {
			scheduledExecutor.shutdownNow();
			scheduledExecutor = null;
		}
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			int viewId = msg.what;
			switch (viewId) {
			case R.id.whole_car_light_add:
				setWholeCarLightValue(wholeCarLight, true);
				break;

			case R.id.whole_car_light_decre:
				setWholeCarLightValue(wholeCarLight, false);
				break;

			case R.id.media_light_add:
				setMediaLightValue(mediaLight, true);
				break;

			case R.id.media_light_decre:
				setMediaLightValue(mediaLight, false);
				break;

			case R.id.instrument_light_add:
				setDashBoardLightValue(dashboardLight, true);
				break;

			case R.id.instrument_light_decre:
				setDashBoardLightValue(dashboardLight, false);
				break;

			default:
				break;
			}
		}
	};

	private Handler wholeCarLightHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MEDIA_LIGHTESSS:
				handleMediaLight((Integer) msg.obj);
				break;
			case DASHBOARD_LIGHTESSS:
//				handleWholeCarLight((CarConfig) msg.obj);
				handleDashboardLight((CarConfig) msg.obj);
				break;

			default:
				break;
			}

		}

	};

	/**
	 * 
	 * 
	 * @param seekBar
	 *   
	 * @param isAdd
	 * 
	 */
	private void setWholeCarLightValue(SeekBar seekBar, boolean isAdd) {
		int progress = seekBar.getProgress();
		int value = 0;
		if (isAdd) {
			value = progress + 1 > seekBar.getMax() ? seekBar.getMax()
					: progress + 1;
		} else {
			value = progress - 1 < 0 ? 0 : progress - 1;
		}
		seekBar.setProgress(value);
		setWholeCarLight(value);
		wholeCarLightText.setText("" + value);
	}

	/**
	 * 
	 * 
	 * @param seekBar
	 *  
	 * @param isAdd
	 * 
	 */
	private void setMediaLightValue(SeekBar seekBar, boolean isAdd) {
		int progress = seekBar.getProgress();
		int value = 0;
		int realValue = 0;
		if (isAdd) {
			value = progress + 1 > seekBar.getMax() ? seekBar.getMax()
					: progress + 1;
		} else {
			value = progress - 1 < 0 ? 0 : progress - 1;
		}

		realValue = value > MEDIA_LIGHT_MAX ? MEDIA_LIGHT_MAX : value;

		if (value <= 0)
			realValue = 0;// when seekbar value is 0

		seekBar.setProgress(value);

		Utils.putCarSettingsString(getActivity().getContentResolver(),
				getCurrentMediaType(), Integer.toString(realValue));

		LogUtils.d("setMediaLightValue: " + realValue);
		mediaLightText.setText("" + value);
	}
	
	private String getCurrentMediaType() {
		int type = 0;
		if(iCanbusService != null) {
			try {
				CarStatus carStatus = iCanbusService.getLastCarStatus(getActivity().getPackageName());
				if(carStatus != null) {
					type = carStatus.getStatus3();
					LogUtils.d("get iCanbusService type:" + type);
				}
				else
					LogUtils.d("carStatus is null");
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return type == 0 ? SettingsProvider.SCREEN_BRIGHTNESS : SettingsProvider.DIMMING_BRIGHTNESS;
	}

	/**
	 * seekbar
	 * 
	 * @param seekBar
	 */
	private void setMediaLightBySeekBar(SeekBar seekBar) {
		int value = seekBar.getProgress();
		if (value <= 0)
			value = 0;
		else if (value >= MEDIA_LIGHT_MAX)
			value = MEDIA_LIGHT_MAX;
		Utils.putCarSettingsString(getActivity().getContentResolver(),
				getCurrentMediaType(), Integer.toString(value));
		LogUtils.d("setMediaLightBySeekBar: " + value);
	}

	/**
	 *
	 * 
	 * @param seekBar
	 *  
	 * @param isAdd
	 * 
	 */
	private void setDashBoardLightValue(SeekBar seekBar, boolean isAdd) {
		int progress = seekBar.getProgress();
		int value = 0;
		if (isAdd) {
			value = progress + 1 > seekBar.getMax() ? seekBar.getMax()
					: progress + 1;
		} else {
			value = progress - 1 < 0 ? 0 : progress - 1;
		}
		seekBar.setProgress(value);
		setDashboardLight(value);
		dashboardLightText.setText("" + value);
	}

	/**
	 *
	 * 
	 * @param value
	 */
	private void setDashboardLight(int value) {
//		value += 3;
		LogUtils.d("setDashboardLight: " + value);
			if (iCanbusService != null)
//				iCanbusService.writeMHU12(
//						F70CarSettingCommand.TYPE_DASHBOARDLIGHT, value);
				F70CanbusUtils.getInstance().writeCarConfig(
						iCanbusService,
						F70CarSettingCommand.TYPE_WHOLECARLIGHT,
						value);
			else
				LogUtils.d("iCanbusService is null");
	}

	private void setWholeCarLight(int value) {
		LogUtils.d("setWholeCarLight: " + value);
		try {
			if (iCanbusService != null)
				iCanbusService.writeCarConfig(
						F70CarSettingCommand.TYPE_WHOLECARLIGHT, value);
			else
				LogUtils.d("iCanbusService is null");
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	//
	private void handleWholeCarLight(CarConfig carConfig) {
		if (carConfig != null) {
			int wholeCarLightvalue = carConfig.getStatus9();
			LogUtils.d("handleWholeCarLight: " + wholeCarLightvalue);
			wholeCarLight.setProgress(wholeCarLightvalue);
			wholeCarLightText.setText("" + wholeCarLightvalue);
		} else
			LogUtils.d("initWholeCar is null");
	}

	//
	private void handleDashboardLight(CarConfig carConfig) {
		if (carConfig != null) {
			int dashboardLightValue = carConfig.getStatus14();
			LogUtils.d("handleDashboardLight: " + dashboardLightValue);
//			if (dashboardLightValue < 3 || dashboardLightValue > 11)
//				dashboardLightValue = 7;
			dashboardLight.setProgress(dashboardLightValue);
			dashboardLightText.setText("" + dashboardLightValue);
		} else
			LogUtils.d("initdashboard light is null");

	}
	
	
	private void handleMediaLight(int type) {
		LogUtils.d("handleMediaLight type : " + type);
		String getType = (type == 0 ? SettingsProvider.SCREEN_BRIGHTNESS : SettingsProvider.DIMMING_BRIGHTNESS);
		String matchLight = Utils.getCarSettingsString(
				getActivity().getContentResolver(),
				getType);
		LogUtils.d("handleMediaLight: " + matchLight);
		int mediaLightValue = Integer.parseInt(matchLight == null ? "7" : matchLight);
		mediaLight.setProgress(mediaLightValue);
	}

	ICarConfigListener iCarConfigListener = new ICarConfigListener.Stub() {

		@Override
		public void onReceived(CarConfig carConfig) throws RemoteException {
			if(carConfig == null) {
				return;
			}
			
			LogUtils.d("iCarConfigListener wholecarlight:"
					+ carConfig.getStatus8() + " 14: " + carConfig.getStatus14());
			
			//得到反馈不处理，如果与界面不符，默默后台再发一次
			int dashboardLightValue = carConfig.getStatus14();
			if(dashboardLightValue != dashboardLight.getProgress()) {
				setDashboardLight(dashboardLight.getProgress());
			}
			
//			Message configMessage = Message.obtain();
//			configMessage.what = DASHBOARD_LIGHTESSS;
//			configMessage.obj = carConfig;
//			wholeCarLightHandler.sendMessage(configMessage);
		}
	};
	
	ICarStatusListener iCarStatusListener = new ICarStatusListener.Stub() {
		
		@Override
		public void onReceived(CarStatus carStatus) throws RemoteException {
			LogUtils.d("iCarStatusListener currentType: " + carStatus.getStatus3());
			Message msg = Message.obtain();
			msg.what = MEDIA_LIGHTESSS;
			msg.obj = carStatus.getStatus3();
			wholeCarLightHandler.sendMessage(msg);
		}
	};

}
