package com.whoisnian.noti;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;


public class PreferenceFragment extends PreferenceFragmentCompat {
    private static final String TAG = "PreferenceFragment";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = getPreferenceManager().getContext();
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);

        // PreferenceCategory Stat Test
        PreferenceCategory statCategory = new PreferenceCategory(context);
        statCategory.setTitle("Stat Test");
        statCategory.setIconSpaceReserved(false);

        Preference datePreference = new Preference(context);
        datePreference.setKey("date");
        datePreference.setTitle("Date");
        datePreference.setIconSpaceReserved(false);
        datePreference.setOnPreferenceClickListener(preference -> {
            Log.d(TAG, "click date");
            return true;
        });

        screen.addPreference(statCategory);
        statCategory.addPreference(datePreference);

        setPreferenceScreen(screen);
    }
}