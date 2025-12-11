package com.deerbank.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {

    @NotBlank(message = "Sender account number is required")
    private String fromAccountNo;

    @NotBlank(message = "Receiver account number is required")
    private String toAccountNo;

    @NotNull(message = "Transfer amount is required")
    @DecimalMin(value = "0.01", message = "Transfer amount must be greater than 0")
    private BigDecimal amount;

    private String description;
}