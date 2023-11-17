package com.whoisnian.noti;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ContentValues;
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
    public static final String ACTION_COPY_TEXT = "com.whoisnian.noti.COPY_TEXT";
    public static final String SQL_CREATE_TABLE = "CREATE TABLE tasks("
            + "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "type TEXT NOT NULL,"
            + "title TEXT NOT NULL,"
            + "text TEXT NOT NULL,"
            + "link TEXT NOT NULL,"
            + "ctime INTEGER NOT NULL)";
    public static final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS tasks";
    private static final String TAG = "Task";
    public final String type, title, text, link;
    public final Date ctime;
    public long tid;

    public Task(long tid, String type, String title, String text, String link, Date ctime) {
        this.tid = tid;
        this.type = type;
        this.title = title;
        this.text = text;
        this.link = link;
        this.ctime = ctime;
    }

    public static List<Task> loadAllFromDB(SQLiteDatabase db) {
        Cursor cursor = db.query("tasks", null, null, null, null, null, "_id desc");
        int idxTID = cursor.getColumnIndex("_id");
        int idxType = cursor.getColumnIndex("type");
        int idxTitle = cursor.getColumnIndex("title");
        int idxText = cursor.getColumnIndex("text");
        int idxLink = cursor.getColumnIndex("link");
        int idxCtime = cursor.getColumnIndex("ctime");
        List<Task> tasks = new ArrayList<Task>(cursor.getCount());
        while (cursor.moveToNext()) {
            tasks.add(new Task(
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

    public static int deleteAllFromDB(SQLiteDatabase db) {
        return db.delete("tasks", null, null);
    }

    public long insertIntoDB(SQLiteDatabase db) {
        if (this.tid != -1) return this.tid;

        ContentValues values = new ContentValues();
        values.put("type", this.type);
        values.put("title", this.title);
        values.put("text", this.text);
        values.put("link", this.link);
        values.put("ctime", this.ctime.getTime());
        this.tid = db.insert("tasks", null, values);
        return this.tid;
    }

    public int deleteFromDB(SQLiteDatabase db) {
        return db.delete("tasks", "_id = ?", new String[]{Long.toString(this.tid)});
    }

    public void showNotification(Context ctx) {
        if (ctx == null) return;

        if (ActivityCompat.checkSelfPermission(ctx, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Missing " + android.Manifest.permission.POST_NOTIFICATIONS);
            return;
        }
        NotificationManagerCompat.from(ctx).notify((int) this.tid, this.buildNotification(ctx));
    }

    private Notification buildNotification(Context ctx) {
        String ChannelID = ctx.getString(R.string.fcm_channel_id);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, ChannelID);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setAutoCancel(true);

        switch (this.type) {
            case "ping":
                builder.setContentTitle("Ping");
                builder.setContentIntent(openMainIntent(ctx));
                break;
            case "text":
                if (!this.title.isEmpty()) builder.setContentTitle(this.title);
                builder.setContentText(this.text);
                builder.setContentIntent(openMainIntent(ctx));
                builder.addAction(0, "copy text", copyTextIntent(ctx, this.text));
                break;
            case "link":
                if (!this.title.isEmpty()) builder.setContentTitle(this.title);
                builder.setContentText(this.link);
                builder.setContentIntent(openLinkIntent(ctx, this.link));
                builder.addAction(0, "copy link", copyTextIntent(ctx, this.link));
                break;
        }
        return builder.build();
    }

    private PendingIntent openMainIntent(Context ctx) {
        Intent intent = new Intent(ctx, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }

    private PendingIntent copyTextIntent(Context ctx, String text) {
        Intent intent = new Intent(ctx, BackgroundReceiver.class);
        intent.setAction(ACTION_COPY_TEXT);
        intent.setClipData(ClipData.newPlainText("text", text));
        intent.setData(Uri.parse("noti://whoisnian.com/intent?tid=" + this.tid));
        return PendingIntent.getBroadcast(ctx, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private PendingIntent openLinkIntent(Context ctx, String link) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        return PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_IMMUTABLE);
    }
}
