package com.hwatong.btphone.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Handler;
import android.os.SystemClock;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hwatong.btphone.CallLog;
import com.hwatong.btphone.activity.base.BaseActivity;
import com.hwatong.btphone.adapter.CallLogListAdapter;
import com.hwatong.btphone.app.BtPhoneApplication;
import com.hwatong.btphone.bean.UICallLog;
import com.hwatong.btphone.constants.Constant;
import com.hwatong.btphone.constants.PhoneState;
import com.hwatong.btphone.ui.DrawableTextView;
import com.hwatong.btphone.ui.KeyBoardCharBuilder;
import com.hwatong.btphone.ui.NoDoubleItemClickListener;
import com.hwatong.btphone.ui.R;
import com.hwatong.btphone.util.DailDTMF;
import com.hwatong.btphone.util.L;

/**
 * 通话界面
 * 
 * 进入拨号界面的五个通道 1、在主界面点击进入拨号界面 2、在蓝牙电话中，非拨号界面来电，点击进入拨号界面 3、在非蓝牙电话中来电，接听进入拨号界面
 * 4、在通讯录界面，点击拨号 5、在通话记录界面点击拨号
 * 
 * 界面上UI的状态显示根据{@link PhoneState}来进行改变，主要是NORMAL和其他状态有所区别
 * 
 * @author zxy zjb time:2017年5月25日
 * 
 */
public class DialActivity extends BaseActivity {
	private static final String thiz = DialActivity.class.getSimpleName();

	private static final int MSG_UPDATE_TALKING_TIME = 0x10000000;

	// -----通话面板
	private View mDialPeopleView;// 总布局，显示正在通话人的信息,便于隐藏

	private ImageView mIvPeopleIcon;

	private TextView mTvName;

	private TextView mTvtalkNumber;// 正在通话的电话号码

	private TextView mTvCallState;

//	private TextView mTvInputNumber;// 显示点击按键输入的号码
	private EditText mTvInputNumber;// 显示点击按键输入的号码

	private DrawableTextView mBtnCall;// 拨号按键

	private DrawableTextView mBtnHandUp;// 挂断电话按键

	private TextView mTvCallOver;
	// -----通话面板

	// ------键盘
	private TextView mIvKey1;

	private TextView mIvKey2;

	private TextView mIvKey3;

	private TextView mIvKey4;

	private TextView mIvKey5;

	private TextView mIvKey6;

	private TextView mIvKey7;

	private TextView mIvKey8;

	private TextView mIvKey9;

	private TextView mIvKeyStar;

	private TextView mIvKeyPound;

	private TextView mIvKey0;

	private ImageButton mIvDelete;

	private DrawableTextView mIvMute;

	private DrawableTextView mIvSwitchChnanel;
	// ----键盘

	// ----底部图标
	private ImageView mIvReturn;

	private View mBtnGotoContacts, mDtvGotoContacts;

	private View mBtnGotoCallLog, mDtvGotoCallLog;
	// ----底部图标

	private ListView mLvCallLog;

	private TextView tvNoData;

	private PhoneState mCurPhoneState;

	private CharSequence currentNumber;

	private KeyBoardCharBuilder mKeyBoardCb = new KeyBoardCharBuilder();

	private CallLogListAdapter mCallAdapter;

	private boolean mCallOverExit;// 通话结束是否离开该界面

	private int key_plus = 123;

	private static final String TIME_FORMAT = "%02d:%02d";

	private DailDTMF dtmf;
	
	private int from = 0;		//1表示通话记录， 2表示通讯录
	
	private long startTime = 0;			//增加一个变量，使得拨打按钮不能使劲狂按，解决这次拨打显示的是上次的号码问题
	
	/**
	 * 保存起来，用于匹配号码对应的联系人名字
	 */
	private List<CallLog> allLogs = new ArrayList<CallLog>();
	
	
	private boolean fromOutside = false;
	
	private boolean fromVoice = false;
	
	private boolean voiceFromOutside = false;
	
