package com.giga.spring.servlet;

import java.io.IOException;

import com.giga.spring.servlet.response.ResponseHandler;
import com.giga.spring.servlet.route.Route;
import com.giga.spring.servlet.route.Router;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is the servlet that takes all incoming requests targeting the app - If
 * the requested resource exists, it delegates to the default dispatcher - else
 * it shows the requested URL
 */
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 10 * 1024 * 1024, maxRequestSize = 50 * 1024 * 1024)
public class FrontServlet extends HttpServlet {

    RequestDispatcher defaultDispatcher;

    Router router;

    @Override
    public void init() {
        ServletContext servletContext = getServletContext();

        defaultDispatcher = servletContext.getNamedDispatcher("default");
        router = (Router) servletContext.getAttribute("router");
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        /**
         * Example: If URI is /app/folder/file.html and context path is /app,
         * then path = /folder/file.html
         */
        String path = Route.getLocalURIPath(req);

        boolean resourceExists = getServletContext().getResource(path) != null;

        /**
         * Only customServe throws Exception 
         * but the code is more readable this way
        */
        
        try {
            if (resourceExists) {
                defaultServe(req, res);
            } else {
                customServe(req, res);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    protected void defaultServe(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        defaultDispatcher.forward(req, res);
    }

    protected void customServe(HttpServletRequest req, HttpServletResponse res) throws Exception {
        String path = Route.getLocalURIPath(req);
        Route route = router.getRoute(path);

        new ResponseHandler(getServletContext()).handleResponse(route, req, res);
    }
}
