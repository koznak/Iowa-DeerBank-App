package com.deerbank.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "loan_id")
    private Integer loanId;

    @Column(name = "loan_no", length = 20, unique = true)
    private String loanNo;

    @Column(name = "loan_type", length = 20)
    private String loanType; // PERSONAL, HOME, AUTO, BUSINESS, EDUCATION

    @Column(name = "principal_amount", precision = 15, scale = 2)
    private BigDecimal principalAmount;

    @Column(name = "interest_rate", precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "loan_term_months")
    private Integer loanTermMonths;

    @Column(name = "monthly_payment", precision = 15, scale = 2)
    private BigDecimal monthlyPayment;

    @Column(name = "remaining_balance", precision = 15, scale = 2)
    private BigDecimal remainingBalance;

    @Column(name = "status", length = 20)
    private String status; // PENDING, APPROVED, DISBURSED, ACTIVE, PAID_OFF, REJECTED, DEFAULTED

    @Column(name = "application_date", columnDefinition = "DATETIME")
    private LocalDateTime applicationDate;

    @Column(name = "approval_date", columnDefinition = "DATETIME")
    private LocalDateTime approvalDate;

    @Column(name = "disbursement_date")
    private LocalDate disbursementDate;

    @Column(name = "maturity_date")
    private LocalDate maturityDate;

    @Column(name = "next_payment_date")
    private LocalDate nextPaymentDate;

    @Column(name = "purpose", columnDefinition = "TEXT")
    private String purpose;

    @Column(name = "collateral", columnDefinition = "TEXT")
    private String collateral;

    @Column(name = "created_date", columnDefinition = "DATETIME")
    private LocalDateTime createdDate;

    @Column(name = "updated_date", columnDefinition = "DATETIME")
    private LocalDateTime updatedDate;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "account_id")
    private Integer accountId;

    @Column(name = "approved_by")
    private Integer approvedBy;

    @Column(name = "disbursed_by")
    private Integer disbursedBy;

    @Column(name = "total_payments_made")
    private Integer totalPaymentsMade = 0;

    @Column(name = "late_payment_count")
    private Integer latePaymentCount = 0;
}
