package com.example.ExpenseTrackerAPI.DTO;

import com.example.ExpenseTrackerAPI.Enum.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExpenseDTO {

    private String title;

    private String description;

    private Category category;

    private double amount;
}
