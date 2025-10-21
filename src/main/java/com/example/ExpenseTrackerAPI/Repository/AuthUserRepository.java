package com.example.ExpenseTrackerAPI.Repository;

import com.example.ExpenseTrackerAPI.Entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {
}
