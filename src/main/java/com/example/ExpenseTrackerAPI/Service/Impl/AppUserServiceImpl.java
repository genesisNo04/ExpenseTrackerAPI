package com.example.ExpenseTrackerAPI.Service.Impl;

import com.example.ExpenseTrackerAPI.Entity.AppUser;
import com.example.ExpenseTrackerAPI.Repository.AppUserRepository;
import com.example.ExpenseTrackerAPI.Service.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppUserServiceImpl implements AppUserService {


    @Autowired
    private AppUserRepository appUserRepository;

    @Override
    public AppUser saveUser(AppUser appUser) {
        return appUserRepository.save(appUser);
    }
}
