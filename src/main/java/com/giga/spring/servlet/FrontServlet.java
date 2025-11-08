package com.giga.spring.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.*;

import com.giga.spring.annotation.*;
import com.giga.spring.util.*;
import com.giga.spring.util.scan.*;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
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
        ServletContext servletContext = getServletContext();
        
        defaultDispatcher = servletContext.getNamedDispatcher("default");
        urlMethodMap = (Map<String, ClassMethod>) servletContext.getAttribute("urlCmMap");
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
        ClassMethod cm = urlMethodMap.get(path);
        boolean cmExists = cm != null;

        String responseBody = "";

        if (cmExists) {
            String classMethodStr = cm.getC().getName() + "." + cm.getM().getName() + "()";
            String htmlBody = """
                    <h1>Method found</h1>
                    <bold>%s</bold>
                    """.formatted("<code>" + classMethodStr + "</code>");

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
