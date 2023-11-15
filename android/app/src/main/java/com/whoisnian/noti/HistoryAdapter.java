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
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private static final String TAG = "HistoryAdapter";

    private final String[] localDataSet;

    public HistoryAdapter(String[] dataSet) {
        localDataSet = dataSet;
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

        TextView textView = new TextView(viewGroup.getContext());
        textView.setId(View.generateViewId());
        base.addView(textView);
        baseSet.constrainHeight(textView.getId(), ConstraintSet.WRAP_CONTENT);
        baseSet.constrainWidth(textView.getId(), ConstraintSet.WRAP_CONTENT);
        baseSet.connect(textView.getId(), ConstraintSet.TOP, typeView.getId(), ConstraintSet.BOTTOM, 25);
        baseSet.connect(textView.getId(), ConstraintSet.LEFT, base.getId(), ConstraintSet.LEFT);

        Button copyText = new Button(viewGroup.getContext());
        copyText.setId(View.generateViewId());
        copyText.setText("COPY");
        base.addView(copyText);
        baseSet.constrainHeight(copyText.getId(), ConstraintSet.WRAP_CONTENT);
        baseSet.constrainWidth(copyText.getId(), ConstraintSet.WRAP_CONTENT);
        baseSet.connect(copyText.getId(), ConstraintSet.TOP, textView.getId(), ConstraintSet.BOTTOM, 25);
        baseSet.connect(copyText.getId(), ConstraintSet.RIGHT, base.getId(), ConstraintSet.RIGHT);

        Button openLink = new Button(viewGroup.getContext());
        openLink.setId(View.generateViewId());
        openLink.setText("OPEN");
        base.addView(openLink);
        baseSet.constrainHeight(openLink.getId(), ConstraintSet.WRAP_CONTENT);
        baseSet.constrainWidth(openLink.getId(), ConstraintSet.WRAP_CONTENT);
        baseSet.connect(openLink.getId(), ConstraintSet.TOP, textView.getId(), ConstraintSet.BOTTOM, 25);
        baseSet.connect(openLink.getId(), ConstraintSet.RIGHT, copyText.getId(), ConstraintSet.LEFT);

        baseSet.applyTo(base);
        return new ViewHolder(base, typeView, timeView, textView, copyText, openLink);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        if (position % 5 == 0) {
            viewHolder.setContent("ping", "title", localDataSet[position], "link");
        } else if (position / 10 == 2) {
            viewHolder.setContent("link", "title", localDataSet[position], "link");
        } else {
            viewHolder.setContent("text", "title", localDataSet[position], "link");
        }
    }

    @Override
    public int getItemCount() {
        return localDataSet.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ConstraintLayout base;
        private final TextView typeView, timeView, textView;
        private final Button copyText, openLink;

        public ViewHolder(ConstraintLayout base, TextView typeView, TextView timeView, TextView textView, Button copyText, Button openLink) {
            super(base);
            this.base = base;
            this.typeView = typeView;
            this.timeView = timeView;
            this.textView = textView;
            this.copyText = copyText;
            this.openLink = openLink;
        }

        public void setContent(String type, String title, String text, String link) {
            typeView.setText(type.toUpperCase());
            timeView.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(new Date()));
            switch (type) {
                case "ping":
                    hideView(textView);
                    hideView(copyText);
                    hideView(openLink);
                    break;
                case "text":
                    textView.setText(title + "\n" + text + "\n" + link);
                    showView(textView);
                    showView(copyText);
                    hideView(openLink);
                    break;
                case "link":
                    textView.setText(title + "\n" + text + "\n" + link);
                    showView(textView);
                    showView(copyText);
                    showView(openLink);
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

