package com.deerbank.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalRequest {

    @NotBlank(message = "Account number is required")
    private String accountNo;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Contact number is required")
    private String contactNo;

    @NotNull(message = "Withdrawal amount is required")
    @DecimalMin(value = "1.00", inclusive = true, message = "Minimum withdrawal amount must be $1.00")
    private BigDecimal amount;
}
