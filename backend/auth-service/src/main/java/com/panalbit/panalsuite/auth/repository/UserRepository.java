package com.panalbit.panalsuite.auth.repository;

import com.panalbit.panalsuite.auth.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByUsername(String username);
    List<UserAccount> findAllByEmailIgnoreCase(String email);
    boolean existsByUsername(String username);
}
