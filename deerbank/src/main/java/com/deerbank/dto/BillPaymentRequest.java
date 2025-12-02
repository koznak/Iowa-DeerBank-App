package com.deerbank.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillPaymentRequest {

    @NotBlank(message = "Customer Account is required")
    private String customer_account;

    @NotBlank(message = "Payee Account is required")
    private String payeeAccount;

    private String payment_type;

    @NotBlank(message = "Username is required")
    private BigDecimal amount;

    @NotBlank(message = "Description is required")
    private String description;
}
