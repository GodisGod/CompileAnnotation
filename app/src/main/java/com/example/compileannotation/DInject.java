package com.example.compileannotation;


import android.app.Activity;
import android.util.Log;
import android.view.View;

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
        inject(host, host.getWindow().getDecorView());
    }

    public static void inject(Object target, View view) {
        //获得 View 所在 Activity 的类路径，然后拼接一个字符串“$$Proxy”。

        Constructor constructor = getBind(target.getClass());

        try {
            constructor.newInstance(target, view);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    //补充知识点
    //getDeclaredConstructor(Class<?>... parameterTypes)
    //这个方法会返回制定参数类型的所有构造器，包括public的和非public的，当然也包括private的。
    //getDeclaredConstructors()的返回结果就没有参数类型的过滤了。
    //
    //再来看getConstructor(Class<?>... parameterTypes)
    //这个方法返回的是上面那个方法返回结果的子集，只返回制定参数类型访问权限是public的构造器。
    //getConstructors()的返回结果同样也没有参数类型的过滤。
    private static Constructor getBind(Class<?> cls) {
        Constructor constructor = hasBinded.get(cls);
        if (constructor == null) {
            // 这个是编译时动态生成的 Class 的完整路径，也就是我们需要实现的，同时也是最关键的部分；
            String classFullName = cls.getName() + "$$Proxy";
            Log.i("LHD", "cls.getName() = " + cls.getName() + "  classFullName = " + classFullName + "  " + cls.getSimpleName() + "  " + cls.getPackage().getName());
            //classFullName = com.example.compileannotation.TestAdapter$TestHolder$$Proxy  TestHolder  com.example.compileannotation
            try {
//            根据 Class 路径，使用 Class.forName(classFullName) 生成 Class 对象；
                Class proxy = Class.forName(classFullName);
                Log.i("LHD", "获取到的类对象 = " + proxy);
                //如果是adapter的话获取到的是com.example.compileannotation.TestAdapter$TestHolder$$Proxy
                //所以我们应当把生成的文件名，也命名成TestAdapter$TestHolder$$Proxy
                //或者把TestAdapter$TestHolder$$Proxy中的TestAdapter$截取掉

//            得到 Class 的构造函数 constructor 对象
                constructor = proxy.getDeclaredConstructor(cls, View.class);
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
