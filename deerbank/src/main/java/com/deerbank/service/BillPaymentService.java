package com.deerbank.service;

import com.deerbank.dto.BillPaymentRequest;
import com.deerbank.dto.BillPaymentResponse;
import com.deerbank.entity.BillPayment;

public interface BillPaymentService {

    BillPaymentResponse createBillPayment(BillPaymentRequest billPaymentRequest);
}
