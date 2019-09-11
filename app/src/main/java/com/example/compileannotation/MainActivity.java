package com.example.compileannotation;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.annotation.DBindView;
import com.example.annotation.DClick;
import com.example.annotation.DLongClick;
import com.example.dcompiler.DInject;

//@ViewProcessor(name = "Method")
public class MainActivity extends AppCompatActivity {

    @DBindView(R.id.tv_test)
    TextView textView;

    @DBindView(R.id.btn_test)
    Button btnTest;

    @DBindView(R.id.recycler_test)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DInject.inject(this);
        System.out.println("LHDDD MainActivity DInject");

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("测试编译时注解 textView");
            }
        });


        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(new TestAdapter(this));
        recyclerView.setHasFixedSize(true);

    }

    @DClick(R.id.btn_test)
    public void onClickTest() {
        textView.setText("测试编译时注解 btnTest");

//        Intent intent = new Intent();
//        intent.putExtra("name", "name");
//        intent.setClass(MainActivity.this, SecondActivity.class);
//        startActivity(intent);

        new QJumpSencondActivity().addParamater("test", "testtest")
                .setFromTo(MainActivity.this, SecondActivity.class)
                .start();

    }

    @DLongClick(R.id.tv_test)
    public void onLongClickTest() {
        textView.setText("测试编译时注解,长按点击事件测试,textView");
    }


}
