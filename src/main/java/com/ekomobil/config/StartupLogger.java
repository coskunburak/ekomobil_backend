package com.ekomobil.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StartupLogger {
    private final DataSource dataSource;

    @PostConstruct
    public void logDs() {
        try {
            HikariDataSource hk = dataSource.unwrap(HikariDataSource.class);
            log.warn("### DS url={}, user={}", hk.getJdbcUrl(), hk.getUsername());
        } catch (Exception e) {
            log.warn("### DS info: {}", dataSource);
        }
    }
}
