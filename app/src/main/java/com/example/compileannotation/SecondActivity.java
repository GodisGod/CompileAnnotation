package com.example.compileannotation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.annotation.BindView;
import com.example.annotation.QJump;
import com.example.annotation.QtInject;
import com.example.dcompiler.AndJump;
import com.example.dcompiler.DInject;

@QtInject
public class SecondActivity extends AppCompatActivity {


    @BindView(R.id.tv_name)
    TextView tvName;

    @QJump
    String name;

    @QtInject
    String name2;

    @QtInject
    int value;

    @QtInject
    TestBean testBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        DInject.inject(this);

        AndJump.inject(this);

//        Intent intent = getIntent();
//
//        String value = intent.getStringExtra("test");
//        Log.i("LHD", "SecondActivity value = " + value + " name2 = " + name2);

        tvName.setText(name2 + " " + value + "  " + testBean.getNameTest());

    }

}
