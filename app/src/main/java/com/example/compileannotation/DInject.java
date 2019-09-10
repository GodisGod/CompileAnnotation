package com.example.compileannotation;


import android.app.Activity;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 用来实例化MainActivity$$ViewInjector
 */

public class DInject {
    private static final Map<Class<?>, Constructor> hasBinded = new LinkedHashMap<>();

    public static void inject(Activity host) {
        //获得 View 所在 Activity 的类路径，然后拼接一个字符串“$$Proxy”。

        Constructor constructor = getBind(host.getClass());

        try {
            constructor.newInstance();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    private static Constructor getBind(Class<?> cls) {
        Constructor constructor = hasBinded.get(cls);
        if (constructor == null) {
            // 这个是编译时动态生成的 Class 的完整路径，也就是我们需要实现的，同时也是最关键的部分；
            String classFullName = cls.getName() + "$$Proxy";
            Log.i("LHD", "classFullName = " + classFullName);
            try {
//            根据 Class 路径，使用 Class.forName(classFullName) 生成 Class 对象；
                Class proxy = Class.forName(classFullName);
//            得到 Class 的构造函数 constructor 对象
                constructor = proxy.getConstructor(cls);
//            使用 constructor.newInstance(host) new 出一个对象，这会执行对象的构造方法，方法内部是我们为 MainActivity 的 tv 赋值的地方。
                constructor.setAccessible(true);
                hasBinded.put(cls, constructor);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return constructor;

    }

}
