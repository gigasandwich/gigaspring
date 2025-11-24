package com.giga.spring.util.http;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

import com.giga.spring.annotation.controller.PathVariable;
import com.giga.spring.annotation.controller.RequestParameter;
import com.giga.spring.servlet.route.Route;

import jakarta.servlet.http.HttpServletRequest;

public class ClassMethod {
    private Class <?> c;
    private Method m;

    public ClassMethod(Class<?> c, Method m) {
        this.c = c;
        this.m = m;
        m.setAccessible(true); // Never forget this 🗿
    }

    public Object invokeMethod() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<?> controllerConstructor = c.getDeclaredConstructor();
        Object controller = controllerConstructor.newInstance();
        return m.invoke(controller);
    }

    public Object invokeMethod(Route route, HttpServletRequest req) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<?> controllerConstructor = c.getDeclaredConstructor();
        Object controller = controllerConstructor.newInstance();

        // Filling the args of the method
        Parameter[] parameters = m.getParameters();
        Object[] args = new Object[parameters.length];
        
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];

            /**
             * If there's an annotation RequestParameter(value=name):
             * match its value with the request.getParameter(name)
             */
            RequestParameter requestParameterannotation = parameter.getAnnotation(RequestParameter.class);
            String paramName = (requestParameterannotation != null) ?
                    requestParameterannotation.value()
                    : parameter.getName();
            String paramValue = req.getParameter(paramName); // Can be null

            if (parameter.getType() == int.class && paramValue != null) {
                args[i] = Integer.parseInt(paramValue);
                continue;
            } else {
                args[i] = paramValue;
            }

            // PathVariable handler
            if (args[i] == null) {
                PathVariable pathVariableAnnotation =  parameter.getAnnotation(PathVariable.class);
                if (pathVariableAnnotation != null) {
                    String uri = Route.getLocalURIPath(req);
                    Map<String, String> pathVariableValues = route.getPathVariableValues(uri);
                    String pathVariableName = pathVariableAnnotation.value();

                    args[i] = (Object) pathVariableValues.get(pathVariableName);
                }
            }
            
            if (args[i] != null && parameter.getType() == int.class) {
                args[i] = Integer.parseInt(args[i].toString());
            }
            
            if (args[i] == null) {
                throw new IllegalArgumentException("Required parameter '" + paramName + "' is missing");                    
            }
        }

        return m.invoke(controller, args);
    }

    public Class<?> getC() {
        return c;
    }
    public void setC(Class<?> c) {
        this.c = c;
    }
    public Method getM() {
        return m;
    }
    public void setM(Method m) {
        this.m = m;
    }
}