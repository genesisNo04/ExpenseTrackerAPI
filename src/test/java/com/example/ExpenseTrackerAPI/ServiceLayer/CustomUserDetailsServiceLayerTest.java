package com.example.ExpenseTrackerAPI.ServiceLayer;

import com.example.ExpenseTrackerAPI.Entity.AppUser;
import com.example.ExpenseTrackerAPI.Entity.AuthUser;
import com.example.ExpenseTrackerAPI.Enum.Role;
import com.example.ExpenseTrackerAPI.Exception.CredentialsNotFound;
import com.example.ExpenseTrackerAPI.Repository.AuthUserRepository;
import com.example.ExpenseTrackerAPI.Service.Impl.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceLayerTest {

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private AuthUserRepository authUserRepository;

    private AuthUser createTestAuthUser() {
        AuthUser authUser = new AuthUser("user", "user", "user@gmail.com", Role.ROLE_USER);
        AppUser appUser = new AppUser("user", new ArrayList<>());
        authUser.setAppUser(appUser);
        appUser.setAuthUser(authUser);

        return authUser;
    }

    @Test
    void shouldReturnAuthUser() {
        AuthUser authUser = createTestAuthUser();

        when(authUserRepository.findByUsername(authUser.getUsername())).thenReturn(Optional.of(authUser));

        UserDetails userDetailsResult = customUserDetailsService.loadUserByUsername(authUser.getUsername());

        assertEquals(authUser.getUsername(), userDetailsResult.getUsername());
        assertEquals(authUser.getPassword(), userDetailsResult.getPassword());
        assertEquals("ROLE_USER", userDetailsResult.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void shouldThrowCredentialsNotFoundWhenUserNotFound() {

        when(authUserRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        when(authUserRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(CredentialsNotFound.class, () -> {
            customUserDetailsService.loadUserByUsername("unknown");
        });
    }

}
