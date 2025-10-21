package com.example.ExpenseTrackerAPI.Repository;

import com.example.ExpenseTrackerAPI.Entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {

    Optional<AuthUser> findByUsername(String username);

    Optional<AuthUser> findByEmail(String email);

    boolean existByUsername(String username);

    boolean existByEmail(String email);
}
