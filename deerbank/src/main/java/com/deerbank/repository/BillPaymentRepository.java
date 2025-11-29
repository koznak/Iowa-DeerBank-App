package com.deerbank.repository;

import com.deerbank.entity.BillPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillPaymentRepository extends JpaRepository<BillPayment, Integer> {

    boolean existsByBillPaymentNo(String billPaymentNo);
}
