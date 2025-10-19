package com.example.ExpenseTrackerAPI.Entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Category {
    GROCERIES("Groceries"),
    LEISURE("Leisure"),
    ELECTRONIC("Electronic"),
    UTILITIES("Utilities"),
    CLOTHING("Clothing"),
    HEALTH("Health"),
    OTHERS("Others");

    private final String category;
}
