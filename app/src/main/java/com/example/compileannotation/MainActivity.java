package com.example.compileannotation;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.annotation.BindView;
import com.example.annotation.ClickEvents;
import com.example.annotation.LongClickEvent;
import com.example.annotation.QtInject;
import com.example.dcompiler.AndJump;
import com.example.dcompiler.DInject;

@QtInject
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tv_test)
    TextView textView;

    @BindView(R.id.btn_test)
    Button btnTest;

    @BindView(R.id.recycler_test)
    RecyclerView recyclerView;

    @QtInject("name")
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DInject.inject(this);
        AndJump.inject(this);
        System.out.println("LHDDD MainActivity DInject");

//        textView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                textView.setText("测试编译时注解 textView");
//            }
//        });

        btnTest.setTag("我是btnTest");
        textView.setTag("我是textView");

        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(new TestAdapter(this));
        recyclerView.setHasFixedSize(true);

    }

//    @ClickEvent(R.id.btn_test)
//    public void onClickTest() {
//        textView.setText("测试编译时注解 btnTest");
//
//        Intent intent = new Intent();
//        intent.putExtra("name", "name");
//        intent.setClass(MainActivity.this, SecondActivity.class);
//        startActivity(intent);
//    }

    @ClickEvents({R.id.btn_test, R.id.tv_test})
    public void allClickTest(View vvvvvv) {
        if (vvvvvv.getId() == R.id.btn_test) {
            textView.setText("测试编译时注解 btnTest + " + btnTest.getTag().toString());

//            new QJumpSencondActivity()
//                    .addParamater("test", "testtest")
//                    .setFrom(MainActivity.this)
//                    .start();
            QtSecondActivity.getInstance().setName2("测试跳转")
                    .setValue(55)
                    .setTestBean(new TestBean("我也觉得可以"))
                    .start(MainActivity.this);

        } else if (vvvvvv.getId() == R.id.tv_test) {
            textView.setText("测试编译时注解 textView + " + textView.getTag().toString());
        }
    }

    @LongClickEvent(R.id.tv_test)
    public void onLongClickTest() {
        textView.setText("测试编译时注解,长按点击事件测试,textView");
    }


}
