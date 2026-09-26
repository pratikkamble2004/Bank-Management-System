package com.bank.controller;

import com.bank.model.Account;
import com.bank.model.User;
import com.bank.service.AccountService;
import com.bank.util.JsonUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/account/deposit")
public class DepositServlet extends HttpServlet {

    private final AccountService accountService = new AccountService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
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

        try {
            DepositRequest reqObj;
            try {
                reqObj = JsonUtil.fromRequest(request, DepositRequest.class);
            } catch (Exception e) {
                reqObj = new DepositRequest();
            }
            if (reqObj == null) {
                reqObj = new DepositRequest();
            }

            if (reqObj.amount == null) {
                String amountParam = request.getParameter("amount");
                if (amountParam != null) {
                    reqObj.amount = new BigDecimal(amountParam);
                }
                reqObj.description = request.getParameter("description");
            }

            if (reqObj.amount == null || reqObj.amount.compareTo(BigDecimal.ZERO) <= 0) {
                Map<String, Object> err = new HashMap<>();
                err.put("success", false);
                err.put("message", "Invalid amount. Must be greater than zero");
                JsonUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, err);
                return;
            }

            String ref = accountService.deposit(user.getId(), reqObj.amount, reqObj.description);

            Account updatedAccount = accountService.getAccountByUserId(user.getId());
            Map<String, Object> data = new HashMap<>();
            data.put("success", true);
            data.put("message", "Deposit successful");
            data.put("balance", updatedAccount.getBalance());
            data.put("reference", ref);
            data.put("amount", reqObj.amount);

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
            err.put("message", "Deposit failed");
            JsonUtil.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, err);
        }
    }

    private static class DepositRequest {
        BigDecimal amount;
        String description;
    }
}
