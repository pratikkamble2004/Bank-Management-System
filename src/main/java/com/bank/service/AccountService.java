package com.bank.service;

import com.bank.dao.AccountDAO;
import com.bank.dao.TransactionDAO;
import com.bank.model.Account;
import com.bank.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

public class AccountService {

    private final AccountDAO accountDAO = new AccountDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    public Account getAccountByUserId(int userId) {
        return accountDAO.findByUserId(userId);
    }

    public Account getAccountByAccountNumber(String accountNumber) {
        return accountDAO.findByAccountNumber(accountNumber);
    }

    public String deposit(int userId, BigDecimal amount, String description) throws Exception {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        try (Connection connection = DBConnection.getConnection()) {
            try {
                connection.setAutoCommit(false);

                Account account = accountDAO.findByUserIdForUpdate(connection, userId);
                if (account == null) {
                    throw new IllegalArgumentException("Account not found");
                }

                BigDecimal newBalance = account.getBalance().add(amount);
                accountDAO.updateBalance(connection, account.getId(), newBalance);

                String ref = "REF-" + System.currentTimeMillis();
                transactionDAO.createTransaction(
                        connection,
                        account.getId(),
                        "DEPOSIT",
                        amount,
                        description != null && !description.isBlank() ? description : "Cash Deposit",
                        ref
                );

                connection.commit();
                return ref;
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        }
    }

    public String withdraw(int userId, BigDecimal amount, String description) throws Exception {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        try (Connection connection = DBConnection.getConnection()) {
            try {
                connection.setAutoCommit(false);

                Account account = accountDAO.findByUserIdForUpdate(connection, userId);
                if (account == null) {
                    throw new IllegalArgumentException("Account not found");
                }

                if (account.getBalance().compareTo(amount) < 0) {
                    throw new IllegalArgumentException("Insufficient balance");
                }

                BigDecimal newBalance = account.getBalance().subtract(amount);
                accountDAO.updateBalance(connection, account.getId(), newBalance);

                String ref = "REF-" + System.currentTimeMillis();
                transactionDAO.createTransaction(
                        connection,
                        account.getId(),
                        "WITHDRAW",
                        amount,
                        description != null && !description.isBlank() ? description : "Cash Withdrawal",
                        ref
                );

                connection.commit();
                return ref;
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        }
    }

    public String transfer(int fromUserId, String toAccountNumber, BigDecimal amount, String description) throws Exception {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        try (Connection connection = DBConnection.getConnection()) {
            try {
                connection.setAutoCommit(false);

                Account sender = accountDAO.findByUserIdForUpdate(connection, fromUserId);
                if (sender == null) {
                    throw new IllegalArgumentException("Sender account not found");
                }

                Account receiver = accountDAO.findByAccountNumberForUpdate(connection, toAccountNumber);
                if (receiver == null) {
                    throw new IllegalArgumentException("Receiver account not found");
                }

                if (sender.getId() == receiver.getId()) {
                    throw new IllegalArgumentException("Cannot transfer to your own account");
                }

                if (sender.getBalance().compareTo(amount) < 0) {
                    throw new IllegalArgumentException("Insufficient balance for transfer");
                }

                // Perform deduction and addition
                accountDAO.updateBalance(connection, sender.getId(), sender.getBalance().subtract(amount));
                accountDAO.updateBalance(connection, receiver.getId(), receiver.getBalance().add(amount));

                String ref = "TXN-" + System.currentTimeMillis();
                String desc = description != null && !description.isBlank() ? description : "Fund Transfer";

                // Log outbound transaction
                transactionDAO.createTransaction(
                        connection,
                        sender.getId(),
                        "TRANSFER_OUT",
                        amount,
                        desc + " to " + receiver.getAccountNumber(),
                        ref
                );

                // Log inbound transaction
                transactionDAO.createTransaction(
                        connection,
                        receiver.getId(),
                        "TRANSFER_IN",
                        amount,
                        desc + " from " + sender.getAccountNumber(),
                        ref
                );

                connection.commit();
                return ref;
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        }
    }

    public String depositToAccount(String accountNumber, BigDecimal amount, String description) throws Exception {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        try (Connection connection = DBConnection.getConnection()) {
            try {
                connection.setAutoCommit(false);

                Account account = accountDAO.findByAccountNumberForUpdate(connection, accountNumber);
                if (account == null) {
                    throw new IllegalArgumentException("Customer account not found: " + accountNumber);
                }

                BigDecimal newBalance = account.getBalance().add(amount);
                accountDAO.updateBalance(connection, account.getId(), newBalance);

                String ref = "DEP-" + System.currentTimeMillis();
                transactionDAO.createTransaction(
                        connection,
                        account.getId(),
                        "DEPOSIT",
                        amount,
                        description != null && !description.isBlank() ? description : "Branch Counter Deposit",
                        ref
                );

                connection.commit();
                return ref;
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        }
    }

    public String transferBetweenAccounts(String fromAccountNumber, String toAccountNumber, BigDecimal amount, String description) throws Exception {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        if (fromAccountNumber == null || fromAccountNumber.equalsIgnoreCase(toAccountNumber)) {
            throw new IllegalArgumentException("Source and Destination accounts cannot be the same");
        }

        try (Connection connection = DBConnection.getConnection()) {
            try {
                connection.setAutoCommit(false);

                Account sender = accountDAO.findByAccountNumberForUpdate(connection, fromAccountNumber);
                if (sender == null) {
                    throw new IllegalArgumentException("Source account not found: " + fromAccountNumber);
                }

                Account receiver = accountDAO.findByAccountNumberForUpdate(connection, toAccountNumber);
                if (receiver == null) {
                    throw new IllegalArgumentException("Destination account not found: " + toAccountNumber);
                }

                if (sender.getBalance().compareTo(amount) < 0) {
                    throw new IllegalArgumentException("Insufficient balance in source account");
                }

                accountDAO.updateBalance(connection, sender.getId(), sender.getBalance().subtract(amount));
                accountDAO.updateBalance(connection, receiver.getId(), receiver.getBalance().add(amount));

                String ref = "TXN-" + System.currentTimeMillis();
                String desc = description != null && !description.isBlank() ? description : "Inter-Account Transfer";

                transactionDAO.createTransaction(
                        connection,
                        sender.getId(),
                        "TRANSFER_OUT",
                        amount,
                        desc + " to " + receiver.getAccountNumber(),
                        ref
                );

                transactionDAO.createTransaction(
                        connection,
                        receiver.getId(),
                        "TRANSFER_IN",
                        amount,
                        desc + " from " + sender.getAccountNumber(),
                        ref
                );

                connection.commit();
                return ref;
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        }
    }
}
