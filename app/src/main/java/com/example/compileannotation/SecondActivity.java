package com.example.compileannotation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.annotation.BindView;
import com.example.annotation.QJump;
import com.example.dcompiler.DInject;

public class SecondActivity extends AppCompatActivity {


    @BindView(R.id.tv_name)
    TextView tvName;

    @QJump
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        DInject.inject(this);

        Intent intent = getIntent();

        String value = intent.getStringExtra("test");
        Log.i("LHD", "SecondActivity value = " + value);
        tvName.setText(value);

    }

}
