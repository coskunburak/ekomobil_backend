package com.ekomobil.config;

import com.ekomobil.repo.DeviceKeyRepository;
import com.ekomobil.repo.UserRepository; // <-- EKLE
import com.ekomobil.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value; // <-- EKLE
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            DeviceKeyAuthFilter deviceKeyAuthFilter,
            DevUserAuthFilter devUserAuthFilter,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) throws Exception {

        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/actuator/**", "/ws/**").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/search", "/api/v1/search/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/map/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/telemetry").authenticated()
                        .requestMatchers("/api/v1/**").authenticated()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(deviceKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(devUserAuthFilter, UsernamePasswordAuthenticationFilter.class)    // <-- EKLE
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public DaoAuthenticationProvider authProvider(org.springframework.security.core.userdetails.UserDetailsService uds,
                                                  PasswordEncoder encoder) {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(uds);
        p.setPasswordEncoder(encoder);
        return p;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public DeviceKeyAuthFilter deviceKeyAuthFilter(DeviceKeyRepository repo) {
        return new DeviceKeyAuthFilter(repo);
    }

    @Bean
    public DevUserAuthFilter devUserAuthFilter(UserRepository userRepo,
                                               @Value("${security.dev-auth.enabled:false}") boolean enabled) {
        return new DevUserAuthFilter(userRepo, enabled);
    }

    static class DeviceKeyAuthFilter extends OncePerRequestFilter {
        private final DeviceKeyRepository repo;
        DeviceKeyAuthFilter(DeviceKeyRepository repo) { this.repo = repo; }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
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
                        request.setAttribute("busId", busId);
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    });
                }
            }

            chain.doFilter(request, response);
        }
    }

    static class DevUserAuthFilter extends OncePerRequestFilter {
        private final UserRepository userRepo;
        private final boolean enabled;
        DevUserAuthFilter(UserRepository userRepo, boolean enabled) {
            this.userRepo = userRepo;
            this.enabled = enabled;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                throws ServletException, IOException {
            if (enabled && SecurityContextHolder.getContext().getAuthentication() == null) {
                String uidHeader = request.getHeader("X-Debug-UserId");
                if (uidHeader != null && !uidHeader.isBlank()) {
                    try {
                        Long uid = Long.parseLong(uidHeader.trim());
                        // Kullanıcı var mı kontrol edelim; yoksa auth vermeyelim:
                        userRepo.findById(uid).ifPresent(u -> {
                            Authentication auth = new AbstractAuthenticationToken(
                                    List.of(new SimpleGrantedAuthority("ROLE_USER"))) {
                                @Override public Object getCredentials() { return "N/A"; }
                                @Override public Object getPrincipal() { return uid; } // principal = userId
                            };
                            ((AbstractAuthenticationToken) auth).setAuthenticated(true);
                            request.setAttribute("uid", uid);
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        });
                    } catch (NumberFormatException ignored) {}
                }
            }
            chain.doFilter(request, response);
        }
    }
}
