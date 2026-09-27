package com.bank.service;

import com.bank.dao.AccountDAO;
import com.bank.dao.UserDAO;
import com.bank.model.User;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.SQLException;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();
    private final AccountDAO accountDAO = new AccountDAO();

    public User login(String username, String password) {
        User user = userDAO.findByUsername(username);
        if (user != null && user.getPassword() != null) {
            String storedPassword = user.getPassword();

            // 1. Verify against BCrypt hash
            if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
                try {
                    if (BCrypt.checkpw(password, storedPassword)) {
                        return user;
                    }
                } catch (IllegalArgumentException ignored) {
                }
            } else {
                // 2. Legacy plain-text fallback (for existing database accounts)
                if (storedPassword.equals(password)) {
                    // Transparently upgrade legacy plain-text password to BCrypt hash in DB
                    try {
                        String newHashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                        userDAO.updatePassword(user.getId(), newHashedPassword);
                        user.setPassword(newHashedPassword);
                    } catch (Exception ignored) {
                    }
                    return user;
                }
            }
        }
        return null;
    }

    public User register(String username, String password, String accountType) throws Exception {
        if (userDAO.findByUsername(username) != null) {
            throw new IllegalArgumentException("Username already exists");
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // Use direct transaction or simple JDBC calls
        int userId = userDAO.createUser(username, hashedPassword, "CUSTOMER");
        
        // Generate a random 10-digit account number
        String accountNumber = String.valueOf(1000000000L + userId);
        accountDAO.createAccount(userId, accountNumber, accountType);

        return userDAO.findById(userId);
    }
}
