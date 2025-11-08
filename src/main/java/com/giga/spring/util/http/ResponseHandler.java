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

        // responseBody is instanciated in either invokeControllerMethod(...) or handle404(...)
        if (responseBody != null) {
            try (PrintWriter out = res.getWriter()) {
                out.println(responseBody);
            } catch (IOException ex) {
                // TODO: log
                ex.printStackTrace();
            }
        }
    }

    protected void invokeControllerMethod(ClassMethod cm, HttpServletRequest req, HttpServletResponse res) {
        try {
            Class<?> c = cm.getC();

            Method m = cm.getM();
            m.setAccessible(true); // Never forget this 🗿

            Class<?> returnType = m.getReturnType();

            Constructor<?> controllerConstructor = c.getDeclaredConstructor();
            Object controller = controllerConstructor.newInstance();

            if (returnType.equals(String.class)) {
                res.setContentType("text/plain");
                responseBody = m.invoke(controller).toString();
            } else if (returnType.equals(ModelAndView.class)) {
                ModelAndView mav = (ModelAndView) m.invoke(controller);
                String view = mav.getView();
                RequestDispatcher requestDispatcher = context.getRequestDispatcher(view);
                requestDispatcher.forward(req, res);
                // No need to set responseBody anymore because requestDispatcher.forward(...) handles the response
            } else {
                // Not sure what `content type` to add yet
                m.invoke(controller);
                // No responseBody either because of the unknown return type
            }
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalArgumentException | InvocationTargetException | IllegalAccessException ex) { // From method invocation
            handleError(res, "Error invoking controller method: " + ex.getMessage());
        } catch (ServletException | IOException ex) { // From requestDispatcher.forward()
            handleError(res, "Error forwarding to view: " + ex.getMessage());
        }
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
