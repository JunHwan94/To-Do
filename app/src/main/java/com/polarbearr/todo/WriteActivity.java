package com.polarbearr.todo;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresPermission;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
import com.polarbearr.todo.databinding.ActivityWriteBinding;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.polarbearr.todo.ListFragment.COMPLETED;
import static com.polarbearr.todo.ListFragment.NOT_COMPLETED;
import static com.polarbearr.todo.data.DatabaseHelper.REPEATABILITY_KEY;
import static com.polarbearr.todo.data.DatabaseHelper.TITLE_KEY;
import static com.polarbearr.todo.data.DatabaseHelper.CONTENT_KEY;
import static com.polarbearr.todo.data.DatabaseHelper.ID_KEY;
import static com.polarbearr.todo.data.DatabaseHelper.DATE_KEY;
import static com.polarbearr.todo.data.DatabaseHelper.ALARM_TIME_KEY;
import static com.polarbearr.todo.data.DatabaseHelper.GREATEST_ID_KEY;
import static com.polarbearr.todo.data.DatabaseHelper.TODO_ITEM;
import static com.polarbearr.todo.ListFragment.NOTHING;

public class WriteActivity extends AppCompatActivity {
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
    private String isCompletedYn;
    private InputMethodManager imm;

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

    private ActivityWriteBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_write);
        binding.setLifecycleOwner(this);

        Intent intent = getIntent();
        title = intent.getStringExtra(TITLE_KEY);
        content = intent.getStringExtra(CONTENT_KEY);
        date = intent.getStringExtra(DATE_KEY);
        alarmTime = intent.getStringExtra(ALARM_TIME_KEY);
        id = intent.getIntExtra(ID_KEY, 0);
        fragmentType = intent.getIntExtra(TYPE_KEY, 0);
        repeatability = intent.getStringExtra(REPEATABILITY_KEY);
        imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);

        String[] items = {NO_REPEAT, EVERY_DAY, EVERY_WEEK, EVERY_MONTH, EVERY_YEAR};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinner.setAdapter(spinnerAdapter);
        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
            binding.etTitle.setText(title);
            binding.etContent.setText(content);

            // 기한 있는 할일이면
            if(!date.equals(NOTHING)) {
                binding.dateSelectButton.setText(date);
                binding.timeSelectButton.setText(alarmTime);
                binding.nCheckBox.setVisibility(View.VISIBLE);
                // 알람 설정돼있으면 알림설정 체크박스 미리 체크
                if(!alarmTime.equals(SELECT_TIME)) {
                    binding.nCheckBox.setChecked(true);
                    binding.timeSelectButton.setEnabled(true);  // 시간 선택 버튼 활성
                    binding.timeSelectButton.setVisibility(View.VISIBLE);
                    binding.spinner.setVisibility(View.VISIBLE); // 스피너도 보이게

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
                    binding.spinner.setSelection(spIndex);
                }
            } else {    // 기한 없을 때 기한없음 체크박스 미리 체크상태로, 날짜선택 버튼 사용불가상태로
                binding.dCheckBox.setChecked(true);
                binding.dateSelectButton.setEnabled(false);
                binding.dateSelectButton.setTextColor(Color.LTGRAY);
            }
        }
        // + 버튼을 눌러서 WriteActivity 실행했을 때
        else {
            isCompletedYn = NOT_COMPLETED;
            id = intent.getIntExtra(GREATEST_ID_KEY, 0) + 1;
            binding.deleteButton.setVisibility(View.INVISIBLE);
            binding.completeButton.setVisibility(View.INVISIBLE);
        }

        switch(fragmentType){
            case 0: // 할일 이면
                binding.restoreButton.setVisibility(View.INVISIBLE);
                break;
            case 1: // 완료한 일이면
                binding.etTitle.setEnabled(false);
                binding.etContent.setEnabled(false);
                binding.etContent.setHint("");
                binding.dateSelectButton.setEnabled(false);
                binding.dCheckBox.setEnabled(false);
                binding.nCheckBox.setVisibility(View.INVISIBLE);
                binding.completeButton.setVisibility(View.INVISIBLE);
                binding.saveButton.setVisibility(View.INVISIBLE);

                if(binding.dCheckBox.isChecked())
                    binding.dateSelectButton.setVisibility(View.INVISIBLE);
                else binding.dCheckBox.setVisibility(View.INVISIBLE);
                break;
        }

        // 저장 버튼 이벤트
        setSaveButtonListener(binding.saveButton);

        // 삭제 버튼 이벤트
        setDeleteButtonListener(binding.deleteButton);

        // 완료, 미완료 버튼 이벤트
        setCompleteOrNotButtonListener(binding.completeButton);
        setCompleteOrNotButtonListener(binding.restoreButton);

        // 날짜 선택 버튼 이벤트 처리
        setDateSelectButtonListener();

        // 기한 없음 체크박스 클릭이벤트
        setCheckBoxListener(binding.dCheckBox, DATE_TYPE);

        // 시간 선택 버튼 이벤트 처리
        setTimeSelectButtonListener();

        // 알림설정 체크박스 클릭이벤트
        setCheckBoxListener(binding.nCheckBox, NOTICE_TYPE);

        // 뒤로가기 버튼 이벤트
        binding.backButton.setOnClickListener( v-> onBackPressed() );

        // 링크처리
        Linkify.addLinks(binding.etContent, Linkify.ALL);

        binding.etContent.setOnFocusChangeListener((v, hasFocus)->{
            if(hasFocus)
                imm.showSoftInput(v, 0);
        });

        binding.rootView.setOnClickListener(v-> hideKeyboard(v) );
    }

    // 시간 선택 버튼 이벤트 처리
    private void setTimeSelectButtonListener(){
        binding.timeSelectButton.setOnClickListener(new View.OnClickListener() {
            int sHour;
            int sMinute;
            @Override
            public void onClick(View v) {
                if(!binding.dateSelectButton.getText().toString().equals(SELECT_DATE)) {
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
                            int sYear = Integer.parseInt(binding.dateSelectButton.getText().toString().split(" - ")[0]);
                            int sMonth = Integer.parseInt(binding.dateSelectButton.getText().toString().split(" - ")[1]);
                            int sDay = Integer.parseInt(binding.dateSelectButton.getText().toString().split(" - ")[2]);
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
    public void setTimeSelectButtonText(int hourOfDay, int minute){
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
        binding.timeSelectButton.setText(noticeTime);
    }

    // 날짜 선택 버튼 이벤트 처리
    private void setDateSelectButtonListener() {
        binding.dateSelectButton.setOnClickListener(new View.OnClickListener() {
            int sYear;
            int sMonth;
            int sDay;
            @Override
            public void onClick(View v) {
                hideKeyboard(v);

                String buttonText = binding.dateSelectButton.getText().toString();
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
                                binding.dateSelectButton.setText(date);
                                binding.nCheckBox.setVisibility(View.VISIBLE);
                                binding.nCheckBox.setEnabled(true);
                            }
                            else Toast.makeText(getBaseContext(), R.string.select_future_date, Toast.LENGTH_SHORT).show();
                        }

                    };
        });

    }

    // 체크박스 클릭 이벤트 처리
    private void setCheckBoxListener(final CheckBox checkBox, final String type){
        checkBox.setClickable(true);
        checkBox.setOnClickListener( v -> {
            switch (type){
                case DATE_TYPE:
                    if(checkBox.isChecked()) {
                        binding.dateSelectButton.setEnabled(false);
                        binding.dateSelectButton.setTextColor(Color.LTGRAY);
                        binding.nCheckBox.setVisibility(View.INVISIBLE);
                        binding.nCheckBox.setChecked(false);
                        binding.timeSelectButton.setText(R.string.select_time);
                        binding.timeSelectButton.setVisibility(View.INVISIBLE);
                        binding.spinner.setVisibility(View.INVISIBLE);
                        binding.spinner.setSelection(0);
                    } else {
                        binding.dateSelectButton.setEnabled(true);
                        binding.dateSelectButton.setTextColor(Color.BLACK);
                        if(!binding.dateSelectButton.getText().toString().equals(SELECT_DATE))
                            binding.nCheckBox.setVisibility(View.VISIBLE);
                    }
                    break;
                case NOTICE_TYPE:
                    if(!checkBox.isChecked()) {
                        binding.timeSelectButton.setVisibility(View.INVISIBLE);
                        binding.timeSelectButton.setText(R.string.select_time);
                        binding.spinner.setVisibility(View.INVISIBLE);
                        binding.spinner.setSelection(0);
                    } else {
                        binding.timeSelectButton.setVisibility(View.VISIBLE);
                        binding.spinner.setVisibility(View.VISIBLE);
                    }
                    break;
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
        DatabaseHelper.deleteData(id);

        MyAlarmManager.deleteAlarm(getApplicationContext(), id);
        databaseChangeFlag = true;
    }

    // 저장버튼 이벤트 처리
    public void setSaveButtonListener(Button saveButton){
        final Bundle giveData = new Bundle();
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title = binding.etTitle.getText().toString();
                content = binding.etContent.getText().toString();
                date = binding.dateSelectButton.getText().toString();
                alarmTime = binding.timeSelectButton.getText().toString();
                cal = new GregorianCalendar();
                Calendar sCal = new GregorianCalendar();
                try {
                    int sYear = Integer.parseInt(date.split(" - ")[0]);
                    int sMonth = Integer.parseInt(date.split(" - ")[1]);
                    int sDay = Integer.parseInt(date.split(" - ")[2]);
                    String ap = alarmTime.split(" ")[0];
                    int sHour = Integer.parseInt(alarmTime.split(" : ")[0].split(" ")[1]);
                    if (ap.equals(PM) && sHour != 12) {
                        sHour += 12;
                    }
                    int sMinute = Integer.parseInt(alarmTime.split(" : ")[1]);
                    sCal.set(Calendar.YEAR, sYear);
                    sCal.set(Calendar.MONTH, sMonth - 1);
                    sCal.set(Calendar.DAY_OF_MONTH, sDay);
                    sCal.set(Calendar.HOUR_OF_DAY, sHour);
                    sCal.set(Calendar.MINUTE, sMinute);
                }catch(Exception e){}

                // 기한 없음 체크했을 때
                if(binding.dCheckBox.isChecked()) date = DATE_NOT_SELECTED;

                // 알림 설정 체크 안돼있으면 텍스트 복구
                if(!binding.nCheckBox.isChecked()) alarmTime = SELECT_TIME;

                // 제목 없을 때
                if (title.equals(""))
                        Toast.makeText(getBaseContext(), R.string.typetitle_toast, Toast.LENGTH_SHORT).show();

                // 날짜 선택 안했을 때
                else if(date.equals(SELECT_DATE))
                    Toast.makeText(getBaseContext(), R.string.dateselect_toast, Toast.LENGTH_SHORT).show();

                // 선택된 시간이 과거일 때
                else if(sCal.getTimeInMillis() < cal.getTimeInMillis())
                    Toast.makeText(getBaseContext(), R.string.select_future_time, Toast.LENGTH_SHORT).show();

                // 반복만 선택하고 알림 시간 선택안했을 때
                else if(binding.timeSelectButton.getText().toString().equals(SELECT_TIME) && !repeatability.equals(NO_REPEAT))
                    Toast.makeText(getBaseContext(), R.string.select_time_please, Toast.LENGTH_SHORT).show();

                // 수정할 때
                else if(!isNew) writeTodo(UPDATE_TYPE);

                // 새로운 할일을 작성할 때
                else writeTodo(INSERT_TYPE);
            }

            public void writeTodo(String type){
                isCompletedYn = NOT_COMPLETED;
                TodoItem item = new TodoItem(title, content, date, alarmTime, repeatability, isCompletedYn, id);
                processData(item, type);
                if(binding.nCheckBox.isChecked() && !binding.timeSelectButton.getText().toString().equals(SELECT_TIME))
                    MyAlarmManager.setAlarm(getApplicationContext(), date, alarmTime, title, content, repeatability, id);

                onBackPressed();
            }

            public void processData(TodoItem item, final String type){
                giveData.putParcelable(TODO_ITEM, item);
                switch (type){
                    case INSERT_TYPE:
                        DatabaseHelper.insertData(giveData);
                        break;
                    case UPDATE_TYPE:
                        DatabaseHelper.updateData(giveData);
                        break;
                }
                databaseChangeFlag = true;
            }
        });
    }

    // 완료, 미완료버튼 이벤트 처리
    public void setCompleteOrNotButtonListener(Button button){
        button.setOnClickListener(v -> {
            switch(fragmentType){
                case 0:
                    isCompletedYn = COMPLETED;
                    break;
                case 1:
                    isCompletedYn = NOT_COMPLETED;
                    break;
            }
            Bundle giveData = new Bundle();
            title = binding.etTitle.getText().toString();
            content = binding.etContent.getText().toString();
            date = binding.dateSelectButton.getText().toString();
            date = date.equals(SELECT_DATE) ? DATE_NOT_SELECTED : date;
            // 기한 없음 체크했을 때
            date = binding.dCheckBox.isChecked() ? DATE_NOT_SELECTED : date;

            TodoItem item = new TodoItem(title, content, date, SELECT_TIME, repeatability, isCompletedYn, id);
            giveData.putParcelable(TODO_ITEM, item);
            DatabaseHelper.updateData(giveData);

            databaseChangeFlag = true;
            onBackPressed();
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(DATABASE_FLAG_KEY, databaseChangeFlag);
        setResult(RESULT_OK, intent);
        finish();
    }

    // 키보드 숨기기
    public void hideKeyboard(View v) {
        v.getRootView().clearFocus();
        imm.hideSoftInputFromWindow(v.getRootView().getWindowToken(), 0);
        Linkify.addLinks(binding.etContent, Linkify.ALL);
    }
}