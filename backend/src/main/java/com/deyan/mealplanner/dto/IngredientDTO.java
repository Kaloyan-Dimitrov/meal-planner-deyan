package com.deyan.mealplanner.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record IngredientDTO(Long id, String name, String category, BigDecimal kcalPer100g) {
}
