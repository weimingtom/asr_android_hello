/**
 * 
 */
package com.example.speechdemo.activity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fontawesome.example.TextAwesome;
import com.example.speechdemo.R;
import com.example.speechdemo.db.DictationsDatabase;
import com.example.speechdemo.db.RecordingsDatabase;
import com.example.speechdemo.db.RecordingsDatabase.OnDatabaseChangedListener;
import com.example.speechdemo.dialog.ListenDialog;
import com.example.speechdemo.dialog.RecognizeDialog;
import com.example.speechdemo.pojo.RecordingItem;
import com.example.speechdemo.service.RecordingService;
import com.example.speechdemo.service.RecordingService.OnAudioLevelChangedListener;
import com.example.speechdemo.service.RecordingService.OnTimerChangedListener;
import com.example.speechdemo.util.FilePathManager;
import com.example.speechdemo.util.RecordingMode;
import com.iflytek.cloud.SpeechUtility;

/**
 * 主入口
 */
public class RecordingActivity extends Activity implements OnItemClickListener, OnAudioLevelChangedListener, OnTimerChangedListener, OnItemLongClickListener {
	private static final boolean D = false;
	private static final String TAG = "RecordingActivity";
	
	private static Object objIsSpeechLoad = null;
	
	public static final String ACTION_UPLOAD_VOICE = "ACTION_UPLOAD_VOICE";
	
	public static final String EXTRA_MEETING_ID = "EXTRA_MEETING_ID";
	public static final String EXTRA_AGENDA_ID = "EXTRA_AGENDA_ID";
	
	private String meetingId = "";
	private String agendaId = "";
	
	//{
	private static final int REQUEST_CODE_SETTINGS = 0;

	private RecordingService mRecordingService;

	private boolean mRecordingQueued = false;
	private boolean mIsBound = false;
	//}
	
	
	private ListView viewListView;
	private ReaderItemsAdapter adapter;
	private TextView timer_textview;
	private TextView state_view;
	private TextView filename_textview;
	private TextView bars;
	private Button button1, button2; 
	private MediaPlayer mPlayer;
	private LinearLayout viewVoice;
	
	private final static class RetainInfo {
		public List<String> items;
		public List<String> itemInfos1;	
		public List<String> itemInfos2;	
	}
	
	private final static class ReaderItemsAdapter extends BaseAdapter implements OnDatabaseChangedListener {
		private LayoutInflater mInflater;

		private Context mContext;
		private RecordingsDatabase mDatabase;
		private static final SimpleDateFormat mDateAddedFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		private static final SimpleDateFormat mDateFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());		
		
		private String mMeetingId;
		private String mAgendaId;
		
		public ReaderItemsAdapter(Context context, String meetingId, String agendaId) {
			this.mInflater = LayoutInflater.from(context);
			mContext = context;
			mDatabase = new RecordingsDatabase(context);
			mDatabase.setOnDatabaseChangedListener(this);
			mMeetingId = meetingId;
			mAgendaId = agendaId;
		}
		
