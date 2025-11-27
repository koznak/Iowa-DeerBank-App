package com.deerbank.controller;

import com.deerbank.dto.AccountResponse;
import com.deerbank.dto.CreateAccountRequest;
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
    
    @GetMapping("/{accountId}")
    public ResponseEntity<?> getAccountById(@PathVariable Integer accountId) {
        try {
            AccountResponse response = accountService.getAccountById(accountId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Account retrieved successfully");
            result.put("data", response);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
    
    @GetMapping
    public ResponseEntity<?> getAllAccounts() {
        try {
            List<AccountResponse> accounts = accountService.getAllAccounts();

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Accounts retrieved successfully");
            result.put("data", accounts);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
     }

}
