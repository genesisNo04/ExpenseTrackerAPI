package com.example.ExpenseTrackerAPI.IntegrationTest;

import com.example.ExpenseTrackerAPI.Entity.Expense;
import com.example.ExpenseTrackerAPI.Enum.Category;
import com.example.ExpenseTrackerAPI.Repository.AuthUserRepository;
import com.example.ExpenseTrackerAPI.Repository.ExpenseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class ExpenseIntegrationTesting {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthUserRepository authUserRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    private String userRequestBody(String username, String password, String email) {
        return String.format("""
                    {
                        "username": "%s",
                        "password": "%s",
                        "email": "%s"
                    }
                """, username, password, email);
    }

    private String expenseRequestBody(String title, String description, Category category, double amount) {
        return String.format("""
                    {
                        "title": "%s",
                        "description": "%s",
                        "category": "%s",
                        "amount": %.2f
                    }
                """, title, description, category, amount);
    }

    private String registerUser(String username, String password, String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/v1/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userRequestBody(username, password, email)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();

        return new ObjectMapper()
                .readTree(responseJson)
                .get("accessToken")
                .asText();
    }

    private void createExpense(String title, String description, Category category, double amount) throws Exception {
        String token = registerUser("user", "user", "user@gmail.com");

        mockMvc.perform(post("/v1/expense")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(expenseRequestBody(title, description, category, amount)));
    }

    @Test
    void shouldReturnExpenseWhenCreate() throws Exception {
        String token = registerUser("user", "user", "user@gmail.com");

        mockMvc.perform(post("/v1/expense")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(expenseRequestBody("expense 1", "expense desc 1", Category.GROCERIES, 101.20)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.title").value("expense 1"))
                        .andExpect(jsonPath("$.description").value("expense desc 1"))
                        .andExpect(jsonPath("$.category").value("GROCERIES"))
                        .andExpect(jsonPath("$.amount").value(101.20));
    }

    @Test
    void shouldThrowErrorWithInvalidToken() throws Exception {
        String invalidJwt =
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJpbnZhbGlkVXNlciIsImlhdCI6MTcwMDAwMDAwMH0.Ww9zVkoGxO8DvyjP6TfGsDz3jHK5xL0F4nBM8h8fr9Y";

        mockMvc.perform(post("/v1/expense")
                        .header("Authorization", "Bearer " + invalidJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(expenseRequestBody("expense 1", "expense desc 1", Category.GROCERIES, 101.20)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid or expired JWT token."));
    }

    @Test
    void ShouldReturnExpenseWithIdWithValidToken() throws Exception {
        String token = registerUser("user", "user", "user@gmail.com");

        mockMvc.perform(post("/v1/expense")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(expenseRequestBody("expense 1", "expense desc 1", Category.GROCERIES, 101.20)))
                .andExpect(status().isCreated())
                .andReturn();

        long id = 1;

        mockMvc.perform(get("/v1/expense/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("expense 1"))
                .andExpect(jsonPath("$.description").value("expense desc 1"))
                .andExpect(jsonPath("$.category").value("GROCERIES"))
                .andExpect(jsonPath("$.amount").value(101.20));
    }

    @Test
    void ShouldThrowUnauthorizedWithDifferentUser() throws Exception {
        String token = registerUser("user", "user", "user@gmail.com");
        String token1 = registerUser("user1", "user1", "user1@gmail.com");

        mockMvc.perform(post("/v1/expense")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(expenseRequestBody("expense 1", "expense desc 1", Category.GROCERIES, 101.20)))
                .andExpect(status().isCreated())
                .andReturn();

        long id = 1;

        mockMvc.perform(get("/v1/expense/" + id)
                        .header("Authorization", "Bearer " + token1))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("You are not authorized to access this expense. This expense does not belong to you."));
    }

}
