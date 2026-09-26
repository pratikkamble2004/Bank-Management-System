package com.bank.controller;

import com.bank.model.Transaction;
import com.bank.model.User;
import com.bank.service.TransactionService;
import com.bank.util.JsonUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/account/transactions")
public class TransactionServlet extends HttpServlet {

    private final TransactionService transactionService = new TransactionService();

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

        try {
            List<Transaction> transactions = transactionService.getTransactionHistory(user.getId());
            JsonUtil.sendResponse(response, HttpServletResponse.SC_OK, transactions);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Failed to retrieve transaction history");
            JsonUtil.sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, err);
        }
    }
}
