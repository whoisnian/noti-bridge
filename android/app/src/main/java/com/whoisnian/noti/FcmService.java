package com.whoisnian.noti;

import android.util.Log;

import androidx.preference.PreferenceManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FcmService extends FirebaseMessagingService {
    private static final String TAG = "FcmService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Task task = new Task(this, remoteMessage.getData());
        task.showNotification();
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString("fcm_token", token)
                .commit();
    }
}