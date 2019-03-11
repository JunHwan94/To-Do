package com.polarbearr.todo;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.polarbearr.todo.data.DatabaseHelper;
import com.polarbearr.todo.data.TodoItem;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.polarbearr.todo.data.DatabaseHelper.COMPLETED_TABLE;
import static com.polarbearr.todo.data.DatabaseHelper.REPEATABILITY_KEY;
import static com.polarbearr.todo.data.DatabaseHelper.TITLE_KEY;
import static com.polarbearr.todo.data.DatabaseHelper.CONTENT_KEY;
import static com.polarbearr.todo.data.DatabaseHelper.ID_KEY;
import static com.polarbearr.todo.data.DatabaseHelper.DATE_KEY;
import static com.polarbearr.todo.data.DatabaseHelper.ALARM_TIME_KEY;
import static com.polarbearr.todo.data.DatabaseHelper.GREATEST_ID_KEY;
import static com.polarbearr.todo.data.DatabaseHelper.TODO_ITEM;
import static com.polarbearr.todo.data.DatabaseHelper.TODO_TABLE;
import static com.polarbearr.todo.ListFragment.NOTHING;

public class WriteActivity extends AppCompatActivity {
    private EditText tvTitle;
    private EditText tvContent;
    private Button dateSelectButton;
    private CheckBox dCheckBox;
    private static Button timeSelectButton;
    private CheckBox nCheckBox;
    private Spinner spinner;

    private Calendar cal;
    private String title;
    private String content;
    private String date;
    private String alarmTime;
    private String repeatability;
    private int id;
    private boolean isNew = true;
    private boolean databaseChangeFlag = false;
    private int fragmentType;

    public static final String DATABASE_FLAG_KEY = "dbkey";
    public static final String TYPE_KEY = "type";
    private static final String SELECT_DATE = "날짜 선택";
    private static final String SELECT_TIME = "시간 선택";
    static final String DATE_NOT_SELECTED = "9999 - 12 - 31";
    static final String INSERT_TYPE = "insert";
    static final String UPDATE_TYPE = "update";
    static final String DATE_TYPE = "date";
    static final String NOTICE_TYPE = "notice";
    static final String AM = "오전";
    static final String PM = "오후";
    static final String NO_REPEAT = "반복 안함";
    static final String EVERY_DAY = "매일";
    static final String EVERY_WEEK = "매주";
    static final String EVERY_MONTH = "매월";
    static final String EVERY_YEAR = "매년";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        tvTitle = findViewById(R.id.title);
        tvContent = findViewById(R.id.content);
        Button deleteButton = findViewById(R.id.deleteButton);
        Button saveButton = findViewById(R.id.saveButton);
        Button completeButton = findViewById(R.id.completeButton);
        Button restoreButton = findViewById(R.id.restoreButton);
        Button backButton = findViewById(R.id.backButton);
        dateSelectButton = findViewById(R.id.dateSelectButton);
        dCheckBox = findViewById(R.id.dCheckBox);
        timeSelectButton = findViewById(R.id.timeSelectButton);
        nCheckBox = findViewById(R.id.nCheckBox);
        spinner = findViewById(R.id.spinner);

        Intent intent = getIntent();
        title = intent.getStringExtra(TITLE_KEY);
        content = intent.getStringExtra(CONTENT_KEY);
        date = intent.getStringExtra(DATE_KEY);
        alarmTime = intent.getStringExtra(ALARM_TIME_KEY);
        id = intent.getIntExtra(ID_KEY, 0);
        fragmentType = intent.getIntExtra(TYPE_KEY, 0);
        repeatability = intent.getStringExtra(REPEATABILITY_KEY);

