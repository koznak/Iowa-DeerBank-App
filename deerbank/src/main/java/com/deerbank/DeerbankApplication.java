package com.deerbank;

import com.deerbank.Security.ApiKeyProperties;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

@SpringBootApplication
@EnableConfigurationProperties(ApiKeyProperties.class)
public class DeerbankApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeerbankApplication.class, args);
    }

    @Bean
    public CommandLineRunner testConnection(DataSource dataSource) {
        return args -> {
            try {
                System.out.println("Testing database connection...");
                Connection conn = dataSource.getConnection();
                System.out.println("✓ Connection successful!");
                System.out.println("Database: " + conn.getMetaData().getDatabaseProductName());
                conn.close();
            } catch (Exception e) {
                System.err.println("✗ Connection failed: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }


}
