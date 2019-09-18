package com.example.processor.factory;

import com.example.annotation.QtInject;
import com.example.processor.data.QtData;
import com.example.processor.data.QtFieldData;
import com.example.processor.util.StringUtils;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.io.IOException;

import javax.lang.model.element.Modifier;

/**
 * Created by hongda on 2019-09-17.
 */
public class ActivityFactory extends FileFactory {
    private final static String REQUEST_CODE_FIELD_NAME = "requestCode";

    private final static String START_METHOD = "start";
    private final static String CREATE_INTENT = "createIntent";
    private final static String GETDATA_METHOD = "getArguments";
    private final static String ACTIVITY_NAME = "android.app.Activity";
    private final static String CONTEXT_NAME = "android.content.Context";
    private final static String FRAGMENT_NAME = "android.app.Fragment";
    private final static String V4FRAGMENT_NAME = "android.support.v4.app.Fragment";
    private final static String INTENT_NAME = "android.content.Intent";
    private final static String SUFFIX = "Qt";


    public ActivityFactory(QtData data) {
        super(data);
    }

    @Override
    public String getSuffix() {
        return SUFFIX;
    }

    public void generateCode() throws IOException {
        TypeSpec.Builder typeBuilder = generateTypeBuilder();
        // add field
        addFields(typeBuilder);
        //implement IInject method
        typeBuilder.addMethod(impIInject(typeBuilder, isEmptyParams));
        typeBuilder.addMethod(implKey(typeBuilder));
        if (!isEmptyParams) {

            // add class RequestData
            typeBuilder.addType(generateRequestData());
            // add get request data method
            typeBuilder.addMethod(createGetDataMethod());
            // add get ArgsData
            typeBuilder.addMethod(createGetArgsDataMethod());
            //add inject
            typeBuilder.addMethod(createInject());

            //add parser method
            typeBuilder.addMethod(createParser());



        }
        // create private constructor method
        typeBuilder.addMethod(createPrivateConstructor());
        // add create method
        typeBuilder.addMethod(createMethod());
        // add setter method
        addParamsSetMethod(typeBuilder);
        // add request code method
        addRequestCodeMethod(typeBuilder);
        // add create intent method
        addCreateIntentMethod(typeBuilder);


        if (!isAbstract) {
            // add start activity method
            addStartMethod(typeBuilder);
        }
        build(typeBuilder);
    }

    private MethodSpec createInject() {
        TypeName intent = getTypeName(qtData.getClazzData().getSimpleName());
        MethodSpec.Builder builder = MethodSpec.methodBuilder(INJECT)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(intent, "activity")
                .beginControlFlow("if(activity==null)")
                .addStatement("return")
                .endControlFlow();
        builder.addCode("ArgsData data=getArguments(activity.getIntent());\n");
        for (QtFieldData data : qtData.getList()) {
            builder.addStatement("if(data.$L||!$L){", StringUtils.getIsMethodName(data.getName()),data.isOpenDefault());
            builder.addStatement("\tactivity." + data.getName() + "=" + "data."+ StringUtils.getGetMethodName(data.getName())+ "()");
            builder.addStatement("}");
        }
        return builder.build();
    }


    private MethodSpec createGetDataMethod() {
        TypeName intent = getTypeName(INTENT_NAME);
        TypeName requestData = getTypeName(REQUEST_DATA_CLASS);
        String paramsName = "data";
        return MethodSpec.methodBuilder(GETDATA_METHOD)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(getTypeName(REQUEST_DATA_CLASS))
                .addParameter(intent, paramsName)
                .beginControlFlow("if (data == null || data.getSerializableExtra(TAG) == null)")
                .addStatement("return parser(data)")
                .endControlFlow()
                .addJavadoc("receive passed data,get data from intent by tag : $L", TAG_FIELD)
                .beginControlFlow("else")
                .addStatement("return ($T) data.getSerializableExtra(TAG)", requestData)
                .endControlFlow()
                .build();
    }

