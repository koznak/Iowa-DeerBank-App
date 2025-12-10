package com.deerbank.repository;

import com.deerbank.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Integer> {

    Optional<Loan> findByLoanNo(String loanNo);

    List<Loan> findByUserId(Integer userId);

    List<Loan> findByStatus(String status);

    List<Loan> findByUserIdAndStatus(Integer userId, String status);

    List<Loan> findByLoanType(String loanType);

    @Query("SELECT l FROM Loan l WHERE l.userId = :userId ORDER BY l.applicationDate DESC")
    List<Loan> findLoansByUserIdOrderByDateDesc(@Param("userId") Integer userId);

    @Query("SELECT l FROM Loan l WHERE l.status = 'ACTIVE' AND l.nextPaymentDate < CURRENT_DATE")
    List<Loan> findOverdueLoans();

    @Query("SELECT COUNT(l) FROM Loan l WHERE l.userId = :userId AND l.status = 'ACTIVE'")
    Long countActiveLoansByUserId(@Param("userId") Integer userId);

    @Query("SELECT SUM(l.remainingBalance) FROM Loan l WHERE l.userId = :userId AND l.status = 'ACTIVE'")
    Double getTotalOutstandingBalanceByUserId(@Param("userId") Integer userId);
}