		@Override
		public int getCount() {
			if (mDatabase != null) {
				return mDatabase.getCount(this.mMeetingId, this.mAgendaId);
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return mDatabase.getItemAt(position, this.mMeetingId, this.mAgendaId);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
           ViewHolder holder;
            if (convertView == null) {
				convertView = mInflater.inflate(R.layout.speech__list_item_recording, null);
				holder = new ViewHolder();
				holder.title = (TextView) convertView.findViewById(R.id.title);
				holder.date = (TextView) convertView.findViewById(R.id.date);
				holder.iconRecord = (TextAwesome) convertView.findViewById(R.id.iconRecord);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
    		RecordingItem item = (RecordingItem)getItem(position);
            if (item != null) {
            	if (item.getRecType() != null && item.getRecType().equals("text")) {
            		holder.title.setText(item.getRecContent());
            		holder.iconRecord.setText(R.string.fa_book);
            	} else { //"wav"
            		//holder.title.setText("录音文件：" + item.getName());
            		holder.title.setText("录音文件");
            		holder.iconRecord.setText(R.string.fa_music);
            	}
            	holder.date.setText(getTime(item.getTime()));
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
        	TextAwesome iconRecord;
        }

		@Override
		public void onDatabaseEntryUpdated() {
			this.notifyDataSetChanged();
		}
		
		public void remove(RecordingItem item) {
			//尝试删除录音文件
			try {
				String path = item.getFilePath();
				if (path != null && path.length() > 0) {
					new File(path).delete();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			mDatabase.removeItemWithId(item.getId(), this.mMeetingId, this.mAgendaId);
		}
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.speech__activity_recording); //list_reader
		
		if (RecordingActivity.objIsSpeechLoad == null) {
			RecordingActivity.objIsSpeechLoad = new Object();
			SpeechUtility.createUtility(this, "appid=" + getString(R.string.xunfei_app_id));
		}
		
		this.setTitle("准备录音");
		
		Intent intent = this.getIntent();
		if (intent != null) {
			this.meetingId = intent.getStringExtra(EXTRA_MEETING_ID);
			this.agendaId = intent.getStringExtra(EXTRA_AGENDA_ID);
		}
		if (this.meetingId == null) {
			this.meetingId = "";
		}
		if (this.agendaId == null) {
			this.agendaId = "";
		}
		
		viewListView = (ListView) this.findViewById(R.id.viewListView);
		timer_textview = (TextView) this.findViewById(R.id.timer_textview);
		state_view = (TextView) this.findViewById(R.id.state_view);
		filename_textview = (TextView) this.findViewById(R.id.filename_textview);
		bars = (TextView) this.findViewById(R.id.bars);
		button1 = (Button) this.findViewById(R.id.button1); 
		button2 = (Button) this.findViewById(R.id.button2); 
		viewVoice = (LinearLayout) this.findViewById(R.id.viewVoice);
		
		RetainInfo info = (RetainInfo) this.getLastNonConfigurationInstance();
		if (info == null) {
			
		} else {
		
		}
		
		adapter = new ReaderItemsAdapter(this, this.meetingId, this.agendaId);
		viewListView.setAdapter(adapter);
		viewListView.setFastScrollEnabled(true);
		viewListView.setOnItemClickListener(this);
		viewListView.setOnItemLongClickListener(this);
		
		onCreate2(savedInstanceState);
		
		receiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_UPLOAD_VOICE);
		this.registerReceiver(receiver, filter);
	}
	
	public class MyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				if (ACTION_UPLOAD_VOICE.equals(intent.getAction())) {
					runOnUiThread(new UiUpdaterUpload());
				}
			}
		}
	}
	
	private class UiUpdaterUpload implements Runnable {
		public UiUpdaterUpload() {
			
		}

		@Override
		public void run() {
			if (adapter != null) adapter.notifyDataSetChanged();
		}
	}
	
    private MyReceiver receiver;
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (this.receiver != null) {
			this.unregisterReceiver(receiver);
		}
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
		RecordingItem item = (RecordingItem)adapter.getItem(position);
		if (item != null) {
        	if (item.getRecType() != null && item.getRecType().equals("text")) {
        		ListenDialog listenDialog = new ListenDialog(RecordingActivity.this, RecordingActivity.this.meetingId, RecordingActivity.this.agendaId, adapter, item);
        		listenDialog.show();
        	} else {
    			//PlayerDialog playerDialog = new PlayerDialog(this, item);
    			//playerDialog.show();
        		RecognizeDialog recogizeDialog = new RecognizeDialog(RecordingActivity.this, item, RecognizeDialog.LANG_CHINESE, RecordingActivity.this.meetingId, RecordingActivity.this.agendaId, adapter);
        		recogizeDialog.show();
        	}

		}
	}
	
	//-----------------------------------------


	private BroadcastReceiver mStateChangedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (RecordingService.INTENT_RECORDING_STARTED.equals(intent.getAction())) {
				String filename = intent.getStringExtra("filename");
				state_view.setText("正在录音");mRecordingMode=RecordingMode.RECORDING;//mRecordingStatusFragment.setRecordingMode(RecordingMode.RECORDING);
				filename_textview.setText(filename.replace(".pcm", "")); //mRecordingStatusFragment.setFileName(filename.replace(".pcm", ""));
				button1.setText("停止录音"); //mRecordingControlsFragment.onRecordingStateChanged(RecordingMode.RECORDING);
				viewVoice.setVisibility(View.VISIBLE);
				viewListView.setVisibility(View.GONE);
				//getActionBar().setTitle(R.string.state_recording);
				if (adapter != null) adapter.notifyDataSetChanged();
			} else if (RecordingService.INTENT_RECORDING_STOPPED.equals(intent.getAction())) {
				state_view.setText("准备录音");mRecordingMode=RecordingMode.IDLE;//mRecordingStatusFragment.setRecordingMode(RecordingMode.IDLE);
				button1.setText("开始录音"); //mRecordingControlsFragment.onRecordingStateChanged(RecordingMode.IDLE);
				viewVoice.setVisibility(View.GONE);
				viewListView.setVisibility(View.VISIBLE);
				//getActionBar().setTitle(R.string.app_name);
				if (adapter != null) adapter.notifyDataSetChanged();
			}
		}
	};

	
	private RecordingMode mRecordingMode = RecordingMode.IDLE;
	public RecordingMode getRecordingMode() {
		return this.mRecordingMode;
	}
	private int mSeconds;
	private static final SimpleDateFormat mDateFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
	public void setTimeFromSeconds(int seconds) {
		mSeconds = seconds;
		if (timer_textview != null)
			timer_textview.setText(mDateFormat.format(seconds*1000));
	}
	@Override
	public void onTimerChanged(final int seconds) {
		this.runOnUiThread(new Runnable() {	
			@Override
			public void run() {
				setTimeFromSeconds(seconds);
			}
		});
	}
	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mRecordingService = ((RecordingService.ServiceBinder)service).getService();
			mRecordingService.setOnTimerChangedListener(RecordingActivity.this);
			mRecordingService.setOnAudioLevelChanged(RecordingActivity.this);
			
			mRecordingService.setMeetingId(RecordingActivity.this.meetingId);
			mRecordingService.setAgendaId(RecordingActivity.this.agendaId); //FIXME:暂时未设置议程
			
			if (mRecordingQueued) {
				mRecordingService.startRecording();
				mRecordingQueued = false;
			}

			if (mRecordingService.isRecording()) {
				state_view.setText("正在录音");mRecordingMode=RecordingMode.RECORDING;//mRecordingStatusFragment.setRecordingMode(RecordingMode.RECORDING);
				filename_textview.setText(mRecordingService.getFilename().replace(".pcm", "")); //mRecordingStatusFragment.setFileName(mRecordingService.getFilename().replace(".pcm", ""));
				button1.setText("正在录音"); //mRecordingControlsFragment.onRecordingStateChanged(RecordingMode.RECORDING);
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			mRecordingService = null;
		}
	};

	private void doBindService() {
		bindService(new Intent(RecordingActivity.this, 
				RecordingService.class), mConnection, Context.BIND_AUTO_CREATE);

		mIsBound = true;
	}

	private void doUnbindService() {
		if (mIsBound) {
			unbindService(mConnection);
			mIsBound = false;
		}
	}

	protected void onCreate2(Bundle savedInstanceState) {
		startService(new Intent(this, RecordingService.class));
		doBindService();

		if (savedInstanceState == null) {
			button1.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (adapter != null) adapter.notifyDataSetChanged();
					
					RecordingMode mode;
					if (mRecordingService == null)
						mode = getRecordingMode();
					else
						mode = mRecordingService.getRecordingMode();
					switch (mode) {
					case IDLE:
						//if (mRecordingStatusFragment != null) {
						//	mRecordingStatusFragment.setTimeFromSeconds(0);
						//	mRecordingStatusFragment.clearAudioBars();
						//}
						if (RecordingActivity.this.timer_textview != null) {
							RecordingActivity.this.setTimeFromSeconds(0);
						}
						if (RecordingActivity.this.bars != null) {
							RecordingActivity.this.bars.setText("");
						}
						MediaPlayer openPlayer = MediaPlayer.create(getApplicationContext(), R.raw.speech__open);
						if (openPlayer == null) {
							startRecording();
						} else {
							openPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
								@Override
								public void onCompletion(MediaPlayer mp) {
									startRecording();
								}
							});
							openPlayer.start();
						}
						break;
					case RECORDING:
					default:
						mRecordingService.stopRecording();
						MediaPlayer successPlayer = MediaPlayer.create(getApplicationContext(), R.raw.speech__success);
						if (successPlayer != null) {
							successPlayer.start();
						}
						break;
					}
				}
			});
			
			
			button2.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ListenDialog dialog = new ListenDialog(RecordingActivity.this, RecordingActivity.this.meetingId, RecordingActivity.this.agendaId, adapter, null);
					dialog.show();
				}
			});
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	protected void startRecording() {
		if (mRecordingService != null)
			mRecordingService.startRecording();
		else
			mRecordingQueued = true;
	}

	@Override
	protected void onPause() {
		unregisterReceiver(mStateChangedReceiver);
		doUnbindService();
		if (mRecordingService.getRecordingMode() == RecordingMode.IDLE) {
			mRecordingService.stopSelf();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter iF = new IntentFilter();
		iF.addAction(RecordingService.INTENT_RECORDING_STARTED);
		iF.addAction(RecordingService.INTENT_RECORDING_STOPPED);
		registerReceiver(mStateChangedReceiver, iF);

		if (mRecordingService == null)
			return;

		//if (mRecordingStatusFragment != null)
		//	mRecordingStatusFragment.setRecordingMode(mRecordingService.getRecordingMode());
		if (state_view != null) {
			state_view.setText(mRecordingService.getRecordingMode() == RecordingMode.RECORDING ? "正在录音" : "准备录音");mRecordingMode=mRecordingService.getRecordingMode();
		}
		
		//if (mRecordingControlsFragment != null)
		//	mRecordingControlsFragment.onRecordingStateChanged(mRecordingService.getRecordingMode());
		if (this.button1 != null) {
			this.button1.setText(mRecordingService.getRecordingMode() == RecordingMode.RECORDING ? "停止录音" : "开始录音");// "正在录音" : "准备录音");
		}
		
		if (mRecordingService.getRecordingMode() == RecordingMode.IDLE)
			this.setTitle("准备录音");
		else if (mRecordingService.getRecordingMode() == RecordingMode.RECORDING)
			this.setTitle("正在录音");
	}

	@Override
	public void onAudioLevelChanged(final int percentage) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				//if (mRecordingControlsFragment != null)
				//	mRecordingControlsFragment.onAudioLevelChanged(percentage);
				
				
				//if (mRecordingStatusFragment != null)
				//	mRecordingStatusFragment.onAudioLevelChanged(percentage);
				RecordingActivity.this.bars.setText("AudioLevel:" + percentage);
			}
		});
	}

	public void setPrettyName(String string) {
		mRecordingService.setNextPrettyRecordingName(string);
		this.filename_textview.setText(string); //mRecordingStatusFragment.setFileName(string);
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> av, View v, int position, long id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final RecordingItem item = (RecordingItem)adapter.getItem(position);
    	if (item.getRecType() != null 
    			&& item.getRecType().equals("text")) {
    		builder.setTitle("同步识别结果")
    		.setItems(new String[] {
    			"上传", //0
    			"复制到剪贴板", //1
    			"共享至...", //2
    			"-----", //3
    			"删除", //4
    		}, new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int which) {
    				if (item != null) {
    					switch (which) {
    					case 0:
    						if (item.getUpStatus() != null && item.getUpStatus().equals("1")) {
    							Toast.makeText(RecordingActivity.this, 
    								"已上传", Toast.LENGTH_SHORT)
    								.show();    							
    						} else {
    							uploadText(item);
    						}
    						break;
    					
    					case 1:
    						copyToClipboard(item);
    						break;
    					
    					case 2:
    						Intent intent;
    						intent = new Intent();
    						intent.setAction(Intent.ACTION_SEND);
    						intent.setType("text/plain");
    						intent.putExtra(Intent.EXTRA_SUBJECT, "语音转换结果：" + ReaderItemsAdapter.getTime(item.getTime()));
    						intent.putExtra(Intent.EXTRA_TEXT, item.getRecContent());
    			            try {
    			            	//startActivity(Intent.createChooser(intent, "共享方式"));
    			            	startActivity(intent);
    			            } catch (Throwable e) {
    							e.printStackTrace();
    							Toast.makeText(RecordingActivity.this, 
    								"共享方式出错", Toast.LENGTH_SHORT)
    								.show();
    						}
    						break;
    					
    					case 4:
    						adapter.remove(item);
    						break;						
    					}
    				}
    			}
    		});
    	} else { //"wav"
    		builder.setTitle(item.getName())
    		.setItems(new String[] {
				"上传", //0
				"转换记录", //1
//				"转换为文字(普通话)",
//				"转换为文字(粤语)",
//				"转换为文字(英语)",
    			"----", //2
    			//"清除重复转换记录",
    			"清除转换记录", //3
    			"删除", //4
    		}, new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int which) {
    				if (item != null) {
    					switch (which) {
    					case 0:
    						if (item.getUpStatus() != null && item.getUpStatus().equals("1")) {
    							Toast.makeText(RecordingActivity.this, 
    								"已上传", Toast.LENGTH_SHORT)
    								.show();    							
    						} else {
    							upload(item);
    						}
    						break;
    						
    					case 1:
    						clearHistoryRepeat(item);
    						startActivity(new Intent(RecordingActivity.this, DictResultActivity.class).putExtra(DictResultActivity.EXTRA_RECORDING_ID, item.getId()));						
    						break;
    						
//    					case 1: {
//    							//Toast.makeText(MainActivity.this, item.getFilePath(), Toast.LENGTH_SHORT).show();
//    							RecognizeDialog recogizeDialog = new RecognizeDialog(RecordingActivity.this, item, RecognizeDialog.LANG_CHINESE);
//    							recogizeDialog.show();
//    						}
//    						break;
//    					
//    					case 2: {
//    							//Toast.makeText(MainActivity.this, item.getFilePath(), Toast.LENGTH_SHORT).show();
//    							RecognizeDialog recogizeDialog = new RecognizeDialog(RecordingActivity.this, item, RecognizeDialog.LANG_CHINESE_GD);
//    							recogizeDialog.show();
//    						}
//    						break;
//    						
//    					case 3: {
//    							//Toast.makeText(MainActivity.this, item.getFilePath(), Toast.LENGTH_SHORT).show();
//    							RecognizeDialog recogizeDialog = new RecognizeDialog(RecordingActivity.this, item, RecognizeDialog.LANG_ENGLISH);
//    							recogizeDialog.show();
//    						}
//    						break;

//    					case 4:
//    						clearHistoryRepeat(item);
//    						break;

    						
    					case 3:
    						clearHistory(item);
    						break;
    						
    					case 4:
    						adapter.remove(item);
    						break;						
    					}
    				}
    			}
    		});
    	}		
		builder.show();
		
		return true;
	}
	
	public String getRootPath() {
		String str = FilePathManager.getFilePathManager(this).getAppDirPath();
		if (str == null || str.trim().length() == 0) {
			return null;
		} else {
			return str;
		}
	}	
	
	private File generateTextFile(RecordingItem item) {
		String filename = getRootPath();//Environment.getExternalStorageDirectory().getAbsolutePath();
		filename += File.separator + "textrecording";

		File dir = new File(filename);
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				// failed to create dir
				Toast.makeText(getApplicationContext(), "创建目录失败", Toast.LENGTH_SHORT).show();
				return null; 
			}
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale
    			.getDefault());
		String currentDateandTime = sdf.format(new Date(item.getTime()));
		File file = new File(dir, "TEXT_" + currentDateandTime + "_" + item.getId() +".txt");
