package com.deerbank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

    private Integer userId;
    private String name;
    private LocalDate dob;
    private String address;
    private String contactNo;
    private String ssn;
    private String createdBy;

    private Integer accountId;
    private String accountNo;
    private String accountType;
    private BigDecimal balance;
    private String status;
    private LocalDateTime openedDate;
    private Integer interestRate;
    private Integer overdraftLimit;
}
