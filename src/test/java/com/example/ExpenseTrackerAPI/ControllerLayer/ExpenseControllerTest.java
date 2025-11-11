package com.example.ExpenseTrackerAPI.ControllerLayer;

import com.example.ExpenseTrackerAPI.Config.JwtAuthenticationFilter;
import com.example.ExpenseTrackerAPI.Config.JwtUtil;
import com.example.ExpenseTrackerAPI.Controller.ExpenseController;
import com.example.ExpenseTrackerAPI.Entity.AppUser;
import com.example.ExpenseTrackerAPI.Entity.AuthUser;
import com.example.ExpenseTrackerAPI.Entity.Expense;
import com.example.ExpenseTrackerAPI.Enum.Category;
import com.example.ExpenseTrackerAPI.Enum.Role;
import com.example.ExpenseTrackerAPI.Exception.CredentialsNotFound;
import com.example.ExpenseTrackerAPI.Exception.ResourceNotFound;
import com.example.ExpenseTrackerAPI.Handler.GlobalExceptionHandler;
import com.example.ExpenseTrackerAPI.Service.ExpenseService;
import com.example.ExpenseTrackerAPI.Service.Impl.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpenseController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExpenseService expenseService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private AppUser createAppUser() {
        AuthUser authUser = new AuthUser("user", "user", "user@gmail.com", Role.ROLE_USER);
        AppUser appUser = new AppUser("user", new ArrayList<Expense>());
        authUser.setAppUser(appUser);
        appUser.setAuthUser(authUser);
        return appUser;
    }

    private Expense createExpense(String title, String description, Double amount, Category category, LocalDateTime created, LocalDateTime modified) {
        Expense expense = new Expense();
        expense.setTitle(title != null ? title : "Test expense 1");
        expense.setDescription(description != null ? description : "Test expense 1 description");
        expense.setAmount(amount != null ? amount : 105.5);
        expense.setCategory(category != null ? category : Category.GROCERIES);
        expense.setCreatedTime(created != null ? created : LocalDateTime.now());
        expense.setLastModifiedTime(modified != null ? modified : LocalDateTime.now());

        return expense;
    }

    @Test
    void shouldCreateExpenseSuccessfully() throws Exception {
        AppUser appUser = createAppUser();
        AuthUser authUser = appUser.getAuthUser();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Expense expense = new Expense("Expense 5 user1", "expense 5 description user1", 80.39, Category.GROCERIES);
        expense.setAppUser(appUser);
        when(expenseService.save(expense)).thenReturn(expense);

        mockMvc.perform(post("/v1/expense")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                            {
                                 "title": "Expense 5 user1",
                                 "description": "expense 5 description user1",
                                 "category": "GROCERIES",
                                 "amount": "80.39"
                             }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Expense 5 user1"))
                .andExpect(jsonPath("$.description").value("expense 5 description user1"))
                .andExpect(jsonPath("$.category").value("GROCERIES"))
                .andExpect(jsonPath("$.amount").value("80.39"));
    }

    @Test
    void shouldReturnExpenseWithId() throws Exception {
        AppUser appUser = createAppUser();
        AuthUser authUser = appUser.getAuthUser();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Expense expense = createExpense(null, null, null, null, null, null);
        expense.setAppUser(appUser);
        expense.setId(1);

        when(expenseService.findExpenseById(expense.getId(), expense.getAppUser())).thenReturn(expense);


        mockMvc.perform(get("/v1/expense/" + expense.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test expense 1"))
                .andExpect(jsonPath("$.description").value("Test expense 1 description"))
                .andExpect(jsonPath("$.amount").value("105.5"))
                .andExpect(jsonPath("$.category").value("GROCERIES"))
                .andExpect(jsonPath("$.createdTime").exists())
                .andExpect(jsonPath("$.modifiedTime").exists());

    }

    @Test
    void shouldThrowErrorExpenseWithIdNotFound() throws Exception {
        AppUser appUser = createAppUser();
        AuthUser authUser = appUser.getAuthUser();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(expenseService.findExpenseById(1, appUser)).thenThrow(new ResourceNotFound("Expense not found."));

        mockMvc.perform(get("/v1/expense/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Expense not found."));
    }

    @Test
    void shouldReturnExpensePastWeek() throws Exception {
        AppUser appUser = createAppUser();
        AuthUser authUser = appUser.getAuthUser();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Expense expense = createExpense(null, null, null, null, null, null);
        expense.setAppUser(appUser);
        expense.setId(1);

        Expense expense1 = createExpense(null, null, null, null, null, null);
        expense1.setAppUser(appUser);
        expense1.setId(2);

        when(expenseService.findAllExpensesInDateRange(
                eq(appUser), eq("pastWeek"), any(), any()))
                .thenReturn(List.of(expense, expense1));

        mockMvc.perform(get("/v1/expense?filter=pastWeek"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].title").value("Test expense 1"))
                .andExpect(jsonPath("$[0].description").value("Test expense 1 description"))
                .andExpect(jsonPath("$[0].amount").value("105.5"))
                .andExpect(jsonPath("$[0].category").value("GROCERIES"))
                .andExpect(jsonPath("$[0].createdTime").exists())
                .andExpect(jsonPath("$[0].lastModifiedTime").exists())
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].title").value("Test expense 1"))
                .andExpect(jsonPath("$[1].description").value("Test expense 1 description"))
                .andExpect(jsonPath("$[1].amount").value("105.5"))
                .andExpect(jsonPath("$[1].category").value("GROCERIES"))
                .andExpect(jsonPath("$[1].createdTime").exists())
                .andExpect(jsonPath("$[1].lastModifiedTime").exists());
    }

    @Test
    void shouldThrowErrorIncorrectFilter() throws Exception {
        AppUser appUser = createAppUser();
        AuthUser authUser = appUser.getAuthUser();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(expenseService.findAllExpensesInDateRange(
                eq(appUser), eq("unknown"), any(), any()))
                .thenThrow(new IllegalArgumentException("Illegal filter, required pastWeek, pastMonth, last3Months, custom"));

        mockMvc.perform(get("/v1/expense?filter=unknown"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Illegal filter, required pastWeek, pastMonth, last3Months, custom"));
    }

    @Test
    void shouldThrowErrorMissingDate() throws Exception {
        AppUser appUser = createAppUser();
        AuthUser authUser = appUser.getAuthUser();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(expenseService.findAllExpensesInDateRange(
                eq(appUser), eq("custom"), any(), any()))
                .thenThrow(new IllegalArgumentException("Custom filter requires startDate and endDate"));

        mockMvc.perform(get("/v1/expense?filter=custom"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Custom filter requires startDate and endDate"));
    }

    @Test
    void shouldReturnUpdateExpense() throws Exception {
        AppUser appUser = createAppUser();
        AuthUser authUser = appUser.getAuthUser();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Expense expense = createExpense(null, null, null, null, null, null);
        expense.setAppUser(appUser);
        expense.setId(1);

        when(expenseService.findExpenseById(expense.getId(), expense.getAppUser())).thenReturn(expense);
        when(expenseService.save(expense)).thenReturn(expense);

        mockMvc.perform(put("/v1/expense/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                 "title": "Expense 5 user1",
                                 "description": "expense 5 description user1",
                                 "category": "GROCERIES",
                                 "amount": "80.39"
                             }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Expense 5 user1"))
                .andExpect(jsonPath("$.description").value("expense 5 description user1"))
                .andExpect(jsonPath("$.category").value("GROCERIES"))
                .andExpect(jsonPath("$.amount").value("80.39"));
    }

    @Test
    void shouldReturnPartiallyUpdateExpense() throws Exception {
        AppUser appUser = createAppUser();
        AuthUser authUser = appUser.getAuthUser();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Expense expense = createExpense(null, null, null, null, null, null);
        expense.setAppUser(appUser);
        expense.setId(1);

        when(expenseService.findExpenseById(expense.getId(), expense.getAppUser())).thenReturn(expense);
        when(expenseService.save(expense)).thenReturn(expense);

        mockMvc.perform(put("/v1/expense/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                 "title": "Expense 5 user1",
                                 "description": "expense 5 description user1",
                                 "category": "GROCERIES",
                                 "amount": "80.39"
                             }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Expense 5 user1"))
                .andExpect(jsonPath("$.description").value("expense 5 description user1"))
                .andExpect(jsonPath("$.category").value("GROCERIES"))
                .andExpect(jsonPath("$.amount").value("80.39"));
    }

    @Test
    void shouldReturnMessageDeleteExpense() throws Exception {
        AppUser appUser = createAppUser();
        AuthUser authUser = appUser.getAuthUser();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Expense expense = createExpense(null, null, null, null, null, null);
        expense.setAppUser(appUser);
        expense.setId(1);

        when(expenseService.findExpenseById(expense.getId(), expense.getAppUser())).thenReturn(expense);

        mockMvc.perform(delete("/v1/expense/1"))
                .andExpect(status().isNoContent());

        verify(expenseService).deleteExpense(expense.getId(), appUser);
    }

    @Test
    void shouldThrowErrorDeleteNotFoundExpenseExpense() throws Exception {
        AppUser appUser = createAppUser();
        AuthUser authUser = appUser.getAuthUser();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        doThrow(new ResourceNotFound("Expense not found.")).when(expenseService).deleteExpense(1L, appUser);

        mockMvc.perform(delete("/v1/expense/1"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.error").value("Expense not found."));
    }
}
