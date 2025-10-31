package com.giga.spring.util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.giga.spring.annotation.UrlMapping;

public class ReflectionUtils {
    private static ReflectionUtils instance;

    public static ReflectionUtils getInstance() {
        if (instance == null)
            instance = new ReflectionUtils();
        return instance;
    }

    /*
     * Gets all the "UrlMapping.path()" values
     * from the methods of clazz
     */
    public Map<String, Method> getAllUrlMappingPathValues(Class<?> clazz) throws SecurityException {
        try {
            Map<String, Method> result = new HashMap<>();

            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(UrlMapping.class)) {
                    UrlMapping annotation = method.getAnnotation(UrlMapping.class);
                    result.put(annotation.path(), method);
                }
            }

            return result;
        } catch (SecurityException se) {
            throw se;
        }
    }
}
