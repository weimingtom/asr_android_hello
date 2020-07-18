package com.example.speechdemo.activity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.speechdemo.R;
import com.example.speechdemo.db.DictationsDatabase;
import com.example.speechdemo.db.DictationsDatabase.OnDatabaseChangedListener;
import com.example.speechdemo.pojo.DictationItem;

public class DictResultActivity extends Activity implements OnItemClickListener, OnItemLongClickListener {
	private static final boolean D = false;
	private static final String TAG = "DictResultActivity";
	
	public static final String EXTRA_RECORDING_ID = "EXTRA_RECORDING_ID";
	
	private ListView viewListView;
	private ReaderItemsAdapter adapter;
	private int mRecordId = 0;
	
	private final static class RetainInfo {
		public List<String> items;
		public List<String> itemInfos1;	
		public List<String> itemInfos2;	
	}
	
	private final static class ReaderItemsAdapter extends BaseAdapter implements OnDatabaseChangedListener {
		private LayoutInflater mInflater;

		private Context mContext;
		private DictationsDatabase mDatabase;
		private static final SimpleDateFormat mDateAddedFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		private static final SimpleDateFormat mDateFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());		
		private int _recordId = 0;
		
		public ReaderItemsAdapter(Context context, int recordId) {
			this.mInflater = LayoutInflater.from(context);
			mContext = context;
			mDatabase = new DictationsDatabase(context);
			mDatabase.setOnDatabaseChangedListener(this);
			_recordId = recordId;
		}
		
		@Override
		public int getCount() {
			if (mDatabase != null) {
				return mDatabase.getCount(_recordId);
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return mDatabase.getItemAt(position, _recordId);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
           ViewHolder holder;
            if (convertView == null) {
				convertView = mInflater.inflate(R.layout.speech__list_item_dict_result, null);
				holder = new ViewHolder();
				holder.title = (TextView) convertView.findViewById(R.id.title);
				holder.date = (TextView) convertView.findViewById(R.id.date);
				holder.lang = (TextView) convertView.findViewById(R.id.lang);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            DictationItem item = (DictationItem)getItem(position);
            if (item != null) {
            	holder.title.setText(item.getContent());
        		holder.date.setText(getTime(item.getTimeCreated()));
        		holder.lang.setText("转换语言：" + item.getLangString());
        		//lengthView.setText(getLengthString(item.getLength()));
        	} else {
            	holder.title.setText("");
            }
            return convertView;
		}
		
		public static String getTime(long milliSeconds) {
			Date date = new Date(milliSeconds);
			return mDateAddedFormatter.format(date);
		}
		
        private static final class ViewHolder {
        	TextView title;
        	TextView date;
        	TextView lang;
        }

		@Override
		public void onDatabaseEntryUpdated() {
			this.notifyDataSetChanged();
		}
		
		public void remove(DictationItem item) {
			mDatabase.removeItemWithId(item.getId(), _recordId);
		}
	}
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.speech__activity_dictresult);
		
		this.setTitle("转换结果");
		
		Intent intent = this.getIntent();
		if (intent != null) {
			mRecordId = intent.getIntExtra(EXTRA_RECORDING_ID, 0);
		}
		
		viewListView = (ListView) this.findViewById(R.id.viewListView);
		
		RetainInfo info = (RetainInfo) this.getLastNonConfigurationInstance();
		if (info == null) {
			
		} else {
		
		}
		
		adapter = new ReaderItemsAdapter(this, mRecordId);
		viewListView.setAdapter(adapter);
		viewListView.setFastScrollEnabled(true);
		viewListView.setOnItemClickListener(this);
		viewListView.setOnItemLongClickListener(this);
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
        	finish();
        	return true;
        }
        return super.onOptionsItemSelected(item);
    }
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("alreadyStarted", true);
	}
	
    @Override
	public Object onRetainNonConfigurationInstance() {
    	RetainInfo info = new RetainInfo();
    	return info;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (D) {
			Log.d(TAG, "onItemClick " + position);
		}
		DictationItem item = (DictationItem)adapter.getItem(position);
		if (item != null) {
			Intent intent;
			intent = new Intent();
			intent.setAction(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_SUBJECT, "语音转换结果：" + ReaderItemsAdapter.getTime(item.getTimeCreated()));
			intent.putExtra(Intent.EXTRA_TEXT, item.getContent());
            try {
            	//startActivity(Intent.createChooser(intent, "共享方式"));
            	startActivity(intent);
            } catch (Throwable e) {
				e.printStackTrace();
				Toast.makeText(this, 
					"共享方式出错", Toast.LENGTH_SHORT)
					.show();
			}
		}
	}

	
	@Override
	public boolean onItemLongClick(AdapterView<?> av, View v, int position, long id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final DictationItem item = (DictationItem)adapter.getItem(position);
		builder.setTitle("操作")
		.setItems(new String[] {
			"删除"
		}, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (item != null) {
					switch (which) {
					case 0:
						adapter.remove(item);
						break;
					}
				}
			}
		});
		builder.show();
		return true;
	}
	
}
