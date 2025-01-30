package com.example.eventapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.eventapp.database.DatabaseHelper;
import com.example.eventapp.model.Event;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CreateEventActivity extends AppCompatActivity {
    private TextInputEditText titleInput;
    private TextInputEditText descriptionInput;
    private TextInputEditText locationInput;
    private TextInputEditText startTimeInput;
    private TextInputEditText endTimeInput;
    private TextInputEditText maxParticipantsInput;
    private DatabaseHelper dbHelper;
    private Calendar startTime;
    private Calendar endTime;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        // 初始化
        initViews();
        setupToolbar();
        dbHelper = new DatabaseHelper(this);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        startTime = Calendar.getInstance();
        endTime = Calendar.getInstance();
        endTime.add(Calendar.HOUR_OF_DAY, 1);

        // 设置时间选择器
        setupTimeInputs();

        // 设置创建按钮
        findViewById(R.id.createButton).setOnClickListener(v -> createEvent());
    }

    private void initViews() {
        titleInput = findViewById(R.id.titleInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        locationInput = findViewById(R.id.locationInput);
        startTimeInput = findViewById(R.id.startTimeInput);
        endTimeInput = findViewById(R.id.endTimeInput);
        maxParticipantsInput = findViewById(R.id.maxParticipantsInput);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupTimeInputs() {
        startTimeInput.setText(dateFormat.format(startTime.getTime()));
        endTimeInput.setText(dateFormat.format(endTime.getTime()));

        View.OnClickListener timeClickListener = v -> {
            final Calendar calendar = Calendar.getInstance();
            final boolean isStartTime = v.getId() == R.id.startTimeInput;

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        
                        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                                (view1, hourOfDay, minute) -> {
                                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    calendar.set(Calendar.MINUTE, minute);

                                    if (isStartTime) {
                                        startTime = calendar;
                                        startTimeInput.setText(dateFormat.format(startTime.getTime()));
                                        // 如果结束时间早于开始时间，自动调整结束时间
                                        if (endTime.before(startTime)) {
                                            endTime = (Calendar) startTime.clone();
                                            endTime.add(Calendar.HOUR_OF_DAY, 1);
                                            endTimeInput.setText(dateFormat.format(endTime.getTime()));
                                        }
                                    } else {
                                        endTime = calendar;
                                        endTimeInput.setText(dateFormat.format(endTime.getTime()));
                                    }
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                true);
                        timePickerDialog.show();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        };

        startTimeInput.setOnClickListener(timeClickListener);
        endTimeInput.setOnClickListener(timeClickListener);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_save) {
            createEvent();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createEvent() {
        // 验证输入
        if (!validateInput()) {
            return;
        }

        // 获取当前用户作为组织者
        SharedPreferences prefs = getSharedPreferences("EventApp", MODE_PRIVATE);
        String organizer = prefs.getString("username", "");

        // 创建事件对象
        Event event = new Event();
        event.setTitle(titleInput.getText().toString().trim());
        event.setDescription(descriptionInput.getText().toString().trim());
        event.setLocation(locationInput.getText().toString().trim());
        event.setStartTime(startTime.getTimeInMillis());
        event.setEndTime(endTime.getTimeInMillis());
        event.setOrganizer(organizer);
        event.setMaxParticipants(Integer.parseInt(maxParticipantsInput.getText().toString().trim()));
        event.setCurrentParticipants(0);

        // 保存到数据库
        long eventId = dbHelper.insertEvent(event);
        if (eventId != -1) {
            Toast.makeText(this, "活动创建成功", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "创建失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(titleInput.getText())) {
            titleInput.setError("请输入活动标题");
            return false;
        }

        if (TextUtils.isEmpty(descriptionInput.getText())) {
            descriptionInput.setError("请输入活动描述");
            return false;
        }

        if (TextUtils.isEmpty(locationInput.getText())) {
            locationInput.setError("请输入活动地点");
            return false;
        }

        if (TextUtils.isEmpty(startTimeInput.getText())) {
            startTimeInput.setError("请选择开始时间");
            return false;
        }

        if (TextUtils.isEmpty(endTimeInput.getText())) {
            endTimeInput.setError("请选择结束时间");
            return false;
        }

        if (endTime.before(startTime)) {
            endTimeInput.setError("结束时间不能早于开始时间");
            return false;
        }

        if (TextUtils.isEmpty(maxParticipantsInput.getText())) {
            maxParticipantsInput.setError("请输入最大参与人数");
            return false;
        }

        try {
            int maxParticipants = Integer.parseInt(maxParticipantsInput.getText().toString().trim());
            if (maxParticipants <= 0) {
                maxParticipantsInput.setError("参与人数必须大于0");
                return false;
            }
        } catch (NumberFormatException e) {
            maxParticipantsInput.setError("请输入有效的数字");
            return false;
        }

        return true;
    }
} 