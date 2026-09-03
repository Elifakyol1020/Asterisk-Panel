package com.netgsm.asterisk.config;

import com.netgsm.asterisk.security.JwtAuthenticationFilter;
import com.netgsm.asterisk.service.JwtService;
import com.netgsm.asterisk.service.AuthService;
import com.netgsm.asterisk.response.ApiError;
import java.time.Instant;
import java.util.Map;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

@Configuration @EnableMethodSecurity
public class SecurityConfiguration {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }
    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService tokens, AuthService auth,
                                                ObjectMapper mapper) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(sessions -> sessions.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requestCache(cache -> cache.disable())
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("SUPER_ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, ex) -> {
                            response.setStatus(401); response.setContentType("application/json");
                            mapper.writeValue(response.getOutputStream(), new ApiError(Instant.now(), 401,
                                    "UNAUTHORIZED", "Authentication required", request.getRequestURI(), Map.of()));
                        })
                        .accessDeniedHandler((request, response, ex) -> {
                            response.setStatus(403); response.setContentType("application/json");
                            mapper.writeValue(response.getOutputStream(), new ApiError(Instant.now(), 403,
                                    "FORBIDDEN", "Access denied", request.getRequestURI(), Map.of()));
                        }))
                .addFilterBefore(new JwtAuthenticationFilter(tokens, auth, mapper), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
