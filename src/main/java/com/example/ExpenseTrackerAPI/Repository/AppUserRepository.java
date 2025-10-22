package com.example.ExpenseTrackerAPI.Repository;

import com.example.ExpenseTrackerAPI.Entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
}
