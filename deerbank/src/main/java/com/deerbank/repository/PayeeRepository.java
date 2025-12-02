package com.deerbank.repository;

import com.deerbank.entity.Payee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PayeeRepository extends JpaRepository<Payee, Integer> {

//    @Query("SELECT p,a.accountId FROM Payee p JOIN Account a WHERE p.accountId=a.accountId and p.id= :id")
//    Optional<Payee> findByIdAndAcc(Integer id);

    Optional<Payee> findByAccountNo(String accountNo);

}
