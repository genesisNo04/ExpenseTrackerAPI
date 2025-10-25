package com.example.ExpenseTrackerAPI.Service.Impl;

import com.example.ExpenseTrackerAPI.Entity.AppUser;
import com.example.ExpenseTrackerAPI.Entity.Expense;
import com.example.ExpenseTrackerAPI.Exception.ResourceNotFound;
import com.example.ExpenseTrackerAPI.Repository.ExpenseRepository;
import com.example.ExpenseTrackerAPI.Service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public Expense findExpenseByTitle(String title) {
        return expenseRepository.findByTitle(title).orElseThrow(() -> new ResourceNotFound("Expense not found."));
    }

    @Override
    public void deleteExpense(long id) {
//        Expense expense = findExpenseById(id);
//        expenseRepository.delete(expense);
    }

    @Override
    public void expenseSummary() {

    }

    @Override
    public Expense save(Expense expense) {
        return expenseRepository.save(expense);
    }
}
