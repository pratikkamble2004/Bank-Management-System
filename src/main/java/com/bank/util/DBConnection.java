package com.bank.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DBConnection {

    private static String url;
    private static String user;
    private static String password;

    static {
        Properties props = new Properties();
        try (InputStream input = DBConnection.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (input == null) {
                throw new IllegalStateException("Database configuration file 'db.properties' not found in classpath");
            }
            props.load(input);
            url = props.getProperty("db.url");
            user = props.getProperty("db.username");
            password = props.getProperty("db.password");

            if (url == null || user == null) {
                throw new IllegalStateException("Database configuration 'db.url' or 'db.username' is missing in db.properties");
            }

            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load database configuration from 'db.properties'", e);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("MySQL JDBC driver 'com.mysql.cj.jdbc.Driver' not found", e);
        }
    }

    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
