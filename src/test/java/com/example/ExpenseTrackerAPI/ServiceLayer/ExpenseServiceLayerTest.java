package com.example.ExpenseTrackerAPI.ServiceLayer;

import com.example.ExpenseTrackerAPI.Entity.AppUser;
import com.example.ExpenseTrackerAPI.Entity.Expense;
import com.example.ExpenseTrackerAPI.Enum.Category;
import com.example.ExpenseTrackerAPI.Exception.AccessDeniedException;
import com.example.ExpenseTrackerAPI.Exception.ResourceNotFound;
import com.example.ExpenseTrackerAPI.Repository.ExpenseRepository;
import com.example.ExpenseTrackerAPI.Service.Impl.ExpenseServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceLayerTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    @Test
    void shouldReturnExpenseWithId() {
        AppUser appUser = new AppUser();
        appUser.setId(1L);
        long id = 8;

        Expense expense = new Expense("Test title 1", "Test description 1", 100.0, Category.CLOTHING);
        expense.setAppUser(appUser);

        when(expenseRepository.findById(id)).thenReturn(Optional.of(expense));

        Expense testExpense = expenseService.findExpenseById(id, appUser);

        assertEquals("Test title 1", testExpense.getTitle());
        assertEquals("Test description 1", testExpense.getDescription());
        assertEquals(100.0, testExpense.getAmount());
        assertEquals(Category.CLOTHING, testExpense.getCategory());
    }

    @Test
    void shouldThrowErrorExpenseWithId() {
        AppUser appUser = new AppUser();
        appUser.setId(1L);
        long id = 8L;

        when(expenseRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFound.class, () -> {
            expenseService.findExpenseById(id, appUser);
        });
    }

    @Test
    void shouldThrowAccessDeniedErrorExpenseWithId() {
        AppUser appUser = new AppUser();
        appUser.setId(1L);
        long id = 8;

        AppUser appUser1 = new AppUser();
        appUser1.setId(2L);

        Expense expense = new Expense("Test title 1", "Test description 1", 100.0, Category.CLOTHING);
        expense.setAppUser(appUser1);

        when(expenseRepository.findById(id)).thenReturn(Optional.of(expense));

        assertThrows(AccessDeniedException.class, () -> {
            expenseService.findExpenseById(id, appUser);
        });
    }

    @Test
    void shouldReturnExpenseWithTitle() {
        AppUser appUser = new AppUser();
        appUser.setId(1L);
        long id = 8;

        Expense expense = new Expense("Test title 1", "Test description 1", 100.0, Category.CLOTHING);
        expense.setAppUser(appUser);

        when(expenseRepository.findByTitle(expense.getTitle())).thenReturn(Optional.of(expense));

        Expense testExpense = expenseService.findExpenseByTitle(expense.getTitle(), appUser);

        assertEquals("Test title 1", testExpense.getTitle());
        assertEquals("Test description 1", testExpense.getDescription());
        assertEquals(100.0, testExpense.getAmount());
        assertEquals(Category.CLOTHING, testExpense.getCategory());
    }

    @Test
    void shouldThrowErrorExpenseWithTitle() {
        AppUser appUser = new AppUser();
        appUser.setId(1L);
        String title = "test title";

        when(expenseRepository.findByTitle(title)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFound.class, () -> {
            expenseService.findExpenseByTitle(title, appUser);
        });
    }

    @Test
    void shouldThrowAccessDeniedErrorExpenseWithTitle() {
        AppUser appUser = new AppUser();
        appUser.setId(1L);
        String title = "test title";

        AppUser appUser1 = new AppUser();
        appUser1.setId(2L);

        Expense expense = new Expense(title, "Test description 1", 100.0, Category.CLOTHING);
        expense.setAppUser(appUser1);

        when(expenseRepository.findByTitle(title)).thenReturn(Optional.of(expense));

        assertThrows(AccessDeniedException.class, () -> {
            expenseService.findExpenseByTitle(title, appUser);
        });
    }

    @Test
    void shouldReturnExpenseAfterSave() {
        AppUser appUser = new AppUser();
        appUser.setId(1L);

        Expense expense = new Expense("Test title", "Test description 1", 100.0, Category.CLOTHING);
        expense.setAppUser(appUser);

        when(expenseRepository.save(expense)).thenReturn(expense);

        Expense expenseResponse = expenseService.save(expense, appUser);

        assertEquals("Test title", expenseResponse.getTitle());
        assertEquals("Test description 1", expenseResponse.getDescription());
        assertEquals(100.0, expenseResponse.getAmount());
        assertEquals(Category.CLOTHING, expenseResponse.getCategory());
    }

    @Test
    void shouldDeleteExpense() {
        AppUser appUser = new AppUser();
        appUser.setId(1L);
        long expenseId = 8L;

        Expense expense = new Expense("Test title 1", "Test description 1", 100.0, Category.CLOTHING);
        expense.setAppUser(appUser);

        when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));

        expenseService.deleteExpense(expenseId, appUser);

        verify(expenseRepository, times(1)).delete(expense);
    }

    @Test
    void shouldThrowErrorNoResourceFoundDeleteExpense() {
        AppUser appUser = new AppUser();
        appUser.setId(1L);
        Long id = 8L;

        when(expenseRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFound.class, () -> {
            expenseService.deleteExpense(id, appUser);
        });
    }

    @Test
    void shouldThrowAccessDeniedDeleteExpense() {
        AppUser appUser = new AppUser();
        appUser.setId(1L);
        long id = 8L;

        AppUser appUser1 = new AppUser();
        appUser1.setId(2L);

        Expense expense = new Expense("test title", "Test description 1", 100.0, Category.CLOTHING);
        expense.setAppUser(appUser1);

        when(expenseRepository.findById(id)).thenReturn(Optional.of(expense));

        assertThrows(AccessDeniedException.class, () -> {
            expenseService.deleteExpense(id, appUser);
        });
    }

    @Test
    void shouldReturnSummaryAllExpense() {
        AppUser appUser = new AppUser();
        appUser.setId(1L);

        Expense expense = new Expense("test title 1", "Test description 1", 100.0, Category.CLOTHING);
        expense.setAppUser(appUser);

        Expense expense1 = new Expense("test title 2", "Test description 2", 300.0, Category.CLOTHING);
        expense1.setAppUser(appUser);

        Expense expense2 = new Expense("test title 3", "Test description 3", 150.0, Category.CLOTHING);
        expense2.setAppUser(appUser);

        when(expenseRepository.findByAppUser(appUser)).thenReturn(List.of(expense, expense1, expense2));

        assertEquals(550.00, expenseService.expenseSummary(appUser));
    }

    @Test
    void shouldReturnZeroSummaryAllExpense() {
        AppUser appUser = new AppUser();
        appUser.setId(1L);

        when(expenseRepository.findByAppUser(appUser)).thenReturn(List.of());

        assertEquals(0.0, expenseService.expenseSummary(appUser));
    }

    @Test
    void shouldReturnExpenseInDateRange() {
        AppUser appUser = new AppUser();
        appUser.setId(1L);

        Expense expense1 = new Expense("test title 2", "Test description 2", 300.0, Category.CLOTHING);
        expense1.setAppUser(appUser);

        LocalDate start = LocalDate.parse("2025-10-20");
        LocalDate end = LocalDate.parse("2025-10-30");

        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.plusDays(1).atStartOfDay().minusNanos(1);

        when(expenseRepository.findByCreatedTimeBetweenAndAppUser(startDateTime, endDateTime, appUser)).thenReturn(List.of(expense1));

        List<Expense> expectedExpense = expenseService.findAllExpensesInDateRange(appUser, "custom", startDateTime, endDateTime);

        assertEquals(1, expectedExpense.size());
        assertEquals("test title 2", expectedExpense.getFirst().getTitle());
        assertEquals(300.0, expectedExpense.getFirst().getAmount());
    }

    @Test
    void shouldThrowInvalidArgumentInDateRange() {
        AppUser appUser = new AppUser();
        appUser.setId(1L);

        LocalDate start = LocalDate.parse("2025-10-20");
        LocalDate end = LocalDate.parse("2025-10-30");

        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.plusDays(1).atStartOfDay().minusNanos(1);

        assertThrows(IllegalArgumentException.class, () -> {
            expenseService.findAllExpensesInDateRange(appUser, "invalid filter", startDateTime, endDateTime);
        });
    }

    @Test
    void shouldThrowInvalidArgumentForNullDateInDateRange() {
        AppUser appUser = new AppUser();
        appUser.setId(1L);

        LocalDate start = LocalDate.parse("2025-10-20");
        LocalDate end = LocalDate.parse("2025-10-30");

        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.plusDays(1).atStartOfDay().minusNanos(1);

        assertThrows(IllegalArgumentException.class, () -> {
            expenseService.findAllExpensesInDateRange(appUser, "custom", null, null);
        });
    }

    @Test
    void shouldReturnExpensePastWeek() {
        AppUser appUser = new AppUser();
        appUser.setId(1L);

        Expense expense1 = new Expense("test title 2", "Test description 2", 300.0, Category.CLOTHING);
        expense1.setAppUser(appUser);

        when(expenseRepository.findByCreatedTimeBetweenAndAppUser(any(LocalDateTime.class), any(LocalDateTime.class), eq(appUser))).thenReturn(List.of(expense1));

        List<Expense> resultExpense = expenseService.findAllExpensesInDateRange(appUser, "pastWeek", null, null);

        assertEquals("test title 2", resultExpense.getFirst().getTitle());
        assertEquals("Test description 2", resultExpense.getFirst().getDescription());
        assertEquals(300.0, resultExpense.getFirst().getAmount());
        assertEquals(Category.CLOTHING, resultExpense.getFirst().getCategory());
    }

    @Test
    void shouldReturnExpensePastMonth() {
        AppUser appUser = new AppUser();
        appUser.setId(1L);

        Expense expense1 = new Expense("test title 2", "Test description 2", 300.0, Category.CLOTHING);
        expense1.setAppUser(appUser);

        when(expenseRepository.findByCreatedTimeBetweenAndAppUser(any(LocalDateTime.class), any(LocalDateTime.class), eq(appUser))).thenReturn(List.of(expense1));

        List<Expense> resultExpense = expenseService.findAllExpensesInDateRange(appUser, "pastMonth", null, null);

        assertEquals("test title 2", resultExpense.getFirst().getTitle());
        assertEquals("Test description 2", resultExpense.getFirst().getDescription());
        assertEquals(300.0, resultExpense.getFirst().getAmount());
        assertEquals(Category.CLOTHING, resultExpense.getFirst().getCategory());
    }

    @Test
    void shouldReturnExpensePastThreeMonths() {
        AppUser appUser = new AppUser();
        appUser.setId(1L);

        Expense expense1 = new Expense("test title 2", "Test description 2", 300.0, Category.CLOTHING);
        expense1.setAppUser(appUser);

        when(expenseRepository.findByCreatedTimeBetweenAndAppUser(any(LocalDateTime.class), any(LocalDateTime.class), eq(appUser))).thenReturn(List.of(expense1));

        List<Expense> resultExpense = expenseService.findAllExpensesInDateRange(appUser, "last3Months", null, null);

        assertEquals("test title 2", resultExpense.getFirst().getTitle());
        assertEquals("Test description 2", resultExpense.getFirst().getDescription());
        assertEquals(300.0, resultExpense.getFirst().getAmount());
        assertEquals(Category.CLOTHING, resultExpense.getFirst().getCategory());
    }

    @Test
    void shouldUpdateExpense() {
        AppUser appUser = new AppUser();
        appUser.setId(1L);

        Expense expense = new Expense("test title update", "Test description update", 300.0, Category.CLOTHING);
        expense.setAppUser(appUser);

        when(expenseRepository.save(expense)).thenReturn(expense);

        Expense expenseExpected = expenseService.save(expense, appUser);

        assertEquals(expenseExpected.getTitle(), expense.getTitle());
        assertEquals(expenseExpected.getDescription(), expense.getDescription());
        assertEquals(expenseExpected.getAmount(), expense.getAmount());
        assertEquals(expenseExpected.getCategory(), expense.getCategory());
    }
}
