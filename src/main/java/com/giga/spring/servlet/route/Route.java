package com.giga.spring.servlet.route;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.giga.spring.util.http.ClassMethod;

import com.giga.spring.util.http.constant.HttpMethod;
import jakarta.servlet.http.HttpServletRequest;

public class Route {

    private String path;
    private List<ClassMethod> cms;

    public Route(String path, List<ClassMethod> cms) {
        String normalizedPath = path.endsWith("/") && path.length() > 1
                ? path.substring(0, path.length() - 1)
                : path;
        this.path = normalizedPath;
        this.cms = cms;
    }

    public String pathToRegex() throws IllegalArgumentException {
        long openingBracesCount = path.chars().filter(c -> c == '{').count();
        long closingBracesCount = path.chars().filter(c -> c == '}').count(); // Why am I obliged to use '' but not ""

        if (openingBracesCount != closingBracesCount) {
            throw new IllegalArgumentException("URI doesn't match standards: " + path);
        }

        boolean isStaticPath = openingBracesCount == 0;
        String pathToRegex;
        if (isStaticPath) {
            pathToRegex = "^" + path + "$";
        } else {
            // JAVA naming convention
            String regex = "\\{([A-Za-z_$][A-Za-z0-9_$]*)\\}";
            String replacement = "(?<$1>[^/]+)"; // Replace {var} with ([^/]+)
            pathToRegex = path.replaceAll(regex, replacement);
        }
        return pathToRegex;
    }

    public Map<String, String> getPathVariableValues(String url) {
        String regex = this.pathToRegex();

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);

        if (matcher.matches()) {
            Map<String, String> variables = new HashMap<>();
            Pattern p  = Pattern.compile("\\(\\?<([^>]+)>");
            Matcher m = p.matcher(regex);
            while (m.find()) {
                String key = m.group(1);
                variables.put(key, matcher.group(key));
            }
            return variables;
        } else {
            return Collections.emptyMap();
        }
    }

    public ClassMethod getClassMethodByRequest(HttpServletRequest req) throws Exception {
        String requestMethod = req.getMethod();
        HttpMethod incomingHttpMethod = getHttpMethod(requestMethod);

        for (ClassMethod cm : cms) {
            System.out.println(cm);

            if (cm.getHttpMethod().equals(HttpMethod.ALL)) {
                return cm;
            }

            if (cm.getHttpMethod().equals(incomingHttpMethod)) {
                return cm;
            }
        }

        return null;
    }


    private static HttpMethod getHttpMethod(String requestMethod) throws Exception {
        if (requestMethod.equalsIgnoreCase("get")) {
            return HttpMethod.GET;
        } else if (requestMethod.equalsIgnoreCase("post")) {
            return HttpMethod.POST;
        } else {
            throw new Exception(requestMethod + " method not handled");
        }
    }

    public static String getLocalURIPath(HttpServletRequest req) {
        return req.getRequestURI().substring(req.getContextPath().length());
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<ClassMethod> getCms() {
        return cms;
    }

    public void setCm(List<ClassMethod> cm) {
        this.cms = cms;
    }

}
