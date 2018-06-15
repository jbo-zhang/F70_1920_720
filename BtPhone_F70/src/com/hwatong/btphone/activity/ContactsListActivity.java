package com.hwatong.btphone.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.KeyboardView;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.hwatong.btphone.Contact;
import com.hwatong.btphone.activity.base.BaseActivity;
import com.hwatong.btphone.app.BtPhoneApplication;
import com.hwatong.btphone.bean.UICallLog;
import com.hwatong.btphone.constants.Constant;
import com.hwatong.btphone.ui.DialogViewControl;
import com.hwatong.btphone.ui.DrawableTextView;
import com.hwatong.btphone.ui.IndexableListView;
import com.hwatong.btphone.ui.R;
import com.hwatong.btphone.ui.ViewHolder;
import com.hwatong.btphone.util.DensityUtils;
import com.hwatong.btphone.util.KeyboardBuildFactory;
import com.hwatong.btphone.util.KeyboardBuildFactory.OnKeyEventCallBack;
import com.hwatong.btphone.util.L;
import com.hwatong.btphone.util.Utils;

/**
 * 后续将显示和隐藏ListView Item的功能封装成一个ListView的控件
 * 
 * @author zxy zjb time:2017年5月25日
 * 
 */
public class ContactsListActivity extends BaseActivity {

	private static final String thiz = ContactsListActivity.class.getSimpleName();

	private IndexableListView mLvContacts;
	private TextView tvNoData;

	private ImageView mIvReturn;

	private View mBtnUpdate;

	private View mBtnLetterSearch;

	private EditText mEtShowInputText;
	private View mLayoutKeyBoard;

	private ContactsAdapter mAdapter;
	private List<Contact> mContactsList = new ArrayList<Contact>();

	private DialogViewControl mDialogControl;

	private int mKeyBoardWidth;
	private int mKeyBoardHeight;

	private boolean keyboardAdded = false;

	private LayoutParams params;
	
	
	private View vRightLabel, flRightLabel;
	private TextView rlName, rlNumber;

	/**
	 * 一个傀儡，不可见，但是弹出虚拟键盘获得输入文字要用到
	 */
	private EditText etSearch;

	/**
	 * 同步上面那个傀儡的显示
	 */
	private TextView tvTexting;

