package com.deerbank.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanRequestDTO {

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotBlank(message = "Loan type is required")
    private String loanType;

    @NotNull(message = "Principal amount is required")
    @DecimalMin(value = "1000.00", message = "Minimum loan amount is $1,000")
    @DecimalMax(value = "1000000.00", message = "Maximum loan amount is $1,000,000")
    private BigDecimal principalAmount;

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.01", message = "Interest rate must be positive")
    @DecimalMax(value = "30.00", message = "Interest rate cannot exceed 30%")
    private BigDecimal interestRate;

    @NotNull(message = "Loan term is required")
    @Min(value = 6, message = "Minimum loan term is 6 months")
    @Max(value = 360, message = "Maximum loan term is 360 months")
    private Integer loanTermMonths;

    @NotBlank(message = "Purpose is required")
    @Size(min = 10, max = 500, message = "Purpose must be between 10 and 500 characters")
    private String purpose;

    private String collateral;
}