    private MethodSpec createParser() {

        TypeName intent = getTypeName(INTENT_NAME);
        String paramsName = "data";
        TypeName requestData = getTypeName(REQUEST_DATA_CLASS);
        TypeName andjumpData = getTypeName(ANDJUMP_PATH);
        MethodSpec.Builder builder = MethodSpec.methodBuilder(PARSER)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .returns(getTypeName(REQUEST_DATA_CLASS))
                .addParameter(intent, paramsName)
                .addStatement("$T arg=new $T()", requestData, requestData)
                .addStatement("if(data == null) return arg");

        if (qtData.getList() == null || qtData.getList().size() == 0) {
            return builder.addStatement("return arg").build();
        }
        String bundleName=qtData.getClazzData().getBundleName();


        for (QtFieldData fieldData : qtData.getList()) {
            builder.addCode("try { \n");
            String fieldBundleName=fieldData.getBundle();
            if(bundleName==null||bundleName.equals("")||bundleName.equals(QtInject.DEFAULT_BUNDLE)){
                if(fieldBundleName==null||fieldBundleName.equals("")||fieldBundleName.equals(QtInject.DEFAULT_BUNDLE)){
                    builder.addCode("\targ." +StringUtils.getSetMethodName(fieldData.getName() )+ "(($L)$L.parse($S,data,$S));\n", fieldData.getFieldType(),AND_PARSET_PATH, fieldData.getFieldType(), fieldData.getAlias());
                }else{
                    builder.addCode("\targ." +StringUtils.getSetMethodName(fieldData.getName() )+ "(($L)$L.parse($S,data,$S,$S));\n", fieldData.getFieldType(),AND_PARSET_PATH, fieldData.getFieldType(), fieldData.getAlias(),fieldBundleName);
                }
            }else {
                if (fieldBundleName != null&&!fieldBundleName.equals("")) {
                    if(fieldBundleName.equals(QtInject.DEFAULT_BUNDLE)){
                        builder.addCode("\targ." + StringUtils.getSetMethodName(fieldData.getName()) + "(($L)$L.parse($S,data,$S));\n", fieldData.getFieldType(), AND_PARSET_PATH, fieldData.getFieldType(), fieldData.getAlias());
                    }else{
                        builder.addCode("\targ." + StringUtils.getSetMethodName(fieldData.getName()) + "(($L)$L.parse($S,data,$S,$S));\n", fieldData.getFieldType(), AND_PARSET_PATH, fieldData.getFieldType(), fieldData.getAlias(), fieldBundleName);
                    }
                } else {
                    builder.addCode("\targ." + StringUtils.getSetMethodName(fieldData.getName()) + "(($L)$L.parse($S,data,$S,$S));\n", fieldData.getFieldType(), AND_PARSET_PATH, fieldData.getFieldType(), fieldData.getAlias(), bundleName);
                }
            }
            builder.addCode("}catch(java.lang.Exception e){\n " +
                    "if ($T.isDebug()){\n\t e.printStackTrace();\n}\n}\n", andjumpData);
        }

        return builder.addStatement("return arg").build();

    }

    private void addCreateIntentMethod(TypeSpec.Builder typeBuilder) {
        TypeName intent = getTypeName(INTENT_NAME);
        MethodSpec.Builder builder = MethodSpec.methodBuilder(CREATE_INTENT)
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("Create intent,put the instance of RequestData into it,and parent class request data")
                .returns(getTypeName(INTENT_NAME))
                .addParameter(getTypeName(CONTEXT_NAME), "context")
                .addStatement("$T intent = new $T($L,$L.class)",
                        intent, intent, "context", qtData.getClazzData().getSimpleName());


        if (!isEmptyParams) {
            builder.addStatement("intent.putExtra($L,$L)", TAG_FIELD, REQUEST_DATA_FIELD_NAME);
        }
        builder.addStatement("return intent");
        typeBuilder.addMethod(builder.build());
    }

