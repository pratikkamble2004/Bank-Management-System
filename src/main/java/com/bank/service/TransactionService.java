package com.bank.service;

import com.bank.dao.TransactionDAO;
import com.bank.model.Transaction;
import java.util.List;

public class TransactionService {

    private final TransactionDAO transactionDAO = new TransactionDAO();

    public List<Transaction> getTransactionHistory(int userId) {
        return transactionDAO.findByUserId(userId);
    }

    public List<Transaction> getAllTransactions() {
        return transactionDAO.findAll();
    }
}
