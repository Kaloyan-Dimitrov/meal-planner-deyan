package com.deyan.mealplanner.service;

import com.deyan.mealplanner.dto.MealPlanDTO;
import com.deyan.mealplanner.dto.RecipeDetailsDTO;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.deyan.mealplanner.jooq.tables.Ingredient.INGREDIENT;
import static com.deyan.mealplanner.jooq.tables.MealPlan.MEAL_PLAN;
import static com.deyan.mealplanner.jooq.tables.MealPlanRecipe.MEAL_PLAN_RECIPE;
import static com.deyan.mealplanner.jooq.tables.Recipe.RECIPE;
import static com.deyan.mealplanner.jooq.tables.RecipeIngredient.RECIPE_INGREDIENT;
import static com.deyan.mealplanner.jooq.tables.ShoppingList.SHOPPING_LIST;
import static com.deyan.mealplanner.jooq.tables.ShoppingListItem.SHOPPING_LIST_ITEM;
import static org.jooq.impl.DSL.*;

@Slf4j
@Service
@Transactional
public class MealPlanService {
    private final RecipeAPIAdapter external;
    private final DSLContext db;
    private final int THRESHOLD_KCAL=200;
    private final int THRESHOLD_MACRO=20;
    public MealPlanService(RecipeAPIAdapter external, DSLContext dsl) {
        this.external = external;
        this.db = dsl;
    }
    public long createPlan(long userId,
                           Integer targetKcal,
                           Integer p, Integer c, Integer f,
                           Integer days) {
        log.debug("SERVICE     ⇢  targetKcal = {}", targetKcal);
        MealPlanDTO apiPlan = external.generateMealPlan(targetKcal, days);
        if (apiPlan.meals() == null || apiPlan.meals().isEmpty()) {
            throw new IllegalStateException(
                    "Spoonacular returned no meals for kcal=" + targetKcal + ", days=" + days);
        }
        int protein = p != null ? p
                : apiPlan.nutrients().protein().intValue();
        int carb = c   != null ? c
                : apiPlan.nutrients().carbohydrates().intValue();
        int fats = f     != null ? f
                : apiPlan.nutrients().fat().intValue();
        int actualKcal = apiPlan.nutrients().calories().intValue();
        int actualProtein = apiPlan.nutrients().protein().intValue();
        int actualCarb = apiPlan.nutrients().carbohydrates().intValue();
        int actualFat = apiPlan.nutrients().fat().intValue();
        // 1) MEAL_PLAN -------------------------------------------------------
        Long planId = (Long) db.fetchValue(
                "insert into meal_plan (" +
                        "   user_id, " +
                        "   target_kcal, target_protein_g, target_carb_g, target_fat_g, " +
                        "   actual_kcal, actual_protein_g, actual_carb_g, actual_fat_g " +
                        ") values (?, ?, ?, ?, ?, ?, ?, ?, ?) returning id",
                userId,
                targetKcal,      protein, carb,      fats,    // targets
                actualKcal,      actualProtein, actualCarb,      actualFat     // actuals
        );
        long kcalDelta  = Math.abs(actualKcal    - targetKcal);
        long protDelta  = Math.abs(actualProtein - protein);
        long carbDelta  = Math.abs(actualCarb    - carb);
        long fatDelta   = Math.abs(actualFat     - fats);
        if (kcalDelta > THRESHOLD_KCAL ||
                protDelta > THRESHOLD_MACRO ||
                carbDelta > THRESHOLD_MACRO ||
                fatDelta  > THRESHOLD_MACRO) {

            /* one central warning with the details */
            log.warn("⚠️  Plan {} differs from targets — kcal Δ={}, P Δ={}, C Δ={}, F Δ={}",
                    planId, kcalDelta, protDelta, carbDelta, fatDelta);
        }


        // 2) loop meals ------------------------------------------------------
        int idx = 0;
        for (MealPlanDTO.Meal m : apiPlan.meals()) {

            RecipeDetailsDTO r = external.getRecipe(m.id());

            upsertRecipe(r);              // recipe table
            upsertIngredients(r);         // ingredient & recipe_ingredient

            db.insertInto(MEAL_PLAN_RECIPE)
                    .set(MEAL_PLAN_RECIPE.MEAL_PLAN_ID, planId)
                    .set(MEAL_PLAN_RECIPE.RECIPE_ID,    r.id())
                    .set(MEAL_PLAN_RECIPE.DAY_INDEX,    (short)(idx / 3))
                    .set(MEAL_PLAN_RECIPE.MEAL_SLOT,
                            switch (idx % 3) { case 0 -> "breakfast"; case 1 -> "lunch"; default -> "dinner";})
                    .execute();

            idx++;
        }

        buildShoppingList(planId);
        return planId;
    }

