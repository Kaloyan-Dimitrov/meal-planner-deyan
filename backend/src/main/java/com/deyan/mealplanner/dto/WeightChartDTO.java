package com.deyan.mealplanner.dto;

import java.math.BigDecimal;
import java.time.LocalDate;


public record WeightChartDTO(LocalDate date, BigDecimal weight) {
}
