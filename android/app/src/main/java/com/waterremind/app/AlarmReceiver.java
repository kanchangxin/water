package com.waterremind.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String ACTION_REMIND = "com.waterremind.action.REMIND";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_REMIND.equals(intent.getAction())) {
            Intent serviceIntent = new Intent(context, WaterReminderService.class);
            serviceIntent.setAction(ACTION_REMIND);
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        }
    }
}