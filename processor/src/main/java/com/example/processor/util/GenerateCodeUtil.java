package com.example.processor.util;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.List;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import static com.example.processor.util.CommonUtils.getTypeName;

/**
 * Created by hongda on 2019-09-20.
 */
public class GenerateCodeUtil {

    private final static String INTENT_NAME = "android.content.Intent";
    private final static String CONTEXT_NAME = "android.content.Context";
    private final static String ACTIVITY_NAME = "android.app.Activity";

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
        builder.addStatement("$L intent = target.getIntent()", INTENT_NAME);
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
                .addParameter(getTypeName(CONTEXT_NAME), "fromContext")
                .addParameter(getTypeName(int.class.getCanonicalName().toString()), "requestCode")
                .addCode("this.requestCode = requestCode;")
                .addStatement("start(fromContext)");

        MethodSpec.Builder builder2 = MethodSpec.methodBuilder("start")
                .addParameter(getTypeName(CONTEXT_NAME), "fromContext")
                .addStatement("$L intent = new $L(fromContext,$L)",
                        INTENT_NAME, INTENT_NAME, typeElement.getSimpleName() + ".class")
                .addCode("intent.putExtra(TAG, argsData);")
                .addCode(" if (fromContext instanceof $L) {", ACTIVITY_NAME)
                .addCode("if (requestCode == -1) {")
                .addCode(" fromContext.startActivity(intent);")
                .addCode("} else {")
                .addCode(" (($L) fromContext).startActivityForResult(intent, requestCode);", ACTIVITY_NAME)
                .addCode("}")
                .addCode(" } else {")
                .addCode(" intent.addFlags($L.FLAG_ACTIVITY_NEW_TASK);", INTENT_NAME)
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
     */
    public static void generateFragmentCode(MethodSpec.Builder builder, List<VariableElement> elements) {


    }

}
