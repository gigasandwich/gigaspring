package com.giga.spring.util.scan;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.giga.spring.annotation.http.DoGet;
import com.giga.spring.annotation.http.DoPost;
import com.giga.spring.annotation.http.RequestMapping;
import com.giga.spring.exception.InvalidAnnotationException;

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
    public Map<String, List<Method>> getAllUrlMappingPathValues(Class<?> clazz) throws SecurityException, InvalidAnnotationException{
        try {
            Map<String, List<Method>> result = new HashMap<>();

            Method[] methods = clazz.getDeclaredMethods();

            for (Method method : methods) {
                String path = null;
                int annotationCount = 0;

                if (method.isAnnotationPresent(RequestMapping.class)) {
                    RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                    path = requestMapping.path();
                    annotationCount++;
                    System.out.println("  has RequestMapping -> " + path);
                }
                if (method.isAnnotationPresent(DoPost.class)) {
                    DoPost doPost = method.getAnnotation(DoPost.class);
                    path = doPost.path();
                    annotationCount++;
                    System.out.println("  has DoPost -> " + path);
                }
                if (method.isAnnotationPresent(DoGet.class)) {
                    DoGet doGet = method.getAnnotation(DoGet.class);
                    path = doGet.path();
                    annotationCount++;
                    System.out.println("  has DoPost -> " + path);
                }

                System.out.println(annotationCount);

                if (annotationCount > 1) {
                    throw new InvalidAnnotationException("There should be only one of these annotation: DoGet/DoPost/RequestMapping in method: " + method.getName());
                }

                result.putIfAbsent(path, new ArrayList<>());
                result.get(path).add(method);
            }

            return result;
        } catch (SecurityException | InvalidAnnotationException e) {
            throw e;
        }
    }
}
