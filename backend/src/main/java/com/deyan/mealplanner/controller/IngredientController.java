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
    /**
     * Retrieves all available ingredients.
     *
     * @return A list of {@link IngredientDTO} objects.
     */
    @GetMapping
    public List<IngredientDTO> getAllIngredients(){
        return ingredientService.getAllIngredients();
    }
    /**
     * Retrieves a specific ingredient by its ID.
     *
     * @param id The ID of the ingredient to retrieve.
     * @return The matching {@link IngredientDTO}.
     */
    @GetMapping("/{id}")
    public IngredientDTO getIngredientById(@PathVariable Long id){
        return ingredientService.getIngredientById(id);
    }
    /**
     * Creates a new ingredient based on the provided data.
     *
     * @param ingredientDTO The data for the new ingredient.
     * @return The created {@link IngredientDTO}.
     */
    @PostMapping
    public IngredientDTO createIngredient(@RequestBody NewIngredientDTO ingredientDTO){
        return ingredientService.createIngredient(ingredientDTO);
    }
    /**
     * Updates an existing ingredient.
     *
     * @param id The ID of the ingredient to update.
     * @param ingredientDTO The new ingredient data.
     * @return The updated {@link IngredientDTO}.
     */
    @PutMapping("/{id}")
    public IngredientDTO updateIngredient(@PathVariable Long id,@RequestBody IngredientDTO ingredientDTO){
        return ingredientService.update(id,ingredientDTO);
    }
    /**
     * Deletes an ingredient by its ID.
     *
     * @param id The ID of the ingredient to delete.
     */
    @DeleteMapping("/{id}")
    public void deleteIngredient(@PathVariable Long id){
        ingredientService.delete(id);
    }

}
