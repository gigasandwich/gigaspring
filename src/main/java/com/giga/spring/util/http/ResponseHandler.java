package com.giga.spring.util.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ResponseHandler {

    private String responseBody = null;
    private final ServletContext context;

    public ResponseHandler(ServletContext context) {
        this.context = context;
    }

    public void handleResponse(ClassMethod cm, HttpServletRequest req, HttpServletResponse res) {
        boolean cmExists = cm != null;

        if (cmExists) {
            invokeControllerMethod(cm, req, res);
        } else {
            handle404(res);
        }

        // responseBody is instantiated in either invokeControllerMethod(...) or handle404(...)
        if (responseBody != null) {
            try (PrintWriter out = res.getWriter()) {
                out.println(responseBody);
            } catch (IOException ex) {
                // TODO: log
                handleError(res, ex.getMessage());
            }
        }
    }

    protected void invokeControllerMethod(ClassMethod cm, HttpServletRequest req, HttpServletResponse res) {
        try {
            Method m = cm.getM();
            Class<?> returnType = m.getReturnType();

            if (returnType.equals(String.class)) {
                handleString(cm, req, res);
            } else if (returnType.equals(ModelAndView.class)) {
                handleMav(cm, req, res);
            } else {
                handleFallback(cm, req, res);
            }
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalArgumentException | InvocationTargetException | IllegalAccessException ex) { // From method invocation
            handleError(res, "Error invoking controller method: " + ex.getMessage());
        } catch (ServletException | IOException ex) { // From requestDispatcher.forward()
            handleError(res, "Error forwarding to view: " + ex.getMessage());
        }
    }

    private void handleString(ClassMethod cm, HttpServletRequest req, HttpServletResponse res) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        res.setContentType("text/plain");
        responseBody = cm.invokeMethod().toString();
    }

    private void handleMav(ClassMethod cm, HttpServletRequest req, HttpServletResponse res) throws InvocationTargetException, IllegalAccessException, ServletException, IOException, NoSuchMethodException, InstantiationException {
        ModelAndView mav = (ModelAndView) cm.invokeMethod();
        String view = mav.getView();

        for (String key : mav.getAttributes().keySet()) {
            Object value =  mav.getAttributes().get(key);
            req.setAttribute(key, value);
        }

        RequestDispatcher requestDispatcher = context.getRequestDispatcher(view);
        requestDispatcher.forward(req, res);
        // No need to set responseBody anymore because requestDispatcher.forward(...) handles the response
    }

    private void handleFallback(ClassMethod cm, HttpServletRequest req, HttpServletResponse res) throws InvocationTargetException, IllegalAccessException, ServletException, IOException, NoSuchMethodException, InstantiationException {
        // Not sure what `content type` to add yet
        cm.invokeMethod();
        // No responseBody either because of the unknown return type
    }

    private void handleError(HttpServletResponse res, String errorMessage) {
        res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        responseBody = formattedHtmlResponseBody("Error", "<h1>" + errorMessage + "</h1>");
        res.setContentType("text/html;charset=UTF-8");
    }

    protected void handle404(HttpServletResponse res) {
        String htmlBody = "<h1>404 not found</h1>";
        responseBody = formattedHtmlResponseBody("Method not found", htmlBody);
        res.setContentType("text/html;charset=UTF-8");
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
