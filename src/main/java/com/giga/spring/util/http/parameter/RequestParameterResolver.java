package com.giga.spring.util.http.parameter;

import com.giga.spring.servlet.route.Route;
import com.giga.spring.util.reflect.Parser;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Parameter;

public class RequestParameterResolver implements ParameterResolver {
    @Override
    public boolean supports(String paramName, Parameter parameter, HttpServletRequest req, Route route) {
        return req.getParameter(paramName) != null;
    }

    @Override
    public Object resolve(String paramName, Parameter parameter, HttpServletRequest req, Route route) {
        Parser parser = Parser.getInstance();
        String value = req.getParameter(paramName);
        return parser.stringToTargetType(value, parameter.getType());
    }
}