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
            Log.e(TAG, "Context must be an instance of Activity");
            return;
        }
        Activity activity = (Activity) context;

        try {
            // Get the currently signed-in Google account
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
            if (account == null) {
                Log.e(TAG, "No Google account signed in");
                activity.runOnUiThread(() -> {
                    Toast.makeText(activity, "Please sign in to your Google account first", Toast.LENGTH_SHORT).show();
                });
                return;
            }
            Log.d(TAG, "Successfully retrieved Google account: " + account.getEmail());

            // Create credentials
            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                    context,
                    Collections.singleton(CalendarScopes.CALENDAR_EVENTS)
            );
            credential.setSelectedAccount(account.getAccount());
            Log.d(TAG, "OAuth credential set");

            // Create Calendar service
            Calendar service = new Calendar.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    credential)
                    .setApplicationName("EventApp")
                    .build();
            Log.d(TAG, "Calendar service instance created");

            // Create an event
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

            Log.d(TAG, "Calendar event object created: " + googleEvent.getSummary());

            // Execute network request in a background thread
            new Thread(() -> {
                try {
                    Log.d(TAG, "Adding event to Google Calendar...");
                    // Insert event
                    com.google.api.services.calendar.model.Event createdEvent = service.events().insert("primary", googleEvent).execute();
                    Log.d(TAG, "Event added to Google Calendar: " + createdEvent.getHtmlLink());
                    activity.runOnUiThread(() -> {
                        Toast.makeText(activity, "Event added to Google Calendar", Toast.LENGTH_SHORT).show();
                    });
                } catch (UserRecoverableAuthIOException e) {
                    Log.d(TAG, "User authorization required, launching authorization flow");
                    activity.runOnUiThread(() -> {
                        activity.startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Failed to add event to Google Calendar: " + e.getMessage(), e);
                    activity.runOnUiThread(() -> {
                        Toast.makeText(activity, "Failed to add event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();

        } catch (Exception e) {
            Log.e(TAG, "Failed to configure Google Calendar service: " + e.getMessage(), e);
            activity.runOnUiThread(() -> {
                Toast.makeText(activity, "Failed to configure calendar service: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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