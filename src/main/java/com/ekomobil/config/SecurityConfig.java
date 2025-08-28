package com.ekomobil.config;

import com.ekomobil.repo.DeviceKeyRepository;
import com.ekomobil.security.JwtAuthenticationFilter;
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
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) throws Exception {

        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // CORS preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Swagger, Actuator, WS serbest
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/actuator/**", "/ws/**").permitAll()

                        // Auth uçları serbest
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // Search endpoint serbest (GET)
                        .requestMatchers(HttpMethod.GET, "/api/v1/search", "/api/v1/search/**").permitAll()

                        // Map endpointleri sadece GET için serbest
                        .requestMatchers(HttpMethod.GET, "/api/v1/map/**").permitAll()

                        // Telemetry POST => DEVICE anahtarıyla authenticated (DeviceKeyAuthFilter set eder)
                        .requestMatchers(HttpMethod.POST, "/api/v1/telemetry").authenticated()

                        // Diğer tüm API'ler auth ister
                        .requestMatchers("/api/v1/**").authenticated()
                        .anyRequest().permitAll()
                )
                // Önce cihaz anahtarı, sonra JWT
                .addFilterBefore(deviceKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)
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

    //DeviceKey filtresi aynı kalır obd2 de işime yarayacak burası
    @Bean
    public DeviceKeyAuthFilter deviceKeyAuthFilter(DeviceKeyRepository repo) {
        return new DeviceKeyAuthFilter(repo);
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
}
