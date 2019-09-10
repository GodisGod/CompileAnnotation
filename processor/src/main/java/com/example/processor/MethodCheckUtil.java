package com.example.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

/**
 * Created by hongda on 2019-09-10.
 */
public class MethodCheckUtil {

    public static boolean isPrivate(Element element) {
        return element.getModifiers().contains(Modifier.PRIVATE);
    }

    public static boolean isAbstract(Element element) {
        return element.getModifiers().contains(Modifier.ABSTRACT);
    }

}
