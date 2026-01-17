package com.giga.spring.util.http.parameter;

import com.giga.spring.annotation.controller.PathVariable;
import com.giga.spring.servlet.route.Route;
import com.giga.spring.util.reflect.Parser;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Parameter;
import java.util.Map;

public class PathVariableParameterResolver implements ParameterResolver {
    @Override
    public boolean supports(String paramName, Parameter parameter, HttpServletRequest req, Route route) {
        return parameter.isAnnotationPresent(PathVariable.class);
    }

    @Override
    public Object resolve(String paramName, Parameter parameter, HttpServletRequest req, Route route) {
        Parser parser = Parser.getInstance();
        String uri = Route.getLocalURIPath(req);
        Map<String, String> pathVars = route.getPathVariableValues(uri);
        return parser.stringToTargetType(pathVars.get(paramName), parameter.getType());
    }
}