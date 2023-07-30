package com.whoisnian.noti;

import android.app.Notification;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Map;

public class Task {
    private static final String TAG = "Task";

    private final Context mContext;

    private final String type; // "ping" | "text" | "link"
    private final String title, text, link;

    public Task(Context ctx, Map<String, String> data) {
        this.mContext = ctx;
        this.type = data.getOrDefault("Type", "ping");
        this.title = data.getOrDefault("Title", "");
        this.text = data.getOrDefault("Text", "");
        this.link = data.getOrDefault("Link", "");
    }

    public void show() {
        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Missing permission.POST_NOTIFICATIONS");
            return;
        }
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
        notificationManager.notify(0, this.buildNotification());
    }

    private Notification buildNotification() {
        String ChannelID = mContext.getString(R.string.fcm_channel_id);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, ChannelID);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLocalOnly(true);
        builder.setAutoCancel(false);

        if (this.type.equals("ping")) {
            builder.setContentTitle("Ping");
        } else if (this.type.equals("text")) {
            if (!this.title.isEmpty()) builder.setContentTitle(this.title);
            builder.setContentText(this.text);
        } else if (this.type.equals("link")) {
            if (!this.title.isEmpty()) builder.setContentTitle(this.title);
            if (!this.text.isEmpty()) builder.setContentText(this.text);
            else builder.setContentText(this.link);
        }
        return builder.build();
    }
}
