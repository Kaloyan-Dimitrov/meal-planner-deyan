package com.deyan.mealplanner.dto;

public record AuthRequest(String email, String password,boolean rememberMe) {
}
