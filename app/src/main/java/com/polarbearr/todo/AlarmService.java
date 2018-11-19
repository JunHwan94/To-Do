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
    private static final String CHANNEL_DESCRIPTION = "channel description";

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
        int id = intent.getIntExtra(ID_KEY, 0);

        System.out.println("id = " + id);
        Bundle getData = DatabaseHelper.selectData(TODO_TABLE, id);
        String title = getData.getString(TITLE_KEY);
        String content = getData.getString(CONTENT_KEY);
        String date = getData.getString(DATE_KEY);

        Intent intent1 = new Intent(AlarmService.this, MainActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP| Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(AlarmService.this, 0, intent1, PendingIntent.FLAG_CANCEL_CURRENT);

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

        builder.setSmallIcon(R.drawable.todo_icon).setTicker(APP_NAME)
                .setWhen(System.currentTimeMillis()).setNumber(0)
                .setContentTitle(title).setContentText(content + "  " + date)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent).setAutoCancel(true)
                .setOngoing(true);

        notificationManager.notify(0, builder.build());

//        Toast.makeText(this, title + "\n" + content + "\n" + date, Toast.LENGTH_LONG).show();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
