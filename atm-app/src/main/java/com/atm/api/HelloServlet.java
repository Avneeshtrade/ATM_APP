package com.atm.api;

import java.io.IOException;
import java.util.stream.Collectors;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "HelloServlet", urlPatterns = {"/api/hello"})
public class HelloServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
       
        String name = req.getParameter("name");
        if (name == null || name.isBlank()) {
            name = "world !!!";
        }

        String safe = name.replace("\"", "\\\"");
        String json = "{\"message\":\"Hello, " + safe + "\"}";
        resp.getWriter().write(json);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String body = req.getReader().lines().collect(Collectors.joining());
        String json = "{\"received\":" + (body == null || body.isBlank() ? "\"\"" : body) + "}";
        resp.getWriter().write(json);
    }
}
