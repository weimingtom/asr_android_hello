package com.example.speechdemo.dialog;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.speechdemo.R;
import com.example.speechdemo.db.DictationsDatabase;
import com.example.speechdemo.db.RecordingsDatabase;
import com.example.speechdemo.pojo.RecordingItem;
import com.example.speechdemo.util.FucUtil;
import com.example.speechdemo.util.JsonParser;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;

public class RecognizeDialog extends Dialog implements Dialog.OnCancelListener {
	private static final boolean D = false;
	private static final String TAG = "RecognizeDialog";	
	
	private static final boolean USE_BUFFER = true;
	
	public static final String LANG_CHINESE = "mandarin";
	public static final String LANG_CHINESE_GD = "cantonese";
	public static final String LANG_ENGLISH = "en_us";
	
	private Activity mAct;
	private RecordingItem mItem;
	private EditText mTextViewContent;
	private String mMeetingId, mAgendaId;
	private BaseAdapter mAdapter;
	private boolean isIgnoreSave = false;
	
	private DictationsDatabase mDictationDatabase;
	
	public RecognizeDialog(Activity context, final RecordingItem item, String lag_, String meetingId, String agendaId, BaseAdapter adapter) {
		super(context);
		this.mAct = context;
		this.mItem = item;
		this.lag = lag_;
		
		mDictationDatabase = new DictationsDatabase(context);
		
		mMeetingId = meetingId;
		mAgendaId = agendaId;
		mAdapter = adapter;

		// 初始化识别无UI识别对象
		// 使用SpeechRecognizer对象，可根据回调消息自定义界面；
		mIat = SpeechRecognizer.createRecognizer(context, mInitListener);
		mToast = Toast.makeText(context, "", Toast.LENGTH_SHORT); 
		
		if (mIat == null) {
			showTip("mIat == null");
		}
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
		//this.setTitle("语音转换");
        
        
        this.setContentView(R.layout.speech__dialog_recognize);
    
        mTextViewContent = (EditText) this.findViewById(R.id.textViewContent);

        //mTextViewContent.setText("识别中，请稍候...");
        if (mItem != null) {
        	String content = mItem.getRecContent();
        	if (content == null) content = "";
        	mTextViewContent.setText(content);
        	mTextViewContent.setSelection(content.length());
        	mResultText = content;
        }
        
        Button buttonClose = (Button) this.findViewById(R.id.buttonClose);
        buttonClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//RecognizeDialog.this.dismiss();
				cancel();
			}
        });
        Button buttonClipboard = (Button) this.findViewById(R.id.buttonClipboard);
        buttonClipboard.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				copyToClipboard();
			}
        });
        Button buttonCancel = (Button) this.findViewById(R.id.buttonCancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				isIgnoreSave = true;
				cancel();
			}
        });
        
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.setOnCancelListener(this);
        
        //readVoice(mItem.getFilePath());
        Button btnPlayFile = (Button) this.findViewById(R.id.btnPlayFile);
        Button btnListen1 = (Button) this.findViewById(R.id.btnListen1);
        Button btnListen2 = (Button) this.findViewById(R.id.btnListen2);
        Button btnListen3 = (Button) this.findViewById(R.id.btnListen3);
        btnPlayFile.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
    			PlayerDialog playerDialog = new PlayerDialog(mAct, mItem);
    			playerDialog.show();
			}
        });
        btnListen1.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				lag = ListenDialog.LANG_CHINESE;
				readVoice(mItem.getFilePath());
			}
        });
        btnListen2.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				lag = ListenDialog.LANG_CHINESE_GD;
				readVoice(mItem.getFilePath());
			}
        });
        btnListen3.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				lag = ListenDialog.LANG_ENGLISH;
				readVoice(mItem.getFilePath());
			}
        });
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressWarnings("deprecation")
	private void copyToClipboard() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			String content = mTextViewContent.getText().toString();
	        ClipboardManager cm =
	                (ClipboardManager) mAct.getSystemService(Context.CLIPBOARD_SERVICE);
	        cm.setText(content);
	        Toast.makeText(mAct, "「" + content + "」已复制至剪贴板",
	                Toast.LENGTH_SHORT).show();	
		}
	}	
	
	@Override
	public void onCancel(DialogInterface arg0) {
		this.dismiss();
		if (!isIgnoreSave) {
			RecordingsDatabase mDatabase = new RecordingsDatabase(this.getContext());
			String content;
			//content = this.mResultText != null ? this.mResultText.trim() : "";
			content = this.mTextViewContent.getText().toString();
			
			if (mItem != null) {
				mDatabase.updateRecording(mItem.getName(),
						mItem.getFilePath(), mItem.getLength(), this.mMeetingId, this.mAgendaId, "wav", 
						content, mItem.getUpStatus(), mItem.getId());	
			} else {
				mDatabase.addRecording(mItem.getName(),
						mItem.getFilePath(), mItem.getLength(), this.mMeetingId, this.mAgendaId, "wav", 
						content, "0");
			}
			mDatabase.close();
			if (mAdapter != null) {
				mAdapter.notifyDataSetChanged();
			}
		}
		if (mDictationDatabase != null) {
			mDictationDatabase.close();
			mDictationDatabase = null;
		}
	}
	

	private Toast mToast;
	private void showTip(final String str) {
		mToast.setText(str);
		mToast.show();
	}
	/**
	 * 听写监听器。
	 */
	private RecognizerListener mRecognizerListener = new RecognizerListener() {

		@Override
		public void onBeginOfSpeech() {
			// 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
			showTip("开始说话");
		}

		@Override
		public void onError(SpeechError error) {
			// Tips：
			// 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
			// 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
			//if(mTranslateEnable && error.getErrorCode() == 14002) {
			//	showTip( error.getPlainDescription(true)+"\n请确认是否已开通翻译功能" );
			//} else {
				showTip(error.getPlainDescription(true));
			//}
		}

		@Override
		public void onEndOfSpeech() {
			// 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
			showTip("结束说话");
		}

		@Override
		public void onResult(RecognizerResult results, boolean isLast) {
			Log.d(TAG, results.getResultString());
			//if( mTranslateEnable ){
			//	printTransResult( results );
			//}else{
				printResult(results);
			//}
			
			addDictation(mItem, mResultText, RecognizeDialog.this.lag);
			if (isLast) {
				// TODO 最后的结果
			}
		}

		@Override
		public void onVolumeChanged(int volume, byte[] data) {
			showTip("当前正在说话，音量大小：" + volume);
			Log.d(TAG, "返回音频数据："+data.length);
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
			// 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
			// 若使用本地能力，会话id为null
			//	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
			//		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
			//		Log.d(TAG, "session id =" + sid);
			//	}
		}
	};
	
	
	
	
	/**
	 * 初始化监听器。
	 */
	private InitListener mInitListener = new InitListener() {

		@Override
		public void onInit(int code) {
			Log.d(TAG, "SpeechRecognizer init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
				showTip("初始化失败，错误码：" + code);
			}
		}
	};
	
	private String mResultText;
	// 用HashMap存储听写结果
	private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
	// 语音听写对象
	private SpeechRecognizer mIat;
	private int ret = 0; // 函数调用返回值
	
	private void readVoice(String path) {
		mResultText = "";
		mIatResults.clear();
		// 设置参数
		setParam();
		if (USE_BUFFER) {
			// 设置音频来源为外部文件
			mIat.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");
			// 也可以像以下这样直接设置音频文件路径识别（要求设置文件在sdcard上的全路径）：
			// mIat.setParameter(SpeechConstant.AUDIO_SOURCE, "-2");
			// mIat.setParameter(SpeechConstant.ASR_SOURCE_PATH, "sdcard/XXX/XXX.pcm");
			ret = mIat.startListening(mRecognizerListener);
			if (ret != ErrorCode.SUCCESS) {
				showTip("识别失败,错误码：" + ret);
			} else {
				//byte[] audioData = FucUtil.readAudioFile(this.mAct, "iattest.wav", true);
				//byte[] audioData = FucUtil.readAudioFile(this.mAct, "/mnt/sdcard/01.wav", false);
				byte[] audioData = FucUtil.readAudioFile(this.mAct, path, false);
				if (null != audioData) {
					Log.e(TAG, "=============>length : " + audioData.length);
					showTip("开始音频流识别");
					// 一次（也可以分多次）写入音频文件数据，数据格式必须是采样率为8KHz或16KHz（本地识别只支持16K采样率，云端都支持），位长16bit，单声道的wav或者pcm
					// 写入8KHz采样的音频时，必须先调用setParameter(SpeechConstant.SAMPLE_RATE, "8000")设置正确的采样率
					// 注：当音频过长，静音部分时长超过VAD_EOS将导致静音后面部分不能识别。
					// 音频切分方法：FucUtil.splitBuffer(byte[] buffer,int length,int spsize);
					mIat.writeAudio(audioData, 0, audioData.length);
					mIat.stopListening();
				} else {
					mIat.cancel();
					showTip("读取音频流失败");
				}
			}
		} else {
			mIat.setParameter(SpeechConstant.AUDIO_SOURCE, "-2");
			mIat.setParameter(SpeechConstant.ASR_SOURCE_PATH, path);
			ret = mIat.startListening(mRecognizerListener);
			if (ret != ErrorCode.SUCCESS) {
				showTip("识别失败,错误码：" + ret);
			} else {
				mIat.stopListening();
			}
		}
	}
	
	private boolean mTranslateEnable = false;
	private String lag = "mandarin"; //mandarin, cantonese, en_us</item>
	private String iat_vadbos_preference = "10000";//default:"4000"/sms:"5000"/other:"4000"
	private String iat_vadeos_preference = "10000";//default:"1000"/sms:"1800"/other:"700"
	private String iat_punc_preference = "1";
	private String mEngineType = SpeechConstant.TYPE_CLOUD;
	/**
	 * 参数设置
	 * 
	 * @param param
	 * @return
	 */
	public void setParam() {
		// 清空参数
		mIat.setParameter(SpeechConstant.PARAMS, null);

		// 设置听写引擎
		mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
		// 设置返回结果格式
		mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

		
		if (lag.equals("en_us")) {
			// 设置语言
			mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
			mIat.setParameter(SpeechConstant.ACCENT, null);
			
			if( mTranslateEnable ){
				mIat.setParameter( SpeechConstant.ORI_LANG, "en" );
				mIat.setParameter( SpeechConstant.TRANS_LANG, "cn" );
			}
		} else {
			// 设置语言
			mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
			// 设置语言区域
			mIat.setParameter(SpeechConstant.ACCENT, lag);
			
			if( mTranslateEnable ){
				mIat.setParameter( SpeechConstant.ORI_LANG, "cn" );
				mIat.setParameter( SpeechConstant.TRANS_LANG, "en" );
			}
		}

		// 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
		mIat.setParameter(SpeechConstant.VAD_BOS, iat_vadbos_preference);
		
		// 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
		mIat.setParameter(SpeechConstant.VAD_EOS, iat_vadeos_preference);
		
		// 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
		mIat.setParameter(SpeechConstant.ASR_PTT, iat_punc_preference);
		
		// 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
		// 注：AUDIO_FORMAT参数语记需要更新版本才能生效
		mIat.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
		mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/iat.wav");
	}
	
	private void printResult(RecognizerResult results) {
		String text = JsonParser.parseIatResult(results.getResultString());

		String sn = null;
		// 读取json结果中的sn字段
		try {
			JSONObject resultJson = new JSONObject(results.getResultString());
			sn = resultJson.optString("sn");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		mIatResults.put(sn, text);

		StringBuffer resultBuffer = new StringBuffer();
		for (String key : mIatResults.keySet()) {
			resultBuffer.append(mIatResults.get(key));
		}

		mResultText = resultBuffer.toString();
		showTip(mResultText);
		this.mTextViewContent.setText("" + mResultText);
	}
	
	public void addDictation(RecordingItem item, String content, String lang) {
		if (mDictationDatabase != null) {
			mDictationDatabase.addItem(item.getId(), content, lang);
		}
	}
}
