package org.example;

import io.github.cdimascio.dotenv.Dotenv;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class Database {
    private static final Dotenv dotenv = Dotenv.load();
    private static final String URL = dotenv.get("DB_URL");
    private static final String USER = dotenv.get("DB_USER");
    private static final String PASSWORD = dotenv.get("DB_PASSWORD");
    private static HikariDataSource dataSource = null;

    private Database() {}

    public static Connection getConnection() {
        if (dataSource == null) createConnection();
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            System.err.println("Error getting connection: " + e.getMessage());
            return null;
        }
    }

    private static void createConnection() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(URL);
            config.setUsername(USER);
            config.setPassword(PASSWORD);
            config.setMaximumPoolSize(10);
            dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            System.err.println("Error creating connection pool: " + e.getMessage());
        }
    }

    public static void closeConnection() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    public static void rollback() {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                conn.rollback();
            }
        } catch (SQLException e) {
            System.err.println("Error rolling back: " + e.getMessage());
        }
    }
}
