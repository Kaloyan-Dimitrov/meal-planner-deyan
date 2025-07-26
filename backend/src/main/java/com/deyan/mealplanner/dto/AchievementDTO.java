package com.deyan.mealplanner.dto;

import java.time.LocalDateTime;

public record AchievementDTO(Long id, String name, String description,Integer target, Integer progress, LocalDateTime completedAt) {
    public boolean unlocked(){
        return completedAt!=null;
    }
}
