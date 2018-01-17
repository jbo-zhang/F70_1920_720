package com.hwatong.btphone.presenter;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;
import android.os.TransactionTooLargeException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RawContacts.Data;

import com.hwatong.btphone.Contact;
import com.hwatong.btphone.util.L;
/**
 * 该类的作用是将通讯录插入到数据库，以便讯飞语音可以通过人名提取联系人拨打电话
 * @author zxy time:2017年11月7日
 *
 */
public class PhoneBookPresenter {
	private static final String thiz = PhoneBookPresenter.class.getSimpleName();
	
	private Context context;
	
	private ContentValues values = new ContentValues();
	
	public PhoneBookPresenter(Context context) {
		this.context = context;
	}
	
	public long addContact(String name, String phoneNumber) {
		long start = System.currentTimeMillis();
		values.clear();
		Uri rawContactUri = context.getContentResolver().insert(RawContacts.CONTENT_URI, values);
		long rawContactId = ContentUris.parseId(rawContactUri);
		values.put(Data.RAW_CONTACT_ID, rawContactId);
		values.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
		values.put(StructuredName.GIVEN_NAME, name);
		context.getContentResolver().insert(android.provider.ContactsContract.Data.CONTENT_URI,values);

		values.clear();
		values.put(Data.RAW_CONTACT_ID, rawContactId);
		values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
		values.put(Phone.NUMBER, phoneNumber);
		values.put(Phone.TYPE, Phone.TYPE_MOBILE);
		context.getContentResolver().insert(android.provider.ContactsContract.Data.CONTENT_URI,values);

		L.d(thiz, "add Contact cost : " + (System.currentTimeMillis() - start));
		
		return rawContactId;
	}
	
	public void addContacts(List<Contact> list) {
		exitToFalse();
		
		delContacts();
		
		if (exitPending())	return;
		
		final int length = list.size();
		
		L.d(thiz, "addContacts list size : " + list.size());
		
		long start = System.currentTimeMillis();
		
		final int LIMIT_PER_LENGTH = 400; //large insert will cause binder data overflow.
		int syncedCount = 0;

		while (syncedCount < length) {
			
			if (exitPending())	break;
			
			int perLength = (length - syncedCount) < LIMIT_PER_LENGTH ? (length - syncedCount) : LIMIT_PER_LENGTH;
			ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

			for (int i = syncedCount; i < syncedCount+perLength; i++) {
				
				if (exitPending())	break;
				
				
				final Contact contact = list.get(i);
				try {
					int rawContactInsertIndex = ops.size();
					ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
										.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
										.withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
										.withYieldAllowed(true).build());

		            ops.add(ContentProviderOperation
		                    .newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
		                    .withValueBackReference(ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactInsertIndex)
		                    .withValue(ContactsContract.Contacts.Data.MIMETYPE, 
		                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
		                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact.name)
		                    .withYieldAllowed(true).build());

					ops.add(ContentProviderOperation
										.newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
										.withValueBackReference(ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactInsertIndex)
										.withValue(ContactsContract.Contacts.Data.MIMETYPE, 
												ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
										.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.number.replaceAll("-|\\s+", ""))
										.withValue(ContactsContract.CommonDataKinds.Phone.TYPE, 
												ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
										.withValue(ContactsContract.CommonDataKinds.Phone.LABEL, "")
										.withYieldAllowed(true).build());
				} catch (IndexOutOfBoundsException e) {
					L.d(thiz, "IndexOutOfBoundsException in commit");
					e.printStackTrace();
				}
			}

			if (exitPending())	break;
			
			try {
				context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
				ops.clear();
			} catch (final TransactionTooLargeException e) {
				L.d(thiz, "TransactionTooLargeException in commit");
				e.printStackTrace();
			} catch (final RemoteException e) {
				L.d(thiz, "RemoteException in commit");
				e.printStackTrace();
			} catch (final OperationApplicationException e) {
				L.d(thiz, "OperationApplicationException in commit");
				e.printStackTrace();
			}
			syncedCount += perLength;
		}
		
		L.d(thiz, "addContacts cost : " + (System.currentTimeMillis() - start));
		
	}
	
	
	public void delContacts() {
		long start = System.currentTimeMillis();
		context.getContentResolver().delete(android.provider.ContactsContract.Data.CONTENT_URI, null, null);
		
		if (exitPending())	return;
		
		context.getContentResolver().delete(RawContacts.CONTENT_URI, null, null);
		
		L.d(thiz, "delContacts cost : " + (System.currentTimeMillis() - start));
	}
	
	
	public void batchDeleteContact() {
		long start = System.currentTimeMillis();
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ops.add(ContentProviderOperation.newDelete(android.provider.ContactsContract.Data.CONTENT_URI)
                .withSelection(null,null)
                .build());
        ops.add(ContentProviderOperation.newDelete(RawContacts.CONTENT_URI)
                .withSelection(null,null)
                .build());
        try {
			context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
			ops.clear();
		} catch (final TransactionTooLargeException e) {
			e.printStackTrace();
		} catch (final RemoteException e) {
			L.d(thiz, "RemoteException in commit");
			e.printStackTrace();
		} catch (final OperationApplicationException e) {
			L.d(thiz, "OperationApplicationException in commit");
			e.printStackTrace();
		}
        
        L.d(thiz, "batchDeleteContact cost : " + (System.currentTimeMillis() - start));
    }
	
