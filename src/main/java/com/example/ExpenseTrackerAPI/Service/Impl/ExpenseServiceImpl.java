package com.example.ExpenseTrackerAPI.Service.Impl;

import com.example.ExpenseTrackerAPI.Entity.AppUser;
import com.example.ExpenseTrackerAPI.Entity.Expense;
import com.example.ExpenseTrackerAPI.Exception.ResourceNotFound;
import com.example.ExpenseTrackerAPI.Repository.ExpenseRepository;
import com.example.ExpenseTrackerAPI.Service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Override
    public List<Expense> findAllExpenses(AppUser appUser) {
        return expenseRepository.findByAppUser(appUser);
    }

    @Override
    public Expense findExpenseById(long id, AppUser appUser) {
        return expenseRepository.findById(id).orElseThrow(() -> new ResourceNotFound("Expense not found."));
    }

    @Override
    public Expense findExpenseByTitle(String title, AppUser appUser) {
        return expenseRepository.findByTitleAndAppUser(title, appUser).orElseThrow(() -> new ResourceNotFound("Expense not found."));
    }

    @Override
    public Expense save(Expense expense) {
        return expenseRepository.save(expense);
    }

    @Override
    public void deleteExpense(long id, AppUser appUser) {
        Expense expense = findExpenseById(id, appUser);
        expenseRepository.delete(expense);
    }

    @Override
    public double expenseSummary(AppUser appUser) {
        List<Expense> expenses = findAllExpenses(appUser);
        return expenses.stream().map(Expense::getAmount).reduce(0.0, Double::sum);
    }

    @Override
    public List<Expense> findAllExpensesInDateRange(AppUser appUser, String filter, LocalDate start, LocalDate end) {
        LocalDate today = LocalDate.now();

        switch (filter) {
            case "pastWeek":
                start = today.minusWeeks(1);
                end = today;
                break;
            case "pastMonth":
                start = today.minusMonths(1);
                end = today;
                break;
            case "last3Months":
                start = today.minusMonths(3);
                end = today;
                break;
            case "custom":
                if (start == null || end == null) {
                    throw  new IllegalArgumentException("Custom filter requires startDate and endDate");
                }
                break;
            default:
                throw new IllegalArgumentException("Illegal filter, required pastWeek, pastMonth, last3Months, custom");
        }
        return expenseRepository.findByCreatedDateAndAppUser(start, end, appUser);
    }
}
