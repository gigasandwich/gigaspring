package com.giga.spring.util.http.parameter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Parameter;

import com.giga.spring.servlet.route.Route;

public interface ParameterResolver {
    boolean supports(String paramName, Parameter parameter, HttpServletRequest req, Route route);
    Object resolve(String paramName, Parameter parameter, HttpServletRequest req, Route route) throws ServletException, IOException;
}