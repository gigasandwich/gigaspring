package com.giga.spring.util.http;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.giga.spring.annotation.controller.PathVariable;
import com.giga.spring.annotation.controller.RequestParameter;
import com.giga.spring.annotation.http.DoGet;
import com.giga.spring.annotation.http.DoPost;
import com.giga.spring.annotation.http.RequestMapping;
import com.giga.spring.exception.BindingException;
import com.giga.spring.servlet.route.Route;
import com.giga.spring.util.http.constant.HttpMethod;
import com.giga.spring.util.reflect.ModelParser;
import com.giga.spring.util.reflect.Parser;
import com.giga.spring.util.reflect.ReflectionUtil;

import jakarta.servlet.http.HttpServletRequest;

public class ClassMethod {
    private Class <?> c;
    private Method m;
    private HttpMethod httpMethod;
    private boolean isOutputToJson;

    public ClassMethod(Class<?> c, Method m, boolean isOutputToJson) {
        this.c = c;
        this.m = m;
        this.httpMethod = getHttpMethodOnInit();
        this.isOutputToJson = isOutputToJson;
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
            try {
                Object paramValue = getParameterValue(paramName, parameter, req, route); // Parsing is already done here,
                args[i] = paramValue;
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage() + " (" + paramName + ")");
            }
        }

        return m.invoke(controller, args);
    }

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
        Parser parser = Parser.getInstance();

        // 1. request.getParameter
        String value = req.getParameter(paramName);
        if (value != null) {
            return parser.stringToTargetType(value, parameter.getType());
        }

        // 2. PathVariable
        PathVariable pv = parameter.getAnnotation(PathVariable.class);
        if (pv != null) {
            String uri = Route.getLocalURIPath(req);
            Map<String, String> pathVars = route.getPathVariableValues(uri);
            return parser.stringToTargetType(pathVars.get(paramName), parameter.getType());
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

        // 4. Raw Object
        try {
            if (!parameter.getType().isPrimitive() && !parameter.getType().equals(String.class)) {
                List<String> objectToStringPatterns = ModelParser.getInstance().getObjectToStringPatterns(req, parameter);
                Object model = ReflectionUtil.getInstance().newInstanceFromNoArgsConstructor(parameter.getType());

                for (String objectToStringPattern : objectToStringPatterns) {
                    // Still using req.getParameterMap() for generalization
                    Map<String, String[]> parameterMap = req.getParameterMap();
                    String[] values = parameterMap.get(objectToStringPattern);

                    /**
                     * If the request provided multiple values for the same (non-indexed) field
                     * ex: checkbox group
                     */
                    boolean hasIndex = objectToStringPattern.matches(".*\\[\\d+\\].*");
                    if (values != null && values.length > 1 && !hasIndex) {
                        for (int i = 0; i < values.length; i++) {
                            String indexedPattern = objectToStringPattern + "[" + i + "]";
                            ModelParser.getInstance().bind(model, indexedPattern.split("\\."), 1, values[i]);
                        }
                    } else {
                        String val = (values != null && values.length > 0) ? values[0] : null;
                        ModelParser.getInstance().bind(model, objectToStringPattern.split("\\."), 1, val);
                    }
                }

                return model;
            }
        } catch (Exception e) {
            throw new BindingException(e.getMessage());
        }

        return null;
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

    public boolean isOutputToJson() {
        return isOutputToJson;
    }
}