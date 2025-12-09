package com.deerbank.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "credential", schema = "u570810680_deerbankdb")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Credential implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "username", unique = true, length = 255)
    private String username;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "admin_type", columnDefinition = "TINYINT")
    private Integer adminType; // 0 = Customer, 1 = Admin/Master

    @Column(name = "status", length = 255)
    private String status; // ACTIVE, INACTIVE, BLOCKED

    @Column(name = "created_date", columnDefinition = "DATETIME")
    private LocalDateTime createdDate;

    @Column(name = "updated_date", columnDefinition = "DATETIME")
    private LocalDateTime updatedDate;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
