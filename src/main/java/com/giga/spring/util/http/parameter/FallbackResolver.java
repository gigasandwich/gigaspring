package com.giga.spring.util.http.parameter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Parameter;

import com.giga.spring.servlet.route.Route;

public class FallbackResolver implements ParameterResolver {
    @Override
    public boolean supports(String paramName, Parameter parameter, HttpServletRequest req, Route route) {
        return true;
    }

    @Override
    public Object resolve(String paramName, Parameter parameter, HttpServletRequest req, Route route) {
        return null;
    }
}