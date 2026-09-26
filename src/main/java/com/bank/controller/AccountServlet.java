package com.bank.controller;

import com.bank.model.Account;
import com.bank.model.User;
import com.bank.service.AccountService;
import com.bank.util.JsonUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(urlPatterns = {"/api/account", "/api/account/balance", "/api/account/lookup"})
public class AccountServlet extends HttpServlet {

    private final AccountService accountService = new AccountService();
    private final com.bank.dao.UserDAO userDAO = new com.bank.dao.UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Not authenticated");
            JsonUtil.sendResponse(response, HttpServletResponse.SC_UNAUTHORIZED, err);
            return;
        }

        String path = request.getServletPath();

        // Recipient account lookup
        if ("/api/account/lookup".equals(path)) {
            String accNum = request.getParameter("accountNumber");
            if (accNum == null || accNum.isBlank()) {
                Map<String, Object> err = new HashMap<>();
                err.put("success", false);
                err.put("found", false);
                err.put("message", "Account number is required");
                JsonUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, err);
                return;
            }

            Account targetAccount = accountService.getAccountByAccountNumber(accNum.trim());
            if (targetAccount == null) {
                Map<String, Object> err = new HashMap<>();
                err.put("success", false);
                err.put("found", false);
                err.put("message", "Recipient account not found");
                JsonUtil.sendResponse(response, HttpServletResponse.SC_NOT_FOUND, err);
                return;
            }

            User targetUser = userDAO.findById(targetAccount.getUserId());
            String holderName = targetUser != null ? targetUser.getUsername() : "Account Holder";

            Map<String, Object> data = new HashMap<>();
            data.put("success", true);
            data.put("found", true);
            data.put("accountNumber", targetAccount.getAccountNumber());
            data.put("accountType", targetAccount.getAccountType());
            data.put("accountHolder", holderName);
            JsonUtil.sendResponse(response, HttpServletResponse.SC_OK, data);
            return;
        }

        Account account = accountService.getAccountByUserId(user.getId());
        if (account == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Account not found");
            JsonUtil.sendResponse(response, HttpServletResponse.SC_NOT_FOUND, err);
            return;
        }

        if ("/api/account/balance".equals(path)) {
            Map<String, Object> data = new HashMap<>();
            data.put("success", true);
            data.put("balance", account.getBalance());
            JsonUtil.sendResponse(response, HttpServletResponse.SC_OK, data);
        } else {
            Map<String, Object> data = new HashMap<>();
            data.put("success", true);
            data.put("id", account.getId());
            data.put("userId", account.getUserId());
            data.put("accountNumber", account.getAccountNumber());
            data.put("accountType", account.getAccountType());
            data.put("balance", account.getBalance());
            JsonUtil.sendResponse(response, HttpServletResponse.SC_OK, data);
        }
    }
}
