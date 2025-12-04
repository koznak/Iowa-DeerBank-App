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

        if(billPaymentRequest.getAmount().compareTo(retrieveCustomerAccount.get().getBalance()) > 0){
            throw new RuntimeException("Unable to make payment due to insufficient balance.");
        }

        Payee payee = retrievePayee.get();
        Account account = retrieveCustomerAccount.get();


        //saving bill payment type in DB
        BillPayment billPayment = new BillPayment();

        billPayment.setPayment_type(billPaymentRequest.getPayment_type());
        billPayment.setAmount(billPaymentRequest.getAmount());
        billPayment.setBillPaymentNo(generateBillNumber());


//        billPayment.setSchedular_type(billPaymentRequest.getSchedular_type());
        billPayment.setSchedule_date(billPayment.getSchedule_date());
        billPayment.setCreated_date(LocalDateTime.now());
//        billPayment.setUpdated_date(LocalDateTime.now());

        if (billPaymentRequest.getPayment_type().equals("ONCE")) {
            billPayment.setStatus("DONE");
        } else {
            billPayment.setStatus("ACTIVE");
        }

        billPayment.setSer_payee_id(payee.getId());

        // 1. Safe bill detail in bill_payment table
        BillPayment billPaymentResult = billPaymentRepository.save(billPayment);

        //2. Now register transactions in Transactions Table.
        // 2.1 credit transaction for payee
        registerTransaction(payee.getId(), billPaymentRequest.getAmount(),LocalDateTime.now(), "External Payment", true);
        // 2.2 debit transaction for customer
        Transaction transaction = registerTransaction(account.getAccountId(),  billPaymentRequest.getAmount(), LocalDateTime.now(),"Subscription Payment", false);

        // 3. Update the balance in account table
        account.setBalance(account.getBalance().subtract(billPaymentRequest.getAmount()));
        Account newcustomeraccountbalance = accountRepository.save(account);
        System.out.println(" ======> new account balance is: "+newcustomeraccountbalance.getBalance());



        //4. make response
        BillPaymentResponse billPaymentResponse=new BillPaymentResponse();
        billPaymentResponse.setBill_payment_no(billPayment.getBillPaymentNo());
        billPaymentResponse.setAmount(billPayment.getAmount());
        billPaymentResponse.setTran_no(transaction.getTranNo()); // debit transaction Number
        return billPaymentResponse;
    }


    private String generateBillNumber() {
        Random random = new Random();
        long number = 1000000000L + (long)(random.nextDouble() * 9000000000L);
        return "BIL-" + number;
    }

    private String generateTransactionNumber() {
        Random random = new Random();
        long number = 1000000000L + (long)(random.nextDouble() * 9000000000L);
        return "TXN" + number;
    }

    private Transaction registerTransaction(int accountId, BigDecimal amount, LocalDateTime dateTime, String paymentType, boolean drCr) {
        Transaction transaction = new Transaction();

        transaction.setTranNo(generateTransactionNumber());
        transaction.setTranDatetime(dateTime);
        transaction.setAmount(amount);

        if(drCr){
            transaction.setPayeeAccId(accountId);
            transaction.setCredit("Cr");
            transaction.setTransferType("External Payment");
        }else{
            transaction.setCustomerAccId(accountId);
            transaction.setDebit("Dr");
            transaction.setTransferType("Payment made by Customer");
        }

        transaction.setDescription(paymentType);

        return transactionRepository.save(transaction);

    }

}
