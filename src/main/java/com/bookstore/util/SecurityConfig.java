package com.bookstore.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

/** Adds security headers and blocks any POST that does not carry the session's CSRF token. */
@Configuration
public class SecurityConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
                res.setHeader("X-Frame-Options", "DENY");
                res.setHeader("X-Content-Type-Options", "nosniff");
                res.setHeader("Referrer-Policy", "same-origin");
                HttpSession s = req.getSession();
                String token = (String) s.getAttribute("csrf");
                if (token == null) { token = UUID.randomUUID().toString(); s.setAttribute("csrf", token); }
                req.setAttribute("csrfToken", token);
                if ("POST".equalsIgnoreCase(req.getMethod())) {
                    String sent = req.getParameter("_csrf");
                    if (sent == null || !MessageDigest.isEqual(sent.getBytes(StandardCharsets.UTF_8), token.getBytes(StandardCharsets.UTF_8))) {
                        res.sendError(403, "Invalid or missing security token. Reload the page and try again.");
                        return false;
                    }
                }
                return true;
            }
        }).excludePathPatterns("/css/**", "/js/**", "/images/**");
    }
}
