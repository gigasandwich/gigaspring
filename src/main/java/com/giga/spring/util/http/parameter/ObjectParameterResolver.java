package com.giga.spring.util.http.parameter;

import com.giga.spring.annotation.controller.PathVariable;
import com.giga.spring.exception.BindingException;
import com.giga.spring.servlet.route.Route;
import com.giga.spring.util.reflect.ModelParser;
import com.giga.spring.util.reflect.Parser;
import com.giga.spring.util.reflect.ReflectionUtil;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

public class ObjectParameterResolver implements ParameterResolver {
    @Override
    public boolean supports(String paramName, Parameter parameter, HttpServletRequest req, Route route) {
        return !parameter.getType().isPrimitive() && !parameter.getType().equals(String.class);
    }

    @Override
    public Object resolve(String paramName, Parameter parameter, HttpServletRequest req, Route route) {
        try {
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
        } catch (Exception e) {
            throw new BindingException(e.getMessage());
        }
    }
}