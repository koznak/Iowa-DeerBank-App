package com.deerbank.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tran_id")
    private Integer tranId;

    @Column(name = "tran_datetime", columnDefinition = "DATETIME")
    private LocalDateTime tranDatetime;

    @Column(name = "transfer_type", length = 255)
    private String transferType;

    @Column(name = "payee_acc_id")
    private Integer payeeAccId;

    @Column(name = "customer_acc_id")
    private Integer customerAccId;

    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "bill_payment_payment_id")
    private Integer billPaymentPaymentId;

    @Column(name = "tran_no", length = 20)
    private String tranNo;

    @Column(name = "debit", columnDefinition = "TEXT")
    private String debit;

    @Column(name = "credit", columnDefinition = "TEXT")
    private String credit;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}