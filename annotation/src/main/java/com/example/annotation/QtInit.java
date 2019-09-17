package com.example.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface QtInit {
    /**
     * 该参数当前仅仅支持在application中使用,表示路由路径
     * @return
     */
    String value() default "";


}
