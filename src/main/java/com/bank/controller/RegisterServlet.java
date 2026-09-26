package com.bank.controller;

import com.bank.model.User;
import com.bank.service.AuthService;
import com.bank.util.JsonUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/auth/register")
public class RegisterServlet extends HttpServlet {

    private final AuthService authService = new AuthService();
    private final com.bank.service.AccountService accountService = new com.bank.service.AccountService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            RegisterRequest reqObj;
            try {
                reqObj = JsonUtil.fromRequest(request, RegisterRequest.class);
            } catch (Exception e) {
                reqObj = new RegisterRequest();
            }
            if (reqObj == null) {
                reqObj = new RegisterRequest();
            }

            if (reqObj.username == null || reqObj.password == null) {
                reqObj.username = request.getParameter("username");
                reqObj.password = request.getParameter("password");
                reqObj.accountType = request.getParameter("accountType");
            }

            if (reqObj.username == null || reqObj.password == null ||
                    reqObj.username.isBlank() || reqObj.password.isBlank()) {
                Map<String, Object> err = new HashMap<>();
                err.put("success", false);
                err.put("message", "Username and password are required");
                JsonUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, err);
                return;
            }

            if (reqObj.accountType == null || reqObj.accountType.isBlank()) {
                reqObj.accountType = "SAVINGS";
            }

            User user = authService.register(reqObj.username, reqObj.password, reqObj.accountType);
            com.bank.model.Account account = accountService.getAccountByUserId(user.getId());

            Map<String, Object> data = new HashMap<>();
            data.put("success", true);
            data.put("message", "Registration successful");
            data.put("user", user);
            data.put("account", account);
            data.put("accountNumber", account != null ? account.getAccountNumber() : "");
            JsonUtil.sendResponse(response, HttpServletResponse.SC_CREATED, data);

        } catch (IllegalArgumentException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            JsonUtil.sendResponse(response, HttpServletResponse.SC_CONFLICT, err);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Registration failed: " + e.getMessage());
            JsonUtil.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, err);
        }
    }

    private static class RegisterRequest {
        String username;
        String password;
        String accountType;
    }
}
