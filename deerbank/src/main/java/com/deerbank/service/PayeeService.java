package com.deerbank.service;

import com.deerbank.dto.PayeeRequest;
import com.deerbank.dto.PayeeResponse;

import java.util.List;

public interface PayeeService {

    List<PayeeResponse> getAllPayees();
    PayeeResponse createPayee(PayeeRequest payeeRequest);
    void deletePayee(int id);
}
