package com.deyan.mealplanner.dto;

import java.math.BigDecimal;
import java.util.List;

public record RecipeDetailsDTO(
        Long id,
        String title,
        int readyInMinutes,       // -> recipe.prep_time
        int servings,             // -> recipe.servings
        Nutrition nutrition,
        List<ExtendedIngredient> extendedIngredients
) {
    public record Nutrition(
            BigDecimal calories,
            BigDecimal protein,
            BigDecimal fat,
            BigDecimal carbohydrates) { }

    public record ExtendedIngredient(
            long id,
            String name,
            BigDecimal amount,
            String unit) { }
}
