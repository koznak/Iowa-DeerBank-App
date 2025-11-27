package com.deerbank.service;

import com.deerbank.dto.AccountResponse;
import com.deerbank.dto.CreateAccountRequest;
import com.deerbank.entity.Account;
import com.deerbank.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        // Generate unique account number
        String accountNo = generateAccountNumber();

        // Create new account entity
        Account account = new Account();
        account.setAccountNo(accountNo);
        account.setAccountType(request.getAccountType());
        account.setBalance(request.getInitialBalance());
        account.setUserUserId(request.getUserId());
        account.setCredentialsId(request.getCredentialsId());
        account.setInterestRate(request.getInterestRate() != null ? request.getInterestRate() : 0);
        account.setOverdraftLimit(request.getOverdraftLimit() != null ? request.getOverdraftLimit() : 0);
        account.setStatus("ACTIVE");
        account.setOpenedDate(LocalDateTime.now());

        // Save account
        Account savedAccount = accountRepository.save(account);

        // Convert to response DTO
        return convertToResponse(savedAccount);
    }

    private String generateAccountNumber() {
        String accountNo;
        Random random = new Random();

        do {
            // Generate 10-digit account number
            long number = 1000000000L + random.nextLong(9000000000L);
            accountNo = "ACC" + number;
        } while (accountRepository.existsByAccountNo(accountNo));

        return accountNo;
    }

    private AccountResponse convertToResponse(Account account) {
        AccountResponse response = new AccountResponse();
        response.setAccountId(account.getAccountId());
        response.setAccountNo(account.getAccountNo());
        response.setAccountType(account.getAccountType());
        response.setBalance(account.getBalance());
        response.setStatus(account.getStatus());
        response.setOpenedDate(account.getOpenedDate());
        response.setInterestRate(account.getInterestRate());
        response.setOverdraftLimit(account.getOverdraftLimit());
        response.setUserId(account.getUserUserId());
        return response;
    }
    
   
    public AccountResponse getAccountById(Integer accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));

        return convertToResponse(account);
    }
    
    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }
}
