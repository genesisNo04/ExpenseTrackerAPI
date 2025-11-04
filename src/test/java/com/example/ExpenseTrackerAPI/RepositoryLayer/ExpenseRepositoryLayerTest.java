package com.example.ExpenseTrackerAPI.RepositoryLayer;

import com.example.ExpenseTrackerAPI.Entity.AppUser;
import com.example.ExpenseTrackerAPI.Entity.AuthUser;
import com.example.ExpenseTrackerAPI.Entity.Expense;
import com.example.ExpenseTrackerAPI.Enum.Category;
import com.example.ExpenseTrackerAPI.Enum.Role;
import com.example.ExpenseTrackerAPI.Repository.AppUserRepository;
import com.example.ExpenseTrackerAPI.Repository.AuthUserRepository;
import com.example.ExpenseTrackerAPI.Repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@DataJpaTest
public class ExpenseRepositoryLayerTest {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private AuthUserRepository authUserRepository;

    private AppUser appUser;

    @BeforeEach
    void setup() {
        // back-reference
        AuthUser authUser = authUserRepository.findByUsername("user1")
                .orElseGet(() -> {
                    AuthUser newAuthUser = new AuthUser();
                    newAuthUser.setUsername("user1");
                    newAuthUser.setPassword("user1");
                    newAuthUser.setEmail("user1@gmail.com");
                    newAuthUser.setRole(Role.ROLE_USER);

                    AppUser appUser = new AppUser(newAuthUser.getUsername(), List.of());
                    appUser.setAuthUser(newAuthUser);  // back-reference
                    newAuthUser.setAppUser(appUser);

                    return authUserRepository.save(newAuthUser);
                });

        this.appUser = authUser.getAppUser();
    }

    @Test
    void shouldFindExpenseByTitleAndAppUser() {
        Expense expense = new Expense("Groceries", "Weekly food", 50.0, Category.GROCERIES);
        expense.setAppUser(appUser);
        expenseRepository.save(expense);

        Optional<Expense> result = expenseRepository.findByTitleAndAppUser(expense.getTitle(), appUser);

        assertTrue(result.isPresent());
        assertEquals(result.get().getTitle(), expense.getTitle());
        assertEquals(result.get().getDescription(), expense.getDescription());
        assertEquals(result.get().getAmount(), expense.getAmount());
        assertEquals(result.get().getCategory(), expense.getCategory());
    }

    @Test
    void shouldFindExpenseByIdAndAppUser() {
        Expense expense = new Expense("Groceries", "Weekly food", 50.0, Category.GROCERIES);
        expense.setAppUser(appUser);
        expenseRepository.save(expense);

        Optional<Expense> result = expenseRepository.findByIdAndAppUser(expense.getId(), appUser);

        assertTrue(result.isPresent());
        assertEquals(expense.getTitle(), result.get().getTitle());
        assertEquals(expense.getDescription(), result.get().getDescription());
        assertEquals(expense.getAmount(), result.get().getAmount());
        assertEquals(expense.getCategory(), result.get().getCategory());
    }

    @Test
    void shouldFindExpenseByAppUser() {
        Expense expense = new Expense("Groceries", "Weekly food", 50.0, Category.GROCERIES);
        expense.setAppUser(appUser);
        expenseRepository.save(expense);

        Expense expense1 = new Expense("Health", "Annual checkup", 100.0, Category.HEALTH);
        expense1.setAppUser(appUser);
        expenseRepository.save(expense1);

        Expense expense2 = new Expense("Electronics", "Bought TV", 300.0, Category.ELECTRONIC);
        expense2.setAppUser(appUser);
        expenseRepository.save(expense2);

        List<Expense> result = expenseRepository.findByAppUser(appUser);

        assertEquals(3, result.size());
        assertEquals(expense.getTitle(), result.getFirst().getTitle());
        assertEquals(expense.getDescription(), result.getFirst().getDescription());
        assertEquals(expense.getAmount(), result.getFirst().getAmount());
        assertEquals(expense.getCategory(), result.getFirst().getCategory());

        assertEquals(expense1.getTitle(), result.get(1).getTitle());
        assertEquals(expense1.getDescription(), result.get(1).getDescription());
        assertEquals(expense1.getAmount(), result.get(1).getAmount());
        assertEquals(expense1.getCategory(), result.get(1).getCategory());

        assertEquals(expense2.getTitle(), result.get(2).getTitle());
        assertEquals(expense2.getDescription(), result.get(2).getDescription());
        assertEquals(expense2.getAmount(), result.get(2).getAmount());
        assertEquals(expense2.getCategory(), result.get(2).getCategory());
    }

    @Test
    void shouldFindExpenseByTitle() {
        Expense expense = new Expense("Groceries", "Weekly food", 50.0, Category.GROCERIES);
        expense.setAppUser(appUser);
        expenseRepository.save(expense);

        Expense expense1 = new Expense("Electronics", "Weekly food", 50.0, Category.ELECTRONIC);
        expense1.setAppUser(appUser);
        expenseRepository.save(expense1);

        Optional<Expense> result = expenseRepository.findByTitle(expense.getTitle());

        assertTrue(result.isPresent());
        assertEquals(result.get().getTitle(), expense.getTitle());
        assertEquals(result.get().getDescription(), expense.getDescription());
        assertEquals(result.get().getAmount(), expense.getAmount());
        assertEquals(result.get().getCategory(), expense.getCategory());
    }

