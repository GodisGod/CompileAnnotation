package com.example.compileannotation;

import android.content.Context;
import android.content.Intent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by hongda on 2019-09-10.
 */
public class QJumpSencondActivity {


        private Context ctx;
        private Class<?> cls;
        private HashMap<String, String> map = new HashMap<>();

        public QJumpSencondActivity addParamater(String key, String value) {
            map.put(key, value);
            return this;
        }

        public QJumpSencondActivity setFromTo(Context context, Class<?> cls) {
            this.ctx = context;
            this.cls = cls;
            return this;
        }

        public void start() {
            Intent intent = new Intent();
            Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                String key = entry.getKey();
                String object = entry.getValue();
                intent.putExtra(key, object);
            }
            intent.setClass(ctx, cls);
            ctx.startActivity(intent);
        }



}
