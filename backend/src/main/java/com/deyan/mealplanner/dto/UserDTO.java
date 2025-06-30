package com.deyan.mealplanner.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

//This is returned by backend to the frontend
public record UserDTO(Long id, String name, String email, BigDecimal weight, int dayStreak, LocalDateTime weightDate) {
}
