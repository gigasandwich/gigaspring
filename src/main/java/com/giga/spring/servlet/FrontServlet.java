package com.giga.spring.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.*;

import com.giga.spring.annotation.ControllerAnnotation;
import com.giga.spring.util.ClassMethod;
import com.giga.spring.util.ScanUtils;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is the servlet that takes all incoming requests targeting the app - If
 * the requested resource exists, it delegates to the default dispatcher - else
 * it shows the requested URL
 */
public class FrontServlet extends HttpServlet {

    RequestDispatcher defaultDispatcher;

    Map<String, ClassMethod> urlMethodMap;

    @Override
    public void init() {
        defaultDispatcher = getServletContext().getNamedDispatcher("default");
        urlMethodMap = getUrlMethodMap();
    }

    /**
     * Early gets all the url - method
     * and inserts it in a map
     */
    private Map<String, ClassMethod> getUrlMethodMap() {
        Map<String, ClassMethod> map = new HashMap<>();

        ScanUtils scanUtils = ScanUtils.getInstance();

        Set<Class<?>> classes = scanUtils.getClassesAnnotatedWith(ControllerAnnotation.class, "com.giga");
        System.out.println(classes.size());
        for (Class<?> c : classes) {
            Map<String, Method> urlMappingPathMap = scanUtils.getAllUrlMappingPathValues(c);

            for (String url : urlMappingPathMap.keySet()) {
                Method m = urlMappingPathMap.get(url);
                System.out.println("Url: " + url + ", method: " + m.getName());
                ClassMethod cm = new ClassMethod(c, m);
                map.put(url, cm);
            }
        }
        return map;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        /**
         * Example:
         * If URI is /app/folder/file.html
         * and context path is /app,
         * then path = /folder/file.html
         */
        String path = getLocalURIPath(req);

        boolean resourceExists = getServletContext().getResource(path) != null;

        if (resourceExists) {
            defaultServe(req, res);
        } else {
            customServe(req, res);
        }
    }

    protected void customServe(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String path = getLocalURIPath(req);
        ClassMethod cm = getCmFor(path);
        boolean cmExists = cm != null;

        String responseBody = "";

        if (cmExists) {
            String classMethodStr = cm.getM().getName();
            String htmlBody = """
                    <h1>Method found</h1>
                    <bold>%s</bold>
                    """.formatted(classMethodStr);

            responseBody = setResponseBody("Method found", htmlBody);
        } else {
            String htmlBody = "<h1>Resource not found: </h1>" + path;
            responseBody = setResponseBody("Method not found", htmlBody);
        }

        try (PrintWriter out = res.getWriter()) {
            res.setContentType("text/html;charset=UTF-8");
            out.println(responseBody);
        }
    }

    private String getLocalURIPath(HttpServletRequest req) {
        return req.getRequestURI().substring(req.getContextPath().length());
    }

    private ClassMethod getCmFor(String uri) {
        return urlMethodMap.get(uri);
    }

    private String setResponseBody(String title, String body) {
        return """
            <html>
                <head><title>%s</title></head>
                <body>
                    %s
                </body>
            </html>""".formatted(title, body);
    }

    protected void defaultServe(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        defaultDispatcher.forward(req, res);
    }

}
