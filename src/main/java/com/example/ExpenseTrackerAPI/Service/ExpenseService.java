package com.example.ExpenseTrackerAPI.Service;

import com.example.ExpenseTrackerAPI.Entity.Expense;

import java.util.List;

public interface ExpenseService {

    Expense save(Expense expense);

    List<Expense> findAllExpenses();

    Expense findExpenseById(long id);

    Expense findExpenseByTitle(String title);

    void deleteExpense(long id);

    void expenseSummary();
}
