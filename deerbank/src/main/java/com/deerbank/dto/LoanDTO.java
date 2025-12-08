package com.deerbank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanDTO {

    private Integer loanId;
    private String loanNo;
    private String loanType;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private Integer loanTermMonths;
    private BigDecimal monthlyPayment;
    private BigDecimal remainingBalance;
    private String status;
    private LocalDateTime applicationDate;
    private LocalDateTime approvalDate;
    private LocalDate disbursementDate;
    private LocalDate maturityDate;
    private LocalDate nextPaymentDate;
    private String purpose;
    private String collateral;
    private Integer userId;
    private Integer accountId;
    private String userName;
    private String accountNo;
    private Integer totalPaymentsMade;
    private Integer latePaymentCount;
}
