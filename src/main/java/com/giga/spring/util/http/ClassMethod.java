package com.giga.spring.util.http;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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