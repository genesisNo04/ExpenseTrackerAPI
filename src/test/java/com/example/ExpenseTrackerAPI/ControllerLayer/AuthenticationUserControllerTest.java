package com.example.ExpenseTrackerAPI.ControllerLayer;

import com.example.ExpenseTrackerAPI.Config.JwtUtil;
import com.example.ExpenseTrackerAPI.Controller.AuthUserController;
import com.example.ExpenseTrackerAPI.Controller.ExpenseController;
import com.example.ExpenseTrackerAPI.Entity.AppUser;
import com.example.ExpenseTrackerAPI.Entity.AuthUser;
import com.example.ExpenseTrackerAPI.Enum.Role;
import com.example.ExpenseTrackerAPI.Exception.DuplicateUserException;
import com.example.ExpenseTrackerAPI.Handler.GlobalExceptionHandler;
import com.example.ExpenseTrackerAPI.Service.AppUserService;
import com.example.ExpenseTrackerAPI.Service.AuthUserService;
import com.example.ExpenseTrackerAPI.Service.Impl.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

@WebMvcTest(AuthUserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class AuthenticationUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthUserService authUserService;

    @MockitoBean
    private AppUserService appUserService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void shouldRegisterUser() throws Exception {
        AppUser newUser = new AppUser();
        newUser.setUsername("user");

        AuthUser authUser = new AuthUser();
        authUser.setUsername("user");
        authUser.setPassword("user");
        authUser.setEmail("user@gmail.com");
        authUser.setRole(Role.ROLE_USER);
        authUser.setAppUser(newUser);
        newUser.setAuthUser(authUser);

        when(authUserService.registerUser(authUser)).thenReturn(authUser);
        when(jwtUtil.generateToken(authUser.getUsername(), authUser.getRole())).thenReturn("JWTTOKEN");

        mockMvc.perform(post("/v1/user/register")
                        .contentType("application/json")
                        .content("""
                                {
                                   "username": "user",
                                   "password": "user",
                                   "email": "user@gmail.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("JWTTOKEN"));

    }

    @Test
    void shouldThrowErrorDuplicateUser() throws Exception {
        when(authUserService.registerUser(any(AuthUser.class))).thenThrow(new DuplicateUserException("Username or email already taken"));

        mockMvc.perform(post("/v1/user/register")
                        .contentType("application/json")
                        .content("""
                                {
                                   "username": "user",
                                   "password": "user",
                                   "email": "user@gmail.com"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Username or email already taken"));
    }

    @Test
    void shouldLogUserIn() throws Exception {
        AppUser newUser = new AppUser();
        newUser.setUsername("user");

        AuthUser authUser = new AuthUser();
        authUser.setUsername("user");
        authUser.setPassword("user");
        authUser.setEmail("user@gmail.com");
        authUser.setRole(Role.ROLE_USER);
        authUser.setAppUser(newUser);
        newUser.setAuthUser(authUser);

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authenticationToken);
        when(jwtUtil.generateToken(authUser.getUsername(), authUser.getRole())).thenReturn("JWTTOKEN");

        mockMvc.perform(post("/v1/user/login")
                        .contentType("application/json")
                        .content("""
                                {
                                   "username": "user",
                                   "password": "user"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("JWTTOKEN"));

    }

    @Test
    void shouldThrowErrorUserNotExists() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(new BadCredentialsException("User not found"));

        mockMvc.perform(post("/v1/user/login")
                        .contentType("application/json")
                        .content("""
                                {
                                   "username": "user",
                                   "password": "user"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

}
