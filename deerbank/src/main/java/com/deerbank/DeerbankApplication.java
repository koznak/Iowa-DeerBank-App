package com.deerbank;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

@SpringBootApplication
public class DeerbankApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeerbankApplication.class, args);
    }

    @Bean
    public CommandLineRunner testDatabaseConnection(DataSource dataSource) {
        return args -> {
            try {
                System.out.println("=================================");
                System.out.println("Testing Database Connection...");
                System.out.println("=================================");

                Connection connection = dataSource.getConnection();

                if (connection != null) {
                    System.out.println("✓ Connection successful!");

                    DatabaseMetaData metaData = connection.getMetaData();
                    System.out.println("Database: " + metaData.getDatabaseProductName());
                    System.out.println("Version: " + metaData.getDatabaseProductVersion());
                    System.out.println("URL: " + metaData.getURL());
                    System.out.println("User: " + metaData.getUserName());

                    connection.close();
                    System.out.println("✓ Connection closed successfully!");
                } else {
                    System.out.println("✗ Failed to establish connection!");
                }

                System.out.println("=================================");

            } catch (Exception e) {
                System.err.println("=================================");
                System.err.println("✗ Database Connection Failed!");
                System.err.println("Error: " + e.getMessage());
                System.err.println("=================================");
                e.printStackTrace();
            }
        };
    }
}
