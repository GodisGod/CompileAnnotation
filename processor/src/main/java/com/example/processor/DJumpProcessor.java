package com.example.processor;

import com.example.annotation.QJump;

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
import javax.lang.model.element.TypeElement;

/**
 * Created by hongda on 2019-09-10.
 * 快速跳转传参的注解解析器
 */
public class DJumpProcessor extends AbstractProcessor {

    //存放同一个Class下的所有视图注解信息,key = 类名 value = 注解元素集合
    Map<TypeElement, List<Element>> classMap = new HashMap<>();


    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        DUtil dUtil = DUtil.getUtil();
        dUtil.setElementUtils(processingEnvironment.getElementUtils());
        dUtil.setFiler(processingEnvironment.getFiler());
        dUtil.setMessager(processingEnvironment.getMessager());

        DUtil.log("初始化   =====================    快速跳转的注解处理器");
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        DUtil.log("快速跳转的注解处理器");


        DUtil.log("快速跳转的注解处理器处理完毕");
        return false;
    }


    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotationTypes = new HashSet<>();
        annotationTypes.add(QJump.class.getCanonicalName());
        return annotationTypes;
    }
}
