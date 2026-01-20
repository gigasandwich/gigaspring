package com.giga.spring.util.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.giga.spring.annotation.controller.PathVariable;
import com.giga.spring.annotation.controller.RequestParameter;
import com.giga.spring.annotation.controller.security.Authorized;
import com.giga.spring.annotation.controller.security.Role;
import com.giga.spring.annotation.http.DoGet;
import com.giga.spring.annotation.http.DoPost;
import com.giga.spring.annotation.http.RequestMapping;
import com.giga.spring.servlet.route.Route;
import com.giga.spring.util.http.constant.HttpMethod;
import com.giga.spring.util.http.parameter.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

public class ClassMethod {
    private Class <?> c;
    private Method m;
    private HttpMethod httpMethod;
    private boolean isOutputToJson;

    private List<ParameterResolver> resolvers = Arrays.asList(
        new RequestParameterResolver(),
        new PathVariableParameterResolver(),
        new MapParameterResolver(),
        new ObjectParameterResolver(),
        new FallbackResolver()
    );

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

    private Object getParameterValue(String paramName, Parameter parameter, HttpServletRequest req, Route route) throws ServletException, IOException {
        for (ParameterResolver resolver : resolvers) {
            if (resolver.supports(paramName, parameter, req, route)) {
                return resolver.resolve(paramName, parameter, req, route);
            }
        }
        return null;
    }

    public boolean isAccessibleBy(String role) {
        if (!m.isAnnotationPresent(Authorized.class) && !m.isAnnotationPresent(Role.class)) {
            return true;
        }

        // If user just has a role (they are authenticated)
        if (m.isAnnotationPresent(Authorized.class)) {
            if (role !=null && !role.isEmpty()) {
                return true;
            } else {
                return false;
            }
        }
        
        // If the role matches
        if (m.isAnnotationPresent(Role.class)) {
            Role roleAnnotation = m.getAnnotation(Role.class);
            String roleOnAnnotation = roleAnnotation.value();
            
            if (role.equals(roleOnAnnotation)) {
                return true;
            }
        }

        return false;
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