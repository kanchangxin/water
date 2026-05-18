package com.waterremind.app;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.app.AlarmManager;

public class MainActivity extends AppCompatActivity {

    private TextView timerDisplay;
    private EditText intervalInput;
    private Button startBtn, pauseBtn, resetBtn, drankBtn, testBtn, test1minBtn;
    private TextView statusText, lastDrankText;
    private CountDownTimer countDownTimer;
    private long totalSeconds = 1200;
    private long intervalMinutes = 20;
    private boolean isRunning = false;
    private Vibrator vibrator;
    private SharedPreferences prefs;

    private static final String CHANNEL_ID = "WATER_REMIND_CHANNEL";
    private static final int NOTIFICATION_ID = 2001;
    private static final int REQUEST_PERMISSIONS = 100;
    private static final int REQUEST_SCHEDULE_EXACT_ALARM = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timerDisplay = findViewById(R.id.timer);
        intervalInput = findViewById(R.id.interval);
        startBtn = findViewById(R.id.startBtn);
        pauseBtn = findViewById(R.id.pauseBtn);
        resetBtn = findViewById(R.id.resetBtn);
        drankBtn = findViewById(R.id.drankBtn);
        statusText = findViewById(R.id.status);
        lastDrankText = findViewById(R.id.lastDrank);
        testBtn = findViewById(R.id.testBtn);
        test1minBtn = findViewById(R.id.test1minBtn);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        prefs = getSharedPreferences("WaterRemind", MODE_PRIVATE);

        requestPermissions();
        checkExactAlarmPermission();
        createNotificationChannel();
        loadSettings();
        updateDisplay();

        startBtn.setOnClickListener(v -> startTimer());
        pauseBtn.setOnClickListener(v -> pauseTimer());
        resetBtn.setOnClickListener(v -> resetTimer());
        drankBtn.setOnClickListener(v -> markAsDrank());
        testBtn.setOnClickListener(v -> testReminder());
        test1minBtn.setOnClickListener(v -> start1MinuteTest());
    }

    private void requestPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQUEST_PERMISSIONS
                );
            }
        }
    }

    private void checkExactAlarmPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivityForResult(intent, REQUEST_SCHEDULE_EXACT_ALARM);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SCHEDULE_EXACT_ALARM) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                if (!alarmManager.canScheduleExactAlarms()) {
                    Toast.makeText(this, "精确闹钟权限未授予，提醒可能不准确", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "通知权限被拒绝，提醒功能可能受限", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadSettings() {
        intervalMinutes = prefs.getLong("interval", 20);
        intervalInput.setText(String.valueOf(intervalMinutes));
        totalSeconds = intervalMinutes * 60;

        long lastDrank = prefs.getLong("lastDrank", 0);
        if (lastDrank > 0) {
            updateLastDrankDisplay(lastDrank);
        }

        isRunning = prefs.getBoolean("isRunning", false);
        if (isRunning) {
            isRunning = false;
            startTimer();
        }
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
            CHANNEL_ID,
            "喝水提醒",
            NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription("提醒您喝水");
        channel.enableVibration(true);
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    private void updateDisplay() {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        timerDisplay.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private void updateLastDrankDisplay(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;
        long hours = diff / (1000 * 60 * 60);
        long minutes = (diff % (1000 * 60 * 60)) / (1000 * 60);

        String timeStr;
        if (hours > 0) {
            timeStr = hours + "小时" + minutes + "分钟前";
        } else if (minutes > 0) {
            timeStr = minutes + "分钟前";
        } else {
            timeStr = "刚刚";
        }
        lastDrankText.setText("上次喝水: " + timeStr);
    }

    private void startTimer() {
        if (isRunning) return;

        try {
            intervalMinutes = Long.parseLong(intervalInput.getText().toString());
            if (intervalMinutes < 1 || intervalMinutes > 120) {
                Toast.makeText(this, "请输入1-120分钟", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效数字", Toast.LENGTH_SHORT).show();
            return;
        }

        totalSeconds = intervalMinutes * 60;
        isRunning = true;
        statusText.setText("提醒进行中...");
        statusText.setBackgroundColor(ContextCompat.getColor(this, R.color.statusActive));
        startBtn.setEnabled(false);

        prefs.edit().putLong("interval", intervalMinutes).apply();
        prefs.edit().putBoolean("isRunning", true).apply();

        startBackgroundService();

        countDownTimer = new CountDownTimer(totalSeconds * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                totalSeconds = millisUntilFinished / 1000;
                updateDisplay();
            }

            @Override
            public void onFinish() {
                showNotification();
                totalSeconds = intervalMinutes * 60;
                updateDisplay();
                startTimer();
            }
        }.start();
    }

    private void startBackgroundService() {
        Intent serviceIntent = new Intent(this, WaterReminderService.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    private void stopBackgroundService() {
        Intent serviceIntent = new Intent(this, WaterReminderService.class);
        stopService(serviceIntent);
    }

    private void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isRunning = false;
        statusText.setText("提醒已暂停");
        statusText.setBackgroundColor(ContextCompat.getColor(this, R.color.statusInactive));
        startBtn.setEnabled(true);

        prefs.edit().putBoolean("isRunning", false).apply();
        stopBackgroundService();
    }

    private void resetTimer() {
        pauseTimer();
        try {
            intervalMinutes = Long.parseLong(intervalInput.getText().toString());
        } catch (NumberFormatException e) {
            intervalMinutes = 20;
        }
        totalSeconds = intervalMinutes * 60;
        updateDisplay();
        statusText.setText("提醒未启动");
    }

    private void markAsDrank() {
        long now = System.currentTimeMillis();
        prefs.edit().putLong("lastDrank", now).apply();
        updateLastDrankDisplay(now);
        Toast.makeText(this, "已记录喝水时间", Toast.LENGTH_SHORT).show();

        if (isRunning) {
            totalSeconds = intervalMinutes * 60;
            updateDisplay();
        }
    }

    private void showNotification() {
        vibrate();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water)
            .setContentTitle("💧 该喝水啦！")
            .setContentText("站起来活动一下，喝杯水吧～")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL);

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, builder.build());
        }

        Toast.makeText(this, "💧 该喝水啦！", Toast.LENGTH_LONG).show();
    }

    private void vibrate() {
        if (vibrator != null && vibrator.hasVibrator()) {
            long[] pattern = {0, 500, 200, 500, 200, 500};
            vibrator.vibrate(pattern, -1);
        }
    }

    private void testReminder() {
        Toast.makeText(this, "🔔 正在测试提醒功能...", Toast.LENGTH_SHORT).show();
        showNotification();
    }

    private void start1MinuteTest() {
        if (isRunning) {
            pauseTimer();
        }
        intervalInput.setText("1");
        startTimer();
        Toast.makeText(this, "⏱️ 已设置为1分钟测试模式", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}