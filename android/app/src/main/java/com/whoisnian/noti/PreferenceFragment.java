package com.whoisnian.noti;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

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
    private final OkHttpClient okClient = new OkHttpClient.Builder().connectTimeout(1, TimeUnit.SECONDS).build();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        EditTextPreference namePreference = findPreference("device_name");
        namePreference.setDefaultValue(Build.BRAND + " " + Build.MODEL);
        namePreference.setOnPreferenceChangeListener((p, v) -> {
            JSONObject obj = new JSONObject();
            try {
                obj.put("Type", 0);
                obj.put("Token", getPreferenceManager().getSharedPreferences().getString("fcm_token", ""));
                obj.put("Name", v.toString());
            } catch (JSONException e) {
                showDialog("Error", e.getMessage());
                return false;
            }
            asyncRequest("/api/device", "POST", obj.toString(), respCallback(res -> namePreference.setText(v.toString())));
            return false;
        });

        String[] entries = getPreferenceManager().getSharedPreferences().getStringSet("group_entries", new HashSet<>()).toArray(new String[0]);
        ListPreference listPreference = findPreference("group_list");
        listPreference.setEntries(entries);
        listPreference.setEntryValues(entries);
        listPreference.setSummaryProvider(p -> "Current " + listPreference.getEntries().length + " groups");
        listPreference.setOnPreferenceChangeListener((p, v) -> {
            JSONObject obj = new JSONObject();
            JSONArray arr = new JSONArray();
            try {
                arr.put(v.toString());
                obj.put("GIDs", arr);
                obj.put("Type", 0);
                obj.put("Token", getPreferenceManager().getSharedPreferences().getString("fcm_token", ""));
            } catch (JSONException e) {
                showDialog("Error", e.getMessage());
                return false;
            }
            asyncRequest("/api/group", "DELETE", obj.toString(), respCallback(res -> {
                HashSet<String> entrySet = new HashSet<>(getPreferenceManager().getSharedPreferences().getStringSet("group_entries", new HashSet<>()));
                entrySet.remove(v.toString());
                String[] newEntries = entrySet.toArray(new String[0]);
                listPreference.setEntries(newEntries);
                listPreference.setEntryValues(newEntries);
                listPreference.setValue(null);
                getPreferenceManager().getSharedPreferences().edit()
                        .putStringSet("group_entries", entrySet)
                        .commit();
            }));
            return false;
        });

        EditTextPreference joinPreference = findPreference("group_join");
        joinPreference.setOnPreferenceChangeListener((p, v) -> {
            JSONObject obj = new JSONObject();
            JSONArray arr = new JSONArray();
            try {
                arr.put(v.toString());
                obj.put("GIDs", arr);
                obj.put("Type", 0);
                obj.put("Token", getPreferenceManager().getSharedPreferences().getString("fcm_token", ""));
                obj.put("Name", getPreferenceManager().getSharedPreferences().getString("device_name", ""));
            } catch (JSONException e) {
                showDialog("Error", e.getMessage());
                return false;
            }
            asyncRequest("/api/group", "PUT", obj.toString(), respCallback(res -> {
                joinPreference.setText("");
                HashSet<String> entrySet = new HashSet<>(getPreferenceManager().getSharedPreferences().getStringSet("group_entries", new HashSet<>()));
                entrySet.add(v.toString());
                String[] newEntries = entrySet.toArray(new String[0]);
                listPreference.setEntries(newEntries);
                listPreference.setEntryValues(newEntries);
                listPreference.setValue(v.toString());
                getPreferenceManager().getSharedPreferences().edit()
                        .putStringSet("group_entries", entrySet)
                        .commit();
            }));
            return false;
        });

        findPreference("group_reset").setOnPreferenceClickListener(p -> {
            JSONObject obj = new JSONObject();
            try {
                obj.put("GIDs", new JSONArray(getPreferenceManager().getSharedPreferences().getStringSet("group_entries", new HashSet<>())));
                obj.put("Type", 0);
                obj.put("Token", getPreferenceManager().getSharedPreferences().getString("fcm_token", ""));
            } catch (JSONException e) {
                showDialog("Error", e.getMessage());
                return false;
            }
            asyncRequest("/api/group", "DELETE", obj.toString(), respCallback(res -> {
                listPreference.setEntries(new String[0]);
                listPreference.setEntryValues(new String[0]);
                listPreference.setValue("");
                getPreferenceManager().getSharedPreferences().edit()
                        .putStringSet("group_entries", new HashSet<>())
                        .commit();
            }));
            return true;
        });

        Preference versionPreference = findPreference("about_version");
        versionPreference.setSummary(BuildConfig.VERSION_NAME);
        versionPreference.setOnPreferenceClickListener(p -> {
            asyncRequest("/status", "GET", null, respCallback(res ->
                    showDialog("Registry Status", res.code() + " " + res.message())
            ));
            return true;
        });

        Preference githubPreference = findPreference("about_github");
        githubPreference.setOnPreferenceClickListener(p -> {
            Uri uri = Uri.parse(getString(R.string.app_repo));
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
            return true;
        });
    }

    private void runInUiThread(Runnable action) {
        this.getView().post(action);
    }

    private void showDialog(String title, String msg) {
        new AlertDialog.Builder(getPreferenceManager().getContext())
                .setTitle(title)
                .setMessage(msg)
                .show();
    }

    private void asyncRequest(String path, String method, String jsonBody, Callback cb) {
        String registry = getPreferenceManager().getSharedPreferences()
                .getString("device_registry", getString(R.string.app_registry));
        HttpUrl url = HttpUrl.parse(registry).newBuilder()
                .encodedPath(path)
                .build();
        RequestBody body = jsonBody == null ? null : RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .method(method, body)
                .build();
        okClient.newCall(request).enqueue(cb);
    }

    private Callback respCallback(RespAction action) {
        return new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runInUiThread(() -> showDialog("Error", e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    runInUiThread(() -> action.run(response));
                } else {
                    runInUiThread(() -> showDialog("Error", response.code() + " " + response.message()));
                }
                response.close();
            }
        };
    }

    private interface RespAction {
        void run(Response response);
    }
}