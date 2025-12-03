package com.deerbank.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bill_payment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ser_payment_id")
    private Integer id;

    @Column(name = "bill_payment_no", length = 20)
    private String billPaymentNo;

    @Column(name = "payment_type", length = 10)
    private String payment_type;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "status", length = 15)
    private String status;

    @Column(name = "schedule_type")
    private String schedule_type;

    @Column(name = "schedule_date")
    private String schedule_date;

    @Column(name = "created_date")
    private LocalDateTime created_date;

    @Column(name = "updated_date")
    private LocalDateTime updated_date;

    //Reparative payment (daily, weekly, monthly)
    @Column(name = "schedular_type")
    private String schedular_type;

    @Column(name = "ser_payee_id")
    private int ser_payee_id;
}
