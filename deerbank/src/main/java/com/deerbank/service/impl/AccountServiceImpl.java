package com.deerbank.service.impl;

import com.deerbank.dto.*;
import com.deerbank.entity.Account;
import com.deerbank.entity.Transaction;
import com.deerbank.entity.User;
import com.deerbank.exception.ResourceNotFoundException;
import com.deerbank.repository.AccountRepository;
import com.deerbank.repository.TransactionRepository;
import com.deerbank.repository.UserRepository;
import com.deerbank.service.AccountService;
import jakarta.validation.Valid;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {

        LocalDateTime now = LocalDateTime.now();

        if(request.getAccountCreatedBy() != 1){
            throw new RuntimeException("Only Admin can create the account.");
        }

        // 1. Create and save User
        User user = new User();
        user.setName(request.getName());
        user.setDob(request.getDob());
        user.setAddress(request.getAddress());
        user.setContactNo(request.getContactNo());
        user.setSsn(request.getSsn());
        user.setCreatedDate(now);
        user.setCreatedBy(request.getCreatedBy());

        User savedUser = userRepository.save(user);

        // 2. Create and save Account
        // Generate unique account number
        String accountNo = generateAccountNumber();

        Account account = new Account();
        account.setAccountNo(accountNo);
        account.setAccountType(request.getAccountType());
        account.setBalance(request.getInitialBalance());
        account.setSerUserId(savedUser.getUserId());
        account.setInterestRate(request.getInterestRate() != null ? request.getInterestRate() : 0);
        account.setOverdraftLimit(request.getOverdraftLimit() != null ? request.getOverdraftLimit() : 0);
        account.setStatus("ACTIVE");
        account.setOpenedDate(LocalDateTime.now());
        account.setAccountCreatedBy(request.getAccountCreatedBy());

        // Save account
        Account savedAccount = accountRepository.save(account);

        // Create initial deposit transaction
        registerTransaction(savedAccount, request.getInitialBalance(), LocalDateTime.now(), true, true);

        // Convert to response DTO
        return convertToResponse(savedAccount, savedUser);
    }

    private Transaction registerTransaction(Account account, BigDecimal amount, LocalDateTime dateTime, boolean initialDeposit, boolean drCr) {
        Transaction transaction = new Transaction();

        transaction.setTranNo(generateTransactionNumber());
        transaction.setTranDatetime(dateTime);

        transaction.setCustomerAccId(account.getAccountId());
        transaction.setAmount(amount);

        if(drCr){
            transaction.setCredit("Cr");
            transaction.setTransferType("DEPOSIT");
        }else{
            transaction.setDebit("Dr");
            transaction.setTransferType("WITHDRAWAL");
            transaction.setDescription("new withdrawal made by customer");
        }

        if(initialDeposit) {
            transaction.setDescription("Initial deposit for account opening");
        }else{
            transaction.setDescription("new Deposit made by customer");
        }

        return transactionRepository.save(transaction);

    }

    private String generateTransactionNumber() {
        Random random = new Random();
        long number = 1000000000L + (long)(random.nextDouble() * 9000000000L);
        return "TXN" + number;
    }

    private String generateAccountNumber() {
        String accountNo;
        Random random = new Random();

        do {
            // Generate 10-digit account number
            long number = 1000000000L + random.nextLong(9000000000L);
            accountNo = "ACC" + number;
        } while (accountRepository.existsByAccountNo(accountNo));

        return accountNo;
    }

    private AccountResponse convertToResponse(Account account, User savedUser) {

        // Set Account info
        AccountResponse response = new AccountResponse();

        // User
        response.setUserId(savedUser.getUserId());
        response.setName(savedUser.getName());
        response.setDob(savedUser.getDob());
        response.setAddress(savedUser.getAddress());
        response.setContactNo(savedUser.getContactNo());
        response.setSsn(savedUser.getSsn());
        response.setCreatedBy(savedUser.getCreatedBy());

        // Account
        response.setAccountId(account.getAccountId());
        response.setAccountNo(account.getAccountNo());
        response.setAccountType(account.getAccountType());
        response.setBalance(account.getBalance());
        response.setStatus(account.getStatus());
        response.setOpenedDate(account.getOpenedDate());
        response.setInterestRate(account.getInterestRate());
        response.setOverdraftLimit(account.getOverdraftLimit());
        response.setUserId(account.getSerUserId());

        return response;
    }


    public TransactionResponse deposit(@Valid DepositRequest request) {
        TransactionResponse response = new TransactionResponse();
        Optional<Account> account = accountRepository.findByAccountNo(request.getAccountNo());

        if(account.isPresent()){

            Account newBalAcc = account.get();
            BigDecimal existingAmount = newBalAcc.getBalance();

            newBalAcc.setBalance(existingAmount.add(request.getAmount()));
            newBalAcc.setUpdateDate(LocalDateTime.now());

            //Update customer account balance
            Account savedAccount = accountRepository.save(newBalAcc);

            // add new transaction
            Transaction transaction = registerTransaction(savedAccount, request.getAmount(), LocalDateTime.now(), false, true);

            // prepare response
            response.setTransactionNo(transaction.getTranNo());
            response.setTransactionType(transaction.getTransferType());
            response.setAmount(transaction.getAmount());

            response.setAccountNo(savedAccount.getAccountNo());
            response.setNewBalance(savedAccount.getBalance());

            response.setPreviousBalance(existingAmount);
            response.setNewBalance(savedAccount.getBalance());
            response.setTransactionDate(LocalDateTime.now());

            response.setMessage("new Deposit by customer on "+ LocalDateTime.now());

            return response;

        }

        response.setMessage("Customer account does not exist");
        return response;

    }

    public TransactionResponse withdrawal(@Valid WithdrawalRequest request) {

        TransactionResponse response = new TransactionResponse();
        Optional<Account> account = accountRepository.findByAccountNo(request.getAccountNo());

        if(account.isEmpty()){
            throw new RuntimeException("Customer account does not exist");
        }

        Account newBalAcc = account.get();
        BigDecimal existingAmount = newBalAcc.getBalance();

        if(request.getAmount().compareTo(existingAmount) > 0){
            response.setMessage("Unable to withdrawal amount due to insufficient balance");
            return response;
        }

        newBalAcc.setBalance(existingAmount.subtract(request.getAmount()));
        newBalAcc.setUpdateDate(LocalDateTime.now());

        //Update customer account balance
        Account savedAccount = accountRepository.save(newBalAcc);

        // add new transaction
        Transaction transaction = registerTransaction(savedAccount, request.getAmount(), LocalDateTime.now(), false, false);

        // prepare response
        response.setTransactionNo(transaction.getTranNo());
        response.setTransactionType(transaction.getTransferType());
        response.setAmount(transaction.getAmount());

        response.setAccountNo(savedAccount.getAccountNo());
        response.setNewBalance(savedAccount.getBalance());

        response.setPreviousBalance(existingAmount);
        response.setNewBalance(savedAccount.getBalance());
        response.setTransactionDate(LocalDateTime.now());

        response.setMessage("new withdrawal by customer on "+ LocalDateTime.now());

        return response;

    }


    public List<TransactionHistoryDTO> getTransactions(@Valid GetTransactionsRequest request) {

        List<TransactionHistoryDTO> lstTransactionHistory = new ArrayList<>();

        if(!request.getAccountNo().isEmpty() && !request.getAccountNo().isBlank()){

            Optional<Account> account = accountRepository.findByAccountNo(request.getAccountNo());

            if(account.isEmpty()){
                throw new RuntimeException("Account does not exist!");
            }
            Account acc = account.get();
            List<Transaction> transactions = transactionRepository.findByAccountId(acc.getAccountId());

            if(!transactions.isEmpty()) {


                for (Transaction transaction : transactions) {

                    TransactionHistoryDTO transactionHistoryDTO = getTransactionHistoryDTO(transaction);

                    lstTransactionHistory.add(transactionHistoryDTO);
                }
            }
        }

        return lstTransactionHistory;

    }

    private static @NonNull TransactionHistoryDTO getTransactionHistoryDTO(Transaction transaction) {
        TransactionHistoryDTO transactionHistoryDTO = new TransactionHistoryDTO();

        transactionHistoryDTO.setTranId(transaction.getTranId());
        transactionHistoryDTO.setTranNo(transaction.getTranNo());
        transactionHistoryDTO.setTranDatetime(transaction.getTranDatetime());
        transactionHistoryDTO.setTransferType(transaction.getTransferType());
        transactionHistoryDTO.setAmount(transaction.getAmount());
        transactionHistoryDTO.setDebit(transaction.getDebit());
        transactionHistoryDTO.setCredit(transaction.getCredit());
        transactionHistoryDTO.setDescription(transaction.getDescription());
        transactionHistoryDTO.setTransferAccId(transaction.getPayeeAccId());
        transactionHistoryDTO.setReceivedAccId(transaction.getCustomerAccId());
        return transactionHistoryDTO;
    }

    @Transactional
    public Transaction transferBillPayment(int fromAcc, int toAcc, BigDecimal amount, String description, int billNo){

        String tranNo = generateTransactionNumber();
        // Debit from the Customer Acc
        Transaction debitTransaction = new Transaction();
        debitTransaction.setTranDatetime(LocalDateTime.now());
        debitTransaction.setTransferType("TRANSFER");
        debitTransaction.setCustomerAccId(toAcc);
        debitTransaction.setAmount(amount);
        debitTransaction.setBillPaymentPaymentId(billNo);
        debitTransaction.setTranNo(tranNo);
        debitTransaction.setDebit("Dr");
        debitTransaction.setDescription(description);

        // Credit to the Transfer Acc
        Transaction creditTransaction = new Transaction();
        creditTransaction.setTranDatetime(LocalDateTime.now());
        creditTransaction.setTransferType("TRANSFER");
        creditTransaction.setPayeeAccId(fromAcc);
        creditTransaction.setAmount(amount);
        creditTransaction.setBillPaymentPaymentId(billNo);
        creditTransaction.setTranNo(tranNo);
        creditTransaction.setCredit("Cr");
        creditTransaction.setDescription(description);

        Account senderAccount = accountRepository.findByAccountIdAndStatus(fromAcc, "ACTIVE")
                .orElseThrow(() -> new ResourceNotFoundException("Your account have some issue. Contact to the Banker!"));

        senderAccount.setBalance(senderAccount.getBalance().subtract(amount));

        accountRepository.save(senderAccount);

        Account receiverAccount = accountRepository.findByAccountIdAndStatus(toAcc, "ACTIVE")
                .orElseThrow(() -> new ResourceNotFoundException("Transfer Account is not found"));

        receiverAccount.setBalance(receiverAccount.getBalance().add(amount));

        accountRepository.save(receiverAccount);

        transactionRepository.save(debitTransaction);

        return transactionRepository.save(creditTransaction);
    }

}
