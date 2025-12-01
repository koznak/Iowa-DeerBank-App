package com.deerbank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionHistoryDTO {
    private Integer tranId;
    private String tranNo;
    private LocalDateTime tranDatetime;
    private String transferType;
    private BigDecimal amount;
    private String debit;
    private String credit;
    private String description;
    private Integer transferAccId;
    private Integer receivedAccId;
}
