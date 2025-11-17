package com.example.ExpenseTrackerAPI.Controller;

import com.example.ExpenseTrackerAPI.DTO.ExpenseDTO;
import com.example.ExpenseTrackerAPI.DTO.SummaryResponseDTO;
import com.example.ExpenseTrackerAPI.Entity.AuthUser;
import com.example.ExpenseTrackerAPI.Entity.Expense;
import com.example.ExpenseTrackerAPI.Service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/v1/expense")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @PostMapping
    @CacheEvict(value = {"expenseByUser", "expenseSummary"},  key = "#authUser.appUser.id")
    public ResponseEntity<ExpenseDTO> createExpense(@AuthenticationPrincipal AuthUser authUser, @RequestBody ExpenseDTO expenseDTO) {
        Expense expense = new Expense(expenseDTO.getTitle(), expenseDTO.getDescription(), expenseDTO.getAmount(), expenseDTO.getCategory());
        expense.setAppUser(authUser.getAppUser());
        if (expenseDTO.getCreatedTime() != null) {
            expense.setCreatedTime(expenseDTO.getCreatedTime());
        }
        expenseService.save(expense);

        expenseDTO.setCreatedTime(expense.getCreatedTime());
        expenseDTO.setModifiedTime(expense.getLastModifiedTime());

        return ResponseEntity.status(HttpStatus.CREATED).body(expenseDTO);
    }

    @GetMapping("/{id}")
    @Cacheable(value = "expenseId", key = "#authUser.appUser.id + '-' + #id")
    public ResponseEntity<ExpenseDTO> getExpenseById(@AuthenticationPrincipal AuthUser authUser, @PathVariable long id) {
        Expense expenses = expenseService.findExpenseById(id, authUser.getAppUser());
        ExpenseDTO expenseDTO = new ExpenseDTO(expenses.getTitle(), expenses.getDescription(), expenses.getCategory(), expenses.getAmount());
        expenseDTO.setModifiedTime(expenses.getLastModifiedTime());
        expenseDTO.setCreatedTime(expenses.getCreatedTime());
        return ResponseEntity.ok(expenseDTO);
    }

    @GetMapping
    @Cacheable(value = "expenseByUser",
            key = "#authUser.appUser.id")
    public ResponseEntity<List<Expense>> getExpenseInDateRange(@AuthenticationPrincipal AuthUser authUser,
                                                               @RequestParam(required = false) String startDate,
                                                               @RequestParam(required = false) String endDate,
                                                               @RequestParam(required = false, defaultValue = "custom") String filter) {

        LocalDate start = (startDate != null) ? LocalDate.parse(startDate) : LocalDate.of(1970, 1, 1);
        LocalDate end = (endDate != null) ? LocalDate.parse(endDate) : LocalDate.now();

        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.plusDays(1).atStartOfDay().minusNanos(1);

        List<Expense> expenses = expenseService.findAllExpensesInDateRange(authUser.getAppUser(), filter, startDateTime, endDateTime);
        return ResponseEntity.ok(expenses);
    }

    @PutMapping("/{id}")
    @CacheEvict(value = {"expenseByUser", "expenseSummary"}, key = "#authUser.appUser.id")
    public ResponseEntity<ExpenseDTO> updateExpense(@AuthenticationPrincipal AuthUser authUser, @RequestBody ExpenseDTO expenseDTO, @PathVariable long id) {
        Expense expense = expenseService.findExpenseById(id, authUser.getAppUser());

        expense.setTitle(expenseDTO.getTitle());
        expense.setDescription(expenseDTO.getDescription());
        expense.setCategory(expenseDTO.getCategory());
        expense.setAmount(expenseDTO.getAmount());

        expenseService.save(expense);

        expenseDTO.setCreatedTime(expense.getCreatedTime());
        expenseDTO.setModifiedTime(expense.getLastModifiedTime());

        return ResponseEntity.ok(expenseDTO);
    }

    @PatchMapping("/{id}")
    @CacheEvict(value = {"expenseByUser", "expenseSummary"}, key = "#authUser.appUser.id")
    public ResponseEntity<ExpenseDTO> patchExpense(@AuthenticationPrincipal AuthUser authUser, @RequestBody ExpenseDTO expenseDTO, @PathVariable long id) {
        Expense expense = expenseService.findExpenseById(id, authUser.getAppUser());

        if (expenseDTO.getTitle() != null) {
            expense.setTitle(expenseDTO.getTitle());
        }

        if(expenseDTO.getDescription() != null) {
            expense.setDescription(expenseDTO.getDescription());
        }

        if (expenseDTO.getCategory() != null) {
            expense.setCategory(expenseDTO.getCategory());
        }

        if (expenseDTO.getAmount() != null) {
            expense.setAmount(expenseDTO.getAmount());
        }

        expenseService.save(expense);

        ExpenseDTO updateDTO = new ExpenseDTO(expense);

        return ResponseEntity.ok(updateDTO);
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = {"expenseByUser", "expenseSummary"}, key = "#authUser.appUser.id")
    public ResponseEntity<Void> deleteExpense(@AuthenticationPrincipal AuthUser authUser, @PathVariable long id) {

        expenseService.deleteExpense(id, authUser.getAppUser());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    @Cacheable(value = "expenseSummary", key = "#authUser.appUser.id")
    public ResponseEntity<SummaryResponseDTO> getSummary(@AuthenticationPrincipal AuthUser authUser) {
        SummaryResponseDTO summaryResponseDTO = new SummaryResponseDTO(expenseService.expenseSummary(authUser.getAppUser()));
        return ResponseEntity.ok(summaryResponseDTO);
    }
}
