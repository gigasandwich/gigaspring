package com.giga.spring.servlet.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giga.spring.servlet.rest.ErrorResponse;
import com.giga.spring.servlet.rest.Response;
import com.giga.spring.servlet.route.Route;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class NotFoundResponse extends GigaResponse {

    public NotFoundResponse(HttpServletRequest req, HttpServletResponse res) throws Exception {
        super(null, req, res, null);
    }

    @Override
    protected void fill(Route route, HttpServletRequest req, HttpServletResponse res, ServletContext context) throws Exception {
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        String accept = req.getHeader("Accept");
        String format = req.getParameter("format");
        boolean wantsJson = (accept != null && accept.contains("application/json")) 
                                || (format != null && format.equalsIgnoreCase("json"));

        if (wantsJson) {
            contentType = "application/json";
            Response response = new ErrorResponse(HttpServletResponse.SC_NOT_FOUND, "Resource not found");
            try {
                responseBody = new ObjectMapper().writeValueAsString(response);
            } catch (JsonProcessingException e) {
                contentType = "text/plain";
                responseBody = "Resource not found";
            }
        } else {
            String htmlBody = "<h1>404 not found</h1>";
            contentType = "text/html;charset=UTF-8";
            responseBody = formattedHtmlResponseBody("Resource not found", htmlBody);
        }
    }
    
}
