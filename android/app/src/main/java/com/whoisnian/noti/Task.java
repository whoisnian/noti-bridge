package com.whoisnian.noti;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.SystemClock;
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
    private final int uid;

    public Task(Context ctx, Map<String, String> data) {
        this.mContext = ctx;
        this.type = data.getOrDefault("Type", "ping");
        this.title = data.getOrDefault("Title", "");
        this.text = data.getOrDefault("Text", "");
        this.link = data.getOrDefault("Link", "");
        this.uid = (int) SystemClock.uptimeMillis();
    }

    public void show() {
        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Missing permission.POST_NOTIFICATIONS");
            return;
        }
        NotificationManagerCompat.from(mContext).notify(this.uid, this.buildNotification());
    }

    private Notification buildNotification() {
        String ChannelID = mContext.getString(R.string.fcm_channel_id);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, ChannelID);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentIntent(openMainIntent());
        builder.setAutoCancel(true);

        switch (this.type) {
            case "ping":
                builder.setContentTitle("Ping");
                break;
            case "text":
                if (!this.title.isEmpty()) builder.setContentTitle(this.title);
                builder.setContentText(this.text);
                builder.addAction(0, "copy text", copyTextIntent());
                break;
            case "link":
                if (!this.title.isEmpty()) builder.setContentTitle(this.title);
                builder.setContentText(this.text.isEmpty() ? this.link : this.text);
                builder.addAction(0, "copy text", copyTextIntent());
                builder.addAction(0, "open link", openLinkIntent());
                break;
        }
        return builder.build();
    }

    private PendingIntent openMainIntent() {
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }

    private PendingIntent copyTextIntent() {
        Intent copy = new Intent(mContext, BackgroundReceiver.class);
        copy.setAction("com.whoisnian.noti.COPY_TEXT");
        copy.setClipData(ClipData.newPlainText("text", this.text.isEmpty() ? this.link : this.text));
        return PendingIntent.getBroadcast(mContext, 0, copy, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private PendingIntent openLinkIntent() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(this.link));
        return PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }
}
