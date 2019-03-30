package com.polarbearr.todo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.view.WindowManager;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.polarbearr.todo.WriteActivity.EVERY_DAY;
import static com.polarbearr.todo.WriteActivity.EVERY_MONTH;
import static com.polarbearr.todo.WriteActivity.EVERY_WEEK;
import static com.polarbearr.todo.WriteActivity.EVERY_YEAR;
import static com.polarbearr.todo.WriteActivity.NO_REPEAT;
import static com.polarbearr.todo.data.DatabaseHelper.ALARM_TIME_KEY;
import static com.polarbearr.todo.data.DatabaseHelper.REPEATABILITY_KEY;
import static com.polarbearr.todo.data.DatabaseHelper.TITLE_KEY;
import static com.polarbearr.todo.data.DatabaseHelper.CONTENT_KEY;
import static com.polarbearr.todo.data.DatabaseHelper.ID_KEY;

public class AlarmService extends Service {
    private static final String APP_NAME = "To Do";
    private static final String CHANNEL_ID = "cid";
    private static final String CHANNEL_DESCRIPTION = "할일 알림";
    private static final long systemTimePerDay = 86400000L;

    String alarmTime;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 인텐트 처리
        processIntent(intent);

        // 화면 깨우기
        wakeScreen();
        return super.onStartCommand(intent, flags, startId);
    }

    public void processIntent(Intent intent){
        String date;
        alarmTime = intent.getStringExtra(ALARM_TIME_KEY);
        String title = intent.getStringExtra(TITLE_KEY);
        String content = intent.getStringExtra(CONTENT_KEY);
        String repeatability = intent.getStringExtra(REPEATABILITY_KEY);
        int id = intent.getIntExtra(ID_KEY, 0);

        setNotification(title, content, id);

        // 반복하면 다음 알람 설정
        if(!repeatability.equals(NO_REPEAT)) {
            Calendar cal = setNextSystemTime(repeatability);
            date = cal.get(Calendar.YEAR) + " - " + (cal.get(Calendar.MONTH)+1) + " - " + cal.get(Calendar.DAY_OF_MONTH);
//            Log.d("다음 날짜", date);
            MyAlarmManager.setAlarm(getApplicationContext(), date, alarmTime, title, content, repeatability, id);
        }
    }

    public Calendar setNextSystemTime(String repeatability){
        Calendar cal = new GregorianCalendar();
        int thisYear = cal.get(Calendar.YEAR);
        boolean isLeapYear = thisYear % 4 == 0 && thisYear % 100 != 0 || thisYear % 400 == 0;
        int nextYear = thisYear + 1;
        boolean nextIsLeapYear = nextYear % 4 == 0 && nextYear % 100 != 0 || nextYear % 400 == 0;
        int thisMonth = cal.get(Calendar.MONTH) + 1;
        int thisDay = cal.get(Calendar.DAY_OF_MONTH);
        boolean isEvenMonth = thisMonth % 2 == 0;
        long systemTime = cal.getTimeInMillis();

        switch (repeatability) {
            case EVERY_DAY:
                systemTime += systemTimePerDay;
                break;
            case EVERY_WEEK:
                systemTime += systemTimePerDay * 7;
                break;
            case EVERY_MONTH:
                if(thisMonth < 8 && thisMonth != 2){
                    if(isEvenMonth) systemTime += systemTimePerDay * 30;
                    else systemTime += systemTimePerDay * 31;
                }else if(thisMonth <= 8){
                    if(isEvenMonth) systemTime += systemTimePerDay * 31;
                    else systemTime += systemTimePerDay * 30;
                }else{
                    if(isLeapYear)
                        systemTime += systemTimePerDay * 29;
                    else systemTime += systemTimePerDay * 28;
                }
                break;
            case EVERY_YEAR:
                if(isLeapYear) { // 윤년일 때
                    if(thisMonth == 1 || thisMonth == 2 && thisDay != 29) // 1월이거나 2월 29일 이전이면 1일 더해야함
                        systemTime = addDays(systemTime, true);
                    else
                        systemTime = addDays(systemTime, false);
                }
                else { // 윤년이 아닐 때
                    if(nextIsLeapYear && 2 < thisMonth) // 다음해가 윤년이고 지금 2월 이후면 1일 더함
                        systemTime = addDays(systemTime, true);
                    else systemTime = addDays(systemTime, false);
                }
                break;
        }
        cal.setTimeInMillis(systemTime);
        return cal;
    }

    public long addDays(long systemTime, boolean addOneMore){
        systemTime += addOneMore ? systemTimePerDay * 366 : systemTimePerDay * 365;
        return systemTime;
    }

    // 가져온 제목 내용으로 알람 설정
    public void setNotification(String title, String content, int id){
        Intent intent = new Intent(AlarmService.this, MainActivity.class);
        intent.putExtra(ID_KEY, id);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(AlarmService.this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(getApplicationContext());

        // 알림 채널 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(getApplicationContext(), CHANNEL_ID);
            NotificationChannel notificationChannel =
                    new NotificationChannel(CHANNEL_ID, APP_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription(CHANNEL_DESCRIPTION);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.CYAN);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 100, 200});
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        builder.setSmallIcon(R.drawable.ic_stat_name).setTicker(APP_NAME)
                .setWhen(System.currentTimeMillis()).setNumber(0)
                .setContentTitle(title).setSubText("|  " + alarmTime).setContentText(content)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent).setAutoCancel(true)
                .setOngoing(false);

        notificationManager.notify(id, builder.build());
    }

    // 화면 깨우기
    public void wakeScreen(){
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG:");
        wakeLock.acquire(3000);
    }
}
