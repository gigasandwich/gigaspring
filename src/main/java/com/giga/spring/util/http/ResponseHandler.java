package com.giga.spring.util.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import jakarta.servlet.http.HttpServletResponse;

public class ResponseHandler {

    private static ResponseHandler instance;

    private ClassMethod cm;

    private String responseBody = null;

    public static ResponseHandler getInstance() {
        if (instance == null) {
            instance = new ResponseHandler();
        }
        return instance;
    }

    public void handleResponse(ClassMethod cm, HttpServletResponse res) {
        boolean cmExists = cm != null;
        System.out.println(cmExists);
        
        if (cmExists) {
            invokeControllerMethod(cm, res);
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

    private void invokeControllerMethod(ClassMethod cm, HttpServletResponse res) {
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
            } else {
                // Not sure what `content type` to add yet
                m.invoke(controller);
            }
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalArgumentException | InvocationTargetException | IllegalAccessException ex) {
            // TODO: log ex.getMessage()
        }
    }

    private void handle404(HttpServletResponse res) {
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
