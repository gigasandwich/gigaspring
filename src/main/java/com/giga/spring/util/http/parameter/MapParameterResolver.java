package com.giga.spring.util.http.parameter;

import com.giga.spring.annotation.controller.GigaSession;
import com.giga.spring.annotation.controller.PathVariable;
import com.giga.spring.servlet.route.Route;
import com.giga.spring.util.SessionMap;
import com.giga.spring.util.file.GigaFile;
import com.giga.spring.util.reflect.Parser;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MapParameterResolver implements ParameterResolver {
    @Override
    public boolean supports(String paramName, Parameter parameter, HttpServletRequest req, Route route) {
        Type type = parameter.getParameterizedType();

        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            if (parameterizedType.getRawType().equals(Map.class)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Object resolve(String paramName, Parameter parameter, HttpServletRequest req, Route route) throws IOException, ServletException {
        Type type = parameter.getParameterizedType();
        ParameterizedType parameterizedType = (ParameterizedType) type;
        Type[] typeArguments = parameterizedType.getActualTypeArguments();
        Type valueType = typeArguments[1];
        String valueTypeName = valueType.getTypeName();

        if (typeArguments.length == 2 && typeArguments[0].equals(String.class)) {
            // value: byte[]
            if (valueTypeName.equals("byte[]") || valueTypeName.equals("java.util.List<byte[]>")) {
                Map<String, List<byte[]>> fileMap = req.getParts().stream()
                                                    .filter(p -> p.getSubmittedFileName() != null)
                                                    .collect(Collectors.groupingBy(Part::getName,
                                                                Collectors.mapping(p -> {
                                                                    try (InputStream in = p.getInputStream()) {
                                                                        return in.readAllBytes();
                                                                    } catch (IOException e) {
                                                                        throw new UncheckedIOException(e);
                                                                    }
                                                                }, Collectors.toList())
                                                            )
                                                    );
                boolean wantsSingleFile = valueTypeName.equals("byte[]");
                if (wantsSingleFile) {
                    return fileMap.entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().isEmpty() ? null : e.getValue().get(0)));
                }
                return fileMap;
            } else if (valueType.equals(GigaFile.class) || valueTypeName.equals("java.util.List<com.giga.spring.util.file.GigaFile>")) {
                Map<String, List<GigaFile>> fileMap = req.getParts().stream()
                                                    .filter(p -> p.getSubmittedFileName() != null)
                                                    .collect(Collectors.groupingBy(Part::getName,
                                                                Collectors.mapping(p -> {
                                                                    try {
                                                                        return new GigaFile(p);
                                                                    } catch (IOException e) {
                                                                        throw new UncheckedIOException(e);
                                                                    }
                                                                }, Collectors.toList())
                                                            )
                                                    );
                boolean wantsSingleFile = valueTypeName.equals("com.giga.spring.util.file.GigaFile");
                if (wantsSingleFile) {
                    return fileMap.entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().isEmpty() ? null : e.getValue().get(0)));
                }
                return fileMap;

            } else { // value: Object
                if (parameter.isAnnotationPresent(GigaSession.class)) {
                    HttpSession session = req.getSession(true);
                    Map<String, Object> debug = new HashMap<>();

                    Enumeration<String> names = session.getAttributeNames();
                    while (names.hasMoreElements()) {
                        String name = names.nextElement();
                        debug.put(name, session.getAttribute(name));
                    }

                    System.out.println("SESSION CONTENT: " + debug);

                    return new SessionMap(session);
                }

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
        
        return null;
    }
}