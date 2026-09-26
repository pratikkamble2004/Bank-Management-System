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
                url = "jdbc:mysql://localhost:3306/bank_management?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
                user = "root";
                password = "12345";
            } else {
                props.load(input);
                url = props.getProperty("db.url");
                user = props.getProperty("db.username");
                password = props.getProperty("db.password");
            }
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            url = "jdbc:mysql://localhost:3306/bank_management?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
            user = "root";
            password = "12345";
        }
    }

    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
