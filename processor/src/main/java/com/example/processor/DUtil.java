package com.example.processor;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

/**
 * Created by hongda on 2019-09-17.
 */
public class DUtil {

    private Filer filer;
    Elements elementUtils;//操作元素
    private Messager messager;

    private static DUtil dUtil = new DUtil();

    public DUtil() {
    }

    public static DUtil getUtil() {
        return dUtil;
    }

    public Filer getFiler() {
        return filer;
    }

    public void setFiler(Filer filer) {
        this.filer = filer;
    }

    public Elements getElementUtils() {
        return elementUtils;
    }

    public void setElementUtils(Elements elementUtils) {
        this.elementUtils = elementUtils;
    }

    public Messager getMessager() {
        return messager;
    }

    public void setMessager(Messager messager) {
        this.messager = messager;
    }

    public static void log(String log) {
        getUtil().getMessager().printMessage(Diagnostic.Kind.NOTE, log);
    }

    public static void error(String error) {
        getUtil().getMessager().printMessage(Diagnostic.Kind.ERROR, error);
    }

}
