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

public class PreferenceFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
    private static final String TAG = "PreferenceFragment";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final OkHttpClient client = new OkHttpClient.Builder().connectTimeout(1, TimeUnit.SECONDS).build();
    private EditTextPreference namePreference, joinPreference;
    private ListPreference listPreference;
    private Preference resetPreference, versionPreference, githubPreference;

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

        namePreference = findPreference("device_name");
        namePreference.setDefaultValue(Build.BRAND + " " + Build.MODEL);
        namePreference.setOnPreferenceChangeListener(this);

        listPreference = findPreference("group_list");
        String[] entries = getPreferenceManager().getSharedPreferences().getStringSet("group_entries", new HashSet<>()).toArray(new String[0]);
        listPreference.setEntries(entries);
        listPreference.setEntryValues(entries);
        listPreference.setSummaryProvider(p -> "Current " + listPreference.getEntries().length + " groups");
        listPreference.setOnPreferenceChangeListener(this);

        joinPreference = findPreference("group_join");
        joinPreference.setOnPreferenceChangeListener(this);

        resetPreference = findPreference("group_reset");
        resetPreference.setOnPreferenceClickListener(this);

        versionPreference = findPreference("about_version");
        versionPreference.setSummary(BuildConfig.VERSION_NAME);
        versionPreference.setOnPreferenceClickListener(this);

        githubPreference = findPreference("about_github");
        githubPreference.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.equals(namePreference)) {
            String oldValue = namePreference.getText();
            JSONObject obj = new JSONObject();
            try {
                obj.put("Type", 0);
                obj.put("Token", getPreferenceManager().getSharedPreferences().getString("fcm_token", ""));
                obj.put("Name", newValue.toString());
            } catch (JSONException e) {
                showDialog("Error", e.getMessage());
                return false;
            }
            asyncPostJson("/api/device", obj.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runInUiThread(() -> {
                        namePreference.setText(oldValue);
                        showDialog("Error", e.getMessage());
                    });
                }

                @Override
                public void onResponse(Call call, Response response) {
                    if (!response.isSuccessful()) {
                        runInUiThread(() -> {
                            namePreference.setText(oldValue);
                            showDialog("Error", response.code() + " " + response.message());
                        });
                    }
                    response.close();
                }
            });
        } else if (preference.equals(listPreference)) {
            JSONObject obj = new JSONObject();
            JSONArray arr = new JSONArray();
            try {
                arr.put(newValue.toString());
                obj.put("GIDs", arr);
                obj.put("Type", 0);
                obj.put("Token", getPreferenceManager().getSharedPreferences().getString("fcm_token", ""));
            } catch (JSONException e) {
                showDialog("Error", e.getMessage());
                return false;
            }
            asyncDeleteJson("/api/group", obj.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runInUiThread(() -> showDialog("Error", e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) {
                    if (response.isSuccessful()) {
                        runInUiThread(() -> {
                            HashSet<String> entrySet = new HashSet<>(getPreferenceManager().getSharedPreferences().getStringSet("group_entries", new HashSet<>()));
                            entrySet.remove(newValue.toString());
                            String[] entries = entrySet.toArray(new String[0]);
                            listPreference.setEntries(entries);
                            listPreference.setEntryValues(entries);
                            listPreference.setValue(null);
                            getPreferenceManager().getSharedPreferences().edit()
                                    .putStringSet("group_entries", entrySet)
                                    .commit();
                        });
                    } else {
                        runInUiThread(() -> showDialog("Error", response.code() + " " + response.message()));
                    }
                    response.close();
                }
            });
        } else if (preference.equals(joinPreference)) {
            JSONObject obj = new JSONObject();
            JSONArray arr = new JSONArray();
            try {
                arr.put(newValue.toString());
                obj.put("GIDs", arr);
                obj.put("Type", 0);
                obj.put("Token", getPreferenceManager().getSharedPreferences().getString("fcm_token", ""));
                obj.put("Name", getPreferenceManager().getSharedPreferences().getString("device_name", ""));
            } catch (JSONException e) {
                showDialog("Error", e.getMessage());
                return false;
            }
            asyncPutJson("/api/group", obj.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runInUiThread(() -> showDialog("Error", e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) {
                    if (response.isSuccessful()) {
                        runInUiThread(() -> {
                            joinPreference.setText("");
                            HashSet<String> entrySet = new HashSet<>(getPreferenceManager().getSharedPreferences().getStringSet("group_entries", new HashSet<>()));
                            entrySet.add(newValue.toString());
                            String[] entries = entrySet.toArray(new String[0]);
                            listPreference.setEntries(entries);
                            listPreference.setEntryValues(entries);
                            listPreference.setValue(newValue.toString());
                            getPreferenceManager().getSharedPreferences().edit()
                                    .putStringSet("group_entries", entrySet)
                                    .commit();
                        });
                    } else {
                        runInUiThread(() -> showDialog("Error", response.code() + " " + response.message()));
                    }
                    response.close();
                }
            });
        }
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.equals(resetPreference)) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("GIDs", new JSONArray(getPreferenceManager().getSharedPreferences().getStringSet("group_entries", new HashSet<>())));
                obj.put("Type", 0);
                obj.put("Token", getPreferenceManager().getSharedPreferences().getString("fcm_token", ""));
            } catch (JSONException e) {
                showDialog("Error", e.getMessage());
                return false;
            }
            asyncDeleteJson("/api/group", obj.toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runInUiThread(() -> showDialog("Error", e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) {
                    if (response.isSuccessful()) {
                        runInUiThread(() -> {
                            listPreference.setEntries(new String[0]);
                            listPreference.setEntryValues(new String[0]);
                            listPreference.setValue("");
                            getPreferenceManager().getSharedPreferences().edit()
                                    .putStringSet("group_entries", new HashSet<>())
                                    .commit();
                        });
                    } else {
                        runInUiThread(() -> showDialog("Error", response.code() + " " + response.message()));
                    }
                    response.close();
                }
            });
        } else if (preference.equals(versionPreference)) {
            asyncGet("/status", new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runInUiThread(() -> showDialog("Error", e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) {
                    runInUiThread(() -> showDialog("Registry Status", response.code() + " " + response.message()));
                    response.close();
                }
            });
        } else if (preference.equals(githubPreference)) {
            Uri uri = Uri.parse(getString(R.string.app_repo));
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        }
        return true;
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

    private void asyncGet(String path, Callback cb) {
        String registry = getPreferenceManager().getSharedPreferences()
                .getString("device_registry", getString(R.string.app_registry));
        HttpUrl url = HttpUrl.parse(registry).newBuilder()
                .encodedPath(path)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        client.newCall(request).enqueue(cb);
    }

    private void asyncPostJson(String path, String jsonBody, Callback cb) {
        String registry = getPreferenceManager().getSharedPreferences()
                .getString("device_registry", getString(R.string.app_registry));
        HttpUrl url = HttpUrl.parse(registry).newBuilder()
                .encodedPath(path)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(jsonBody, JSON))
                .build();
        client.newCall(request).enqueue(cb);
    }

    private void asyncPutJson(String path, String jsonBody, Callback cb) {
        String registry = getPreferenceManager().getSharedPreferences()
                .getString("device_registry", getString(R.string.app_registry));
        HttpUrl url = HttpUrl.parse(registry).newBuilder()
                .encodedPath(path)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .put(RequestBody.create(jsonBody, JSON))
                .build();
        client.newCall(request).enqueue(cb);
    }

    private void asyncDeleteJson(String path, String jsonBody, Callback cb) {
        String registry = getPreferenceManager().getSharedPreferences()
                .getString("device_registry", getString(R.string.app_registry));
        HttpUrl url = HttpUrl.parse(registry).newBuilder()
                .encodedPath(path)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .delete(RequestBody.create(jsonBody, JSON))
                .build();
        client.newCall(request).enqueue(cb);
    }
}