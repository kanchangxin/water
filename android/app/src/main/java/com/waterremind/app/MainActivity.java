package com.waterremind.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private TextView timerDisplay;
    private Button startBtn, pauseBtn, drankBtn, testBtn, helpBtn, settingsBtn;
    private TextView statusText, lastDrankText;
    private CountDownTimer countDownTimer;
    private long totalSeconds = 1200;
    private long intervalMinutes = 20;
    private boolean isRunning = false;
    private Vibrator vibrator;
    private SharedPreferences prefs;
    private Ringtone ringtone;

    private static final String CHANNEL_ID = "WATER_REMIND_CHANNEL";
    private static final int NOTIFICATION_ID = 2001;
    private static final int REQUEST_SCHEDULE_EXACT_ALARM = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timerDisplay = findViewById(R.id.timer);
        startBtn = findViewById(R.id.startBtn);
        pauseBtn = findViewById(R.id.pauseBtn);
        drankBtn = findViewById(R.id.drankBtn);
        statusText = findViewById(R.id.status);
        lastDrankText = findViewById(R.id.lastDrank);
        testBtn = findViewById(R.id.testBtn);
        helpBtn = findViewById(R.id.helpBtn);
        settingsBtn = findViewById(R.id.settingsBtn);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        prefs = getSharedPreferences("WaterRemind", MODE_PRIVATE);
        initRingtone();

        checkExactAlarmPermission();
        createNotificationChannel();
        loadSettings();
        updateDisplay();

        startBtn.setOnClickListener(v -> startTimer());
        pauseBtn.setOnClickListener(v -> pauseTimer());
        drankBtn.setOnClickListener(v -> markAsDrank());
        testBtn.setOnClickListener(v -> testReminder());
        helpBtn.setOnClickListener(v -> showHelpDialog());
        settingsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        if (isRunning) {
            startService();
        }
    }

    private void initRingtone() {
        String savedUri = prefs.getString("ringtone_uri", null);
        Uri ringtoneUri;
        
        if (savedUri != null) {
            ringtoneUri = Uri.parse(savedUri);
        } else {
            ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        
        if (ringtoneUri != null) {
            ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
        }
    }

    private void showHelpDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("📖 权限设置帮助");

        String helpText =
            "为确保应用在熄屏和后台运行时能正常接收提醒，请按以下步骤设置：\n\n" +
            "1. 打开手机设置 → 电池 → 后台应用耗电管理（或类似选项）\n" +
            "2. 找到 喝水提醒 应用\n" +
            "3. 将其设置为 允许后台耗电 或 不受电池优化限制\n\n" +
            "不同品牌手机的设置路径可能略有不同：\n\n" +
            "华为/荣耀：\n" +
            "设置 > 电池 > 应用启动管理 > 找到喝水提醒 > 关闭\"自动管理\" > 手动打开\"允许后台活动\"\n\n" +
            "小米/红米：\n" +
            "设置 > 电池与性能 > 应用耗电管理 > 喝水提醒 > 设为\"无限制\"\n\n" +
            "OPPO/一加：\n" +
            "设置 > 电池 > 耗电保护 > 喝水提醒 > 关闭\"后台冻结\"和\"深度睡眠\"\n\n" +
            "vivo/iQOO：\n" +
            "设置 > 电池 > 后台高耗电 > 找到喝水提醒 > 开启开关\n\n" +
            "三星：\n" +
            "设置 > 电池和设备维护 > 电池 > 后台应用程序 > 添加喝水提醒\n\n" +
            "原生安卓：\n" +
            "设置 > 电池 > 电池优化 > 不优化 > 选择喝水提醒";

        builder.setMessage(helpText);
        builder.setPositiveButton("我知道了", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "请允许精确闹钟权限以确保定时提醒", Toast.LENGTH_LONG).show();
                try {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    startActivityForResult(intent, REQUEST_SCHEDULE_EXACT_ALARM);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadSettings() {
        intervalMinutes = prefs.getLong("interval", 20);
        totalSeconds = intervalMinutes * 60;

        String lastDrank = prefs.getString("last_drink_time", "");
        if (!lastDrank.isEmpty()) {
            lastDrankText.setText("上次喝水: " + lastDrank);
        }
    }

    private void startTimer() {
        if (isRunning) return;

        isRunning = true;
        updateDisplay();
        updateStatus();
        startService();

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

    private void pauseTimer() {
        if (!isRunning) return;

        isRunning = false;
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        updateStatus();
    }

    private void markAsDrank() {
        String currentTime = java.text.DateFormat.getTimeInstance().format(new java.util.Date());
        prefs.edit().putString("last_drink_time", currentTime).apply();
        lastDrankText.setText("上次喝水: " + currentTime);

        totalSeconds = intervalMinutes * 60;
        updateDisplay();

        Toast.makeText(this, "已记录喝水时间", Toast.LENGTH_SHORT).show();
    }

    private void testReminder() {
        showNotification();
    }

    private void updateDisplay() {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        timerDisplay.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private void updateStatus() {
        if (isRunning) {
            statusText.setText("提醒已启动");
        } else {
            statusText.setText("提醒已暂停");
        }
    }

    private void showNotification() {
        vibrate();
        playRingtone();

        String title = prefs.getString("custom_message_title", "💧 该喝水啦！");
        String body = prefs.getString("custom_message_body", "站起来活动一下，喝杯水吧～");

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL);

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    private void vibrate() {
        if (vibrator != null && vibrator.hasVibrator()) {
            long[] pattern = {0, 500, 200, 500, 200, 500};
            vibrator.vibrate(pattern, -1);
        }
    }

    private void playRingtone() {
        if (ringtone != null && !ringtone.isPlaying()) {
            try {
                ringtone.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void startService() {
        Intent intent = new Intent(this, WaterReminderService.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
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
        channel.enableLights(true);

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSettings();
    }
}
