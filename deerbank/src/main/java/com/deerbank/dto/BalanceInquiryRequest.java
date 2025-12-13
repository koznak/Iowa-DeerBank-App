package com.deerbank.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalanceInquiryRequest {

    @NotBlank(message = "Account number is required")
    private String accountNo;
}