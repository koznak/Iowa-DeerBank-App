package com.deerbank.service;

import com.deerbank.dto.LoanDTO;
import com.deerbank.dto.LoanRequestDTO;

import java.util.List;

public interface LoanService {

    LoanDTO applyForLoan(LoanRequestDTO loanRequestDTO);

    LoanDTO approveLoan(Integer loanId, Integer approvedBy);

    LoanDTO rejectLoan(Integer loanId, Integer rejectedBy);

    LoanDTO disburseLoan(Integer loanId, Integer disbursedBy);

    LoanDTO getLoanById(Integer loanId);

    LoanDTO getLoanByLoanNo(String loanNo);

    List<LoanDTO> getLoansByUserId(Integer userId);

    List<LoanDTO> getLoansByStatus(String status);

    List<LoanDTO> getAllLoans();

    List<LoanDTO> getActiveLoansByUserId(Integer userId);

    Long getActiveLoanCount(Integer userId);

    Double getTotalOutstandingBalance(Integer userId);

    List<LoanDTO> getOverdueLoans();

    void deleteLoan(Integer loanId);
}