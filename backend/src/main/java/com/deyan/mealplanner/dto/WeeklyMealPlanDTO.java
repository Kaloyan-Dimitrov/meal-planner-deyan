package com.deyan.mealplanner.dto;

import java.util.List;
import java.util.Map;

public record WeeklyMealPlanDTO(Map<String,Day> week) {
    public record Day(List<MealPlanDTO.Meal> meals
                    , MealPlanDTO.Nutrients nutrients){}
}
