package com.giga.spring.servlet.route;

import com.giga.spring.util.http.ClassMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Router {
    private List<Route> routes;

    public Router(Map<String, List<ClassMethod>> map) {
        routes = new ArrayList<>();

        for (Map.Entry<String, List<ClassMethod>> e : map.entrySet()) {
            Route route = new Route(e.getKey(), e.getValue());
            routes.add(route);
        }
    }

    public Route getRoute(String uri) throws IllegalArgumentException {
        uri = Route.normalizePath(uri);

        for(Route route : routes) {
            String pathRegex = route.pathToRegex();
            Pattern pattern = Pattern.compile(pathRegex);
            boolean uriMatchesPattern = pattern.matcher(uri).matches();
            if (uriMatchesPattern) {
                return route;
            }
        }

        return null;
    }
}
