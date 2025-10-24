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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/expense")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseDTO> createExpense(@RequestBody ExpenseDTO expenseDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        AppUser appUser = authUser.getAppUser();

        Expense expense = new Expense(expenseDTO.getTitle(), expenseDTO.getDescription(), expenseDTO.getAmount(), expenseDTO.getCategory());
        expense.setAppUser(appUser);

        expenseService.save(expense);

        return ResponseEntity.status(HttpStatus.CREATED).body(expenseDTO);
    }
}
