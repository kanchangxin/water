package com.waterremind.app;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private static final int REQUEST_RINGTONE_PICKER = 102;

    private EditText intervalInput;
    private Button ringtoneBtn, saveBtn, backBtn, helpBtn;
    private Button increaseInterval, decreaseInterval;
    private Button nightStartTimeBtn, nightEndTimeBtn, napStartTimeBtn, napEndTimeBtn;
    private EditText customMessage, customMessageSub;
    private TextView ringtoneName;
    private SwitchCompat dndSwitch;

    private SharedPreferences prefs;
    private Ringtone ringtone;
    private Uri selectedRingtoneUri;

    private int nightStartHour = 22, nightStartMinute = 0;
    private int nightEndHour = 7, nightEndMinute = 0;
    private int napStartHour = 12, napStartMinute = 0;
    private int napEndHour = 14, napEndMinute = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("WaterRemind", MODE_PRIVATE);

        intervalInput = findViewById(R.id.intervalInput);
        increaseInterval = findViewById(R.id.increaseInterval);
        decreaseInterval = findViewById(R.id.decreaseInterval);
        ringtoneBtn = findViewById(R.id.ringtoneBtn);
        saveBtn = findViewById(R.id.saveBtn);
        backBtn = findViewById(R.id.backBtn);
        helpBtn = findViewById(R.id.helpBtn);
        nightStartTimeBtn = findViewById(R.id.nightStartTimeBtn);
        nightEndTimeBtn = findViewById(R.id.nightEndTimeBtn);
        napStartTimeBtn = findViewById(R.id.napStartTimeBtn);
        napEndTimeBtn = findViewById(R.id.napEndTimeBtn);
        customMessage = findViewById(R.id.customMessage);
        customMessageSub = findViewById(R.id.customMessageSub2);
        ringtoneName = findViewById(R.id.ringtoneName);
        dndSwitch = findViewById(R.id.dndSwitch);

        loadSettings();

        backBtn.setOnClickListener(v -> finish());

        increaseInterval.setOnClickListener(v -> adjustInterval(1));
        decreaseInterval.setOnClickListener(v -> adjustInterval(-1));

        ringtoneBtn.setOnClickListener(v -> showRingtonePicker());

        nightStartTimeBtn.setOnClickListener(v -> showTimePicker(true, true));
        nightEndTimeBtn.setOnClickListener(v -> showTimePicker(true, false));
        napStartTimeBtn.setOnClickListener(v -> showTimePicker(false, true));
        napEndTimeBtn.setOnClickListener(v -> showTimePicker(false, false));

        helpBtn.setOnClickListener(v -> showHelpDialog());

        saveBtn.setOnClickListener(v -> saveSettings());
    }

    private void loadSettings() {
        intervalInput.setText(String.valueOf(prefs.getLong("interval", 20)));

        String savedUri = prefs.getString("ringtone_uri", null);
        if (savedUri != null) {
            selectedRingtoneUri = Uri.parse(savedUri);
        } else {
            selectedRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        if (selectedRingtoneUri != null) {
            ringtone = RingtoneManager.getRingtone(this, selectedRingtoneUri);
            ringtoneName.setText(ringtone.getTitle(this));
        }

        customMessage.setText(prefs.getString("custom_message_title", "💧 该喝水啦！"));
        customMessageSub.setText(prefs.getString("custom_message_body", "站起来活动一下，喝杯水吧～"));

        dndSwitch.setChecked(prefs.getBoolean("dnd_enabled", false));

        nightStartHour = prefs.getInt("night_start_hour", 22);
        nightStartMinute = prefs.getInt("night_start_minute", 0);
        nightEndHour = prefs.getInt("night_end_hour", 7);
        nightEndMinute = prefs.getInt("night_end_minute", 0);
        napStartHour = prefs.getInt("nap_start_hour", 12);
        napStartMinute = prefs.getInt("nap_start_minute", 0);
        napEndHour = prefs.getInt("nap_end_hour", 14);
        napEndMinute = prefs.getInt("nap_end_minute", 0);

        updateTimeButtons();
    }

    private void adjustInterval(int delta) {
        try {
            long current = Long.parseLong(intervalInput.getText().toString());
            long newValue = current + delta;
            if (newValue < 1) newValue = 1;
            if (newValue > 180) newValue = 180;
            intervalInput.setText(String.valueOf(newValue));
        } catch (NumberFormatException e) {
            intervalInput.setText("20");
        }
    }

    private void updateTimeButtons() {
        nightStartTimeBtn.setText(String.format(Locale.getDefault(), "%02d:%02d", nightStartHour, nightStartMinute));
        nightEndTimeBtn.setText(String.format(Locale.getDefault(), "%02d:%02d", nightEndHour, nightEndMinute));
        napStartTimeBtn.setText(String.format(Locale.getDefault(), "%02d:%02d", napStartHour, napStartMinute));
        napEndTimeBtn.setText(String.format(Locale.getDefault(), "%02d:%02d", napEndHour, napEndMinute));
    }

    private void showTimePicker(boolean isNight, boolean isStart) {
        int hour, minute;
        if (isNight) {
            hour = isStart ? nightStartHour : nightEndHour;
            minute = isStart ? nightStartMinute : nightEndMinute;
        } else {
            hour = isStart ? napStartHour : napEndHour;
            minute = isStart ? napStartMinute : napEndMinute;
        }

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
            (view, selectedHour, selectedMinute) -> {
                if (isNight) {
                    if (isStart) {
                        nightStartHour = selectedHour;
                        nightStartMinute = selectedMinute;
                    } else {
                        nightEndHour = selectedHour;
                        nightEndMinute = selectedMinute;
                    }
                } else {
                    if (isStart) {
                        napStartHour = selectedHour;
                        napStartMinute = selectedMinute;
                    } else {
                        napEndHour = selectedHour;
                        napEndMinute = selectedMinute;
                    }
                }
                updateTimeButtons();
            },
            hour, minute, true);

        timePickerDialog.show();
    }

    private void showRingtonePicker() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "选择提醒铃声");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);

        String savedUri = prefs.getString("ringtone_uri", null);
        if (savedUri != null) {
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(savedUri));
        } else {
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        }

        startActivityForResult(intent, REQUEST_RINGTONE_PICKER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_RINGTONE_PICKER && resultCode == RESULT_OK) {
            Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                selectedRingtoneUri = uri;
                ringtone = RingtoneManager.getRingtone(this, selectedRingtoneUri);
                ringtoneName.setText(ringtone.getTitle(this));
                try {
                    ringtone.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void saveSettings() {
        try {
            long interval = Long.parseLong(intervalInput.getText().toString());
            if (interval < 1) {
                Toast.makeText(this, "提醒间隔不能小于1分钟", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences.Editor editor = prefs.edit();
            editor.putLong("interval", interval);
            editor.putBoolean("dnd_enabled", dndSwitch.isChecked());
            editor.putInt("night_start_hour", nightStartHour);
            editor.putInt("night_start_minute", nightStartMinute);
            editor.putInt("night_end_hour", nightEndHour);
            editor.putInt("night_end_minute", nightEndMinute);
            editor.putInt("nap_start_hour", napStartHour);
            editor.putInt("nap_start_minute", napStartMinute);
            editor.putInt("nap_end_hour", napEndHour);
            editor.putInt("nap_end_minute", napEndMinute);

            String title = customMessage.getText().toString().trim();
            String body = customMessageSub.getText().toString().trim();
            if (!title.isEmpty()) {
                editor.putString("custom_message_title", title);
            }
            if (!body.isEmpty()) {
                editor.putString("custom_message_body", body);
            }

            if (selectedRingtoneUri != null) {
                editor.putString("ringtone_uri", selectedRingtoneUri.toString());
            }

            editor.apply();

            Toast.makeText(this, "设置已保存", Toast.LENGTH_SHORT).show();
            finish();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效的数字", Toast.LENGTH_SHORT).show();
        }
    }

    private void showHelpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
}
