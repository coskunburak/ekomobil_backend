package com.ekomobil.config;

import com.ekomobil.repo.DeviceKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           DeviceKeyAuthFilter deviceKeyAuthFilter) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/actuator/**",
                                "/ws/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/telemetry").authenticated()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(deviceKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public DeviceKeyAuthFilter deviceKeyAuthFilter(DeviceKeyRepository repo) {
        return new DeviceKeyAuthFilter(repo);
    }
    static class DeviceKeyAuthFilter extends OncePerRequestFilter {
        private final DeviceKeyRepository repo;

        DeviceKeyAuthFilter(DeviceKeyRepository repo) {
            this.repo = repo;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain)
                throws ServletException, IOException {

            if ("POST".equalsIgnoreCase(request.getMethod())
                    && request.getRequestURI().startsWith("/api/v1/telemetry")
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                String key = request.getHeader("X-Device-Key");
                if (key != null && !key.isBlank()) {
                    repo.findByApiKeyAndEnabledTrue(key).ifPresent(deviceKey -> {
                        Long busId = deviceKey.getBus().getId();

                        Authentication auth = new AbstractAuthenticationToken(
                                List.of(new SimpleGrantedAuthority("ROLE_DEVICE"))) {
                            @Override public Object getCredentials() { return "N/A"; }
                            @Override public Object getPrincipal() { return busId; }
                        };
                        ((AbstractAuthenticationToken) auth).setAuthenticated(true);

                        // İstersen request attribute da kalsın:
                        request.setAttribute("busId", busId);

                        SecurityContextHolder.getContext().setAuthentication(auth);
                    });
                }
            }

            filterChain.doFilter(request, response);
        }
    }
}
