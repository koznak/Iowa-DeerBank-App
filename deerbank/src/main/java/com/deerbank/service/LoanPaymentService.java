package com.deerbank.service;

import com.deerbank.dto.LoanPaymentDTO;

import java.util.List;

public interface LoanPaymentService {

    LoanPaymentDTO makePayment(LoanPaymentDTO paymentDTO);

    LoanPaymentDTO getPaymentById(Integer paymentId);

    LoanPaymentDTO getPaymentByPaymentNo(String paymentNo);

    List<LoanPaymentDTO> getPaymentsByLoanId(Integer loanId);

    List<LoanPaymentDTO> getAllPayments();

    Double getTotalPaymentsByLoanId(Integer loanId);

    Long countPaymentsByLoanId(Integer loanId);

    void deletePayment(Integer paymentId);
}
