package com.deerbank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private Integer credentialId;
    private String username;
    private String userType; // "CUSTOMER" or "MASTER"
    private String status;

    // User details (for customers)
    private Integer userId;
    private String name;
    private LocalDate dob;
    private String address;
    private String contactNo;

    // Account details (for customers)
    private Integer accountId;
    private String accountNo;
    private String accountType;
    private BigDecimal balance;

    private String token;
}
