package com.deerbank.service.impl;

import com.deerbank.Security.JwtService;
import com.deerbank.dto.*;
import com.deerbank.entity.Account;
import com.deerbank.entity.Credential;
import com.deerbank.entity.User;
import com.deerbank.repository.AccountRepository;
import com.deerbank.repository.CredentialRepository;
import com.deerbank.repository.TransactionRepository;
import com.deerbank.repository.UserRepository;
import com.deerbank.service.AuthService;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private CredentialRepository credentialRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private JwtService jwtService;

    //NEW: Inject PasswordEncoder
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        // 1. Find credential by username
        System.out.println("Password::: "+passwordEncoder.encode(request.getPassword()));

        Credential credential = credentialRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        // 2. Verify password using Bcrypt
        if (!passwordEncoder.matches(request.getPassword(), credential.getPassword())) {
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
            Optional<User> retrieveUser = userRepository.findByCredentialsId(credential.getId());

            if (retrieveUser.isEmpty()) {
                throw new RuntimeException("user not found, Unable to create the user for customer.");
            }

            User user = retrieveUser.get();
            response.setUserId(user.getUserId());
            response.setName(user.getName());
            response.setDob(user.getDob());
            response.setAddress(user.getAddress());
            response.setContactNo(user.getContactNo());

            // Get account details
            List<Account> accounts = accountRepository.findBySerUserId(user.getUserId());
            if (!accounts.isEmpty()) {
                Account account = accounts.getFirst(); // Get first account
                response.setAccountId(account.getAccountId());
                response.setAccountNo(account.getAccountNo());
                response.setBalance(account.getBalance());
                response.setAccountType(account.getAccountType());
            }

        }
        response.setToken(getToken(credential));
        return response;
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        LocalDateTime now = LocalDateTime.now();

        // 1. Check if username already exists
        if (credentialRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // 2. Create credential with ENCRYPTED password
        Credential credential = new Credential();
        credential.setUsername(request.getUsername());
        credential.setPassword(passwordEncoder.encode(request.getPassword()));
        credential.setAdminType(Boolean.TRUE.equals(request.getIsAdmin()) ? 1 : 0);
        credential.setStatus("ACTIVE");
        credential.setCreatedDate(now);
        credential.setUpdatedDate(now);

        Credential savedCredential = credentialRepository.save(credential);
        System.out.println(" Created Credential ID: " + savedCredential.getId() +
                " | Type: " + (savedCredential.getAdminType() == 1 ? "ADMIN" : "CUSTOMER"));

        // 3. If ADMIN registration, return immediately WITHOUT linking to user/account
        if (savedCredential.getAdminType() == 1) {
            LoginResponse response = new LoginResponse();
            response.setCredentialId(savedCredential.getId());
            response.setUsername(savedCredential.getUsername());
            response.setUserType("ADMIN");
            response.setStatus(savedCredential.getStatus());
            response.setToken(getToken(savedCredential));

            System.out.println("Admin account created - NO user/account association");
            return response;
        }

        // 4. For CUSTOMER registration, link with existing account
        if (request.getAccountNumber() == null || request.getAccountNumber().isBlank()) {
            throw new RuntimeException("Account number is required for customer registration");
        }

        Optional<Account> customerAccount = accountRepository.findByAccountNo(request.getAccountNumber());

        if (customerAccount.isEmpty()) {
            throw new RuntimeException("Account does not exist with account number: " + request.getAccountNumber());
        }

        Account account = customerAccount.get();

        // Verify account is not already linked
        if (account.getCredentialsId() != null && account.getCredentialsId() > 0) {
            throw new RuntimeException("This account is already registered to another user");
        }

        account.setCredentialsId(savedCredential.getId());
        account.setUpdateDate(LocalDateTime.now());
        Account savedAccount = accountRepository.save(account);
        System.out.println("Linked Account ID: " + savedAccount.getAccountId());

        // 5. Update user with credentials_id
        Optional<User> userDetail = userRepository.findById(customerAccount.get().getSerUserId());

        if (userDetail.isEmpty()) {
            throw new RuntimeException("User not found for this account");
        }

        User user = userDetail.get();

        if (user.getCredentialsId() != null && user.getCredentialsId() > 0) {
            throw new RuntimeException("This user is already registered");
        }

        user.setCredentialsId(credential.getId());
        user.setUpdateDate(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        System.out.println(" Linked User ID: " + savedUser.getUserId());

        // 6. Build response
        return response(savedCredential, savedUser, savedAccount);
    }

    private LoginResponse response(Credential savedCredential, User savedUser, Account savedAccount) {

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
        response.setToken(getToken(savedCredential));

        return response;
    }

    // Update Password Method
    @Transactional
    public UpdatePasswordResponse updatePassword(UpdatePasswordRequest request) {
        // 1. Find credential by username
        Credential credential = credentialRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), credential.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // 3. Validate new password
        if (request.getNewPassword().equals(request.getCurrentPassword())) {
            throw new RuntimeException("New password must be different from current password");
        }

        if (request.getNewPassword().length() < 6) {
            throw new RuntimeException("New password must be at least 6 characters long");
        }

        // 4. Update password with encryption
        credential.setPassword(passwordEncoder.encode(request.getNewPassword()));
        credential.setUpdatedDate(LocalDateTime.now());
        credentialRepository.save(credential);

        System.out.println(" Password updated for user: " + credential.getUsername());

        // 5. Build response
        UpdatePasswordResponse response = new UpdatePasswordResponse();
        response.setUsername(credential.getUsername());
        response.setMessage("Password updated successfully");
        response.setUpdatedDate(LocalDateTime.now());

        return response;
    }

    public String getToken(Credential user) {

        String token = jwtService.generateToken(user);

        return token;
    }

}
