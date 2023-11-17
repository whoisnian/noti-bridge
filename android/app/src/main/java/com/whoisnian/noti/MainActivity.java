package com.whoisnian.noti;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private SQLiteDatabase DB;
    private ConstraintLayout layoutMain;
    private MenuItem optionsClear;
    private MenuItem optionsSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        layoutMain = new ConstraintLayout(this);
        layoutMain.setId(View.generateViewId());
        setContentView(layoutMain);
        setActionBarBackStack();
        setCurrentFragment(new HistoryFragment(), false);

        createNotificationChannel();
        checkRequestPermission();
        DB = new DatabaseHelper(this).getWritableDatabase();
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                Toast.makeText(this, "Fetching FCM registration token failed", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "FCM registration Token: " + task.getResult());
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                        .putString("fcm_token", task.getResult())
                        .commit();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        optionsClear = menu.add("clear");
        optionsSettings = menu.add("settings");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: " + item);
        if (item.equals(optionsClear)) {
            Task.deleteAllFromDB(DB);
        } else if (item.equals(optionsSettings)) {
            setCurrentFragment(new PreferenceFragment(), true);
        } else if (item.getItemId() == android.R.id.home) {
            getSupportFragmentManager().popBackStack();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // reset permission for test:
        // adb shell pm revoke com.whoisnian.noti android.permission.POST_NOTIFICATIONS
        // adb shell pm clear-permission-flags com.whoisnian.noti android.permission.POST_NOTIFICATIONS user-set
        // adb shell pm clear-permission-flags com.whoisnian.noti android.permission.POST_NOTIFICATIONS user-fixed
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Request permission success");
        } else {
            Log.w(TAG, "Request permission failed");
            Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
            startActivity(intent);
        }
    }

    @Override
    public void onDestroy() {
        DB.close();
        super.onDestroy();
    }

    private void createNotificationChannel() {
        String ChannelID = getString(R.string.fcm_channel_id);
        CharSequence ChannelName = getString(R.string.fcm_channel_name);
        Log.d(TAG, "Creating notification channel for " + ChannelID);
        NotificationChannel channel = new NotificationChannel(ChannelID, ChannelName, NotificationManager.IMPORTANCE_HIGH);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    private void checkRequestPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Missing " + android.Manifest.permission.POST_NOTIFICATIONS);
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 0);
        }
    }

    private void setActionBarBackStack() {
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0)
                getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP, ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
            else
                getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE, ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
        });
    }

    private void setCurrentFragment(Fragment fragment, boolean canBack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(layoutMain.getId(), fragment);
        if (canBack) transaction.addToBackStack(null);
        transaction.commit();
    }
}