package com.waterremind.app;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.AlarmManager;
import android.content.Context;
import androidx.core.app.NotificationCompat;
import android.os.Vibrator;

public class WaterReminderService extends Service {

    private static final String CHANNEL_ID = "WATER_REMIND_SERVICE";
    private static final String CHANNEL_ID_REMIND = "WATER_REMIND_CHANNEL";
    private static final int NOTIFICATION_ID = 1001;
    private static final int NOTIFICATION_ID_REMIND = 2001;
    private static final String ACTION_REMIND = "com.waterremind.action.REMIND";

    private long intervalMinutes = 20;
    private Vibrator vibrator;
    private SharedPreferences prefs;
    private AlarmManager alarmManager;

    @Override
    public void onCreate() {
        super.onCreate();
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        prefs = getSharedPreferences("WaterRemind", MODE_PRIVATE);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_REMIND.equals(intent.getAction())) {
            showReminder();
            scheduleNextReminder();
        } else {
            loadSettings();
            scheduleNextReminder();
            startForeground(NOTIFICATION_ID, createServiceNotification());
        }
        return START_STICKY;
    }

    private void loadSettings() {
        intervalMinutes = prefs.getLong("interval", 20);
    }

    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
            CHANNEL_ID,
            "喝水提醒服务",
            NotificationManager.IMPORTANCE_LOW
        );
        serviceChannel.setDescription("后台提醒服务");

        NotificationChannel remindChannel = new NotificationChannel(
            CHANNEL_ID_REMIND,
            "喝水提醒",
            NotificationManager.IMPORTANCE_HIGH
        );
        remindChannel.setDescription("提醒您喝水");
        remindChannel.enableVibration(true);

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(serviceChannel);
            manager.createNotificationChannel(remindChannel);
        }
    }

    private Notification createServiceNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water)
            .setContentTitle("💧 喝水提醒")
            .setContentText("提醒服务运行中...")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build();
    }

    private void scheduleNextReminder() {
        loadSettings();
        Intent intent = new Intent(this, WaterReminderService.class);
        intent.setAction(ACTION_REMIND);
        PendingIntent pendingIntent = PendingIntent.getService(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerTime = System.currentTimeMillis() + (intervalMinutes * 60 * 1000);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                );
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                );
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            );
        }
    }

    private void showReminder() {
        vibrate();
        sendNotification();
    }

    private void vibrate() {
        if (vibrator != null && vibrator.hasVibrator()) {
            long[] pattern = {0, 500, 200, 500, 200, 500};
            vibrator.vibrate(pattern, -1);
        }
    }

    private void sendNotification() {
        NotificationManager manager = getSystemService(NotificationManager.class);
        
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID_REMIND)
            .setSmallIcon(R.drawable.ic_water)
            .setContentTitle("💧 该喝水啦！")
            .setContentText("站起来活动一下，喝杯水吧～")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL);

        if (manager != null) {
            manager.notify(NOTIFICATION_ID_REMIND, builder.build());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
