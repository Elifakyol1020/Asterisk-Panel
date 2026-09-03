package com.netgsm.asterisk.security;
import com.netgsm.asterisk.service.JwtService;

import com.netgsm.asterisk.service.AuthService;
import com.netgsm.asterisk.exception.InvalidCredentialsException;
import com.netgsm.asterisk.response.ApiError;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService tokens;
    private final AuthService auth;
    private final ObjectMapper mapper;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null) {
            try {
                if (!authorization.startsWith("Bearer ")) throw new InvalidCredentialsException();
                var user = auth.authenticate(tokens.decode(authorization.substring(7)));
                var authentication = new UsernamePasswordAuthenticationToken(user, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.role().name())));
                var context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            } catch (JwtException | InvalidCredentialsException | IllegalArgumentException | ClassCastException ex) {
                SecurityContextHolder.clearContext();
                response.setStatus(401);
                response.setContentType("application/json");
                mapper.writeValue(response.getOutputStream(), new ApiError(Instant.now(), 401, "UNAUTHORIZED",
                        "Invalid or expired token", request.getRequestURI(), Map.of()));
                return;
            } catch (org.springframework.dao.DataAccessException ex) {
                response.setStatus(503); response.setContentType("application/json");
                mapper.writeValue(response.getOutputStream(), new ApiError(Instant.now(), 503, "SERVICE_UNAVAILABLE",
                        "Authentication service unavailable", request.getRequestURI(), Map.of()));
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
