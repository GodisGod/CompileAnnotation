package com.example.compileannotation;

import java.io.Serializable;

/**
 * Created by hongda on 2019-09-17.
 */
public class TestBean implements Serializable {

    private String nameTest;

    public TestBean(String nameTest) {
        this.nameTest = nameTest;
    }

    public String getNameTest() {
        return nameTest;
    }

    public void setNameTest(String nameTest) {
        this.nameTest = nameTest;
    }

}
