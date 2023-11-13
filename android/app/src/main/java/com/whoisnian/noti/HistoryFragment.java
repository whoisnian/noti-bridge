package com.whoisnian.noti;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;

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
        ScrollView scroll = new ScrollView(context);
        scroll.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(context);
        root.setPadding(50, 50, 50, 50);
        root.setOrientation(LinearLayout.VERTICAL);

        ConstraintLayout msg1 = createMsgCard("ping", "", "", "");
        root.addView(msg1);

        ConstraintLayout msg2 = createMsgCard("text", "text title", "content content", "");
        root.addView(msg2);

        ConstraintLayout msg3 = createMsgCard("link", "link title", "link url", "https://baidu.com");
        root.addView(msg3);

        scroll.addView(root);
        return scroll;
    }

    private ConstraintLayout createMsgCard(String type, String title, String text, String link) {
        ConstraintLayout base = new ConstraintLayout(getContext());
        base.setId(View.generateViewId());
        base.setBackgroundColor(Color.rgb(27, 30, 32));

        LayoutParams baseLayout = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        baseLayout.setMargins(50, 50, 50, 50);
        base.setLayoutParams(baseLayout);
        base.setPadding(50, 50, 50, 50);

        ConstraintSet baseSet = new ConstraintSet();
        baseSet.clone(base);
        
        TextView typeView = new TextView(getContext());
        typeView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        typeView.setId(View.generateViewId());
        typeView.setText(type.toUpperCase());
        base.addView(typeView);
        baseSet.constrainHeight(typeView.getId(), ConstraintSet.WRAP_CONTENT);
        baseSet.constrainWidth(typeView.getId(), ConstraintSet.WRAP_CONTENT);
        baseSet.connect(typeView.getId(), ConstraintSet.TOP, base.getId(), ConstraintSet.TOP);
        baseSet.connect(typeView.getId(), ConstraintSet.LEFT, base.getId(), ConstraintSet.LEFT);

        TextView timeView = new TextView(getContext());
        timeView.setId(View.generateViewId());
        timeView.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        base.addView(timeView);
        baseSet.constrainHeight(timeView.getId(), ConstraintSet.WRAP_CONTENT);
        baseSet.constrainWidth(timeView.getId(), ConstraintSet.WRAP_CONTENT);
        baseSet.connect(timeView.getId(), ConstraintSet.TOP, base.getId(), ConstraintSet.TOP);
        baseSet.connect(timeView.getId(), ConstraintSet.RIGHT, base.getId(), ConstraintSet.RIGHT);

        switch (type) {
            case "text":
                TextView textView = new TextView(getContext());
                textView.setId(View.generateViewId());
                textView.setText(text);
                base.addView(textView);
                baseSet.constrainHeight(textView.getId(), ConstraintSet.WRAP_CONTENT);
                baseSet.constrainWidth(textView.getId(), ConstraintSet.WRAP_CONTENT);
                baseSet.connect(textView.getId(), ConstraintSet.TOP, typeView.getId(), ConstraintSet.BOTTOM, 25);
                baseSet.connect(textView.getId(), ConstraintSet.LEFT, base.getId(), ConstraintSet.LEFT);

                Button copyText1 = new Button(getContext());
                copyText1.setId(View.generateViewId());
                copyText1.setText("COPY");
                base.addView(copyText1);
                baseSet.constrainHeight(copyText1.getId(), ConstraintSet.WRAP_CONTENT);
                baseSet.constrainWidth(copyText1.getId(), ConstraintSet.WRAP_CONTENT);
                baseSet.connect(copyText1.getId(), ConstraintSet.TOP, textView.getId(), ConstraintSet.BOTTOM, 25);
                baseSet.connect(copyText1.getId(), ConstraintSet.RIGHT, base.getId(), ConstraintSet.RIGHT);
                break;
            case "link":
                TextView linkView = new TextView(getContext());
                linkView.setId(View.generateViewId());
                linkView.setText(link);
                base.addView(linkView);
                baseSet.constrainHeight(linkView.getId(), ConstraintSet.WRAP_CONTENT);
                baseSet.constrainWidth(linkView.getId(), ConstraintSet.WRAP_CONTENT);
                baseSet.connect(linkView.getId(), ConstraintSet.TOP, typeView.getId(), ConstraintSet.BOTTOM, 25);
                baseSet.connect(linkView.getId(), ConstraintSet.LEFT, base.getId(), ConstraintSet.LEFT);

                Button copyText2 = new Button(getContext());
                copyText2.setId(View.generateViewId());
                copyText2.setText("COPY");
                base.addView(copyText2);
                baseSet.constrainHeight(copyText2.getId(), ConstraintSet.WRAP_CONTENT);
                baseSet.constrainWidth(copyText2.getId(), ConstraintSet.WRAP_CONTENT);
                baseSet.connect(copyText2.getId(), ConstraintSet.TOP, linkView.getId(), ConstraintSet.BOTTOM, 25);
                baseSet.connect(copyText2.getId(), ConstraintSet.RIGHT, base.getId(), ConstraintSet.RIGHT);

                Button openLink = new Button(getContext());
                openLink.setId(View.generateViewId());
                openLink.setText("OPEN");
                base.addView(openLink);
                baseSet.constrainHeight(openLink.getId(), ConstraintSet.WRAP_CONTENT);
                baseSet.constrainWidth(openLink.getId(), ConstraintSet.WRAP_CONTENT);
                baseSet.connect(openLink.getId(), ConstraintSet.TOP, linkView.getId(), ConstraintSet.BOTTOM, 25);
                baseSet.connect(openLink.getId(), ConstraintSet.RIGHT, copyText2.getId(), ConstraintSet.LEFT);
                break;
        }
        baseSet.applyTo(base);
        return base;
    }
}