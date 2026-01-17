package com.giga.spring.servlet.response;

import com.giga.spring.servlet.route.Route;
import com.giga.spring.util.http.ClassMethod;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class StringResponse extends GigaResponse {

    public StringResponse(Route route, HttpServletRequest req, HttpServletResponse res, ServletContext context) throws Exception {
        super(route, req, res, context);
    }

    @Override
    protected void fill(Route route, HttpServletRequest req, HttpServletResponse res, ServletContext context) throws Exception {
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
}
