package com.polarbearr.todo;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
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
    private CheckBox dCheckBox;
    private Button timeSelectButton;
    private CheckBox nCheckBox;

    private String title;
    private String content;
    private String date;
    private int id;

    private boolean databaseChangeFlag = false;

    public static final String DATABASE_FLAG_KEY = "dbkey";
    private static final String SELECT_DATE = "-- 날짜 선택 --";
    static final String DATE_NOT_SELECTED = "9999 - 12 - 31";
    static final String INSERT_TYPE = "insert";
    static final String UPDATE_TYPE = "update";
    static final String DATE_TYPE = "date";
    static final String NOTICE_TYPE = "notice";
    static final String PM = "오후";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        Intent intent = getIntent();
        title = intent.getStringExtra(TITLE_KEY);
        content = intent.getStringExtra(CONTENT_KEY);
        date = intent.getStringExtra(DATE_KEY);
        id = intent.getIntExtra(ID_KEY, 0);

        tvTitle = findViewById(R.id.title);
        tvContent = findViewById(R.id.content);
        Button deleteButton = findViewById(R.id.deleteButton);
        Button saveButton = findViewById(R.id.saveButton);
        dateSelectButton = findViewById(R.id.dateSelectButton);
        dCheckBox = findViewById(R.id.dCheckBox);
        timeSelectButton = findViewById(R.id.timeSelectButton);
        nCheckBox = findViewById(R.id.nCheckBox);

        // 새 할일 작성 할 경우
