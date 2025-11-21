package com.giga.spring.util.http;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

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

    public Object invokeMethod(HttpServletRequest req) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<?> controllerConstructor = c.getDeclaredConstructor();
        Object controller = controllerConstructor.newInstance();

        Parameter[] parameters = m.getParameters();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter p = parameters[i];
            String paramValue = req.getParameter(p.getName());
            if (p.getType() == int.class) {
                args[i] = paramValue != null ? Integer.parseInt(paramValue) : 0;
            } else {
                args[i] = paramValue;
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