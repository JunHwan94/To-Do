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
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import static com.polarbearr.todo.DatabaseHelper.TODO_TABLE;
import static com.polarbearr.todo.ListFragment.CONTENT_KEY;
import static com.polarbearr.todo.ListFragment.DATE_KEY;
import static com.polarbearr.todo.ListFragment.ID_KEY;
import static com.polarbearr.todo.ListFragment.TITLE_KEY;

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
        String title = intent.getStringExtra(TITLE_KEY);
        String content = intent.getStringExtra(CONTENT_KEY);
        int id = intent.getIntExtra(ID_KEY, 0);
//            Bundle getData = DatabaseHelper.selectData(TODO_TABLE, id);
        setNotification(title, content, id);
    }

    // 가져온 제목 내용으로 알람 설정
    public void setNotification(String title, String content, int id){
        Intent intent = new Intent(AlarmService.this, MainActivity.class);
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
            notificationChannel.setLightColor(Color.GREEN);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 100, 200});
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        builder.setSmallIcon(R.mipmap.ic_todo_round).setTicker(APP_NAME)
                .setWhen(System.currentTimeMillis()).setNumber(0)
                .setContentTitle(title).setContentText(content)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent).setAutoCancel(true)
                .setOngoing(true);

        notificationManager.notify(id, builder.build());

//        Toast.makeText(this, title + "\n" + content + "\n" + date, Toast.LENGTH_LONG).show();
    }

    // 화면 깨우기
    public void wakeScreen(){
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock( PowerManager.SCREEN_DIM_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG:");
        wakeLock.acquire(3000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
