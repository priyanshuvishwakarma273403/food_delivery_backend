package com.delivery.foodDelivery.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

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
        
        return DataSourceBuilder.create()
                .url(trimmedUrl)
                .username(username)
                .password(password)
                .driverClassName(driverClassName)
                .build();
    }
}
