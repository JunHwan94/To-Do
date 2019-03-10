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

import static com.polarbearr.todo.WriteActivity.EVERY_DAY;
import static com.polarbearr.todo.WriteActivity.EVERY_MONTH;
import static com.polarbearr.todo.WriteActivity.EVERY_WEEK;
import static com.polarbearr.todo.WriteActivity.EVERY_YEAR;
import static com.polarbearr.todo.WriteActivity.NO_REPEAT;
import static com.polarbearr.todo.data.DatabaseHelper.ALARM_TIME_KEY;
import static com.polarbearr.todo.data.DatabaseHelper.DATE_KEY;
import static com.polarbearr.todo.data.DatabaseHelper.REPEATABILITY_KEY;
import static com.polarbearr.todo.data.DatabaseHelper.TITLE_KEY;
import static com.polarbearr.todo.data.DatabaseHelper.CONTENT_KEY;
import static com.polarbearr.todo.data.DatabaseHelper.ID_KEY;

public class AlarmService extends Service {
    private static final String APP_NAME = "To Do";
    private static final String CHANNEL_ID = "cid";
    private static final String CHANNEL_DESCRIPTION = "할일 알림";

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
        String date = intent.getStringExtra(DATE_KEY);
        String alarmTime = intent.getStringExtra(ALARM_TIME_KEY);
        String title = intent.getStringExtra(TITLE_KEY);
        String content = intent.getStringExtra(CONTENT_KEY);
        String repeatability = intent.getStringExtra(REPEATABILITY_KEY);
        int id = intent.getIntExtra(ID_KEY, 0);

        setNotification(title, content, id);

//        Calendar cal = new GregorianCalendar();
//        if(!repeatability.equals(NO_REPEAT)) {
//            switch (repeatability) {
//                case EVERY_DAY:
//                    break;
//                case EVERY_WEEK:
//                    break;
//                case EVERY_MONTH:
//                    break;
//                case EVERY_YEAR:
//                    break;
//            }
//            // 다음 알람 설정
//            MyAlarmManager.setAlarm(getApplicationContext(), date, alarmTime, title, content, repeatability, id);
//            Log.d("다음 알람", date);
//        }
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
                .setContentTitle(title).setContentText(content)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent).setAutoCancel(true)
                .setOngoing(true);

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
