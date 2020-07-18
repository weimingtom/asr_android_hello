package com.example.speechdemo.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.example.speechdemo.pojo.DictationItem;

public class DictationsDatabase extends SQLiteOpenHelper {
	private static final boolean D = false;
	private static final String TAG = "DictationsDatabase";
	
	public static final String DATABASE_NAME = "dictations.db";
	private static final int DATABASE_VERSION = 1;	
	
	
	public static abstract class DictationDatabaseItem implements BaseColumns {
		public static final String TABLE_NAME = "dictations";

		public static final String COLUMN_NAME_RECORD_ID = "record_id";
		public static final String COLUMN_NAME_CONTENT = "content";
		public static final String COLUMN_NAME_CONTENT_LENGTH = "content_length";
		public static final String COLUMN_NAME_TIME_CREATED = "time_created";
		public static final String COLUMN_NAME_LANG = "lang";
		public static final String COLUMN_NAME_MEMO = "memo";
	}
	
	private Context mContext;
	
	private static final String TEXT_TYPE = " TEXT ";
	private static final String COMMA_SEP = ",";
	private static final String SQL_CREATE_ENTRIES =
			"CREATE TABLE " + DictationDatabaseItem.TABLE_NAME + " (" +
					DictationDatabaseItem._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
					DictationDatabaseItem.COLUMN_NAME_RECORD_ID + TEXT_TYPE + COMMA_SEP +
					DictationDatabaseItem.COLUMN_NAME_CONTENT + TEXT_TYPE + COMMA_SEP +
					DictationDatabaseItem.COLUMN_NAME_CONTENT_LENGTH + " INTEGER " + COMMA_SEP +
					DictationDatabaseItem.COLUMN_NAME_TIME_CREATED + " INTEGER " + COMMA_SEP +
					DictationDatabaseItem.COLUMN_NAME_LANG + TEXT_TYPE + COMMA_SEP +
					DictationDatabaseItem.COLUMN_NAME_MEMO + TEXT_TYPE + 					
					")";
	
	private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + DictationDatabaseItem.TABLE_NAME;
	
