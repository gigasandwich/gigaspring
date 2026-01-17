package com.giga.spring.servlet.response;

import com.giga.spring.servlet.route.Route;
import com.giga.spring.util.http.ClassMethod;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FallbackResponse extends GigaResponse {

    public FallbackResponse(Route route, HttpServletRequest req, HttpServletResponse res, ServletContext context) throws Exception {
        super(route, req, res, context);
    }

    @Override
    protected void fill(Route route, HttpServletRequest req, HttpServletResponse res, ServletContext context) throws Exception {
        ClassMethod cm = route.getClassMethodByRequest(req);
        // Not sure what `content type` to add yet
        cm.invokeMethod(route, req);
        // No responseBody either because of the unknown return type
    }
    
}
