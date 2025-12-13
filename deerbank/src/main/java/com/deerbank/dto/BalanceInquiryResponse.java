package com.deerbank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalanceInquiryResponse {

    private Integer accountId;
    private String accountNo;
    private String accountType;
    private BigDecimal currentBalance;
    private String status;
    private LocalDateTime lastUpdated;

    // User details
    private Integer userId;
    private String userName;
    private String contactNo;

    // Additional account info
    private Integer interestRate;
    private Integer overdraftLimit;
    private LocalDateTime openedDate;

    private String message;
}
