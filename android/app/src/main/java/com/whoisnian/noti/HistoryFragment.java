package com.whoisnian.noti;

import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams;
import androidx.fragment.app.Fragment;

public class HistoryFragment extends Fragment implements View.OnLongClickListener {
    private static final String TAG = "HistoryFragment";

    private TextView textView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ScrollView scrollView = new ScrollView(container.getContext());
        LayoutParams lp_scrollView = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lp_scrollView.setMargins(10, 0, 10, 0);
        scrollView.setLayoutParams(lp_scrollView);
        scrollView.setFillViewport(true);

        textView = new TextView(container.getContext());
        textView.setOnLongClickListener(this);
        textView.setTypeface(Typeface.MONOSPACE);
        textView.setText("hello world\n0123456789abcdef\n");

        scrollView.addView(textView);
        return scrollView;
    }

    @Override
    public boolean onLongClick(View v) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this.getContext());
        dialogBuilder.setMessage("Clear log?");
        dialogBuilder.setPositiveButton("clear", (dialog, id) -> {
            textView.setText("");
        });
        dialogBuilder.setNegativeButton("cancel", null);
        dialogBuilder.show();
        return true;
    }
}