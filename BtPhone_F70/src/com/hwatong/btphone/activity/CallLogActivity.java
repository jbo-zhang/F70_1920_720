package com.hwatong.btphone.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hwatong.btphone.CallLog;
import com.hwatong.btphone.activity.base.BaseActivity;
import com.hwatong.btphone.adapter.CallLogListAdapter;
import com.hwatong.btphone.adapter.CallLogListAdapter.ButtonOnClick;
import com.hwatong.btphone.bean.UICallLog;
import com.hwatong.btphone.constants.Constant;
import com.hwatong.btphone.ui.DialogViewControl;
import com.hwatong.btphone.ui.PopItemButtonListView;
import com.hwatong.btphone.ui.R;
import com.hwatong.btphone.util.L;
import com.hwatong.btphone.util.Utils;

/**
 * 通话记录界面
 * @author zxy zjb time:2017年5月25日
 */
public class CallLogActivity extends BaseActivity {
	
	private static final String thiz = CallLogActivity.class.getSimpleName();
	
	private PopItemButtonListView mLvCallLog;

	private ImageView mIvReturn;

	private TextView mTvUpdateLog;

	private TextView mTvCallMiss;// 未接电话

	private TextView mTvCallIn;// 已拨电话

	private TextView mTvCallOut;// 已接电话

	private TextView mTvNoData;

	private CallLogListAdapter mAdapter;
	private SparseArray<List<CallLog>> mCallLogMap = new SparseArray<List<CallLog>>(3);
	private int mCurCallLogType = UICallLog.TYPE_CALL_MISS;

	private DialogViewControl mDialogControl;

	@Override
	protected void initView() {
		mLvCallLog = (PopItemButtonListView) findViewById(R.id.lv_call_log);

		mIvReturn = (ImageView) findViewById(R.id.iv_return);
		mIvReturn.setOnClickListener(this);

		mTvUpdateLog = (TextView) findViewById(R.id.tv_update_log);
		mTvUpdateLog.setOnClickListener(this);

		mTvCallMiss = (TextView) findViewById(R.id.tv_log_miss);
		mTvCallMiss.setOnClickListener(this);

		mTvCallIn = (TextView) findViewById(R.id.tv_log_in);
		mTvCallIn.setOnClickListener(this);

		mTvCallOut = (TextView) findViewById(R.id.tv_log_out);
		mTvCallOut.setOnClickListener(this);

		mTvNoData = (TextView) findViewById(R.id.tv_nodata);

		// 默认显示未接来电
		mTvCallMiss.setSelected(true);

		initListView();
	}

	private void initListView() {
		mCallLogMap.put(UICallLog.TYPE_CALL_MISS, new ArrayList<CallLog>());
		mCallLogMap.put(UICallLog.TYPE_CALL_IN, new ArrayList<CallLog>());
		mCallLogMap.put(UICallLog.TYPE_CALL_OUT, new ArrayList<CallLog>());
		
		mLvCallLog.setEmptyView(mTvNoData);

		mAdapter = new CallLogListAdapter(this, R.layout.item_contacts_btn, mCallLogMap.get(mCurCallLogType));
		mLvCallLog.setAdapter(mAdapter);

		mAdapter.setmBtnOnClickListener(new ButtonOnClick() {
			@Override
			public void clickButton(CallLog callLog) {
				if (callLog != null && !TextUtils.isEmpty(callLog.number) && mService != null) {
					mService.dial(callLog.number);
				}
			}
		});
	}

	/**
	 * 通话挂断、结束时调用
	 */
	private void onHangUp() {
		mLvCallLog.setItemClickEnable(true);
		setResult(Constant.RESULT_FINISH_ACTIVITY);
		finish();
	}

	/**
	 * 未接来电、已拨电话以及已接电话，变化时刷新界面
	 */
	private void onCallLogTypeChange(Integer callType) {
		if (mCurCallLogType == callType) {
			return;
		}
		mService.syncLogsStatus(callType);
		mCurCallLogType = callType;
		mTvCallMiss.setSelected(UICallLog.TYPE_CALL_MISS == mCurCallLogType);
		mTvCallIn.setSelected(UICallLog.TYPE_CALL_IN == mCurCallLogType);
		mTvCallOut.setSelected(UICallLog.TYPE_CALL_OUT == mCurCallLogType);
		updateCallLogListView();
	}

