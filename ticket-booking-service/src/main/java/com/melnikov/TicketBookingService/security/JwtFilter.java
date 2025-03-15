package com.melnikov.TicketBookingService.security;

import com.melnikov.TicketBookingService.services.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    public JwtFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getServletPath();
        // Пропускаем JWT-проверку для эндпоинтов аутентификации
        if (path.startsWith("/auth/")) {
            chain.doFilter(request, response);
            return;
        }

        log.debug("Starting JWT processing for request: {} {}", request.getMethod(), request.getRequestURI());

        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header. Header: {}", authHeader);
                chain.doFilter(request, response);
                return;
            }

            String token = authHeader.substring(7);
            log.debug("JWT token found. Token length: {}", token.length());

            Claims claims = jwtService.extractClaims(token); // Здесь может быть ExpiredJwtException

            String email = claims.getSubject();
            Integer roleId = claims.get("roleId", Integer.class);
            log.info("Authenticating user. Email: {}, RoleID: {}", email, roleId);

            List<GrantedAuthority> authorities = switch (roleId) {
                case 1 -> List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
                case 2 -> List.of(new SimpleGrantedAuthority("ROLE_USER"));
                default -> Collections.emptyList();
            };

            User user = new User(email, "", authorities);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("Authenticated user: {} with roles: {}", email, authorities);

        } catch (ExpiredJwtException e) {
            log.error("JWT token expired: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"code\": \"TOKEN_EXPIRED\", \"message\": \"Token has expired\"}");
            return;
        } catch (Exception e) {
            log.error("JWT processing error: {}", e.getMessage(), e);
            SecurityContextHolder.clearContext();
        }
        chain.doFilter(request, response);
    }
}