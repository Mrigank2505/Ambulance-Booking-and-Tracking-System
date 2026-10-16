package com.ambulance.system.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.InetSocketAddress;
import java.net.Socket;

@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${spring.datasource.url:jdbc:mysql://localhost:3306/ambulance_booking_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}")
    private String mysqlUrl;

    @Value("${spring.datasource.username:root}")
    private String mysqlUsername;

    @Value("${spring.datasource.password:Root@123}")
    private String mysqlPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        boolean mysqlAvailable = isPortAvailable("localhost", 3306, 1200);

        if (mysqlAvailable) {
            try {
                log.info("Connecting to MySQL at: {}", mysqlUrl);
                HikariDataSource ds = new HikariDataSource();
                ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
                ds.setJdbcUrl(mysqlUrl);
                ds.setUsername(mysqlUsername);
                ds.setPassword(mysqlPassword);
                ds.setMaximumPoolSize(10);
                ds.setConnectionTimeout(3000);
                // Test connection
                ds.getConnection().close();
                log.info("Successfully connected to MySQL database!");
                return ds;
            } catch (Exception ex) {
                log.warn("MySQL connection attempt failed ({}). Gracefully falling back to in-memory H2 database.", ex.getMessage());
            }
        } else {
            log.warn("MySQL service not detected on localhost:3306. Automatically activating high-performance in-memory database with MySQL dialect compatibility.");
        }

        // Resilient Fallback to H2 in-memory
        HikariDataSource fallbackDs = new HikariDataSource();
        fallbackDs.setDriverClassName("org.h2.Driver");
        fallbackDs.setJdbcUrl("jdbc:h2:mem:ambulance_booking_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL");
        fallbackDs.setUsername("sa");
        fallbackDs.setPassword("");
        fallbackDs.setMaximumPoolSize(10);
        return fallbackDs;
    }

    private boolean isPortAvailable(String host, int port, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
