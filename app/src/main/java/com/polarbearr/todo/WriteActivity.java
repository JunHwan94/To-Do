package com.polarbearr.todo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static com.polarbearr.todo.DatabaseHelper.TODO_ITEM;
import static com.polarbearr.todo.DatabaseHelper.TODO_TABLE;
import static com.polarbearr.todo.ListFragment.CONTENT_KEY;
import static com.polarbearr.todo.ListFragment.TITLE_KEY;

public class WriteActivity extends AppCompatActivity {
    Bundle giveData;
    EditText titleText;
    EditText contentText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        Intent intent = getIntent();

        titleText = findViewById(R.id.title);
        contentText = findViewById(R.id.content);
        Button saveButton = findViewById(R.id.saveButton);

        giveData = new Bundle();
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = titleText.getText().toString();
                String content = contentText.getText().toString();
                TodoItem item = new TodoItem(title, content);
                giveData.putParcelable(TODO_ITEM, item);

                try {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            DatabaseHelper.insertData(TODO_TABLE, giveData);
                        }
                    }).start();
                } catch(Exception e){
                    e.printStackTrace();
                } finally{
                    Toast.makeText(getBaseContext(), R.string.save_toast, Toast.LENGTH_SHORT).show();
                }
            }
        });

        String title = intent.getStringExtra(TITLE_KEY);
        String content = intent.getStringExtra(CONTENT_KEY);
//        int id = intent.getIntExtra(ID_KEY, 0);
        if(title != null){
            titleText.setText(title);
            contentText.setText(content);
        }
    }
}
