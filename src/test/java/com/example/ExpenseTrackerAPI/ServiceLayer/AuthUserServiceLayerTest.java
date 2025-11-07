package com.example.ExpenseTrackerAPI.ServiceLayer;

import com.example.ExpenseTrackerAPI.Entity.AppUser;
import com.example.ExpenseTrackerAPI.Entity.AuthUser;
import com.example.ExpenseTrackerAPI.Enum.Role;
import com.example.ExpenseTrackerAPI.Repository.AppUserRepository;
import com.example.ExpenseTrackerAPI.Repository.AuthUserRepository;
import com.example.ExpenseTrackerAPI.Service.AuthUserService;
import com.example.ExpenseTrackerAPI.Service.Impl.AuthUserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;

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

    @Test
    void registerUserReturnAuthUser() {
        AuthUser authUser = new AuthUser("user", "user", "user@gmail.com", Role.ROLE_USER);
        AppUser appUser = new AppUser("user", new ArrayList<>());
        authUser.setAppUser(appUser);
        appUser.setAuthUser(authUser);

        when(authUserRepository.save(authUser)).thenReturn(authUser);

        AuthUser resultUser = authUserService.registerUser(authUser);

        assertNotNull(resultUser);
        assertEquals(authUser.getUsername(), resultUser.getUsername());
        assertEquals(authUser.getPassword(), resultUser.getPassword());
        assertEquals(authUser.getEmail(), resultUser.getEmail());
        assertEquals(authUser.getRole(), resultUser.getRole());
        assertEquals(authUser.getAppUser(), resultUser.getAppUser());
    }
}
