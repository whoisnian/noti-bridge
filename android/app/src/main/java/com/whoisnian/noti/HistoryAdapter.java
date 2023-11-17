package com.whoisnian.noti;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private static final String TAG = "HistoryAdapter";

    private final List<Task> tasks;

    public HistoryAdapter(List<Task> tasks) {
        this.tasks = tasks;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        ConstraintLayout base = new ConstraintLayout(viewGroup.getContext());
        base.setId(View.generateViewId());
        base.setBackgroundColor(Color.rgb(27, 30, 32));
        base.setPadding(50, 50, 50, 50);

        ConstraintLayout.LayoutParams baseLayout = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        baseLayout.setMargins(50, 50, 50, 50);
        base.setLayoutParams(baseLayout);

        ConstraintSet baseSet = new ConstraintSet();
        baseSet.clone(base);

        TextView typeView = new TextView(viewGroup.getContext());
        typeView.setId(View.generateViewId());
        typeView.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT));
        base.addView(typeView);
        baseSet.constrainHeight(typeView.getId(), ConstraintSet.WRAP_CONTENT);
        baseSet.constrainWidth(typeView.getId(), ConstraintSet.WRAP_CONTENT);
        baseSet.connect(typeView.getId(), ConstraintSet.TOP, base.getId(), ConstraintSet.TOP);
        baseSet.connect(typeView.getId(), ConstraintSet.LEFT, base.getId(), ConstraintSet.LEFT);

        TextView timeView = new TextView(viewGroup.getContext());
        timeView.setId(View.generateViewId());
        base.addView(timeView);
        baseSet.constrainHeight(timeView.getId(), ConstraintSet.WRAP_CONTENT);
        baseSet.constrainWidth(timeView.getId(), ConstraintSet.WRAP_CONTENT);
        baseSet.connect(timeView.getId(), ConstraintSet.TOP, base.getId(), ConstraintSet.TOP);
        baseSet.connect(timeView.getId(), ConstraintSet.RIGHT, base.getId(), ConstraintSet.RIGHT);

        TextView contentView = new TextView(viewGroup.getContext());
        contentView.setId(View.generateViewId());
        base.addView(contentView);
        baseSet.constrainHeight(contentView.getId(), ConstraintSet.WRAP_CONTENT);
        baseSet.constrainWidth(contentView.getId(), ConstraintSet.WRAP_CONTENT);
        baseSet.connect(contentView.getId(), ConstraintSet.TOP, typeView.getId(), ConstraintSet.BOTTOM, 25);
        baseSet.connect(contentView.getId(), ConstraintSet.LEFT, base.getId(), ConstraintSet.LEFT);

        Button copyText = new Button(viewGroup.getContext());
        copyText.setId(View.generateViewId());
        copyText.setText("COPY");
        base.addView(copyText);
        baseSet.constrainHeight(copyText.getId(), ConstraintSet.WRAP_CONTENT);
        baseSet.constrainWidth(copyText.getId(), ConstraintSet.WRAP_CONTENT);
        baseSet.connect(copyText.getId(), ConstraintSet.TOP, contentView.getId(), ConstraintSet.BOTTOM, 25);
        baseSet.connect(copyText.getId(), ConstraintSet.RIGHT, base.getId(), ConstraintSet.RIGHT);

        baseSet.applyTo(base);
        return new ViewHolder(base, typeView, timeView, contentView, copyText);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.setTask(tasks.get(position));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ConstraintLayout base;
        private final TextView typeView, timeView, contentView;
        private final Button copyText;

        public ViewHolder(ConstraintLayout base, TextView typeView, TextView timeView, TextView contentView, Button copyText) {
            super(base);
            this.base = base;
            this.typeView = typeView;
            this.timeView = timeView;
            this.contentView = contentView;
            this.copyText = copyText;
        }

        public void setTask(Task task) {
            typeView.setText(task.type.toUpperCase());
            timeView.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(task.ctime));
            switch (task.type) {
                case "ping":
                    hideView(contentView);
                    hideView(copyText);
                    break;
                case "text":
                    contentView.setText(task.title + "\n" + task.text);
                    showView(contentView);
                    showView(copyText);
                    break;
                case "link":
                    contentView.setText(task.title + "\n" + task.link);
                    showView(contentView);
                    showView(copyText);
                    break;
            }
        }

        private void hideView(View view) {
            if (view.getVisibility() == View.VISIBLE) {
                base.removeView(view);
                view.setVisibility(View.INVISIBLE);
            }
        }

        private void showView(View view) {
            if (view.getVisibility() == View.INVISIBLE) {
                view.setVisibility(View.VISIBLE);
                base.addView(view);
            }
        }
    }
}

