package com.example.speechdemo.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.example.speechdemo.pojo.RecordingItem;

public class RecordingsDatabase extends SQLiteOpenHelper {
	private static final String TAG = "RecordingsDatabase";
	
	private Context mContext;

	public static final String DATABASE_NAME = "recordings.db";
	private static final int DATABASE_VERSION = 1;

	public static abstract class RecordingDatabaseItem implements BaseColumns {
		public static final String TABLE_NAME = "recordings";

		public static final String COLUMN_NAME_RECORDING_NAME = "recording_name";
		public static final String COLUMN_NAME_RECORDING_FILE_PATH = "file_path";
		public static final String COLUMN_NAME_RECORDING_LENGTH = "length";
		public static final String COLUMN_NAME_TIME_ADDED = "time_added";
		public static final String COLUMN_NAME_REC_TYPE = "recType";
		public static final String COLUMN_NAME_REC_CONTENT = "recContent";
		public static final String COLUMN_NAME_MEETING_ID = "meetingId";
		public static final String COLUMN_NAME_AGENDA_ID = "agendaId";
		public static final String COLUMN_NAME_UP_STATUS = "upStatus";
	}

	public interface OnDatabaseChangedListener {
		void onDatabaseEntryUpdated();
	}

	private OnDatabaseChangedListener mOnDatabaseChangedListener;

	private static final String TEXT_TYPE = " TEXT";
	private static final String COMMA_SEP = ",";
	private static final String SQL_CREATE_ENTRIES =
			"CREATE TABLE " + RecordingDatabaseItem.TABLE_NAME + " (" +
					RecordingDatabaseItem._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
					RecordingDatabaseItem.COLUMN_NAME_RECORDING_NAME + TEXT_TYPE + COMMA_SEP +
					RecordingDatabaseItem.COLUMN_NAME_RECORDING_FILE_PATH + TEXT_TYPE + COMMA_SEP +
					RecordingDatabaseItem.COLUMN_NAME_RECORDING_LENGTH + " INTEGER " + COMMA_SEP +
					RecordingDatabaseItem.COLUMN_NAME_TIME_ADDED + " INTEGER " + COMMA_SEP +
					RecordingDatabaseItem.COLUMN_NAME_REC_TYPE + TEXT_TYPE + COMMA_SEP +
					RecordingDatabaseItem.COLUMN_NAME_REC_CONTENT + TEXT_TYPE + COMMA_SEP +
					RecordingDatabaseItem.COLUMN_NAME_MEETING_ID + TEXT_TYPE + COMMA_SEP +
					RecordingDatabaseItem.COLUMN_NAME_AGENDA_ID + TEXT_TYPE + COMMA_SEP +
					RecordingDatabaseItem.COLUMN_NAME_UP_STATUS + TEXT_TYPE + 
					")";

	@SuppressWarnings("unused")
	private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + RecordingDatabaseItem.TABLE_NAME;

