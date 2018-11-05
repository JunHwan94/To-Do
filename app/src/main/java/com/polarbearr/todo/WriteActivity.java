package com.polarbearr.todo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static com.polarbearr.todo.DatabaseHelper.TODO_ITEM;
import static com.polarbearr.todo.DatabaseHelper.TODO_TABLE;
import static com.polarbearr.todo.ListFragment.CONTENT_KEY;
import static com.polarbearr.todo.ListFragment.ID_KEY;
import static com.polarbearr.todo.ListFragment.TITLE_KEY;

public class WriteActivity extends AppCompatActivity {
    private EditText tvTitle;
    private EditText tvContent;

    private String title;
    private String content;
    private int id;

    private boolean saveFlag = false;
    private boolean databaseChangeFlag = false;

    public static final String DATABASE_FLAG_KEY = "dbkey";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        Intent intent = getIntent();

        tvTitle = findViewById(R.id.title);
        tvContent = findViewById(R.id.content);

        title = intent.getStringExtra(TITLE_KEY);
        content = intent.getStringExtra(CONTENT_KEY);
        id = intent.getIntExtra(ID_KEY, 0);

        Button deleteButton = findViewById(R.id.deleteButton);
        // 목록의 아이템을 눌러서 넘어왔을 때 뷰 처리
        if(id != 0){
            tvTitle.setText(title);
            tvContent.setText(content);
        } else deleteButton.setVisibility(View.INVISIBLE);

        Button saveButton = findViewById(R.id.saveButton);
        setSaveButtonListener(saveButton);
        setDeleteButtonListener(deleteButton);
    }

    // 삭제버튼 이벤트 처리
    public void setDeleteButtonListener(Button deleteButton){
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        DatabaseHelper.deleteData(TODO_TABLE, id);
                    }
                }).start();
//                Toast.makeText(getBaseContext(), R.string.delete_toast, Toast.LENGTH_SHORT).show();
                databaseChangeFlag = true;
                onBackPressed();
            }
        });
    }

    // 저장버튼 이벤트 처리
    public void setSaveButtonListener(Button saveButton){
        final Bundle giveData = new Bundle();
        final Activity activity = this;
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(activity);

                title = tvTitle.getText().toString();
                content = tvContent.getText().toString();

                // 작성 내용이 없을 때
                if(title.equals("") && content.equals("")){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            DatabaseHelper.deleteData(TODO_TABLE, id);
                        }
                    }).start();
                    Toast.makeText(getBaseContext(), R.string.notsave_toast, Toast.LENGTH_SHORT).show();
                } else if(id != 0){  // 수정할 때
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            DatabaseHelper.updateData(TODO_TABLE, id, title, content);
                        }
                    }).start();
                    databaseChangeFlag = true;
//                    Toast.makeText(getBaseContext(), R.string.save_toast, Toast.LENGTH_SHORT).show();
                    onBackPressed();
                } else {    // 새로운 할일을 작성할 때
                    TodoItem item = new TodoItem(title, content);
                    giveData.putParcelable(TODO_ITEM, item);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            DatabaseHelper.insertData(TODO_TABLE, giveData);
                        }
                    }).start();
                    databaseChangeFlag = true;
//                    Toast.makeText(getBaseContext(), R.string.save_toast, Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
            }
        });
    }

    // 키보드 숨기기
    public void hideKeyboard(Activity activity){
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if(view != null){
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // 인텐트에 dataChangeFlag 넣어서 전달
        intent.putExtra(DATABASE_FLAG_KEY, databaseChangeFlag);
        setResult(RESULT_OK, intent);
        finish();
    }
}