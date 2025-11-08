package com.example.ExpenseTrackerAPI.ControllerLayer;

import com.example.ExpenseTrackerAPI.Config.JwtAuthenticationFilter;
import com.example.ExpenseTrackerAPI.Config.JwtUtil;
import com.example.ExpenseTrackerAPI.Controller.ExpenseController;
import com.example.ExpenseTrackerAPI.Entity.AppUser;
import com.example.ExpenseTrackerAPI.Entity.AuthUser;
import com.example.ExpenseTrackerAPI.Entity.Expense;
import com.example.ExpenseTrackerAPI.Enum.Category;
import com.example.ExpenseTrackerAPI.Enum.Role;
import com.example.ExpenseTrackerAPI.Service.ExpenseService;
import com.example.ExpenseTrackerAPI.Service.Impl.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExpenseController.class)
@AutoConfigureMockMvc(addFilters = false)
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
}
