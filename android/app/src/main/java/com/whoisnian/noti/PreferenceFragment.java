package com.whoisnian.noti;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import java.util.Collections;

public class PreferenceFragment extends PreferenceFragmentCompat {
    private static final String TAG = "PreferenceFragment";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = getPreferenceManager().getContext();
        SharedPreferences shared = getPreferenceManager().getSharedPreferences();
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(context);

        // Device Management
        PreferenceCategory deviceCategory = new PreferenceCategory(context);
        deviceCategory.setTitle("Device Management");
        deviceCategory.setIconSpaceReserved(false);
        // * Name
        EditTextPreference namePreference = new EditTextPreference(context);
        namePreference.setKey("device_name");
        namePreference.setTitle("Name");
        namePreference.setIconSpaceReserved(false);
        namePreference.setSummaryProvider(EditTextPreference.SimpleSummaryProvider.getInstance());
        namePreference.setDialogTitle("Rename");
        namePreference.setDefaultValue(Build.BRAND + " " + Build.MODEL);

        // Group Binding
        PreferenceCategory groupCategory = new PreferenceCategory(context);
        groupCategory.setTitle("Group Binding");
        groupCategory.setIconSpaceReserved(false);
        // * List
        String[] entries = shared.getStringSet("entries", Collections.emptySet()).toArray(new String[0]);
        ListPreference listPreference = new ListPreference(context);
        listPreference.setKey("show_list");
        listPreference.setTitle("List");
        listPreference.setIconSpaceReserved(false);
        listPreference.setSummaryProvider(p -> "Currently joined " + listPreference.getEntries().length + " groups");
        listPreference.setDialogTitle("Select to quit");
        listPreference.setEntries(entries);
        listPreference.setEntryValues(entries);
        // * Add New
        EditTextPreference joinPreference = new EditTextPreference(context);
        joinPreference.setKey("join_new");
        joinPreference.setTitle("Add New");
        joinPreference.setIconSpaceReserved(false);
        joinPreference.setSummary("Auto create group if not exist");
        joinPreference.setDialogTitle("Group ID");
        // * Reset
        Preference resetPreference = new Preference(context);
        resetPreference.setKey("reset");
        resetPreference.setTitle("Reset");
        resetPreference.setIconSpaceReserved(false);
        resetPreference.setSummary("Quit from all groups");

        // About
        PreferenceCategory aboutCategory = new PreferenceCategory(context);
        aboutCategory.setTitle("About");
        aboutCategory.setIconSpaceReserved(false);
        // * Version
        Preference versionPreference = new Preference(context);
        versionPreference.setKey("version");
        versionPreference.setTitle("Version");
        versionPreference.setIconSpaceReserved(false);
        versionPreference.setSummary("1.0");
        // * GitHub
        Preference githubPreference = new Preference(context);
        githubPreference.setKey("github");
        githubPreference.setTitle("GitHub");
        githubPreference.setIconSpaceReserved(false);
        githubPreference.setSummary("https://github.com/whoisnian/noti-bridge");

        screen.addPreference(deviceCategory); // Device Management
        deviceCategory.addPreference(namePreference); // * Name
        screen.addPreference(groupCategory); // Group Binding
        groupCategory.addPreference(listPreference); // * List
        groupCategory.addPreference(joinPreference); // * Add New
        groupCategory.addPreference(resetPreference); // * Reset
        screen.addPreference(aboutCategory); // About
        aboutCategory.addPreference(versionPreference); // * Version
        aboutCategory.addPreference(githubPreference); // * GitHub
        setPreferenceScreen(screen);
    }
}