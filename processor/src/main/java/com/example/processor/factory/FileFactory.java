package com.example.processor.factory;

import com.example.processor.UtilMgr;
import com.example.processor.data.QtData;
import com.example.processor.data.QtFieldData;
import com.example.processor.reflect.Reflect;
import com.example.processor.util.StringUtils;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.lang.model.element.Modifier;

import static com.squareup.javapoet.MethodSpec.methodBuilder;

/**
 * @author Administrator
 */
public abstract class FileFactory {
     final static String PARSER = "parser";
    final static String INJECT = "inject";
    final static String DEFAULT_INSTANCE = "instance";
    final static String REQUEST_DATA_CLASS = "ArgsData";
    final static String REQUEST_DATA_FIELD_NAME = "data";
    final static String PARENT_CLASS_FIELD_NAME = "parent";
    final static String TAG_FIELD = "TAG";
    final static String INSTANCE_METHOD = "getInstance";
    private static final String GET_ARGSDATA_METHOD_NAME = "getArgsData";
    final static String I_INJECT = "toInject";
    final static String I_KEY = "getKey";
    final static String I_INJECT_PATH= "com.example.annotation.IInject";

    final static String ANDJUMP_PATH="com.example.dcompiler.AndJump";

    final static String AND_PARSET_PATH="com.example.dcompiler.AndParser";

    public static final String BOOLEAN = "boolean";

    boolean isEmptyParams = true;
    boolean isAbstract = true;
    String pageName;
    QtData qtData;
    TypeName generateClassName = null;

    public FileFactory(QtData qtData) {
        this.qtData = qtData;
        this.isEmptyParams = qtData.getList().size() == 0;
        this.isAbstract = qtData.getClazzData().isAbstract();
        pageName = qtData.getClazzData().getPageName();
        generateClassName = getTypeName(getSuffix() + qtData.getClazzData().getSimpleName());
    }

    /**
     * create inner class RequestData,contains all of the field define by annotation @Field
     */
    TypeSpec generateRequestData() {
        TypeSpec.Builder builder = TypeSpec.classBuilder(REQUEST_DATA_CLASS)
                .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                .addJavadoc("inner class RequestData,contains all of the field define by annotation @Field")
                .addSuperinterface(TypeName.get(Serializable.class));
        List<QtFieldData> fieldDatas = qtData.getList();
        for (int i = 0; i < fieldDatas.size(); i++) {
            QtFieldData data = fieldDatas.get(i);
            builder.addField(createField(data));
            builder.addField(createIsField(data));
            builder.addMethod(createGetRequestBuilder(data));
            builder.addMethod(createSetRequestBuilder(data));
            builder.addMethod(createSetIsRequestBuilder(data));
            builder.addMethod(createGetIsRequestBuilder(data));
        }

        return builder.build();
    }

