package com.whoisnian.noti;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import java.io.IOException;
import java.util.Collections;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PreferenceFragment extends PreferenceFragmentCompat {
    private static final String TAG = "PreferenceFragment";

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final OkHttpClient client = new OkHttpClient();

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
        namePreference.setOnPreferenceChangeListener((p, v) -> {
            httpPostJson("/api/device", "");
            return true;
        });
        // * Registry
        EditTextPreference registryPreference = new EditTextPreference(context);
        registryPreference.setKey("device_registry");
        registryPreference.setTitle("Registry");
        registryPreference.setIconSpaceReserved(false);
        registryPreference.setSummaryProvider(EditTextPreference.SimpleSummaryProvider.getInstance());
        registryPreference.setDialogTitle("Address");
        registryPreference.setDefaultValue(getString(R.string.app_registry));

        // Group Binding
        PreferenceCategory groupCategory = new PreferenceCategory(context);
        groupCategory.setTitle("Group Binding");
        groupCategory.setIconSpaceReserved(false);
        // * List
        String[] entries = shared.getStringSet("group_entries", Collections.emptySet()).toArray(new String[0]);
        ListPreference listPreference = new ListPreference(context);
        listPreference.setKey("group_list");
        listPreference.setTitle("List");
        listPreference.setIconSpaceReserved(false);
        listPreference.setSummaryProvider(p -> "Current " + listPreference.getEntries().length + " groups");
        listPreference.setDialogTitle("Select to quit");
        listPreference.setEntries(entries);
        listPreference.setEntryValues(entries);
        // * Join
        EditTextPreference joinPreference = new EditTextPreference(context);
        joinPreference.setKey("group_join");
        joinPreference.setTitle("Join");
        joinPreference.setIconSpaceReserved(false);
        joinPreference.setSummary("Create group if not exist");
        joinPreference.setDialogTitle("Group ID");
        // * Reset
        Preference resetPreference = new Preference(context);
        resetPreference.setKey("group_reset");
        resetPreference.setTitle("Reset");
        resetPreference.setIconSpaceReserved(false);
        resetPreference.setSummary("Quit from all groups");

        // About
        PreferenceCategory aboutCategory = new PreferenceCategory(context);
        aboutCategory.setTitle("About");
        aboutCategory.setIconSpaceReserved(false);
        // * Version
        Preference versionPreference = new Preference(context);
        versionPreference.setKey("about_version");
        versionPreference.setTitle("Version");
        versionPreference.setIconSpaceReserved(false);
        versionPreference.setSummary(BuildConfig.VERSION_NAME);
        // * GitHub
        Preference githubPreference = new Preference(context);
        githubPreference.setKey("about_github");
        githubPreference.setTitle("GitHub");
        githubPreference.setIconSpaceReserved(false);
        githubPreference.setSummary(R.string.app_repo);
        githubPreference.setOnPreferenceClickListener(p -> {
            Uri uri = Uri.parse(getString(R.string.app_repo));
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
            return true;
        });

        screen.addPreference(deviceCategory); // Device Management
        deviceCategory.addPreference(namePreference); // * Name
        deviceCategory.addPreference(registryPreference); // * Registry
        screen.addPreference(groupCategory); // Group Binding
        groupCategory.addPreference(listPreference); // * List
        groupCategory.addPreference(joinPreference); // * Join
        groupCategory.addPreference(resetPreference); // * Reset
        screen.addPreference(aboutCategory); // About
        aboutCategory.addPreference(versionPreference); // * Version
        aboutCategory.addPreference(githubPreference); // * GitHub
        setPreferenceScreen(screen);
    }

    private void httpPostJson(String path, String jsonBody) {
        String registry = getPreferenceManager().getSharedPreferences()
                .getString("device_registry", getString(R.string.app_registry));
        HttpUrl url = HttpUrl.parse(registry).newBuilder()
                .encodedPath(path)
                .build();
        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onFailure: ", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                response.close();
                Log.d(TAG, "onResponse: " + response.code());
            }
        });
    }
}