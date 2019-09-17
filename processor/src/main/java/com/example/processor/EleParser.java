package com.example.processor;

import com.example.annotation.QtInject;
import com.example.processor.data.ClazzData;
import com.example.processor.data.QtData;
import com.example.processor.data.QtFieldData;
import com.example.processor.factory.ActivityFactory;
import com.example.processor.factory.FragmentFactory;
import com.example.processor.factory.RouteFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;

/**
 * author: liutao
 * date: 2016/6/17.
 */
public class EleParser {
    private static final String APP_NAME = "android.app.Application";//
    private static final String APPLOGIC_NAME = "android.app.Application";//
    private static final String ACT_NAME = "android.app.Activity";
    private static final String FRAG_NAME = "android.app.Fragment";
    private static final String V4_FRAG_NAME = "android.support.v4.app.Fragment";
    private static final String SPLIT = "@@";
    ProcessingEnvironment log;

    RouteFactory routeFactory;

    public enum ElementType {
        APPLICATION, ACTIVITY, FRAGMENT
    }

    private static EleParser parser;

    Map<String, List<VariableElement>> fieldMap = new HashMap<>();
    Map<String, ClazzData> clazzMap = new HashMap<>();

    private EleParser() {
        routeFactory = new RouteFactory();
    }

    public static EleParser getInstance() {
        if (parser == null) {
            parser = new EleParser();
        }

        return parser;
    }

    protected void init(Element element) {
        routeFactory.setModelClassName(getPkgName((TypeElement) element));
    }

    protected void parser(ProcessingEnvironment log, Element element, boolean isClass, boolean isInject) {
        this.log = log;
        //put field into map group by class
        if (isInject) {
            parserInjectAnmation(element, isClass);
        } else {
            parserRouterAnmation(element, isClass);
        }

    }

    private void parserRouterAnmation(Element element, boolean isClass) {
        VariableElement variableElement = null;
        TypeElement typeElement;
        if (!isClass) {
            variableElement = (VariableElement) element;
            typeElement = (TypeElement) variableElement.getEnclosingElement();
        } else {
            typeElement = (TypeElement) element;
        }
        ElementType type = getElementType(typeElement);

        if (isClass) {
            routeFactory.parserRouter(element, type);
        }

    }


    protected void build() {
        parserData();
    }

    public void initPreCode() {
        routeFactory.initPreCode();
    }


    private void parserInjectAnmation(Element element, boolean isClass) {
        VariableElement variableElement = null;
        TypeElement typeElement;
        String bundleName = "";
        if (!isClass) {
            variableElement = (VariableElement) element;
            typeElement = (TypeElement) variableElement.getEnclosingElement();
        } else {
            typeElement = (TypeElement) element;

        }
        List<VariableElement> fields = null;
        String key = typeElement.getQualifiedName().toString();
        ElementType type = getElementType(typeElement);
        key = key + SPLIT + type.name();

        fields = fieldMap.get(key);
        if (fields == null) {
            QtInject qtInject = element.getAnnotation(QtInject.class);
            bundleName = qtInject.bundle();
            fieldMap.put(key, fields = new ArrayList<VariableElement>());
            ClazzData data = new ClazzData();
            data.setBundleName(bundleName);
            data.setAbstract(isAbstract(typeElement));
            data.setPageName(getPkgName(typeElement));
            data.setSimpleName(typeElement.getSimpleName().toString());
            clazzMap.put(key, data);
        }
        if (!isClass) {
            fields.add(variableElement);
        }
        if (type == ElementType.APPLICATION) {
            routeFactory.setModelClassName(getPkgName(typeElement));
        }
    }

    private ElementType getElementType(TypeElement typeElement) {
        ElementType type = ElementType.ACTIVITY;
        if (checkIsSubClass(typeElement, ACT_NAME)) {
            type = ElementType.ACTIVITY;
        } else if (checkIsSubClass(typeElement, FRAG_NAME) || checkIsSubClass(typeElement, V4_FRAG_NAME)) {
            type = ElementType.FRAGMENT;
        } else if (checkIsSubClass(typeElement, APP_NAME) || checkIsSubClass(typeElement, APPLOGIC_NAME)) {
            type = ElementType.APPLICATION;
        } else {
            throw new IllegalArgumentException(String.format("class %s must be extends from %s or %s or %s or %s", typeElement.getQualifiedName(), ACT_NAME, FRAG_NAME, V4_FRAG_NAME, APP_NAME));
        }
        return type;
    }

    /**
     * check out if is target's subclass
     */
    private boolean checkIsSubClass(TypeElement typeElement, String target) {
        while (true) {
            if (typeElement == null) {
                return false;
            } else if (target.equals(typeElement.getQualifiedName().toString())) {
                return true;
            }
            typeElement = getParentClass(typeElement);
        }
    }

    TypeElement getParentClass(TypeElement child) {
        return (TypeElement) UtilMgr.getMgr().getTypeUtils().asElement(child.getSuperclass());
    }

    private void parserData() {
        QtData qtData = new QtData();
        for (String key : fieldMap.keySet()) {
            String keys[] = key.split(SPLIT);
            String type = keys[1];
            String clzName = keys[0];
            String bundleName = "";
            if (keys.length > 2) {
                bundleName = keys[2];
            }


            List<VariableElement> list = fieldMap.get(key);
            List<QtFieldData> qtFieldDatas = new ArrayList<>();
            ElementType elementType;
            if (type.equals(ElementType.ACTIVITY.name())) {
                elementType = ElementType.ACTIVITY;
            } else if (type.equals(ElementType.FRAGMENT.name())) {
                elementType = ElementType.FRAGMENT;
            } else {
                elementType = ElementType.APPLICATION;
            }
            VariableElement first = null;
            for (VariableElement element : list) {
                if (first == null) {
                    first = element;
                }
                QtInject router = element.getAnnotation(QtInject.class);
                QtFieldData qtFieldData = new QtFieldData();
                qtFieldData.setFieldType(element.asType().toString());
                qtFieldData.setName(element.getSimpleName().toString());
                qtFieldData.setAlias(router.alias());
                qtFieldData.setBundle(router.bundle());
                qtFieldData.setOpenDefault(router.defaultValue());
                qtFieldData.setDoc("create from QtInject");
                qtFieldDatas.add(qtFieldData);
            }
            qtData.setElementType(elementType);
            qtData.setList(qtFieldDatas);
            qtData.setClzName(clzName);
            qtData.setClazzData(clazzMap.get(key));
            try {
                log.getMessager().printMessage(Diagnostic.Kind.NOTE, "begin-----------------");
                if (elementType == ElementType.ACTIVITY) {
                    new ActivityFactory(qtData).generateCode();
                } else if (elementType == ElementType.FRAGMENT) {
                    new FragmentFactory(qtData).generateCode();
                } else {//
                }
                if (first != null) {
                    routeFactory.parserInject(first.getEnclosingElement(), elementType);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        generateRouter();
    }

    private void generateRouter() {
        routeFactory.generateRouter();
    }

    public boolean isAbstract(TypeElement typeElement) {
        Set<Modifier> modifiers = typeElement.getModifiers();
        return modifiers.contains(Modifier.ABSTRACT);
    }

    public String getPkgName(TypeElement typeElement) {
        PackageElement pkgElement = UtilMgr.getMgr().getElementUtils().getPackageOf(typeElement);
        return pkgElement.isUnnamed() ? "" : pkgElement.getQualifiedName().toString();
    }
}
