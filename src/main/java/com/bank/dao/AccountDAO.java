package com.bank.dao;

import com.bank.model.Account;
import com.bank.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {

    public Account findByUserId(int userId) {
        String sql = """
                SELECT id, user_id, account_number, account_type, balance
                FROM accounts
                WHERE user_id = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new Account(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("account_number"),
                            rs.getString("account_type"),
                            rs.getBigDecimal("balance")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Account findByUserIdForUpdate(Connection connection, int userId) throws SQLException {
        String sql = """
                SELECT id, user_id, account_number, account_type, balance
                FROM accounts
                WHERE user_id = ?
                FOR UPDATE
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new Account(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("account_number"),
                            rs.getString("account_type"),
                            rs.getBigDecimal("balance")
                    );
                }
            }
        }
        return null;
    }

    public Account findByAccountNumber(String accountNumber) {
        String sql = """
                SELECT id, user_id, account_number, account_type, balance
                FROM accounts
                WHERE account_number = ?
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, accountNumber);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new Account(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("account_number"),
                            rs.getString("account_type"),
                            rs.getBigDecimal("balance")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Account findByAccountNumberForUpdate(Connection connection, String accountNumber) throws SQLException {
        String sql = """
                SELECT id, user_id, account_number, account_type, balance
                FROM accounts
                WHERE account_number = ?
                FOR UPDATE
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, accountNumber);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new Account(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("account_number"),
                            rs.getString("account_type"),
                            rs.getBigDecimal("balance")
                    );
                }
            }
        }
        return null;
    }

    public void createAccount(int userId, String accountNumber, String accountType) throws SQLException {
        String sql = """
                INSERT INTO accounts(user_id, account_number, account_type, balance)
                VALUES (?, ?, ?, 0.00)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setString(2, accountNumber);
            statement.setString(3, accountType != null ? accountType.toUpperCase() : "SAVINGS");
            statement.executeUpdate();
        }
    }

    public void updateBalance(Connection connection, int accountId, BigDecimal balance) throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBigDecimal(1, balance);
            statement.setInt(2, accountId);
            statement.executeUpdate();
        }
    }

    public List<Account> findAll() {
        String sql = "SELECT id, user_id, account_number, account_type, balance FROM accounts";
        List<Account> list = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                list.add(new Account(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("account_number"),
                        rs.getString("account_type"),
                        rs.getBigDecimal("balance")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
}
