package com.example.processor.util;

import com.example.processor.EleParser;
import com.example.processor.reflect.Reflect;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

/**
 * Created by hongda on 2019-09-17.
 */
public class CommonUtils {

    public static String getClassName(TypeElement type, String pkgName) {
        int packageLength = pkgName.length() + 1;
        DUtil.log("pakageName = " + pkgName + "  type.getQualifiedName().toString() = " + type.getQualifiedName().toString());
        return type.getQualifiedName().toString().substring(packageLength).replace('.', '$');
    }

    public static String getPackageName(TypeElement type) {
        PackageElement packageOf = DUtil.getUtil().getElementUtils().getPackageOf(type);
        return packageOf.isUnnamed() ? "" : packageOf.getQualifiedName().toString();
    }


    private static final String APP_NAME = "android.app.Application";
    private static final String ACT_NAME = "android.app.Activity";
    private static final String FRAG_NAME = "android.app.Fragment";
    private static final String V4_FRAG_NAME = "android.support.v4.app.Fragment";

    public enum ElementType {
        APPLICATION, ACTIVITY, FRAGMENT
    }

    public static ElementType getElementType(TypeElement typeElement) {
        ElementType type;
        if (checkIsSubClass(typeElement, ACT_NAME)) {
            type = ElementType.ACTIVITY;
        } else if (checkIsSubClass(typeElement, FRAG_NAME) || checkIsSubClass(typeElement, V4_FRAG_NAME)) {
            type = ElementType.FRAGMENT;
        } else if (checkIsSubClass(typeElement, APP_NAME)) {
            type = ElementType.APPLICATION;
        } else {
            throw new IllegalArgumentException(String.format("class %s must be extends from %s or %s or %s or %s", typeElement.getQualifiedName(), ACT_NAME, FRAG_NAME, V4_FRAG_NAME, APP_NAME));
        }
        return type;
    }

    /**
     * check out if is target's subclass
     */
    private static boolean checkIsSubClass(TypeElement typeElement, String target) {
        while (true) {
            if (typeElement == null) {
                return false;
            } else if (target.equals(typeElement.getQualifiedName().toString())) {
                return true;
            }
            typeElement = getParentClass(typeElement);
        }
    }

    private static TypeElement getParentClass(TypeElement parent) {
        return (TypeElement) DUtil.getUtil().getTypeUtils().asElement(parent.getSuperclass());
    }

    public static TypeName getTypeName(String clzName) {
        return Reflect.on(TypeName.class).create(clzName).get();
    }

}
