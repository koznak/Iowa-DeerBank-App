package com.deerbank.controller;


import com.deerbank.dto.BillPaymentRequest;
import com.deerbank.dto.BillPaymentResponse;
import com.deerbank.dto.PayeeRequest;
import com.deerbank.dto.PayeeResponse;
import com.deerbank.service.BillPaymentService;
import com.deerbank.service.PayeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/bill-payment")
@CrossOrigin(origins = "*")
public class BillPaymentController {

    @Autowired
    private BillPaymentService billPaymentService;

    @PostMapping("/pay")
    public ResponseEntity<?> createPayee(@Valid @RequestBody BillPaymentRequest paymentRequest) {

        BillPaymentResponse paymentResponse = billPaymentService.createBillPayment(paymentRequest);
        return ResponseEntity
                .status((HttpStatus.CREATED))
                .body(resultMap(true,"Payment Successfully", paymentResponse)
                );
    }

    public <T> Map<String, Object> resultMap(boolean status, String message, T data) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", status);
        result.put("message", message);
        result.put("data", data);

        return result;
    }
}
