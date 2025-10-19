package com.example.ExpenseTrackerAPI.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String title;

    private String description;

    private Category category;

    private AppUser appUser;

    public Expense(String title, String description, Category category, AppUser appUser) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.appUser = appUser;
    }
}
