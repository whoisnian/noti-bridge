package com.whoisnian.noti;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryFragment extends Fragment {
    private static final String TAG = "HistoryFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context context = container.getContext();
        RecyclerView root = new RecyclerView(context);
        root.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        root.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

        SQLiteDatabase db = new DatabaseHelper(container.getContext()).getReadableDatabase();
        root.setAdapter(new HistoryAdapter(Task.loadTasksFromDB(db)));
        return root;
    }
}