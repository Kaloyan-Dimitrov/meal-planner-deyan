package com.deyan.mealplanner.dto;

import java.time.LocalDateTime;

public record AchievementDTO(Long id, String name, String description, LocalDateTime completedAt) {
}
