package com.deyan.mealplanner.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BasicController {
    @GetMapping("/api/mealplanner")
    public String hello() {
        return "This is the meal planner";
    }
    @GetMapping("/api/mealplanner/recipe")
    public String recipe() {
        return "This is the recipe";
    }
}
