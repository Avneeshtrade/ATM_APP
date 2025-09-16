package com.atm.filter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebFilter(urlPatterns = "/*")
public class CorsHeadersFilter implements Filter {

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse resp = (HttpServletResponse) response;

    String origin = req.getHeader("Origin");
    if (origin != null && !origin.isBlank()) {
      // For production, validate against an allowlist instead of "*"
      resp.setHeader("Access-Control-Allow-Origin", origin);
      resp.setHeader("Vary", "Origin");
      resp.setHeader("Access-Control-Allow-Credentials", "true");
      resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
      resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
      resp.setHeader("Access-Control-Expose-Headers", "Location, X-Request-Id");
      resp.setHeader("Content-Type", "application/json");
      if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
        resp.setHeader("Access-Control-Max-Age", "3600");
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        return;
      }
    }

    chain.doFilter(request, response);
  }
}
