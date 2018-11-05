package com.polarbearr.todo;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import static com.polarbearr.todo.ListFragment.CONTENT_KEY;
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
                "   _id integer PRIMARY KEY AUTOINCREMENT," +
//                "   id integer, " +
                "   title text, " +
                "   content text" +
                ")";

        database.execSQL(sql);
    }

    // 데이터 조회
    public static Bundle selectData(String tableName){
        Bundle data = new Bundle();
        Cursor cursor;

        String sql = "select title, content, _id from " +
                tableName +
                " order by _id";

        if(database != null){
            Bundle bundle;
            cursor = database.rawQuery(sql, null);
            cursor.moveToFirst();

            for(int i = 0; i < cursor.getCount(); i++){
                bundle = new Bundle();
                String title = cursor.getString(0);
                String content = cursor.getString(1);
                int id = cursor.getInt(2);

                bundle.putString(TITLE_KEY, title);
                bundle.putString(CONTENT_KEY, content);
                bundle.putInt(ID_KEY, id);

                System.out.println("조회한 데이터 : " + title + " " + content);

                data.putBundle(TODO_ITEM + i, bundle);

                cursor.moveToNext();
            }

            cursor.close();
        }
        return data;
    }

    public static void insertData(String tableName, Bundle bundle){
        TodoItem item = bundle.getParcelable(TODO_ITEM);
        String title = item.title;
        String content = item.content;
        if(database != null) {
            String sql = "insert into " + tableName + "(title, content) values(?, ?)";
            Object[] params = {title, content};
            database.execSQL(sql, params);
        }
    }

    public static void deleteData(String tableName, int id){
        if(database != null){
            String sql = "delete from " + tableName + " where _id = " + id;
            database.execSQL(sql);
        }
    }

    public static void updateData(String tableName, int id, String title, String content){
        if(database != null){
            String sql = "update " + tableName + " set title = \'" + title + "\', content = \'" + content + "\' where _id = " + id;
            database.execSQL(sql);
        }
    }
}
