package com.hwatong.f70.soundsetting;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.hwatong.f70.baseview.BaseFragment;
import com.hwatong.f70.main.LogUtils;
import com.hwatong.f70.observable.Function;
import com.hwatong.f70.observable.ObservableManager;
import com.hwatong.settings.R;
import com.hwatong.settings.widget.StartPointSeekBar;
import com.hwatong.settings.widget.StartPointSeekBar.OnStartPointSeekBarChangeListener;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/**
 * 
 * @author ljw ���û�иı�ֵ��ÿ�ν���������Ϊ��һ�ο���
 * 
 */
public class CurrentSoundSetting extends BaseFragment implements OnClickListener,
		OnTouchListener, Function<Object, Object> {

	private SeekBar bassSeekbar, midSeekbar, trebleSeekbar;// ���е�������

	private StartPointSeekBar l_r_Seekbar, f_b_Seekbar;// ǰ������ƽ��

	private TextView bassText, midText, trebleText, l_r_Text, f_b_Text;// ������ֵ����ʾ

	private ImageButton bass_decre, bass_add, mid_decre, mid_add, treble_decre,
			treble_add;// ���е���������ť

	private ImageButton l_r_decre, l_r_add, f_b_decre, f_b_add;// ǰ������ƽ��������ť

	private static final int EQ_MAX = 14;

	private final static int BASS = 0;
	private final static int MID = 1;
	private final static int TREBLE = 2;
	private final static int L_R_EQUAL = 3;
	private final static int F_B_EQUAL = 4;

	private ScheduledExecutorService scheduledExecutor;// java�ڲ��ṩ������Ķ�ʱִ��������
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ObservableManager.newInstance().registerObserver(FuntionCommon.CURRENT_SOUND_SETTING_RESULT_FRAGMENT, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.f70_currentvolume_setting,
				container, false);

		initWidget(rootView);
		initSeekBarListener();

		initFaderAndBalance();

//		initModeStatus();
		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
		// initAllConfig();
		initBassValue();
		changedActivityImage(this.getClass().getName()); 
	}

	@Override
	public void onStop() {
		super.onStop();
		LogUtils.d("current bass: " + bassSeekbar.getProgress()
				+ ", current mid: " + midSeekbar.getProgress()
				+ ", current treble: " + trebleSeekbar.getProgress()
				+ ", current balance: " + l_r_Seekbar.getProcess()
				+ ", current fader: " + f_b_Seekbar.getProcess());

		// saveCurrentEffectValueAfterExit();
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		if (!hidden) { // ��activityδ�˳����½���ý����ʱ��
		} else {// ���˽�������ʱ�ͷ���Դ);
			handler.removeCallbacksAndMessages(null);
		}
		super.onHiddenChanged(hidden);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		ObservableManager.newInstance().removeObserver(this);
	}

	private void initWidget(View rootView) {
		bassSeekbar = (SeekBar) rootView.findViewById(R.id.bass_seekbar);
		midSeekbar = (SeekBar) rootView.findViewById(R.id.mid_seekbar);
		trebleSeekbar = (SeekBar) rootView.findViewById(R.id.treble_seekbar);

		l_r_Seekbar = (StartPointSeekBar) rootView
				.findViewById(R.id.r_l_equal_seekbar);
		f_b_Seekbar = (StartPointSeekBar) rootView
				.findViewById(R.id.f_b_equal_seekbar);

		bassText = (TextView) rootView.findViewById(R.id.bass_text);
		midText = (TextView) rootView.findViewById(R.id.mid_text);
		trebleText = (TextView) rootView.findViewById(R.id.treble_text);
		l_r_Text = (TextView) rootView.findViewById(R.id.r_l_equal_text);
		f_b_Text = (TextView) rootView.findViewById(R.id.f_b_equal_text);

		bass_add = (ImageButton) rootView.findViewById(R.id.bass_add);
		bass_decre = (ImageButton) rootView.findViewById(R.id.bass_decre);
		mid_add = (ImageButton) rootView.findViewById(R.id.mid_add);
		mid_decre = (ImageButton) rootView.findViewById(R.id.mid_decre);
		treble_add = (ImageButton) rootView.findViewById(R.id.treble_add);
		treble_decre = (ImageButton) rootView.findViewById(R.id.treble_decre);

		l_r_decre = (ImageButton) rootView.findViewById(R.id.r_l_equal_decre);
		l_r_add = (ImageButton) rootView.findViewById(R.id.r_l_equal_add);
		f_b_add = (ImageButton) rootView.findViewById(R.id.f_b_equal_add);
		f_b_decre = (ImageButton) rootView.findViewById(R.id.f_b_equal_decre);

		bass_add.setOnTouchListener(this);
		bass_decre.setOnTouchListener(this);
		mid_add.setOnTouchListener(this);
		mid_decre.setOnTouchListener(this);
		treble_add.setOnTouchListener(this);
		treble_decre.setOnTouchListener(this);
		l_r_decre.setOnTouchListener(this);
		l_r_add.setOnTouchListener(this);
		f_b_add.setOnTouchListener(this);
		f_b_decre.setOnTouchListener(this);

		bass_add.setOnClickListener(this);
		bass_decre.setOnClickListener(this);
		mid_add.setOnClickListener(this);
		mid_decre.setOnClickListener(this);
		treble_add.setOnClickListener(this);
		treble_decre.setOnClickListener(this);
		l_r_decre.setOnClickListener(this);
		l_r_add.setOnClickListener(this);
		f_b_add.setOnClickListener(this);
		f_b_decre.setOnClickListener(this);

	}

	private void initBassValue() {
		// ��ʼ���ص���
		String bass = EffectUtils.getBassValue();
		String mid = EffectUtils.getMidValue();
		String treble = EffectUtils.getTrebleValue();

		LogUtils.d("get bass value��" + bass + "get mid value��" + mid
				+ "get treble value��" + treble);

		// ������ֵ
		if (bass == null)
			return;
		bassText.setText("" + getDisplayTextviewBassValue(bass));
		midText.setText("" + getDisplayTextviewBassValue(mid));
		trebleText.setText("" + getDisplayTextviewBassValue(treble));

		bassSeekbar.setProgress(getDisplaySeekbarBassValue(bass));
		midSeekbar.setProgress(getDisplaySeekbarBassValue(mid));
		trebleSeekbar.setProgress(getDisplaySeekbarBassValue(treble));

		LogUtils.d("get display value��" + getDisplayTextviewBassValue(bass)
				+ "get display value��" + getDisplayTextviewBassValue(mid)
				+ "get display value��" + getDisplayTextviewBassValue(treble)
				+ ", " + getDisplaySeekbarBassValue(bass) + ", "
				+ getDisplaySeekbarBassValue(mid) + ", "
				+ getDisplaySeekbarBassValue(treble));
	}

	private void initFaderAndBalance() {
		// ��ʼ������ƽ��
		String l_r_value = EffectUtils.getBalanceValue();// �ڵ����ֵ
		int balanceInitVlue = getDisplayBalanceValue(l_r_value);
		LogUtils.d("getDisplayBalanceValue: " + balanceInitVlue);

		// if (isFirstBoot("balance")) { // ��һ�ο���
		// l_r_Seekbar.setProgress(Integer.parseInt(l_r_value));
		// l_r_Text.setText(l_r_value);
		// } else {
		l_r_Seekbar.setProgress(balanceInitVlue);
		l_r_Text.setText("" + balanceInitVlue);
		// }

		// ��ʼ��fader
		String f_b_value = EffectUtils.getFaderValue();// �ڵ����ֵ
		int faderInitVlue = getDisplayFaderValue(f_b_value);// Ҫ��ʾ��Textview�ϵ�ֵ
		LogUtils.d("getDisplayFaderValue: " + faderInitVlue);
		// if (isFirstBoot("fader")) { // ��һ�ο���
		// f_b_Seekbar.setProgress(Integer.parseInt(f_b_value));
		// f_b_Text.setText(f_b_value);
		// } else {
		f_b_Seekbar.setProgress(faderInitVlue);
		f_b_Text.setText("" + faderInitVlue);
		// }
		syncActivityData(balanceInitVlue, faderInitVlue);
	}

	// private void initAllConfig() {
	// // ��ʼ���ص���
	// String bass = EffectUtils.getBassValue();
	// String mid = EffectUtils.getMidValue();
	// String treble = EffectUtils.getTrebleValue();
	//
	// LogUtils.d("get bass value��" + bass + "get mid value��" + mid
	// + "get treble value��" + treble);
	// if (bass == null)
	// return;
	//
	// // int bassProgress = getDisplaySeekbarBassValue(bass);
	// // int midProgress = getDisplaySeekbarBassValue(mid);
	// // int trebleProgress = getDisplaySeekbarBassValue(treble);
	// bassText.setText("" + getDisplayTextviewBassValue(bass));
	// midText.setText("" + getDisplayTextviewBassValue(mid));
	// trebleText.setText("" + getDisplayTextviewBassValue(treble));
	//
	// bassSeekbar.setProgress(getDisplaySeekbarBassValue(bass));
	// midSeekbar.setProgress(getDisplaySeekbarBassValue(mid));
	// trebleSeekbar.setProgress(getDisplaySeekbarBassValue(treble));
	//
	// LogUtils.d("get display value��" + getDisplayTextviewBassValue(bass) +
	// "get display value��" + getDisplayTextviewBassValue(mid)
	// + "get display value��" + getDisplayTextviewBassValue(treble) + ", " +
	// getDisplaySeekbarBassValue(bass) + ", " + getDisplaySeekbarBassValue(mid)
	// + ", " + getDisplaySeekbarBassValue(treble));
	//
	// // ��ʼ������ƽ��
	// String l_r_value = EffectUtils.getBalanceValue();// �ڵ����ֵ
	// int balanceInitVlue = getDisplayBalanceValue(l_r_value);
	// LogUtils.d("getDisplayBalanceValue: " + balanceInitVlue);
	//
	// // if (isFirstBoot("balance")) { // ��һ�ο���
	// // l_r_Seekbar.setProgress(Integer.parseInt(l_r_value));
	// // l_r_Text.setText(l_r_value);
	// // } else {
	// l_r_Seekbar.setProgress(balanceInitVlue);
	// l_r_Text.setText("" + balanceInitVlue);
	// // }
	//
	// // ��ʼ��fader
	// String f_b_value = EffectUtils.getFaderValue();// �ڵ����ֵ
	// int faderInitVlue = getDisplayFaderValue(f_b_value);// Ҫ��ʾ��Textview�ϵ�ֵ
	// LogUtils.d("getDisplayFaderValue: " + faderInitVlue);
	// // if (isFirstBoot("fader")) { // ��һ�ο���
	// // f_b_Seekbar.setProgress(Integer.parseInt(f_b_value));
	// // f_b_Text.setText(f_b_value);
	// // } else {
	// f_b_Seekbar.setProgress(faderInitVlue);
	// f_b_Text.setText("" + faderInitVlue);
	// // }
	// }

	// �����ǰ��������Чģʽ�����ܵ��ڸ��е���
	private void initModeStatus() {
		String currentModeName = EffectUtils.getCarSettingsString(getActivity()
				.getContentResolver(), EffectUtils.EQUALIZER_MODE);
		LogUtils.d("initEffectRadioGroup: " + currentModeName);
		if (!currentModeName.equals(EffectUtils.CUSTOMER)) {
			bassSeekbar.setEnabled(false);
			midSeekbar.setEnabled(false);
			trebleSeekbar.setEnabled(false);

			bass_decre.setEnabled(false);
			bass_add.setEnabled(false);
			mid_decre.setEnabled(false);
			mid_add.setEnabled(false);
			treble_decre.setEnabled(false);
			treble_add.setEnabled(false);
		}
	}

	// ��ʼ��seekbar�ļ����¼�
	private void initSeekBarListener() {
		bassSeekbar.setMax(EQ_MAX);
		midSeekbar.setMax(EQ_MAX);
		trebleSeekbar.setMax(EQ_MAX);

		// ��������
		bassSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// Ҫд���ڵ��ֵ
				int value = getWriteBassValue(seekBar.getProgress());
				if (!EffectUtils.setBassValue(value))
					LogUtils.d("write bass failed!");
				EffectUtils.setCustomBass(value);
				EffectUtils.setCurrentBass(value);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
//				changeEffectToCustomer();
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// ��ʾ�ڽ����ϵ�ֵ
				if (fromUser) {
					progress -= 7;
					bassText.setText("" + progress);
				}
			}
		});

		// ��������
		midSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// Ҫд���ڵ��ֵ
				int value = getWriteBassValue(seekBar.getProgress());
				if (!EffectUtils.setMidValue(value))
					LogUtils.d("write mid failed!");
				EffectUtils.setCustomMid(value);
				EffectUtils.setCurrentMid(value);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
