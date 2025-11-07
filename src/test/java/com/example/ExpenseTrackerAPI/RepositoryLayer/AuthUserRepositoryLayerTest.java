package com.example.ExpenseTrackerAPI.RepositoryLayer;

import com.example.ExpenseTrackerAPI.Entity.AppUser;
import com.example.ExpenseTrackerAPI.Entity.AuthUser;
import com.example.ExpenseTrackerAPI.Enum.Role;
import com.example.ExpenseTrackerAPI.Repository.AppUserRepository;
import com.example.ExpenseTrackerAPI.Repository.AuthUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
public class AuthUserRepositoryLayerTest {

    @Autowired
    private AuthUserRepository authUserRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    private AuthUser createUser(String username, String password, String email, Role role) {
        AuthUser authUser = new AuthUser(username, password, email, role);
        authUser.setAppUser(new AppUser(username, new ArrayList<>()));
        authUser.getAppUser().setAuthUser(authUser);
        return authUserRepository.save(authUser);
    }

    @Test
    void shouldFindUserByUsername() {
        AuthUser authUser = createUser("user", "user", "user@gmail.com", Role.ROLE_USER);

        Optional<AuthUser> resultUser = authUserRepository.findByUsername("user");

        assertTrue(resultUser.isPresent());
        assertEquals(authUser.getAppUser(), resultUser.get().getAppUser());
        assertEquals(authUser.getUsername(), resultUser.get().getUsername());
        assertEquals(authUser.getPassword(), resultUser.get().getPassword());
        assertEquals(authUser.getEmail(), resultUser.get().getEmail());
        assertEquals(authUser.getRole(), resultUser.get().getRole());
    }

    @Test
    void shouldThrowDuplicateUserExceptionForDuplicateUsername() {
        AuthUser authUser = createUser("user", "user", "user1@gmail.com", Role.ROLE_USER);

        assertThrows(DataIntegrityViolationException.class, () -> createUser("user", "user", "user2@gmail.com", Role.ROLE_USER));
    }

    @Test
    void shouldReturnEmptyFindByUsernameNotExists() {
        Optional<AuthUser> resultUser = authUserRepository.findByUsername("user");
        assertTrue(resultUser.isEmpty());
    }

    @Test
    void shouldThrowDuplicateUserExceptionForDuplicateEmail() {
        AuthUser authUser = createUser("user1", "user", "user@gmail.com", Role.ROLE_USER);

        assertThrows(DataIntegrityViolationException.class, () -> createUser("user2", "user", "user@gmail.com", Role.ROLE_USER));
    }

    @Test
    void shouldFindUserByEmail() {
        AuthUser authUser = createUser("user", "user", "user@gmail.com", Role.ROLE_USER);

        Optional<AuthUser> resultUser = authUserRepository.findByEmail("user@gmail.com");

        assertTrue(resultUser.isPresent());
        assertEquals(authUser.getAppUser(), resultUser.get().getAppUser());
        assertEquals(authUser.getUsername(), resultUser.get().getUsername());
        assertEquals(authUser.getPassword(), resultUser.get().getPassword());
        assertEquals(authUser.getEmail(), resultUser.get().getEmail());
        assertEquals(authUser.getRole(), resultUser.get().getRole());
    }

    @Test
    void shouldReturnEmptyFindByEmailNotExists() {
        Optional<AuthUser> resultUser = authUserRepository.findByEmail("email@gmail.com");
        assertTrue(resultUser.isEmpty());
    }

    @Test
    void shouldReturnTrueCheckUserExistByUsername() {
        AuthUser authUser = createUser("user", "user", "user@gmail.com", Role.ROLE_USER);
        assertTrue(authUserRepository.existsByUsername(authUser.getUsername()));
    }

    @Test
    void shouldReturnTrueCheckUserExistByEmail() {
        AuthUser authUser = createUser("user", "user", "user@gmail.com", Role.ROLE_USER);
        assertTrue(authUserRepository.existsByEmail(authUser.getEmail()));
    }

    @Test
    void shouldReturnFalseCheckUserExistByUsername() {
        assertFalse(authUserRepository.existsByUsername("nonexistent"));
    }

    @Test
    void shouldReturnFalseCheckUserExistByEmail() {
        assertFalse(authUserRepository.existsByEmail("nonexistent@gmail.com"));
    }
}
