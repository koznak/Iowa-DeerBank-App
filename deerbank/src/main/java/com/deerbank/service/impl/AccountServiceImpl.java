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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

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
        account.setUserUserId(savedUser.getUserId());
        account.setInterestRate(request.getInterestRate() != null ? request.getInterestRate() : 0);
        account.setOverdraftLimit(request.getOverdraftLimit() != null ? request.getOverdraftLimit() : 0);
        account.setStatus("ACTIVE");
        account.setOpenedDate(LocalDateTime.now());

        // Save account
        Account savedAccount = accountRepository.save(account);

        // Create initial deposit transaction
        depositTransaction(savedAccount, request.getInitialBalance(), LocalDateTime.now(), true, true);

        // Convert to response DTO
        return convertToResponse(savedAccount, savedUser);
    }

    private Transaction depositTransaction(Account account, BigDecimal amount, LocalDateTime dateTime, boolean initialDeposit, boolean drCr) {
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
        }

        if(initialDeposit) {
            transaction.setDescription("Initial deposit for account opening");
        }else{
            transaction.setDescription("new Deposit by customer");
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
        response.setUserId(account.getUserUserId());

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
            Transaction transaction = depositTransaction(savedAccount, request.getAmount(), LocalDateTime.now(), false, true);

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

        if(account.isPresent()){

            Account newBalAcc = account.get();
            BigDecimal existingAmount = newBalAcc.getBalance();

            if(request.getAmount().compareTo(existingAmount) > 0){
                response.setMessage("Requested amount is greater than deposit amount");
                return response;
            }

            newBalAcc.setBalance(existingAmount.subtract(request.getAmount()));
            newBalAcc.setUpdateDate(LocalDateTime.now());

            //Update customer account balance
            Account savedAccount = accountRepository.save(newBalAcc);

            // add new transaction
            Transaction transaction = depositTransaction(savedAccount, request.getAmount(), LocalDateTime.now(), false, false);

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

        response.setMessage("Customer account does not exist");
        return response;

    }


    public List<TransactionHistoryDTO> getTransactions(@Valid GetTransactionsRequest request) {

        List<TransactionHistoryDTO> lstTransactionHistory = new ArrayList<>();

        if(!request.getAccountNo().isEmpty() && !request.getAccountNo().isBlank()){

            Optional<Account> account = accountRepository.findByAccountNo(request.getAccountNo());

            if(account.isPresent()){
                Account acc = account.get();
                List<Transaction> transactions = transactionRepository.findByAccountId(acc.getAccountId());

                if(!transactions.isEmpty()) {
                    TransactionHistoryDTO transactionHistoryDTO = new TransactionHistoryDTO();

                    for (Transaction transaction : transactions) {

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

                        lstTransactionHistory.add(transactionHistoryDTO);
                    }
                }
            }
        }

        return lstTransactionHistory;

    }

    @Override
    public List<TransactionHistoryDTO> getCustomerAccountBalance(String accountNo) {
        return List.of();
    }

    @Transactional
    public Transaction transferBillPayment(int fromAcc, int toAcc, BigDecimal amount, String description, int billNo){

        String tranNo = generateTransactionNumber();
        // Debit from the Customer Acc
        Transaction transaction1 = new Transaction();
        transaction1.setTranDatetime(LocalDateTime.now());
        transaction1.setTransferType("TRANSFER");
        transaction1.setCustomerAccId(toAcc);
        transaction1.setAmount(amount);
        transaction1.setBillPaymentPaymentId(billNo);
        transaction1.setTranNo(tranNo);
        transaction1.setDebit("Dr");
        transaction1.setDescription(description);

        // Credit to the Transer Acc
        Transaction transaction2 = new Transaction();
        transaction2.setTranDatetime(LocalDateTime.now());
        transaction2.setTransferType("TRANSFER");
        transaction2.setPayeeAccId(fromAcc);
        transaction2.setAmount(amount);
        transaction2.setBillPaymentPaymentId(billNo);
        transaction2.setTranNo(tranNo);
        transaction2.setCredit("Cr");
        transaction2.setDescription(description);

        Account account1 = accountRepository.findByAccountIdAndStatus(fromAcc, "ACTIVE")
                .orElseThrow(() -> new ResourceNotFoundException("Your account have some issue. Contact to the Banker!"));

        account1.setBalance(account1.getBalance().subtract(amount));

        accountRepository.save(account1);

        Account account2 = accountRepository.findByAccountIdAndStatus(toAcc, "ACTIVE")
                .orElseThrow(() -> new ResourceNotFoundException("Transfer Account is not found"));

        account2.setBalance(account2.getBalance().add(amount));

        accountRepository.save(account2);

        transactionRepository.save(transaction1);

        return transactionRepository.save(transaction2);
    }

}