    /* ---------- helpers ---------- */

    private void upsertRecipe(RecipeDetailsDTO r) {
        db.insertInto(RECIPE)
                .set(RECIPE.ID,                     r.id())
                .set(RECIPE.NAME,                   r.title())
                .set(RECIPE.DESCRIPTION,            (String) null)          // fill later if you like
                .set(RECIPE.PREP_TIME,              r.readyInMinutes())
                .set(RECIPE.SERVINGS,               r.servings())
                .set(RECIPE.KCAL_PER_SERVING,       r.nutrition().calories())
                .set(RECIPE.PROTEIN_G_PER_SERVING,  r.nutrition().protein())
                .set(RECIPE.CARB_G_PER_SERVING,     r.nutrition().carbohydrates())
                .set(RECIPE.FAT_G_PER_SERVING,      r.nutrition().fat())
                .onConflict(RECIPE.ID).doUpdate()
                .set(RECIPE.NAME, r.title())        // update title if it changed
                .execute();
    }

    private void upsertIngredients(RecipeDetailsDTO r) {
        for (var ing : r.extendedIngredients()) {

            db.insertInto(INGREDIENT)
                    .set(INGREDIENT.ID,   ing.id())
                    .set(INGREDIENT.NAME, ing.name())
                    .onConflictDoNothing()
                    .execute();

            db.insertInto(RECIPE_INGREDIENT)
                    .set(RECIPE_INGREDIENT.RECIPE_ID,     r.id())
                    .set(RECIPE_INGREDIENT.INGREDIENT_ID, ing.id())
                    .set(RECIPE_INGREDIENT.QUANTITY_G,      ing.amount())
                    .set(RECIPE_INGREDIENT.UNIT,          ing.unit())
                    .onConflictDoNothing()
                    .execute();
        }
    }

    private void buildShoppingList(long planId) {
        Long listId = (Long) db.fetchValue(
                "insert into shopping_list(meal_plan_id) values (?) returning id",
                planId);




        /* sum grams, then turn the number into a human-readable string (“250 g”) */
        db.insertInto(SHOPPING_LIST_ITEM,
                        SHOPPING_LIST_ITEM.SHOPPING_LIST_ID,
                        SHOPPING_LIST_ITEM.INGREDIENT_ID,
                        SHOPPING_LIST_ITEM.QUANTITY)                 // ← this is TEXT
                .select(
                        db.select(inline(listId),
                                        RECIPE_INGREDIENT.INGREDIENT_ID,
                                        concat(
                                                sum(RECIPE_INGREDIENT.QUANTITY_G)      // numeric SUM
                                                        .cast(String.class),
                                                inline(" g")                           // add unit suffix
                                        ))
                                .from(MEAL_PLAN_RECIPE)
                                .join(RECIPE_INGREDIENT)
                                .on(MEAL_PLAN_RECIPE.RECIPE_ID.eq(RECIPE_INGREDIENT.RECIPE_ID))                // FK (recipe_id)
                                .where(MEAL_PLAN_RECIPE.MEAL_PLAN_ID.eq(planId))
                                .groupBy(RECIPE_INGREDIENT.INGREDIENT_ID)
                ).execute();
    }
}
