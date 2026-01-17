package com.giga.spring.servlet.response;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.giga.spring.servlet.route.Route;
import com.giga.spring.util.http.ClassMethod;
import com.giga.spring.util.http.ModelAndView;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ResponseHandler {
    private final ServletContext context;

    GigaResponse response = null;

    public ResponseHandler(ServletContext context) {
        this.context = context;
    }

    /**
     * The goal is to out.println(responseBody)
     * */
    public void handleResponse(Route route, HttpServletRequest req, HttpServletResponse res) throws Exception {
        boolean routeExists = route != null;

        if (routeExists) {
            response = getResponse(route, req, res);
        } else {
            response = new NotFoundResponse(req, res);
        }

        String contentType = response.contentType;
        String responseBody = response.responseBody;

        if (contentType != null && responseBody != null) {
            res.setContentType(contentType);
            try (PrintWriter out = res.getWriter()) {
                out.println(responseBody);
            } catch (IOException ex) {
                throw ex;
            }
        }
    }

    private GigaResponse getResponse(Route route, HttpServletRequest req, HttpServletResponse res) throws Exception {
        ClassMethod cm = null;
        try {
            cm = route.getClassMethodByRequest(req);

            // URL exists, but the proper method doesn't
            if (cm == null) {
                return new MethodNotAllowedResponse(route, req, res, context);
            }

            Method m = cm.getM();
            Class<?> returnType = m.getReturnType();

            // Default content type is set here
            if (returnType.equals(String.class)) {
                return new StringResponse(route, req, res, context);
            } else if (returnType.equals(ModelAndView.class)) {
                return new MavResponse(route, req, res, context);
            } else if (Object.class.isAssignableFrom(returnType)) {
                return new ObjectResponse(route, req, res, context);
            } else {
                return new FallbackResponse(route, req, res, context);
            }

        } catch (InvocationTargetException ex) {
            // Unwrap controller exception so client gets real error class/message
            Throwable cause = ex.getTargetException();
            String msg = cause == null ? ex.toString() : (cause.getClass().getName() + ": " + cause.getMessage());
            boolean isOutputToJson = cm != null && cm.isOutputToJson();
            return new ErrorResponse(res, "Error invoking controller method: " + msg, isOutputToJson);
        } catch (NoSuchMethodException | SecurityException | InstantiationException |
                 IllegalArgumentException | IllegalAccessException ex) {
            boolean isOutputToJson = cm != null && cm.isOutputToJson();
            return new ErrorResponse(res, "Error invoking controller method: " + ex.getMessage(), isOutputToJson);
        } catch (ServletException ex) {
            // Servlet forward might wrap the real cause
            Throwable cause = ex.getCause();
            String msg = cause == null ? ex.getMessage() : (cause.getClass().getName() + ": " + cause.getMessage());
            boolean isOutputToJson = cm != null && cm.isOutputToJson();
            return new ErrorResponse(res, "Error forwarding to view: " + msg, isOutputToJson);
        } catch (IOException ex) {
            boolean isOutputToJson = cm != null && cm.isOutputToJson();
            if (!isOutputToJson) {
                return new ErrorResponse(res, "Error forwarding to view: " + ex, false);
            } else { // JsonIForgotException is a subclass of IOException
                return new ErrorResponse(res, "JSON serialization error: " + ex, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
