package com.polarbearr.todo;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

public class DatabaseHelper {
    private static SQLiteDatabase database;
    public static final String TODO_TABLE = "todolist";
    public static final String COMPLETED_TABLE = "completedlist";
    public static final String TITLE = "title";
    public static final String CONTENT = "content";
    public static final String DATEVALUE = "datevalue";
    public static final String TODOITEM = "todoitem";

//    private static String createTableTotoListSql = "create table if not exists " + TODO_TABLE +
//            "(" +
//            "   _id integer PRIMARY KEY AUTOINCREMENT," +
//            "   id integer, " +
//            "   title text, " +
//            "   content text, " +
//            "   dateValue text" +
//            ")";
//
//    private static String createTableCompletedListSql = "create table if not exists " + COMPLETED_TABLE +
//            "(" +
//            "   _id integer PRIMARY KEY AUTOINCREMENT," +
//            "   id integer, " +
//            "   title text, " +
//            "   content text, " +
//            "   dateValue text" +
//            ")";

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
                "   _id integer PRIMARY KEY AUTOINCREMENT," +
                "   id integer, " +
                "   title text, " +
                "   content text, " +
                "   dateValue text" +
                ")";

        database.execSQL(sql);
//        switch(tableName){
//            case TODO_TABLE:
//                database.execSQL(createTableTotoListSql);
//                break;
//            case COMPLETED_TABLE:
//                database.execSQL(createTableCompletedListSql);
//                break;
//        }
    }

    //
    public static Bundle selectData(String tableName){
        Bundle data = new Bundle();
        Cursor cursor;

        String sql = "select title, content, dateValue from " +
                tableName +
                " order by id desc";

        if(database != null){
            Bundle bundle = new Bundle();
            cursor = database.rawQuery(sql, null);
            cursor.moveToFirst();

            for(int i = 0; i < cursor.getCount(); i++){
                String title = cursor.getString(0);
                String content = cursor.getString(1);
                String dateValue = cursor.getString(2);

                bundle.putString(TITLE, title);
                bundle.putString(CONTENT, content);
                bundle.putString(DATEVALUE, dateValue);

                data.putBundle(TODOITEM + i, bundle);
            }

            cursor.close();
            System.out.println("데이터베이스 조회됨");
        }

        return data;
    }

    public static void insertData(String tableName, Bundle bundle){
        TodoItem item = bundle.getParcelable(TODOITEM);
        String title = item.title;
        String content = item.content;
        String dateValue = item.date;
        if(database != null) {
            String sql = "insert into " + tableName + "(title, content, dateValue) values(?, ?, ?)";
            Object[] params = {title, content, dateValue};
            database.execSQL(sql, params);
        }
    }

    public static void deleteData(String tableName, String title){
        if(database != null){
            String sql = "delete from " + tableName + " where title=" + title;
        }
    }
}
