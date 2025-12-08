package com.deerbank.repository;

import com.deerbank.entity.LoanPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanPaymentRepository extends JpaRepository<LoanPayment, Integer> {

    Optional<LoanPayment> findByPaymentNo(String paymentNo);

    List<LoanPayment> findByLoanId(Integer loanId);

    List<LoanPayment> findByLoanIdOrderByPaymentDateDesc(Integer loanId);

    List<LoanPayment> findByPaymentStatus(String paymentStatus);

    @Query("SELECT lp FROM LoanPayment lp WHERE lp.loanId = :loanId AND lp.paymentStatus = 'COMPLETED' ORDER BY lp.paymentDate DESC")
    List<LoanPayment> findCompletedPaymentsByLoanId(@Param("loanId") Integer loanId);

    @Query("SELECT SUM(lp.paymentAmount) FROM LoanPayment lp WHERE lp.loanId = :loanId AND lp.paymentStatus = 'COMPLETED'")
    Double getTotalPaymentsByLoanId(@Param("loanId") Integer loanId);

    @Query("SELECT COUNT(lp) FROM LoanPayment lp WHERE lp.loanId = :loanId AND lp.paymentStatus = 'COMPLETED'")
    Long countPaymentsByLoanId(@Param("loanId") Integer loanId);
}
