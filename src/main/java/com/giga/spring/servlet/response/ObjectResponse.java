package com.giga.spring.servlet.response;

import com.giga.spring.servlet.route.Route;
import com.giga.spring.util.http.ClassMethod;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ObjectResponse extends GigaResponse {
    ServletContext context = null;
    
    public ObjectResponse(Route route, HttpServletRequest req, HttpServletResponse res, ServletContext context) throws Exception {
        super(route, req, res, context);
    }

    @Override
    protected void fill(Route route, HttpServletRequest req, HttpServletResponse res, ServletContext context) throws Exception {
        ClassMethod cm = route.getClassMethodByRequest(req);
        Object object = cm.invokeMethod(route, req);
        // contentType is application/json, set in invokeControllerMethod
        responseObject = object;
    }
    
}
