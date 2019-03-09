package com.polarbearr.todo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

import static com.polarbearr.todo.ListFragment.CONTENT_KEY;
import static com.polarbearr.todo.ListFragment.ID_KEY;
import static com.polarbearr.todo.ListFragment.TITLE_KEY;

public class MyAlarmManager{
    static final String PM = "오후";

    public void setAlarm(Context context, String date, String time, String title, String content, int id){
//        String dateButtonText = dateSelectButton.getText().toString();
//        String timeButtonText = timeSelectButton.getText().toString();
        String ap = time.split(" ")[0];

        int year = Integer.valueOf(date.split(" - ")[0]);
        int month  = Integer.valueOf(date.split(" - ")[1]);
        int day = Integer.valueOf(date.split(" - ")[2]);
        int hour = Integer.valueOf(time.split(" : ")[0].split(" ")[1]);
        if(ap.equals(PM) && hour != 12){
            hour += 12;
        }
        int minute = Integer.valueOf(time.split(" : ")[1]);

        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);    // 인덱스 방식 / 1 작은 숫자를 설정해줘야 제대로 작동함. DatePicker가 1 작은 값을 반환하는 것과 같은듯
        calendar.set(Calendar.DATE, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.putExtra(TITLE_KEY, title);
        alarmIntent.putExtra(CONTENT_KEY, content);
        alarmIntent.putExtra(ID_KEY, id);

        final PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        id,
                        alarmIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        final AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // 선택한 날짜, 시간으로 알람 설정
        new Thread(() -> {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        }).start();
    }

    public void deleteAlarm(Context context, int id){
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        context,
                        id,
                        alarmIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT
                );
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        //현재 아이템의 id로 알람이 설정되어있으면 취소
        if (pendingIntent != null) {
            pendingIntent = PendingIntent.getBroadcast(
                    context,
                    id,
                    alarmIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT
            );
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
        }
    }
}