package com.hwatong.radio.ui;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.hwatong.radio.CustomDialog;
import com.hwatong.radio.Frequence;
import com.hwatong.radio.presenter.BroadcastPresenter;
import com.hwatong.radio.presenter.RadioPresenter;
import com.hwatong.radio.ui.iview.IRadioView;
import com.hwatong.radio.ui.iview.IReceiverView;
import com.hwatong.utils.L;
import com.hwatong.utils.Utils;

/**
 * 收音机
 * 
 * @author dengshun
 * 
 */
public class Radio extends Activity implements OnClickListener,
		OnLongClickListener, IRadioView, IReceiverView {

	private static final String thiz = Radio.class.getSimpleName();

	public static final int MIN_FREQUENCE_FM = 8750;
	public static final int MAX_FREQUENCE_FM = 10800;
	
	public static final float MIN_FREQUENCE_FM_FLOAT = 87.5f;
	public static final float MAX_FREQUENCE_FM_FLOAT = 108.0f;

	public static final int MIN_FREQUENCE_AM = 531;
	public static final int MAX_FREQUENCE_AM = 1629;

	private static final int MSG_DELAYCLICK = 1000;

	private int MSG_UPDATE_CHANNEL = 555;

	private Button mBtnPre;// 上一个有效电台

	private TextView mTextBand;

	private TextView mTextCurfreq;

	private TextView mTextUnit;

	private Button mBtnNext;// 下一个有效电台

	private Button btn_up;

	private SeekBar seekBarFm;

	private SeekBar seekBarAm;

	private View sbBg;

	private Button btn_down;

	// ---预设按钮，长按可存台
	private TextView mBtnCollect1;

	private TextView mBtnCollect2;

	private TextView mBtnCollect3;

	private TextView mBtnCollect4;

	private TextView mBtnCollect5;

	private TextView mBtnCollect6;
	// ---预设按钮

	private TextView[] bottomTvs = new TextView[6];
	private TextView[] topTvs = new TextView[6];

	// ---长按预设存台的按钮
	private TextView mBtnCollect01;

	private TextView mBtnCollect02;

	private TextView mBtnCollect03;

	private TextView mBtnCollect04;

	private TextView mBtnCollect05;

	private TextView mBtnCollect06;
	// ---长按预设存台的按钮

	// ---底部button按钮
	private ImageButton mBtnBack;

	private LinearLayout mBtnBandSwitch;

	private LinearLayout mBtnRadioUpdate;

	private LinearLayout mBtnRadioPreview;

	private FrameLayout mSeekbarBg;

	// 右侧列表
	private TextView mTvNoChannel;

	private ListView mLvChannelList;

	private RadioListAdapter mRadioAdapter;

	private Rect imgBounds = new Rect(15, 0, 40, 20);

	private Toast mCollectToast;
	private CustomDialog dialog;
	
	private int[] stringIds = new int[] { R.string.collection1,
			R.string.collection2, R.string.collection3, R.string.collection4,
			R.string.collection5, R.string.collection6, R.string.collection7,
			R.string.collection8, R.string.collection9, R.string.collection10,
			R.string.collection11, R.string.collection12,
			R.string.collection13, R.string.collection14,
			R.string.collection15, R.string.collection16,
			R.string.collection17, R.string.collection18 };

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			/**
			 * 点击事件
			 */
			case MSG_DELAYCLICK:
				
				if (msg.arg1 != R.id.btn_radio_preview) {
					radioPresenter.stopPreview();
				}
				switch (msg.arg1) {
				case R.id.btn_pre:
					hideLoading();
					radioPresenter.seek(false);
					break;
				case R.id.btn_next:
					hideLoading();
					radioPresenter.seek(true);
					break;
				case R.id.btn_down:
					hideLoading();
					radioPresenter.tune(false);
					break;
				case R.id.btn_up:
					hideLoading();
					radioPresenter.tune(true);
					break;
				case R.id.btn_back:
					hideLoading();
					radioPresenter.stopScan();
					finish();
					break;
				case R.id.btn_band_switch:
					radioPresenter.band();
					break;
				case R.id.btn_radio_update:
					hideLoading();
					radioPresenter.scan();
					break;
				case R.id.btn_radio_preview:
					hideLoading();
					radioPresenter.previewChannels();
					break;
				case R.id.btn_collect1:
				case R.id.btn_collect2:
				case R.id.btn_collect3:
				case R.id.btn_collect4:
				case R.id.btn_collect5:
				case R.id.btn_collect6:
					hideLoading();
					L.d(thiz, "" + (Integer) ((View) msg.obj).getTag());
					radioPresenter.play((Integer) ((View) msg.obj).getTag());
					break;
				}
				break;
			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		radioPresenter = new RadioPresenter(this);
		broadcastPresenter = new BroadcastPresenter(this);
		
		initView();

		initData();

		startService(new Intent("com.hwatong.radio.service"));

		broadcastPresenter.regVoiceBroadcast(this);
		
		if(getIntent() != null) {
			int type = getIntent().getIntExtra("type", -1);
			radioPresenter.setInitType(type);
			L.d(thiz, "onResume type : " + type + " isFm : " + radioPresenter.isFm());
		}
		
	}
	

	@Override
	protected void onResume() {
		super.onResume();
		radioPresenter.bindService(this);
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		int type = intent.getIntExtra("type", -1);
		radioPresenter.setInitType(type);
		L.d(thiz, "onNewIntent type : "+ type + " isFm : " + radioPresenter.isFm());
	}

	@Override
	protected void onPause() {
		super.onPause();
		radioPresenter.unbindService(this);
	}
	
	@Override
	protected void onDestroy() {
		broadcastPresenter.unregVoiceBroadcast(this);
		super.onDestroy();
	}

	private void initView() {
		mBtnPre = (Button) findViewById(R.id.btn_pre);
		mBtnPre.setOnClickListener(this);

		mTextBand = (TextView) findViewById(R.id.text_fmam);

		mTextCurfreq = (TextView) findViewById(R.id.text_curfreq);

		mTextUnit = (TextView) findViewById(R.id.text_mhz);

		mBtnNext = (Button) findViewById(R.id.btn_next);
		mBtnNext.setOnClickListener(this);

		btn_up = (Button) findViewById(R.id.btn_up);
		btn_up.setOnClickListener(this);

		seekBarFm = (SeekBar) findViewById(R.id.seekbar_fm);

		seekBarAm = (SeekBar) findViewById(R.id.seekbar_am);

		sbBg = findViewById(R.id.sb_bg);

		btn_down = (Button) findViewById(R.id.btn_down);
		btn_down.setOnClickListener(this);

		mBtnCollect1 = (TextView) findViewById(R.id.btn_collect1);
		mBtnCollect1.setOnClickListener(this);
		mBtnCollect1.setOnLongClickListener(this);

		mBtnCollect2 = (TextView) findViewById(R.id.btn_collect2);
		mBtnCollect2.setOnClickListener(this);
		mBtnCollect2.setOnLongClickListener(this);

		mBtnCollect3 = (TextView) findViewById(R.id.btn_collect3);
		mBtnCollect3.setOnClickListener(this);
		mBtnCollect3.setOnLongClickListener(this);

		mBtnCollect4 = (TextView) findViewById(R.id.btn_collect4);
		mBtnCollect4.setOnClickListener(this);
		mBtnCollect4.setOnLongClickListener(this);

		mBtnCollect5 = (TextView) findViewById(R.id.btn_collect5);
		mBtnCollect5.setOnClickListener(this);
		mBtnCollect5.setOnLongClickListener(this);

		mBtnCollect6 = (TextView) findViewById(R.id.btn_collect6);
		mBtnCollect6.setOnClickListener(this);
		mBtnCollect6.setOnLongClickListener(this);

		// ---长按预设存台的按钮
		mBtnCollect01 = (TextView) findViewById(R.id.btn_collect01);
		mBtnCollect01.setOnLongClickListener(this);

		mBtnCollect02 = (TextView) findViewById(R.id.btn_collect02);
		mBtnCollect02.setOnLongClickListener(this);

		mBtnCollect03 = (TextView) findViewById(R.id.btn_collect03);
		mBtnCollect03.setOnLongClickListener(this);

		mBtnCollect04 = (TextView) findViewById(R.id.btn_collect04);
		mBtnCollect04.setOnLongClickListener(this);

		mBtnCollect05 = (TextView) findViewById(R.id.btn_collect05);
		mBtnCollect05.setOnLongClickListener(this);

		mBtnCollect06 = (TextView) findViewById(R.id.btn_collect06);
		mBtnCollect06.setOnLongClickListener(this);

		// ---底部button按钮
		mBtnBack = (ImageButton) findViewById(R.id.btn_back);
		mBtnBack.setOnClickListener(this);

		mBtnBandSwitch = (LinearLayout) findViewById(R.id.btn_band_switch);
		mBtnBandSwitch.setOnClickListener(this);

		mBtnRadioUpdate = (LinearLayout) findViewById(R.id.btn_radio_update);
		mBtnRadioUpdate.setOnClickListener(this);

		mBtnRadioPreview = (LinearLayout) findViewById(R.id.btn_radio_preview);
		mBtnRadioPreview.setOnClickListener(this);

		mSeekbarBg = (FrameLayout) findViewById(R.id.seekbar_layout);

		// 右侧列表
		mTvNoChannel = (TextView) findViewById(R.id.tv_nochannel);

		mLvChannelList = (ListView) findViewById(R.id.lv_radio_list);

		dialog = new CustomDialog(this, null);

		initSeekBar();

		initListView();

		initCollectionToast();

		textViewToArray();

	}

	private void initData() {
		radioPresenter.initSharedPreferences(this);
	}

	private Handler seekbarHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == MSG_UPDATE_CHANNEL) {
				radioPresenter.stopPreview();
				radioPresenter.play(msg.arg1);
			}
		};
	};

	private void initSeekBar() {
		seekBarFm.setMax((MAX_FREQUENCE_FM - MIN_FREQUENCE_FM) / 10);
		seekBarAm.setMax((MAX_FREQUENCE_AM - MIN_FREQUENCE_AM) / 9);

		seekBarAm.setVisibility(radioPresenter.isFm() ? View.INVISIBLE
				: View.VISIBLE);
		seekBarFm.setVisibility(radioPresenter.isFm() ? View.VISIBLE
				: View.INVISIBLE);

		sbBg.setBackgroundResource(radioPresenter.isFm() ? R.drawable.bg_seekbarbg_radio_fm2
				: R.drawable.bg_seekbarbg_radio_am2);

		seekBarFm.setOnSeekBarChangeListener(onSeekBarChangeListener);

		seekBarAm.setOnSeekBarChangeListener(onSeekBarChangeListener);
	}

	/**
	 * SeekBar监听
	 */
	private OnSeekBarChangeListener onSeekBarChangeListener = new OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			switch (seekBar.getId()) {
			case R.id.seekbar_fm:
				if (fromUser) {
					seekbarHandler.removeMessages(MSG_UPDATE_CHANNEL);
					seekbarHandler.sendMessageDelayed(seekbarHandler
							.obtainMessage(MSG_UPDATE_CHANNEL, progress * 10
									+ MIN_FREQUENCE_FM, 0), 100);
				}
				break;
			case R.id.seekbar_am:
				if (fromUser) {
					seekbarHandler.removeMessages(MSG_UPDATE_CHANNEL);
					seekbarHandler.sendMessageDelayed(seekbarHandler
							.obtainMessage(MSG_UPDATE_CHANNEL, progress * 9
									+ MIN_FREQUENCE_AM, 0), 100);
				}
				break;
			default:
				break;
			}
		}
	};

	private void initListView() {

		mLvChannelList.setEmptyView(mTvNoChannel);

		mRadioAdapter = new RadioListAdapter(new ArrayList<Frequence>());
		mLvChannelList.setAdapter(mRadioAdapter);
		mLvChannelList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				radioPresenter.play(mRadioAdapter.getItem(position).frequence);
			}
		});
	}

	private void initCollectionToast() {
		LayoutInflater inflater = getLayoutInflater();
		View layout = inflater.inflate(R.layout.toast_collection_success,
				(ViewGroup) findViewById(R.id.collection_success));
		mCollectToast = new Toast(getBaseContext());
		mCollectToast.setGravity(Gravity.LEFT | Gravity.TOP, 0, 0);
		mCollectToast.setDuration(3000);
		mCollectToast.setView(layout);

	}

	private void textViewToArray() {
		// 底层
		bottomTvs[0] = mBtnCollect1;
		bottomTvs[1] = mBtnCollect2;
		bottomTvs[2] = mBtnCollect3;
		bottomTvs[3] = mBtnCollect4;
		bottomTvs[4] = mBtnCollect5;
		bottomTvs[5] = mBtnCollect6;
		// 上层
		topTvs[0] = mBtnCollect01;
		topTvs[1] = mBtnCollect02;
		topTvs[2] = mBtnCollect03;
		topTvs[3] = mBtnCollect04;
		topTvs[4] = mBtnCollect05;
		topTvs[5] = mBtnCollect06;
	}

	@Override
	public boolean onLongClick(View v) {
		doLongClick(v);
		return true;
	}
	
	public void doLongClick(View v) {
		hideLoading();
		int band = radioPresenter.getCurrentBand();
		switch (v.getId()) {
		case R.id.btn_collect1:
		case R.id.btn_collect01:
			collectChannel(mBtnCollect1, mBtnCollect01, (band - 1) * 6 + 0);
			break;
		case R.id.btn_collect2:
		case R.id.btn_collect02:
			collectChannel(mBtnCollect2, mBtnCollect02, (band - 1) * 6 + 1);
			break;
		case R.id.btn_collect3:
		case R.id.btn_collect03:
			collectChannel(mBtnCollect3, mBtnCollect03, (band - 1) * 6 + 2);
			break;
		case R.id.btn_collect4:
		case R.id.btn_collect04:
			collectChannel(mBtnCollect4, mBtnCollect04, (band - 1) * 6 + 3);
			break;
		case R.id.btn_collect5:
		case R.id.btn_collect05:
			collectChannel(mBtnCollect5, mBtnCollect05, (band - 1) * 6 + 4);
			break;
		case R.id.btn_collect6:
		case R.id.btn_collect06:
			collectChannel(mBtnCollect6, mBtnCollect06, (band - 1) * 6 + 5);
			break;
		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		// 防止快速点击，所以采用Handler事件
		mHandler.removeMessages(MSG_DELAYCLICK);
		Message msg = mHandler.obtainMessage(MSG_DELAYCLICK, v.getId(), 1, v);
		mHandler.sendMessageDelayed(msg, 200);
	}

	/**
	 * 收藏电台
	 * 
	 * @param textView
	 * @param textView01
	 */
	private void collectChannel(TextView textView, TextView textView01,
			int position) {
//		int collectedPos = radioPresenter.checkIfCollected();
//		if (collectedPos != -1) {
//			Toast.makeText(
//					this,
//					String.format(getString(R.string.already_collected),
//							collectedPos), Toast.LENGTH_SHORT).show();
//			return;
//		}

		textView.setEnabled(true);
		textView01.setVisibility(View.INVISIBLE);

		if (radioPresenter.isFm()) {
			// FM
			radioPresenter.collectFmChannel(textView, position);
			Drawable drawable = getResources().getDrawable(
					R.drawable.select_icon);
			// / 这一步必须要做,否则不会显示.
			drawable.setBounds(imgBounds);
			textView.setCompoundDrawables(drawable, null, null, null);
			textView.setSelected(true);
		} else {
			// AM
			position -= 18;
			radioPresenter.collectAmChannel(textView, position);
			Drawable drawable = getResources().getDrawable(
					R.drawable.select_icon);
			// 这一步必须要做,否则不会显示.
			drawable.setBounds(imgBounds);
			textView.setCompoundDrawables(drawable, null, null, null);
			textView.setSelected(true);
		}
		mCollectToast.show();

		refreshChannelListStatus(radioPresenter.isFm(),
				radioPresenter.getCurrentFreq());
	}

	private void refreshChannelListStatus(boolean fm, int currentFreq) {
		if (fm) {
			for (int i = 0; i < mFreqList.size(); i++) {
				mFreqList.get(i).isCollected = false;
				if (mFreqList.get(i).frequence == radioPresenter
						.getCurrentFreq()) {
					mRadioAdapter.setPlayingChannel(i);
				}
				for (int j = 0; j < 18; j++) {
					if (mFreqList.get(i).frequence == radioPresenter
							.getFmPosFreq(j)) {
						mFreqList.get(i).isCollected = true;
					}
				}
			}
		} else {
			for (int i = 0; i < mFreqList.size(); i++) {
				mFreqList.get(i).isCollected = false;
				if (mFreqList.get(i).frequence == radioPresenter
						.getCurrentFreq()) {
					mRadioAdapter.setPlayingChannel(i);
				}
				for (int j = 0; j < 12; j++) {
					if (mFreqList.get(i).frequence == radioPresenter
							.getAmPosFreq(j)) {
						mFreqList.get(i).isCollected = true;
					}
				}
			}
		}
		mRadioAdapter.notifyDataSetChanged();
		L.d(thiz, "in refreshChannelListStatus");
		refreshListPosition(mRadioAdapter.getPlayingPosition());

	}

	/**
	 * Adapter
	 * 
	 * true open FM false open AM
	 * 
	 * @return boolean
	 */
	private List<Frequence> mFreqList;
	private RadioPresenter radioPresenter;

	private BroadcastPresenter broadcastPresenter;

	private class RadioListAdapter extends BaseAdapter {

		private int mSelectId = -1;

		public RadioListAdapter(List<Frequence> mRadioList) {
			mFreqList = mRadioList;
		}

		public void setmRadioList(List<Frequence> mRadioList) {
			mFreqList = mRadioList;
		}

		public void setPlayingChannel(int selectedIndex) {
			this.mSelectId = selectedIndex;
		}
		
		public int getPlayingPosition() {
			return this.mSelectId;
		}

		@Override
		public int getCount() {
			return mFreqList.size();
		}

		@Override
		public Frequence getItem(int position) {
			return mFreqList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder = null;
			if (convertView == null) {
				viewHolder = new ViewHolder();

				convertView = getLayoutInflater().inflate(
						R.layout.item_radio_list, null, false);
				viewHolder.mIvPlay = (ImageView) convertView
						.findViewById(R.id.iv_icon_play);
				viewHolder.mTvFreq = (TextView) convertView
						.findViewById(R.id.tv_freq);
				viewHolder.mIvCollecStar = (ImageView) convertView
						.findViewById(R.id.iv_collect_star);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			Frequence freq = mFreqList.get(position);

			boolean isPlay = mSelectId == position;

			viewHolder.mIvPlay.setVisibility(isPlay ? View.VISIBLE
					: View.INVISIBLE);

			viewHolder.mTvFreq.setTextColor(getResources().getColor(
					isPlay ? R.color.red : R.color.white));
			if (freq.frequence >= MIN_FREQUENCE_FM) {
				viewHolder.mTvFreq
						.setText(Utils.numberToString(freq.frequence));
			} else {
				viewHolder.mTvFreq.setText(String.valueOf(freq.frequence));
			}

			if (freq.isCollected) {
				viewHolder.mIvCollecStar.setVisibility(View.VISIBLE);
				viewHolder.mIvCollecStar.setSelected(isPlay);
			} else {
				viewHolder.mIvCollecStar.setVisibility(View.INVISIBLE);
			}

			return convertView;
		}

		private class ViewHolder {
			private TextView mTvFreq;
			private ImageView mIvPlay;
			private ImageView mIvCollecStar;
		}
	}

	
	private void refreshListPosition(int position) {
		L.d(thiz, "refreshListPosition position : " + position);
		
		L.d(thiz, "first : " + mLvChannelList.getFirstVisiblePosition() + " last : " + mLvChannelList.getLastVisiblePosition());
		
		if(position < mLvChannelList.getFirstVisiblePosition() || position > mLvChannelList.getLastVisiblePosition()) {
			if(position > 3) {
				mLvChannelList.setSelection(position - 3);
			} else if(position >= 0) {
				mLvChannelList.setSelection(0);
			}
		}
	}
	
	@Override
	public void refreshView(final int band, final int freq,
			ArrayList<Frequence> list) {
		if (radioPresenter.isFm()) { // FM
			// 更新Channel
			refreshChannel(Utils.getBandText(band), "MHz",
					Utils.numberToString(freq));
			// 更新Channel
			refreshSeekbar(true, freq);
		} else { // AM
			// 更新Channel
			refreshChannel(Utils.getBandText(band), "KHz", String.valueOf(freq));
			// 更新Channel
			refreshSeekbar(false, freq);
		}

		// 收藏按钮数据更新
		refreshFavorList(band, freq);
		// 更新频道列表
		refreshChannelList(freq, list);

	}
	
	/**
	 * 增加这个方法，让搜台的时候搜到相对的频率不会收藏电台与列表对应频率不会闪动
	 */
	@Override
	public void refreshView(final int band, final int freq) {
		if (radioPresenter.isFm()) { // FM
			// 更新Channel
			refreshChannel(Utils.getBandText(band), "MHz",
					Utils.numberToString(freq));
			// 更新Channel
			refreshSeekbar(true, freq);
		} else { // AM
			// 更新Channel
			refreshChannel(Utils.getBandText(band), "KHz", String.valueOf(freq));
			// 更新Channel
			refreshSeekbar(false, freq);
		}
		
		// 收藏按钮数据更新
		refreshFavorList(band, -1);
		// 更新频道列表
		refreshChannelList(-1, null);
	}
	
	
	
	

	private void refreshChannel(String band, String unit, String freq) {
		// 更新频道
		mTextBand.setText(band);
		// 更新频率单位
		mTextUnit.setText(unit);
		// 更新当前频率
		mTextCurfreq.setText(freq);
	}

	private void refreshSeekbar(boolean isFm, int freq) {
		// 更新seekBar显示
		seekBarFm.setVisibility(isFm ? View.VISIBLE : View.INVISIBLE);
		seekBarAm.setVisibility(isFm ? View.INVISIBLE : View.VISIBLE);
		// 图片
		sbBg.setBackgroundResource(radioPresenter.isFm() ? R.drawable.bg_seekbarbg_radio_fm2
				: R.drawable.bg_seekbarbg_radio_am2);

		// 更新seekBar进度
		if (isFm) {
			seekBarFm.setProgress((freq - MIN_FREQUENCE_FM) / 10);
		} else {
			seekBarAm.setProgress((freq - MIN_FREQUENCE_AM) / 9);
		}
	}

	// 收藏按钮数据更新
	private void refreshFavorList(int band, int currentFreq) {
		if (band <= 3 && band > 0) {
			// FM
			for (int i = 0; i < 6; i++) {
				int posInSp = (band - 1) * 6 + i;
				if (radioPresenter.getFmPosFreq(posInSp) != 0) {
					bottomTvs[i].setText(Utils.numberToString(radioPresenter
							.getFmPosFreq(posInSp)));
					bottomTvs[i].setTag(radioPresenter.getFmPosFreq(posInSp));
					bottomTvs[i].setEnabled(true);
					topTvs[i].setVisibility(View.INVISIBLE);

					// 收藏按钮是否高亮
					if (radioPresenter.getFmPosFreq(posInSp) == currentFreq) {
						Drawable drawable = getResources().getDrawable(
								R.drawable.select_icon);
						drawable.setBounds(imgBounds);
						bottomTvs[i].setCompoundDrawables(drawable, null, null,
								null);
						bottomTvs[i].setSelected(true);
					} else {
						bottomTvs[i].setCompoundDrawables(null, null, null,
								null);
						bottomTvs[i].setSelected(false);
					}
				} else {
					bottomTvs[i].setEnabled(false);
					bottomTvs[i].setCompoundDrawables(null, null, null, null);
					bottomTvs[i].setSelected(false);
					topTvs[i].setVisibility(View.VISIBLE);
					topTvs[i].setText(stringIds[posInSp]);
				}
			}
		} else if (band > 3 && band <= 5) {
			// AM
			for (int i = 0; i < 6; i++) {
				int posInSp = (band - 4) * 6 + i;
				if (radioPresenter.getAmPosFreq(posInSp) != 0) {
					bottomTvs[i].setText(String.valueOf(radioPresenter
							.getAmPosFreq(posInSp)));
					bottomTvs[i].setTag(radioPresenter.getAmPosFreq(posInSp));
					bottomTvs[i].setEnabled(true);
					topTvs[i].setVisibility(View.INVISIBLE);
					// 按钮是否高亮
					if (radioPresenter.isCurrentAmFreq(posInSp)) {
						Drawable drawable = getResources().getDrawable(
								R.drawable.select_icon);
						drawable.setBounds(imgBounds);
						bottomTvs[i].setCompoundDrawables(drawable, null, null,
								null);
						bottomTvs[i].setSelected(true);
					} else {
						bottomTvs[i].setCompoundDrawables(null, null, null,
								null);
						bottomTvs[i].setSelected(false);
					}

				} else {
					bottomTvs[i].setEnabled(false);
					bottomTvs[i].setCompoundDrawables(null, null, null, null);
					bottomTvs[i].setSelected(false);
					topTvs[i].setVisibility(View.VISIBLE);
					topTvs[i].setText(stringIds[posInSp]);
				}
			}
		}
	}

	@Override
	public void refreshChannelList(int freq, List<Frequence> list) {
		// 更新频道列表
		if (list != null) {
			mFreqList.clear();
			mFreqList.addAll(list);
		}
		int indexOf = mFreqList.indexOf(new Frequence(freq));
		if (indexOf >= 0) {
			mRadioAdapter.setPlayingChannel(indexOf);
		} else {
			mRadioAdapter.setPlayingChannel(-1);
		}
		mRadioAdapter.notifyDataSetChanged();
		
		L.d(thiz, "in refreshChannelList");
		refreshListPosition(mRadioAdapter.getPlayingPosition());
	}

	@Override
	public void showLoading() {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				dialog.show();
			}
		});
	}

	@Override
	public void hideLoading() {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if (dialog.isShowing()) {
					dialog.dismiss();
				}
			}
		});
	}

	@Override
	public void showFirstScan() {
//		Toast.makeText(this, getString(R.string.first_scan), Toast.LENGTH_LONG)
//				.show();
	}

	@Override
	public void showPreview() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mBtnRadioPreview.setSelected(true);
			}
		});
	}

	@Override
	public void hidePreview() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mBtnRadioPreview.setSelected(false);
			}
		});
	}

	@Override
	public void close() {
		finish();
	}

	@Override
	public void playChannel(int freq) {
		radioPresenter.play(freq);
	}

	@Override
	public void collect() {
		int emptyPosition = radioPresenter.getEmptyPosition();
		//必须先sync后再收藏，保证band正确
		
		radioPresenter.syncBand(emptyPosition);
		
		doLongClick(bottomTvs[emptyPosition % 6]);
	}

	@Override
	public void playPosition(int pos) {
		if(pos < 1 || pos > 18) {
			return ;
		}
		if(!radioPresenter.isFm() && pos > 12) {
			return;
		}
		radioPresenter.playPosition(pos);
	}

}
