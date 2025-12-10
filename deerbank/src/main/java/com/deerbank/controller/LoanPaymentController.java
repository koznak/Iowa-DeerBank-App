package com.deerbank.controller;

import com.deerbank.dto.LoanPaymentDTO;
import com.deerbank.dto.TransactionHistoryDTO;
import com.deerbank.service.LoanPaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loan-payments")
@CrossOrigin(origins = "*")
public class LoanPaymentController {

    @Autowired
    private LoanPaymentService loanPaymentService;

    @PostMapping
    public ResponseEntity<?> makePayment(@Valid @RequestBody LoanPaymentDTO paymentDTO) {
        try {
            LoanPaymentDTO payment = loanPaymentService.makePayment(paymentDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<?> getPaymentById(@PathVariable Integer paymentId) {
        try {
            LoanPaymentDTO payment = loanPaymentService.getPaymentById(paymentId);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/payment-number/{paymentNo}")
    public ResponseEntity<?> getPaymentByPaymentNo(@PathVariable String paymentNo) {
        try {
            LoanPaymentDTO payment = loanPaymentService.getPaymentByPaymentNo(paymentNo);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/loan/{loanId}")
    public ResponseEntity<?> getPaymentsByLoanId(@PathVariable Integer loanId) {
        try {
            List<LoanPaymentDTO> payments = loanPaymentService.getPaymentsByLoanId(loanId);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllPayments() {
        try {
            List<LoanPaymentDTO> payments = loanPaymentService.getAllPayments();
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/loan/{loanId}/summary")
    public ResponseEntity<?> getPaymentSummary(@PathVariable Integer loanId) {
        try {
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalPayments", loanPaymentService.getTotalPaymentsByLoanId(loanId));
            summary.put("paymentCount", loanPaymentService.countPaymentsByLoanId(loanId));
            summary.put("payments", loanPaymentService.getPaymentsByLoanId(loanId));
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{paymentId}")
    public ResponseEntity<?> deletePayment(@PathVariable Integer paymentId) {
        try {
            loanPaymentService.deletePayment(paymentId);
            return ResponseEntity.ok(createSuccessResponse("Payment deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    //Get loan payment transaction history by loan ID
    @GetMapping("/loan/{loanId}/transactions")
    public ResponseEntity<?> getLoanPaymentTransactionHistory(@PathVariable Integer loanId) {
        try {
            List<TransactionHistoryDTO> transactions = loanPaymentService.getLoanPaymentTransactionHistory(loanId);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    //Get loan payment transaction history by account number
    @GetMapping("/account/{accountNo}/transactions")
    public ResponseEntity<?> getLoanPaymentTransactionHistoryByAccountNo(@PathVariable String accountNo) {
        try {
            List<TransactionHistoryDTO> transactions = loanPaymentService.getLoanPaymentTransactionHistoryByAccountNo(accountNo);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        response.put("status", "error");
        return response;
    }

    private Map<String, String> createSuccessResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        response.put("status", "success");
        return response;
    }
}