package com.example.ExpenseTrackerAPI.IntegrationTest;

import com.example.ExpenseTrackerAPI.Enum.Category;
import com.example.ExpenseTrackerAPI.Repository.AuthUserRepository;
import com.example.ExpenseTrackerAPI.Repository.ExpenseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.Clock;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ExpenseIntegrationTesting {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthUserRepository authUserRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @MockitoBean
    private Clock clock;

    private String userRequestBody(String username, String password, String email) {
        return String.format("""
                    {
                        "username": "%s",
                        "password": "%s",
                        "email": "%s"
                    }
                """, username, password, email);
    }

    private final String  UNAUTHORIZED_ERROR_MESSAGE = "You are not authorized to access this expense. This expense does not belong to you.";
    private final String  RESOURCE_NOT_FOUND_ERROR_MESSAGE = "Expense not found.";
    private final String  NO_TOKEN_ERROR_MESSAGE = "Full authentication is required to access this resource";

    private String expenseRequestBody(String title, String description, Category category, Double amount, LocalDateTime createdTime) {
        StringBuilder sb = new StringBuilder("{");

        if (title != null) {
            sb.append("\"title\": \"").append(title).append("\",");
        }

        if (description != null) {
            sb.append("\"description\": \"").append(description).append("\",");
        }

        if (category != null) {
            sb.append("\"category\": \"").append(category).append("\",");
        }

        if (amount != null) {
            sb.append("\"amount\": \"").append(amount).append("\",");
        }

        if (createdTime != null) {
            sb.append("\"createdTime\": \"").append(createdTime).append("\",");
        }

        if (sb.charAt(sb.length() - 1) == ',') {
            sb.setLength(sb.length() - 1);
        }

        sb.append("}");

        return sb.toString();
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

    private void createExpense(String title, String description, Category category, double amount, String token, LocalDateTime createdTime) throws Exception {
        mockMvc.perform(post("/v1/expense")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(expenseRequestBody(title, description, category, amount, createdTime)));
    }

    @BeforeEach
    void cleanDatabase() {
        expenseRepository.deleteAll();
        authUserRepository.deleteAll();

        Instant fixedInstant = LocalDateTime.of(2025, 11, 16, 0, 0)
                .atZone(ZoneId.systemDefault())
                .toInstant();

        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
    }

    @Test
    void shouldReturnExpenseWhenCreate() throws Exception {
        String token = registerUser("user", "user", "user@gmail.com");

        mockMvc.perform(post("/v1/expense")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(expenseRequestBody("expense 1", "expense desc 1", Category.GROCERIES, 101.20, null)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.title").value("expense 1"))
                        .andExpect(jsonPath("$.description").value("expense desc 1"))
                        .andExpect(jsonPath("$.category").value("GROCERIES"))
                        .andExpect(jsonPath("$.amount").value(101.20));
    }

    @Test
    void shouldThrowErrorWithoutSendingToken() throws Exception {
        String token = registerUser("user", "user", "user@gmail.com");

        mockMvc.perform(post("/v1/expense")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(expenseRequestBody("expense 1", "expense desc 1", Category.GROCERIES, 101.20, null)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value(NO_TOKEN_ERROR_MESSAGE));
    }


    @Test
    void shouldThrowErrorWithInvalidToken() throws Exception {
        String invalidJwt =
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJpbnZhbGlkVXNlciIsImlhdCI6MTcwMDAwMDAwMH0.Ww9zVkoGxO8DvyjP6TfGsDz3jHK5xL0F4nBM8h8fr9Y";

        mockMvc.perform(post("/v1/expense")
                        .header("Authorization", "Bearer " + invalidJwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(expenseRequestBody("expense 1", "expense desc 1", Category.GROCERIES, 101.20, null)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid or expired JWT token."));
    }

    @Test
    void shouldReturnExpenseWithIdWithValidToken() throws Exception {
        String token = registerUser("user", "user", "user@gmail.com");

        createExpense("expense 1", "expense desc 1", Category.GROCERIES, 101.20, token, null);

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
    void ShouldThrowNoResourceFoundWithIdNotExist() throws Exception {
        String token = registerUser("user", "user", "user@gmail.com");

        long id = 1;

        mockMvc.perform(get("/v1/expense/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(RESOURCE_NOT_FOUND_ERROR_MESSAGE));
    }

    @Test
    void ShouldThrowUnauthorizedWithoutSendingTokenWithSearchWithId() throws Exception {
        String token = registerUser("user", "user", "user@gmail.com");

        long id = 1;

        mockMvc.perform(get("/v1/expense/" + id))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value(NO_TOKEN_ERROR_MESSAGE));
    }

    @Test
    void ShouldThrowUnauthorizedWithDifferentUser() throws Exception {
        String token = registerUser("user", "user", "user@gmail.com");
        String token1 = registerUser("user1", "user1", "user1@gmail.com");

        mockMvc.perform(post("/v1/expense")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(expenseRequestBody("expense 1", "expense desc 1", Category.GROCERIES, 101.20, null)))
                .andExpect(status().isCreated())
                .andReturn();

        long id = 1;

        mockMvc.perform(get("/v1/expense/" + id)
                        .header("Authorization", "Bearer " + token1))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value(UNAUTHORIZED_ERROR_MESSAGE));
    }

    @Test
    void shouldReturnAllExpense() throws Exception {
        String token = registerUser("user", "user", "user@gmail.com");
        createExpense("expense1", "expense description 1", Category.GROCERIES, 153.6, token, LocalDateTime.of(2025, 11, 14, 16, 30, 30, 0));
        createExpense("expense2", "expense description 2", Category.ELECTRONIC, 358.6, token, LocalDateTime.of(2025, 11, 14, 16, 30, 30, 0));
        createExpense("expense3", "expense description 3", Category.LEISURE, 1503.6, token, LocalDateTime.of(2025, 10, 14, 16, 30, 30, 0));

        mockMvc.perform(get("/v1/expense")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].title").value("expense1"))
                .andExpect(jsonPath("$[0].description").value("expense description 1"))
                .andExpect(jsonPath("$[0].category").value("GROCERIES"))
                .andExpect(jsonPath("$[0].amount").value(153.6))
                .andExpect(jsonPath("$[1].title").value("expense2"))
                .andExpect(jsonPath("$[1].description").value("expense description 2"))
                .andExpect(jsonPath("$[1].category").value("ELECTRONIC"))
                .andExpect(jsonPath("$[1].amount").value(358.6))
                .andExpect(jsonPath("$[2].title").value("expense3"))
                .andExpect(jsonPath("$[2].description").value("expense description 3"))
                .andExpect(jsonPath("$[2].category").value("LEISURE"))
                .andExpect(jsonPath("$[2].amount").value(1503.6));
    }

    @Test
    void shouldNotReturnExpenseOfDifferentUser() throws Exception {
        String token = registerUser("user", "user", "user@gmail.com");
        String token1 = registerUser("user1", "user1", "user1@gmail.com");
        createExpense("expense1", "expense description 1", Category.GROCERIES, 153.6, token, LocalDateTime.of(2025, 11, 14, 16, 30, 30, 0));
        createExpense("expense2", "expense description 2", Category.ELECTRONIC, 358.6, token, LocalDateTime.of(2025, 12, 14, 16, 30, 30, 0));
        createExpense("expense3", "expense description 3", Category.LEISURE, 1503.6, token, LocalDateTime.of(2025, 10, 14, 16, 30, 30, 0));

        mockMvc.perform(get("/v1/expense")
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldThrowUnauthorizedWithoutSendingTokenWithSearchAll() throws Exception {
        String token = registerUser("user", "user", "user@gmail.com");
        createExpense("expense1", "expense description 1", Category.GROCERIES, 153.6, token, LocalDateTime.of(2025, 11, 14, 16, 30, 30, 0));
        createExpense("expense2", "expense description 2", Category.ELECTRONIC, 358.6, token, LocalDateTime.of(2025, 12, 14, 16, 30, 30, 0));
        createExpense("expense3", "expense description 3", Category.LEISURE, 1503.6, token, LocalDateTime.of(2025, 10, 14, 16, 30, 30, 0));

        mockMvc.perform(get("/v1/expense"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value(NO_TOKEN_ERROR_MESSAGE));
    }

    @Test
    void shouldReturnExpenseForPastWeek() throws Exception {
        String token = registerUser("user", "user", "user@gmail.com");
        createExpense("expense1", "expense description 1", Category.GROCERIES, 153.6, token, LocalDateTime.of(2025, 11, 12, 16, 30, 30, 0));
        createExpense("expense2", "expense description 2", Category.ELECTRONIC, 358.6, token, LocalDateTime.of(2025, 11, 06, 16, 30, 30, 0));
        createExpense("expense3", "expense description 3", Category.LEISURE, 1503.6, token, LocalDateTime.of(2025, 10, 15, 16, 30, 30, 0));

        mockMvc.perform(get("/v1/expense?filter=pastWeek")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("expense1"))
                .andExpect(jsonPath("$[0].description").value("expense description 1"))
                .andExpect(jsonPath("$[0].category").value("GROCERIES"))
                .andExpect(jsonPath("$[0].amount").value(153.6));
    }

    @Test
    void shouldReturnExpenseForPastMonth() throws Exception {
        String token = registerUser("user", "user", "user@gmail.com");
        createExpense("expense1", "expense description 1", Category.GROCERIES, 153.6, token, LocalDateTime.of(2025, 10, 12, 16, 30, 30, 0));
        createExpense("expense2", "expense description 2", Category.ELECTRONIC, 358.6, token, LocalDateTime.of(2025, 11, 06, 16, 30, 30, 0));
        createExpense("expense3", "expense description 3", Category.LEISURE, 1503.6, token, LocalDateTime.of(2025, 10, 18, 16, 30, 30, 0));

        mockMvc.perform(get("/v1/expense?filter=pastMonth")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("expense2"))
                .andExpect(jsonPath("$[0].description").value("expense description 2"))
                .andExpect(jsonPath("$[0].category").value("ELECTRONIC"))
                .andExpect(jsonPath("$[0].amount").value(358.6))
                .andExpect(jsonPath("$[1].title").value("expense3"))
                .andExpect(jsonPath("$[1].description").value("expense description 3"))
                .andExpect(jsonPath("$[1].category").value("LEISURE"))
                .andExpect(jsonPath("$[1].amount").value(1503.6));
    }

    @Test
    void shouldReturnExpenseForLast3Months() throws Exception {
        String token = registerUser("user", "user", "user@gmail.com");
        createExpense("expense1", "expense description 1", Category.GROCERIES, 153.6, token, LocalDateTime.of(2025, 10, 12, 16, 30, 30, 0));
        createExpense("expense2", "expense description 2", Category.ELECTRONIC, 358.6, token, LocalDateTime.of(2025, 11, 06, 16, 30, 30, 0));
        createExpense("expense3", "expense description 3", Category.LEISURE, 1503.6, token, LocalDateTime.of(2025, 7, 15, 16, 30, 30, 0));

        mockMvc.perform(get("/v1/expense?filter=last3Months")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("expense1"))
                .andExpect(jsonPath("$[0].description").value("expense description 1"))
                .andExpect(jsonPath("$[0].category").value("GROCERIES"))
                .andExpect(jsonPath("$[0].amount").value(153.6))
                .andExpect(jsonPath("$[1].title").value("expense2"))
                .andExpect(jsonPath("$[1].description").value("expense description 2"))
                .andExpect(jsonPath("$[1].category").value("ELECTRONIC"))
                .andExpect(jsonPath("$[1].amount").value(358.6));
    }

    @Test
    void shouldReturnUpdatedExpense() throws Exception {
        String token = registerUser("user", "user", "user@gmail.com");
        createExpense("expense1", "expense description 1", Category.GROCERIES, 153.6, token, LocalDateTime.of(2025, 10, 12, 16, 30, 30, 0));

        String updateExpense = expenseRequestBody("expense1 update", "expense description 1 update", Category.CLOTHING, 200.50, null);

        mockMvc.perform(put("/v1/expense/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateExpense))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("expense1 update"))
                .andExpect(jsonPath("$.description").value("expense description 1 update"))
                .andExpect(jsonPath("$.category").value("CLOTHING"))
                .andExpect(jsonPath("$.amount").value(200.50));
    }

    @Test
    void shouldThrowForbiddenUpdateExpenseWithUnauthorizedUser() throws Exception {
        String token = registerUser("user", "user", "user@gmail.com");
        String token1 = registerUser("user1", "user1", "user1@gmail.com");
        createExpense("expense1", "expense description 1", Category.GROCERIES, 153.6, token, LocalDateTime.of(2025, 10, 12, 16, 30, 30, 0));

        String updateExpense = expenseRequestBody("expense1 update", "expense description 1 update", Category.CLOTHING, 200.50, null);

        mockMvc.perform(put("/v1/expense/1")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateExpense))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value(UNAUTHORIZED_ERROR_MESSAGE));
    }

    @Test
    void shouldThrowUnauthorizedUpdateExpenseWithoutSendingToken() throws Exception {
        String token = registerUser("user", "user", "user@gmail.com");
        createExpense("expense1", "expense description 1", Category.GROCERIES, 153.6, token, LocalDateTime.of(2025, 10, 12, 16, 30, 30, 0));

        String updateExpense = expenseRequestBody("expense1 update", "expense description 1 update", Category.CLOTHING, 200.50, null);

        mockMvc.perform(put("/v1/expense/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateExpense))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value(NO_TOKEN_ERROR_MESSAGE));
    }

    @Test
    void shouldThrowResourceNotFoundExpenseWithIdNotExists() throws Exception {
        String token = registerUser("user", "user", "user@gmail.com");

        String updateExpense = expenseRequestBody("expense1 update", "expense description 1 update", Category.CLOTHING, 200.50, null);

        mockMvc.perform(put("/v1/expense/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateExpense))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(RESOURCE_NOT_FOUND_ERROR_MESSAGE));
    }

    @Test
    void shouldReturnPatchExpense() throws Exception {
        String token = registerUser("user", "user", "user@gmail.com");
        createExpense("expense1", "expense description 1", Category.GROCERIES, 153.6, token, LocalDateTime.of(2025, 10, 12, 16, 30, 30, 0));

        String updateExpense = expenseRequestBody("expense1 update", null, Category.CLOTHING, null, null);

        mockMvc.perform(patch("/v1/expense/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateExpense))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("expense1 update"))
                .andExpect(jsonPath("$.description").value("expense description 1"))
                .andExpect(jsonPath("$.category").value("CLOTHING"))
                .andExpect(jsonPath("$.amount").value(153.6));
    }

    @Test
    void shouldThrowForbiddenPatchExpenseWithUnauthorizedUser() throws Exception {
        String token = registerUser("user", "user", "user@gmail.com");
        String token1 = registerUser("user1", "user1", "user1@gmail.com");
        createExpense("expense1", "expense description 1", Category.GROCERIES, 153.6, token, LocalDateTime.of(2025, 10, 12, 16, 30, 30, 0));

        String updateExpense = expenseRequestBody(null, "expense description 1 update", null, 200.50, null);

        mockMvc.perform(patch("/v1/expense/1")
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateExpense))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value(UNAUTHORIZED_ERROR_MESSAGE));
    }

    @Test
    void shouldThrowUnauthorizedPatchExpenseWithoutSendingToken() throws Exception {
        String token = registerUser("user", "user", "user@gmail.com");
        createExpense("expense1", "expense description 1", Category.GROCERIES, 153.6, token, LocalDateTime.of(2025, 10, 12, 16, 30, 30, 0));

        String updateExpense = expenseRequestBody("expense1 update", "expense description 1 update", Category.CLOTHING, 200.50, null);

        mockMvc.perform(patch("/v1/expense/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateExpense))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value(NO_TOKEN_ERROR_MESSAGE));
    }

    @Test
    void shouldThrowResourceNotFoundPatchExpenseWithIdNotExists() throws Exception {
        String token = registerUser("user", "user", "user@gmail.com");

        String updateExpense = expenseRequestBody("expense1 update", null, null, null, null);

        mockMvc.perform(patch("/v1/expense/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateExpense))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(RESOURCE_NOT_FOUND_ERROR_MESSAGE));
    }

    @Test
    void shouldDeleteExpenseWithId() throws Exception {
        String token = registerUser("user", "user", "user@gmail.com");

        createExpense("expense1", "expense description 1", Category.GROCERIES, 153.6, token, null);
        createExpense("expense2", "expense description 2", Category.GROCERIES, 153.6, token, null);
        createExpense("expense3", "expense description 3", Category.GROCERIES, 153.6, token, null);


        mockMvc.perform(delete("/v1/expense/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/v1/expense")
                        .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("expense2"))
                .andExpect(jsonPath("$[1].title").value("expense3"));
    }

    @Test
    void shouldThrowUnauthorizedWithoutSendingToken() throws Exception {
        String token = registerUser("user", "user", "user@gmail.com");

        createExpense("expense1", "expense description 1", Category.GROCERIES, 153.6, token, null);

        mockMvc.perform(delete("/v1/expense/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value(NO_TOKEN_ERROR_MESSAGE));
    }

    @Test
    void shouldThrowForbiddenExceptionWithDifferentUserToken() throws Exception {
        String token = registerUser("user", "user", "user@gmail.com");
        String token1 = registerUser("user1", "use1r", "user1@gmail.com");

        createExpense("expense1", "expense description 1", Category.GROCERIES, 153.6, token, null);


        mockMvc.perform(delete("/v1/expense/1")
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value(UNAUTHORIZED_ERROR_MESSAGE));
    }

    @Test
    void shouldReturnExpenseSummary() throws Exception {
        String token = registerUser("user", "user", "user@gmail.com");

        createExpense("expense1", "expense description 1", Category.GROCERIES, 153.6, token, null);
        createExpense("expense2", "expense description 2", Category.GROCERIES, 153.6, token, null);
        createExpense("expense3", "expense description 3", Category.GROCERIES, 153.6, token, null);

        mockMvc.perform(get("/v1/expense/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value(460.8));
    }

    @Test
    void shouldNotReturnExpenseSummaryOfAnotherUser() throws Exception {
        String token = registerUser("user", "user", "user@gmail.com");
        String token1 = registerUser("user1", "user1", "user1@gmail.com");

        createExpense("expense1", "expense description 1", Category.GROCERIES, 153.6, token, null);
        createExpense("expense2", "expense description 2", Category.GROCERIES, 153.6, token, null);
        createExpense("expense3", "expense description 3", Category.GROCERIES, 153.6, token, null);

        mockMvc.perform(get("/v1/expense/summary")
                        .header("Authorization", "Bearer " + token1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value(0.0));
    }

    @Test
    void shouldThrowUnauthorizedWithoutSendingTokenForSummary() throws Exception {
        String token = registerUser("user", "user", "user@gmail.com");

        createExpense("expense1", "expense description 1", Category.GROCERIES, 153.6, token, null);
        createExpense("expense2", "expense description 2", Category.GROCERIES, 153.6, token, null);
        createExpense("expense3", "expense description 3", Category.GROCERIES, 153.6, token, null);

        mockMvc.perform(get("/v1/expense/summary"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value(NO_TOKEN_ERROR_MESSAGE));
    }

}
