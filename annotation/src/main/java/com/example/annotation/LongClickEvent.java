package com.example.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by hongda on 2019-09-10.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface LongClickEvent {
    int value();//需要绑定点击事件的控件的id
}
