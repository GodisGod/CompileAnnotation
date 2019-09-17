package com.example.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by liutao on 07/07/2017.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface QtRouter {
    /**
     * 该参数当前仅仅支持在activity中使用,表示路由列表
     * @return
     */
    String[] value() default {};
}
