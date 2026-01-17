package com.giga.spring.servlet.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giga.spring.servlet.rest.ErrorResponse;
import com.giga.spring.servlet.rest.Response;
import com.giga.spring.servlet.route.Route;
import com.giga.spring.util.http.ClassMethod;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class NotAllowedResponse extends GigaResponse {

    public NotAllowedResponse(Route route, HttpServletRequest req, HttpServletResponse res, ServletContext context) throws Exception {
        super(route, req, res, context);
    }

    @Override
    protected void fill(Route route, HttpServletRequest req, HttpServletResponse res, ServletContext context) throws Exception {
        res.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);

        // Collect allowed methods from route
        StringBuilder allow = new StringBuilder();
        boolean first = true;
        for (ClassMethod cm : route.getCms()) {
            String methodName = cm.getHttpMethod().name();
            if (!first) allow.append(", ");
            allow.append(methodName);
            first = false;
        }

        if (allow.length() > 0) {
            res.setHeader("Allow", allow.toString());
        }

        String accept = req.getHeader("Accept");
        String format = req.getParameter("format");
        boolean wantsJson = (accept != null && accept.contains("application/json"))
                || (format != null && format.equalsIgnoreCase("json"));

        if (wantsJson) {
            contentType = "application/json";
            Response response = new ErrorResponse(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Method not allowed");
            try {
                responseBody = new ObjectMapper().writeValueAsString(response);
            } catch (JsonProcessingException e) {
                contentType = "text/plain";
                responseBody = "Method not allowed";
            }
        } else {
            contentType = "text/html;charset=UTF-8";
            responseBody = formattedHtmlResponseBody("Method not allowed", "<h1>405 Method Not Allowed</h1>");
        }
    }
    
}
