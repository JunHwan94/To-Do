package com.polarbearr.todo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static com.polarbearr.todo.data.DatabaseHelper.ALARM_TIME_KEY;
import static com.polarbearr.todo.data.DatabaseHelper.DATE_KEY;
import static com.polarbearr.todo.data.DatabaseHelper.REPEATABILITY_KEY;
import static com.polarbearr.todo.data.DatabaseHelper.TITLE_KEY;
import static com.polarbearr.todo.data.DatabaseHelper.CONTENT_KEY;
import static com.polarbearr.todo.data.DatabaseHelper.ID_KEY;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String date = intent.getStringExtra(DATE_KEY);
        String alarmTime = intent.getStringExtra(ALARM_TIME_KEY);
        String title = intent.getStringExtra(TITLE_KEY);
        String content = intent.getStringExtra(CONTENT_KEY);
        String repeatability = intent.getStringExtra(REPEATABILITY_KEY);
        int id = intent.getIntExtra(ID_KEY, 0);

        Intent serviceIntent = new Intent(context, AlarmService.class);
        serviceIntent.putExtra(DATE_KEY, date);
        serviceIntent.putExtra(ALARM_TIME_KEY, alarmTime);
        serviceIntent.putExtra(TITLE_KEY, title);
        serviceIntent.putExtra(CONTENT_KEY, content);
        serviceIntent.putExtra(REPEATABILITY_KEY, repeatability);
        serviceIntent.putExtra(ID_KEY, id);
        context.startService(serviceIntent);
    }
}