package com.example.ExpenseTrackerAPI.Controller;

import com.example.ExpenseTrackerAPI.DTO.ExpenseDTO;
import com.example.ExpenseTrackerAPI.Entity.AppUser;
import com.example.ExpenseTrackerAPI.Entity.AuthUser;
import com.example.ExpenseTrackerAPI.Entity.Expense;
import com.example.ExpenseTrackerAPI.Service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/v1/expense")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseDTO> createExpense(@AuthenticationPrincipal AuthUser authUser, @RequestBody ExpenseDTO expenseDTO) {
        Expense expense = new Expense(expenseDTO.getTitle(), expenseDTO.getDescription(), expenseDTO.getAmount(), expenseDTO.getCategory());
        expense.setAppUser(authUser.getAppUser());

        expenseService.save(expense);

        return ResponseEntity.status(HttpStatus.CREATED).body(expenseDTO);
    }

    @GetMapping
    public ResponseEntity<List<Expense>> getAllExpense(@AuthenticationPrincipal AuthUser authUser) {
        List<Expense> expenses = expenseService.findAllExpenses(authUser.getAppUser());
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Expense> getExpenseById(@AuthenticationPrincipal AuthUser authUser, @PathVariable long id) {
        Expense expenses = expenseService.findExpenseById(id, authUser.getAppUser());
        return ResponseEntity.ok(expenses);
    }

    @GetMapping
    public ResponseEntity<List<Expense>> getExpenseInDateRange(@AuthenticationPrincipal AuthUser authUser,
                                                               @RequestParam(required = false) String startDate,
                                                               @RequestParam(required = false) String endDate) {

        List<Expense> expenses = expenseService.findAllExpenses(authUser.getAppUser());
        return ResponseEntity.ok(expenses);
    }
}
