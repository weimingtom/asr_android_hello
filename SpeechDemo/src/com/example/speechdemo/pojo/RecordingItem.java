package com.example.speechdemo.pojo;


import android.os.Parcel;
import android.os.Parcelable;

public class RecordingItem implements Parcelable {
	private int mId;
	private int mLength;
	private long mTime;
	private String mFilePath;
	private String mName;
	private String meetingId;
	private String agendaId;
	private String recType;
	private String recContent;
	private String upStatus;//up status, 0: no, 1: yes
	
	public RecordingItem() {

	}

	public RecordingItem(Parcel in) {
		mId = in.readInt();
		mLength = in.readInt();
		mTime = in.readLong();
		mFilePath = in.readString();
		mName = in.readString();
		meetingId = in.readString();
		agendaId = in.readString();
		recType = in.readString();
		recContent = in.readString();
		upStatus = in.readString();
	}

	public String getFilePath() {
		return mFilePath;
	}

	public void setFilePath(String filePath) {
		mFilePath = filePath;
	}

	public int getLength() {
		return mLength;
	}

	public void setLength(int length) {
		mLength = length;
	}

	public int getId() {
		return mId;
	}

	public void setId(int id) {
		mId = id;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public long getTime() {
		return mTime;
	}

	public void setTime(long time) {
		mTime = time;
	}

	public String getMeetingId() {
		return meetingId;
	}

	public void setMeetingId(String meetingId) {
		this.meetingId = meetingId;
	}

	public String getAgendaId() {
		return agendaId;
	}

	public void setAgendaId(String agendaId) {
		this.agendaId = agendaId;
	}

	public String getRecType() {
		return recType;
	}

	public void setRecType(String recType) {
		this.recType = recType;
	}

	public String getRecContent() {
		return recContent;
	}

	public void setRecContent(String recContent) {
		this.recContent = recContent;
	}

	public String getUpStatus() {
		return upStatus;
	}

	public void setUpStatus(String upStatus) {
		this.upStatus = upStatus;
	}

	public static final Parcelable.Creator<RecordingItem> CREATOR = new Parcelable.Creator<RecordingItem>() {
		public RecordingItem createFromParcel(Parcel in) {
			return new RecordingItem(in); 
		}

		public RecordingItem[] newArray(int size) {
			return new RecordingItem[size];
		}
	};

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mId);
		dest.writeInt(mLength);
		dest.writeLong(mTime);
		dest.writeString(mFilePath);
		dest.writeString(mName);
		dest.writeString(meetingId);
		dest.writeString(agendaId);
		dest.writeString(recType);
		dest.writeString(recContent);
		dest.writeString(upStatus);
	}

	@Override
	public int describeContents() {
		return 0;
	}
}