    @Test
    void shouldFindByCreatedTimePastWeek() {
        LocalDateTime now = LocalDateTime.of(2025, 1, 15, 10, 0);

        Expense expense = new Expense("Groceries", "Weekly food", 50.0, Category.GROCERIES);
        expense.setAppUser(appUser);
        expense.setCreatedTime(now.minusHours(2));
        expenseRepository.save(expense);

        Expense expense1 = new Expense("Health", "Annual checkup", 100.0, Category.HEALTH);
        expense1.setAppUser(appUser);
        expense1.setCreatedTime(now.minusWeeks(1).minusHours(1));
        expenseRepository.save(expense1);

        Expense expense2 = new Expense("Electronics", "Bought TV", 300.0, Category.ELECTRONIC);
        expense2.setAppUser(appUser);
        expense2.setCreatedTime(now.minusWeeks(2));
        expenseRepository.save(expense2);


        List<Expense> result = expenseRepository.findByCreatedTimeBetweenAndAppUser(now.minusWeeks(1), now, appUser);

        assertEquals(1, result.size());
        assertEquals(expense.getTitle(), result.getFirst().getTitle());
        assertEquals(expense.getDescription(), result.getFirst().getDescription());
        assertEquals(expense.getAmount(), result.getFirst().getAmount());
        assertEquals(expense.getCategory(), result.getFirst().getCategory());
    }

    @Test
    void shouldFindByCreatedTimePastMonth() {
        LocalDateTime now = LocalDateTime.of(2025, 1, 15, 10, 0);

        Expense expense = new Expense("Groceries", "Weekly food", 50.0, Category.GROCERIES);
        expense.setAppUser(appUser);
        expense.setCreatedTime(now.minusMonths(1));
        expenseRepository.save(expense);

        Expense expense1 = new Expense("Health", "Annual checkup", 100.0, Category.HEALTH);
        expense1.setAppUser(appUser);
        expense1.setCreatedTime(now.minusMonths(2));
        expenseRepository.save(expense1);

        Expense expense2 = new Expense("Electronics", "Bought TV", 300.0, Category.ELECTRONIC);
        expense2.setAppUser(appUser);
        expense2.setCreatedTime(now.minusMonths(4));
        expenseRepository.save(expense2);


        List<Expense> result = expenseRepository.findByCreatedTimeBetweenAndAppUser(now.minusMonths(1), now, appUser);

        assertEquals(1, result.size());
        assertEquals(expense.getTitle(), result.getFirst().getTitle());
        assertEquals(expense.getDescription(), result.getFirst().getDescription());
        assertEquals(expense.getAmount(), result.getFirst().getAmount());
        assertEquals(expense.getCategory(), result.getFirst().getCategory());
    }

    @Test
    void shouldFindByCreatedTimePast3Months() {
        LocalDateTime now = LocalDateTime.of(2025, 1, 15, 10, 0);

        Expense expense = new Expense("Groceries", "Weekly food", 50.0, Category.GROCERIES);
        expense.setAppUser(appUser);
        expense.setCreatedTime(now.minusMonths(1));
        expenseRepository.save(expense);

        Expense expense1 = new Expense("Health", "Annual checkup", 100.0, Category.HEALTH);
        expense1.setAppUser(appUser);
        expense1.setCreatedTime(now.minusMonths(2));
        expenseRepository.save(expense1);

        Expense expense2 = new Expense("Electronics", "Bought TV", 300.0, Category.ELECTRONIC);
        expense2.setAppUser(appUser);
        expense2.setCreatedTime(now.minusMonths(4));
        expenseRepository.save(expense2);

        List<Expense> result = expenseRepository.findByCreatedTimeBetweenAndAppUser(now.minusMonths(3), now, appUser);

        assertEquals(2, result.size());
        assertEquals(expense.getTitle(), result.getFirst().getTitle());
        assertEquals(expense.getDescription(), result.getFirst().getDescription());
        assertEquals(expense.getAmount(), result.getFirst().getAmount());
        assertEquals(expense.getCategory(), result.getFirst().getCategory());
    }

    @Test
    void shouldFindByCreatedTimeCustom() {
        LocalDateTime start = LocalDateTime.of(2025, 1, 15, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 3, 15, 10, 0);

        Expense expense = new Expense("Groceries", "Weekly food", 50.0, Category.GROCERIES);
        expense.setAppUser(appUser);
        expense.setCreatedTime(start.plusDays(2));
        expenseRepository.save(expense);

        Expense expense1 = new Expense("Health", "Annual checkup", 100.0, Category.HEALTH);
        expense1.setAppUser(appUser);
        expense1.setCreatedTime(start.minusMonths(2));
        expenseRepository.save(expense1);

        Expense expense2 = new Expense("Electronics", "Bought TV", 300.0, Category.ELECTRONIC);
        expense2.setAppUser(appUser);
        expense2.setCreatedTime(end.minusWeeks(2));
        expenseRepository.save(expense2);

        List<Expense> result = expenseRepository.findByCreatedTimeBetweenAndAppUser(start, end, appUser);

        assertEquals(2, result.size());
        assertEquals(expense.getTitle(), result.getFirst().getTitle());
        assertEquals(expense.getDescription(), result.getFirst().getDescription());
        assertEquals(expense.getAmount(), result.getFirst().getAmount());
        assertEquals(expense.getCategory(), result.getFirst().getCategory());
    }

}
