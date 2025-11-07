package com.example.ExpenseTrackerAPI.ServiceLayer;

import com.example.ExpenseTrackerAPI.Entity.AppUser;
import com.example.ExpenseTrackerAPI.Entity.AuthUser;
import com.example.ExpenseTrackerAPI.Enum.Role;
import com.example.ExpenseTrackerAPI.Exception.CredentialsNotFound;
import com.example.ExpenseTrackerAPI.Exception.DuplicateUserException;
import com.example.ExpenseTrackerAPI.Repository.AppUserRepository;
import com.example.ExpenseTrackerAPI.Repository.AuthUserRepository;
import com.example.ExpenseTrackerAPI.Service.Impl.AuthUserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthUserServiceLayerTest {

    @Mock
    private AuthUserRepository authUserRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthUserServiceImpl authUserService;

    private AuthUser createTestAuthUser() {
        AuthUser authUser = new AuthUser("user", "user", "user@gmail.com", Role.ROLE_USER);
        AppUser appUser = new AppUser("user", new ArrayList<>());
        authUser.setAppUser(appUser);
        appUser.setAuthUser(authUser);

        return authUser;
    }

    @Test
    void registerUserReturnAuthUser() {
        AuthUser authUser = createTestAuthUser();

        when(authUserRepository.save(authUser)).thenReturn(authUser);
        when(passwordEncoder.encode(authUser.getPassword())).thenReturn("encodedPassword");

        AuthUser resultUser = authUserService.registerUser(authUser);

        assertNotNull(resultUser);
        assertEquals(authUser.getUsername(), resultUser.getUsername());
        assertEquals("encodedPassword", resultUser.getPassword());
        assertEquals(authUser.getEmail(), resultUser.getEmail());
        assertEquals(authUser.getRole(), resultUser.getRole());
        assertEquals(authUser.getAppUser(), resultUser.getAppUser());
    }

    @Test
    void registerUserThrowDuplicateErrorUsername() {
        AuthUser authUser = createTestAuthUser();

        when(authUserRepository.existsByUsername(authUser.getUsername()))
                .thenReturn(true);

        assertThrows(DuplicateUserException.class, () -> {
            authUserService.registerUser(authUser);
        });
    }

    @Test
    void registerUserThrowDuplicateErrorEmail() {
        AuthUser authUser = createTestAuthUser();

        when(authUserRepository.existsByEmail(authUser.getEmail()))
                .thenReturn(true);

        assertThrows(DuplicateUserException.class, () -> {
            authUserService.registerUser(authUser);
        });
    }

    @Test
    void shouldReturnAuthUserWithUsername() {
        AuthUser authUser = createTestAuthUser();

        when(authUserRepository.findByUsername(authUser.getUsername())).thenReturn(Optional.of(authUser));

        AuthUser resultUser = authUserService.findByUsername(authUser.getUsername());

        assertEquals(authUser.getUsername(), resultUser.getUsername());
        assertEquals(authUser.getPassword(), resultUser.getPassword());
        assertEquals(authUser.getEmail(), resultUser.getEmail());
        assertEquals(authUser.getRole(), resultUser.getRole());
        assertEquals(authUser.getAppUser(), resultUser.getAppUser());
    }

    @Test
    void shouldThrowCredentialsNotFoundWithUsername() {
        AuthUser authUser = createTestAuthUser();
        when(authUserRepository.findByUsername(authUser.getUsername())).thenReturn(Optional.empty());

        assertThrows(CredentialsNotFound.class, () -> authUserService.findByUsername(authUser.getUsername()), "Invalid credentials. Please check your username/email and password.");
    }

    @Test
    void shouldReturnAuthUserWithEmail() {
        AuthUser authUser = createTestAuthUser();

        when(authUserRepository.findByEmail(authUser.getEmail())).thenReturn(Optional.of(authUser));

        AuthUser resultUser = authUserService.findByEmail(authUser.getEmail());

        assertEquals(authUser.getUsername(), resultUser.getUsername());
        assertEquals(authUser.getPassword(), resultUser.getPassword());
        assertEquals(authUser.getEmail(), resultUser.getEmail());
        assertEquals(authUser.getRole(), resultUser.getRole());
        assertEquals(authUser.getAppUser(), resultUser.getAppUser());
    }

    @Test
    void shouldThrowCredentialsNotFoundWithEmail() {
        AuthUser authUser = createTestAuthUser();

        when(authUserRepository.findByEmail(authUser.getEmail())).thenReturn(Optional.empty());

        assertThrows(CredentialsNotFound.class, () -> authUserService.findByEmail(authUser.getEmail()), "Invalid credentials. Please check your username/email and password.");
    }

    @Test
    void shouldReturnTrueAuthUserWithEmail() {
        AuthUser authUser = createTestAuthUser();

        when(authUserRepository.existsByEmail(authUser.getEmail())).thenReturn(true);

        assertTrue(authUserService.existByEmail(authUser.getEmail()));
    }

    @Test
    void shouldReturnFalseAuthUserWithEmail() {
        AuthUser authUser = createTestAuthUser();

        when(authUserRepository.existsByEmail(authUser.getEmail())).thenReturn(false);

        assertFalse(authUserService.existByEmail(authUser.getEmail()));
    }

    @Test
    void shouldReturnTrueAuthUserWithUsername() {
        AuthUser authUser = createTestAuthUser();

        when(authUserRepository.existsByUsername(authUser.getUsername())).thenReturn(true);

        assertTrue(authUserService.existByUsername(authUser.getUsername()));
    }

    @Test
    void shouldReturnFalseAuthUserWithUsername() {
        AuthUser authUser = createTestAuthUser();

        when(authUserRepository.existsByUsername(authUser.getUsername())).thenReturn(false);

        assertFalse(authUserService.existByUsername(authUser.getUsername()));
    }

    @Test
    void shouldReturnAuthUserWhenFindingByEmail() {
        AuthUser authUser = createTestAuthUser();

        when(authUserRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(authUserRepository.findByEmail(authUser.getEmail())).thenReturn(Optional.of(authUser));

        AuthUser resultUser = authUserService.findByUsernameOrEmail(authUser.getEmail());

        assertEquals(authUser, resultUser);
    }

    @Test
    void shouldReturnAuthUserWhenFindingByUsername() {
        AuthUser authUser = createTestAuthUser();

        when(authUserRepository.findByUsername(authUser.getUsername())).thenReturn(Optional.of(authUser));

        AuthUser resultUser = authUserService.findByUsernameOrEmail(authUser.getUsername());

        assertEquals(authUser, resultUser);
    }

    @Test
    void shouldThrowCredentialsNotFoundWhenUsernameAndPasswordNotFound() {

        when(authUserRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(authUserRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(CredentialsNotFound.class, () -> authUserService.findByUsernameOrEmail("unknown"));
    }

}
