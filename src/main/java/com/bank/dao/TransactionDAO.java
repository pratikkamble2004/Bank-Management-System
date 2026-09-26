package com.bank.dao;

import com.bank.model.Transaction;
import com.bank.util.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    public List<Transaction> findByAccountId(int accountId) {
        String sql = """
                SELECT id, account_id, transaction_type, amount, description, reference, created_at
                FROM transactions
                WHERE account_id = ?
                ORDER BY created_at DESC
                """;
        List<Transaction> list = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, accountId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    list.add(new Transaction(
                            rs.getInt("id"),
                            rs.getInt("account_id"),
                            rs.getString("transaction_type"),
                            rs.getBigDecimal("amount"),
                            rs.getString("description"),
                            rs.getString("reference"),
                            rs.getTimestamp("created_at")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<Transaction> findByUserId(int userId) {
        String sql = """
                SELECT t.id, t.account_id, t.transaction_type, t.amount, t.description, t.reference, t.created_at
                FROM transactions t
                JOIN accounts a ON a.id = t.account_id
                WHERE a.user_id = ?
                ORDER BY t.created_at DESC
                """;
        List<Transaction> list = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    list.add(new Transaction(
                            rs.getInt("id"),
                            rs.getInt("account_id"),
                            rs.getString("transaction_type"),
                            rs.getBigDecimal("amount"),
                            rs.getString("description"),
                            rs.getString("reference"),
                            rs.getTimestamp("created_at")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public void createTransaction(Connection conn, int accountId, String type, BigDecimal amount,
                                  String description, String reference) throws SQLException {
        String sql = """
                INSERT INTO transactions (account_id, transaction_type, amount, description, reference)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, accountId);
            statement.setString(2, type);
            statement.setBigDecimal(3, amount);
            statement.setString(4, description);
            statement.setString(5, reference);
            statement.executeUpdate();
        }
    }

    public List<Transaction> findAll() {
        String sql = """
                SELECT id, account_id, transaction_type, amount, description, reference, created_at
                FROM transactions
                ORDER BY created_at DESC
                """;
        List<Transaction> list = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                list.add(new Transaction(
                        rs.getInt("id"),
                        rs.getInt("account_id"),
                        rs.getString("transaction_type"),
                        rs.getBigDecimal("amount"),
                        rs.getString("description"),
                        rs.getString("reference"),
                        rs.getTimestamp("created_at")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
}
