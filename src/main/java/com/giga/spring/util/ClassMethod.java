package com.giga.spring.util;

import java.lang.reflect.Method;

public class ClassMethod {
    private Class <?> c;
    private Method m;

    public ClassMethod(Class<?> c, Method m) {
        this.c = c;
        this.m = m;
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