package com.giga.spring.util.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.giga.spring.servlet.rest.ErrorResponse;
import com.giga.spring.servlet.rest.Response;
import com.giga.spring.servlet.rest.SuccessResponse;
import com.giga.spring.servlet.route.Route;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

public class ResponseHandler {
    private final ServletContext context;

    private String contentType = null;
    private String responseBody = null;
    private Object responseObject = null;

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
        if (contentType != null && responseBody != null) {
            res.setContentType(contentType);
            try (PrintWriter out = res.getWriter()) {
                out.println(responseBody);
            } catch (IOException ex) {
                // TODO: log
                handleError(res, ex.getMessage(), false);
            }
        }
    }

    protected void invokeControllerMethod(Route route, HttpServletRequest req, HttpServletResponse res) {
        ClassMethod cm = null;
        try {
            cm = route.getClassMethodByRequest(req);
            Method m = cm.getM();

            Class<?> returnType = m.getReturnType();

            // Default content type is set here
            if (returnType.equals(String.class)) {
                handleString(route, req, res);
            } else if (returnType.equals(ModelAndView.class)) {
                handleMav(route, req, res);
            } else if (Object.class.isAssignableFrom(returnType)) {
                handleObject(route, req, res);
            } else {
                handleFallback(route, req, res);
            }

            if (cm.isOutputToJson()) {
                contentType = "application/json";
                Response response = new SuccessResponse(200, responseObject);
                responseBody = response._toString();
            }
        } catch (InvocationTargetException ex) {
            // Unwrap controller exception so client gets real error class/message
            Throwable cause = ex.getTargetException();
            String msg = cause == null ? ex.toString() : (cause.getClass().getName() + ": " + cause.getMessage());
            boolean isOutputToJson = cm != null && cm.isOutputToJson();
            handleError(res, "Error invoking controller method: " + msg, isOutputToJson);
        } catch (NoSuchMethodException | SecurityException | InstantiationException |
                 IllegalArgumentException | IllegalAccessException ex) {
            boolean isOutputToJson = cm != null && cm.isOutputToJson();
            handleError(res, "Error invoking controller method: " + ex.getMessage(), isOutputToJson);
        } catch (ServletException ex) {
            // Servlet forward might wrap the real cause
            Throwable cause = ex.getCause();
            String msg = cause == null ? ex.getMessage() : (cause.getClass().getName() + ": " + cause.getMessage());
            boolean isOutputToJson = cm != null && cm.isOutputToJson();
            handleError(res, "Error forwarding to view: " + msg, isOutputToJson);
        } catch (IOException ex) {
            boolean isOutputToJson = cm != null && cm.isOutputToJson();
            if (isOutputToJson) {
                handleError(res, "Error forwarding to view: " + ex, false);
            } else { // JsonIForgotException is a subclass or IOException
                handleError(res, "JSON serialization error: " + ex, true);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handleString(Route route, HttpServletRequest req, HttpServletResponse res) throws Exception {
        ClassMethod cm = route.getClassMethodByRequest(req);
        Object result = cm.invokeMethod(route, req);
        if (cm.isOutputToJson()) {
            // Keep the raw object so the wrapper can serialize it (avoid double-escaping)
            responseObject = result;
        } else {
            contentType = "text/plain";
            responseBody = result == null ? "" : result.toString();
        }
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

    private void handleObject(Route route, HttpServletRequest req, HttpServletResponse res) throws Exception {
        ClassMethod cm = route.getClassMethodByRequest(req);
        Object object = cm.invokeMethod(route, req);
        // contentType is application/json, set in invokeControllerMethod
        responseObject = object;
    }

    private void handleError(HttpServletResponse res, String errorMessage, boolean isOutputToJson) {
        res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        if (isOutputToJson) {
            contentType = "application/json";
            Response response = new ErrorResponse(500, errorMessage);
            try {
                responseBody = new ObjectMapper().writeValueAsString(response);
            } catch (JsonProcessingException e) {
                contentType = "text/plain";
                responseBody = errorMessage;
            }
        } else {
            contentType = "text/html;charset=UTF-8";
            responseBody = formattedHtmlResponseBody("Error", "<h1>" + errorMessage + "</h1>");
        }
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
