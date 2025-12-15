package com.giga.spring.servlet;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

import com.giga.spring.annotation.controller.Controller;
import com.giga.spring.annotation.controller.RestController;
import com.giga.spring.exception.InvalidConfigurationException;
import com.giga.spring.servlet.route.Router;
import com.giga.spring.util.http.ClassMethod;
import com.giga.spring.util.scan.ClassScanner;
import com.giga.spring.util.scan.MethodScanner;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class GigaServletContextListener implements ServletContextListener{
    private String basePackage;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext servletContext = event.getServletContext();

        try {
            basePackage = servletContext.getInitParameter("basePackage");
            if (basePackage == null || basePackage.trim().isEmpty()) {
                throw new InvalidConfigurationException("context-param 'basePackage' is required in web.xml");
            }
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
            throw e;
        }

        servletContext.setAttribute("router", new Router(getUrlMethodMap()));
    }

    /**
     * Gets all the url - List<CM> during init()
     */

    private Map<String, List<ClassMethod>> getUrlMethodMap() {
        Map<String, List<ClassMethod>> map = new HashMap<>();

        Set<Class<? extends Annotation>> annotations = Set.of(Controller.class, RestController.class);
        Set<Class<?>> classes = ClassScanner.getInstance().getClassesAnnotatedWith(annotations, basePackage);

        System.out.println("Valid backend URLs: ");
        for (Class<?> c : classes) {
            boolean isOutputToJson = false;
            if (c.isAnnotationPresent(RestController.class)) {
                isOutputToJson = true;
            }

            Map<String, List<Method>> urlMappingPathMap = MethodScanner.getInstance().getAllUrlMappingPathValues(c);

            for (String url : urlMappingPathMap.keySet()) {
                List<Method> methods = urlMappingPathMap.get(url);
                List<ClassMethod> classMethods = new ArrayList<>();

                for (Method m : methods) {
                    ClassMethod cm = new ClassMethod(c, m, isOutputToJson);
                    classMethods.add(cm);
                }

                map.put(url, classMethods);

                System.out.println("\t" + url);
            }
        }
        return map;
    }
}
