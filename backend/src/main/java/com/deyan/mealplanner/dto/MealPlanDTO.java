package com.deyan.mealplanner.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public record MealPlanDTO(@JsonProperty("meals") List<Meal> meals,
                          @JsonProperty("nutrients") Nutrients nutrients) {
    public record Meal(Long index,Long id,String title,String imageType,int readyInMinutes,Integer servings,String sourceUrl){}
    public record Nutrients( BigDecimal calories,
                             BigDecimal protein,
                             BigDecimal fat,
                             BigDecimal carbohydrates){}
}
