package com.deyan.mealplanner.service;

import com.deyan.mealplanner.dto.MealPlanDTO;
import com.deyan.mealplanner.dto.RecipeDetailsDTO;

public interface RecipeAPIAdapter {
    MealPlanDTO generateMealPlan(Integer targetKcal,int days);
    RecipeDetailsDTO getRecipe(Long apiId);
}