    private MethodSpec createGetRequestBuilder(QtFieldData data) {
        String getMethodName = StringUtils.getGetMethodName(data.getName());
        return MethodSpec.methodBuilder(getMethodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(getTypeName(data.getFieldType()))
                .addStatement("return this.$L", data.getName())
                .addJavadoc(data.getDoc())
                .build();
    }

//    private MethodSpec createSetRequestBuilder(QtFieldData data) {
//        String setMethodName = StringUtils.getSetMethodName(data.getName());
//        return MethodSpec.methodBuilder(setMethodName)
//                .addModifiers(Modifier.PUBLIC)
//                .addParameter(getTypeName(data.getFieldType()), data.getName())
//                .returns(getTypeName(REQUEST_DATA_CLASS))
//                .addStatement("this.$L = $L", data.getName(), data.getName())
//                .addStatement("return this")
//                .addJavadoc(data.getDoc())
//                .build();
//    }

    private FieldSpec createField(QtFieldData data) {
        FieldSpec.Builder builder = FieldSpec.builder(getTypeName(data.getFieldType()), data.getName(), Modifier.PRIVATE)
                .addJavadoc(data.getDoc()+data.getFieldType());

        return builder.build();
    }


    void build(TypeSpec.Builder typeBuilder) throws IOException {
        JavaFile.Builder javaBuilder = JavaFile.builder(pageName, typeBuilder.build());
        javaBuilder.addFileComment("The file is auto-generate by processorTool,do not modify!");
        javaBuilder.build().writeTo(UtilMgr.getMgr().getFiler());
    }
    MethodSpec createGetArgsDataMethod() {
        MethodSpec.Builder builder = methodBuilder(GET_ARGSDATA_METHOD_NAME)
                .addModifiers(Modifier.PUBLIC)
                .returns(getTypeName(REQUEST_DATA_CLASS))
                .addJavadoc("get args you has already set")
                .addStatement("return $L", REQUEST_DATA_FIELD_NAME);
        return builder.build();
    }

    /***create is **/
    private FieldSpec createIsField(QtFieldData data) {
        String isFieldName = StringUtils.getIsMethodName(data.getName());
        FieldSpec.Builder builder = FieldSpec.builder(getTypeName(BOOLEAN),isFieldName, Modifier.PRIVATE)
                .addJavadoc(data.getDoc());
        return builder.build();
    }
    private MethodSpec createSetIsRequestBuilder(QtFieldData data) {
        String isMethodName = StringUtils.getIsMethodName(data.getName());
        return methodBuilder(isMethodName)
                .addModifiers(Modifier.PRIVATE)
                .addParameter(getTypeName(BOOLEAN), isMethodName)
                .returns(getTypeName(REQUEST_DATA_CLASS))
                .addStatement("this.$L = $L", isMethodName, isMethodName)
                .addStatement("return this")
                .addJavadoc(data.getDoc())
                .build();
    }

    private MethodSpec createSetRequestBuilder(QtFieldData data) {
        String setMethodName = StringUtils.getSetMethodName(data.getName());
        return methodBuilder(setMethodName)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(getTypeName(data.getFieldType()), data.getName())
                .returns(getTypeName(REQUEST_DATA_CLASS))
                .addStatement("if(this.$L == $L ) return this",data.getName(),data.getName())
                .addStatement("$L(true)",StringUtils.getIsMethodName(data.getName()))
                .addStatement("this.$L = $L", data.getName(),  data.getName())
                .addStatement("return this")
                .addJavadoc(data.getDoc())
                .build();
    }
    private MethodSpec createGetIsRequestBuilder(QtFieldData data) {
        String isMethodName = StringUtils.getIsMethodName(data.getName());

        return methodBuilder(isMethodName)
                .addModifiers(Modifier.PRIVATE)
                .returns(getTypeName(BOOLEAN))
                .addStatement("return this.$L", isMethodName)
                .addJavadoc(data.getDoc())
                .build();
    }
    /***end  create is ****/


    /**
     * generate static create method
     */
    MethodSpec createMethod() {
        MethodSpec.Builder createBuilder = MethodSpec.methodBuilder(INSTANCE_METHOD)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .beginControlFlow("if(instance==null)")
                .addStatement("instance = new $T()", generateClassName)
                .endControlFlow();
        if (!isEmptyParams) {
            createBuilder.addStatement("instance.$L = new $L()", REQUEST_DATA_FIELD_NAME, REQUEST_DATA_CLASS);
        }
        return createBuilder.addStatement("return instance")
                .returns(generateClassName)
                .build();
    }

    void addParamsSetMethod(TypeSpec.Builder typeBuilder) {
        List<QtFieldData> fieldList = qtData.getList();
        for (int i = 0; i < fieldList.size(); i++) {
            QtFieldData data = fieldList.get(i);
            createSetMethod(data, typeBuilder);
        }

    }



    void createSetMethod(QtFieldData data, TypeSpec.Builder typeBuilder) {
        String setMethodName = StringUtils.getSetMethodName(data.getName());
        TypeName realType = getTypeName(data.getFieldType());
        MethodSpec setMethod = MethodSpec.methodBuilder(setMethodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(generateClassName)
                .addParameter(realType, data.getName())
                .addStatement("this.$L.$L($L)", REQUEST_DATA_FIELD_NAME, setMethodName, data.getName())
                .addStatement("return this")
                .addJavadoc(data.getDoc())
                .build();
        typeBuilder.addMethod(setMethod);
    }

    TypeName getTypeName(String clzName) {
        return Reflect.on(TypeName.class).create(clzName).get();
    }
    public MethodSpec impIInject(TypeSpec.Builder  typeBuilder,boolean isEmptyParams) {
        TypeName intent = getTypeName(qtData.getClazzData().getSimpleName());
        MethodSpec.Builder builder = MethodSpec.methodBuilder(I_INJECT)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.BOOLEAN)
                .addAnnotation(Override.class)
                .addParameter(TypeName.OBJECT, "o");
        if(!isEmptyParams){
            builder.addCode("if(o==null)\n")
                    .addStatement("return false")
                    .addCode("if(!(o instanceof $L)){\nreturn false;\n}",intent)
                    .addCode("\ninject(($L)o);\n",intent)
                    .addCode("return true;\n");
        }else{
            builder.addCode("return true;");
        }

        return builder.build();
    }
    public MethodSpec implKey(TypeSpec.Builder  typeBuilder){
        TypeName intent = getTypeName(qtData.getClazzData().getPageName()+"."+qtData.getClazzData().getSimpleName()+".class");
        TypeName string = getTypeName("Class");
        MethodSpec.Builder builder = MethodSpec.methodBuilder(I_KEY)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(string);
        builder.addStatement("return $L",intent.toString());
        return builder.build();
    }

    /**
     * create private constructor method
     */
    MethodSpec createPrivateConstructor() {
        MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE);
        return builder.build();
    }

    protected abstract String getSuffix();

    protected abstract void generateCode() throws IOException;
}
