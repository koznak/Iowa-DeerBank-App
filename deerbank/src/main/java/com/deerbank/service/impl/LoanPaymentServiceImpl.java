package com.deerbank.service.impl;

import com.deerbank.dto.LoanPaymentDTO;
import com.deerbank.entity.Account;
import com.deerbank.entity.Loan;
import com.deerbank.entity.LoanPayment;
import com.deerbank.entity.Transaction;
import com.deerbank.repository.AccountRepository;
import com.deerbank.repository.LoanPaymentRepository;
import com.deerbank.repository.LoanRepository;
import com.deerbank.repository.TransactionRepository;
import com.deerbank.service.LoanPaymentService;
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
public class LoanPaymentServiceImpl implements LoanPaymentService {

    @Autowired
    private LoanPaymentRepository loanPaymentRepository;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    public LoanPaymentDTO makePayment(LoanPaymentDTO paymentDTO) {
        // Fetch loan
        Loan loan = loanRepository.findById(paymentDTO.getLoanId())
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (!"ACTIVE".equals(loan.getStatus())) {
            throw new RuntimeException("Can only make payments on active loans");
        }

        // Fetch account
        Account account = accountRepository.findById(paymentDTO.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        // Check if account has sufficient balance
        if (account.getBalance().compareTo(paymentDTO.getPaymentAmount()) < 0) {
            throw new RuntimeException("Insufficient balance in account");
        }

        // Calculate interest and principal portions
        BigDecimal monthlyRate = loan.getInterestRate()
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        BigDecimal interestAmount = loan.getRemainingBalance()
                .multiply(monthlyRate)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal principalAmount = paymentDTO.getPaymentAmount()
                .subtract(interestAmount)
                .setScale(2, RoundingMode.HALF_UP);

        // Check for late payment
        BigDecimal lateFee = BigDecimal.ZERO;
        String paymentStatus = "COMPLETED";

        if (loan.getNextPaymentDate() != null && LocalDate.now().isAfter(loan.getNextPaymentDate())) {
            lateFee = BigDecimal.valueOf(25.00); // $25 late fee
            loan.setLatePaymentCount(loan.getLatePaymentCount() + 1);
            paymentStatus = "LATE";
        }

        // Update loan
        BigDecimal newBalance = loan.getRemainingBalance().subtract(principalAmount);
        loan.setRemainingBalance(newBalance);
        loan.setTotalPaymentsMade(loan.getTotalPaymentsMade() + 1);
        loan.setNextPaymentDate(LocalDate.now().plusMonths(1));
        loan.setUpdatedDate(LocalDateTime.now());

        // Check if loan is paid off
        if (newBalance.compareTo(BigDecimal.ONE) <= 0) {
            loan.setStatus("PAID_OFF");
            loan.setRemainingBalance(BigDecimal.ZERO);
        }

        loanRepository.save(loan);

        // Debit account
        BigDecimal totalDebit = paymentDTO.getPaymentAmount().add(lateFee);
        account.setBalance(account.getBalance().subtract(totalDebit));
        account.setUpdateDate(LocalDateTime.now());
        accountRepository.save(account);

        // Create transaction record
        Transaction transaction = new Transaction();
        transaction.setTranNo(generateTransactionNumber());
        transaction.setTranDatetime(LocalDateTime.now());
        transaction.setTransferType("LOAN_PAYMENT");
        transaction.setCustomerAccId(account.getAccountId());
        transaction.setAmount(totalDebit);
        transaction.setDebit(totalDebit.toString());
        transaction.setCredit("0");
        transaction.setDescription("Loan payment for " + loan.getLoanNo() +
                (lateFee.compareTo(BigDecimal.ZERO) > 0 ? " (includes late fee: $" + lateFee + ")" : ""));
        transactionRepository.save(transaction);

        // Create payment record
        LoanPayment payment = new LoanPayment();
        payment.setPaymentNo(generatePaymentNumber());
        payment.setLoanId(loan.getLoanId());
        payment.setPaymentAmount(paymentDTO.getPaymentAmount());
        payment.setPrincipalAmount(principalAmount);
        payment.setInterestAmount(interestAmount);
        payment.setRemainingBalance(newBalance);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentStatus(paymentStatus);
        payment.setPaymentMethod(paymentDTO.getPaymentMethod());
        payment.setAccountId(paymentDTO.getAccountId());
        payment.setLateFee(lateFee);
        payment.setNotes(paymentDTO.getNotes());
        payment.setTransactionId(transaction.getTranId());
        payment.setCreatedDate(LocalDateTime.now());

        LoanPayment savedPayment = loanPaymentRepository.save(payment);
        return convertToDTO(savedPayment, loan);
    }

    @Override
    public LoanPaymentDTO getPaymentById(Integer paymentId) {
        LoanPayment payment = loanPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return convertToDTO(payment);
    }

    @Override
    public LoanPaymentDTO getPaymentByPaymentNo(String paymentNo) {
        LoanPayment payment = loanPaymentRepository.findByPaymentNo(paymentNo)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return convertToDTO(payment);
    }

    @Override
    public List<LoanPaymentDTO> getPaymentsByLoanId(Integer loanId) {
        List<LoanPayment> payments = loanPaymentRepository.findByLoanIdOrderByPaymentDateDesc(loanId);
        return payments.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public List<LoanPaymentDTO> getAllPayments() {
        List<LoanPayment> payments = loanPaymentRepository.findAll();
        return payments.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public Double getTotalPaymentsByLoanId(Integer loanId) {
        Double total = loanPaymentRepository.getTotalPaymentsByLoanId(loanId);
        return total != null ? total : 0.0;
    }

    @Override
    public Long countPaymentsByLoanId(Integer loanId) {
        return loanPaymentRepository.countPaymentsByLoanId(loanId);
    }

    @Override
    public void deletePayment(Integer paymentId) {
        LoanPayment payment = loanPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        // Only allow deletion of failed payments
        if ("COMPLETED".equals(payment.getPaymentStatus()) || "LATE".equals(payment.getPaymentStatus())) {
            throw new RuntimeException("Cannot delete completed payments");
        }

        loanPaymentRepository.delete(payment);
    }

    // Helper Methods

    private String generatePaymentNumber() {
        return "LP" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String generateTransactionNumber() {
        return "TXN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    private LoanPaymentDTO convertToDTO(LoanPayment payment) {
        LoanPaymentDTO dto = new LoanPaymentDTO();
        dto.setPaymentId(payment.getPaymentId());
        dto.setPaymentNo(payment.getPaymentNo());
        dto.setLoanId(payment.getLoanId());
        dto.setPaymentAmount(payment.getPaymentAmount());
        dto.setPrincipalAmount(payment.getPrincipalAmount());
        dto.setInterestAmount(payment.getInterestAmount());
        dto.setRemainingBalance(payment.getRemainingBalance());
        dto.setPaymentDate(payment.getPaymentDate());
        dto.setPaymentStatus(payment.getPaymentStatus());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setAccountId(payment.getAccountId());
        dto.setLateFee(payment.getLateFee());
        dto.setNotes(payment.getNotes());

        // Fetch loan number
        if (payment.getLoanId() != null) {
            loanRepository.findById(payment.getLoanId()).ifPresent(loan ->
                    dto.setLoanNo(loan.getLoanNo())
            );
        }

        return dto;
    }

    private LoanPaymentDTO convertToDTO(LoanPayment payment, Loan loan) {
        LoanPaymentDTO dto = convertToDTO(payment);
        dto.setLoanNo(loan.getLoanNo());
        return dto;
    }
}
