package com.giga.spring.servlet.response;

import com.giga.spring.servlet.rest.Response;
import com.giga.spring.servlet.rest.SuccessResponse;
import com.giga.spring.servlet.route.Route;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

abstract class GigaResponse {
    String contentType;
    String responseBody;
    Object responseObject;

    /**
     * TODO: understand why certain subclasses don't work when context isn't directly injected
     * Like how it is here
     */
    public GigaResponse(Route route, HttpServletRequest req, HttpServletResponse res, ServletContext context) throws Exception {
        fill(route, req, res, context);

        if (route.getClassMethodByRequest(req).isOutputToJson()) {
            contentType = "application/json";
            Response response = new SuccessResponse(200, responseObject);
            responseBody = response._toString();
        }
    }
    
    protected abstract void fill(Route route, HttpServletRequest req, HttpServletResponse res, ServletContext context) throws Exception;

    // Used by error classes
    protected String formattedHtmlResponseBody(String title, String body) {
        return """
            <html>
                <head><title>%s</title></head>
                <body>
                    %s
                </body>
            </html>""".formatted(title, body);
    }
}
