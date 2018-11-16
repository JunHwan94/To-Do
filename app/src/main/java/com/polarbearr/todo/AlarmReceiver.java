package com.polarbearr.todo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import static com.polarbearr.todo.ListFragment.ID_KEY;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra(ID_KEY, 0);
        Intent serviceIntent = new Intent(context, AlarmService.class);
        serviceIntent.putExtra(ID_KEY, id);
        context.startService(serviceIntent);
    }
}