        String[] items = {NO_REPEAT, EVERY_DAY, EVERY_WEEK, EVERY_MONTH, EVERY_YEAR};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                repeatability = items[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 푸시 알람 터치해서 열릴 시
//        if(title == null){
//            Bundle bundle = DatabaseHelper.selectData(TODO_TABLE, id);
//            title = bundle.getString(TITLE_KEY);
//            content = bundle.getString(CONTENT_KEY);
//            date = bundle.getString(DATE_KEY);
//            alarmTime = bundle.getString(ALARM_TIME_KEY);
//            repeatability = bundle.getString(REPEATABILITY_KEY);
//        }

        // 목록의 아이템을 눌러서 WriteActivity 실행했을 때 뷰 처리
        if(id != 0){
            isNew = false;
            tvTitle.setText(title);
            tvContent.setText(content);

            // 기한 있는 할일이면
            if(!date.equals(NOTHING)) {
                dateSelectButton.setText(date);
                timeSelectButton.setText(alarmTime);
                nCheckBox.setVisibility(View.VISIBLE);
                // 알람 설정돼있으면 알림설정 체크박스 미리 체크
                if(!alarmTime.equals(SELECT_TIME)) {
                    nCheckBox.setChecked(true);
                    timeSelectButton.setEnabled(true);  // 시간 선택 버튼 활성
                    timeSelectButton.setVisibility(View.VISIBLE);
                    spinner.setVisibility(View.VISIBLE); // 스피너도 보이게

                    int spIndex = 0;
                    switch(repeatability){
                        case EVERY_DAY:
                            spIndex = 1;
                            break;
                        case EVERY_WEEK:
                            spIndex = 2;
                            break;
                        case EVERY_MONTH:
                            spIndex = 3;
                            break;
                        case EVERY_YEAR:
                            spIndex = 4;
                            break;
                    }
                    spinner.setSelection(spIndex);
                }
            } else {    // 기한 없을 때 기한없음 체크박스 미리 체크상태로, 날짜선택 버튼 사용불가상태로
                dCheckBox.setChecked(true);
                dateSelectButton.setEnabled(false);
                dateSelectButton.setTextColor(Color.LTGRAY);
                nCheckBox.setEnabled(false);
                nCheckBox.setTextColor(Color.LTGRAY);
            }
        }
        // + 버튼을 눌러서 WriteActivity 실행했을 때
        else {
            id = intent.getIntExtra(GREATEST_ID_KEY, 0) + 1;
            deleteButton.setVisibility(View.INVISIBLE);
            completeButton.setVisibility(View.INVISIBLE);
        }

        switch(fragmentType){
            case 0: // 할일 이면
                restoreButton.setVisibility(View.INVISIBLE);
                break;
            case 1: // 완료한 일이면
                tvTitle.setEnabled(false);
                tvContent.setEnabled(false);
                tvContent.setHint("");
                dateSelectButton.setEnabled(false);
                dCheckBox.setEnabled(false);
                nCheckBox.setVisibility(View.INVISIBLE);
                completeButton.setVisibility(View.INVISIBLE);
                saveButton.setVisibility(View.INVISIBLE);

                if(dCheckBox.isChecked())
                    dateSelectButton.setVisibility(View.INVISIBLE);
                else dCheckBox.setVisibility(View.INVISIBLE);
                break;
        }

        // 저장 버튼 이벤트
        setSaveButtonListener(saveButton);

        // 삭제 버튼 이벤트
        setDeleteButtonListener(deleteButton);

        // 완료, 미완료 버튼 이벤트
        setCompleteOrNotButtonListener(completeButton);
        setCompleteOrNotButtonListener(restoreButton);

        // 날짜 선택 버튼 이벤트 처리
        setDateSelectButtonListener();

        // 기한 없음 체크박스 클릭이벤트
        setCheckBoxListener(dCheckBox, DATE_TYPE);

        // 시간 선택 버튼 이벤트 처리
        setTimeSelectButtonListener();

        // 알림설정 체크박스 클릭이벤트
        setCheckBoxListener(nCheckBox, NOTICE_TYPE);

        // 뒤로가기 버튼 이벤트
        backButton.setOnClickListener( v-> onBackPressed() );
    }

