package com.giga.spring.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        showURL(req, res);
    }

    private void showURL(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("text/html;charset=UTF-8");

        String uri = req.getRequestURI();

        PrintWriter out = res.getWriter();

        out.println("<html><body>");
        out.println("<h1>FrontServlet Catch All incoming URL</h1>");
        out.println("<p>Requested URL: " + uri + "</p>");
        out.println("</body></html>");
    }
  
}
