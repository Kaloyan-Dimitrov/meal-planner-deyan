package com.deyan.mealplanner.config;

import com.deyan.mealplanner.dto.MealPlanDTO;
import com.deyan.mealplanner.dto.MealPlanDTO.Meal;
import com.deyan.mealplanner.dto.MealPlanDTO.Nutrients;
import com.deyan.mealplanner.dto.RecipeDetailsDTO;
import com.deyan.mealplanner.dto.RecipeDetailsDTO.ExtendedIngredient;
import com.deyan.mealplanner.dto.RecipeDetailsDTO.Nutrition;
import com.deyan.mealplanner.dto.NutritionResponse;
import com.deyan.mealplanner.service.interfaces.RecipeAPIAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Minimal, deterministic implementation of RecipeAPIAdapter
 * used ONLY when the 'test' profile is active.
 */
@Profile("test")                 // <-- active for tests only
@Configuration
public class RecipeAdapterStubConfig {

    @Bean
    public RecipeAPIAdapter recipeAPIAdapter() {

        return new RecipeAPIAdapter() {

            // ---------- 1)  generateMealPlan ---------------------------------
            @Override
            public MealPlanDTO generateMealPlan(Integer targetKcal, int days) {

                List<Meal> meals = new ArrayList<>();
                for (int i = 0; i < days; i++) {
                    meals.add(new Meal(
                             (long) i,
                             999L + i,            // fake API id
                             "Stub meal " + i,
                             "jpg",
                             15,
                             1,
                             "https://example.com/meal/" + i
                    ));
                }

                // split target kcal evenly across P/F/C for predictability
                BigDecimal kcal = BigDecimal.valueOf(targetKcal != null ? targetKcal : 2000);
                Nutrients nutrients = new Nutrients(
                        kcal,
                        kcal.multiply(BigDecimal.valueOf(0.30)),   // protein 30 %
                        kcal.multiply(BigDecimal.valueOf(0.30)),   // fat     30 %
                        kcal.multiply(BigDecimal.valueOf(0.40))    // carbs   40 %
                );

                return new MealPlanDTO(meals, nutrients);
            }

            // ---------- 2)  getRecipe ----------------------------------------
            @Override
            public RecipeDetailsDTO getRecipe(Long apiId) {

                Nutrition nutrition = new Nutrition(
                        BigDecimal.valueOf(550),
                        BigDecimal.valueOf(40),
                        BigDecimal.valueOf(20),
                        BigDecimal.valueOf(60)
                );

                List<ExtendedIngredient> ing = List.of(
                        new ExtendedIngredient(1, "Rice", BigDecimal.valueOf(100), "g"),
                        new ExtendedIngredient(2, "Beans", BigDecimal.valueOf(150), "g")
                );

                return new RecipeDetailsDTO(
                        apiId,
                        "Stubbed recipe " + apiId,
                        /* readyInMinutes */ 20,
                        /* servings       */ 2,
                        nutrition,
                        ing,
                        /* sourceUrl      */ "https://example.com/recipe/" + apiId
                );
            }

            // ---------- 3)  fetchNutritionWidget -----------------------------
            @Override
            public Optional<NutritionResponse> fetchNutritionWidget(Long id) {
                return Optional.of(new NutritionResponse(
                        "529 kcal", "30 g", "18 g", "65 g"
                ));
            }
        };
    }
}
