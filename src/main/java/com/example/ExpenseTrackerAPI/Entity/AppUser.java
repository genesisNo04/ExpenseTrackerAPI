package com.example.ExpenseTrackerAPI.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "Application_User")
@Getter
@Setter
@NoArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @OneToOne
    private AuthUser authUser;

    private List<Expense> expenses;

    public AppUser(String username, List<Expense> expenses) {
        this.username = username;
        this.expenses = expenses;
    }
}
