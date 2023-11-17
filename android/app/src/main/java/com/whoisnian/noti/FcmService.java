package com.whoisnian.noti;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Date;
import java.util.Map;

public class FcmService extends FirebaseMessagingService {
    private static final String TAG = "FcmService";

    private SQLiteDatabase DB;

    @Override
    public void onCreate() {
        super.onCreate();
        this.DB = new DatabaseHelper(this).getWritableDatabase();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        Task task = new Task(-1,
                data.getOrDefault("Type", "ping"),
                data.getOrDefault("Title", ""),
                data.getOrDefault("Text", ""),
                data.getOrDefault("Link", ""),
                new Date()
        );
        task.insertIntoDB(this.DB);
        task.showNotification(this);
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString("fcm_token", token)
                .commit();
    }

    @Override
    public void onDestroy() {
        this.DB.close();
        super.onDestroy();
    }
}