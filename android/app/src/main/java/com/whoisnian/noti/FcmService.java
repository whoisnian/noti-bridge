package com.whoisnian.noti;

import android.util.Log;

import androidx.preference.PreferenceManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Date;
import java.util.Map;

public class FcmService extends FirebaseMessagingService {
    private static final String TAG = "FcmService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        new Task(this,
                0,
                data.getOrDefault("Type", "ping"),
                data.getOrDefault("Title", ""),
                data.getOrDefault("Text", ""),
                data.getOrDefault("Link", ""),
                new Date()
        ).showNotification();
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString("fcm_token", token)
                .commit();
    }
}