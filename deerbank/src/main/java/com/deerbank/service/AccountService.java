package com.deerbank.service;

import com.deerbank.dto.*;
import com.deerbank.entity.Account;
import com.deerbank.entity.Transaction;
import com.deerbank.entity.User;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
public interface AccountService{


    AccountResponse createAccount(CreateAccountRequest request);

    TransactionResponse deposit(DepositRequest request);

    TransactionResponse withdrawal(WithdrawalRequest request);

    List<TransactionHistoryDTO> getTransactions(GetTransactionsRequest request);

    TransferResponse transferBetweenAccounts(TransferRequest request);

    BalanceInquiryResponse getBalance(BalanceInquiryRequest request);
}
