package com.example.compileannotation;

import android.app.Application;

import com.example.annotation.QtInit;
import com.example.dcompiler.AndJump;

/**
 * Created by hongda on 2019-09-17.
 */
@QtInit
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AndJump.register(QtInitializer.getInjectTable());
        AndJump.setDebug(true);

    }

}
