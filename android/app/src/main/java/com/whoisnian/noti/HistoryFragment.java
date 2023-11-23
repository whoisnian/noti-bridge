package com.whoisnian.noti;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

public class HistoryFragment extends Fragment {
    public HistoryAdapter adapter;
    private SQLiteDatabase DB;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        DB = new DatabaseHelper(getContext()).getWritableDatabase();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RecyclerView view = (RecyclerView) inflater.inflate(R.layout.history_fragment, container, false);
        view.setAdapter(adapter = new HistoryAdapter(DB));
        return view;
    }

    @Override
    public void onDestroy() {
        DB.close();
        super.onDestroy();
    }
}