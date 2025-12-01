package com.deerbank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillPaymentResponse {

    private String bill_payment_no;
    private BigDecimal amount;
    private String tran_no;
}
