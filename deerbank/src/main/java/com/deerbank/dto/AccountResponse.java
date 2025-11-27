package com.deerbank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {
    private Integer accountId;
    private String accountNo;
    private String accountType;
    private BigDecimal balance;
    private String status;
    private LocalDateTime openedDate;
    private Integer interestRate;
    private Integer overdraftLimit;
    private Integer userId;
}
