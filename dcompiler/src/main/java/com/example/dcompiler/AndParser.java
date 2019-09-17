package com.example.dcompiler;

import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.os.Parcelable;
import android.text.TextUtils;

import java.io.Serializable;

/**
 * Created by liutao on 05/07/2017.
 */

public class AndParser {
    public static <T> T parse(String fieldType, Intent i, String key, String bundleName) {
        if (TextUtils.isEmpty(bundleName)) {
            return parse(fieldType, i, key);
        } else {
            return parse(fieldType, i.getBundleExtra(bundleName), key);
        }
    }

    public static <T> T parse(String fieldType, Intent i, String key) {

        return parse(fieldType,i.getExtras(),key);

    }

    public static <T> T parse(String fieldType, Bundle i, String key) {
        try {
            switch (fieldType) {
                case "java.lang.String":
                    return (T) i.getString(key);
                case "char":
                case "java.lang.Character":
                    return (T) new Character(i.getChar(key, '0'));
                case "int":
                case "java.lang.Integer":
                    return (T) new Integer(i.getInt(key, 0));
                case "long":
                case "java.lang.Long":
                    return (T) new Long(i.getLong(key, 0L));
                case "float":
                case "java.lang.Float":
                    return (T) new Float(i.getFloat(key, 0f));
                case "double":
                case "java.lang.Double":
                    return (T) new Double(i.getDouble(key, 0d));
                case "byte":
                case "java.lang.Byte":
                    return (T) new Byte(i.getByte(key, (byte) 0));
                case "short":
                case "java.lang.Short":
                    return (T) new Short(i.getShort(key, (short) 0));
                default:
                    Serializable serializable = null;
                    Parcelable parcelableExtra = null;
                    T t = null;
                    try {
                        serializable = i.getSerializable(key);
                        if (serializable != null) {
                            t = (T) serializable;
                        }
                        if(t!=null){
                            return t;
                        }

                    } catch (Exception e) {
                        if (Debug.isDebuggerConnected()) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        parcelableExtra = i.getParcelable(key);
                        if (parcelableExtra != null) {
                            t= (T) parcelableExtra;
                        }
                        if(t!=null){
                            return t;
                        }
                    } catch (Exception e) {
                        if (Debug.isDebuggerConnected()) {
                            e.printStackTrace();
                        }
                    }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
