package com.example.ExpenseTrackerAPI.IntegrationTest;

import com.example.ExpenseTrackerAPI.Repository.AuthUserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthUserRepository authUserRepository;

    private String userJson(String username, String password, String email) {
        return String.format("""
                    {
                        "username": "%s",
                        "password": "%s",
                        "email": "%s"
                    }
                """, username, password, email);
    }

    private String loginReturnJWTToken(String username, String password) throws Exception {
        String loginJSON = String.format("""
                    {
                        "username": "%s",
                        "password": "%s"
                    }
                """, username, password);

        ResultActions action = mockMvc.perform(post("/v1/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJSON));
        String token = action.andReturn().getResponse().getContentAsString();;
        System.out.println(token);
        return token;
    }

    @Test
    @DisplayName("Should register new user and return access token")
    void shouldRegisterUserAndReturnAccessToken() throws Exception {
        mockMvc.perform(post("/v1/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson("user", "user", "user@gmail.com")))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists());

        assertTrue(authUserRepository.findByUsername("user").isPresent());
    }

    @Test
    @DisplayName("Should throw error for duplicate username without returning token")
    void shouldFailWhenRegisteringDuplicateUsername() throws Exception {
        mockMvc.perform(post("/v1/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("user", "user", "user@gmail.com")))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists());

        mockMvc.perform(post("/v1/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("user", "user", "user1@gmail.com")))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username or email already taken."))
                .andExpect(jsonPath("$.accessToken").doesNotExist());

        assertTrue(authUserRepository.findByUsername("user").isPresent());
        assertFalse(authUserRepository.findByEmail("user1@gmail.com").isPresent());
    }

    @Test
    @DisplayName("Should throw error for duplicate email without returning token")
    void shouldFailWhenRegisteringDuplicateEmail() throws Exception {
        mockMvc.perform(post("/v1/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("user1", "user1", "user@gmail.com")))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists());

        mockMvc.perform(post("/v1/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("user", "user", "user@gmail.com")))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username or email already taken."))
                .andExpect(jsonPath("$.accessToken").doesNotExist());

        assertTrue(authUserRepository.findByUsername("user1").isPresent());
        assertFalse(authUserRepository.findByUsername("user").isPresent());
    }

    @Test
    @DisplayName("Should throw error for duplicate email with case insensitive without returning token")
    void registerUserShouldThrowErrorInsensitiveCaseEmail() throws Exception {
        mockMvc.perform(post("/v1/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("user1", "user1", "user@gmail.com")))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists());

        mockMvc.perform(post("/v1/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("user1", "user1", "USER@gmail.com")))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username or email already taken."))
                .andExpect(jsonPath("$.accessToken").doesNotExist());

        assertTrue(authUserRepository.findByUsername("user1").isPresent());
        assertFalse(authUserRepository.findByEmail("USER@gmail.com").isPresent());
    }


    @Test
    @DisplayName("Should login successful with returning token")
    void shouldLoginSuccessfully() throws Exception {
        mockMvc.perform(post("/v1/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("user", "user", "user@gmail.com")))
                .andDo(print())
                .andExpect(jsonPath("$.accessToken").exists());

        mockMvc.perform(post("/v1/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "username": "user",
                                "password": "user"
                            }
                        """))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    @DisplayName("Should login fail with incorrect password without returning token")
    void shouldFailedWithIncorrectPassword() throws Exception {
        mockMvc.perform(post("/v1/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson("user", "user", "user@gmail.com")))
                .andDo(print())
                .andExpect(jsonPath("$.accessToken").exists());

        mockMvc.perform(post("/v1/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "username": "user",
                                "password": "user123"
                            }
                        """))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.accessToken").doesNotExist());
    }

    @Test
    @DisplayName("Should throw error for incorrect username without returning token")
    void shouldFailLoginWithInvalidCredentials() throws Exception {
        mockMvc.perform(post("/v1/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "username": "user",
                                "password": "user"
                            }
                        """))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid credentials. Please check your username/email and password."))
                .andExpect(jsonPath("$.accessToken").doesNotExist());

    }

    @Test
    @DisplayName("Should throw unauthorized error without sending the token")
    void protectedEndpointShouldRejectUnauthorized() throws Exception {
        mockMvc.perform(get("/v1/expense"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}
