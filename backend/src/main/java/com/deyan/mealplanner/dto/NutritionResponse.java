package com.deyan.mealplanner.dto;

public record NutritionResponse(
        String calories,     // e.g. "529 kcal"
        String protein,      // e.g. "30g"
        String fat,
        String carbs
) {}
