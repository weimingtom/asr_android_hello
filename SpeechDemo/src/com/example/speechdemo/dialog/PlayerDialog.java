package com.example.speechdemo.dialog;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.speechdemo.R;
import com.example.speechdemo.pojo.RecordingItem;
import com.example.speechdemo.view.RoundButton;
import com.example.speechdemo.view.RoundButton.Type;

public class PlayerDialog extends Dialog implements Dialog.OnCancelListener {
	private MediaPlayer mPlayer;
	private Activity mAct;
	private RecordingItem mItem;
	private TextView mCurrentPositionTextView;
	private SeekBar mCurrentPositionSeekBar;

	public PlayerDialog(Activity context, final RecordingItem item) {
		super(context);
		this.mAct = context;
		this.mItem = item;
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        
        this.setContentView(R.layout.speech__dialog_playback);
        
		final RoundButton pauseButton = (RoundButton) this.findViewById(R.id.pause_button);
		pauseButton.setType(Type.PAUSE);
		//pauseButton.setType(Type.PLAY);

		final RoundButton stopButton = (RoundButton) this.findViewById(R.id.stop_button);
		stopButton.setType(Type.STOP);
		stopButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mPlayer.pause();
				mPlayer.seekTo(0);
				stopButton.setEnabled(false);
				pauseButton.setType(Type.PLAY);
				stopTimer();
			}
		});
		
		mPlayer = new MediaPlayer();
		try {
			mPlayer.setDataSource(mItem.getFilePath());
			mPlayer.prepare();
			mPlayer.start();
			startTimer();
		} catch (Exception e) {
			e.printStackTrace();
		}
		mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer arg0) {
				stopButton.setEnabled(false);
				pauseButton.setType(Type.PLAY);
				stopTimer();
				mCurrentPositionSeekBar.setProgress(mCurrentPositionSeekBar.getMax());
				mCurrentPositionTextView.setText(getTimeText(mItem.getLength()));
			}
		});

		pauseButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				stopButton.setEnabled(true);
				if (mPlayer.isPlaying()) {
					mPlayer.pause();
					pauseButton.setType(Type.PLAY);
					stopTimer();
				} else {
					mPlayer.start();
					pauseButton.setType(Type.PAUSE);
					startTimer();
				}
			}
		});

		TextView recordingName = (TextView) this.findViewById(R.id.recording_name_textview);
		recordingName.setText(mItem.getName());

		mCurrentPositionSeekBar = (SeekBar) this.findViewById(R.id.seek_bar);
		mCurrentPositionSeekBar.setMax(mPlayer.getDuration());
		mCurrentPositionSeekBar.setProgress(mPlayer.getCurrentPosition());
		mCurrentPositionSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) { }
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) { }
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (!fromUser)
					return;

				mPlayer.seekTo(progress);
			}
		});
		mCurrentPositionTextView = (TextView) this.findViewById(R.id.current_position);
		updateSeekBar();

		TextView duration = (TextView) this.findViewById(R.id.total_duration);
		duration.setText(getTimeText(mItem.getLength()));

		TextView timeAdded = (TextView) this.findViewById(R.id.time_added_textview);
		timeAdded.setVisibility(View.GONE);

		this.setOnCancelListener(this);
	}
	
	private Timer mTimer;
	private TimerTask mProgressTimerTask;
	private void startTimer() {
		mTimer = new Timer();
		mProgressTimerTask = new TimerTask() {
			@Override
			public void run() {
				mAct.runOnUiThread(new Runnable() {	
					@Override
					public void run() {
						updateSeekBar();
					}
				});
			}
		};
		mTimer.scheduleAtFixedRate(mProgressTimerTask, 500, 500);
	}
	private void stopTimer() {
		if (mProgressTimerTask != null) {
			mProgressTimerTask.cancel();
			mProgressTimerTask = null;
		}
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}

		updateSeekBar();
	}
	private static final String getTimeText(int position) {
		SimpleDateFormat mDateFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());
		return mDateFormat.format(position);
	}
	private void updateSeekBar() {
		if (mCurrentPositionTextView != null && mPlayer != null) {
			int position = 0;
			try {
				position = mPlayer.getCurrentPosition();
			} catch (Throwable t) {

			}
			mCurrentPositionTextView.setText(getTimeText(position));
			mCurrentPositionSeekBar.setProgress(position);
		}
	}

	@Override
	public void onCancel(DialogInterface arg0) {
		mPlayer.stop();
		mPlayer.release();
		mPlayer = null;
		mCurrentPositionTextView = null;
		mCurrentPositionSeekBar = null;
		stopTimer();
		this.dismiss();
	}
}
