package com.deyan.mealplanner.service;

import com.deyan.mealplanner.exceptions.NotFoundException;
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

    /**
     * Constructs the service with a JOOQ DSL context.
     *
     * @param dsl The JOOQ {@link DSLContext} used for querying.
     */
    public RecipeService(DSLContext dsl) {
        this.dsl = dsl;
    }

    /**
     * Retrieves a full recipe by its ID, including ingredients and optional nutrition.
     *
     * @param recipeId The ID of the recipe to fetch.
     * @return A {@link RecipeDetailsDTO} object with detailed data.
     * @throws NotFoundException If the recipe does not exist.
     */
    public RecipeDetailsDTO getRecipeDetailsById(Long recipeId) {
        RecipeRecord recipe = dsl.selectFrom(RECIPE)
                .where(RECIPE.ID.eq(recipeId))
                .fetchOne();

        if (recipe == null) {
            throw new NotFoundException("Recipe not found with ID: " + recipeId);
        }

        List<RecipeDetailsDTO.ExtendedIngredient> ingredients = dsl
                .select(RECIPE_INGREDIENT.INGREDIENT_ID,
                        INGREDIENT.NAME,
                        RECIPE_INGREDIENT.QUANTITY_G,
                        RECIPE_INGREDIENT.UNIT)
                .from(RECIPE_INGREDIENT)
                .join(INGREDIENT).on(RECIPE_INGREDIENT.INGREDIENT_ID.eq(INGREDIENT.ID))
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
                nutrition,
                ingredients,
                recipe.getUrl()
        );
    }
}
