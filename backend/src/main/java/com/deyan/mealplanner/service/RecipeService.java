package com.deyan.mealplanner.service;

import com.deyan.mealplanner.dto.NutritionResponse;
import com.deyan.mealplanner.dto.RecipeDetailsDTO;
import com.deyan.mealplanner.jooq.tables.records.RecipeRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.deyan.mealplanner.jooq.tables.Ingredient.INGREDIENT;
import static com.deyan.mealplanner.jooq.tables.Recipe.RECIPE;
import static com.deyan.mealplanner.jooq.tables.RecipeIngredient.RECIPE_INGREDIENT;

@Service
public class RecipeService {
    private final DSLContext dsl;
    public RecipeService(DSLContext dsl) {
        this.dsl = dsl;
    }
    public RecipeDetailsDTO getRecipeDetailsById(Long recipeId){
        RecipeRecord recipe = dsl.selectFrom(RECIPE)
                .where(RECIPE.ID.eq(recipeId))
                .fetchOne();

        if (recipe == null) {
            throw new IllegalArgumentException("Recipe not found with ID: " + recipeId);
        }

        // Fetch ingredients
        List<RecipeDetailsDTO.ExtendedIngredient> ingredients = dsl
                .select(RECIPE_INGREDIENT.INGREDIENT_ID,
                        INGREDIENT.NAME,
                        RECIPE_INGREDIENT.QUANTITY_G,
                        RECIPE_INGREDIENT.UNIT)
                .from(RECIPE_INGREDIENT)
                .join(INGREDIENT)
                .on(RECIPE_INGREDIENT.INGREDIENT_ID.eq(INGREDIENT.ID))
                .where(RECIPE_INGREDIENT.RECIPE_ID.eq(recipeId))
                .fetch()
                .map(r -> new RecipeDetailsDTO.ExtendedIngredient(
                        r.get(RECIPE_INGREDIENT.INGREDIENT_ID),
                        r.get(INGREDIENT.NAME),
                        r.get(RECIPE_INGREDIENT.QUANTITY_G),
                        r.get(RECIPE_INGREDIENT.UNIT)
                ));
        RecipeDetailsDTO.Nutrition nutrition = new RecipeDetailsDTO.Nutrition(
                recipe.getCalories(),
                recipe.getProtein(),
                recipe.getFat(),
                recipe.getCarbohydrates()
        );
        return new RecipeDetailsDTO(
                recipe.getId(),
                recipe.getName(),
                recipe.getPrepTime(),
                recipe.getServings(),
                nutrition, // optional: compute nutrition later
                ingredients,
                recipe.getUrl()
        );
    }

}
