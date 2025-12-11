package com.deerbank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {

    private String transactionNo;
    private String transferType;

    // Sender Details
    private String fromAccountNo;
    private BigDecimal senderPreviousBalance;
    private BigDecimal senderNewBalance;

    // Receiver Details
    private String toAccountNo;
    private BigDecimal receiverPreviousBalance;
    private BigDecimal receiverNewBalance;

    // Transfer Details
    private BigDecimal amount;
    private String description;
    private LocalDateTime transactionDate;
    private String message;
}