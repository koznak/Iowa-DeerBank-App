package com.deerbank.service.impl;

import com.deerbank.dto.LoanDTO;
import com.deerbank.dto.LoanRequestDTO;
import com.deerbank.entity.Account;
import com.deerbank.entity.Loan;
import com.deerbank.entity.User;
import com.deerbank.repository.AccountRepository;
import com.deerbank.repository.LoanRepository;
import com.deerbank.repository.UserRepository;
import com.deerbank.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class LoanServiceImpl implements LoanService {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public LoanDTO applyForLoan(LoanRequestDTO loanRequestDTO) {
        // Verify user and account exist
        User user = userRepository.findById(loanRequestDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Account account = accountRepository.findById(loanRequestDTO.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Calculate monthly payment using loan amortization formula
        BigDecimal monthlyPayment = calculateMonthlyPayment(
                loanRequestDTO.getPrincipalAmount(),
                loanRequestDTO.getInterestRate(),
                loanRequestDTO.getLoanTermMonths()
        );

        // Create loan entity
        Loan loan = new Loan();
        loan.setLoanNo(generateLoanNumber());
        loan.setLoanType(loanRequestDTO.getLoanType());
        loan.setPrincipalAmount(loanRequestDTO.getPrincipalAmount());
        loan.setInterestRate(loanRequestDTO.getInterestRate());
        loan.setLoanTermMonths(loanRequestDTO.getLoanTermMonths());
        loan.setMonthlyPayment(monthlyPayment);
        loan.setRemainingBalance(loanRequestDTO.getPrincipalAmount());
        loan.setStatus("PENDING");
        loan.setApplicationDate(LocalDateTime.now());
        loan.setPurpose(loanRequestDTO.getPurpose());
        loan.setCollateral(loanRequestDTO.getCollateral());
        loan.setUserId(loanRequestDTO.getUserId());
        loan.setAccountId(loanRequestDTO.getAccountId());
        loan.setCreatedDate(LocalDateTime.now());
        loan.setTotalPaymentsMade(0);
        loan.setLatePaymentCount(0);

        Loan savedLoan = loanRepository.save(loan);
        return convertToDTO(savedLoan, user, account);
    }

    @Override
    public LoanDTO approveLoan(Integer loanId, Integer approvedBy) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!"PENDING".equals(loan.getStatus())) {
            throw new RuntimeException("Only pending loans can be approved");
        }

        loan.setStatus("APPROVED");
        loan.setApprovalDate(LocalDateTime.now());
        loan.setApprovedBy(approvedBy);
        loan.setUpdatedDate(LocalDateTime.now());

        Loan updatedLoan = loanRepository.save(loan);
        return convertToDTO(updatedLoan);
    }

    @Override
    public LoanDTO rejectLoan(Integer loanId, Integer rejectedBy) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!"PENDING".equals(loan.getStatus())) {
            throw new RuntimeException("Only pending loans can be rejected");
        }

        loan.setStatus("REJECTED");
        loan.setApprovedBy(rejectedBy);
        loan.setUpdatedDate(LocalDateTime.now());

        Loan updatedLoan = loanRepository.save(loan);
        return convertToDTO(updatedLoan);
    }

    @Override
    public LoanDTO disburseLoan(Integer loanId, Integer disbursedBy) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!"APPROVED".equals(loan.getStatus())) {
            throw new RuntimeException("Only approved loans can be disbursed");
        }

        Account account = accountRepository.findById(loan.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Credit the loan amount to the account
        BigDecimal newBalance = account.getBalance().add(loan.getPrincipalAmount());
        account.setBalance(newBalance);
        account.setUpdateDate(LocalDateTime.now());
        accountRepository.save(account);

        // Update loan status
        loan.setStatus("ACTIVE");
        loan.setDisbursementDate(LocalDate.now());
        loan.setDisbursedBy(disbursedBy);
        loan.setMaturityDate(LocalDate.now().plusMonths(loan.getLoanTermMonths()));
        loan.setNextPaymentDate(LocalDate.now().plusMonths(1));
        loan.setUpdatedDate(LocalDateTime.now());

        Loan updatedLoan = loanRepository.save(loan);
        return convertToDTO(updatedLoan);
    }

    @Override
    public LoanDTO getLoanById(Integer loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        return convertToDTO(loan);
    }

    @Override
    public LoanDTO getLoanByLoanNo(String loanNo) {
        Loan loan = loanRepository.findByLoanNo(loanNo)
                .orElseThrow(() -> new RuntimeException("Loan not found"));
        return convertToDTO(loan);
    }

    @Override
    public List<LoanDTO> getLoansByUserId(Integer userId) {
        List<Loan> loans = loanRepository.findLoansByUserIdOrderByDateDesc(userId);
        return loans.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<LoanDTO> getLoansByStatus(String status) {
        List<Loan> loans = loanRepository.findByStatus(status);
        return loans.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<LoanDTO> getAllLoans() {
        List<Loan> loans = loanRepository.findAll();
        return loans.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<LoanDTO> getActiveLoansByUserId(Integer userId) {
        List<Loan> loans = loanRepository.findByUserIdAndStatus(userId, "ACTIVE");
        return loans.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public Long getActiveLoanCount(Integer userId) {
        return loanRepository.countActiveLoansByUserId(userId);
    }

    @Override
    public Double getTotalOutstandingBalance(Integer userId) {
        Double total = loanRepository.getTotalOutstandingBalanceByUserId(userId);
        return total != null ? total : 0.0;
    }

    @Override
    public List<LoanDTO> getOverdueLoans() {
        List<Loan> loans = loanRepository.findOverdueLoans();
        return loans.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public void deleteLoan(Integer loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if ("ACTIVE".equals(loan.getStatus()) || "DISBURSED".equals(loan.getStatus())) {
            throw new RuntimeException("Cannot delete active or disbursed loans");
        }

        loanRepository.delete(loan);
    }

    // Helper Methods

    private BigDecimal calculateMonthlyPayment(BigDecimal principal, BigDecimal annualRate, Integer months) {
        // Monthly interest rate
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        // If interest rate is 0, simple division
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        }

        // EMI = [P x R x (1+R)^N] / [(1+R)^N-1]
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal power = onePlusR.pow(months);

        BigDecimal numerator = principal.multiply(monthlyRate).multiply(power);
        BigDecimal denominator = power.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private String generateLoanNumber() {
        return "LN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private LoanDTO convertToDTO(Loan loan) {
        LoanDTO dto = new LoanDTO();
        dto.setLoanId(loan.getLoanId());
        dto.setLoanNo(loan.getLoanNo());
        dto.setLoanType(loan.getLoanType());
        dto.setPrincipalAmount(loan.getPrincipalAmount());
        dto.setInterestRate(loan.getInterestRate());
        dto.setLoanTermMonths(loan.getLoanTermMonths());
        dto.setMonthlyPayment(loan.getMonthlyPayment());
        dto.setRemainingBalance(loan.getRemainingBalance());
        dto.setStatus(loan.getStatus());
        dto.setApplicationDate(loan.getApplicationDate());
        dto.setApprovalDate(loan.getApprovalDate());
        dto.setDisbursementDate(loan.getDisbursementDate());
        dto.setMaturityDate(loan.getMaturityDate());
        dto.setNextPaymentDate(loan.getNextPaymentDate());
        dto.setPurpose(loan.getPurpose());
        dto.setCollateral(loan.getCollateral());
        dto.setUserId(loan.getUserId());
        dto.setAccountId(loan.getAccountId());
        dto.setTotalPaymentsMade(loan.getTotalPaymentsMade());
        dto.setLatePaymentCount(loan.getLatePaymentCount());

        // Fetch additional info
        if (loan.getUserId() != null) {
            userRepository.findById(loan.getUserId()).ifPresent(user ->
                    dto.setUserName(user.getName())
            );
        }
        if (loan.getAccountId() != null) {
            accountRepository.findById(loan.getAccountId()).ifPresent(account ->
                    dto.setAccountNo(account.getAccountNo())
            );
        }

        return dto;
    }

    private LoanDTO convertToDTO(Loan loan, User user, Account account) {
        LoanDTO dto = convertToDTO(loan);
        dto.setUserName(user.getName());
        dto.setAccountNo(account.getAccountNo());
        return dto;
    }
}