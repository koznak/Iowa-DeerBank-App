package com.deerbank.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {

    // User Information
    @NotBlank(message = "Name is required")
    @Size(max = 50, message = "Name must be less than 50 characters")
    private String name;

    @NotNull(message = "Date of birth is required")
    private LocalDate dob;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must be less than 255 characters")
    private String address;

    @NotBlank(message = "Contact number is required")
    @Size(max = 13, message = "Contact number must be less than 13 characters")
    private String contactNo;

    @NotBlank(message = "SSN is required")
    @Size(max = 15, message = "SSN must be less than 15 characters")
    private String ssn;

    @NotBlank(message = "Created By is required")
    @Size(max = 15, message = "Need the person name who want to create account for the customer")
    private String createdBy;

    // Account Information
    @NotBlank(message = "Account type is required")
    private String accountType; // e.g., "SAVINGS", "CHECKING", "BUSINESS"

    @NotNull(message = "Initial balance is required")
    @DecimalMin(value = "50.00", inclusive = true, message = "Minimum initial deposit must be $50.00")
    private BigDecimal initialBalance;

    @NotNull(message = "Account createdBy is required")
    private Integer accountCreatedBy;

    private Integer interestRate; // Optional

    private Integer overdraftLimit; // Optional


}
