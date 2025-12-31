package com.messagerie.util;

import org.apache.commons.dbcp2.BasicDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;

public class DatabaseConnectionPool {
    private static BasicDataSource dataSource;

    static {
        try (InputStream input = DatabaseConnectionPool.class
                .getClassLoader().getResourceAsStream("db.properties")) {

            Properties prop = new Properties();
            prop.load(input);

            dataSource = new BasicDataSource();
            dataSource.setDriverClassName(prop.getProperty("db.driver"));
            dataSource.setUrl(prop.getProperty("db.url"));
            dataSource.setUsername(prop.getProperty("db.username"));
            dataSource.setPassword(prop.getProperty("db.password"));

            dataSource.setInitialSize(Integer.parseInt(
                prop.getProperty("db.pool.initialSize", "5")));
            dataSource.setMaxTotal(Integer.parseInt(
                prop.getProperty("db.pool.maxActive", "20"))); // CORRIGÉ : maxActive
            dataSource.setMaxIdle(Integer.parseInt(
                prop.getProperty("db.pool.maxIdle", "10")));
            dataSource.setMinIdle(Integer.parseInt(
                prop.getProperty("db.pool.minIdle", "5")));

        } catch (Exception e) {
            System.err.println("Erreur d'initialisation du pool de connexions: " + e.getMessage());
            throw new ExceptionInInitializerError(
                "Failed to initialize database connection pool: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void close() throws SQLException {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}