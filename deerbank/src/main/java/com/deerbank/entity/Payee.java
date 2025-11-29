package com.deerbank.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "payee")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payee_id")
    private Integer id;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "nickname", length = 50)
    private String nickname;

    @Column(name = "email", length = 50)
    private String email;

    @Column(name = "phone", length = 50)
    private String phone;

    @Column(name = "account_no", length = 20)
    private String accountNo;

    @Column(name = "status", length = 15)
    private String status;

    @Column(name = "user_id")
    private Integer userUserId;
}
