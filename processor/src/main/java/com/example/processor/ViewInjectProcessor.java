package com.example.processor;

import com.example.annotation.BindView;
import com.example.annotation.ClickEvent;
import com.example.annotation.ClickEvents;
import com.example.annotation.LongClickEvent;
import com.example.processor.util.CommonUtils;
import com.example.processor.util.DUtil;
import com.example.processor.util.MethodCheckUtil;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * 视图绑定注解信息解析器
 */
public class ViewInjectProcessor extends AbstractProcessor {

    //存放同一个Class下的所有视图注解信息,key = 类名 value = 注解元素集合
    Map<TypeElement, List<Element>> classMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        DUtil dUtil = DUtil.getUtil();
        dUtil.setElementUtils(processingEnvironment.getElementUtils());
        dUtil.setFiler(processingEnvironment.getFiler());
        dUtil.setMessager(processingEnvironment.getMessager());

    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        System.out.println("====================================");
        //1、收集 Class 内的所有被 @BindView 注解的成员变量；
        collectInfo(roundEnvironment);
        //2、根据上一步收集的内容，生成 .java 源文件。
        generateCode();
        return false;
    }

    //返回支持的注解类型
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotationTypes = new HashSet<>();
        annotationTypes.add(BindView.class.getCanonicalName());
        annotationTypes.add(ClickEvent.class.getCanonicalName());
        annotationTypes.add(LongClickEvent.class.getCanonicalName());
        return annotationTypes;
    }

    //返回支持的源码版本
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private void collectInfo(RoundEnvironment roundEnvironment) {
        classMap.clear();

        DUtil.log("开始收集注解信息");

        checkAllAnnotations(roundEnvironment, BindView.class);
        checkAllAnnotations(roundEnvironment, ClickEvent.class);
        checkAllAnnotations(roundEnvironment, ClickEvents.class);
        checkAllAnnotations(roundEnvironment, LongClickEvent.class);

        DUtil.log("注解信息收集完毕");
    }

    private boolean checkAllAnnotations(RoundEnvironment roundEnvironment, Class<? extends Annotation> annotationClass) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(annotationClass);

        if (elements == null || elements.size() < 1) {

            DUtil.log("没有收集到注解信息:" + annotationClass);
            return false;
        }

        for (Element element : elements) {
            //被注解元素所在的Class
            TypeElement typeElement = (TypeElement) element.getEnclosingElement();

            // 收集Class中所有被注解的元素
            List<Element> els = classMap.get(typeElement);
            if (els == null) {
                els = new ArrayList<>();
                classMap.put(typeElement, els);

                DUtil.log("解析类 = " + typeElement.asType().toString() + "  " + annotationClass);
            }
            els.add(element);
        }

        return true;

    }

    private boolean checkMethod(Element element) {
        if (element.getKind() != ElementKind.METHOD) {
            return false;
        }
        if (MethodCheckUtil.isPrivate(element) || MethodCheckUtil.isAbstract(element)) {
            return false;
        }
        return true;
    }


    //通过 javapoet 来生成 .java 源文件
    void generateCode() {

        DUtil.log("开始生成相关类文件");

        /**
         * 遍历每一个类
         */
        for (TypeElement typeElement : classMap.keySet()) {
            //1、 使用构造函数绑定视图数据
            MethodSpec.Builder methodBuilder = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ParameterSpec.builder(TypeName.get(typeElement.asType()), "target", Modifier.FINAL).build())
                    .addParameter(ClassName.get("android.view", "View"), "v");

            try {
                // Class的完整路径
                String classFullName = typeElement.getQualifiedName().toString();

                List<Element> elements = classMap.get(typeElement);

                for (Element e : elements) {
                    ElementKind kind = e.getKind();

                    if (kind == ElementKind.FIELD) {
                        // 变量名称(比如：TextView tv 的 tv)
                        String variableName = e.getSimpleName().toString();
                        // 变量类型的完整类路径（比如：android.widget.TextView）
                        String variableFullName = e.asType().toString();

                        // 获取 BindView 注解的值
                        BindView bindView = e.getAnnotation(BindView.class);
                        int viewId = bindView.value();

                        // 在构造方法中增加赋值语句，例如：target.tv = (android.widget.TextView)v.findViewById(215334);
                        DUtil.log("LHDDD variableName = " + variableName + "  variableFullName = " + variableFullName + "  variableInfo.getViewId() = " + viewId);

                        // target.textView=(android.widget.TextView)v.findViewById(2131165326);
                        methodBuilder.addStatement("target.$L=($L)v.findViewById($L)", variableName, variableFullName, viewId);

                    } else if (kind == ElementKind.METHOD) {

                        ExecutableElement executableElement = (ExecutableElement) e;

                        if (!checkMethod(executableElement)) {
                            return;
                        }

                        // 变量类型的完整类路径（比如：android.widget.TextView）
//                        String variableFullName = e.getSimpleName().toString();

                        // 获取 ClickEvent 注解的值
                        ClickEvent dBindView = e.getAnnotation(ClickEvent.class);
                        if (dBindView != null) {
                            int viewId = dBindView.value();

                            // 在构造方法中增加赋值语句，例如：target.tv = (android.widget.TextView)v.findViewById(215334);
                            DUtil.log("LHDDD" + "  variableInfo.getViewId() = " + viewId);

                            methodBuilder.addStatement(
                                    "android.view.View view = (android.view.View)v.findViewById($L)",
                                    viewId);

                            //2、绑定点击事件
                            MethodSpec innerMethodSpec = MethodSpec.methodBuilder("onClick")
                                    .addAnnotation(Override.class)
                                    .addModifiers(Modifier.PUBLIC)
                                    .returns(void.class)
                                    .addParameter(ClassName.get("android.view", "View"), "v")
                                    .addStatement("target.$L()", executableElement.getSimpleName().toString())
                                    .build();
                            TypeSpec innerTypeSpec = TypeSpec.anonymousClassBuilder("")
                                    .addSuperinterface(ClassName.bestGuess("View.OnClickListener"))
                                    .addMethod(innerMethodSpec)
                                    .build();
                            methodBuilder.addStatement("view.setOnClickListener($L)", innerTypeSpec);

                        }


                        // 获取 LongClickEvent 注解的值
                        LongClickEvent dBindView2 = e.getAnnotation(LongClickEvent.class);

                        if (dBindView2 != null) {
                            int viewId2 = dBindView2.value();

                            // 在构造方法中增加赋值语句，例如：target.tv = (android.widget.TextView)v.findViewById(215334);
                            DUtil.log("LHDDD" + "  variableInfo.getViewId() = " + viewId2);

                            methodBuilder.addStatement(
                                    "android.view.View longClickView = (android.view.View)v.findViewById($L)",
                                    viewId2);

                            //2、绑定点击事件
                            MethodSpec innerMethodSpec2 = MethodSpec.methodBuilder("onLongClick")
                                    .addAnnotation(Override.class)
                                    .addModifiers(Modifier.PUBLIC)
                                    .returns(TypeName.BOOLEAN)
                                    .addParameter(ClassName.get("android.view", "View"), "v")
                                    .addStatement("target.$L()", executableElement.getSimpleName().toString())
                                    .addStatement("return true")
                                    .build();
                            TypeSpec innerTypeSpec2 = TypeSpec.anonymousClassBuilder("")
                                    .addSuperinterface(ClassName.bestGuess("View.OnLongClickListener"))
                                    .addMethod(innerMethodSpec2)
                                    .build();
                            methodBuilder.addStatement("longClickView.setOnLongClickListener($L)", innerTypeSpec2);

                        }

                        // 获取 ClickEvents 注解的值
                        ClickEvents clickEvents = e.getAnnotation(ClickEvents.class);
                        DUtil.log("LHDDD" + "  点击事件组 = clickEvents = " + clickEvents);
                        if (clickEvents != null && clickEvents.value().length > 0) {

                            List<? extends VariableElement> parameters = ((ExecutableElement) e).getParameters();
                            DUtil.log("注解的方法的参数 = " + parameters.size() + " " + parameters.get(0));
                            if (parameters == null || parameters.size() != 1) {
                                DUtil.error("注解的方法必须有且只能有一个参数" + parameters.size());
                                return;
                            }

                            VariableElement variableElement = parameters.get(0);
                            TypeMirror typeMirror = variableElement.asType();
                            String type = typeMirror.toString();
                            if (!"android.view.View".equals(type)) {
                                DUtil.error("注解的方法必须有且只有一个参数View");
                                return;
                            }

                            for (int i = 0; i < clickEvents.value().length; i++) {

                                int id = clickEvents.value()[i];
                                // 在构造方法中增加赋值语句，例如：target.tv = (android.widget.TextView)v.findViewById(215334);
                                String clickViewName = "View" + i;
                                methodBuilder.addStatement(
                                        "android.view.View " + clickViewName + " = (android.view.View)v.findViewById($L)",
                                        id);

                                //2、绑定点击事件
                                MethodSpec innerMethodSpec = MethodSpec.methodBuilder("onClick")
                                        .addAnnotation(Override.class)
                                        .addModifiers(Modifier.PUBLIC)
                                        .returns(void.class)
                                        .addParameter(ClassName.get("android.view", "View"), "v")
                                        .addStatement("target.$L(v)", executableElement.getSimpleName().toString())
                                        .build();
                                TypeSpec innerTypeSpec = TypeSpec.anonymousClassBuilder("")
                                        .addSuperinterface(ClassName.bestGuess("View.OnClickListener"))
                                        .addMethod(innerMethodSpec)
                                        .build();
                                methodBuilder.addStatement(clickViewName + ".setOnClickListener($L)", innerTypeSpec);


                            }

                        }

                    }

                }

                final String pakageName = CommonUtils.getPackageName(typeElement);
                final String className = CommonUtils.getClassName(typeElement, pakageName) + "$$Proxy";
                //2、构建Class
                TypeSpec typeSpec = TypeSpec.classBuilder(className)
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(methodBuilder.build())
                        .build();

                // 与目标Class放在同一个包下，解决Class属性的可访问性
                String packageFullName = DUtil.getUtil().getElementUtils().getPackageOf(typeElement).getQualifiedName().toString();

                JavaFile javaFile = JavaFile.builder(packageFullName, typeSpec).build();
                // 生成class文件
                javaFile.writeTo(DUtil.getUtil().getFiler());


            } catch (Exception ex) {
                ex.printStackTrace();
                DUtil.error(ex.getMessage());
            }


        }


    }

}
