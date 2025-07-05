package com.deyan.mealplanner.service;

import com.deyan.mealplanner.dto.MealPlanDTO;
import com.deyan.mealplanner.dto.NutritionResponse;
import com.deyan.mealplanner.dto.RecipeDetailsDTO;

import java.util.Optional;

public interface RecipeAPIAdapter {
    MealPlanDTO generateMealPlan(Integer targetKcal,int days);
    RecipeDetailsDTO getRecipe(Long apiId);
    Optional<NutritionResponse> fetchNutritionWidget(Long id);
}
