package com.deerbank.service.impl;

import com.deerbank.dto.PayeeRequest;
import com.deerbank.dto.PayeeResponse;
import com.deerbank.entity.Account;
import com.deerbank.entity.Payee;
import com.deerbank.exception.ResourceNotFoundException;
import com.deerbank.repository.AccountRepository;
import com.deerbank.repository.PayeeRepository;
import com.deerbank.service.PayeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class PayeeServiceImpl implements PayeeService {

    @Autowired
    private PayeeRepository  payeeRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public List<PayeeResponse> getAllPayees() {
        return payeeRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public PayeeResponse createPayee(PayeeRequest payeeRequest) {

        Account foundAcc = accountRepository.findByAccountNoAndStatus(payeeRequest.getAccountNo(), "ACTIVE").orElseThrow(
                () -> new ResourceNotFoundException("Account Not Found")
        );

        Optional<Payee> checkAccount = payeeRepository.findByAccountNo(payeeRequest.getAccountNo());

        if(checkAccount.isPresent()){
            throw new RuntimeException("Payee account already exist");
        }

        Payee payee = new Payee();
        payee.setName(payeeRequest.getName());
        payee.setEmail(payeeRequest.getEmail());
        payee.setNickname(payeeRequest.getNickname());
        payee.setPhone(payeeRequest.getPhone());
        payee.setAccountNo(payeeRequest.getAccountNo());
        payee.setStatus("Active");
        payee.setUserUserId(foundAcc.getSerUserId());


        Payee createdPayee = payeeRepository.save(payee);


        return convertToResponse(createdPayee);
    }

    @Override
    public void deletePayee(int id) {
        Payee payee = payeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payee not found"));

        payee.setStatus("Inactive");

        payeeRepository.save(payee);

    }

    @Override
    public PayeeResponse updatePayee(PayeeRequest payeeRequest, int id) {
        Account foundAcc=accountRepository.findByAccountNoAndStatus(payeeRequest.getAccountNo(), "ACTIVE").orElseThrow(
                () -> new ResourceNotFoundException("Account Not Found")
        );
        Payee existingPayee= payeeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Your account have some issue. Contact to the Banker!"));


        Payee payee = new Payee();
        payee.setId(existingPayee.getId());
        payee.setName(payeeRequest.getName());
        payee.setEmail(payeeRequest.getEmail());
        payee.setNickname(payeeRequest.getNickname());
        payee.setPhone(payeeRequest.getPhone());
        payee.setAccountNo(existingPayee.getAccountNo());
        payee.setStatus("Active");
        payee.setUserUserId(foundAcc.getSerUserId());


        Payee createdPayee=payeeRepository.save(payee);


        return convertToResponse(createdPayee);
    }

    @Override
    public PayeeResponse getPayee(int id) {
        Payee payeeData=payeeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Payee ID cannot found"));

        PayeeResponse payeeResponse= convertToResponse(payeeData);
        payeeResponse.setAccountNo(accountRepository.findByAccountNo(payeeData.getAccountNo()).get().getAccountNo());

        return payeeResponse;
    }

    private PayeeResponse convertToResponse(Payee payee) {
        PayeeResponse response = new PayeeResponse();
        response.setPayeeId(payee.getId());
        response.setName(payee.getName());
        response.setNickname(payee.getNickname());
        response.setEmail(payee.getEmail());
        response.setPhone(payee.getPhone());
        response.setAccountNo(payee.getAccountNo());
        response.setStatus(payee.getStatus());
        response.setUserId(payee.getUserUserId());
        return response;
    }

}
