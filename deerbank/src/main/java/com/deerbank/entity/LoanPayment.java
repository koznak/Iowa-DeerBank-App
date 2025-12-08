package com.deerbank.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_payment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Integer paymentId;

    @Column(name = "payment_no", length = 20, unique = true)
    private String paymentNo;

    @Column(name = "loan_id")
    private Integer loanId;

    @Column(name = "payment_amount", precision = 15, scale = 2)
    private BigDecimal paymentAmount;

    @Column(name = "principal_amount", precision = 15, scale = 2)
    private BigDecimal principalAmount;

    @Column(name = "interest_amount", precision = 15, scale = 2)
    private BigDecimal interestAmount;

    @Column(name = "remaining_balance", precision = 15, scale = 2)
    private BigDecimal remainingBalance;

    @Column(name = "payment_date", columnDefinition = "DATETIME")
    private LocalDateTime paymentDate;

    @Column(name = "payment_status", length = 20)
    private String paymentStatus; // COMPLETED, PENDING, FAILED, LATE

    @Column(name = "payment_method", length = 20)
    private String paymentMethod; // ACCOUNT_DEBIT, ONLINE, CHECK, CASH

    @Column(name = "transaction_id")
    private Integer transactionId;

    @Column(name = "account_id")
    private Integer accountId;

    @Column(name = "late_fee", precision = 10, scale = 2)
    private BigDecimal lateFee;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_date", columnDefinition = "DATETIME")
    private LocalDateTime createdDate;
}
