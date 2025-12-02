package com.deerbank.controller;

import com.deerbank.dto.*;
import com.deerbank.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = "*")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping("/create")
    public ResponseEntity<?> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        try {
            AccountResponse response = accountService.createAccount(request);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Account created successfully");
            result.put("data", response);

            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to create account: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@Valid @RequestBody DepositRequest request) {
        try {
            TransactionResponse response = accountService.deposit(request);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", response.getMessage());
            result.put("data", response);

            return ResponseEntity.status(HttpStatus.OK).body(result);

        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to process deposit: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/withdrawal")
    public ResponseEntity<?> withdraw(@Valid @RequestBody WithdrawalRequest request) {
        try {
            TransactionResponse response = accountService.withdrawal(request);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", response.getMessage());
            result.put("data", response);

            return ResponseEntity.status(HttpStatus.OK).body(result);

        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to process withdrawal: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/transactions")
    public ResponseEntity<?> getTransactions(@Valid @RequestBody GetTransactionsRequest request) {
        try {
            List<TransactionHistoryDTO> transactions = accountService.getTransactions(request);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Transactions retrieved successfully");
            result.put("count", transactions.size());
            result.put("data", transactions);

            return ResponseEntity.status(HttpStatus.OK).body(result);

        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.OK).body(error);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Failed to retrieve transactions: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

}
