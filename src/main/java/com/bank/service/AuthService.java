package com.bank.service;

import com.bank.dao.AccountDAO;
import com.bank.dao.UserDAO;
import com.bank.model.User;
import java.sql.SQLException;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();
    private final AccountDAO accountDAO = new AccountDAO();

    public User login(String username, String password) {
        User user = userDAO.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public User register(String username, String password, String accountType) throws Exception {
        if (userDAO.findByUsername(username) != null) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Use direct transaction or simple JDBC calls
        int userId = userDAO.createUser(username, password, "CUSTOMER");
        
        // Generate a random 10-digit account number
        String accountNumber = String.valueOf(1000000000L + userId);
        accountDAO.createAccount(userId, accountNumber, accountType);

        return userDAO.findById(userId);
    }
}
