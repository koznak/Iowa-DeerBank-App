package com.deerbank.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {

    @NotBlank(message = "Account type is required")
    private String accountType; // e.g., "SAVINGS", "CHECKING", "BUSINESS"

    @NotNull(message = "Initial balance is required")
    @DecimalMin(value = "50.00", inclusive = true, message = "Minimum initial deposit must be $50.00")
    private BigDecimal initialBalance;

    @NotNull(message = "User ID is required")
    private Integer userId;

    @NotNull(message = "Credentials ID is required")
    private Integer credentialsId;

    private Integer interestRate; // Optional

    private Integer overdraftLimit; // Optional
}