	private boolean exit = false;
	
	private synchronized boolean exitPending() {
		return exit;
	}
	
	public synchronized void requestExit() {
		exit = true;
	}
	
	public synchronized void exitToFalse() {
		exit = false;
	}
	
	
	
//	private class WorkThread extends Thread {
//		public static final int IMPORT_PB = 1;
//		public static final int DELETE_PB = 2;
//
//		private boolean exit;
//		private boolean done;
//		private int process;
//		private final List<Contact> mWorkList = new ArrayList<Contact>();
//
//		public WorkThread(int process) {
//			this.process = process;
//		}
//		public synchronized void requestExitAndWait() {
//			exit = true;
//			while(!done) {
//				try {
//					wait();
//				}catch(InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//
//		public void updateWorkList(List<Contact> list) {
//			mWorkList.clear();
//			mWorkList.addAll(list);
//		}
//
//		@Override
//		public synchronized void start() {
//			done = false;
//			exit = false;
//			super.start();
//		}
//		@Override
//		public void run() {
//			super.run();
//
//			if (!exitPending()) {
//				try {
//					//delay for frequent connect disconnect
//					sleep(500);
//				} catch (InterruptedException e) {
//				}
//			}
//
//			if (process == IMPORT_PB) {
//				//first delete all pb
//				Log.d("TestPB", "start delContacts!");
//				delContacts();
//				Log.d("TestPB", "end delContacts!");
//
//				Log.d("TestPB", "start addContacts!");
//				addContacts();
//				Log.d("TestPB", "end addContacts!");
//			} else if (process == DELETE_PB) {
//				delContacts();
//			}
//
//			synchronized(this) {
//				done=true;
//				notifyAll();
//			}
//		}
//
//		private synchronized boolean exitPending() {
//			return exit;
//		}
//
//		ContentValues values = new ContentValues();
//		private long addContact(Context context, String name, String phoneNumber) {
//			values.clear();
//			Uri rawContactUri = context.getContentResolver().insert(RawContacts.CONTENT_URI, values);
//			long rawContactId = ContentUris.parseId(rawContactUri);
//			values.put(Data.RAW_CONTACT_ID, rawContactId);
//			values.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
//			values.put(StructuredName.GIVEN_NAME, name);
//			context.getContentResolver().insert(android.provider.ContactsContract.Data.CONTENT_URI,values);
//
//			values.clear();
//			values.put(Data.RAW_CONTACT_ID, rawContactId);
//			values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
//			values.put(Phone.NUMBER, phoneNumber);
//			values.put(Phone.TYPE, Phone.TYPE_MOBILE);
//			context.getContentResolver().insert(android.provider.ContactsContract.Data.CONTENT_URI,values);
//
//			return rawContactId;
//		}
//
//		private void addContacts() {
//			final int length = mWorkList.size();
//			final int LIMIT_PER_LENGTH = 400; //large insert will cause binder data overflow.
//			int syncedCount = 0;
//
//			while (syncedCount < length) {
//				if (exitPending())	break;
//
//				int perLength = (length - syncedCount) < LIMIT_PER_LENGTH ? (length - syncedCount) : LIMIT_PER_LENGTH;
//				ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
//
//				for (int i = syncedCount; i < syncedCount+perLength; i++) {
//					if (exitPending())	break;
//
//					final Contact contact = mWorkList.get(i);
//					try {
//						int rawContactInsertIndex = ops.size();
//						ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
//											.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
//											.withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
//											.withYieldAllowed(true).build());
//
//			            ops.add(ContentProviderOperation
//			                    .newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
//			                    .withValueBackReference(ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactInsertIndex)
//			                    .withValue(ContactsContract.Contacts.Data.MIMETYPE, 
//			                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
//			                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contact.name)
//			                    .withYieldAllowed(true).build());
//
//						ops.add(ContentProviderOperation
//											.newInsert(android.provider.ContactsContract.Data.CONTENT_URI)
//											.withValueBackReference(ContactsContract.Contacts.Data.RAW_CONTACT_ID, rawContactInsertIndex)
//											.withValue(ContactsContract.Contacts.Data.MIMETYPE, 
//													ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
//											.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contact.number.replaceAll("-|\\s+", ""))
//											.withValue(ContactsContract.CommonDataKinds.Phone.TYPE, 
//													ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
//											.withValue(ContactsContract.CommonDataKinds.Phone.LABEL, "")
//											.withYieldAllowed(true).build());
//					} catch (IndexOutOfBoundsException e) {}
//				}
//
//				if (exitPending())	break;
//
//				try {
//					getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
//					ops.clear();
//				} catch (final TransactionTooLargeException e) {
//					e.printStackTrace();
//				} catch (final RemoteException e) {
//					if(DBG)Log.d(TAG, "RemoteException in commit");
//					e.printStackTrace();
//				} catch (final OperationApplicationException e) {
//					if(DBG)Log.d(TAG, "OperationApplicationException in commit");
//					e.printStackTrace();
//				}
//				syncedCount += perLength;
//			}
//
//		}
//
////		private void addContacts() {
////			for (int i = 0; i < mWorkList.size(); i++) {
////				if (exitPending())	break;
////
////				try {
////					final Contact contact = mWorkList.get(i);
////					long id = addContact(contact.name, contact.number.replaceAll("-|\\s+", ""));
////					//mRawContactIdList.add(id);
////				} catch (IndexOutOfBoundsException e) {}
////			}
////		}
//
//		private void delContacts() {
//	//			boolean breakLoop = false;
//	//			for (Long rawContactId : mRawContactIdList) {
//	//				if (exitPending()) {
//	//					breakLoop = true;
//	//					break;
//	//				}
//	//
//	//				if (rawContactId <= 0)	continue;
//	//				getContentResolver().delete(android.provider.ContactsContract.Data.CONTENT_URI, "raw_contact_id=?", new String[]{String.valueOf(rawContactId)});
//	//				getContentResolver().delete(RawContacts.CONTENT_URI, ContactsContract.Data._ID+"=?", new String[]{String.valueOf(rawContactId)});
//	//			}
//	//
//	//			if (!breakLoop) mRawContactIdList.clear();
//			getContentResolver().delete(android.provider.ContactsContract.Data.CONTENT_URI, null, null);
//			getContentResolver().delete(RawContacts.CONTENT_URI, null, null);
//		}
//	};
//
//	private void startPBWork(int process) {
//		mWorkerThread = new WorkThread(process);
//		mWorkerThread.updateWorkList(mContactList);
//		mWorkerThread.start();
//	}
//
//	private void stopPBWork() {
//		if (mWorkerThread != null) {
//			mWorkerThread.requestExitAndWait();
//			mWorkerThread = null;
//		}
//	}
}
