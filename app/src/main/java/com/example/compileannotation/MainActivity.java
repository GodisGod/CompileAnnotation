package com.example.compileannotation;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.annotation.RBindView;

//@ViewProcessor(name = "Method")
public class MainActivity extends AppCompatActivity {

    @RBindView(R.id.tv_test)
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RInject.inject(this);
        System.out.println("LHDDD MainActivity RInject");

        findViewById(R.id.btn_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("测试编译时注解");
            }
        });
    }

}
