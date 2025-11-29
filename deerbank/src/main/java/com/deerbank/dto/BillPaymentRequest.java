package com.deerbank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillPaymentRequest {

    private String from_account_no;
    private String to_account_no;

    private int payee_id;
    private String payment_type;
    private BigDecimal amount;

    private String schedule_type;
    private LocalDateTime schedule_date;
    private String schedular_type;

    private String description;
}
