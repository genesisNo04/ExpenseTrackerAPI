package com.example.ExpenseTrackerAPI.Service;

import com.example.ExpenseTrackerAPI.Entity.AppUser;
import com.example.ExpenseTrackerAPI.Entity.Expense;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ExpenseService {

    Expense save(Expense expense, AppUser appUser);

    List<Expense> findAllExpenses(AppUser appUser);

    Expense findExpenseById(long id, AppUser appUser);

    Expense findExpenseByTitle(String title, AppUser appUser);

    void deleteExpense(long id, AppUser appUser);

    double expenseSummary(AppUser appUser);

    List<Expense> findAllExpensesInDateRange(AppUser appUser, String filter, LocalDateTime start, LocalDateTime end);
}
