package com.giga.spring.util.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.giga.spring.servlet.route.Route;
import com.giga.spring.util.http.constant.HttpMethod;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ResponseHandler {
    private final ServletContext context;

    private String contentType = null;
    private String responseBody = null;

    public ResponseHandler(ServletContext context) {
        this.context = context;
    }

    /**
     * The goal is to out.println(responseBody)
     * */
    public void handleResponse(Route route, HttpServletRequest req, HttpServletResponse res) {
        boolean routeExists = route != null;

        if (routeExists) {
            invokeControllerMethod(route, req, res);
        } else {
            handle404(res);
        }

        // responseBody is instantiated in either invokeControllerMethod(...) or handle404(...)
        if (responseBody != null) {
            res.setContentType(contentType);
            try (PrintWriter out = res.getWriter()) {
                out.println(responseBody);
            } catch (IOException ex) {
                // TODO: log
                handleError(res, ex.getMessage());
            }
        }
    }

    protected void invokeControllerMethod(Route route, HttpServletRequest req, HttpServletResponse res) {
        try {
            Method m = route.getClassMethodByRequest(req).getM();
            ClassMethod cm = route.getClassMethodByRequest(req);
            Method m = cm.getM();

            Class<?> returnType = m.getReturnType();

            // Default content type is set here
            if (returnType.equals(String.class)) {
                handleString(route, req, res);
            } else if (returnType.equals(ModelAndView.class)) {
                handleMav(route, req, res);
            } else {
                handleFallback(route, req, res);
            }
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalArgumentException |
                 InvocationTargetException | IllegalAccessException ex) { // From method invocation
            handleError(res, "Error invoking controller method: " + ex.getMessage());
        } catch (ServletException | IOException ex) { // From requestDispatcher.forward()
            handleError(res, "Error forwarding to view: " + ex.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handleString(Route route, HttpServletRequest req, HttpServletResponse res) throws Exception {
        ClassMethod cm = route.getClassMethodByRequest(req);
        contentType = "text/plain";
        responseBody = cm.invokeMethod(route, req).toString();
    }

    private void handleMav(Route route, HttpServletRequest req, HttpServletResponse res) throws Exception {
        ClassMethod cm = route.getClassMethodByRequest(req);
        ModelAndView mav = (ModelAndView) cm.invokeMethod(route, req);
        String view = mav.getView();

        for (String key : mav.getAttributes().keySet()) {
            Object value =  mav.getAttributes().get(key);
            req.setAttribute(key, value);
        }

        RequestDispatcher requestDispatcher = context.getRequestDispatcher(view);
        requestDispatcher.forward(req, res);
        // No need to set responseBody anymore because requestDispatcher.forward(...) handles the response
    }

    private void handleFallback(Route route, HttpServletRequest req, HttpServletResponse res) throws Exception {
        ClassMethod cm = route.getClassMethodByRequest(req);
        // Not sure what `content type` to add yet
        cm.invokeMethod(route, req);
        // No responseBody either because of the unknown return type
    }

    private void handleError(HttpServletResponse res, String errorMessage) {
        res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        contentType = "text/html;charset=UTF-8";
        responseBody = formattedHtmlResponseBody("Error", "<h1>" + errorMessage + "</h1>");
    }

    protected void handle404(HttpServletResponse res) {
        String htmlBody = "<h1>404 not found</h1>";
        contentType = "text/html;charset=UTF-8";
        responseBody = formattedHtmlResponseBody("Method not found", htmlBody);
    }

    private String formattedHtmlResponseBody(String title, String body) {
        return """
            <html>
                <head><title>%s</title></head>
                <body>
                    %s
                </body>
            </html>""".formatted(title, body);
    }
}
