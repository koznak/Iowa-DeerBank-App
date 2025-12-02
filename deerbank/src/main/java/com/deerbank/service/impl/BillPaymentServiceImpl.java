package com.deerbank.service.impl;

import com.deerbank.dto.BillPaymentRequest;
import com.deerbank.dto.BillPaymentResponse;
import com.deerbank.entity.Account;
import com.deerbank.entity.BillPayment;
import com.deerbank.entity.Payee;
import com.deerbank.entity.Transaction;
import com.deerbank.repository.AccountRepository;
import com.deerbank.repository.BillPaymentRepository;
import com.deerbank.repository.PayeeRepository;
import com.deerbank.repository.TransactionRepository;
import com.deerbank.service.BillPaymentService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class BillPaymentServiceImpl implements BillPaymentService {

    @Autowired
    private BillPaymentRepository billPaymentRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PayeeRepository payeeRepository;

    @Override
    @Transactional
    public BillPaymentResponse createBillPayment(BillPaymentRequest billPaymentRequest) {

        //Validation
        //Get Payee Information
        Optional<Payee> retrievePayee = payeeRepository.findByAccountNo(billPaymentRequest.getPayeeAccount());
        if(retrievePayee.isEmpty()){
            throw new RuntimeException("Wrong Payee Account, Please put correct Payee Account Number.");
        }

        //Get Payee Information
        Optional<Account> retrieveCustomerAccount = accountRepository.findByAccountNo(billPaymentRequest.getCustomer_account());
        if(retrieveCustomerAccount.isEmpty()){
            throw new RuntimeException("Wrong Customer Account, Please put correct Customer Account Number.");
        }

        Payee payee = retrievePayee.get();
        Account account = retrieveCustomerAccount.get();

        //update balance
        account.setBalance(account.getBalance().subtract(billPaymentRequest.getAmount()));
        Account newAccountBalance = accountRepository.save(account);
        System.out.println("New Customer Account Balance: "+newAccountBalance);


        //saving bill payment type in DB
        BillPayment billPayment = new BillPayment();
        billPayment.setBillPaymentNo(generateBillNumber());
        billPayment.setPayment_type(billPaymentRequest.getPayment_type());
        billPayment.setAmount(billPaymentRequest.getAmount());
//        billPayment.setSchedular_type(billPaymentRequest.getSchedular_type());
        billPayment.setSchedule_date(billPayment.getSchedule_date());
        billPayment.setSchedule_date(billPayment.getSchedule_date());
        billPayment.setCreated_date(LocalDateTime.now());
        billPayment.setUpdated_date(LocalDateTime.now());

        if (billPaymentRequest.getPayment_type().equals("ONCE")) {
            billPayment.setStatus("DONE");
        } else {
            billPayment.setStatus("ACTIVE");
        }

        billPayment.setSer_payee_id(payee.getId());

        // 1. Safe bill detail in bill_payment table
        BillPayment billPaymentResult = billPaymentRepository.save(billPayment);

        // 2. Update the balance in account table
        Account newcustomeraccountbalance = accountRepository.save(account);
        System.out.println("new account balance is:"+newcustomeraccountbalance.getBalance());

        //3. Now register transactions in Transactions Table.
        // 3.1 credit transaction for payee
        registerTransaction(payee.getId(), billPaymentRequest.getAmount(),LocalDateTime.now(), false, true);
        // 3.2 debit transaction for customer
        Transaction transaction = registerTransaction(account.getAccountId(),  billPaymentRequest.getAmount(), LocalDateTime.now(),false, false);

        //4. make response
        BillPaymentResponse billPaymentResponse=new BillPaymentResponse();
        billPaymentResponse.setBill_payment_no(billPayment.getBillPaymentNo());
        billPaymentResponse.setAmount(billPayment.getAmount());
        billPaymentResponse.setTran_no(transaction.getTranNo()); // debit transaction Number
        return billPaymentResponse;
    }


    private String generateBillNumber() {
        String billNo;
        Random random = new Random();

        do {
            // Generate 10-digit account number
            long number = 1000000000L + random.nextLong(9000000000L);
            billNo = "BIL" + number;
        } while (billPaymentRepository.existsByBillPaymentNo(billNo));

        return billNo;
    }

    private String generateTransactionNumber() {
        Random random = new Random();
        long number = 1000000000L + (long)(random.nextDouble() * 9000000000L);
        return "TXN" + number;
    }

    private Transaction registerTransaction(int accountId, BigDecimal amount, LocalDateTime dateTime, boolean initialDeposit, boolean drCr) {
        Transaction transaction = new Transaction();

        transaction.setTranNo(generateTransactionNumber());
        transaction.setTranDatetime(dateTime);
        transaction.setAmount(amount);

        if(drCr){
            transaction.setPayeeAccId(accountId);
            transaction.setCredit("Cr");
            transaction.setTransferType("Payment Received by Payee");
        }else{
            transaction.setCustomerAccId(accountId);
            transaction.setDebit("Dr");
            transaction.setTransferType("Payment made by Customer");
        }

        if(initialDeposit) {
            transaction.setDescription("Initial deposit for account opening");
        }else{
            transaction.setDescription("new Deposit by customer");
        }

        return transactionRepository.save(transaction);

    }

}
