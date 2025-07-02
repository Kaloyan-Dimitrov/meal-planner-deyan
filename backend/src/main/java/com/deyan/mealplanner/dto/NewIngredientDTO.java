package com.deyan.mealplanner.dto;

import java.math.BigDecimal;

public record NewIngredientDTO(String name, String category, BigDecimal kcalPer100g) {
}
