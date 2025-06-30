package com.deyan.mealplanner.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WeightEntryDTO(BigDecimal weight, LocalDateTime date) {
}
