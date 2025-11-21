package com.giga.spring.servlet;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.giga.spring.annotation.ControllerAnnotation;
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
    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext servletContext = event.getServletContext();
        servletContext.setAttribute("router", new Router(getUrlMethodMap()));
    }

    /**
     * Gets all the url - CM during init()
     * and inserts it in a map
     */

    private Map<String, ClassMethod> getUrlMethodMap() {
        Map<String, ClassMethod> map = new HashMap<>();

        Set<Class<?>> classes = ClassScanner.getInstance().getClassesAnnotatedWith(ControllerAnnotation.class, "com.giga");

        System.out.println("Valid backend URLs: ");
        for (Class<?> c : classes) {
            Map<String, Method> urlMappingPathMap = MethodScanner.getInstance().getAllUrlMappingPathValues(c);

            for (String url : urlMappingPathMap.keySet()) {
                Method m = urlMappingPathMap.get(url);
                ClassMethod cm = new ClassMethod(c, m);
                map.put(url, cm);

                System.out.println("\t" + url);
            }
        }
        return map;
    }
}