//		int count = 1;
//		while (file.exists()) {
//			file = new File(dir, "TEXT_" + currentDateandTime + "_" + String.valueOf(count) + ".txt");
//			if (count > 1000) {
//				break;
//			}
//		}

		return file;
	}	
	
	public void clearHistoryRepeat(RecordingItem item) {
		DictationsDatabase mDatabase = new DictationsDatabase(this);
		mDatabase.removeItemShort(item.getId());
		mDatabase.close();
	}
	
	private void clearHistory(RecordingItem item) {
		DictationsDatabase mDatabase = new DictationsDatabase(this);
		mDatabase.removeAllItems();
		mDatabase.close();
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressWarnings("deprecation")
	private void copyToClipboard(final RecordingItem item) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			String content = item.getRecContent();
	        ClipboardManager cm =
	                (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
	        cm.setText(content);
	        Toast.makeText(this, "「" + content + "」已复制至剪贴板",
	                Toast.LENGTH_SHORT).show();	
		}
	}
	
	private void uploadText(RecordingItem item) {
		String vdate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale
    			.getDefault()).format(new Date(item.getTime()));
		String type = "";
		if (item.getRecType() != null) {
			if (item.getRecType().equals("text")) {
				type = "1";
			} else if (item.getRecType().equals("wav")) {
				type = "2";
			}
		}
		if (D) {
			Log.e(TAG, "===============>pid:" + item.getMeetingId() + ", aid:" + item.getAgendaId());
		}
		String agendaId = item.getAgendaId();
		if (agendaId == null || agendaId.equals("")) {
			agendaId = "0";
		}
		File file = generateTextFile(item);
		String path = "";
		if (file != null) {
			path = file.getAbsolutePath();
			putFileString(path, item.getRecContent());
		}
		uploadFile(path, "0", item.getName(), type, item.getRecContent(), 
				item.getMeetingId(), agendaId, 
				vdate, Integer.toString(item.getLength() / 1000), item.getId());
	}
	
	private static void putFileString(String filename, String str) {
		FileOutputStream outstr = null;
		OutputStreamWriter writer = null;
		BufferedWriter buffer = null;
		try {
			outstr = new FileOutputStream(filename);
			writer = new OutputStreamWriter(outstr, "utf-8");
			buffer = new BufferedWriter(writer);
			buffer.write(str != null ? str : "");
			buffer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (buffer != null) {
				try {
					buffer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (outstr != null) {
				try {
					outstr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void upload(RecordingItem item) {
		String vdate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale
    			.getDefault()).format(new Date(item.getTime()));
		String type = "";
		if (item.getRecType() != null) {
			if (item.getRecType().equals("text")) {
				type = "1";
			} else if (item.getRecType().equals("wav")) {
				type = "2";
			}
		}
		if (D) {
			Log.e(TAG, "===============>pid:" + item.getMeetingId() + ", aid:" + item.getAgendaId());
		}
		String agendaId = item.getAgendaId();
		if (agendaId == null || agendaId.equals("")) {
			agendaId = "0";
		}
		uploadFile(item.getFilePath(), "0", item.getName(), type, item.getRecContent(), 
				item.getMeetingId(), agendaId, 
				vdate, Integer.toString(item.getLength() / 1000), item.getId());
	}
	
	public void uploadFile(String path, String oid, String title, String type, String note, String pid, String aid, String vdate, String secs, int recid) {
		
	}
}
