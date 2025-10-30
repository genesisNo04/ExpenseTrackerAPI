package com.example.ExpenseTrackerAPI.Service.Impl;

import com.example.ExpenseTrackerAPI.Entity.AppUser;
import com.example.ExpenseTrackerAPI.Entity.Expense;
import com.example.ExpenseTrackerAPI.Exception.AccessDeniedException;
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
        Expense expense = expenseRepository.findById(id).orElseThrow(() -> new ResourceNotFound("Expense not found."));

        if (!expense.getAppUser().getId().equals(appUser.getId())) {
            throw  new AccessDeniedException("You are not authorized to access this expense. This expense does not belong to you.");
        }

        return expense;
    }

    @Override
    public Expense findExpenseByTitle(String title, AppUser appUser) {
        Expense expense = expenseRepository.findByTitle(title).orElseThrow(() -> new ResourceNotFound("Expense not found."));

        if (!expense.getAppUser().getId().equals(appUser.getId())) {
            throw  new AccessDeniedException("You are not authorized to access this expense. This expense does not belong to you.");
        }

        return expense;
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
        double total = expenses.stream().map(Expense::getAmount).reduce(0.0, Double::sum);
        return Math.round(total * 100.0) / 100.0;
    }

    @Override
    public List<Expense> findAllExpensesInDateRange(AppUser appUser, String filter, LocalDateTime start, LocalDateTime end) {
        LocalDate today = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate;

        switch (filter != null ? filter : "") {
            case "pastWeek":
                startDate = today.minusWeeks(1);
                endDate = today;
                break;
            case "pastMonth":
                startDate = today.minusMonths(1);
                endDate = today;
                break;
            case "last3Months":
                startDate = today.minusMonths(3);
                endDate = today;
                break;
            case "custom":
                if (start == null || end == null) {
                    throw  new IllegalArgumentException("Custom filter requires startDate and endDate");
                }
                startDate = start.toLocalDate();
                endDate = end.toLocalDate();
                break;
            default:
                throw new IllegalArgumentException("Illegal filter, required pastWeek, pastMonth, last3Months, custom");
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay().minusNanos(1);

        return expenseRepository.findByCreatedTimeBetweenAndAppUser(startDateTime, endDateTime, appUser);
    }
}
