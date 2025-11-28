package com.deerbank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private String transactionNo;
    private String accountNo;
    private String transactionType;
    private BigDecimal amount;
    private BigDecimal previousBalance;
    private BigDecimal newBalance;
    private LocalDateTime transactionDate;
    private String message;
}
