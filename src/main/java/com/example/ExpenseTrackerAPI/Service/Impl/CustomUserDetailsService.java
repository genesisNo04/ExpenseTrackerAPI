package com.example.ExpenseTrackerAPI.Service.Impl;

import com.example.ExpenseTrackerAPI.Exception.CredentialsNotFound;
import com.example.ExpenseTrackerAPI.Repository.AuthUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private AuthUserRepository authUserRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        return authUserRepository.findByUsername(identifier)
                .or(() -> authUserRepository.findByEmail(identifier))
                .orElseThrow(() -> new CredentialsNotFound("Invalid credentials. Please check your username/email and password."));
    }
}
