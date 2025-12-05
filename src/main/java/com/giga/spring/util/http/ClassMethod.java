package com.giga.spring.util.http;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

import com.giga.spring.annotation.controller.PathVariable;
import com.giga.spring.annotation.controller.RequestParameter;
import com.giga.spring.annotation.http.DoGet;
import com.giga.spring.annotation.http.DoPost;
import com.giga.spring.annotation.http.RequestMapping;
import com.giga.spring.servlet.route.Route;

import com.giga.spring.util.http.constant.HttpMethod;
import jakarta.servlet.http.HttpServletRequest;

public class ClassMethod {
    private Class <?> c;
    private Method m;
    private HttpMethod httpMethod;

    public ClassMethod(Class<?> c, Method m) {
        this.c = c;
        this.m = m;
        this.httpMethod = getHttpMethodOnInit();
        m.setAccessible(true); // Never forget this 🗿
    }

    private HttpMethod getHttpMethodOnInit() {
        if (m.isAnnotationPresent(DoGet.class)) {
            return HttpMethod.GET;
        } else if (m.isAnnotationPresent(DoPost.class)) {
            return HttpMethod.POST;
        } else if (m.isAnnotationPresent(RequestMapping.class))  {
            return HttpMethod.ALL;
        } else {
            throw new EnumConstantNotPresentException(HttpMethod.GET.getDeclaringClass(), "HttpMethod");
        }
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
            
            String paramName = getParameterName(parameter);
            Object paramValue = getParameterValue(paramName, parameter, req, route);

            args[i] = convertValue(paramValue, parameter.getType(), paramName);
        }

        return m.invoke(controller, args);
    }

    /**
     * It has been created because the real value of the parameter might not be from the variable name
     * but sometimes from the annotation
     * eg:
     * - `void method (@RequestParam("usedVariableName") String unusedVariableName) { ... }`
     * - `void method (@PathVariable("usedVariableName") String unusedVariableName) { ... }`
     * - `void method (String usedVariableName) { ... }`
     */
    private String getParameterName(Parameter parameter) {
        RequestParameter rp = parameter.getAnnotation(RequestParameter.class);
        if (rp != null) {
            return rp.value();
        }
        
        PathVariable pv = parameter.getAnnotation(PathVariable.class);
        if (pv != null) {
            return pv.value();
        }

        return parameter.getName();
    }

    private Object getParameterValue(String paramName, Parameter parameter, HttpServletRequest req, Route route) {
        // 1. request.getParameter
        String value = req.getParameter(paramName);
        if (value != null) {
            return value;
        }

        // 2. PathVariable
        PathVariable pv = parameter.getAnnotation(PathVariable.class);
        if (pv != null) {
            String uri = Route.getLocalURIPath(req);
            Map<String, String> pathVars = route.getPathVariableValues(uri);
            return pathVars.get(paramName);
        }

        // 3. Map<String, Object>
        Type type = parameter.getParameterizedType();
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            if (parameterizedType.getRawType().equals(Map.class)) {
                Type[] typeArguments = parameterizedType.getActualTypeArguments();
                if (typeArguments.length == 2 && typeArguments[0].equals(String.class) && typeArguments[1].equals(Object.class) ){
                    Map<String, Object> paramMapObject = new HashMap<>();
                    Map<String, String[]> parameterMap = req.getParameterMap();
                    for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                        String key = entry.getKey();
                        String[] values = entry.getValue();

                        if (values.length == 1) {
                            paramMapObject.put(key, values[0]);
                        } else {
                            paramMapObject.put(key, values);
                        }
                    }
                    return paramMapObject;
                }
            }
        }

        return null;
    }

    private Object convertValue(Object value, Class<?> targetType, String paramName) {
        if (value == null) {
            if (targetType.isPrimitive()) {
                throw new IllegalArgumentException("Required primitive parameter '" + paramName + "' is missing");
            } else { // Objects
                return null;
            }
        }

        String strValue = value.toString();
        try {
            if (targetType == int.class || targetType == Integer.class) {
                return Integer.parseInt(strValue);
            } else if (targetType == long.class || targetType == Long.class) {
                return Long.parseLong(strValue);
            } else if (targetType == boolean.class || targetType == Boolean.class) {
                return Boolean.parseBoolean(strValue);
            } else if (targetType == double.class || targetType == Double.class) {
                return Double.parseDouble(strValue);
            } else if (targetType == String.class) {
                return strValue;
            } else {
                // throw new IllegalArgumentException("Unsupported parameter type: " + targetType.getSimpleName());
                return value;
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid value for parameter '" + paramName + "': " + strValue, e);
        }
    }

    /****************************
     * Getters and setters
     ****************************/

    public HttpMethod getHttpMethod() {
        return httpMethod;
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

    public String toString() {
        return c.getName() + " " + m.toString();
    }
}