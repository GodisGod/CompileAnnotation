package com.example.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface QtInject {
    //改字段在设置了类bundle之后生效.
    String DEFAULT_BUNDLE="defalutbundle_qt";
    /**
     * 该参数当前仅仅支持在application中使用,表示路由列表
     *
     * @return
     */
    String value() default "";

    /**
     * 默认以字段的名称作为key值去获取相应的value
     * 如果设置了alias,那么会优先以alias作为Key
     *
     * @return
     */
    String alias() default "";

    /**
     * 选择从某个bundle下获取数据,参数仅仅在activity中有效,不支持fragment
     * 默认activity 是从 intent.getXXXExtral();获取数据
     * 如果设置了bundle名，将优先去对应bundle里面的内容
     * 目前暂时不做处理.
     * @return
     */
    String bundle() default "";

    /**
     * 是否使用默认值,即没有外部没有传递该值或传递的值为数据默认类型时,默认使用本地参数
     *
     * @return
     */
    boolean defaultValue() default true;

}
