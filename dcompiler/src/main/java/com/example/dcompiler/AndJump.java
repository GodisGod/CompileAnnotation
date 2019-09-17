package com.example.dcompiler;

import com.example.annotation.IInject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liutao on 05/07/2017.
 */

public class AndJump {
    private static final Map<Class, IInject> VALUE = new HashMap<>();
    private static boolean isDebug;

    public static boolean inject(Object o) {
        if (o == null) {
            return false;
        }
        IInject result = VALUE.get(o.getClass());
        if (result == null) {
            return false;
        }
        return result.toInject(o);
    }

    public static void register(IInject o) {
        if (o == null) {
            return;
        }
        Class key = o.getKey();
        VALUE.put(key,  o);
    }

    public static void register(List<IInject> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        for (IInject data : list) {
            register(data);
        }
    }

    public static void setDebug(boolean isDebug) {
        AndJump.isDebug = isDebug;
    }

    public static boolean isDebug() {
        return isDebug;
    }
}
