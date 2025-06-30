package com.deyan.mealplanner.dto;

import java.math.BigDecimal;

//This is sent from frontend
public record CreateUserRequest(String name, String email, String password, BigDecimal weight) {
}
