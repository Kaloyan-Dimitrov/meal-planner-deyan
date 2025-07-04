package com.deyan.mealplanner.dto;

import java.math.BigDecimal;

public record MealPlanSummaryDTO(Long id,
                                 BigDecimal targetKcal,
                                 BigDecimal actualKcal,
                                 String createdAt) {
}
