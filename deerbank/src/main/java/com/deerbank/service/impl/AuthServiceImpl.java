package com.deerbank.service.impl;

import com.deerbank.dto.LoginRequest;
import com.deerbank.dto.LoginResponse;
import com.deerbank.dto.RegisterRequest;
import com.deerbank.entity.Account;
import com.deerbank.entity.Credential;
import com.deerbank.entity.User;
import com.deerbank.repository.AccountRepository;
import com.deerbank.repository.CredentialRepository;
import com.deerbank.repository.TransactionRepository;
import com.deerbank.repository.UserRepository;
import com.deerbank.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class AuthServiceImpl implements AuthService {

    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 1. Find credential by username
        Credential credential = credentialRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        // 2. Verify password (in production, use BCrypt or similar)
        if (!credential.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        // 3. Check if account is active
        if (!"ACTIVE".equalsIgnoreCase(credential.getStatus())) {
            throw new RuntimeException("Account is " + credential.getStatus() + ". Please contact support.");
        }

        // 4. Build response based on user type
        LoginResponse response = new LoginResponse();
        response.setCredentialId(credential.getId());
        response.setUsername(credential.getUsername());
        response.setStatus(credential.getStatus());

        if (credential.getAdminType() == 1) {
            // Master/Admin user
            response.setUserType("MASTER");
        } else {
            // Customer user
            response.setUserType("CUSTOMER");

            // Get user details
            User user = userRepository.findById(credential.getId())
                    .orElse(null);

            if (user != null) {
                response.setUserId(user.getUserId());
                response.setName(user.getName());
                response.setDob(user.getDob());
                response.setAddress(user.getAddress());
                response.setContactNo(user.getContactNo());

                // Get account details
                List<Account> accounts = accountRepository.findByUserByUserId(user.getUserId());
                if (!accounts.isEmpty()) {
                    Account account = accounts.getFirst(); // Get first account
                    response.setAccountId(account.getAccountId());
                    response.setAccountNo(account.getAccountNo());
                    response.setBalance(account.getBalance());
                    response.setAccountType(account.getAccountType());
                }
            }
        }

        return response;
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        LocalDateTime now = LocalDateTime.now();

        // 1. Check if username already exists
        if (credentialRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // 2. Create credential FIRST
        Credential credential = new Credential();
        credential.setUsername(request.getUsername());
        credential.setPassword(request.getPassword()); // In production, hash this!
        credential.setAdminType(0); // 0 = Customer
        credential.setStatus("ACTIVE");
        credential.setCreatedDate(now);
        credential.setUpdatedDate(now);

        Credential savedCredential = credentialRepository.save(credential);

        System.out.println("Created Credential ID: " + savedCredential.getId());

        // 2. Update account with credentials_id reference

        Optional<Account> customerAccount = accountRepository.findByAccountNo(request.getAccountNumber());

        if(customerAccount.isEmpty()){
            throw new RuntimeException("Account does not exists");
        }
        Account account = customerAccount.get();
        account.setCredentialsId(savedCredential.getId()); // Reference to credential
        account.setUpdateDate(LocalDateTime.now());
        Account savedAccount = accountRepository.save(account);
        System.out.println("Created Account ID: " + savedAccount.getAccountId() + " with Credentials ID: " + savedAccount.getCredentialsId());

        // 3. Update user with credentials_id reference
        Optional<User> userDetail = userRepository.findById(customerAccount.get().getUserUserId());

        if(userDetail.isEmpty()) {
            throw new RuntimeException("Account does not exists");
        }
        User user = userDetail.get();
        user.setCredentialsId(credential.getId());
        user.setUpdateDate(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        System.out.println("Created User ID: " + savedUser.getUserId() + " with Credentials ID: " + savedUser.getCredentialsId());

        // 4. Build response
        LoginResponse response = new LoginResponse();
        response.setCredentialId(savedCredential.getId());
        response.setUsername(savedCredential.getUsername());
        response.setUserType("CUSTOMER");
        response.setStatus(savedCredential.getStatus());
        response.setUserId(savedUser.getUserId());
        response.setName(savedUser.getName());
        response.setDob(savedUser.getDob());
        response.setAddress(savedUser.getAddress());
        response.setContactNo(savedUser.getContactNo());
        response.setAccountId(savedAccount.getAccountId());
        response.setAccountNo(savedAccount.getAccountNo());
        response.setAccountType(savedAccount.getAccountType());
        response.setBalance(savedAccount.getBalance());

        return response;
    }

}
