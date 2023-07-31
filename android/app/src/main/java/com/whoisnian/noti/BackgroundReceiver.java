package com.whoisnian.noti;

import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class BackgroundReceiver extends BroadcastReceiver {
    private static final String TAG = "BackgroundReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received intent " + intent.toString());
        if (intent.getAction() != null && intent.getAction().equals("com.whoisnian.noti.COPY_TEXT")) {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setPrimaryClip(intent.getClipData());
            Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show();
        }
    }
}

