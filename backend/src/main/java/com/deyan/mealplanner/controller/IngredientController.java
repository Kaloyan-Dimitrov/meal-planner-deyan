package com.deyan.mealplanner.controller;

import com.deyan.mealplanner.dto.IngredientDTO;
import com.deyan.mealplanner.dto.NewIngredientDTO;
import com.deyan.mealplanner.service.IngredientService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingredients")
public class IngredientController {
    private final IngredientService ingredientService;
    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }
    @GetMapping
    public List<IngredientDTO> getAllIngredients(){
        return ingredientService.getAllIngredients();
    }
    @GetMapping("/{id}")
    public IngredientDTO getIngredientById(@PathVariable Long id){
        return ingredientService.getIngredientById(id);
    }
    @PostMapping
    public IngredientDTO createIngredient(@RequestBody NewIngredientDTO ingredientDTO){
        return ingredientService.createIngredient(ingredientDTO);
    }
    @PutMapping("/{id}")
    public IngredientDTO updateIngredient(@PathVariable Long id,@RequestBody IngredientDTO ingredientDTO){
        return ingredientService.update(id,ingredientDTO);
    }
    @DeleteMapping("/{id}")
    public void deleteIngredient(@PathVariable Long id){
        ingredientService.delete(id);
    }

}
