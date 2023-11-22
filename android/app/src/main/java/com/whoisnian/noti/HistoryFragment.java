package com.whoisnian.noti;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

public class HistoryFragment extends Fragment {
    private final SQLiteDatabase DB;
    public HistoryAdapter adapter;

    public HistoryFragment(SQLiteDatabase DB) {
        this.DB = DB;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.history_fragment, container, false);
        ((RecyclerView) view.findViewById(R.id.historyFragment)).setAdapter(adapter = new HistoryAdapter(DB));
        return view;
    }
}