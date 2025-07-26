package com.deyan.mealplanner.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record WeightEntryDTO(BigDecimal weight, LocalDateTime date, List<Long> newAchievements) {
}
