package com.giga.spring.servlet.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giga.spring.servlet.rest.Response;
import com.giga.spring.servlet.route.Route;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ErrorResponse extends GigaResponse {
    String errorMessage;
    boolean isOutputToJson;

    public ErrorResponse(HttpServletResponse res, String errorMessage, boolean isOutputToJson) throws Exception {
        super(null, null, res, null);
    }

    @Override
    protected void fill(Route route, HttpServletRequest req, HttpServletResponse res, ServletContext context) throws Exception {
        res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        if (isOutputToJson) {
            contentType = "application/json";
            Response response = new com.giga.spring.servlet.rest.ErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage);
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
}
