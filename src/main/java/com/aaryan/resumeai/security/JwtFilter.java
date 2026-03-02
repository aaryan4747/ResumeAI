package com.aaryan.resumeai.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/auth/")
                || path.startsWith("/api/ai/")
                || path.startsWith("/swagger-ui/")
                || path.startsWith("/v3/api-docs/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        String path = request.getServletPath();

        System.out.println("===== JWT FILTER =====");
        System.out.println("Path: " + path);
        System.out.println("Authorization Header: " + header);

        if (header != null && header.startsWith("Bearer ")) {

            String token = header.substring(7);

            System.out.println("Token received: " + token);

            if (jwtUtil.validateToken(token)) {

                Claims claims = jwtUtil.extractClaims(token);

                String username = claims.getSubject();
                String role = claims.get("role", String.class);

                System.out.println("Username: " + username);
                System.out.println("Role: " + role);

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role)));

                SecurityContextHolder.getContext().setAuthentication(auth);

                System.out.println("Authentication set successfully");

            } else {
                System.out.println("Invalid token");
            }
        } else {
            System.out.println("No Authorization header");
        }

        filterChain.doFilter(request, response);
    }
}