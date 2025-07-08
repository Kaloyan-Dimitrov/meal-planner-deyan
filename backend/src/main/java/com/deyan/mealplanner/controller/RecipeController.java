package com.deyan.mealplanner.controller;

import com.deyan.mealplanner.dto.RecipeDetailsDTO;
import com.deyan.mealplanner.service.RecipeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {
    private final RecipeService recipeService;
    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }
    @GetMapping("/{id}")
    public RecipeDetailsDTO getRecipeById(@PathVariable Long id) {
        return recipeService.getRecipeDetailsById(id);
    }
}
