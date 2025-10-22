package com.example.ExpenseTrackerAPI.DTO;

import com.example.ExpenseTrackerAPI.Enum.Category;
import lombok.Getter;

@Getter
public class ExpenseDTO {

    private String title;

    private String description;

    private Category category;

    private double amount;
}
