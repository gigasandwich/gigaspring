package com.giga.spring.servlet.route;

import com.giga.spring.util.http.ClassMethod;

public class Route {

    private String path;
    private ClassMethod cm;

    public Route(String path, ClassMethod cm) {
        this.path = path;
        this.cm = cm;
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
            String regex = "\\{[A-Za-z_$][A-Za-z0-9_$]*}";
            String replacement = "([^/]+)"; // Replace {var} with ([^/]+)
            pathToRegex = path.replaceAll(regex, replacement);
        }
        System.out.println(pathToRegex);
        return pathToRegex;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public ClassMethod getCm() {
        return cm;
    }

    public void setCm(ClassMethod cm) {
        this.cm = cm;
    }

}
