package com.deerbank.service.impl;

import com.deerbank.dto.PayeeRequest;
import com.deerbank.dto.PayeeResponse;
import com.deerbank.entity.Payee;
import com.deerbank.exception.ResourceNotFoundException;
import com.deerbank.repository.AccountRepository;
import com.deerbank.repository.PayeeRepository;
import com.deerbank.service.PayeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

        accountRepository.findByAccountNo(payeeRequest.getAccountNo()).orElseThrow(
                () -> new ResourceNotFoundException("Account Not Found")
        );


        Payee payee = new Payee();
        payee.setName(payeeRequest.getName());
        payee.setEmail(payeeRequest.getEmail());
        payee.setNickname(payeeRequest.getNickname());
        payee.setPhone(payeeRequest.getPhone());
        payee.setAccountNo(payeeRequest.getAccountNo());
        payee.setStatus("Active");
        payee.setUserUserId(payeeRequest.getUserId());


        Payee createdPayee=payeeRepository.save(payee);


        return convertToResponse(createdPayee);
    }

    @Override
    public void deletePayee(int id) {
        Payee payee = payeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payee not found"));

        payee.setStatus("Inactive");

        payeeRepository.save(payee);

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
