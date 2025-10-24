package com.example.ExpenseTrackerAPI.Entity;

import com.example.ExpenseTrackerAPI.Enum.Category;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String title;

    private String description;

    private double amount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Category category;

    private LocalDateTime createdTime;

    private LocalDateTime lastModifiedTime;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser appUser;

    public Expense(String title, String description, double amount, Category category) {
        this.title = title;
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.createdTime = LocalDateTime.now();
        this.lastModifiedTime = LocalDateTime.now();
    }
}
