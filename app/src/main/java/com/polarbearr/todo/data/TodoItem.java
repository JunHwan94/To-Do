package com.polarbearr.todo.data;

import android.os.Parcel;
import android.os.Parcelable;

public class TodoItem implements Parcelable {
    private String title;
    private String content;
    private int id;
    private String date;
    private String alarmTime;
    private String repeatability;

    public TodoItem(String title, String content, String date, String alarmTime, String repeatability, int id) {
        this.title = title;
        this.content = content;
        this.date = date;
        this.alarmTime = alarmTime;
        this.repeatability = repeatability;
        this.id = id;
    }

    protected TodoItem(Parcel in) {
        title = in.readString();
        content = in.readString();
        date = in.readString();
        alarmTime = in.readString();
        repeatability = in.readString();
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        @Override
        public TodoItem createFromParcel(Parcel in) {
            return new TodoItem(in);
        }

        @Override
        public TodoItem[] newArray(int size) {
            return new TodoItem[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAlarmTime() {
        return alarmTime;
    }

    public void setAlarmTime(String alarmTime) {
        this.alarmTime = alarmTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRepeatability() {
        return repeatability;
    }

    public void setRepeatability(String repeatability) {
        this.repeatability = repeatability;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(content);
        dest.writeString(date);
        dest.writeString(alarmTime);
        dest.writeString(repeatability);
    }
}
