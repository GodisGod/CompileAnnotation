package com.example.processor;

import com.example.annotation.RBindView;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

public class ViewInjectProcessor extends AbstractProcessor {

    //存放同一个Class下的所有注解信息
    Map<String, List<VariableInfo>> classMap = new HashMap<>();
    // 存放Class对应的信息：TypeElement
    Map<String, TypeElement> classTypeElement = new HashMap<>();

    private Filer filer;
    Elements elementUtils;//操作元素
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        this.filer = processingEnvironment.getFiler();
        this.elementUtils = processingEnvironment.getElementUtils();
        messager = processingEnvironment.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        System.out.println("====================================");
        //1、收集 Class 内的所有被 @BindView 注解的成员变量；
        collectInfo(roundEnvironment);
        //2、根据上一步收集的内容，生成 .java 源文件。
        writeToFile();
        return false;
    }

    //返回支持的注解类型
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotationTypes = new HashSet<>();
        annotationTypes.add(RBindView.class.getCanonicalName());
        return annotationTypes;
    }

    //返回支持的源码版本
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    void collectInfo(RoundEnvironment roundEnvironment) {
        classMap.clear();
        classTypeElement.clear();

        messager.printMessage(Diagnostic.Kind.NOTE, "开始解析注解");

        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(RBindView.class);
        for (Element element : elements) {
            // 获取 BindView 注解的值。例如：   @RBindView(R.id.tv_test)，那么value就是R.id.tv_test的int值
            int viewId = element.getAnnotation(RBindView.class).value();

            // 代表被注解的元素
            VariableElement variableElement = (VariableElement) element;

            // 备注解元素所在的Class
            TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
            // Class的完整路径
            String classFullName = typeElement.getQualifiedName().toString();

            // 收集Class中所有被注解的元素
            List<VariableInfo> variableList = classMap.get(classFullName);
            if (variableList == null) {
                variableList = new ArrayList<>();
                classMap.put(classFullName, variableList);

                // 保存Class对应要素（名称、完整路径等）
                classTypeElement.put(classFullName, typeElement);
            }
            VariableInfo variableInfo = new VariableInfo();
            variableInfo.setVariableElement(variableElement);
            variableInfo.setViewId(viewId);
            variableList.add(variableInfo);
        }
    }

    //通过 javapoet 来生成 .java 源文件
    void writeToFile() {
        messager.printMessage(Diagnostic.Kind.NOTE, "开始生成相关类文件");
        try {
            for (String classFullName : classMap.keySet()) {
                TypeElement typeElement = classTypeElement.get(classFullName);

                // 使用构造函数绑定数据
                MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ParameterSpec.builder(TypeName.get(typeElement.asType()), "activity").build());
                List<VariableInfo> variableList = classMap.get(classFullName);
                for (VariableInfo variableInfo : variableList) {
                    VariableElement variableElement = variableInfo.getVariableElement();
                    // 变量名称(比如：TextView tv 的 tv)
                    String variableName = variableElement.getSimpleName().toString();
                    // 变量类型的完整类路径（比如：android.widget.TextView）
                    String variableFullName = variableElement.asType().toString();
                    // 在构造方法中增加赋值语句，例如：activity.tv = (android.widget.TextView)activity.findViewById(215334);
                    System.out.println("LHDDD variableName = " + variableName + "  variableFullName = " + variableFullName + "  variableInfo.getViewId() = " + variableInfo.getViewId());
                    constructor.addStatement("activity.$L=($L)activity.findViewById($L)", variableName, variableFullName, variableInfo.getViewId());

                    // activity.textView=(android.widget.TextView)activity.findViewById(2131165326);
                }

                // 构建Class
                TypeSpec typeSpec = TypeSpec.classBuilder(typeElement.getSimpleName() + "$$Proxy")
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(constructor.build())
                        .build();

                // 与目标Class放在同一个包下，解决Class属性的可访问性
                String packageFullName = elementUtils.getPackageOf(typeElement).getQualifiedName().toString();

                System.out.println("LHDDD ======================= = " + packageFullName);

                JavaFile javaFile = JavaFile.builder(packageFullName, typeSpec).build();
                // 生成class文件
                javaFile.writeTo(filer);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
