package com.giga.spring.servlet;

import java.io.IOException;
import java.util.Map;

import com.giga.spring.util.http.ClassMethod;
import com.giga.spring.util.http.ResponseHandler;

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
         * Example: If URI is /app/folder/file.html and context path is /app,
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

        new ResponseHandler(getServletContext()).handleResponse(cm, req, res);
    }

    protected void defaultServe(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        defaultDispatcher.forward(req, res);
    }

    /****************************
     * Utils
     ****************************/

    private String getLocalURIPath(HttpServletRequest req) {
        return req.getRequestURI().substring(req.getContextPath().length());
    }
}
