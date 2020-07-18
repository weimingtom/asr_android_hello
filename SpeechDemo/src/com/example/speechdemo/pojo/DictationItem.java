package com.example.speechdemo.pojo;

import com.example.speechdemo.dialog.RecognizeDialog;

import android.os.Parcel;
import android.os.Parcelable;

public class DictationItem implements Parcelable {
	private int id;
	private int recordId;
	private String content;
	private int contentLength;
	private long timeCreated;
	private String lang;
	private String memo;
	
	public String getLangString() {
		String result;
		if (lang == null) {
			lang = "";
		}
		if (lang.equals(RecognizeDialog.LANG_CHINESE)) {
			result = "普通话";
		} else if (lang.equals(RecognizeDialog.LANG_CHINESE_GD)) {
			result = "粤语";
		} else if (lang.equals(RecognizeDialog.LANG_ENGLISH)) {
			result = "英语";
		} else {
			result = lang;
		}
		return result;
	}

	public DictationItem() {

	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getRecordId() {
		return recordId;
	}

	public void setRecordId(int recordId) {
		this.recordId = recordId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	

	public int getContentLength() {
		return contentLength;
	}

	public void setContentLength(int contentLength) {
		this.contentLength = contentLength;
	}

	public long getTimeCreated() {
		return timeCreated;
	}

	public void setTimeCreated(long timeCreated) {
		this.timeCreated = timeCreated;
	}
	
	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}



	public static final Parcelable.Creator<DictationItem> CREATOR = new Parcelable.Creator<DictationItem>() {
		public DictationItem createFromParcel(Parcel in) {
			return new DictationItem(in); 
		}

		public DictationItem[] newArray(int size) {
			return new DictationItem[size];
		}
	};

	public DictationItem(Parcel in) {
		id = in.readInt();
		recordId = in.readInt();
		content = in.readString();
		contentLength = in.readInt();
		timeCreated = in.readLong();
		lang = in.readString();
		memo = in.readString();
	}	
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeInt(recordId);
		dest.writeString(content);
		dest.writeInt(contentLength);
		dest.writeLong(timeCreated);
		dest.writeString(lang);
		dest.writeString(memo);
	}

	@Override
	public int describeContents() {
		return 0;
	}
}
