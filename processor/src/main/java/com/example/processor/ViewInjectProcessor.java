package com.example.processor;

import com.example.annotation.DBindView;
import com.example.annotation.DClick;
import com.example.annotation.DLongClick;
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
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

public class ViewInjectProcessor extends AbstractProcessor {

    //存放同一个Class下的所有视图注解信息,key = 类名 value = 注解元素集合
    Map<TypeElement, List<Element>> classMap = new HashMap<>();

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
        generateCode();
        return false;
    }

    //返回支持的注解类型
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotationTypes = new HashSet<>();
        annotationTypes.add(DBindView.class.getCanonicalName());
        annotationTypes.add(DClick.class.getCanonicalName());
        annotationTypes.add(DLongClick.class.getCanonicalName());
        return annotationTypes;
    }

    //返回支持的源码版本
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    void collectInfo(RoundEnvironment roundEnvironment) {
        classMap.clear();

        messager.printMessage(Diagnostic.Kind.NOTE, "开始收集注解信息");

        checkAllAnnotations(roundEnvironment, DBindView.class);
        checkAllAnnotations(roundEnvironment, DClick.class);
        checkAllAnnotations(roundEnvironment, DLongClick.class);

        messager.printMessage(Diagnostic.Kind.NOTE, "注解信息收集完毕");
    }

    private void checkAllAnnotations(RoundEnvironment roundEnvironment, Class<? extends Annotation> annotationClass) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(annotationClass);

        for (Element element : elements) {
            //被注解元素所在的Class
            TypeElement typeElement = (TypeElement) element.getEnclosingElement();

            // 收集Class中所有被注解的元素
            List<Element> els = classMap.get(typeElement);
            if (els == null) {
                els = new ArrayList<>();
                classMap.put(typeElement, els);
            }
            els.add(element);
        }

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
        messager.printMessage(Diagnostic.Kind.NOTE, "开始生成相关类文件");

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
                        DBindView dBindView = e.getAnnotation(DBindView.class);
                        int viewId = dBindView.value();

                        // 在构造方法中增加赋值语句，例如：target.tv = (android.widget.TextView)v.findViewById(215334);
                        messager.printMessage(Diagnostic.Kind.NOTE, "LHDDD variableName = " + variableName + "  variableFullName = " + variableFullName + "  variableInfo.getViewId() = " + viewId);

                        // target.textView=(android.widget.TextView)v.findViewById(2131165326);
                        methodBuilder.addStatement("target.$L=($L)v.findViewById($L)", variableName, variableFullName, viewId);

                    } else if (kind == ElementKind.METHOD) {

                        ExecutableElement executableElement = (ExecutableElement) e;

                        if (!checkMethod(executableElement)) {
                            return;
                        }

                        // 变量类型的完整类路径（比如：android.widget.TextView）
//                        String variableFullName = e.getSimpleName().toString();

                        // 获取 BindView 注解的值
                        DClick dBindView = e.getAnnotation(DClick.class);
                        if (dBindView != null) {
                            int viewId = dBindView.value();

                            // 在构造方法中增加赋值语句，例如：target.tv = (android.widget.TextView)v.findViewById(215334);
                            messager.printMessage(Diagnostic.Kind.NOTE, "LHDDD" + "  variableInfo.getViewId() = " + viewId);

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


                        // 获取 BindView 注解的值
                        DLongClick dBindView2 = e.getAnnotation(DLongClick.class);

                        if (dBindView2 != null) {
                            int viewId2 = dBindView2.value();

                            // 在构造方法中增加赋值语句，例如：target.tv = (android.widget.TextView)v.findViewById(215334);
                            messager.printMessage(Diagnostic.Kind.NOTE, "LHDDD" + "  variableInfo.getViewId() = " + viewId2);

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

                    }


                }

                final String pakageName = getPackageName(typeElement);
                final String className = getClassName(typeElement, pakageName) + "$$Proxy";
                //2、构建Class
                TypeSpec typeSpec = TypeSpec.classBuilder(className)
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(methodBuilder.build())
                        .build();

                // 与目标Class放在同一个包下，解决Class属性的可访问性
                String packageFullName = elementUtils.getPackageOf(typeElement).getQualifiedName().toString();

                System.out.println("LHDDD ======================= = " + packageFullName);

                JavaFile javaFile = JavaFile.builder(packageFullName, typeSpec).build();
                // 生成class文件
                javaFile.writeTo(filer);


            } catch (Exception ex) {
                ex.printStackTrace();
            }


        }


    }

    private String getClassName(TypeElement type, String pkgName) {
        int packageLength = pkgName.length() + 1;

        messager.printMessage(Diagnostic.Kind.NOTE, "pakageName = " + pkgName + "  type.getQualifiedName().toString() = " + type.getQualifiedName().toString());

        //com.example.compileannotation.TestAdapter.TestHolder
        return type.getQualifiedName().toString().substring(packageLength).replace('.', '$');
    }

    private String getPackageName(TypeElement type) {
        return elementUtils.getPackageOf(type).getQualifiedName().toString();
    }

}
