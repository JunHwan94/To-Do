package com.polarbearr.todo.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import static com.polarbearr.todo.ListFragment.ALARM_TIME_KEY;
import static com.polarbearr.todo.ListFragment.CONTENT_KEY;
import static com.polarbearr.todo.ListFragment.DATE_KEY;
import static com.polarbearr.todo.ListFragment.ID_KEY;
import static com.polarbearr.todo.ListFragment.TITLE_KEY;

public class DatabaseHelper {
    private static SQLiteDatabase database;
    public static final String TODO_TABLE = "todolist";
    public static final String COMPLETED_TABLE = "completedlist";
    public static final String TODO_ITEM = "todoitem";

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
                "   alarmTime text" +
                ")";
        if(database != null) {
            database.execSQL(sql);
        }
    }

    // 테이블 전체 조회
    public static Bundle selectAllData(String tableName){
        Bundle data = new Bundle();
        Cursor cursor;

        String sql = "select title, content, _id, dateValue, alarmTime from " +
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

                bundle.putString(TITLE_KEY, title);
                bundle.putString(CONTENT_KEY, content);
                bundle.putInt(ID_KEY, id);
                bundle.putString(DATE_KEY, date);
                bundle.putString(ALARM_TIME_KEY, alarmTime);

                data.putBundle(TODO_ITEM + i, bundle);

                cursor.moveToNext();
            }

            cursor.close();
        }
        return data;
    }

//    // 행 조회
//    public static Bundle selectData(String tableName, int id){
//        Bundle data = new Bundle();
//        Cursor cursor;
//
//        String sql = "select title, content, dateValue, alarmTime from " +
//                tableName +
//                " where _id = " + id;
//        if(database != null) {
//            cursor = database.rawQuery(sql, null);
//            cursor.moveToFirst();
//
//            String title = cursor.getString(0);
//            String content = cursor.getString(1);
//            String date = cursor.getString(2);
//            String alarmTime = cursor.getString(3);
//
//            data.putString(TITLE_KEY, title);
//            data.putString(CONTENT_KEY, content);
//            data.putString(DATE_KEY, date);
//            data.putString(ALARM_TIME_KEY, alarmTime);
//        }
//        return data;
//    }

    // 행 삽입
    public static void insertData(String tableName, Bundle bundle){
        TodoItem item = bundle.getParcelable(TODO_ITEM);
        String title = item.getTitle();
        String content = item.getContent();
        String date = item.getDate();
        String alarmTime = item.getAlarmTime();
        int id = item.getId();

        if(database != null) {
            String sql = "insert into " + tableName + "(title, content, dateValue, alarmTime, _id) values(?, ?, ?, ?, ?)";
            Object[] params = {title, content, date, alarmTime, id};
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
        int id = item.getId();

        if(database != null){
            String sql = "update " + tableName + " set title = \'" + title + "\', content = \'" + content + "\', dateValue = \'" + date + "\', alarmTime = \'" + alarmTime + "\' where _id = " + id;
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
