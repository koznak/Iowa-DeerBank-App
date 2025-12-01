package com.deerbank.repository;

import com.deerbank.entity.Credential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, Integer> {
    Optional<Credential> findByUsername(String username);
    boolean existsByUsername(String username);
}
