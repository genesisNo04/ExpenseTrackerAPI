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

import java.util.ArrayList;
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

    private AuthUser authUser;

    private AppUser appUser;

    @BeforeEach
    void setup() {
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
    void shouldFindExpenseByTitle() {
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

}