    // 시간 선택 버튼 이벤트 처리
    private void setTimeSelectButtonListener(){
        timeSelectButton.setOnClickListener(new View.OnClickListener() {
            int sHour;
            int sMinute;
            @Override
            public void onClick(View v) {
                if(!dateSelectButton.getText().toString().equals(SELECT_DATE)) {
                    cal = new GregorianCalendar();
                    sHour = cal.get(Calendar.HOUR_OF_DAY);
                    sMinute = cal.get(Calendar.MINUTE);
                    new TimePickerDialog(WriteActivity.this, timeSetListener, sHour, sMinute, DateFormat.is24HourFormat(getBaseContext())).show();
                }else Toast.makeText(getBaseContext(), R.string.dateselect_toast, Toast.LENGTH_SHORT).show();
            }

            TimePickerDialog.OnTimeSetListener timeSetListener =
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            int sYear = Integer.parseInt(dateSelectButton.getText().toString().split(" - ")[0]);
                            int sMonth = Integer.parseInt(dateSelectButton.getText().toString().split(" - ")[1]);
                            int sDay = Integer.parseInt(dateSelectButton.getText().toString().split(" - ")[2]);
                            int year = cal.get(Calendar.YEAR);
                            int month = cal.get(Calendar.MONTH);
                            int day = cal.get(Calendar.DAY_OF_MONTH);

                            if (sYear == year && sMonth-1 == month && sDay == day) {
                                if (sHour <= hourOfDay && sMinute < minute
                                        || sHour < hourOfDay) {
                                    setTimeSelectButtonText(hourOfDay, minute);
                                } else
                                    Toast.makeText(getBaseContext(), R.string.select_future_time, Toast.LENGTH_SHORT).show();
                            } else{
                                setTimeSelectButtonText(hourOfDay, minute);
                            }
                        }
                    };
        });
    }

    // 시간 선택 버튼 텍스트 설정
    public static void setTimeSelectButtonText(int hourOfDay, int minute){
        // 오전
        String noticeTime = AM + " " + hourOfDay + " : " + minute;
        int hour = hourOfDay;
        if (minute < 10) noticeTime = AM + " " + hour + " : 0" + minute;

        // 오후
        if (12 <= hourOfDay) {
            if (hourOfDay != 12) hour = hourOfDay - 12;
            noticeTime = PM + " " + hour + " : " + minute;
            if (minute < 10)
                noticeTime = PM + " " + hour + " : 0" + minute;
        }
        timeSelectButton.setText(noticeTime);
    }

    // 날짜 선택 버튼 이벤트 처리
    private void setDateSelectButtonListener() {
        dateSelectButton.setOnClickListener(new View.OnClickListener() {
            int sYear;
            int sMonth;
            int sDay;
            @Override
            public void onClick(View v) {
                String buttonText = dateSelectButton.getText().toString();
                cal = new GregorianCalendar();
                // 날짜 이미 선택한 상태면 텍스트에서 불러오기
                if(!buttonText.equals(SELECT_DATE)){
                    sYear = Integer.valueOf(buttonText.split(" - ")[0]);
                    sMonth = Integer.valueOf(buttonText.split(" - ")[1]) - 1;
                    sDay = Integer.valueOf(buttonText.split(" - ")[2]);
                }else{
                    sYear = cal.get(Calendar.YEAR);
                    sMonth = cal.get(Calendar.MONTH);
                    sDay = cal.get(Calendar.DAY_OF_MONTH);
                }
                new DatePickerDialog(WriteActivity.this, dateSetListener, sYear, sMonth, sDay).show();
            }

            DatePickerDialog.OnDateSetListener dateSetListener =
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            if(cal.get(Calendar.YEAR) <= year && cal.get(Calendar.MONTH) <= month && cal.get(Calendar.DAY_OF_MONTH) <= dayOfMonth
                                    || cal.get(Calendar.YEAR) <= year && cal.get(Calendar.MONTH) < month
                                    || cal.get(Calendar.YEAR) < year) {
                                String date;
                                if (month < 10) {
                                    date = year + " - 0" + (month + 1) + " - " + dayOfMonth;
                                    if (dayOfMonth < 10)
                                        date = year + " - 0" + (month + 1) + " - 0" + dayOfMonth;
                                } else {
                                    date = year + " - " + (month + 1) + " - " + dayOfMonth;
                                    if (dayOfMonth < 10)
                                        date = year + " - " + (month + 1) + " - 0" + dayOfMonth;
                                }
                                dateSelectButton.setText(date);
                                nCheckBox.setVisibility(View.VISIBLE);
                            }
                            else Toast.makeText(getBaseContext(), R.string.select_future_date, Toast.LENGTH_SHORT).show();
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

//                            nCheckBox.setEnabled(false);
                            nCheckBox.setVisibility(View.INVISIBLE);
                            nCheckBox.setChecked(false);
//                            timeSelectButton.setEnabled(false);
                            timeSelectButton.setVisibility(View.INVISIBLE);
                            spinner.setVisibility(View.INVISIBLE);
                            spinner.setSelection(0);
                        } else {
                            dateSelectButton.setEnabled(true);
                            dateSelectButton.setTextColor(Color.BLACK);
//                            nCheckBox.setEnabled(true);
                            if(!dateSelectButton.getText().toString().equals(SELECT_DATE))
                                nCheckBox.setVisibility(View.VISIBLE);
                        }
                        break;
                    case NOTICE_TYPE:
                        if(!checkBox.isChecked()) {
//                            timeSelectButton.setEnabled(false);
                            timeSelectButton.setVisibility(View.INVISIBLE);
                            spinner.setVisibility(View.INVISIBLE);
                            spinner.setSelection(0);
                        } else {
//                            timeSelectButton.setEnabled(true);
                            timeSelectButton.setVisibility(View.VISIBLE);
                            timeSelectButton.setText(R.string.select_time);
                            spinner.setVisibility(View.VISIBLE);
                        }
                        break;
                }
            }
        });
    }

    // 삭제버튼 이벤트 처리
    public void setDeleteButtonListener(Button deleteButton){
        deleteButton.setOnClickListener(v ->{
                deleteItem();
                onBackPressed();
            }
        );
    }

    // 데이터 삭제
    public void deleteItem(){
        switch(fragmentType){
            case 0:
                DatabaseHelper.deleteData(TODO_TABLE, id);
                break;
            case 1:
                DatabaseHelper.deleteData(COMPLETED_TABLE, id);
                break;
        }

        MyAlarmManager.deleteAlarm(getApplicationContext(), id);
        databaseChangeFlag = true;
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
                alarmTime = timeSelectButton.getText().toString();
                cal = new GregorianCalendar();
                Calendar sCal = new GregorianCalendar();
                int sYear = Integer.parseInt(date.split(" - ")[0]);
                int sMonth = Integer.parseInt(date.split(" - ")[0]);
                int sDay = Integer.parseInt(date.split(" - ")[0]);
                int sHour = Integer.parseInt(alarmTime.split(PM + " ")[1].split(" : ")[0]);
                int sMinute = Integer.parseInt(alarmTime.split(" : ")[1]);
                sCal.set(Calendar.YEAR, sYear);
                sCal.set(Calendar.MONTH, sMonth-1);
                sCal.set(Calendar.DAY_OF_MONTH, sDay);
                sCal.set(Calendar.HOUR_OF_DAY, sHour);
                sCal.set(Calendar.MINUTE, sMinute);

                // 기한 없음 체크했을 때
                if(dCheckBox.isChecked()) date = DATE_NOT_SELECTED;

                // 알림 설정 체크 안돼있으면 텍스트 복구
                if(!nCheckBox.isChecked()) alarmTime = SELECT_TIME;

                // 제목 없을 때
                if (title.equals(""))
                        Toast.makeText(getBaseContext(), R.string.typetitle_toast, Toast.LENGTH_SHORT).show();
                // 날짜 선택 안했을 때
                else if(date.equals(SELECT_DATE))
                    Toast.makeText(getBaseContext(), R.string.dateselect_toast, Toast.LENGTH_SHORT).show();

                // 선택된 시간이 과거일 때
                else if(cal.getTimeInMillis() < sCal.getTimeInMillis())
                    Toast.makeText(getBaseContext(), R.string.select_future_time, Toast.LENGTH_SHORT).show();

                // 반복만 선택하고 알림 시간 선택안했을 때
                else if(timeSelectButton.getText().toString().equals(SELECT_TIME) && !repeatability.equals(NO_REPEAT))
                    Toast.makeText(getBaseContext(), R.string.select_time_please, Toast.LENGTH_SHORT).show();

                // 수정할 때
                else if(!isNew) writeTodo(UPDATE_TYPE);

                // 새로운 할일을 작성할 때
                else writeTodo(INSERT_TYPE);
            }

            public void writeTodo(String type){
                TodoItem item = new TodoItem(title, content, date, alarmTime, repeatability, id);
                processData(item, type);
                if(nCheckBox.isChecked() && !timeSelectButton.getText().toString().equals(SELECT_TIME))
                    MyAlarmManager.setAlarm(getApplicationContext(), date, alarmTime, title, content, repeatability, id);

                onBackPressed();
            }

            public void processData(TodoItem item, final String type){
                giveData.putParcelable(TODO_ITEM, item);
                switch (type){
                    case INSERT_TYPE:
                        DatabaseHelper.insertData(TODO_TABLE, giveData);
                        break;
                    case UPDATE_TYPE:
                        DatabaseHelper.updateData(TODO_TABLE, giveData);
                        break;
                }
                databaseChangeFlag = true;
            }
        });
    }

    // 완료, 미완료버튼 이벤트 처리
    public void setCompleteOrNotButtonListener(Button button){
        button.setOnClickListener(v -> {
            String tableName1 = "";
            switch(fragmentType){
                case 0:
                    tableName1 = COMPLETED_TABLE;
                    break;
                case 1:
                    tableName1 = TODO_TABLE;
                    break;
            }
            final String tableName = tableName1;

            // 기존 목록에서 삭제
            deleteItem();

            // 반대 목록에 추가
            try {
                id = DatabaseHelper.selectGreatestId(tableName) + 1;
            }catch(Exception e){
                id = 1;
            }

            Bundle giveData = new Bundle();
            title = tvTitle.getText().toString();
            content = tvContent.getText().toString();
            date = dateSelectButton.getText().toString();
            date = date.equals(SELECT_DATE) ? DATE_NOT_SELECTED : date;

            // 기한 없음 체크했을 때
            if(dCheckBox.isChecked()) date = DATE_NOT_SELECTED;

            TodoItem item = new TodoItem(title, content, date, SELECT_TIME, repeatability, id);
            giveData.putParcelable(TODO_ITEM, item);
            DatabaseHelper.insertData(tableName, giveData);

            databaseChangeFlag = true;
            onBackPressed();
        });
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