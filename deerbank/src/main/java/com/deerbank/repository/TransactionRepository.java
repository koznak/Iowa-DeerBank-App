package com.deerbank.repository;

import com.deerbank.dto.TransactionHistoryDTO;
import com.deerbank.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    @Query("SELECT t FROM transaction t WHERE t.transfer_acc_Id = :accountId OR t.received_acc_id = :accountId ORDER BY t.tranDatetime DESC")
    List<Transaction> findByAccountId(Integer accountId);

}
