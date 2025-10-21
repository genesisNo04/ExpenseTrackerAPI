package com.example.ExpenseTrackerAPI.Service;

import com.example.ExpenseTrackerAPI.Entity.AuthUser;

public interface AuthUserService {

    AuthUser registerUser(AuthUser authUser);

    AuthUser findByUsername(String username);

    AuthUser findByEmail(String email);

    boolean existByUsername(String username);

    boolean existByEmail(String email);

    AuthUser findByUsernameOrEmail(String identifier);
}
