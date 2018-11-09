package com.polarbearr.todo;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.polarbearr.todo.DatabaseHelper.TODO_ITEM;
import static com.polarbearr.todo.DatabaseHelper.TODO_TABLE;
import static com.polarbearr.todo.ListFragment.CONTENT_KEY;
import static com.polarbearr.todo.ListFragment.DATE_KEY;
import static com.polarbearr.todo.ListFragment.ID_KEY;
import static com.polarbearr.todo.ListFragment.ITEM_COUNT_KEY;
import static com.polarbearr.todo.ListFragment.NOTHING;
import static com.polarbearr.todo.ListFragment.TITLE_KEY;

public class WriteActivity extends AppCompatActivity {
    private EditText tvTitle;
    private EditText tvContent;
    private Button dateSelectButton;
    private CheckBox checkBox;

    private String title;
    private String content;
    private String date;
    private int id;

    private boolean saveFlag = false;
    private boolean databaseChangeFlag = false;

    public static final String DATABASE_FLAG_KEY = "dbkey";
    private static final String SELECT_DATE = "== 날짜 선택 ==";
    static final String DATE_NOT_SELECTED = "9999 - 12 - 31";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        Intent intent = getIntent();

        tvTitle = findViewById(R.id.title);
        tvContent = findViewById(R.id.content);
        checkBox = findViewById(R.id.checkBox);

        title = intent.getStringExtra(TITLE_KEY);
        content = intent.getStringExtra(CONTENT_KEY);
        date = intent.getStringExtra(DATE_KEY);
        id = intent.getIntExtra(ID_KEY, 0);

        // 새 할일 작성 할 경우
        if(id == 0) id = intent.getIntExtra(ITEM_COUNT_KEY, 0);

        // 체크박스 클릭이벤트
        setCheckBoxListener(checkBox);

        Button deleteButton = findViewById(R.id.deleteButton);
        dateSelectButton = findViewById(R.id.dateSelectButton);
        // 목록의 아이템을 눌러서 WriteActivity 실행했을 때 뷰 처리
        if(id != 0){
            tvTitle.setText(title);
            tvContent.setText(content);
            if(!date.equals(NOTHING)) dateSelectButton.setText(date);
        }
        // + 버튼을 눌러서 WriteActivity 실행했을 때
        else deleteButton.setVisibility(View.INVISIBLE);

        Button saveButton = findViewById(R.id.saveButton);
        setSaveButtonListener(saveButton);
        setDeleteButtonListener(deleteButton);

        // 날짜 선택 버튼 이벤트 처리
        setDateSelectButtonListener(dateSelectButton);
    }

    // 체크박스 클릭 이벤트 처리
    private void setCheckBoxListener(final CheckBox checkBox){
        checkBox.setClickable(true);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkBox.isChecked()) {
                    dateSelectButton.setEnabled(false);
                    dateSelectButton.setTextColor(Color.LTGRAY);
                } else {
                    dateSelectButton.setEnabled(true);
                    dateSelectButton.setTextColor(Color.BLACK);
                }
            }
        });
    }

    // 날짜 선택 버튼 이벤트 처리
    private void setDateSelectButtonListener(final Button dateSelectButton) {
        final String buttonText = dateSelectButton.getText().toString();
        dateSelectButton.setOnClickListener(new View.OnClickListener() {
            Calendar cal = new GregorianCalendar();
            int sYear = cal.get(Calendar.YEAR);
            int sMonth = cal.get(Calendar.MONTH);
            int sDay = cal.get(Calendar.DAY_OF_MONTH);

            @Override
            public void onClick(View v) {
                if(!buttonText.equals(SELECT_DATE)){
                    sYear = Integer.valueOf(buttonText.split(" - ")[0]);
                    sMonth = Integer.valueOf(buttonText.split(" - ")[1]) - 1;
                    sDay = Integer.valueOf(buttonText.split(" - ")[2]);
                }
                new DatePickerDialog(WriteActivity.this, dateSetListener, sYear, sMonth, sDay).show();
            }

            DatePickerDialog.OnDateSetListener dateSetListener =
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            sYear = year;
                            sMonth = month;
                            sDay = dayOfMonth;
                            String date;
                            if (sMonth < 10) {
                                date = sYear + " - 0" + (sMonth + 1) + " - " + sDay;
                                if (sDay < 10)
                                    date = sYear + " - 0" + (sMonth + 1) + " - 0" + sDay;
                                } else {
                                date = sYear + " - " + (sMonth + 1) + " - " + sDay;
                                if (sDay < 10)
                                    date = sYear + " - " + (sMonth + 1) + " - 0" + sDay;
                                }
                                dateSelectButton.setText(date);
                        }
                    };
        });
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
                tvTitle.clearFocus();
                tvContent.clearFocus();
                hideKeyboard(activity);

                title = tvTitle.getText().toString();
                content = tvContent.getText().toString();
                date = dateSelectButton.getText().toString();
                // 기한 없음 체크했을 때
                if(checkBox.isChecked()) date = DATE_NOT_SELECTED;

                // 날짜 선택 안했을 때
                if(date.equals(SELECT_DATE))
                    Toast.makeText(getBaseContext(), "날짜를 선택하세요", Toast.LENGTH_SHORT).show();
                // 제목 없을 때
                else if (title.equals(""))
                        Toast.makeText(getBaseContext(), R.string.notsave_toast, Toast.LENGTH_SHORT).show();
                // 수정할 때
                else if (id != 0 || saveFlag == true) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            DatabaseHelper.updateData(TODO_TABLE, id, title, content, date);
                        }
                    }).start();
                    databaseChangeFlag = true;
                    Toast.makeText(getBaseContext(), R.string.save_toast, Toast.LENGTH_SHORT).show();
//                    onBackPressed();
                }
                // 새로운 할일을 작성할 때
                else {
                    TodoItem item = new TodoItem(title, content, date);
                    giveData.putParcelable(TODO_ITEM, item);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            DatabaseHelper.insertData(TODO_TABLE, giveData);
                        }
                    }).start();
                    databaseChangeFlag = true;
                    saveFlag = true;
                    Toast.makeText(getBaseContext(), R.string.save_toast, Toast.LENGTH_SHORT).show();
//                   onBackPressed();
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
        intent.putExtra(DATABASE_FLAG_KEY, databaseChangeFlag);
        setResult(RESULT_OK, intent);
        finish();
    }
}