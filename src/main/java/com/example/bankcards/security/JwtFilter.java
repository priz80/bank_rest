package com.example.bankcards.security;

import com.example.bankcards.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    public JwtFilter(JwtUtil jwtUtil, CustomUserDetailsService customUserDetailsService) {
        this.jwtUtil = jwtUtil;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        System.out.println("🔍 JwtFilter: processing request: " + request.getRequestURI());
        System.out.println("🔍 Method: " + request.getMethod());

        final String header = request.getHeader("Authorization");
        System.out.println("🔍 Header: " + header);

        if (header == null || !header.startsWith("Bearer ")) {
            System.out.println("🔍 No Bearer token, passing to chain");
            chain.doFilter(request, response);
            return;
        }

        final String token = header.substring(7);
        final String username;

        try {
            username = jwtUtil.extractUsername(token);
        } catch (Exception e) {
            logger.warn("Invalid JWT token", e);
            chain.doFilter(request, response);
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetailsImpl userDetails;
            try {
                userDetails = (UserDetailsImpl) customUserDetailsService.loadUserByUsername(username);
            } catch (UsernameNotFoundException e) {
                logger.warn("User not found: {}", username);
                chain.doFilter(request, response);
                return;
            }

            if (jwtUtil.validateToken(token, username)) {
                String role = jwtUtil.extractRole(token);
                if (role == null || (!"ADMIN".equals(role) && !"USER".equals(role))) {
                    logger.warn("Invalid or missing role in token: {}", role);
                    chain.doFilter(request, response);
                    return;
                }

                GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,
                        null, Collections.singletonList(authority));
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.info("Authenticated user: {}, Role: {}", username, role);
            }
        }

        chain.doFilter(request, response);
    }
}