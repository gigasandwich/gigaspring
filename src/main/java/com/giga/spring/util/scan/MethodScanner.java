package com.giga.spring.util.scan;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.giga.spring.annotation.http.DoGet;
import com.giga.spring.annotation.http.DoPost;
import com.giga.spring.annotation.http.RequestMapping;

public class MethodScanner {
    private static MethodScanner instance;

    public static MethodScanner getInstance() {
        if (instance == null)
            instance = new MethodScanner();
        return instance;
    }

    /*
     * Gets all the "RequestMapping.path()" values
     * from the methods of clazz
     */
    public Map<String, List<Method>> getAllUrlMappingPathValues(Class<?> clazz) throws SecurityException {
        try {
            Map<String, List<Method>> result = new HashMap<>();

            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                String path = null;

                if (method.isAnnotationPresent(RequestMapping.class)) {
                    RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                    path = requestMapping.path();
                } else if (method.isAnnotationPresent(DoPost.class)) {
                    DoPost doPost = method.getAnnotation(DoPost.class);
                    path = doPost.path();
                } else if (method.isAnnotationPresent(DoGet.class)) {
                    DoGet doGet = method.getAnnotation(DoGet.class);
                    path = doGet.path();
                }
                result.putIfAbsent(path, new ArrayList<>());
                result.get(path).add(method);
            }

            return result;
        } catch (SecurityException se) {
            throw se;
        }
    }
}