    private void addStartMethod(TypeSpec.Builder typeBuilder) {
        String paramsName = "target";
        MethodSpec.Builder startByContext = createStartMethodBuilder(CONTEXT_NAME, paramsName, paramsName);
        typeBuilder.addMethod(
                startByContext
                        .addCode("if(target instanceof " + ACTIVITY_NAME + "){ \n((" + ACTIVITY_NAME + ")target).startActivityForResult(intent,$L);\n}\n", REQUEST_CODE_FIELD_NAME)
                        .addStatement("return this")
                        .addJavadoc("launcher a Activity by $L", CONTEXT_NAME)
                        .build()
        );

        MethodSpec.Builder startByActivity = createStartMethodBuilder(ACTIVITY_NAME, paramsName, paramsName);
        typeBuilder.addMethod(
                startByActivity.addStatement("target.startActivityForResult(intent,$L)", REQUEST_CODE_FIELD_NAME)
                        .addStatement("return this")
                        .addJavadoc("launcher a Activity by $L", ACTIVITY_NAME)
                        .build()
        );
        MethodSpec.Builder startByFragment = createStartMethodBuilder(FRAGMENT_NAME, paramsName, paramsName + ".getActivity()");
        typeBuilder.addMethod(
                startByFragment.addStatement("target.startActivityForResult(intent,$L)", REQUEST_CODE_FIELD_NAME)
                        .addJavadoc("launcher a Activity by $L", FRAGMENT_NAME)
                        .addStatement("return this")
                        .build()
        );
        MethodSpec.Builder startByV4Fragment = createStartMethodBuilder(V4FRAGMENT_NAME, paramsName, paramsName + ".getActivity()");
        typeBuilder.addMethod(
                startByV4Fragment.addStatement("target.startActivityForResult(intent,$L)", REQUEST_CODE_FIELD_NAME)
                        .addJavadoc("launcher a Activity by $L", V4FRAGMENT_NAME)
                        .addStatement("return this")
                        .build()
        );

    }

    private MethodSpec.Builder createStartMethodBuilder(String paramsType, String paramsName, String context) {
        TypeName intent = getTypeName(INTENT_NAME);
        return MethodSpec.methodBuilder(START_METHOD)
                .addModifiers(Modifier.PUBLIC)
                .returns(generateClassName)
                .addParameter(getTypeName(paramsType), paramsName)
                .addStatement("$T intent = $L($L)", intent, CREATE_INTENT, context);
    }

    private void addRequestCodeMethod(TypeSpec.Builder typeBuilder) {
        MethodSpec build = MethodSpec.methodBuilder(REQUEST_CODE_FIELD_NAME)
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("Set request code,use -1 if not defined")
                .returns(generateClassName)
                .addParameter(TypeName.INT, REQUEST_CODE_FIELD_NAME)
                .addStatement("this.$L = $L", REQUEST_CODE_FIELD_NAME, REQUEST_CODE_FIELD_NAME)
                .addStatement("return this")
                .build();
        typeBuilder.addMethod(build);
    }


    private void addFields(TypeSpec.Builder typeBuilder) {
        //
        String clzName = qtData.getClazzData().getSimpleName();
        clzName = SUFFIX + clzName;
        typeBuilder.addField(FieldSpec.builder(getTypeName(clzName), DEFAULT_INSTANCE, Modifier.PRIVATE, Modifier.STATIC).build());
        // add tag
        typeBuilder.addField(FieldSpec.builder(TypeName.get(String.class), TAG_FIELD, Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
                .addJavadoc("The tag to pass data")
                .initializer("$L.class.getCanonicalName()", qtData.getClazzData().getSimpleName())
                .build());
        if (!isEmptyParams) {
            // add RequestData filed
            typeBuilder.addField(FieldSpec.builder(getTypeName(REQUEST_DATA_CLASS), REQUEST_DATA_FIELD_NAME, Modifier.PRIVATE)
                    .addJavadoc("The instance of RequestData that is the container of whole filed")
                    .build());
        }
        // add request code field
        typeBuilder.addField(FieldSpec.builder(TypeName.INT, REQUEST_CODE_FIELD_NAME, Modifier.PRIVATE)
                .initializer("-1")
                .build());


    }

    /**
     * create generate class builder
     */
    private TypeSpec.Builder generateTypeBuilder() {
        String clzName = qtData.getClazzData().getSimpleName();
        clzName = SUFFIX + clzName;
        TypeSpec.Builder builder = TypeSpec.classBuilder(clzName);
        if (!isEmptyParams) {

        }
        return builder
                .addJavadoc("This class is generated by annotation @QtInject")
                .addSuperinterface(TypeVariableName.get(I_INJECT_PATH))
                .addModifiers(Modifier.PUBLIC);
    }
}
