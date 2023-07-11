package com.whoisnian.noti;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    ConstraintLayout layout_main;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        layout_main = new ConstraintLayout(this);
        layout_main.setId(View.generateViewId());
        setContentView(layout_main);
    }
}