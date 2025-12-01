package com.deerbank.service.impl;

import com.deerbank.dto.BillPaymentRequest;
import com.deerbank.dto.BillPaymentResponse;
import com.deerbank.entity.BillPayment;
import com.deerbank.entity.Transaction;
import com.deerbank.repository.BillPaymentRepository;
import com.deerbank.service.AccountService;
import com.deerbank.service.BillPaymentService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class BillPaymentServiceImpl implements BillPaymentService {

    @Autowired
    private BillPaymentRepository billPaymentRepository;

    @Autowired
    private AccountService accountService;

    @Override
    @Transactional
    public BillPaymentResponse createBillPayment(BillPaymentRequest billPaymentRequest) {

        //saving bill payment type in DB
        String billNo = generateBillNumber();
        BillPayment billPayment = new BillPayment();
        billPayment.setBillPaymentNo(billNo);
        billPayment.setPayment_type(billPaymentRequest.getPayment_type());
        billPayment.setAmount(billPaymentRequest.getAmount());
        billPayment.setSchedular_type(billPaymentRequest.getSchedular_type());
        billPayment.setSchedule_date(billPayment.getSchedule_date());
        billPayment.setSchedule_date(billPayment.getSchedule_date());
        billPayment.setCreated_date(LocalDateTime.now());
        billPayment.setUpdated_date(LocalDateTime.now());

        if (billPaymentRequest.getPayment_type().equals("ONCE")) {
            billPayment.setStatus("DONE");
        } else {
            billPayment.setStatus("ACTIVE");
        }

        billPayment.setSer_payee_id(billPaymentRequest.getPayee_id());

        BillPayment billPaymentResult=billPaymentRepository.save(billPayment);


        //saving all the transaction in DB
        Transaction transaction = new Transaction();
        if (billPaymentRequest.getPayment_type().equals("ONCE")) {
            transaction=accountService.transferBillPayment(billPaymentRequest.getFrom_account_no(), billPaymentRequest.getTo_account_no(),
                    billPaymentRequest.getAmount(), billPaymentRequest.getDescription(), billPaymentResult.getId());
        }

        BillPaymentResponse billPaymentResponse=new BillPaymentResponse();
        if(billPaymentResult != null){
            billPaymentResponse.setBill_payment_no(billPayment.getBillPaymentNo());
            billPaymentResponse.setAmount(billPayment.getAmount());
            billPaymentResponse.setTran_no(transaction.getTranNo());
        }
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
}
