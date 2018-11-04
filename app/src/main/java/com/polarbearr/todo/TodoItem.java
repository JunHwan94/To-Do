package com.polarbearr.todo;

import android.os.Parcel;
import android.os.Parcelable;

public class TodoItem implements Parcelable {
    String title;
    String content;
    int id;


    public TodoItem(String title, String content) {
        this.title = title;
        this.content = content;
    }

    // DB에서 조회할 때는 id 값 넣어주기
    public TodoItem(String title, String content, int id) {
        this.title = title;
        this.content = content;
        this.id = id;
    }


    protected TodoItem(Parcel in) {
        title = in.readString();
        content = in.readString();
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(content);
    }
}
