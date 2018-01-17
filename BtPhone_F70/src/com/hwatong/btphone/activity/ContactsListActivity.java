package com.hwatong.btphone.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.KeyboardView;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.hwatong.btphone.Contact;
import com.hwatong.btphone.activity.base.BaseActivity;
import com.hwatong.btphone.bean.UICallLog;
import com.hwatong.btphone.constants.Constant;
import com.hwatong.btphone.ui.DialogViewControl;
import com.hwatong.btphone.ui.DrawableTextView;
import com.hwatong.btphone.ui.IndexableListView;
import com.hwatong.btphone.ui.R;
import com.hwatong.btphone.ui.ViewHolder;
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

		initListView();
		initKeyBoard();
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
		finish();
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
		
		L.d(thiz,"search cost : " + (SystemClock.currentThreadTimeMillis() - start));
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
			finish();
			break;
		case R.id.btn_update_contacts:
			if(mService != null) {
				mService.loadBooks();
			}
			break;
		case R.id.btn_letter_search:
			showKeyBoard();
			break;
		default:
			break;
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
		searchContactsByLetter(mEtShowInputText.getText().toString().trim());
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
			Utils.gotoDialActivity(this, callLog);
		}
	}

	@Override
	public void showCalling(UICallLog callLog) {
		if(callLog.shouldJump == 1) {
			Utils.gotoDialActivity(this, callLog);
		}
	}
	
	@Override
	public void showTalking(UICallLog callLog) {
		if(callLog.shouldJump == 1) {
			Utils.gotoDialActivity(this, callLog);
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

}
