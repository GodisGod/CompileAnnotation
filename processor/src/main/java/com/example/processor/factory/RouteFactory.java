package com.example.processor.factory;

import com.example.annotation.QtRouter;
import com.example.processor.EleParser;
import com.example.processor.UtilMgr;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

/**
 * Created by liutao on 05/07/2017.
 */

public class RouteFactory {
    private static String MODEL_CLASS_NAME = "";
    private static String ROUTE_NAME = "QtInitializer";
    private static String ROUTE_METHOD_NAME = "getRouterTable";
    private static String Inject_METHOD_NAME = "getInjectTable";
    private MethodSpec.Builder routerInitBuilder;
    private MethodSpec.Builder injectInitBuilder;
    private static final String ACT_NAME = "android.app.Activity";
    final static String I_INJECT_PATH = "com.example.annotation.IInject";

    public void initPreCode() {
        initRoute();
        initInject();
    }

    private void initRoute() {
        TypeElement activityType = UtilMgr.getMgr().getElementUtils().getTypeElement("android.app.Activity");
        ParameterizedTypeName mapTypeName = ParameterizedTypeName
                .get(ClassName.get(Map.class), ClassName.get(String.class), ParameterizedTypeName.get(ClassName.get(Class.class),
                        WildcardTypeName.subtypeOf(ClassName.get(activityType))));
        ParameterSpec mapParameterSpec = ParameterSpec.builder(mapTypeName, "router")
                .build();

        routerInitBuilder = MethodSpec.methodBuilder(ROUTE_METHOD_NAME)
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.STATIC)
                .addParameter(mapParameterSpec)
                .returns(mapTypeName);
    }

    private void initInject() {
        TypeElement injectType = UtilMgr.getMgr().getElementUtils().getTypeElement(I_INJECT_PATH);

        ParameterizedTypeName mapTypeName = ParameterizedTypeName
                .get(ClassName.get(List.class), ClassName.get(injectType));
        injectInitBuilder = MethodSpec.methodBuilder(Inject_METHOD_NAME)
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.STATIC)
                .addCode("List<"+I_INJECT_PATH+"> data =new java.util.ArrayList<>();\n")
                .returns(mapTypeName);
    }

    public void parserRouter(Element element, EleParser.ElementType elementType) {
        if(elementType== EleParser.ElementType.APPLICATION){
            return;
        }
        //路由url
        QtRouter router = element.getAnnotation(QtRouter.class);
        String[] routerUrls = router.value();
        if(elementType== EleParser.ElementType.ACTIVITY){
            for (String routerUrl : routerUrls) {
                routerInitBuilder.addStatement("router.put($S, $T.class)", routerUrl, ClassName.get((TypeElement) element));
            }
        }
    }
    public void parserInject(Element element, EleParser.ElementType elementType){
        if(elementType== EleParser.ElementType.APPLICATION){
            return;
        }
        //init inject
        injectInitBuilder.addCode("data.add(" +getPkgName((TypeElement) element)+"."+ String.valueOf("Qt" + ( element).getSimpleName().toString() + ".getInstance());\n"));
    }

    public void generateRouter() {
        if (MODEL_CLASS_NAME == null || MODEL_CLASS_NAME.isEmpty()) {
            return;
        }
        routerInitBuilder.addCode("return router;\n");
        injectInitBuilder.addCode("return data;\n");
        MethodSpec routerInitMethod = routerInitBuilder.build();
        TypeSpec type = TypeSpec.classBuilder(ROUTE_NAME)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(routerInitMethod)
                .addMethod(injectInitBuilder.build())
                .build();

        try {
            if (type != null) {
                JavaFile.builder(MODEL_CLASS_NAME, type).build().writeTo(UtilMgr.getMgr().getFiler());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setModelClassName(String modelClassName) {
        MODEL_CLASS_NAME = modelClassName;
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
    public String getPkgName(TypeElement typeElement) {
        PackageElement pkgElement = UtilMgr.getMgr().getElementUtils().getPackageOf(typeElement);
        return pkgElement.isUnnamed() ? "" : pkgElement.getQualifiedName().toString();
    }

}
