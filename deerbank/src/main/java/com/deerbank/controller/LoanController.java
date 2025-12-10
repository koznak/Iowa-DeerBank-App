package com.deerbank.controller;

import com.deerbank.dto.LoanDTO;
import com.deerbank.dto.LoanRequestDTO;
import com.deerbank.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loans")
@CrossOrigin(origins = "*")
public class LoanController {

    @Autowired
    private LoanService loanService;

    @PostMapping("/apply")
    public ResponseEntity<?> applyForLoan(@Valid @RequestBody LoanRequestDTO loanRequestDTO) {
        try {
            LoanDTO loan = loanService.applyForLoan(loanRequestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(loan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{loanId}/approve")
    public ResponseEntity<?> approveLoan(@PathVariable Integer loanId, @RequestParam Integer approvedBy) {
        try {
            LoanDTO loan = loanService.approveLoan(loanId, approvedBy);
            return ResponseEntity.ok(loan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{loanId}/reject")
    public ResponseEntity<?> rejectLoan(@PathVariable Integer loanId, @RequestParam Integer rejectedBy) {
        try {
            LoanDTO loan = loanService.rejectLoan(loanId, rejectedBy);
            return ResponseEntity.ok(loan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{loanId}/disburse")
    public ResponseEntity<?> disburseLoan(@PathVariable Integer loanId, @RequestParam Integer disbursedBy) {
        try {
            LoanDTO loan = loanService.disburseLoan(loanId, disbursedBy);
            return ResponseEntity.ok(loan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{loanId}")
    public ResponseEntity<?> getLoanById(@PathVariable Integer loanId) {
        try {
            LoanDTO loan = loanService.getLoanById(loanId);
            return ResponseEntity.ok(loan);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/loan-number/{loanNo}")
    public ResponseEntity<?> getLoanByLoanNo(@PathVariable String loanNo) {
        try {
            LoanDTO loan = loanService.getLoanByLoanNo(loanNo);
            return ResponseEntity.ok(loan);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getLoansByUserId(@PathVariable Integer userId) {
        try {
            List<LoanDTO> loans = loanService.getLoansByUserId(userId);
            return ResponseEntity.ok(loans);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}/active")
    public ResponseEntity<?> getActiveLoansByUserId(@PathVariable Integer userId) {
        try {
            List<LoanDTO> loans = loanService.getActiveLoansByUserId(userId);
            return ResponseEntity.ok(loans);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getLoansByStatus(@PathVariable String status) {
        try {
            List<LoanDTO> loans = loanService.getLoansByStatus(status);
            return ResponseEntity.ok(loans);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllLoans() {
        try {
            List<LoanDTO> loans = loanService.getAllLoans();
            return ResponseEntity.ok(loans);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/overdue")
    public ResponseEntity<?> getOverdueLoans() {
        try {
            List<LoanDTO> loans = loanService.getOverdueLoans();
            return ResponseEntity.ok(loans);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}/summary")
    public ResponseEntity<?> getLoanSummary(@PathVariable Integer userId) {
        try {
            Map<String, Object> summary = new HashMap<>();
            summary.put("activeLoanCount", loanService.getActiveLoanCount(userId));
            summary.put("totalOutstandingBalance", loanService.getTotalOutstandingBalance(userId));
            summary.put("activeLoans", loanService.getActiveLoansByUserId(userId));
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{loanId}")
    public ResponseEntity<?> deleteLoan(@PathVariable Integer loanId) {
        try {
            loanService.deleteLoan(loanId);
            return ResponseEntity.ok(createSuccessResponse("Loan deleted successfully"));
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