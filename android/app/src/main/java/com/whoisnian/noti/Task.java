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
    private final int tid;

    public Task(Context ctx, Map<String, String> data) {
        this.mContext = ctx;
        this.type = data.getOrDefault("Type", "ping");
        this.title = data.getOrDefault("Title", "");
        this.text = data.getOrDefault("Text", "");
        this.link = data.getOrDefault("Link", "");
        this.tid = (int) SystemClock.uptimeMillis();
    }

    public void show() {
        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Missing permission.POST_NOTIFICATIONS");
            return;
        }
        NotificationManagerCompat.from(mContext).notify(this.tid, this.buildNotification());
    }

    private Notification buildNotification() {
        String ChannelID = mContext.getString(R.string.fcm_channel_id);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, ChannelID);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setAutoCancel(true);

        switch (this.type) {
            case "ping":
                builder.setContentTitle("Ping");
                builder.setContentIntent(openMainIntent());
                break;
            case "text":
                if (!this.title.isEmpty()) builder.setContentTitle(this.title);
                builder.setContentText(this.text);
                builder.setContentIntent(openMainIntent());
                builder.addAction(0, "copy text", copyTextIntent(this.text, "copy_text"));
                break;
            case "link":
                if (!this.title.isEmpty()) builder.setContentTitle(this.title);
                String text = this.text.isEmpty() ? this.link : this.text;
                builder.setContentText(text);
                builder.setContentIntent(openLinkIntent(this.link));
                builder.addAction(0, "copy text", copyTextIntent(text, "copy_text"));
                builder.addAction(0, "copy link", copyTextIntent(this.link, "copy_link"));
                break;
        }
        return builder.build();
    }

    private Uri buildUri(String typ) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("noti");
        builder.authority("whoisnian.com");
        builder.appendPath("intent");
        builder.appendQueryParameter("tid", Integer.toString(this.tid));
        builder.appendQueryParameter("typ", typ);
        return builder.build();
    }

    private PendingIntent openMainIntent() {
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setData(buildUri("open_main"));
        return PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }

    private PendingIntent copyTextIntent(String text, String typ) {
        Intent intent = new Intent(mContext, BackgroundReceiver.class);
        intent.setAction("com.whoisnian.noti.COPY_TEXT");
        intent.setClipData(ClipData.newPlainText("text", text));
        intent.setData(buildUri(typ));
        return PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private PendingIntent openLinkIntent(String link) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        intent.setData(buildUri("open_link"));
        return PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }
}
