package com.whoisnian.noti;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Task {
    private static final String TAG = "Task";

    public static final String ACTION_COPY_TEXT = "com.whoisnian.noti.COPY_TEXT";
    public static final String SQL_CREATE_TABLE = "CREATE TABLE tasks("
            + "tid INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "type TEXT NOT NULL,"
            + "title TEXT NOT NULL,"
            + "text TEXT NOT NULL,"
            + "link TEXT NOT NULL,"
            + "ctime INTEGER NOT NULL)";
    public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS tasks";

    public final Context mContext;
    public final long tid;
    public final String type, title, text, link;
    public final Date ctime;

    public Task(Context ctx, long tid, String type, String title, String text, String link, Date ctime) {
        this.mContext = ctx;
        this.tid = tid;
        this.type = type;
        this.title = title;
        this.text = text;
        this.link = link;
        this.ctime = ctime;
    }

    public void insertToDB(SQLiteDatabase db) {

    }

    public void deleteFromDB(SQLiteDatabase db) {

    }

    public static List<Task> loadTasksFromDB(SQLiteDatabase db) {
        Cursor cursor = db.query("tasks", null, null, null, null, null, null);
        int idxTID = cursor.getColumnIndex("tid");
        int idxType = cursor.getColumnIndex("type");
        int idxTitle = cursor.getColumnIndex("title");
        int idxText = cursor.getColumnIndex("text");
        int idxLink = cursor.getColumnIndex("link");
        int idxCtime = cursor.getColumnIndex("ctime");
        List<Task> tasks = new ArrayList<Task>(cursor.getCount());
        while (cursor.moveToNext()) {
            tasks.add(new Task(
                    null,
                    cursor.getLong(idxTID),
                    cursor.getString(idxType),
                    cursor.getString(idxTitle),
                    cursor.getString(idxText),
                    cursor.getString(idxLink),
                    new Date(cursor.getLong(idxCtime))
            ));
        }
        cursor.close();
        return tasks;
    }

    public void showNotification() {
        if (mContext == null) return;

        if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Missing " + android.Manifest.permission.POST_NOTIFICATIONS);
            return;
        }
        NotificationManagerCompat.from(mContext).notify((int) this.tid, this.buildNotification());
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
        builder.appendQueryParameter("tid", Long.toString(this.tid));
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
        intent.setAction(ACTION_COPY_TEXT);
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
