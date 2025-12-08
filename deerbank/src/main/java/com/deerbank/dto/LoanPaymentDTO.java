package com.deerbank.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanPaymentDTO {

    private Integer paymentId;
    private String paymentNo;

    @NotNull(message = "Loan ID is required")
    private Integer loanId;

    @NotNull(message = "Payment amount is required")
    @DecimalMin(value = "0.01", message = "Payment amount must be positive")
    private BigDecimal paymentAmount;

    private BigDecimal principalAmount;
    private BigDecimal interestAmount;
    private BigDecimal remainingBalance;
    private LocalDateTime paymentDate;
    private String paymentStatus;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    @NotNull(message = "Account ID is required")
    private Integer accountId;

    private BigDecimal lateFee;
    private String notes;
    private String loanNo;
}
