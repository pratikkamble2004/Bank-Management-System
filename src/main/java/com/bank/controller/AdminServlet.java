package com.bank.controller;

import com.bank.model.Account;
import com.bank.model.Transaction;
import com.bank.model.User;
import com.bank.dao.UserDAO;
import com.bank.dao.AccountDAO;
import com.bank.dao.TransactionDAO;
import com.bank.util.JsonUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/admin/*")
public class AdminServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final AccountDAO accountDAO = new AccountDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    private final com.bank.service.AccountService accountService = new com.bank.service.AccountService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;

        if (currentUser == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Not authenticated");
            JsonUtil.sendResponse(response, HttpServletResponse.SC_UNAUTHORIZED, err);
            return;
        }

        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Access denied. Admin role required");
            JsonUtil.sendResponse(response, HttpServletResponse.SC_FORBIDDEN, err);
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            List<User> users = userDAO.findAll();
            List<Account> accounts = accountDAO.findAll();
            List<Transaction> txns = transactionDAO.findAll();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", users.size());
            stats.put("totalAccounts", accounts.size());
            stats.put("totalTransactions", txns.size());
            
            JsonUtil.sendResponse(response, HttpServletResponse.SC_OK, stats);
            return;
        }

        switch (pathInfo) {
            case "/users" -> {
                List<User> users = userDAO.findAll();
                JsonUtil.sendResponse(response, HttpServletResponse.SC_OK, users);
            }
            case "/accounts" -> {
                List<Account> accounts = accountDAO.findAll();
                JsonUtil.sendResponse(response, HttpServletResponse.SC_OK, accounts);
            }
            case "/transactions" -> {
                List<Transaction> txns = transactionDAO.findAll();
                JsonUtil.sendResponse(response, HttpServletResponse.SC_OK, txns);
            }
            default -> {
                Map<String, Object> err = new HashMap<>();
                err.put("success", false);
                err.put("message", "Endpoint not found");
                JsonUtil.sendResponse(response, HttpServletResponse.SC_NOT_FOUND, err);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("user") : null;

        if (currentUser == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Not authenticated");
            JsonUtil.sendResponse(response, HttpServletResponse.SC_UNAUTHORIZED, err);
            return;
        }

        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Access denied. Admin role required");
            JsonUtil.sendResponse(response, HttpServletResponse.SC_FORBIDDEN, err);
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Endpoint not found");
            JsonUtil.sendResponse(response, HttpServletResponse.SC_NOT_FOUND, err);
            return;
        }

        if ("/deposit".equals(pathInfo)) {
            try {
                AdminDepositRequest req = JsonUtil.fromRequest(request, AdminDepositRequest.class);
                if (req == null || req.targetAccountNumber == null || req.amount == null || req.amount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                    Map<String, Object> err = new HashMap<>();
                    err.put("success", false);
                    err.put("message", "Valid customer account number and amount greater than zero are required");
                    JsonUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, err);
                    return;
                }

                String ref = accountService.depositToAccount(
                        req.targetAccountNumber.trim(),
                        req.amount,
                        req.description != null && !req.description.isBlank() ? req.description.trim() : "Branch Counter Deposit"
                );

                Account acc = accountService.getAccountByAccountNumber(req.targetAccountNumber.trim());
                String holderName = "Customer";
                if (acc != null) {
                    User u = userDAO.findById(acc.getUserId());
                    if (u != null) {
                        holderName = u.getUsername();
                    }
                }

                Map<String, Object> data = new HashMap<>();
                data.put("success", true);
                data.put("message", "Cash deposit completed successfully");
                data.put("reference", ref);
                data.put("balance", acc != null ? acc.getBalance() : java.math.BigDecimal.ZERO);
                data.put("targetAccountNumber", req.targetAccountNumber.trim());
                data.put("accountHolder", holderName);
                data.put("amount", req.amount);

                JsonUtil.sendResponse(response, HttpServletResponse.SC_OK, data);
            } catch (IllegalArgumentException e) {
                Map<String, Object> err = new HashMap<>();
                err.put("success", false);
                err.put("message", e.getMessage());
                JsonUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, err);
            } catch (Exception e) {
                e.printStackTrace();
                Map<String, Object> err = new HashMap<>();
                err.put("success", false);
                err.put("message", "Deposit failed: " + e.getMessage());
                JsonUtil.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, err);
            }
            return;
        }

        if ("/transfer".equals(pathInfo)) {
            try {
                AdminTransferRequest req = JsonUtil.fromRequest(request, AdminTransferRequest.class);
                if (req == null || req.fromAccountNumber == null || req.toAccountNumber == null || req.amount == null || req.amount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                    Map<String, Object> err = new HashMap<>();
                    err.put("success", false);
                    err.put("message", "Source account, destination account, and valid amount are required");
                    JsonUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, err);
                    return;
                }

                String ref = accountService.transferBetweenAccounts(
                        req.fromAccountNumber.trim(),
                        req.toAccountNumber.trim(),
                        req.amount,
                        req.description != null && !req.description.isBlank() ? req.description.trim() : "Inter-Account Transfer"
                );

                Map<String, Object> data = new HashMap<>();
                data.put("success", true);
                data.put("message", "Transfer executed successfully");
                data.put("reference", ref);
                data.put("fromAccountNumber", req.fromAccountNumber.trim());
                data.put("toAccountNumber", req.toAccountNumber.trim());
                data.put("amount", req.amount);

                JsonUtil.sendResponse(response, HttpServletResponse.SC_OK, data);
            } catch (IllegalArgumentException e) {
                Map<String, Object> err = new HashMap<>();
                err.put("success", false);
                err.put("message", e.getMessage());
                JsonUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, err);
            } catch (Exception e) {
                e.printStackTrace();
                Map<String, Object> err = new HashMap<>();
                err.put("success", false);
                err.put("message", "Transfer failed: " + e.getMessage());
                JsonUtil.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, err);
            }
            return;
        }

        Map<String, Object> err = new HashMap<>();
        err.put("success", false);
        err.put("message", "Endpoint not found");
        JsonUtil.sendResponse(response, HttpServletResponse.SC_NOT_FOUND, err);
    }

    private static class AdminDepositRequest {
        String targetAccountNumber;
        java.math.BigDecimal amount;
        String description;
    }

    private static class AdminTransferRequest {
        String fromAccountNumber;
        String toAccountNumber;
        java.math.BigDecimal amount;
        String description;
    }
}
