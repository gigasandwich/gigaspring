package com.giga.spring.servlet.response;

import com.giga.spring.servlet.route.Route;
import com.giga.spring.util.http.ClassMethod;
import com.giga.spring.util.http.ModelAndView;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class MavResponse extends GigaResponse {
    
    public MavResponse(Route route, HttpServletRequest req, HttpServletResponse res, ServletContext context) throws Exception {
        super(route, req, res, context);
    }

    /**
     * Doesn't instanciate responseBody in fill
     */
    @Override
    protected void fill(Route route, HttpServletRequest req, HttpServletResponse res, ServletContext context) throws Exception {
        ClassMethod cm = route.getClassMethodByRequest(req);
        ModelAndView mav = (ModelAndView) cm.invokeMethod(route, req);
        String view = mav.getView();

        for (String key : mav.getAttributes().keySet()) {
            Object value = mav.getAttributes().get(key);
            req.setAttribute(key, value);
        }

        RequestDispatcher requestDispatcher = context.getRequestDispatcher(view);
        requestDispatcher.forward(req, res);
        // No need to set responseBody anymore because requestDispatcher.forward(...) handles the response
    }
    
}
