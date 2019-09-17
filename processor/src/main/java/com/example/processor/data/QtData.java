package com.example.processor.data;

import com.example.processor.EleParser;

import java.util.List;

/**
 * author: liutao
 * date: 2016/6/17.
 */
public class QtData {
    private List<QtFieldData> list;
    private String clzName;
    private EleParser.ElementType elementType;
    private ClazzData clazzData;
    public void setList(List<QtFieldData> list) {
        this.list = list;
    }

    public void setClzName(String clzName) {
        this.clzName = clzName;
    }

    public void setElementType(EleParser.ElementType elementType) {
        this.elementType = elementType;
    }

    public void setClazzData(ClazzData clazzData) {
        this.clazzData = clazzData;
    }

    public ClazzData getClazzData() {
        return clazzData;
    }

    public EleParser.ElementType getElementType() {
        return elementType;
    }

    public List<QtFieldData> getList() {
        return list;
    }

    public String getClzName() {
        return clzName;
    }



}
