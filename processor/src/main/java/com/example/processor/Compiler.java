package com.example.processor;

import com.example.annotation.QtInit;
import com.example.annotation.QtInject;
import com.example.annotation.QtRouter;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

public class Compiler extends AbstractProcessor {
    private static final String TAG = "[QtInject] :";
    private boolean isFirst;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(QtInject.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        UtilMgr mgr = UtilMgr.getMgr();
        mgr.setElementUtils(processingEnv.getElementUtils());
        mgr.setFiler(processingEnv.getFiler());
        mgr.setMessager(processingEnv.getMessager());
        mgr.setTypeUtils(processingEnv.getTypeUtils());
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "-----------------------------");
        if (isFirst) {
            return false;
        }
        isFirst = true;
        long startTime = System.currentTimeMillis();
        EleParser.getInstance().initPreCode();

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(QtInject.class);
        Set<? extends Element> routers = roundEnv.getElementsAnnotatedWith(QtRouter.class);
        if(!init(roundEnv)){
            return true;
        }
        for (Element ele : elements) {
            if (ele.getKind() == ElementKind.FIELD) {
                EleParser.getInstance().parser(processingEnv, ele, false,true);
            } else if (ele.getKind() == ElementKind.CLASS) {
                EleParser.getInstance().parser(processingEnv, ele, true,true);
            }
        }
        for (Element ele : routers) {
            if (ele.getKind() == ElementKind.CLASS) {
                EleParser.getInstance().parser(processingEnv, ele, true,false);
            }
        }
        EleParser.getInstance().build();
        log("the auto-generate time is " + (System.currentTimeMillis() - startTime));
        return true;
    }

    public boolean init(RoundEnvironment roundEnv){
        Set<? extends Element> init = roundEnv.getElementsAnnotatedWith(QtInit.class);
        if(init==null||init.size()==0){
            error("must use @QtInit in your app,Otherwise can not use the @QtInject and @QtRouter");
            return false;
        }
        if(init.size()>1){
            error("the @QtInit Can be used only once ,Otherwise can not use the @QtInject and @QtRouter");
            return false;
        }
        Element initEle=init.iterator().next();
        if(initEle.getKind()!=ElementKind.CLASS){
            error("the @QtInit can only be used in the class");
            return false;
        }
        EleParser.getInstance().init(initEle);
        return true;
    }

    private void error(String error) {
        UtilMgr.getMgr().getMessager().printMessage(Diagnostic.Kind.ERROR, TAG + error);
    }

    private void log(String log) {
        UtilMgr.getMgr().getMessager().printMessage(Diagnostic.Kind.WARNING, TAG + log);
    }
}
