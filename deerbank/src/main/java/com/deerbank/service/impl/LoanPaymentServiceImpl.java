package com.deerbank.service.impl;

import com.deerbank.dto.LoanPaymentDTO;
import com.deerbank.dto.TransactionHistoryDTO;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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

        // Check if loan is already paid off
        if ("PAID_OFF".equals(loan.getStatus())) {
            throw new RuntimeException("This loan is already paid off. No further payments can be made.");
        }

        if (!"ACTIVE".equals(loan.getStatus())) {
            throw new RuntimeException("Can only make payments on active loans. Current status: " + loan.getStatus());
        }

        // Fetch account by account number
        Account account = accountRepository.findByAccountNo(paymentDTO.getAccountNumber())
                .orElseThrow(() -> new RuntimeException("Account not found with account number: " + paymentDTO.getAccountNumber()));

        if (!"ACTIVE".equals(account.getStatus())) {
            throw new RuntimeException("Account must be active to make payments. Current status: " + account.getStatus());
        }

        //Check if remaining balance is zero or negative
        if (loan.getRemainingBalance().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("This loan has no remaining balance. Loan is paid off.");
        }

        //Check if payment amount exceeds remaining balance
        if (paymentDTO.getPaymentAmount().compareTo(loan.getRemainingBalance()) > 0) {
            throw new RuntimeException("Payment amount ($" + paymentDTO.getPaymentAmount() +
                    ") exceeds remaining balance ($" + loan.getRemainingBalance() +
                    "). Please enter a payment amount equal to or less than the remaining balance.");
        }

        // Validate account belongs to the loan's user
        if (!account.getSerUserId().equals(loan.getUserId())) {
            throw new RuntimeException("This account does not belong to the loan holder");
        }

        // Check for late payment and calculate late fee
        BigDecimal lateFee = BigDecimal.ZERO;
        if (loan.getNextPaymentDate() != null && LocalDate.now().isAfter(loan.getNextPaymentDate())) {
            lateFee = BigDecimal.valueOf(25.00); // $25 late fee
        }

        BigDecimal totalRequired = paymentDTO.getPaymentAmount().add(lateFee);

        // Check if account has sufficient balance
        if (account.getBalance().compareTo(totalRequired) < 0) {
            throw new RuntimeException("Insufficient balance in account. Available: $" + account.getBalance() +
                    ", Required: $" + totalRequired +
                    (lateFee.compareTo(BigDecimal.ZERO) > 0 ? " (includes $25 late fee)" : ""));
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

        // Determine payment status
        String paymentStatus = "COMPLETED";
        if (lateFee.compareTo(BigDecimal.ZERO) > 0) {
            loan.setLatePaymentCount(loan.getLatePaymentCount() + 1);
            paymentStatus = "LATE";
        }

        // Update loan
        BigDecimal newBalance = loan.getRemainingBalance().subtract(principalAmount);

        // Prevent negative balance (should not happen with above checks, but safety measure)
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            newBalance = BigDecimal.ZERO;
        }



        loan.setRemainingBalance(newBalance);
        loan.setTotalPaymentsMade(loan.getTotalPaymentsMade() + 1);
        loan.setNextPaymentDate(LocalDate.now().plusMonths(1));
        loan.setUpdatedDate(LocalDateTime.now());

        // Check if loan is paid off
        if (newBalance.compareTo(BigDecimal.ONE) <= 0) {
            loan.setStatus("PAID_OFF");
            loan.setRemainingBalance(BigDecimal.ZERO);
            newBalance = BigDecimal.ZERO;
        }

        loanRepository.save(loan);

        //UPDATE ACCOUNT BALANCE - Debit from customer account
        BigDecimal totalDebit = paymentDTO.getPaymentAmount().add(lateFee);
        account.setBalance(account.getBalance().subtract(totalDebit));
        account.setUpdateDate(LocalDateTime.now());
        accountRepository.save(account);

        // Register transaction (Following your pattern)
        Transaction transaction = registerLoanPaymentTransaction(
                account.getAccountId(),
                totalDebit,
                LocalDateTime.now(),
                loan.getLoanNo(),
                lateFee
        );

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
        payment.setAccountId(account.getAccountId());
        payment.setLateFee(lateFee);
        payment.setNotes(paymentDTO.getNotes());
        payment.setTransactionId(transaction.getTranId());
        payment.setCreatedDate(LocalDateTime.now());

        LoanPayment savedPayment = loanPaymentRepository.save(payment);
        return convertToDTO(savedPayment, loan, account);
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

        if ("COMPLETED".equals(payment.getPaymentStatus()) || "LATE".equals(payment.getPaymentStatus())) {
            throw new RuntimeException("Cannot delete completed payments");
        }

        loanPaymentRepository.delete(payment);
    }

    //Get loan payment transaction history by loan ID
    @Override
    public List<TransactionHistoryDTO> getLoanPaymentTransactionHistory(Integer loanId) {
        List<TransactionHistoryDTO> transactionHistory = new ArrayList<>();

        // Get loan
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found with ID: " + loanId));

        // Get account
        Account account = accountRepository.findById(loan.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found for this loan"));

        // Get all transactions for this account
        List<Transaction> transactions = transactionRepository.findByAccountId(account.getAccountId());

        // Filter only LOAN_PAYMENT and LOAN_DISBURSEMENT transactions
        for (Transaction transaction : transactions) {
            if ("LOAN_PAYMENT".equals(transaction.getTransferType()) ||
                    "LOAN_DISBURSEMENT".equals(transaction.getTransferType())) {

                TransactionHistoryDTO dto = new TransactionHistoryDTO();
                dto.setTranId(transaction.getTranId());
                dto.setTranNo(transaction.getTranNo());
                dto.setTranDatetime(transaction.getTranDatetime());
                dto.setTransferType(transaction.getTransferType());
                dto.setAmount(transaction.getAmount());
                dto.setDebit(transaction.getDebit());
                dto.setCredit(transaction.getCredit());
                dto.setDescription(transaction.getDescription());
                dto.setTransferAccId(transaction.getPayeeAccId());
                dto.setReceivedAccId(transaction.getCustomerAccId());

                transactionHistory.add(dto);
            }
        }

        return transactionHistory;
    }

    // Get loan payment transaction history by account number
    @Override
    public List<TransactionHistoryDTO> getLoanPaymentTransactionHistoryByAccountNo(String accountNo) {
        List<TransactionHistoryDTO> transactionHistory = new ArrayList<>();

        // Get account
        Account account = accountRepository.findByAccountNo(accountNo)
                .orElseThrow(() -> new RuntimeException("Account not found with account number: " + accountNo));

        // Get all transactions for this account
        List<Transaction> transactions = transactionRepository.findByAccountId(account.getAccountId());

        // Filter only LOAN_PAYMENT and LOAN_DISBURSEMENT transactions
        for (Transaction transaction : transactions) {
            if ("LOAN_PAYMENT".equals(transaction.getTransferType()) ||
                    "LOAN_DISBURSEMENT".equals(transaction.getTransferType())) {

                TransactionHistoryDTO dto = new TransactionHistoryDTO();
                dto.setTranId(transaction.getTranId());
                dto.setTranNo(transaction.getTranNo());
                dto.setTranDatetime(transaction.getTranDatetime());
                dto.setTransferType(transaction.getTransferType());
                dto.setAmount(transaction.getAmount());
                dto.setDebit(transaction.getDebit());
                dto.setCredit(transaction.getCredit());
                dto.setDescription(transaction.getDescription());
                dto.setTransferAccId(transaction.getPayeeAccId());
                dto.setReceivedAccId(transaction.getCustomerAccId());

                transactionHistory.add(dto);
            }
        }

        return transactionHistory;
    }

    /**
     * Register loan payment transaction
     * Creates a DEBIT entry for the customer account
     */
    private Transaction registerLoanPaymentTransaction(int accountId, BigDecimal amount,
                                                       LocalDateTime dateTime, String loanNo,
                                                       BigDecimal lateFee) {
        Transaction transaction = new Transaction();

        transaction.setTranNo(generateTransactionNumber());
        transaction.setTranDatetime(dateTime);
        transaction.setAmount(amount);
        transaction.setCustomerAccId(accountId);
        transaction.setDebit("Dr");  // Debit from customer account
        transaction.setTransferType("LOAN_PAYMENT");

        // Build description
        String description = "Loan payment for " + loanNo;
        if (lateFee.compareTo(BigDecimal.ZERO) > 0) {
            description += " (includes late fee: $" + lateFee + ")";
        }
        transaction.setDescription(description);

        return transactionRepository.save(transaction);
    }

    private String generatePaymentNumber() {
        Random random = new Random();
        long number = 1000000000L + (long)(random.nextDouble() * 9000000000L);
        return "LP-" + number;
    }

    private String generateTransactionNumber() {
        Random random = new Random();
        long number = 1000000000L + (long)(random.nextDouble() * 9000000000L);
        return "TXN" + number;
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
        dto.setLateFee(payment.getLateFee());
        dto.setNotes(payment.getNotes());

        if (payment.getAccountId() != null) {
            accountRepository.findById(payment.getAccountId()).ifPresent(account ->
                    dto.setAccountNumber(account.getAccountNo())
            );
        }

        if (payment.getLoanId() != null) {
            loanRepository.findById(payment.getLoanId()).ifPresent(loan ->
                    dto.setLoanNo(loan.getLoanNo())
            );
        }

        return dto;
    }

    private LoanPaymentDTO convertToDTO(LoanPayment payment, Loan loan, Account account) {
        LoanPaymentDTO dto = convertToDTO(payment);
        dto.setLoanNo(loan.getLoanNo());
        dto.setAccountNumber(account.getAccountNo());
        return dto;
    }
}