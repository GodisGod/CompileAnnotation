package com.example.compileannotation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.annotation.BindView;
import com.example.annotation.QJump;
import com.example.annotation.QtInject;
import com.example.dcompiler.AndJump;
import com.example.dcompiler.DInject;

@QJump
@QtInject
public class SecondActivity extends AppCompatActivity {


    @BindView(R.id.tv_name)
    TextView tvName;

    @QtInject
    String name2;

    @QtInject
    int value;

    @QtInject
    TestBean testBean;

    @BindView(R.id.container)
    FrameLayout frameLayout;


    @QJump
    String name;

    @QJump
    String jumpTest;

    @QJump
    int value2;

    @QJump
    TestBean testBean2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        DInject.inject(this);

//        AndJump.inject(this);

//        QSecondActivity.inject(this);

//        Intent intent = getIntent();
//
//        String value = intent.getStringExtra("test");
//        Log.i("LHD", "SecondActivity value = " + value + " name2 = " + name2);

        tvName.setText(name2 + " " + value + "  " + testBean.getNameTest());

//        BlankFragment blankFragment = BlankFragment.newInstance("小小琳", "可爱哟");

//        tvName.setText(jumpTest + " " + name + "  " + value2 + "  " + testBean2.getNameTest());

        BlankFragment blankFragment = QtBlankFragment.getInstance().setMParam1("琳琳").build();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.container, blankFragment);
        transaction.commitAllowingStateLoss();
    }

}
