package com.giga.spring.util.reflect;

import java.lang.reflect.Constructor;

public class ReflectionUtil {
    private static ReflectionUtil instance;

    public static ReflectionUtil getInstance() {
        if (instance == null) {
            return new ReflectionUtil();
        }
        return instance;
    }

    public Object newInstanceFromNoArgsConstructor(Class<?> c)
            throws Exception {
        Constructor<?> constructor = c.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }
}
