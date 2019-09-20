package com.example.processor.util;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import static com.example.processor.util.CommonUtils.getTypeName;

/**
 * Created by hongda on 2019-09-20.
 */
public class GenerateCodeUtil {

    private final static String INTENT_PATH = "android.content.Intent";
    private final static String CONTEXT_PATH = "android.content.Context";
    private final static String ACTIVITY_PATH = "android.app.Activity";
    public static final String BUNDLE_PATH = "android.os.Bundle";

    /**
     * activity的注册方法
     *
     * @param builder
     * @param List<VariableElement> 注解的参数
     *                              <p>
     *                              Intent intent = secondActivity.getIntent();
     *                              if (intent != null) {
     *                              ArgsData argsData = (ArgsData) intent.getSerializableExtra(TAG);
     *                              if (argsData != null) {
     *                              secondActivity.name = argsData.getName();
     *                              secondActivity.value = argsData.getValue();
     *                              }
     *                              }
     */
    public static void generateActivityInjectCode(MethodSpec.Builder builder, List<VariableElement> elements) {
        builder.addStatement("$L intent = target.getIntent()", INTENT_PATH);
        builder.beginControlFlow("if(intent != null)")
                .addStatement("ArgsData argsData = (ArgsData) intent.getSerializableExtra(TAG)")
                .addCode("if (argsData != null) {");

        for (VariableElement v : elements) {
//            builder.addStatement("target." + v.getSimpleName() + "=" + "argsData." +
//                    StringUtils.getGetMethodName(v.getSimpleName().toString()) + "()");

            builder.addCode("target.$L=argsData.$L();", v.getSimpleName(), StringUtils.getGetMethodName(v.getSimpleName().toString()));
        }

        builder.addCode("}")
                .endControlFlow();

    }

    /**
     * 生成activity跳转方法
     *
     * @param builder
     * @param typeElement
     */
    //    public void start(Context fromContext, int requestCode) {
    //        this.requestCode = requestCode;
    //        start(fromContext);
    //    }
    //
    //    /**
    //     * fragment需要传activity的context才可以使用startActivityForResult
    //     */
    //    public void start(Context fromContext) {
    //        Intent intent = new Intent(fromContext, SecondActivity.class);
    //        intent.putExtra(TAG, argsData);
    //        if (fromContext instanceof Activity) {
    //            if (requestCode == -1) {
    //                fromContext.startActivity(intent);
    //            } else {
    //                ((Activity) fromContext).startActivityForResult(intent, requestCode);
    //            }
    //        } else {
    //            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    //            fromContext.startActivity(intent);
    //        }
    //    }
    public static void generateActivityStartCode(TypeSpec.Builder typeBuilder, TypeElement typeElement) {
        MethodSpec.Builder builder1 = MethodSpec.methodBuilder("start")
                .addParameter(getTypeName(CONTEXT_PATH), "fromContext")
                .addParameter(getTypeName(int.class.getCanonicalName().toString()), "requestCode")
                .addCode("this.requestCode = requestCode;")
                .addStatement("start(fromContext)");

        MethodSpec.Builder builder2 = MethodSpec.methodBuilder("start")
                .addParameter(getTypeName(CONTEXT_PATH), "fromContext")
                .addStatement("$L intent = new $L(fromContext,$L)",
                        INTENT_PATH, INTENT_PATH, typeElement.getSimpleName() + ".class")
                .addCode("intent.putExtra(TAG, argsData);")
                .addCode(" if (fromContext instanceof $L) {", ACTIVITY_PATH)
                .addCode("if (requestCode == -1) {")
                .addCode(" fromContext.startActivity(intent);")
                .addCode("} else {")
                .addCode(" (($L) fromContext).startActivityForResult(intent, requestCode);", ACTIVITY_PATH)
                .addCode("}")
                .addCode(" } else {")
                .addCode(" intent.addFlags($L.FLAG_ACTIVITY_NEW_TASK);", INTENT_PATH)
                .addCode("  fromContext.startActivity(intent);")
                .addCode("}");


        typeBuilder.addMethod(builder1.build());
        typeBuilder.addMethod(builder2.build());

    }

    /**
     * fragment的注册方法
     *
     * @param builder
     * @param List<VariableElement> elements
     *                              <p>
     *                              if (fragmentTest == null) {
     *                              return;
     *                              }
     *                              <p>
     *                              Bundle arguments = fragmentTest.getArguments();
     *                              if (arguments != null) {
     *                              ArgsData argsData = (ArgsData) arguments.getSerializable(TAG);
     *                              if (argsData != null) {
     *                              fragmentTest.name = argsData.getName();
     *                              fragmentTest.value = argsData.getValue();
     *                              }
     *                              }
     */
    public static void generateFragmentCode(MethodSpec.Builder builder, List<VariableElement> elements) {

        builder.addStatement("$L arguments = target.getArguments()", BUNDLE_PATH);

        builder.beginControlFlow("if(arguments != null)")
                .addStatement("ArgsData argsData = (ArgsData) arguments.getSerializable(TAG)")
                .addCode("if (argsData != null) {");

        for (VariableElement v : elements) {
//            builder.addStatement("target." + v.getSimpleName() + "=" + "argsData." +
//                    StringUtils.getGetMethodName(v.getSimpleName().toString()) + "()");

            builder.addCode("target.$L=argsData.$L();", v.getSimpleName(), StringUtils.getGetMethodName(v.getSimpleName().toString()));
        }

        builder.addCode("}")
                .endControlFlow();

    }

    /**
     * 生成建造fragment的方法
     *
     * @param builder
     * @param typeElement public FragmentTest build() {
     *                    FragmentTest fragmentTest = new FragmentTest();
     *                    Bundle bundle = new Bundle();
     *                    bundle.putSerializable(TAG, argsData);
     *                    fragmentTest.setArguments(bundle);
     *                    return fragmentTest;
     *                    }
     */
    public static void generateFragmentBuildCode(TypeSpec.Builder typeBuilder, TypeElement typeElement) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("build")
                .returns(getTypeName(typeElement.getSimpleName().toString()))
                .addModifiers(Modifier.PUBLIC)
                .addStatement("$L target = new $L()", typeElement.getSimpleName(), typeElement.getSimpleName())
                .addCode("$L bundle = new $L();", BUNDLE_PATH, BUNDLE_PATH)
                .addCode("bundle.putSerializable(TAG, argsData);")
                .addCode("target.setArguments(bundle);")
                .addCode("return target;");
        typeBuilder.addMethod(builder.build());
    }

}
