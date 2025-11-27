package com.deerbank.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "account")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Integer accountId;

    @Column(name = "account_no", length = 20)
    private String accountNo;

    @Column(name = "account_type", length = 10)
    private String accountType;

    @Column(name = "balance", precision = 15, scale = 2)
    private BigDecimal balance;

    @Column(name = "status", length = 15)
    private String status;

    @Column(name = "opened_date", columnDefinition = "DATETIME")
    private LocalDateTime openedDate;

    @Column(name = "update_date", columnDefinition = "DATETIME")
    private LocalDateTime updateDate;

    @Column(name = "user_user_id")
    private Integer userUserId;

    @Column(name = "credentials_id")
    private Integer credentialsId;

    @Column(name = "interest_rate")
    private Integer interestRate;

    @Column(name = "overdraft_limit")
    private Integer overdraftLimit;
}