	/**
	 * 数据变化时刷新ListView界面
	 */
	private void updateCallLogListView() {
		List<CallLog> callLogs = mCallLogMap.get(mCurCallLogType);
		if (callLogs != null) {
			L.d(thiz, "mAdapter.refresh() type : " + mCurCallLogType);
			mAdapter.refresh(callLogs);
		}
	}

	/**
	 * 点击更新通话记录
	 * @param firstIn
	 */
	private void clickUpdateCallLog() {
		if (mService != null) {
			switch (mCurCallLogType) {
			case UICallLog.TYPE_CALL_MISS:
				mService.loadMissedLogs();
				break;
			case UICallLog.TYPE_CALL_IN:
				mService.loadReceivedLogs();
				break;
			case UICallLog.TYPE_CALL_OUT:
				mService.loadDialedLogs();
				break;
			default:
				break;
			}
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
	

	@Override
	public void doClick(View v) {
		mLvCallLog.hideCurrentItemBtn();
		switch (v.getId()) {
		case R.id.iv_return:
			finish();
			break;
		case R.id.tv_update_log:
			clickUpdateCallLog();
			break;
		case R.id.tv_log_miss:
			onCallLogTypeChange(UICallLog.TYPE_CALL_MISS);
			break;
		case R.id.tv_log_in:
			onCallLogTypeChange(UICallLog.TYPE_CALL_IN);
			break;
		case R.id.tv_log_out:
			onCallLogTypeChange(UICallLog.TYPE_CALL_OUT);
			break;

		default:
			break;
		}
	}

	@Override
	protected int getLayoutId() {
		return R.layout.activity_call_log;
	}

	@Override
	protected String getPageName() {
		return "btphone_calllog";
	}
	
	@Override
	public void showDisconnected() {
		startActivity(new Intent(this, PhoneActivity.class));
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
	public void showLogsLoadStart(int type) {
		if(type == mCurCallLogType) {
			showTextDialog(R.string.dialog_update_callog);
		}
	}
	
	@Override
	public void showLogsLoading(int type) {
		if(type == mCurCallLogType) {
			showProgressDialog(R.string.dialog_updating);
		}
	}
	
	@Override
	public void showLogsLoaded(int type, int result) {
		L.d(thiz, "showLogLoaded type= " + type + " result= " + result);
		if(type == mCurCallLogType) {
			showTextDialog(R.string.dialog_updated);
		}
	}
	
	@Override
	public void syncLogsAlreadyLoad(int type) {
		if(type == mCurCallLogType) {
			dismissDialog();
		}
	}
	
	@Override
	public void updateDialedLogs(List<CallLog> list) {
		mCallLogMap.get(UICallLog.TYPE_CALL_OUT).clear();
		mCallLogMap.get(UICallLog.TYPE_CALL_OUT).addAll(list);
		if(UICallLog.TYPE_CALL_OUT == mCurCallLogType) {
			mAdapter.refresh(list);
		}
	}
	
	@Override
	public void updateMissedLogs(List<CallLog> list) {
		mCallLogMap.get(UICallLog.TYPE_CALL_MISS).clear();
		mCallLogMap.get(UICallLog.TYPE_CALL_MISS).addAll(list);
		if(UICallLog.TYPE_CALL_MISS == mCurCallLogType) {
			mAdapter.refresh(list);
		}
	}
	
	@Override
	public void updateReceivedLogs(List<CallLog> list) {
		mCallLogMap.get(UICallLog.TYPE_CALL_IN).clear();
		mCallLogMap.get(UICallLog.TYPE_CALL_IN).addAll(list);
		if(UICallLog.TYPE_CALL_IN == mCurCallLogType) {
			mAdapter.refresh(list);
		}
	}
	
	@Override
	public void toMissedCalls() {
		onCallLogTypeChange(UICallLog.TYPE_CALL_MISS);
	}
	
}
