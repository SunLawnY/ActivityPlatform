package com.example.eventapp.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.EventDateTime;
import java.util.Collections;

public class CalendarHelper {
    private static final String TAG = "CalendarHelper";
    private static final int CALENDAR_PERMISSION_REQUEST_CODE = 1000;
    public static final int REQUEST_AUTHORIZATION = 1001;

    public static void addEventToCalendar(Context context, com.example.eventapp.model.Event appEvent) {
        if (!(context instanceof Activity)) {
            Log.e(TAG, "Context必须是Activity实例");
            return;
        }
        Activity activity = (Activity) context;

        try {
            // 获取当前登录的Google账号
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
            if (account == null) {
                Log.e(TAG, "未登录Google账号");
                activity.runOnUiThread(() -> {
                    Toast.makeText(activity, "请先登录Google账号", Toast.LENGTH_SHORT).show();
                });
                return;
            }
            Log.d(TAG, "成功获取Google账号: " + account.getEmail());

            // 创建凭证
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                    context,
                    Collections.singleton(CalendarScopes.CALENDAR_EVENTS)
            );
            credential.setSelectedAccount(account.getAccount());
            Log.d(TAG, "已设置OAuth凭证");

            // 创建Calendar服务
            Calendar service = new Calendar.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    credential)
                    .setApplicationName("EventApp")
                    .build();
            Log.d(TAG, "已创建Calendar服务实例");

            // 创建事件
            com.google.api.services.calendar.model.Event googleEvent = new com.google.api.services.calendar.model.Event()
                    .setSummary(appEvent.getTitle())
                    .setDescription(appEvent.getDescription())
                    .setLocation(appEvent.getLocation());

            DateTime startDateTime = new DateTime(appEvent.getStartTime());
            EventDateTime start = new EventDateTime()
                    .setDateTime(startDateTime)
                    .setTimeZone("Asia/Shanghai");
            googleEvent.setStart(start);

            DateTime endDateTime = new DateTime(appEvent.getEndTime());
            EventDateTime end = new EventDateTime()
                    .setDateTime(endDateTime)
                    .setTimeZone("Asia/Shanghai");
            googleEvent.setEnd(end);

            Log.d(TAG, "已创建日历事件对象: " + googleEvent.getSummary());

            // 在后台线程中执行网络请求
            new Thread(() -> {
                try {
                    Log.d(TAG, "开始添加事件到Google日历...");
                    // 插入事件
                    com.google.api.services.calendar.model.Event createdEvent = service.events().insert("primary", googleEvent).execute();
                    Log.d(TAG, "事件已添加到Google日历: " + createdEvent.getHtmlLink());
                    activity.runOnUiThread(() -> {
                        Toast.makeText(activity, "事件已添加到Google日历", Toast.LENGTH_SHORT).show();
                    });
                } catch (UserRecoverableAuthIOException e) {
                    Log.d(TAG, "需要用户授权，启动授权流程");
                    activity.runOnUiThread(() -> {
                        activity.startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
                    });
                } catch (Exception e) {
                    Log.e(TAG, "添加事件到Google日历失败: " + e.getMessage(), e);
                    activity.runOnUiThread(() -> {
                        Toast.makeText(activity, "添加事件失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();

        } catch (Exception e) {
            Log.e(TAG, "配置Google日历服务失败: " + e.getMessage(), e);
            activity.runOnUiThread(() -> {
                Toast.makeText(activity, "配置日历服务失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private static boolean checkCalendarPermissions(Activity activity) {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED;
    }

    private static void requestCalendarPermissions(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{
                        Manifest.permission.READ_CALENDAR,
                        Manifest.permission.WRITE_CALENDAR
                },
                CALENDAR_PERMISSION_REQUEST_CODE);
    }
} 