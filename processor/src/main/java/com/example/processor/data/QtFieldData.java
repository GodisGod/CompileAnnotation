package com.example.processor.data;

/**
 * author: liutao
 * date: 2016/6/17.
 */
public class QtFieldData {
    private String name;
    private String doc;
    private String fieldType;
    private String alias;
    private boolean isOpenDefault;
    private String bundle;
    public void setDoc(String doc) {
        this.doc = doc;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getFieldType() {
        return fieldType;
    }

    public String getDoc() {
        return doc;
    }

    public String getName() {
        return name;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        if(alias==null||alias.isEmpty()){
            return name;
        }
        return alias;
    }

    public void setBundle(String bundle) {
        this.bundle = bundle;
    }

    public String getBundle() {
        return bundle;
    }

    public void setOpenDefault(boolean openDefault) {
        isOpenDefault = openDefault;
    }

    public boolean isOpenDefault() {
        return isOpenDefault;
    }
}
