package com.deerbank.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "contact_no", length = 13)
    private String contactNo;

    @Column(name = "created_date", columnDefinition = "DATETIME")
    private LocalDateTime createdDate;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "update_date", columnDefinition = "DATETIME")
    private LocalDateTime updateDate;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "credentials_id")
    private Integer credentialsId;

    @Column(name = "ssn", length = 15)
    private String ssn;
}