//				changeEffectToCustomer();
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// ��ʾ�ڽ����ϵ�ֵ
				if (fromUser) {
					progress -= 7;
					midText.setText("" + progress);
				}
			}
		});

		// ��������
		trebleSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// Ҫд���ڵ��ֵ
				int value = getWriteBassValue(seekBar.getProgress());
				if (!EffectUtils.setTrebleValue(value))
					LogUtils.d("write treble failed!");
				EffectUtils.setCustomTreble(value);
				EffectUtils.setCurrentTreble(value);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
//				changeEffectToCustomer();
				LogUtils.d("trebleSeekbar: " + seekBar.getProgress());
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// ��ʾ�ڽ����ϵ�ֵ
				if (fromUser) {
					progress -= 7;
					trebleText.setText("" + progress);
				}

			}
		});

		// ����ƽ��
		l_r_Seekbar
				.setOnStartPointSeekBarChangeListener(new OnStartPointSeekBarChangeListener() {

					@Override
					public void onOnSeekBarValueChange(StartPointSeekBar bar,
							int value) {
					}

					@Override
					public void onStopTrackingTouch(StartPointSeekBar bar,
							int value, boolean fromUser) {
						l_r_Text.setText("" + value);
						value = getWriteBalanceValue(value);
						if (fromUser && setBalanceValue(value)) {
							LogUtils.d("write balance success!");
							syncTouchLayoutValue();
						}
						EffectUtils.setCurrentBalance(value);
					}
				});

		// ǰ��ƽ��
		f_b_Seekbar
				.setOnStartPointSeekBarChangeListener(new OnStartPointSeekBarChangeListener() {

					@Override
					public void onStopTrackingTouch(StartPointSeekBar bar,
							int value, boolean fromUser) {
						f_b_Text.setText("" + value);
						value = getWriteFaderValue(value);
						if (fromUser && setFaderValue(value)) {
							LogUtils.d("write fader success!");
							syncTouchLayoutValue();
						}
						EffectUtils.setCurrentFader(value);
					}

					@Override
					public void onOnSeekBarValueChange(StartPointSeekBar bar,
							int value) {

					}
				});
	}

	@Override
	public void onClick(View v) {
		int resId = v.getId();
		switch (resId) {
		case R.id.bass_add:
			// setAndSaveProgress(bassSeekbar, true, BASS);
			break;

		case R.id.bass_decre:
			// setAndSaveProgress(bassSeekbar, false, BASS);
			break;

		case R.id.mid_add:
			// setAndSaveProgress(midSeekbar, true, MID);
			break;

		case R.id.mid_decre:
			// setAndSaveProgress(midSeekbar, false, MID);
			break;

		case R.id.treble_add:
			// setAndSaveProgress(trebleSeekbar, true, TREBLE);
			break;

		case R.id.treble_decre:
			// setAndSaveProgress(trebleSeekbar, false, TREBLE);
			break;

		case R.id.r_l_equal_decre:
			// setSoundEqualizer(l_r_Seekbar, false, L_R_EQUAL);
			break;

		case R.id.r_l_equal_add:
			// setSoundEqualizer(l_r_Seekbar, true, L_R_EQUAL);
			break;

		case R.id.f_b_equal_add:
			// setSoundEqualizer(f_b_Seekbar, true, F_B_EQUAL);
			break;

		case R.id.f_b_equal_decre:
			// setSoundEqualizer(f_b_Seekbar, false, F_B_EQUAL);
			break;

		default:
			break;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {// ��ָ����ʱ������ͣ�ķ�����Ϣ
			LogUtils.d("��ָ����");
			responseButtonEvent(v.getId(), true);
		} else if (event.getAction() == MotionEvent.ACTION_UP) {// ��ָ̧��ʱֹͣ����
			LogUtils.d("��ָ�ɿ�");
			responseButtonEvent(v.getId(), false);
		}
		return false;
	}
	
	//activity data return fragment
	@Override
	public Object function(Object... data) {
		LogUtils.d("get Activity result data: " + Arrays.asList(data));
		int balance = (Integer)data[0];
		int fade = (Integer)data[1];
		l_r_Seekbar.setProgress(balance);
		f_b_Seekbar.setProgress(fade);
		setBalanceValue(getWriteBalanceValue(balance));
		setFaderValue(getWriteFaderValue(fade));
		return null;
	}
	
	private void syncActivityData(int balance, int fade) {
	    Object notify = ObservableManager.newInstance()
	            .notify(FuntionCommon.CURRENT_SOUND_SETTING_RESULT_ACTIVITY, balance, fade);
	}

	/**
	 * ��Ӧ��Ӧ�İ�ť�¼�
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
	 * һֱִ�мӻ���Ĳ���
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
		}, 0, 300, TimeUnit.MILLISECONDS); // ÿ���200ms����Message
	}

	/**
	 * ֹͣ�ӻ���Ĳ���
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
			case R.id.bass_add:
				setAndSaveProgress(bassSeekbar, true, BASS);
				break;

			case R.id.bass_decre:
				setAndSaveProgress(bassSeekbar, false, BASS);
				break;

			case R.id.mid_add:
				setAndSaveProgress(midSeekbar, true, MID);
				break;

			case R.id.mid_decre:
				setAndSaveProgress(midSeekbar, false, MID);
				break;

			case R.id.treble_add:
				setAndSaveProgress(trebleSeekbar, true, TREBLE);
				break;

			case R.id.treble_decre:
				setAndSaveProgress(trebleSeekbar, false, TREBLE);
				break;

			case R.id.r_l_equal_decre:
				setSoundEqualizer(l_r_Seekbar, false, L_R_EQUAL);
				buttonsyncActivityData();
				break;

			case R.id.r_l_equal_add:
				setSoundEqualizer(l_r_Seekbar, true, L_R_EQUAL);
				buttonsyncActivityData();
				break;

			case R.id.f_b_equal_add:
				setSoundEqualizer(f_b_Seekbar, true, F_B_EQUAL);
				buttonsyncActivityData();
				break;

			case R.id.f_b_equal_decre:
				setSoundEqualizer(f_b_Seekbar, false, F_B_EQUAL);
				buttonsyncActivityData();
				break;

			default:
				break;
			}
		}
	};

	/**
	 * ���е����ĵ���
	 * 
	 * @param seekBar
	 * @param isAdd
	 *            �ӻ��Ǽ�
	 * @param whichValue
	 *            �����������ǵ���
	 * @param isInit
	 *            �Ƿ��ǳ�ʼ��������ǳ�ʼ���Ͳ��ý��б�����
	 */
	private void setAndSaveProgress(SeekBar seekBar, boolean isAdd,
			int whichValue) {
//		changeEffectToCustomer();
		int progress = seekBar.getProgress();
		int value = 0;
		if (isAdd) {
			value = (progress + 1 > seekBar.getMax()) ? seekBar.getMax()
					: progress + 1;
			seekBar.setProgress(value);
		} else {
			value = (progress - 1 < 0) ? 0 : progress - 1;
			seekBar.setProgress(value);
		}
		manaulSaveEffect(seekBar, value);

		// ��ʾ�ڽ����ϵ�ֵ
		value -= 7;
		if (whichValue == BASS)
			bassText.setText("" + value);
		else if (whichValue == MID)
			midText.setText("" + value);
		else
			trebleText.setText("" + value);
	}

	/**
	 * �ֶ������Ч������ťʱ����ֵ
	 * 
	 * @param whichSeekBar
	 */
	private void manaulSaveEffect(SeekBar whichSeekBar, int progress) {
		int seekBarId = whichSeekBar.getId();
		switch (seekBarId) {
		case R.id.bass_seekbar:
			EffectUtils.setBassValue(getWriteBassValue(progress));
			EffectUtils.setCurrentBass(getWriteBassValue(progress));
			break;
		case R.id.mid_seekbar:
			EffectUtils.setMidValue(getWriteBassValue(progress));
			EffectUtils.setCurrentMid(getWriteBassValue(progress));
			break;
		case R.id.treble_seekbar:
			EffectUtils.setTrebleValue(getWriteBassValue(progress));
			EffectUtils.setCurrentTreble(getWriteBassValue(progress));
			break;
		default:
			break;
		}
	}

	// ƽ����ֶ�����
	private void setSoundEqualizer(StartPointSeekBar seekBar, boolean isAdd,
			int whichValue) {
		int progress = seekBar.getProcess();
		int value = 0;

		if (isAdd) {
			value = (progress + 1 > seekBar.getSeekBarMaxValue()) ? seekBar
					.getSeekBarMaxValue() : progress + 1;
			seekBar.setProgress(value);
			LogUtils.d("seekbar progress: " + seekBar.getProcess());
		} else {
			value = (progress - 1 < seekBar.getSeekBarMinValue()) ? seekBar
					.getSeekBarMinValue() : progress - 1;
			seekBar.setProgress(value);
		}

		if (whichValue == L_R_EQUAL)
			l_r_Text.setText("" + value);
		else
			f_b_Text.setText("" + value);

		if (seekBar.getId() == l_r_Seekbar.getId()) {
			value = getWriteBalanceValue(value);
			LogUtils.d("set balace value: " + value);
			setBalanceValue(value);
		} else {
			value = getWriteFaderValue(value);
			LogUtils.d("set fader value: " + value);
			setFaderValue(value);
		}
	}

	/**
	 * ��seekbar��ֵת��ɿ���д���ļ���ֵ
	 */
	private int getWriteBassValue(int value) {
		return 2 * (value - 7);
	}

	/**
	 * ���ڵ��ֵת��ɿ�����ʾ��seekbar��ֵ
	 */
	private int getDisplaySeekbarBassValue(String value) {
		return Integer.parseInt(value) / 2 + EQ_MAX / 2;
	}

	/**
	 * ���ڵ��ֵת��ɿ�����ʾ�������ϵ�ֵ
	 */
	private int getDisplayTextviewBassValue(String value) {
		return Integer.parseInt(value) / 2;
	}

	/**
	 * ���ڵ���дƽ���ֵ
	 */
	private boolean setBalanceValue(int value) {
		// if (isFirstBoot("balance"))
		// alreadyPassFirstBoot("balance");
		return EffectUtils.setBalanceValue(value);
	}

	/**
	 * ���ڵ���дfader��ֵ
	 */
	private boolean setFaderValue(int value) {
		// if (isFirstBoot("fader"))
		// alreadyPassFirstBoot("fader");
		return EffectUtils.setFaderValue(value);
	}

	/**
	 * 
	 * @param fileValue
	 *            ��Ч�ڵ����õ���ֵ
	 * @return ��ʾ��seekbar�������ϵ�ֵ
	 */
	private int getDisplayBalanceValue(String fileValue) {
		if (TextUtils.isEmpty(fileValue))
			return 0;
		return Integer.parseInt(fileValue) / 2;
	}

	/**
	 * 
	 * @param fileValue
	 *            ��Ч�ڵ����õ���ֵ
	 * @return ��ʾ��seekbar�������ϵ�ֵ
	 */
	private int getDisplayFaderValue(String fileValue) {
		if (TextUtils.isEmpty(fileValue))
			return 0;
		return Integer.parseInt(fileValue) / 4;
	}

	/**
	 * ��seekbar��ֵת��ɿ���д���ļ���ֵ
	 * 
	 * @param seekbarValue
	 * @return
	 */
	private int getWriteBalanceValue(int seekbarValue) {
		return seekbarValue * 2;
	}

	/**
	 * ��seekbar��ֵת��ɿ���д���ļ���ֵ
	 * 
	 * @param seekbarValue
	 * @return
	 */
	private int getWriteFaderValue(int seekbarValue) {
		return 4 * seekbarValue;
	}

	/**
	 * ���ı���е�����ֵ����Чģʽ���Ϊ�Զ���
	 */
//	private void changeEffectToCustomer() {
//		EffectUtils.putCarSettingsString(getActivity().getContentResolver(),
//				EffectUtils.EQUALIZER_MODE, EffectUtils.CUSTOMER);
//		EffectUtils.setCurrentEqAnother(EffectUtils.CUSTOMER);
//		EffectUtils.setEqAnother(EffectUtils.CUSTOMER);
//	}

	/**
	 * �����˳�ʱ���浱ǰ��Чֵ
	 */
	private void saveCurrentEffectValueAfterExit() {
		LogUtils.d("save bass: " + bassSeekbar.getProgress() + ", save mid: "
				+ midSeekbar.getProgress() + ", save treble: "
				+ trebleSeekbar.getProgress() + ", save balance: "
				+ getWriteBalanceValue(l_r_Seekbar.getProcess())
				+ ", save fader: "
				+ getWriteFaderValue(f_b_Seekbar.getProcess()));
		EffectUtils
				.setCurrentBass(getWriteBassValue(bassSeekbar.getProgress()));
		EffectUtils.setCurrentMid(getWriteBassValue(midSeekbar.getProgress()));
		EffectUtils.setCurrentTreble(getWriteBassValue(trebleSeekbar
				.getProgress()));
		EffectUtils.setCurrentBalance(getWriteBalanceValue(l_r_Seekbar
				.getProcess()));
		EffectUtils
				.setCurrentFader(getWriteFaderValue(f_b_Seekbar.getProcess()));
	}
	
	/**
	 * ͬ���ұߵ�UI
	 */
	private void syncTouchLayoutValue() {
		int balance = l_r_Seekbar.getProcess();
		int fade = f_b_Seekbar.getProcess();
//		Log.d("ljwtestfuntion", "get sb: " + balance + ", " + fade);
		
		syncActivityData(balance, fade);
		
//		resyncTouchUIValueLater();
	}

	
	//delay 200ms later sync touchmode UI
	private void resyncTouchUIValueLater() {
		if(reSyncTouchUIHandler.hasMessages(TOUCH_MODE))
			reSyncTouchUIHandler.removeMessages(TOUCH_MODE);
		reSyncTouchUIHandler.sendEmptyMessageDelayed(TOUCH_MODE, TOUCH_MODE_DELAY);
	}
	
	private static final int BUTTON_MODE = 0x94;
	private static final int BUTTON_LIMIT_DELAY = 150;
	
	
	private void buttonsyncActivityData() {
		if(touchLimitHandler.hasMessages(BUTTON_MODE))
			touchLimitHandler.removeMessages(BUTTON_MODE);
		touchLimitHandler.sendMessageDelayed(Message.obtain(touchLimitHandler,
				BUTTON_MODE), BUTTON_LIMIT_DELAY);
	}
	
	private Handler touchLimitHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			LogUtils.d("delay buttonsyncActivityData");
			syncTouchLayoutValue();
		}
		
	};
	
	private static final int TOUCH_MODE = 0x93;
	private static final int TOUCH_MODE_DELAY = 200;
	private Handler reSyncTouchUIHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			syncTouchLayoutValue();
		}
		
	};
	
	// /**
	// * �ж��Ƿ��һ�ν��뿪��
	// */
	// private boolean isFirstBoot(String type) {
	// String s = type.equals("balance") ? "balance" : "fader";
	// SharedPreferences pref = getActivity().getSharedPreferences(
	// "first_boot", Context.MODE_PRIVATE);
	// return pref.getInt(s, 0) == 0;
	// }
	//
	// /**
	// * ��һ�ο�����д��ֵ
	// */
	// private void alreadyPassFirstBoot(String type) {
	// SharedPreferences pref = getActivity().getSharedPreferences(
	// "first_boot", Context.MODE_PRIVATE);
	// pref.edit().putInt(type, 1).apply();
	// }
}
