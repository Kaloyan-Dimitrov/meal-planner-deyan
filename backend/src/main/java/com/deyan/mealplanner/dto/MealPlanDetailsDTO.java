package com.deyan.mealplanner.dto;

import java.math.BigDecimal;
import java.util.List;

public record MealPlanDetailsDTO(Long id,
                                 BigDecimal targetKcal, BigDecimal targetProteinG, BigDecimal targetCarbG, BigDecimal targetFatG,
                                 BigDecimal actualKcal, BigDecimal actualProteinG, BigDecimal actualCarbG, BigDecimal actualFatG,
                                 List<MealSlotDTO> meals,
                                 List<ShoppingListItemDTO> shoppingList) {
    public record MealSlotDTO(String day, String mealSlot, RecipeDTO recipe) {}

    public record RecipeDTO(Long id, String title, Integer prepTime, Integer servings,String sourceUrl,
                            List<RecipeDetailsDTO.ExtendedIngredient> ingredients) {}

    public record ShoppingListItemDTO(Long ingredientId, String name, String quantityText) {}
}
