package com.polarbearr.todo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import static com.polarbearr.todo.ListFragment.CONTENT_KEY;
import static com.polarbearr.todo.ListFragment.ID_KEY;
import static com.polarbearr.todo.ListFragment.TITLE_KEY;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra(TITLE_KEY);
        String content = intent.getStringExtra(CONTENT_KEY);
        Intent serviceIntent = new Intent(context, AlarmService.class);
        serviceIntent.putExtra(TITLE_KEY, title);
        serviceIntent.putExtra(CONTENT_KEY, content);
        context.startService(serviceIntent);
    }
}