package com.example.annotation;

/**the func to parse the activity or fragment
 * Created by liutao on 05/07/2017.
 */

public interface IInject {

    /**
     * to parse some obj,that
     * @param o
     * @return
     */
    boolean toInject(Object o);

    Class getKey();
}
