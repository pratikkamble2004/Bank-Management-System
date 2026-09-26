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

@WebServlet("/api/account/transfer")
public class TransferServlet extends HttpServlet {

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
            TransferRequest reqObj;
            try {
                reqObj = JsonUtil.fromRequest(request, TransferRequest.class);
            } catch (Exception e) {
                reqObj = new TransferRequest();
            }
            if (reqObj == null) {
                reqObj = new TransferRequest();
            }

            if (reqObj.toAccountNumber == null || reqObj.amount == null) {
                reqObj.toAccountNumber = request.getParameter("toAccountNumber");
                String amountParam = request.getParameter("amount");
                if (amountParam != null) {
                    reqObj.amount = new BigDecimal(amountParam);
                }
                reqObj.description = request.getParameter("description");
            }

            if (reqObj.toAccountNumber == null || reqObj.toAccountNumber.isBlank() ||
                    reqObj.amount == null || reqObj.amount.compareTo(BigDecimal.ZERO) <= 0) {
                Map<String, Object> err = new HashMap<>();
                err.put("success", false);
                err.put("message", "Receiver account and valid amount greater than zero are required");
                JsonUtil.sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, err);
                return;
            }

            String ref = accountService.transfer(
                    user.getId(),
                    reqObj.toAccountNumber,
                    reqObj.amount,
                    reqObj.description
            );

            Account updatedAccount = accountService.getAccountByUserId(user.getId());
            Account receiverAccount = accountService.getAccountByAccountNumber(reqObj.toAccountNumber);
            String receiverName = "";
            if (receiverAccount != null) {
                com.bank.dao.UserDAO userDAO = new com.bank.dao.UserDAO();
                com.bank.model.User receiverUser = userDAO.findById(receiverAccount.getUserId());
                if (receiverUser != null) {
                    receiverName = receiverUser.getUsername();
                }
            }

            Map<String, Object> data = new HashMap<>();
            data.put("success", true);
            data.put("message", "Transfer successful");
            data.put("balance", updatedAccount.getBalance());
            data.put("reference", ref);
            data.put("amount", reqObj.amount);
            data.put("toAccountNumber", reqObj.toAccountNumber);
            data.put("recipientName", receiverName);

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
    }

    private static class TransferRequest {
        String toAccountNumber;
        BigDecimal amount;
        String description;
    }
}