	public RecordingsDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mContext = context;
	}

	public long addRecording(String recordingName, String filePath, long length, String meetingId, String agendaId, String recType, String recContent, String upStatus) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(RecordingDatabaseItem.COLUMN_NAME_RECORDING_NAME, recordingName);
		values.put(RecordingDatabaseItem.COLUMN_NAME_RECORDING_FILE_PATH, filePath);
		values.put(RecordingDatabaseItem.COLUMN_NAME_RECORDING_LENGTH, length);
		values.put(RecordingDatabaseItem.COLUMN_NAME_TIME_ADDED, System.currentTimeMillis());
		values.put(RecordingDatabaseItem.COLUMN_NAME_REC_TYPE, recType);
		values.put(RecordingDatabaseItem.COLUMN_NAME_REC_CONTENT, recContent);
		values.put(RecordingDatabaseItem.COLUMN_NAME_MEETING_ID, meetingId);
		values.put(RecordingDatabaseItem.COLUMN_NAME_AGENDA_ID, agendaId);
		values.put(RecordingDatabaseItem.COLUMN_NAME_UP_STATUS, upStatus);
		
		long rowId = db.insert(RecordingDatabaseItem.TABLE_NAME, null, values);

		Log.e(TAG, "==============addRecording meetingId = " + meetingId + ", agendaId = " + agendaId);
		
		if (mOnDatabaseChangedListener != null)
			mOnDatabaseChangedListener.onDatabaseEntryUpdated();

		return rowId;
	}
	
	public long updateRecording(String recordingName, String filePath, long length, String meetingId, String agendaId, String recType, String recContent, String upStatus, int id) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(RecordingDatabaseItem.COLUMN_NAME_RECORDING_NAME, recordingName);
		values.put(RecordingDatabaseItem.COLUMN_NAME_RECORDING_FILE_PATH, filePath);
		values.put(RecordingDatabaseItem.COLUMN_NAME_RECORDING_LENGTH, length);
		values.put(RecordingDatabaseItem.COLUMN_NAME_TIME_ADDED, System.currentTimeMillis());
		values.put(RecordingDatabaseItem.COLUMN_NAME_REC_TYPE, recType);
		values.put(RecordingDatabaseItem.COLUMN_NAME_REC_CONTENT, recContent);
		values.put(RecordingDatabaseItem.COLUMN_NAME_MEETING_ID, meetingId);
		values.put(RecordingDatabaseItem.COLUMN_NAME_AGENDA_ID, agendaId);
		values.put(RecordingDatabaseItem.COLUMN_NAME_UP_STATUS, upStatus);
		
		long rowId = db.update(RecordingDatabaseItem.TABLE_NAME, values, RecordingDatabaseItem._ID + "=?", new String[]{Integer.toString(id)});

		Log.e(TAG, "==============addRecording meetingId = " + meetingId + ", agendaId = " + agendaId);
		
		if (mOnDatabaseChangedListener != null)
			mOnDatabaseChangedListener.onDatabaseEntryUpdated();

		return rowId;
	}

	public long updateRecordingUpStatus(String upStatus, int id) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(RecordingDatabaseItem.COLUMN_NAME_UP_STATUS, upStatus);
		
		long rowId = db.update(RecordingDatabaseItem.TABLE_NAME, values, RecordingDatabaseItem._ID + "=?", new String[]{Integer.toString(id)});

		if (mOnDatabaseChangedListener != null)
			mOnDatabaseChangedListener.onDatabaseEntryUpdated();

		return rowId;
	}	
	
	public RecordingItem getItemAt(int position, String meetingId, String agendaId) {
		SQLiteDatabase db = getReadableDatabase();

		String[] projection = {
				RecordingDatabaseItem._ID,
				RecordingDatabaseItem.COLUMN_NAME_RECORDING_NAME,
				RecordingDatabaseItem.COLUMN_NAME_RECORDING_FILE_PATH,
				RecordingDatabaseItem.COLUMN_NAME_RECORDING_LENGTH,
				RecordingDatabaseItem.COLUMN_NAME_TIME_ADDED,
				RecordingDatabaseItem.COLUMN_NAME_REC_TYPE,
				RecordingDatabaseItem.COLUMN_NAME_REC_CONTENT,
				RecordingDatabaseItem.COLUMN_NAME_MEETING_ID,
				RecordingDatabaseItem.COLUMN_NAME_AGENDA_ID,
				RecordingDatabaseItem.COLUMN_NAME_UP_STATUS,
		};

		Cursor c = db.query(RecordingDatabaseItem.TABLE_NAME, projection, 
				RecordingDatabaseItem.COLUMN_NAME_MEETING_ID + " = ?", 
				new String[] {meetingId}, 
				null, null, null);
		if (c.moveToPosition(position)) {
			RecordingItem item = new RecordingItem();
			item.setId(c.getInt(c.getColumnIndex(RecordingDatabaseItem._ID)));
			item.setLength(c.getInt(c.getColumnIndex(RecordingDatabaseItem.COLUMN_NAME_RECORDING_LENGTH)));
			item.setFilePath(c.getString(c.getColumnIndex(RecordingDatabaseItem.COLUMN_NAME_RECORDING_FILE_PATH)));
			item.setName(c.getString(c.getColumnIndex(RecordingDatabaseItem.COLUMN_NAME_RECORDING_NAME)));
			item.setTime(c.getLong(c.getColumnIndex(RecordingDatabaseItem.COLUMN_NAME_TIME_ADDED)));
			item.setRecType(c.getString(c.getColumnIndex(RecordingDatabaseItem.COLUMN_NAME_REC_TYPE)));
			item.setRecContent(c.getString(c.getColumnIndex(RecordingDatabaseItem.COLUMN_NAME_REC_CONTENT)));
			item.setMeetingId(c.getString(c.getColumnIndex(RecordingDatabaseItem.COLUMN_NAME_MEETING_ID)));
			item.setAgendaId(c.getString(c.getColumnIndex(RecordingDatabaseItem.COLUMN_NAME_AGENDA_ID)));
			item.setUpStatus(c.getString(c.getColumnIndex(RecordingDatabaseItem.COLUMN_NAME_UP_STATUS)));
			c.close();
			return item;
		}

		return null;
	}

	public void removeItemWithId(int id, String meetingId, String agendaId) {
		SQLiteDatabase db = getWritableDatabase();
		String[] whereArgs = { String.valueOf(id), meetingId };
		db.delete(RecordingDatabaseItem.TABLE_NAME, 
				"_id=? and " + RecordingDatabaseItem.COLUMN_NAME_MEETING_ID + " = ?", 
				whereArgs);

		if (mOnDatabaseChangedListener != null)
			mOnDatabaseChangedListener.onDatabaseEntryUpdated();
	}

	public int getCount(String meetingId, String agendaId) {
		SQLiteDatabase db = getReadableDatabase();
		String[] projection = { RecordingDatabaseItem._ID };
		Cursor c = db.query(RecordingDatabaseItem.TABLE_NAME, projection, 
				RecordingDatabaseItem.COLUMN_NAME_MEETING_ID + " = ?", 
				new String[] {meetingId}, 
				null, null, null);
		int count = c.getCount();
		c.close();
		return count;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_ENTRIES);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// onUpgrade(db, oldVersion, newVersion);
	}

	public Context getContext() {
		return mContext;
	}

//	public class RecordingComparator implements Comparator<RecordingItem> {
//		public int compare(RecordingItem item1, RecordingItem item2) {
//			Long o1 = item1.getTime();
//			Long o2 = item2.getTime();
//			return o2.compareTo(o1);
//		}
//	}

//	public void renameItem(RecordingItem item, String recordingName) {
//		SQLiteDatabase db = getWritableDatabase();
//
//		ContentValues values = new ContentValues();
//		values.put(RecordingDatabaseItem.COLUMN_NAME_RECORDING_NAME, recordingName);
//		db.update(RecordingDatabaseItem.TABLE_NAME, values,
//				RecordingDatabaseItem._ID + "=" + item.getId(), null);
//
//		if (mOnDatabaseChangedListener != null)
//			mOnDatabaseChangedListener.onDatabaseEntryUpdated();
//	}

	public void setOnDatabaseChangedListener(OnDatabaseChangedListener listener) {
		mOnDatabaseChangedListener = listener;
	}
}
