package com.giga.spring.util.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.giga.spring.servlet.route.Route;
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

    public void handleResponse(Route route, HttpServletRequest req, HttpServletResponse res) {
        boolean routeExists = route != null;

        if (routeExists) {
            invokeControllerMethod(route, req, res);
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

    protected void invokeControllerMethod(Route route, HttpServletRequest req, HttpServletResponse res) {
        try {
            Method m = route.getCm().getM();
            Class<?> returnType = m.getReturnType();

            if (returnType.equals(String.class)) {
                handleString(route, req, res);
            } else if (returnType.equals(ModelAndView.class)) {
                handleMav(route, req, res);
            } else {
                handleFallback(route, req, res);
            }
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalArgumentException | InvocationTargetException | IllegalAccessException ex) { // From method invocation
            handleError(res, "Error invoking controller method: " + ex.getMessage());
        } catch (ServletException | IOException ex) { // From requestDispatcher.forward()
            handleError(res, "Error forwarding to view: " + ex.getMessage());
        }
    }

    private void handleString(Route route, HttpServletRequest req, HttpServletResponse res) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        ClassMethod cm = route.getCm();
        res.setContentType("text/plain");
        responseBody = cm.invokeMethod().toString();
    }

    private void handleMav(Route route, HttpServletRequest req, HttpServletResponse res) throws InvocationTargetException, IllegalAccessException, ServletException, IOException, NoSuchMethodException, InstantiationException {
        ClassMethod cm = route.getCm();
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

    private void handleFallback(Route route, HttpServletRequest req, HttpServletResponse res) throws InvocationTargetException, IllegalAccessException, ServletException, IOException, NoSuchMethodException, InstantiationException {
        ClassMethod cm = route.getCm();
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
