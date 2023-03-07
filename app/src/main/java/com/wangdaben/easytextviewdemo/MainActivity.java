package com.wangdaben.easytextviewdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.wangdaben.easytextview.EasyTextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EasyTextView easyTextView = findViewById(R.id.tv_easy);
        easyTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                easyTextView.setSelected(!easyTextView.isSelected());
            }
        });
    }
}