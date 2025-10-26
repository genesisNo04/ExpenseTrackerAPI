package com.example.ExpenseTrackerAPI.Repository;

import com.example.ExpenseTrackerAPI.Entity.AppUser;
import com.example.ExpenseTrackerAPI.Entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    Optional<Expense> findByTitleAndAppUser(String title, AppUser appUser);

    Optional<Expense> findByIdAndAppUser(long id, AppUser appUser);

    List<Expense> findByAppUser(AppUser appUser);

    List<Expense> findByCreatedDateAndAppUser(LocalDate startDate, LocalDate endDate, AppUser appUser);


}
