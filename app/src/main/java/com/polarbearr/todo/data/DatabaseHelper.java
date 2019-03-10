package com.polarbearr.todo.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

public class DatabaseHelper {
    private static SQLiteDatabase database;
    public static final String TODO_TABLE = "todolist";
    public static final String COMPLETED_TABLE = "completedlist";
    public static final String TODO_ITEM = "todoitem";

    public static final String TITLE_KEY = "titlekey";
    public static final String CONTENT_KEY = "contentkey";
    public static final String ID_KEY = "idkey";
    public static final String DATE_KEY = "datekey";
    public static final String ALARM_TIME_KEY = "alarmtimekey";
    public static final String REPEATABILITY_KEY = "repeatkey";
    public static final String GREATEST_ID_KEY = "greatestidkey";

    // 데이터베이스 오픈
    public static void openDatabase(Context context, String databaseName){
        database = context.openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null);
        if(database != null){
            try{
                System.out.println("데이터베이스 " + databaseName + "오픈됨");
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    // 테이블 생성
    public static void createTable(String tableName){
        String sql = "create table if not exists " + tableName +
                "(" +
                "   _id integer PRIMARY KEY," +
                "   title text, " +
                "   content text, " +
                "   dateValue text, " +
                "   alarmTime text," +
                "   repeatability text" +
                ")";
        if(database != null) {
            database.execSQL(sql);
        }
    }

    // 테이블 전체 조회
    public static Bundle selectAllData(String tableName){
        Bundle data = new Bundle();
        Cursor cursor;

        String sql = "select title, content, _id, dateValue, alarmTime, repeatability from " +
                tableName +
                " order by dateValue";

        if(database != null){
            Bundle bundle;
            cursor = database.rawQuery(sql, null);
            cursor.moveToFirst();

            for(int i = 0; i < cursor.getCount(); i++){
                bundle = new Bundle();
                String title = cursor.getString(0);
                String content = cursor.getString(1);
                int id = cursor.getInt(2);
                String date = cursor.getString(3);
                String alarmTime = cursor.getString(4);
                String repeatability = cursor.getString(5);

                bundle.putString(TITLE_KEY, title);
                bundle.putString(CONTENT_KEY, content);
                bundle.putInt(ID_KEY, id);
                bundle.putString(DATE_KEY, date);
                bundle.putString(ALARM_TIME_KEY, alarmTime);
                bundle.putString(REPEATABILITY_KEY, repeatability);

                data.putBundle(TODO_ITEM + i, bundle);

                cursor.moveToNext();
            }

            cursor.close();
        }
        return data;
    }

    // 행 조회
    public static Bundle selectData(String tableName, int id){
        Bundle data = new Bundle();
        Cursor cursor;

        String sql = "select title, content, dateValue, alarmTime, repeatability from " +
                tableName +
                " where _id = " + id;
        if(database != null) {
            cursor = database.rawQuery(sql, null);
            cursor.moveToFirst();

            String title = cursor.getString(0);
            String content = cursor.getString(1);
            String date = cursor.getString(2);
            String alarmTime = cursor.getString(3);
            String repeatability = cursor.getString(4);

            data.putString(TITLE_KEY, title);
            data.putString(CONTENT_KEY, content);
            data.putString(DATE_KEY, date);
            data.putString(ALARM_TIME_KEY, alarmTime);
            data.putString(REPEATABILITY_KEY, repeatability);
        }
        return data;
    }

    // 행 삽입
    public static void insertData(String tableName, Bundle bundle){
        TodoItem item = bundle.getParcelable(TODO_ITEM);
        String title = item.getTitle();
        String content = item.getContent();
        String date = item.getDate();
        String alarmTime = item.getAlarmTime();
        String repeatability = item.getRepeatability();
        int id = item.getId();

        if(database != null) {
            String sql = "insert into " + tableName + "(title, content, dateValue, alarmTime, repeatability, _id) values(?, ?, ?, ?, ?, ?)";
            Object[] params = {title, content, date, alarmTime, repeatability, id};
            database.execSQL(sql, params);
        }
    }

    // 행 삭제
    public static void deleteData(String tableName, int id){
        if(database != null){
            String sql = "delete from " + tableName + " where _id = " + id;
            database.execSQL(sql);
        }
    }

    // 데이터 업데이트
    public static void updateData(String tableName, Bundle bundle){
        TodoItem item = bundle.getParcelable(TODO_ITEM);
        String title = item.getTitle();
        String content = item.getContent();
        String date = item.getDate();
        String alarmTime = item.getAlarmTime();
        String repeatability = item.getRepeatability();
        int id = item.getId();

        if(database != null){
            String sql = "update " + tableName + " set title = \'" + title + "\', content = \'" + content + "\', dateValue = \'" + date + "\', alarmTime = \'" + alarmTime + "\', repeatability =\'" + repeatability + "\' where _id = " + id;
            database.execSQL(sql);
        }
    }

    public static int selectGreatestId(String tableName){
        String sql = "select _id from " + tableName + " order by _id desc";
        Cursor cursor;
        int id = 0;

        if(database != null){
            cursor = database.rawQuery(sql, null);
            cursor.moveToFirst();
            id = cursor.getInt(0);
        }

        return id;
    }
}
