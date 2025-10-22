package com.example.ExpenseTrackerAPI.Service.Impl;

import com.example.ExpenseTrackerAPI.Entity.AuthUser;
import com.example.ExpenseTrackerAPI.Exception.CredentialsNotFound;
import com.example.ExpenseTrackerAPI.Exception.DuplicateUserException;
import com.example.ExpenseTrackerAPI.Repository.AuthUserRepository;
import com.example.ExpenseTrackerAPI.Service.AuthUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthUserServiceImpl implements AuthUserService {

    @Autowired
    private AuthUserRepository authUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public AuthUser registerUser(AuthUser authUser) {
        if (existByEmail(authUser.getEmail()) || existByUsername(authUser.getUsername())) {
            throw new DuplicateUserException("Username or email already taken");
        }
        authUser.setPassword(passwordEncoder.encode(authUser.getPassword()));
        return authUserRepository.save(authUser);
    }

    @Override
    public AuthUser findByUsername(String username) {
        return authUserRepository.findByUsername(username)
                .orElseThrow(() -> new CredentialsNotFound("Invalid credentials. Please check your username/email and password."));
    }

    @Override
    public AuthUser findByEmail(String email) {
        return authUserRepository.findByEmail(email)
                .orElseThrow(() -> new CredentialsNotFound("Invalid credentials. Please check your username/email and password."));
    }


    @Override
    public boolean existByUsername(String username) {
        return authUserRepository.existsByUsername(username);
    }

    @Override
    public boolean existByEmail(String email) {
        return authUserRepository.existsByEmail(email);
    }

    @Override
    public AuthUser findByUsernameOrEmail(String identifier) {
        return authUserRepository.findByUsername(identifier)
                .or(() -> authUserRepository.findByEmail(identifier))
                .orElseThrow(() -> new CredentialsNotFound("Invalid credentials. Please check your username/email and password."));
    }
}