//        if(id == 0) id = intent.getIntExtra(ITEM_COUNT_KEY, 0);

        Toast.makeText(this, "id = " + id, Toast.LENGTH_SHORT).show();
        // 목록의 아이템을 눌러서 WriteActivity 실행했을 때 뷰 처리
        if(id != 0){
            tvTitle.setText(title);
            tvContent.setText(content);
            if(!date.equals(NOTHING)) {
                dateSelectButton.setText(date);
            } else {    // 기한 없을 때 체크박스 미리 체크상태로, 날짜선택 버튼 사용불가상태로
                dCheckBox.setChecked(true);
                dateSelectButton.setEnabled(false);
                dateSelectButton.setTextColor(Color.LTGRAY);
            }
        }
        // + 버튼을 눌러서 WriteActivity 실행했을 때
        else deleteButton.setVisibility(View.INVISIBLE);

        // 저장 버튼 이벤트
        setSaveButtonListener(saveButton);

        // 삭제 버튼 이벤트
        setDeleteButtonListener(deleteButton);

        // 날짜 선택 버튼 이벤트 처리
        setDateSelectButtonListener();

        // 기한 없음 체크박스 클릭이벤트
        setCheckBoxListener(dCheckBox, DATE_TYPE);

        // 시간 선택 버튼 이벤트 처리
        setTimeSelectButtonListener();

        // 알림설정 체크박스 클릭이벤트
        setCheckBoxListener(nCheckBox, NOTICE_TYPE);
    }

    private void setTimeSelectButtonListener(){
        timeSelectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(WriteActivity.this, timeSetListener, 0, 0, DateFormat.is24HourFormat(getBaseContext())).show();
            }

            TimePickerDialog.OnTimeSetListener timeSetListener =
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            String noticeTime = "오전 " + hourOfDay + " : " + minute;
                            if(12 < hourOfDay){
                                int hour = hourOfDay - 12;
                                noticeTime = "오후 " + hour + " : " + minute;
                            }
                            timeSelectButton.setText(noticeTime);
                        }
                    };
        });
    }

    // 체크박스 클릭 이벤트 처리
    private void setCheckBoxListener(final CheckBox checkBox, final String type){
        checkBox.setClickable(true);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (type){
                    case DATE_TYPE:
                        if(checkBox.isChecked()) {
                            dateSelectButton.setEnabled(false);
                            dateSelectButton.setTextColor(Color.LTGRAY);
                        } else {
                            dateSelectButton.setEnabled(true);
                            dateSelectButton.setTextColor(Color.BLACK);
                        }
                        break;
                    case NOTICE_TYPE:
                        if(!checkBox.isChecked()) {
                            timeSelectButton.setEnabled(false);
                            timeSelectButton.setVisibility(View.INVISIBLE);
                        } else {
                            timeSelectButton.setEnabled(true);
                            timeSelectButton.setVisibility(View.VISIBLE);
                            timeSelectButton.setText(R.string.select_time);
                        }
                        break;
                }

            }
        });
    }

    // 날짜 선택 버튼 이벤트 처리
    private void setDateSelectButtonListener() {
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
                databaseChangeFlag = true;
                onBackPressed();
            }
        });
    }

    // 저장버튼 이벤트 처리
    public void setSaveButtonListener(Button saveButton){
        final Bundle giveData = new Bundle();
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title = tvTitle.getText().toString();
                content = tvContent.getText().toString();
                date = dateSelectButton.getText().toString();
                // 기한 없음 체크했을 때
                if(dCheckBox.isChecked()) date = DATE_NOT_SELECTED;

                // 제목 없을 때
                if (title.equals(""))
                        Toast.makeText(getBaseContext(), R.string.typetitle_toast, Toast.LENGTH_SHORT).show();
                // 날짜 선택 안했을 때
                else if(date.equals(SELECT_DATE))
                    Toast.makeText(getBaseContext(), R.string.dateselect_toast, Toast.LENGTH_SHORT).show();
                // 수정할 때
                else if (id != 0) {
                    TodoItem item = new TodoItem(title, content, date, id);
                    processData(item, UPDATE_TYPE);
                    if(nCheckBox.isChecked()){
                        setAlarm();
                    }
                    onBackPressed();
                }
                // 새로운 할일을 작성할 때
                else {
                    TodoItem item = new TodoItem(title, content, date);
                    processData(item, INSERT_TYPE);
                    if(nCheckBox.isChecked()){
                        setAlarm();
                    }
                    onBackPressed();
                }
            }

            public void processData(TodoItem item, final String type){
                giveData.putParcelable(TODO_ITEM, item);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        switch (type){
                            case INSERT_TYPE:
                                DatabaseHelper.insertData(TODO_TABLE, giveData);
                                break;
                            case UPDATE_TYPE:
                                DatabaseHelper.updateData(TODO_TABLE, giveData);
                                break;
                        }
                    }
                }).start();
                databaseChangeFlag = true;
            }
        });
    }

    // 알림 추가
    public void setAlarm(){
        String dateButtonText = dateSelectButton.getText().toString();
        String timeButtonText = timeSelectButton.getText().toString();
        String ap = timeButtonText.split(" ")[0];

        int year = Integer.valueOf(dateButtonText.split(" - ")[0]);
        int month  = Integer.valueOf(dateButtonText.split(" - ")[1]);
        int day = Integer.valueOf(dateButtonText.split(" - ")[2]);
        int hour = Integer.valueOf(timeButtonText.split(" : ")[0].split(" ")[1]);
        if(ap.equals(PM)){
            hour += 12;
        }
        int minute = Integer.valueOf(timeButtonText.split(" : ")[1]);

        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DATE, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        Intent alarmIntent = new Intent(WriteActivity.this, AlarmReceiver.class);
        alarmIntent.putExtra(ID_KEY, id);
        final PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        WriteActivity.this,
                        id,
                        alarmIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT
                );

        final AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        // 현재 아이템의 id로 알람이 설정되어있으면 취소
//        if(pendingIntent != null){
//            pendingIntent =
//                    PendingIntent.getBroadcast(
//                            WriteActivity.this,
//                            id,
//                            alarmIntent,
//                            PendingIntent.FLAG_UPDATE_CURRENT
//                    );
//            if(pendingIntent != null) {
//                alarmManager.cancel(pendingIntent);
//                pendingIntent.cancel();
//            }
//        }

        // 선택한 날짜, 시간으로 알람 설정
        new Thread(new Runnable() {
            @Override
            public void run() {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }
        }).start();


        System.out.println(year + "년 " + month + "월 " +  day + "일 " + hour + "시 " + minute + "분으로 알람 설정됨");
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