	@Override
	protected void initView() {
		mLvContacts = (IndexableListView) findViewById(R.id.ilv_contacts);
		tvNoData = (TextView) findViewById(R.id.tv_nodata);

		mIvReturn = (ImageView) findViewById(R.id.iv_return);
		mIvReturn.setOnClickListener(this);

		mBtnUpdate = findViewById(R.id.btn_update_contacts);
		mBtnUpdate.setOnClickListener(this);

		mBtnLetterSearch = findViewById(R.id.btn_letter_search);
		mBtnLetterSearch.setOnClickListener(this);
		
		L.d(thiz, DensityUtils.getScreenWidth(this) + " width! " + DensityUtils.getScreenHeight(this) + " height!");
		if(DensityUtils.getScreenWidth(this) == 1920) {
			vRightLabel = findViewById(R.id.v_right_label);
			flRightLabel = findViewById(R.id.fl_right_label);
			rlName = (TextView) flRightLabel.findViewById(R.id.tv_rl_name);
			rlNumber = (TextView) flRightLabel.findViewById(R.id.tv_rl_number);
			mLvContacts.attachLabel(vRightLabel, flRightLabel,  rlName, rlNumber);
		}
		
		initListView();
		initKeyBoard();
		
		if(getIntent() != null) {
			fromDial = getIntent().getBooleanExtra("from_dial", false);
			L.d(thiz, "from dial : " + fromDial);
		}
		
		etSearch = (EditText)findViewById(R.id.et_search);
		
		tvTexting = (TextView) findViewById(R.id.tv_texting);
		
		etSearch.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				tvTexting.setText(s.toString());
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
		});
		
		etSearch.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				/*判断是否是“GO”键*/  
	              if(keyCode == KeyEvent.KEYCODE_ENTER){  
	                  /*隐藏软键盘*/  
	            	  hideSoftInput();
	                  searchContactsByLetter(etSearch.getText().toString().trim());
	                  return true;  
	              }  
	              return false;  
			}
		});
		
	}
	
	@Override
	 public boolean dispatchTouchEvent(MotionEvent ev) { 
	 if (ev.getAction() == MotionEvent.ACTION_DOWN) { 
		 // 获得当前得到焦点的View，一般情况下就是EditText（特殊情况就是轨迹求或者实体案件会移动焦点） 
		//View v = getCurrentFocus(); 
		 if (isShouldHideInput(etSearch, ev)) { 
			 L.d(thiz, "hide Soft Input ");
			 hideSoftInput();
		 } 
	 } 
	 return super.dispatchTouchEvent(ev); 
	 } 
	 /** 
	 * 根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘，因为当用户点击EditText时没必要隐藏 
	 * 
	 * @param v 
	 * @param event 
	 * @return 
	 */
	 private boolean isShouldHideInput(View v, MotionEvent event) { 
		 if (v != null && (v instanceof EditText)) { 
			 int[] l = { 0, 0 }; 
			 v.getLocationInWindow(l); 
			 int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left + v.getWidth(); 
			 if (event.getX() > left && event.getX() < right && event.getY() > top && event.getY() < bottom) { 
				 // 点击EditText的事件，忽略它。 
				 return false; 
			 } else { 
				 return true; 
			 } 
		 } 
		 // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditView上，和用户用轨迹球选择其他的焦点 
		 return false; 
	 } 
	
	 
	 private void hideSoftInput() {
		 startAnimation(tvTexting, false);
		 InputMethodManager imm = (InputMethodManager) etSearch.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);  
         if (imm.isActive()) {  
             imm.hideSoftInputFromWindow(etSearch.getApplicationWindowToken(), 0);  
         }  
	 }
	 
	private void initListView() {
		mAdapter = new ContactsAdapter(mContactsList);
		mLvContacts.setAdapter(mAdapter);
		mLvContacts.setEmptyView(tvNoData);
	}

	@Override
	protected void serviceConnected() {
		
	}

	private void initKeyBoard() {
		mLayoutKeyBoard = getLayoutInflater().inflate(R.layout.keyboard, null);
		mEtShowInputText = (EditText) mLayoutKeyBoard.findViewById(R.id.et_show_input);
		KeyboardBuildFactory factory = KeyboardBuildFactory.bindKeyboard(this,
				(KeyboardView) mLayoutKeyBoard.findViewById(R.id.keyboard),
				R.xml.input_method_layput, mEtShowInputText);
		factory.setOnKeyEventCallBack(new OnKeyEventCallBack() {
			@Override
			public void onKeyAction(int primaryCode) {
				switch (primaryCode) {
				case -1:// 返回键
					hideKeyboard();
					break;
				case -3:// 确定键
					hideKeyboard();
					searchContactsByLetter(mEtShowInputText.getText().toString().trim());
					break;

				default:
					break;
				}
			}
		});

		DisplayMetrics outMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
		mKeyBoardWidth = outMetrics.widthPixels * 2 / 3;
		mKeyBoardHeight = getResources().getDimensionPixelSize(R.dimen.keyboard_height);

		params = new LayoutParams();
		params.windowAnimations = R.style.style_anim_keyboard_popup_windows;
		params.width = mKeyBoardWidth;
		params.height = mKeyBoardHeight;
		params.type = LayoutParams.TYPE_SYSTEM_ALERT;
		params.format = PixelFormat.RGB_888;
		params.flags = LayoutParams.FLAG_ALT_FOCUSABLE_IM;
		params.x = 0;
		params.y = 0;
		params.gravity = Gravity.LEFT | Gravity.BOTTOM;

	}

	/**
	 * 通话挂断、结束时调用
	 */
	private void onHangUp() {
		mLvContacts.setItemClickEnable(true);
		setResult(Constant.RESULT_FINISH_ACTIVITY);
		//电话在这个界面挂断，不结束
		//finish();
	}

	/**
	 * 根据输入的首字符显示所有匹配的联系人
	 * @param letters 为空时默认显示所有联系人
	 */
	private void searchContactsByLetter(String letters) {
		L.d(thiz, "searchContactsByLetter! letters = " + letters + "mContactsList.size : " + mContactsList.size());
		long start = SystemClock.currentThreadTimeMillis();
		if (TextUtils.isEmpty(letters)) {
			mAdapter.refresh(mContactsList);
			
		} else {
			List<Contact> selectContacts1 = new ArrayList<Contact>();
			List<Contact> selectContacts2 = new ArrayList<Contact>();
			
			if(isChinese(letters)) {
				L.d(thiz, "isChinese!");
				for (Contact contact : mContactsList) {
					if (contact.name.contains(letters)) {
						L.d(thiz, "name Chinese match: " + contact.name);
						selectContacts1.add(contact);
					}
				}
				mAdapter.refresh(selectContacts1);
				
			} else {
				L.d(thiz, "is not Chinese!");
				letters = letters.toUpperCase();
				for (Contact contact : mContactsList) {
					if (contact.comFlg.toUpperCase().startsWith(letters.substring(0, 1))) {
						L.d(thiz, "name first letter: " + contact.name);
						selectContacts1.add(contact);
					}
				}
				
				if(letters.length() > 1) {
					//汉字首字母匹配
					for (Contact contact : selectContacts1) {
						if(contact.comFlg.toUpperCase().contains(letters.substring(1, 2))) {
							L.d(thiz, "name second letter: " + contact.name);
							if(Utils.getPinyinAndFirstLetter(contact.name)[1].startsWith(letters)) {
								selectContacts2.add(contact);
							}
						}
					}
					
					if(selectContacts2.size() > 0) {
						mAdapter.refresh(selectContacts2);
					} else {
						//全拼匹配
						for (Contact contact : selectContacts1) {
							if(contact.comFlg.toUpperCase().startsWith(letters)) {
								selectContacts2.add(contact);
							}
						}
						mAdapter.refresh(selectContacts2);
					}
					
				} else {
					mAdapter.refresh(selectContacts1);
				}
			}
		}
		
		L.d(thiz,"search cost : " + (SystemClock.currentThreadTimeMillis() - start));
	}
	
	
	public static boolean isChinese(String str) {
		String regEx = "[\u4e00-\u9fa5]";
		Pattern pat = Pattern.compile(regEx);
		Matcher matcher = pat.matcher(str);
		boolean flg = false;
		if (matcher.find())
			flg = true;

		return flg;
	}
	

	/**
	 * 弹出虚拟键盘
	 */
	private void showKeyBoard() {
		if (!keyboardAdded) {
			getWindowManager().addView(mLayoutKeyBoard, params);
			keyboardAdded = true;
		}
	}

	/**
	 * 收起虚拟键盘
	 */
	private void hideKeyboard() {
		if (keyboardAdded) {
			getWindowManager().removeView(mLayoutKeyBoard);
			keyboardAdded = false;
		}
	}

	@Override
	public void doClick(View v) {
		mLvContacts.hideCurrentItemBtn();
		switch (v.getId()) {
		case R.id.iv_return:
			if(fromDial) {
				finish();
			} else {
				toHomeActivity();
			}
			break;
		case R.id.btn_update_contacts:
			if(mService != null) {
				mService.loadBooks();
			}
			break;
		case R.id.btn_letter_search:
//			showKeyBoard();
			
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);  
			imm.showSoftInput(etSearch,InputMethodManager.SHOW_FORCED); 
			startAnimation(tvTexting, true);
			
			break;
		default:
			break;
		}

	}
	
	
	/**
	 * 显示与隐藏伪造的输入法文本框的方法
	 * @param v
	 * @param show
	 */
	private void startAnimation(View v, boolean show) {
		if(show) {
			SystemClock.sleep(200);
			v.setVisibility(View.VISIBLE);
		} else {
			v.setVisibility(View.INVISIBLE);
		}
	}
	
	
	private void showProgressDialog(int textId) {
		if (mDialogControl == null) {
			mDialogControl = new DialogViewControl(this);
		}
		mDialogControl.showProgressWithText(getResources().getString(textId));
	}
	
	private void showTextDialog(int textId) {
		if (mDialogControl == null) {
			mDialogControl = new DialogViewControl(this);
		}
		mDialogControl.showOnlyText(getResources().getString(textId));
	}
	
	private void dismissDialog() {
		if(mDialogControl != null) {
			mDialogControl.dismiss();
			mDialogControl = null;
		}
	}

	private void dial(String number) {
		if (!TextUtils.isEmpty(number) && mService != null) {
			mService.dial(number);
		}
	}

	private class ContactsAdapter extends BaseAdapter implements SectionIndexer {

		private String sectionStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZ#";
		private String[] sections;
		private List<Contact> mDataList = new ArrayList<Contact>();

		public ContactsAdapter(List<Contact> data) {
			mDataList.addAll(data);
		}

		public void refresh(List<Contact> data) {
			mDataList.clear();
			mDataList.addAll(data);
			notifyDataSetChanged();
		}

		@Override
		public Object[] getSections() {
			if (sections == null) {
				sections = new String[sectionStr.length()];
				for (int i = 0; i < sectionStr.length(); i++) {
					sections[i] = String.valueOf(sectionStr.charAt(i));
				}
			}
			return sections;
		}

		@Override
		public int getPositionForSection(int section) {
			for (int i = section; i >= 0; i--) {
				int index = Utils.getPositionForSection(mDataList, sections[i].charAt(0));
				if (index != -1) {
					return index;
				}
			}
			return 0;
		}

		@Override
		public int getSectionForPosition(int position) {
			return position;
		}

		@Override
		public int getCount() {
			return mDataList.size();
		}

		@Override
		public Contact getItem(int position) {
			if (mDataList.size() > position) {
				return mDataList.get(position);
			} else {
				return null;
			}
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {

			ViewHolder holder = null;

			if (convertView == null) {
				holder = new ViewHolder();

				convertView = getLayoutInflater().inflate(
						R.layout.item_contacts_btn, null);
				holder.mDtvName = (DrawableTextView) convertView
						.findViewById(R.id.dtv_name);
				holder.mTvNumber = (TextView) convertView
						.findViewById(R.id.tv_phone_number);
				holder.mBtnDial = (ImageButton) convertView
						.findViewById(R.id.btn_dial);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			Contact contact = getItem(position);
			if (contact == null) {
				return convertView;
			}
			Drawable drawableLeft = getResources().getDrawable(
					R.drawable.icon_people);

			holder.mDtvName.setDrawables(drawableLeft, null, null, null);
//			holder.mDtvName.setText(contact.name);
//			holder.mTvNumber.setText(contact.number);
			holder.mDtvName.setText(TextUtils.ellipsize(contact.name, holder.mDtvName.getPaint(), 310, TextUtils.TruncateAt.END));
			holder.mTvNumber.setText(TextUtils.ellipsize(contact.number,holder.mTvNumber.getPaint(), 260, TextUtils.TruncateAt.END));
			
			
			
			holder.mBtnDial.setFocusable(false);
			holder.mBtnDial.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Contact contact = getItem(position);
					dial(contact.number);
				}
			});

			return convertView;
		}

	}

	@Override
	protected int getLayoutId() {
		return R.layout.activity_contacts;
	}

	@Override
	protected String getPageName() {
		return "btphone_contacts";
	}

	@Override
	public void updateBooks(List<Contact> list) {
		mContactsList.clear();
		mContactsList.addAll(list);
		//searchContactsByLetter(mEtShowInputText.getText().toString().trim());
		searchContactsByLetter(tvTexting.getText().toString().trim());
		mLvContacts.hideCurrentItemBtn();
		//mAdapter.refresh(list);
	}

	@Override
	public void showBooksLoadStart() {
		showTextDialog(R.string.dialog_update_contact);
	}

	@Override
	public void showBooksLoading() {
		showProgressDialog(R.string.dialog_updating);
	}

	@Override
	public void showBooksLoaded(boolean succeed, int reason) {
		showTextDialog(R.string.dialog_updated);
	}

	@Override
	public void showDisconnected() {
		startActivity(new Intent(ContactsListActivity.this, PhoneActivity.class));
	}

	@Override
	public void showComing(UICallLog callLog) {
		if(callLog.shouldJump == 1) {
			if(((BtPhoneApplication) getApplication()).getActivitySize() == 3) {
				Utils.gotoDialActivity(this, callLog, 2);
			} else {
				Utils.gotoDialActivity(this, callLog);
			}
		}
	}

	@Override
	public void showCalling(UICallLog callLog) {
		if(callLog.shouldJump == 1) {
			if(((BtPhoneApplication) getApplication()).getActivitySize() == 3) {
				Utils.gotoDialActivity(this, callLog, 2);
			} else {
				Utils.gotoDialActivity(this, callLog);
			}
		}
	}
	
	@Override
	public void showTalking(UICallLog callLog) {
		if(callLog.shouldJump == 1) {
			if(((BtPhoneApplication) getApplication()).getActivitySize() == 3) {
				Utils.gotoDialActivity(this, callLog, 2);
			} else {
				Utils.gotoDialActivity(this, callLog);
			}
		}
	}

	@Override
	public void showHangUp(UICallLog callLog) {
		onHangUp();
	}

	@Override
	public void syncBooksAlreadyLoad() {
		dismissDialog();		
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		hideSoftInput();
	}
}
