package com.example.eventapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.eventapp.database.DatabaseHelper;
import com.example.eventapp.model.Event;
import com.google.android.material.button.MaterialButton;
import com.example.eventapp.utils.CalendarHelper;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventDetailActivity extends AppCompatActivity {
    private TextView titleText;
    private TextView organizerText;
    private TextView timeText;
    private TextView locationText;
    private TextView participantsText;
    private TextView descriptionText;
    private MaterialButton joinButton;
    private MaterialButton addToCalendarButton;
    private ProgressBar loadingView;
    private DatabaseHelper dbHelper;
    private Event event;
    private long userId;
    private boolean isRegistered;
    private SimpleDateFormat dateFormat;
    private static final int CALENDAR_PERMISSION_REQUEST_CODE = 1001;
    private View adminButtonsContainer;
    private boolean isStaff;
    private static final int EDIT_EVENT_REQUEST = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        // 初始化
        initViews();
        setupToolbar();
        
        dbHelper = new DatabaseHelper(this);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        
        // 检查是否是工作人员
        SharedPreferences prefs = getSharedPreferences("EventApp", MODE_PRIVATE);
        isStaff = prefs.getBoolean("isStaff", false);
        userId = prefs.getLong("userId", -1);
        
        // 获取活动ID
        long eventId = getIntent().getLongExtra("event_id", -1);
        if (eventId == -1) {
            Toast.makeText(this, "活动ID无效", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // 加载活动数据
        loadEventData(eventId);
    }

    private void initViews() {
        titleText = findViewById(R.id.titleText);
        organizerText = findViewById(R.id.organizerText);
        timeText = findViewById(R.id.timeText);
        locationText = findViewById(R.id.locationText);
        participantsText = findViewById(R.id.participantsText);
        descriptionText = findViewById(R.id.descriptionText);
        joinButton = findViewById(R.id.joinButton);
        addToCalendarButton = findViewById(R.id.addToCalendarButton);
        loadingView = findViewById(R.id.loadingView);
        adminButtonsContainer = findViewById(R.id.adminButtonsContainer);
        
        joinButton.setOnClickListener(v -> toggleRegistration());
        addToCalendarButton.setOnClickListener(v -> addToCalendar());
        
        // 设置编辑和删除按钮点击事件
        findViewById(R.id.editButton).setOnClickListener(v -> editEvent());
        findViewById(R.id.deleteButton).setOnClickListener(v -> deleteEvent());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void loadEventData(long eventId) {
        showLoading(true);
        new Thread(() -> {
            try {
                // 先获取活动数据
                event = dbHelper.getEvent(eventId);
                // 再检查用户报名状态，注意参数顺序：先userId，后eventId
                isRegistered = dbHelper.isUserRegistered(userId, eventId);
                
                runOnUiThread(() -> {
                    if (event != null) {
                        updateUI();
                        // 如果是工作人员，显示管理按钮
                        adminButtonsContainer.setVisibility(isStaff ? View.VISIBLE : View.GONE);
                    } else {
                        Toast.makeText(this, "加载活动数据失败", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    showLoading(false);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "加载数据时出错：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
            }
        }).start();
    }

    private void updateUI() {
        if (event != null) {
            titleText.setText(event.getTitle());
            organizerText.setText(getString(R.string.organizer_label) + event.getOrganizer());
            timeText.setText(String.format("%s - %s",
                    dateFormat.format(new Date(event.getStartTime())),
                    dateFormat.format(new Date(event.getEndTime()))));
            locationText.setText(event.getLocation());
            participantsText.setText(String.format(getString(R.string.participants_label),
                    event.getCurrentParticipants(),
                    event.getMaxParticipants()));
            descriptionText.setText(event.getDescription());

            // 更新按钮状态
            updateButtonState();
        }
    }

    private void updateButtonState() {
        // 先检查活动是否已满
        boolean isFull = event.getCurrentParticipants() >= event.getMaxParticipants();

        if (isRegistered) {
            joinButton.setText(getString(R.string.cancel_registration));
            joinButton.setEnabled(true); // 已报名的用户始终可以取消报名
            joinButton.setIcon(getDrawable(android.R.drawable.ic_menu_close_clear_cancel));
            addToCalendarButton.setVisibility(View.VISIBLE);
        } else {
            joinButton.setText(isFull ? getString(R.string.event_full) : getString(R.string.join_event));
            joinButton.setEnabled(!isFull); // 未报名的用户只有在活动未满时才能报名
            joinButton.setIcon(getDrawable(android.R.drawable.ic_menu_add));
            addToCalendarButton.setVisibility(View.GONE);
        }
    }

    private void toggleRegistration() {
        if (event == null) {
            Toast.makeText(this, "活动数据未加载，请稍后重试", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        new Thread(() -> {
            boolean success;
            try {
                if (isRegistered) {
                    success = dbHelper.cancelRegistration(event.getId(), userId);
                    if (success) {
                        isRegistered = false;
                        event.setCurrentParticipants(event.getCurrentParticipants() - 1);
                    }
                } else {
                    success = dbHelper.registerEvent(event.getId(), userId);
                    if (success) {
                        isRegistered = true;
                        event.setCurrentParticipants(event.getCurrentParticipants() + 1);
                    }
                }
                
                final boolean finalSuccess = success;
                runOnUiThread(() -> {
                    if (finalSuccess) {
                        updateUI(); // 更新整个UI，包括参与人数和按钮状态
                        Toast.makeText(this, isRegistered ? "报名成功" : "已取消报名", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, isRegistered ? "取消报名失败" : "报名失败，活动可能已满", Toast.LENGTH_SHORT).show();
                    }
                    showLoading(false);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "操作失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
            }
        }).start();
    }

    private void addToCalendar() {
        if (event == null) {
            Toast.makeText(this, "活动信息不可用", Toast.LENGTH_SHORT).show();
            return;
        }

        if (checkCalendarPermissions()) {
            CalendarHelper.addEventToCalendar(this, event);
        } else {
            requestCalendarPermissions();
        }
    }

    private boolean checkCalendarPermissions() {
        boolean readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED;
        boolean writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED;
        return readPermission && writePermission;
    }

    private void requestCalendarPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.READ_CALENDAR,
                        Manifest.permission.WRITE_CALENDAR
                },
                CALENDAR_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CALENDAR_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                addToCalendar();
            } else {
                Toast.makeText(this, "需要日历权限才能添加活动", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_EVENT_REQUEST && resultCode == RESULT_OK) {
            // 重新加载活动数据
            loadEventData(event.getId());
        }
    }

    private void editEvent() {
        Intent intent = new Intent(this, EventEditActivity.class);
        intent.putExtra("event_id", event.getId());
        startActivityForResult(intent, EDIT_EVENT_REQUEST);
    }

    private void deleteEvent() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("删除活动")
            .setMessage("确定要删除这个活动吗？此操作不可恢复。")
            .setPositiveButton("删除", (dialog, which) -> {
                showLoading(true);
                new Thread(() -> {
                    boolean success = dbHelper.deleteEvent(event.getId());
                    runOnUiThread(() -> {
                        if (success) {
                            Toast.makeText(this, "活动已删除", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(this, "删除活动失败", Toast.LENGTH_SHORT).show();
                        }
                        showLoading(false);
                    });
                }).start();
            })
            .setNegativeButton("取消", null)
            .show();
    }

    private void showLoading(boolean show) {
        loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
        joinButton.setEnabled(!show);
        addToCalendarButton.setEnabled(!show);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 