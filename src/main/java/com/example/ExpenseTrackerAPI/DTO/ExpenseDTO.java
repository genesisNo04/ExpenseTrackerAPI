package com.example.ExpenseTrackerAPI.DTO;

import com.example.ExpenseTrackerAPI.Entity.Expense;
import com.example.ExpenseTrackerAPI.Enum.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ExpenseDTO {

    private String title;

    private String description;

    private Category category;

    private Double amount;

    private LocalDateTime createdTime;

    private LocalDateTime modifiedTime;

    public ExpenseDTO(String title, String description, Category category, Double amount) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.amount = amount;
    }

    public ExpenseDTO(Expense expense) {
        this.title = expense.getTitle();
        this.description = expense.getDescription();
        this.category = expense.getCategory();
        this.amount = expense.getAmount();
        this.createdTime = expense.getCreatedTime();
        this.modifiedTime = expense.getLastModifiedTime();
    }
}
