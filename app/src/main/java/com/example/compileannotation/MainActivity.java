package com.example.compileannotation;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.annotation.DBindView;
import com.example.annotation.DClick;

//@ViewProcessor(name = "Method")
public class MainActivity extends AppCompatActivity {

    @DBindView(R.id.tv_test)
    TextView textView;

    @DBindView(R.id.btn_test)
    Button btnTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DInject.inject(this);
        System.out.println("LHDDD MainActivity DInject");

       /* btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("测试编译时注解");
            }
        });*/

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("测试编译时注解 textView");
            }
        });
    }

    @DClick(R.id.btn_test)
    public void onClickTest() {
        textView.setText("测试编译时注解 btnTest");
    }

}
