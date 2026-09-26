package com.bank.controller;

import com.bank.model.Account;
import com.bank.model.User;
import com.bank.service.AccountService;
import com.bank.service.AuthService;
import com.bank.util.JsonUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(urlPatterns = {"/api/auth/login", "/api/auth/me"})
public class LoginServlet extends HttpServlet {

    private final AuthService authService = new AuthService();
    private final AccountService accountService = new AccountService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String path = request.getServletPath();
        if (!"/api/auth/login".equals(path)) {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        try {
            // Read login request (from JSON first, then parameter fallback)
            LoginRequest reqObj;
            try {
                reqObj = JsonUtil.fromRequest(request, LoginRequest.class);
            } catch (Exception e) {
                reqObj = new LoginRequest();
            }
            if (reqObj == null) {
                reqObj = new LoginRequest();
            }

            if (reqObj.username == null || reqObj.password == null) {
                reqObj.username = request.getParameter("username");
                reqObj.password = request.getParameter("password");
            }

            if (reqObj.username == null || reqObj.password == null ||
                    reqObj.username.isBlank() || reqObj.password.isBlank()) {
                Map<String, Object> err = new HashMap<>();
                err.put("success", false);
                err.put("message", "Username and password are required");
                JsonUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, err);
                return;
            }

            User user = authService.login(reqObj.username, reqObj.password);

            if (user != null) {
                // Set session
                HttpSession session = request.getSession(true);
                session.setAttribute("user", user);

                // Find associated account (for CUSTOMER roles)
                Account account = accountService.getAccountByUserId(user.getId());
                String accountNumber = account != null ? account.getAccountNumber() : "";
                String accountType = account != null ? account.getAccountType() : "";
                String balance = account != null ? account.getBalance().toPlainString() : "0.00";

                Map<String, Object> data = new HashMap<>();
                data.put("success", true);
                data.put("message", "Login successful");
                
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("username", user.getUsername());
                userMap.put("role", user.getRole());
                userMap.put("accountNumber", accountNumber);
                userMap.put("accountType", accountType);
                userMap.put("balance", balance);
                data.put("user", userMap);

                JsonUtil.sendResponse(response, HttpServletResponse.SC_OK, data);
            } else {
                Map<String, Object> err = new HashMap<>();
                err.put("success", false);
                err.put("message", "Invalid username or password");
                JsonUtil.sendResponse(response, HttpServletResponse.SC_UNAUTHORIZED, err);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Internal server error: " + e.getMessage());
            JsonUtil.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, err);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String path = request.getServletPath();
        if (!"/api/auth/me".equals(path)) {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user != null) {
            Account account = accountService.getAccountByUserId(user.getId());
            String accountNumber = account != null ? account.getAccountNumber() : "";
            String accountType = account != null ? account.getAccountType() : "";
            String balance = account != null ? account.getBalance().toPlainString() : "0.00";

            Map<String, Object> data = new HashMap<>();
            data.put("success", true);

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("username", user.getUsername());
            userMap.put("role", user.getRole());
            userMap.put("accountNumber", accountNumber);
            userMap.put("accountType", accountType);
            userMap.put("balance", balance);
            data.put("user", userMap);

            JsonUtil.sendResponse(response, HttpServletResponse.SC_OK, data);
        } else {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Not authenticated");
            JsonUtil.sendResponse(response, HttpServletResponse.SC_UNAUTHORIZED, err);
        }
    }

    private static class LoginRequest {
        String username;
        String password;
    }
}
