package com.delivery.foodDelivery.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Bean
    @Primary
    public DataSource dataSource() {
        // Trimming the URL to handle cases where environment variables might have trailing newlines
        String trimmedUrl = url != null ? url.trim() : null;
        
        DataSource dataSource = DataSourceBuilder.create()
                .url(trimmedUrl)
                .username(username)
                .password(password)
                .driverClassName(driverClassName)
                .build();

        // Run cleanup logic before Hibernate starts
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            try {
                stmt.execute("ALTER TABLE order_items DROP FOREIGN KEY FKdtfg1f49yr5yye2fpl2xid2xo");
            } catch (Exception ignored) {}

            try {
                stmt.execute("ALTER TABLE orders DROP FOREIGN KEY FK2m9qulf12xm537bku3jnrrbup");
            } catch (Exception ignored) {}
            
        } catch (Exception e) {
            // Log or ignore if DB is not ready
        }

        return dataSource;
    }
}