	public DictationsDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mContext = context;
	}

	public long addItem(int recordId, String content, String lang) {
		Log.e(TAG, "==================addItem : " + recordId + ", " + content);
		
		if (content == null) {
			content = "";
		}
		
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DictationDatabaseItem.COLUMN_NAME_RECORD_ID, recordId);
		values.put(DictationDatabaseItem.COLUMN_NAME_CONTENT, content);
		values.put(DictationDatabaseItem.COLUMN_NAME_CONTENT_LENGTH, content.length());
		values.put(DictationDatabaseItem.COLUMN_NAME_TIME_CREATED, System.currentTimeMillis());
		values.put(DictationDatabaseItem.COLUMN_NAME_LANG, lang);
		values.put(DictationDatabaseItem.COLUMN_NAME_MEMO, "");
		
		if (mOnDatabaseChangedListener != null)
			mOnDatabaseChangedListener.onDatabaseEntryUpdated();		
		
		long rowId = db.insert(DictationDatabaseItem.TABLE_NAME, null, values);

		return rowId;
	}
	
	public DictationItem getItemAt(int position, int recordId) {
		SQLiteDatabase db = getReadableDatabase();

		String[] projection = {
				DictationDatabaseItem._ID,
				DictationDatabaseItem.COLUMN_NAME_RECORD_ID, 
				DictationDatabaseItem.COLUMN_NAME_CONTENT,
				DictationDatabaseItem.COLUMN_NAME_CONTENT_LENGTH,
				DictationDatabaseItem.COLUMN_NAME_TIME_CREATED,
				DictationDatabaseItem.COLUMN_NAME_LANG,
				DictationDatabaseItem.COLUMN_NAME_MEMO
		};

		Cursor c = db.query(DictationDatabaseItem.TABLE_NAME, projection, 
				DictationDatabaseItem.COLUMN_NAME_RECORD_ID + " = ?", new String[] {Integer.toString(recordId)},  
				null, null, null);
		if (c.moveToPosition(position)) {
			DictationItem item = new DictationItem();
			item.setId(c.getInt(c.getColumnIndex(DictationDatabaseItem._ID)));
			item.setRecordId(c.getInt(c.getColumnIndex(DictationDatabaseItem.COLUMN_NAME_RECORD_ID)));
			item.setContent(c.getString(c.getColumnIndex(DictationDatabaseItem.COLUMN_NAME_CONTENT)));
			item.setContentLength(c.getInt(c.getColumnIndex(DictationDatabaseItem.COLUMN_NAME_CONTENT_LENGTH)));
			item.setTimeCreated(c.getLong(c.getColumnIndex(DictationDatabaseItem.COLUMN_NAME_TIME_CREATED)));
			item.setLang(c.getString(c.getColumnIndex(DictationDatabaseItem.COLUMN_NAME_LANG)));
			item.setMemo(c.getString(c.getColumnIndex(DictationDatabaseItem.COLUMN_NAME_MEMO)));
			c.close();
			return item;
		}

		return null;
	}
	
	public void removeItemWithId(int id, int recordId) {
		SQLiteDatabase db = getWritableDatabase();
		String[] whereArgs = { String.valueOf(id), Integer.toString(recordId)};
		db.delete(DictationDatabaseItem.TABLE_NAME, 
				"_id=? and " + DictationDatabaseItem.COLUMN_NAME_RECORD_ID + " = ?", 
				whereArgs);
		
		if (mOnDatabaseChangedListener != null)
			mOnDatabaseChangedListener.onDatabaseEntryUpdated();
	}

	public void removeAllItems() {
		SQLiteDatabase db = getWritableDatabase();
		db.delete(DictationDatabaseItem.TABLE_NAME, null, null);
		
		if (mOnDatabaseChangedListener != null)
			mOnDatabaseChangedListener.onDatabaseEntryUpdated();
	}	
	
	public void removeItemShort(int recordId) {
		SQLiteDatabase db = getWritableDatabase();
		
		String[] projection = {
				DictationDatabaseItem._ID,
				DictationDatabaseItem.COLUMN_NAME_RECORD_ID, 
				DictationDatabaseItem.COLUMN_NAME_CONTENT,
				DictationDatabaseItem.COLUMN_NAME_CONTENT_LENGTH,
				DictationDatabaseItem.COLUMN_NAME_TIME_CREATED,
				DictationDatabaseItem.COLUMN_NAME_LANG,
				DictationDatabaseItem.COLUMN_NAME_MEMO
		};

		List<DictationItem> items = new ArrayList<DictationItem>();
		Cursor c = db.query(DictationDatabaseItem.TABLE_NAME, projection, 
				DictationDatabaseItem.COLUMN_NAME_RECORD_ID + " = ?", new String[] {Integer.toString(recordId)},  
				null, null, DictationDatabaseItem.COLUMN_NAME_CONTENT_LENGTH + " asc");
		while (c.moveToNext()) {
			DictationItem item = new DictationItem();
			item.setId(c.getInt(c.getColumnIndex(DictationDatabaseItem._ID)));
			item.setRecordId(c.getInt(c.getColumnIndex(DictationDatabaseItem.COLUMN_NAME_RECORD_ID)));
			item.setContent(c.getString(c.getColumnIndex(DictationDatabaseItem.COLUMN_NAME_CONTENT)));
			item.setContentLength(c.getInt(c.getColumnIndex(DictationDatabaseItem.COLUMN_NAME_CONTENT_LENGTH)));
			item.setTimeCreated(c.getLong(c.getColumnIndex(DictationDatabaseItem.COLUMN_NAME_TIME_CREATED)));
			item.setLang(c.getString(c.getColumnIndex(DictationDatabaseItem.COLUMN_NAME_LANG)));
			item.setMemo(c.getString(c.getColumnIndex(DictationDatabaseItem.COLUMN_NAME_MEMO)));
			items.add(item);
		}
		c.close();
		
		List<DictationItem> itemsRemoved = new ArrayList<DictationItem>();
		for (int i = 0; i < items.size(); i++) {
			DictationItem item1 = items.get(i);
			if (item1.getContent() == null || item1.getContent().length() == 0) {
				itemsRemoved.add(item1);
				continue;
			}
			String content1 = item1.getContent();
			for (int k = i+1; k < items.size(); k++) {
				DictationItem item2 = items.get(k);
				if (item2.getContent() == null || item2.getContent().length() == 0) {
					continue;
				}
				String content2 = item2.getContent();
				if (content2.contains(content1)) {
					itemsRemoved.add(item1);
					break;
				}
			}
		}
		
		String ids = "0";
		for (DictationItem item : itemsRemoved) {
			ids += "," + item.getId();
		}
		String[] whereArgs = { Integer.toString(recordId)};
		db.delete(DictationDatabaseItem.TABLE_NAME, 
				"_id in (" + ids + ") and " + DictationDatabaseItem.COLUMN_NAME_RECORD_ID + " = ?", 
				whereArgs);
		
		if (mOnDatabaseChangedListener != null)
			mOnDatabaseChangedListener.onDatabaseEntryUpdated();
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_ENTRIES);		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

	public Context getContext() {
		return mContext;
	}
	
	//FIXME:
	public int getCount(int recordId) {
		SQLiteDatabase db = getReadableDatabase();
		String[] projection = { DictationDatabaseItem._ID };
		Cursor c = db.query(DictationDatabaseItem.TABLE_NAME, projection, 
				DictationDatabaseItem.COLUMN_NAME_RECORD_ID + " = ?", new String[] {Integer.toString(recordId)}, 
				null, null, null);
		int count = c.getCount();
		c.close();
		return count;
	}
	
	public interface OnDatabaseChangedListener {
		void onDatabaseEntryUpdated();
	}
	private OnDatabaseChangedListener mOnDatabaseChangedListener;
	
	public void setOnDatabaseChangedListener(OnDatabaseChangedListener listener) {
		mOnDatabaseChangedListener = listener;
	}
}