	private static final  int FROM_VOICE_TO_FALSE = 1;
	
	
	/**
	 * 防止跳转后回到拨号界面，showIdel的时候退出界面
	 */
	private boolean everJump = false;
	
	private Handler myHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case FROM_VOICE_TO_FALSE:
				fromVoice = false;
				break;
			default:
				break;
			}
		};
	};
	
	

	@Override
	protected void initView() {
		// -----通话面板
		mDialPeopleView = findViewById(R.id.ll_dial_people_info);

		mIvPeopleIcon = (ImageView) findViewById(R.id.iv_people_icon);

		mTvName = (TextView) findViewById(R.id.tv_name);

		mTvtalkNumber = (TextView) findViewById(R.id.tv_talking_number);

		mTvCallState = (TextView) findViewById(R.id.tv_call_state);

		mTvInputNumber = (EditText) findViewById(R.id.tv_input_number);
		mTvInputNumber.setInputType(InputType.TYPE_NULL);

		mBtnCall = (DrawableTextView) findViewById(R.id.dtv_call);
		mBtnCall.setOnClickListener(this);

		mBtnHandUp = (DrawableTextView) findViewById(R.id.dtv_hand_up);
		mBtnHandUp.setOnClickListener(this);

		mTvCallOver = (TextView) findViewById(R.id.tv_call_over);

		mIvKey1 = (TextView) findViewById(R.id.key_1);
		mIvKey1.setOnClickListener(this);
		mIvKey1.setSoundEffectsEnabled(false);

		mIvKey2 = (TextView) findViewById(R.id.key_2);
		mIvKey2.setOnClickListener(this);
		mIvKey2.setSoundEffectsEnabled(false);

		mIvKey3 = (TextView) findViewById(R.id.key_3);
		mIvKey3.setOnClickListener(this);
		mIvKey3.setSoundEffectsEnabled(false);

		mIvKey4 = (TextView) findViewById(R.id.key_4);
		mIvKey4.setOnClickListener(this);
		mIvKey4.setSoundEffectsEnabled(false);

		mIvKey5 = (TextView) findViewById(R.id.key_5);
		mIvKey5.setOnClickListener(this);
		mIvKey5.setSoundEffectsEnabled(false);

		mIvKey6 = (TextView) findViewById(R.id.key_6);
		mIvKey6.setOnClickListener(this);
		mIvKey6.setSoundEffectsEnabled(false);

		mIvKey7 = (TextView) findViewById(R.id.key_7);
		mIvKey7.setOnClickListener(this);
		mIvKey7.setSoundEffectsEnabled(false);

		mIvKey8 = (TextView) findViewById(R.id.key_8);
		mIvKey8.setOnClickListener(this);
		mIvKey8.setSoundEffectsEnabled(false);

		mIvKey9 = (TextView) findViewById(R.id.key_9);
		mIvKey9.setOnClickListener(this);
		mIvKey9.setSoundEffectsEnabled(false);

		mIvKeyStar = (TextView) findViewById(R.id.key_star);
		mIvKeyStar.setOnClickListener(this);
		mIvKeyStar.setSoundEffectsEnabled(false);

		mIvKeyPound = (TextView) findViewById(R.id.key_pound);
		mIvKeyPound.setOnClickListener(this);
		mIvKeyPound.setSoundEffectsEnabled(false);

		mIvKey0 = (TextView) findViewById(R.id.key_0);
		mIvKey0.setOnClickListener(this);
		mIvKey0.setOnLongClickListener(this);
		mIvKey0.setSoundEffectsEnabled(false);

		mIvDelete = (ImageButton) findViewById(R.id.key_delete);
		mIvDelete.setOnClickListener(this);
		mIvDelete.setOnLongClickListener(this);
		

		mIvMute = (DrawableTextView) findViewById(R.id.key_mute);
		mIvMute.setOnClickListener(this);

		mIvSwitchChnanel = (DrawableTextView) findViewById(R.id.key_switch_channel);
		mIvSwitchChnanel.setOnClickListener(this);

		mIvReturn = (ImageView) findViewById(R.id.iv_return);
		mIvReturn.setOnClickListener(this);

		mBtnGotoContacts = findViewById(R.id.btn_goto_contacts);
		mDtvGotoContacts = findViewById(R.id.dtv_goto_contacts);
		mBtnGotoContacts.setOnClickListener(this);

		mBtnGotoCallLog = findViewById(R.id.btn_goto_call_log);
		mDtvGotoCallLog = findViewById(R.id.dtv_goto_call_log);
		mBtnGotoCallLog.setOnClickListener(this);

		mLvCallLog = (ListView) findViewById(R.id.lv_call_log_list);

		tvNoData = (TextView) findViewById(R.id.tv_nodata);
    
		checkIntent(getIntent());

		mKeyBoardCb.initData(new int[] { R.id.key_0, R.id.key_1, R.id.key_2, R.id.key_3,
						R.id.key_4, R.id.key_5, R.id.key_6, R.id.key_7,
						R.id.key_8, R.id.key_9, R.id.key_star, R.id.key_pound,
						key_plus }, new char[] { '0', '1', '2', '3', '4', '5',
						'6', '7', '8', '9', '*', '#', '+' });
		mKeyBoardCb.bindTextView(mTvInputNumber);
		
		dtmf = new DailDTMF(this);
		
		//setVolumeControlStream(AudioManager.STREAM_RING);
		
		initListView();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		L.d(thiz, "onNewIntent!");
		checkIntent(intent);
	}
	
	private void checkIntent(final Intent intent) {
		if (intent != null) {
			UICallLog log = intent.getParcelableExtra("call_log");
			from = intent.getIntExtra("from", 0);
			fromOutside = intent.getBooleanExtra("from_out_side", false);
			if(log != null) {
				mCallOverExit = true;
				switch (log.type) {
				case UICallLog.TYPE_CALL_IN:
					showComing(log);
					break;
				case UICallLog.TYPE_CALL_OUT:
					showCalling(log);
					break;
				default:
					break;
				}
			} else if(intent.getBooleanExtra("from_voice", false)) {
				fromVoice = true;
				from = intent.getIntExtra("from", 0);
				fromOutside = intent.getBooleanExtra("voice_from_out_side", false);
				mCallOverExit = true;
			}
			
			L.d(thiz, "from=" + from + " fromOutside=" + fromOutside + " fromVoice=" + fromVoice + " callOverExit=" + mCallOverExit);
			
		}
	}
	

	private void initListView() {
		mCallAdapter = new CallLogListAdapter(this, R.layout.item_cantacts,new ArrayList<CallLog>(0));
		mLvCallLog.setAdapter(mCallAdapter);

		mLvCallLog.setEmptyView(tvNoData);

		mLvCallLog.setOnItemClickListener(new NoDoubleItemClickListener() {

			@Override
			public void onItemClickImp(AdapterView<?> parent, View view, int position, long id) {
				if (mCurPhoneState == PhoneState.IDEL) {
					mCallOverExit = false;
					CallLog callLog = (CallLog) parent.getItemAtPosition(position);
					dial(callLog.number);
				}
			}
		});
	}

	@Override
	protected void serviceConnected() {
		
	}
	
	
	/**
	 * 更新状态和UI,PhoneState状态统一在此方法中更新
	 * @param state
	 */
	private void onStateChange(PhoneState state) {
		if (mCurPhoneState != state) {
			mCurPhoneState = state;
			updateDialPanel();
		}
	}

	/**
	 * 根据{@link PhoneState} 状态变化，对view进行显示、隐藏、禁用
	 */
	private void updateDialPanel() {
		
		L.d(thiz, "updateDialPanel state : " + mCurPhoneState);
		
		// 若是未通话，或者通话过程中输入了按键，则头像需要去掉，显示输入数字
		boolean isIdel = mCurPhoneState == PhoneState.IDEL;
		// 头像，人名，号码，时间
		mDialPeopleView.setVisibility(isIdel ? View.GONE : View.VISIBLE);
		// 电话号码文本
		mTvInputNumber.setVisibility(isIdel ? View.VISIBLE : View.GONE);

		isIdel = mCurPhoneState == PhoneState.IDEL;

		// 挂断
		mBtnHandUp.setVisibility(isIdel ? View.GONE : View.VISIBLE);

		boolean showBtnCall = mCurPhoneState == PhoneState.IDEL
				| mCurPhoneState == PhoneState.INCOMING;
		// 通话按钮
		mBtnCall.setVisibility(showBtnCall ? View.VISIBLE : View.GONE);

		// 麦克风静音与声道切换
		mIvMute.setEnabled(!isIdel);
		mIvSwitchChnanel.setEnabled(!isIdel);

		// 状态文本
		if (mCurPhoneState == PhoneState.OUTGOING) {
			mTvCallState.setText(R.string.call_outing);
		} else if (mCurPhoneState == PhoneState.INCOMING) {
			mTvCallState.setText(R.string.call_in);
		} else if (mCurPhoneState == PhoneState.TALKING) {
			
		}

		mIvReturn.setEnabled(isIdel);
		mIvReturn.setImageResource(isIdel ? R.drawable.btn_return : R.drawable.btn_return_gray);
	}

	/**
	 * 显示正在通话的时长
	 */
	private void showTalkingTime(long duration) {
		long time = duration / 1000;

		long minute = time / 60;
		long second = time % 60;

		mTvCallState.setText(String.format(TIME_FORMAT, minute, second));
	}

	private void dial(String number) {
		if (!TextUtils.isEmpty(number) && mService != null) {
			mService.dial(number);
		}
	}

	
	
	/**
	 * 点击通话按键
	 */
	private void clickCall() {
		if(System.currentTimeMillis() - startTime > 1000) {
			if (mCurPhoneState == PhoneState.IDEL) {
					// 拨号操作
					mCallOverExit = false;
					String number = mTvInputNumber.getText().toString();
					dial(number);
			} else {
				// 接听操作
				if (mService != null) {
					mService.pickUp();
				}
			}
		}
		startTime = System.currentTimeMillis();
	}

	/**
	 * 点击挂断按键
	 */
	private void clickHangUp() {
		if (mService != null) {
			if (mCurPhoneState == PhoneState.INCOMING) {
				// 来电时拒接
				mService.reject();
			} else {
				// 拨号或者通话中挂断
				mService.hangUp();
			}
		}
	}

	@Override
	public void doClick(View v) {
		switch (v.getId()) {
		case R.id.dtv_call:
			clickCall();
			break;
		case R.id.dtv_hand_up:
			clickHangUp();
			break;
		case R.id.iv_return:
			toHomeActivity();
			break;
		case R.id.btn_goto_contacts:
			L.d(thiz, "onclick goto contacts!");
			everJump = true;
			if (mCurPhoneState == PhoneState.IDEL) {
				goContactsFromDial();
			} else {
				Intent intent = new Intent(this, ContactsListActivity.class);
				intent.putExtra("from_dial", true);
				startActivityForResult(intent, 0);
			}
			break;
		case R.id.btn_goto_call_log:
			L.d(thiz, "onclick goto calllog!");
			everJump = true;
			if (mCurPhoneState == PhoneState.IDEL) {
				goCalllogFromDial();
			} else {
				Intent intent = new Intent(this, CallLogActivity.class);
				intent.putExtra("from_dial", true);
				startActivityForResult(intent, 0);
			}
			break;
		case R.id.key_delete:
			L.d(thiz, "onclick key_delete!");
			if (mCurPhoneState == PhoneState.IDEL) {
				mKeyBoardCb.deleteOne();
			}
			break;
		case R.id.key_mute:
			if (mService != null) {
				mService.toggleMic();
			}
			break;
		case R.id.key_switch_channel:
			if (mService != null) {
				mService.toggleTrack();
			}
			break;
		case R.id.key_0:
		case R.id.key_1:
		case R.id.key_2:
		case R.id.key_3:
		case R.id.key_4:
		case R.id.key_5:
		case R.id.key_6:
		case R.id.key_7:
		case R.id.key_8:
		case R.id.key_9:
		case R.id.key_star:
		case R.id.key_pound:
			
			dtmf.playTone(mKeyBoardCb.getChar(v.getId()));
			
			L.d(thiz, "onclick key phone state " + mCurPhoneState);
			if (mCurPhoneState == PhoneState.IDEL) {
				mKeyBoardCb.append(v.getId(), dtmf);
			} else if (mCurPhoneState == PhoneState.TALKING || mCurPhoneState == PhoneState.INPUT) {
				char code = mKeyBoardCb.getChar(v.getId());
				if (mService != null) {
					mService.dtmf(code);
				}
			}
			break;
		default:
			break;
		}
	}

	


	@Override
	protected boolean doLongClick(View v) {
		L.d(thiz, "onLongClick");
		switch (v.getId()) {
		case R.id.key_delete:
			mKeyBoardCb.deleteAll();
			break;
		case R.id.key_0:
			
			if (mCurPhoneState == PhoneState.IDEL) {
				mKeyBoardCb.append(key_plus, dtmf);
			} else if (mCurPhoneState == PhoneState.TALKING || mCurPhoneState == PhoneState.INPUT) {
				char code = mKeyBoardCb.getChar(key_plus);
				if (mService != null) {
					mService.dtmf(code);
				}
			}
			
			dtmf.playTone(mKeyBoardCb.getChar(v.getId()));
			return true;
		default:
			break;
		}
		return false;
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		mKeyBoardCb.unBindTextView();
		mKeyBoardCb.deleteAll();
		dtmf.destory();
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Constant.RESULT_FINISH_ACTIVITY) {
			//finish();
		}
	}

	@Override
	protected int getLayoutId() {
		return R.layout.activity_dial;
	}


	@Override
	protected String getPageName() {
		return "btphone_dial";
	}
	
	private void setMicMute(boolean isMute) {
		mIvMute.setSelected(isMute);
	}
	
	private void setSoundTrack(boolean isCar) {
		mIvSwitchChnanel.setSelected(isCar);
	}

	@Override
	public void showDisconnected() {
		L.d(thiz, "showDisconnected startActivity! start ");
		startActivity(new Intent(DialActivity.this, PhoneActivity.class));
		L.d(thiz, "showDisconnected startActivity! end ");
	}

	@Override
	public void showComing(UICallLog callLog) {
		onStateChange(PhoneState.INCOMING);
		setNameAndNumber(callLog.name, callLog.number);
	}

	@Override
	public void showCalling(UICallLog callLog) {
		L.d(thiz, "showCalling!");
		onStateChange(PhoneState.OUTGOING);
		setNameAndNumber(callLog.name, callLog.number);
		//拨打后就删掉输入文字
		mKeyBoardCb.deleteAll();
	}

	@Override
	public void showTalking(UICallLog callLog) {
		L.d(thiz, "showTalking");
		onStateChange(PhoneState.TALKING);
		setNameAndNumber(callLog.name, callLog.number);
		showTalkingTime(callLog.duration);
	}
	
	@Override
	public void showIdel() {
		L.d(thiz, "showIdel");
		setNameAndNumber("", "");
		onStateChange(PhoneState.IDEL);
		if(fromVoice) {
			myHandler.removeMessages(FROM_VOICE_TO_FALSE);
			myHandler.sendEmptyMessageDelayed(FROM_VOICE_TO_FALSE, 1000);
		} else {
			if (mCallOverExit && !everJump) {
				if(from == 1) {
					goCalllogFromDial();
					from = 0;
				} else if(from == 2) {
					goContactsFromDial();
					from = 0;
				} else {
					finish();
				}
			} 
			fromOutside = mCallOverExit = false;
		}
	
	}

	@Override
	public void showReject(UICallLog callLog) {
		L.d(thiz, "showReject");
		callOver();
	}
	
	@Override
	public void showHangUp(UICallLog callLog) {
		L.d(thiz, "showHangUp");
		callOver();
	}
	
	private void callOver() {
		mBtnHandUp.setVisibility(View.GONE);
		mBtnCall.setVisibility(View.GONE);
		mTvCallOver.setVisibility(View.VISIBLE);

		mKeyBoardCb.deleteAll();

		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				mTvCallOver.setVisibility(View.GONE);
				onStateChange(PhoneState.IDEL);
				
				setNameAndNumber("", "");
			}
		}, 500);
		
		if(fromOutside) {
			BtPhoneApplication.getInstance().exit();
			return;
		}
		
		if (mCallOverExit) {
			if(from == 1) {
				goCalllogFromDial();
				from = 0;
			} else if(from == 2) {
				goContactsFromDial();
				from = 0;
			} else {
				finish();
			}
		} 
		fromOutside = mCallOverExit = false;
	}
	
	


	@Override
	public void showLogsLoading(int type) {
//		mBtnGotoContacts.setEnabled(false);
//		mBtnGotoCallLog.setEnabled(false);
//		
//		mDtvGotoContacts.setEnabled(false);
//		mDtvGotoCallLog.setEnabled(false);
	}
	
	@Override
	public void syncLogsAlreadyLoad(int type) {
//		// 表示全部类型都下载完成 
//		if(type == 10) {
//			mBtnGotoContacts.setEnabled(true);
//			mBtnGotoCallLog.setEnabled(true);
//			
//			mDtvGotoContacts.setEnabled(true);
//			mDtvGotoCallLog.setEnabled(true);
//		}
	}
	
	@Override
	public void showMicMute(boolean isMute) {
		setMicMute(isMute);
	}

	@Override
	public void showSoundTrack(boolean isCar) {
		setSoundTrack(isCar);		
	}

	@Override
	public void updateAllLogs(List<CallLog> list) {
		allLogs = list;
		mCallAdapter.refresh(list);
	}
	
	
	@Override
	public void showDTMFInput(UICallLog callLog) {
		onStateChange(PhoneState.INPUT);
		mTvName.setText(TextUtils.ellipsize(callLog.dtmfStr, mTvName.getPaint(), 500, TextUtils.TruncateAt.START));
		//mTvName.setText(callLog.dtmfStr);
		showTalkingTime(callLog.duration);
	}
	
	
	private void setNameAndNumber(String name, String number) {
		name = handleUnknownName(name, number);
		mTvName.setText(TextUtils.ellipsize(name, mTvName.getPaint(), 500, TextUtils.TruncateAt.END));
		//mTvName.setText(name);
		mTvtalkNumber.setText(number);
	}
	
	
	private String handleUnknownName(String name, String number) {
		long start = System.currentTimeMillis();
		if(TextUtils.isEmpty(name) && !TextUtils.isEmpty(number)) {
			String tempName = "";
			for (CallLog log : allLogs) {
				if(number.equals(log.number)) {
					if(!TextUtils.isEmpty(log.name)) {
						tempName = log.name;
						break;
					}
				}
			}
			
			L.dRoll(thiz, "handleUnknownName cost : " + (System.currentTimeMillis() - start));
			
			return tempName;
			
		} else {
			return name;
		}
	}
	
	//from_dial 说明是从dial界面点击下部按钮跳到通讯录界面，通讯录结束后需要返回dial界面
	private void goContactsFromDial() {
		Intent intent = new Intent(this, ContactsListActivity.class);
		intent.putExtra("from_dial", true);
		startActivity(intent);
	}
	
	private void goCalllogFromDial() {
		Intent intent = new Intent(this, CallLogActivity.class);
		intent.putExtra("from_dial", true);
		startActivity(intent);
	}
	
	
	
}
