package com.example.eventapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EventEditActivity extends AppCompatActivity {
    private TextInputEditText titleInput;
    private TextInputEditText descriptionInput;
    private TextInputEditText locationInput;
    private TextInputEditText startTimeInput;
    private TextInputEditText endTimeInput;
    private TextInputEditText maxParticipantsInput;
    private View loadingView;
    private DatabaseHelper dbHelper;
    private SimpleDateFormat dateFormat;
    private Event currentEvent;
    private Calendar startTime;
    private Calendar endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_edit);

        // 初始化
        initViews();
        setupToolbar();
        
        dbHelper = new DatabaseHelper(this);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        
        // 获取要编辑的活动ID
        long eventId = getIntent().getLongExtra("event_id", -1);
        if (eventId == -1) {
            Toast.makeText(this, "Invalid Event ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 加载活动数据
        loadEventData(eventId);
        
        // 设置时间选择器
        setupTimeInputs();
        
        // 设置更新按钮
        findViewById(R.id.updateButton).setOnClickListener(v -> updateEvent());
    }

    private void initViews() {
        titleInput = findViewById(R.id.titleInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        locationInput = findViewById(R.id.locationInput);
        startTimeInput = findViewById(R.id.startTimeInput);
        endTimeInput = findViewById(R.id.endTimeInput);
        maxParticipantsInput = findViewById(R.id.maxParticipantsInput);
        loadingView = findViewById(R.id.loadingView);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Event");
        }
    }

    private void loadEventData(long eventId) {
        showLoading(true);
        new Thread(() -> {
            currentEvent = dbHelper.getEvent(eventId);
            runOnUiThread(() -> {
                if (currentEvent != null) {
                    titleInput.setText(currentEvent.getTitle());
                    descriptionInput.setText(currentEvent.getDescription());
                    locationInput.setText(currentEvent.getLocation());
                    startTimeInput.setText(dateFormat.format(new Date(currentEvent.getStartTime())));
                    endTimeInput.setText(dateFormat.format(new Date(currentEvent.getEndTime())));
                    maxParticipantsInput.setText(String.valueOf(currentEvent.getMaxParticipants()));
                    
                    startTime = Calendar.getInstance();
                    endTime = Calendar.getInstance();
                    startTime.setTimeInMillis(currentEvent.getStartTime());
                    endTime.setTimeInMillis(currentEvent.getEndTime());
                } else {
                    Toast.makeText(this, "Failed to load event data", Toast.LENGTH_SHORT).show();
                    finish();
                }
                showLoading(false);
            });
        }).start();
    }

    private void setupTimeInputs() {
        View.OnClickListener timeClickListener = v -> {
            final Calendar calendar = v.getId() == R.id.startTimeInput ? startTime : endTime;
            final TextInputEditText input = (TextInputEditText) v;

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    new TimePickerDialog(this,
                        (view1, hourOfDay, minute) -> {
                            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            calendar.set(Calendar.MINUTE, minute);
                            input.setText(dateFormat.format(calendar.getTime()));
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true).show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        };

        startTimeInput.setOnClickListener(timeClickListener);
        endTimeInput.setOnClickListener(timeClickListener);
    }

    private void updateEvent() {
        if (!validateInput()) {
            return;
        }

        showLoading(true);
        new Thread(() -> {
            try {
                currentEvent.setTitle(titleInput.getText().toString().trim());
                currentEvent.setDescription(descriptionInput.getText().toString().trim());
                currentEvent.setLocation(locationInput.getText().toString().trim());
                Date startDate = dateFormat.parse(startTimeInput.getText().toString());
                Date endDate = dateFormat.parse(endTimeInput.getText().toString());
                currentEvent.setStartTime(startDate.getTime());
                currentEvent.setEndTime(endDate.getTime());
                currentEvent.setMaxParticipants(Integer.parseInt(maxParticipantsInput.getText().toString()));

                boolean success = dbHelper.updateEvent(currentEvent);
                runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(this, "Event updated successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(this, "Event update failed", Toast.LENGTH_SHORT).show();
                    }
                    showLoading(false);
                });
            } catch (ParseException e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
            }
        }).start();
    }

    private boolean validateInput() {
        if (TextUtils.isEmpty(titleInput.getText())) {
            titleInput.setError("Please enter the event title");
            return false;
        }
        if (TextUtils.isEmpty(descriptionInput.getText())) {
            descriptionInput.setError("Please enter the event description");
            return false;
        }
        if (TextUtils.isEmpty(locationInput.getText())) {
            locationInput.setError("Please enter the event location");
            return false;
        }
        if (TextUtils.isEmpty(startTimeInput.getText())) {
            startTimeInput.setError("Please select a start time");
            return false;
        }
        if (TextUtils.isEmpty(endTimeInput.getText())) {
            endTimeInput.setError("Please select an end time");
            return false;
        }
        if (TextUtils.isEmpty(maxParticipantsInput.getText())) {
            maxParticipantsInput.setError("Please enter the maximum number of participants");
            return false;
        }
        
        try {
            Date startDate = dateFormat.parse(startTimeInput.getText().toString());
            Date endDate = dateFormat.parse(endTimeInput.getText().toString());
            if (startDate != null && endDate != null && startDate.after(endDate)) {
                startTimeInput.setError("End time cannot be earlier than start time");
                return false;
            }
        } catch (ParseException e) {
            Toast.makeText(this, "Invalid date format", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            int maxParticipants = Integer.parseInt(maxParticipantsInput.getText().toString().trim());
            if (maxParticipants <= 0) {
                maxParticipantsInput.setError("Number of participants must be greater than 0");
                return false;
            }
        } catch (NumberFormatException e) {
            maxParticipantsInput.setError("Please enter a valid number");
            return false;
        }

        return true;
    }

    private void showLoading(boolean show) {
        loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
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