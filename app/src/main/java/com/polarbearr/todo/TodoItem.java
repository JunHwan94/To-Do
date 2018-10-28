package com.polarbearr.todo;

import android.os.Parcel;
import android.os.Parcelable;

public class TodoItem implements Parcelable {
    String title;
    String content;
    String date;

    public TodoItem(String title, String content, String date) {
        this.title = title;
        this.content = content;
        this.date = date;
    }

    protected TodoItem(Parcel in) {
        title = in.readString();
        content = in.readString();
        date = in.readString();
    }

    public static final Creator<TodoItem> CREATOR = new Creator<TodoItem>() {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(content);
        dest.writeString(date);
    }
}
