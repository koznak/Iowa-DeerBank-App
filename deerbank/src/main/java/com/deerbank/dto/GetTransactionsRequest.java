package com.deerbank.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetTransactionsRequest {

    @NotBlank(message = "Account number is required")
    private String accountNo;

    // For customer verification (optional for master user)
    private String name;
    private String contactNo;

    // For master/admin access
    private Boolean isMasterUser = false;
}
