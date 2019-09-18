package com.example.processor.factory;


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
public class FragmentFactory extends FileFactory {
    static final String SUFFIX = "Qt";
    static final String BUILD_METHOD = "build";
    static final String GET_DATA_METHOD = "getArguments";
    static final String CREATE_BUNDLE_NAME = "createBundle";

    public static final String BUNDLE_NAME = "android.os.Bundle";

    public FragmentFactory(QtData data) {
        super(data);
    }

    @Override
   public String getSuffix() {
        return SUFFIX;
    }

    @Override
    public void generateCode() throws IOException {
        TypeSpec.Builder typeBuilder = generateTypeBuilder();
        //implement IInject method
        typeBuilder.addMethod(impIInject(typeBuilder,isEmptyParams));
        typeBuilder.addMethod(implKey(typeBuilder));
        if (!isEmptyParams) {
            // create inner data class method
            typeBuilder.addType(generateRequestData());
            // create get data method
            typeBuilder.addMethod(createGetDataMethod());
            // add get ArgsData
            typeBuilder.addMethod(createGetArgsDataMethod());
            //add inject
            typeBuilder.addMethod(createInject());
            typeBuilder.addMethod(createParser());
        }
        // create filed
        createFields(typeBuilder);
        // create private constructor method
        typeBuilder.addMethod(createPrivateConstructor());
        // add create bundle method
        typeBuilder.addMethod(createBundle());
        // add create method
        typeBuilder.addMethod(createMethod());
        // create set params method
        addParamsSetMethod(typeBuilder);
        if (!isAbstract) {
            // create build method
            typeBuilder.addMethod(buildMethod());
        }
        build(typeBuilder);
    }

    private MethodSpec createInject() {
        TypeName intent = getTypeName(qtData.getClazzData().getSimpleName());
        MethodSpec.Builder builder = MethodSpec.methodBuilder(INJECT)
                .addModifiers(Modifier.PUBLIC,Modifier.STATIC)
                .addParameter(intent, "fragment")
                .beginControlFlow("if(fragment==null)")
                .addStatement("return")
                .endControlFlow();
        builder.addCode("ArgsData data=getArguments(fragment);\n");
        for (QtFieldData data : qtData.getList()) {
            builder.addStatement("if(data.$L&&$L){", StringUtils.getIsMethodName(data.getName()),data.isOpenDefault());
            builder.addStatement("\tfragment." + data.getName() + "=" + "data."+ StringUtils.getGetMethodName(data.getName())+ "()");
            builder.addStatement("}");
        }
        return builder.build();
    }
    private MethodSpec createParser() {

        TypeName intent = getTypeName(BUNDLE_NAME);
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

        for (QtFieldData fieldData : qtData.getList()) {
            builder.addCode("try { \n");
            builder.addCode("\targ." +StringUtils.getSetMethodName(fieldData.getName() )+ "(($L)$L.parse($S,data,$S));\n", fieldData.getFieldType(),AND_PARSET_PATH, fieldData.getFieldType(), fieldData.getAlias());
            builder.addCode("}catch(java.lang.Exception e){\n " +
                    "if ($T.isDebug()){\n\t e.printStackTrace();\n}\n}\n",andjumpData);
        }
        return builder.addStatement("return arg").build();

    }
    private MethodSpec createBundle() {
        TypeName bundle = getTypeName(BUNDLE_NAME);
        MethodSpec.Builder builder = MethodSpec.methodBuilder(CREATE_BUNDLE_NAME)
                .addModifiers(Modifier.PUBLIC)
                .returns(bundle)
                .addStatement("$T bundle = new $T()", bundle, bundle);

        if (!isEmptyParams) {
            builder.addStatement("bundle.putSerializable($L,$L)",TAG_FIELD,REQUEST_DATA_FIELD_NAME);
        }

        builder.addStatement("return bundle");

        return builder.build();
    }

    private MethodSpec createGetDataMethod() {
        String params = "target";
        TypeName typeName = getTypeName(qtData.getClzName());
        return MethodSpec.methodBuilder(GET_DATA_METHOD)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(getTypeName(REQUEST_DATA_CLASS))
                .addParameter(typeName, params)
                .addCode("if (target == null||target.getArguments()==null)\n")
                .addStatement("\treturn new $L()",REQUEST_DATA_CLASS)
                .addCode("else if (target.getArguments().getSerializable(TAG) == null)\n")
                .addCode("\treturn parser(target.getArguments());\n")
                .addCode("else\n")
                .addStatement("\treturn ($L)$L.getArguments().getSerializable($L)",REQUEST_DATA_CLASS,params,TAG_FIELD)
                .build();
    }

    private MethodSpec buildMethod() {
        TypeName bundle = getTypeName(BUNDLE_NAME);
        TypeName clz = getTypeName(qtData.getClzName());
        MethodSpec.Builder builder = MethodSpec.methodBuilder(BUILD_METHOD)
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc("build fragment instance of $L",clz)
                .returns(getTypeName(qtData.getClzName()))
                .addStatement("$T instance = new $T()",clz,clz)
                .addStatement("$T bundle = $L()",bundle,CREATE_BUNDLE_NAME)
                .addStatement("instance.setArguments(bundle)")
                .addStatement("return instance");

        return builder.build();
    }

    private void createFields(TypeSpec.Builder typeBuilder) {
        // add tag
        typeBuilder.addField(FieldSpec.builder(TypeName.get(String.class), TAG_FIELD, Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
                .initializer("$L.class.getCanonicalName()", qtData.getClzName()).build());
        if (!isEmptyParams) {
            // add RequestData filed
            typeBuilder.addField(FieldSpec.builder(getTypeName(REQUEST_DATA_CLASS), REQUEST_DATA_FIELD_NAME, Modifier.PUBLIC).build());
        }
        String clzName = qtData.getClazzData().getSimpleName();
        clzName = SUFFIX + clzName;
        typeBuilder.addField(FieldSpec.builder(getTypeName(clzName),DEFAULT_INSTANCE,Modifier.PRIVATE,Modifier.STATIC).build());
    }

    /**
     * create generate class builder
     */
    private TypeSpec.Builder generateTypeBuilder() {
        String clzName = qtData.getClazzData().getSimpleName();
        clzName = SUFFIX+clzName;
        return TypeSpec.classBuilder(clzName)
                .addSuperinterface(TypeVariableName.get(I_INJECT_PATH))
                .addModifiers(Modifier.PUBLIC);
    }
}
