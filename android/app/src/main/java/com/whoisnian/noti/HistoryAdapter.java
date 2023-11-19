package com.whoisnian.noti;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private final SQLiteDatabase DB;
    private final List<Task> tasks;

    public HistoryAdapter(SQLiteDatabase DB) {
        this.DB = DB;
        this.tasks = Task.loadAllFromDB(DB);
        this.setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        ConstraintLayout base = new ConstraintLayout(viewGroup.getContext());
        base.setId(View.generateViewId());
        base.setBackgroundColor(Color.rgb(27, 30, 32));
        base.setPadding(50, 25, 50, 25);

        ConstraintLayout.LayoutParams baseLayout = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        baseLayout.setMargins(100, 50, 100, 0);
        base.setLayoutParams(baseLayout);

        ConstraintSet baseSet = new ConstraintSet();
        baseSet.clone(base);

        ImageView typeView = new ImageView(viewGroup.getContext());
        typeView.setId(View.generateViewId());
        typeView.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT));
        base.addView(typeView);
        baseSet.constrainHeight(typeView.getId(), ConstraintSet.WRAP_CONTENT);
        baseSet.constrainWidth(typeView.getId(), ConstraintSet.WRAP_CONTENT);
        baseSet.connect(typeView.getId(), ConstraintSet.TOP, base.getId(), ConstraintSet.TOP);
        baseSet.connect(typeView.getId(), ConstraintSet.LEFT, base.getId(), ConstraintSet.LEFT);

        TextView timeView = new TextView(viewGroup.getContext());
        timeView.setId(View.generateViewId());
        timeView.setLines(1);
        timeView.setTextSize(12);
        base.addView(timeView);
        baseSet.constrainHeight(timeView.getId(), ConstraintSet.WRAP_CONTENT);
        baseSet.constrainWidth(timeView.getId(), ConstraintSet.WRAP_CONTENT);
        baseSet.connect(timeView.getId(), ConstraintSet.TOP, typeView.getId(), ConstraintSet.TOP);
        baseSet.connect(timeView.getId(), ConstraintSet.RIGHT, base.getId(), ConstraintSet.RIGHT);
        baseSet.connect(timeView.getId(), ConstraintSet.BOTTOM, typeView.getId(), ConstraintSet.BOTTOM);

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
        copyText.setMinimumHeight(0);
        copyText.setMinimumWidth(0);
        copyText.setMinHeight(0);
        copyText.setMinWidth(0);
        copyText.setPadding(50, 20, 50, 20);
        copyText.setBackgroundColor(Color.rgb(49, 54, 59));
        base.addView(copyText);
        baseSet.constrainHeight(copyText.getId(), ConstraintSet.WRAP_CONTENT);
        baseSet.constrainWidth(copyText.getId(), ConstraintSet.WRAP_CONTENT);
        baseSet.connect(copyText.getId(), ConstraintSet.TOP, contentView.getId(), ConstraintSet.BOTTOM, 25);
        baseSet.connect(copyText.getId(), ConstraintSet.RIGHT, base.getId(), ConstraintSet.RIGHT);

        baseSet.applyTo(base);
        return new ViewHolder(this, base, typeView, timeView, contentView, copyText);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.setTask(tasks.get(position));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    @Override
    public long getItemId(int position) {
        return tasks.get(position).tid;
    }

    public void clear() {
        Task.deleteAllFromDB(DB);
        tasks.clear();
        notifyDataSetChanged();
    }

    public void delete(int position) {
        tasks.get(position).deleteFromDB(DB);
        tasks.remove(position);
        notifyItemRemoved(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private final HistoryAdapter adapter;
        private final ConstraintLayout base;
        private final ImageView typeView;
        private final TextView timeView, contentView;
        private final Button copyText;
        private Task task;

        public ViewHolder(HistoryAdapter adapter, ConstraintLayout base, ImageView typeView, TextView timeView, TextView contentView, Button copyText) {
            super(base);
            this.adapter = adapter;
            this.base = base;
            base.setOnClickListener(this);
            base.setOnLongClickListener(this);
            this.typeView = typeView;
            this.timeView = timeView;
            this.contentView = contentView;
            this.copyText = copyText;
            copyText.setOnClickListener(this);
        }

        public void setTask(Task task) {
            this.task = task;
            switch (task.type) {
                case "ping":
                    typeView.setImageResource(R.drawable.outline_notifications_24);
                    break;
                case "text":
                    typeView.setImageResource(R.drawable.outline_message_24);
                    contentView.setText(task.title.isEmpty() ? task.text : task.title + "\n" + task.text);
                    break;
                case "link":
                    typeView.setImageResource(R.drawable.outline_link_24);
                    contentView.setText(task.title.isEmpty() ? task.link : task.title + "\n" + task.link);
                    break;
            }
            timeView.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(task.ctime));
            setVisibility(contentView, !task.type.equals("ping"));
            setVisibility(copyText, !task.type.equals("ping"));
        }

        @Override
        public void onClick(View view) {
            if (view.equals(base)) {
                if (task.type.equals("link"))
                    base.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(task.link)));
            } else if (view.equals(copyText)) {
                Intent intent = new Intent(base.getContext(), BackgroundReceiver.class);
                intent.setAction(Task.ACTION_COPY_TEXT);
                intent.setClipData(ClipData.newPlainText("text", task.type.equals("link") ? task.link : task.text));
                intent.setData(Uri.parse("noti://whoisnian.com/intent?tid=" + task.tid));
                base.getContext().sendBroadcast(intent);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (view.equals(base)) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(base.getContext());
                dialogBuilder.setMessage("Delete this history?");
                dialogBuilder.setPositiveButton("delete", (dialog, id) -> adapter.delete(getAdapterPosition()));
                dialogBuilder.setNegativeButton("cancel", null);
                dialogBuilder.show();
            }
            return true;
        }

        private void setVisibility(View view, boolean value) {
            if (value && view.getVisibility() == View.INVISIBLE) {
                view.setVisibility(View.VISIBLE);
                base.addView(view);
            } else if (!value && view.getVisibility() == View.VISIBLE) {
                base.removeView(view);
                view.setVisibility(View.INVISIBLE);
            }
        }
    }
}

