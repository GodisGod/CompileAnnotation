package com.example.processor.util;

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
        return DUtil.getUtil().getElementUtils().getPackageOf(type).getQualifiedName().toString();
    }

}
