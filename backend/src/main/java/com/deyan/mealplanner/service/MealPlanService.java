package com.deyan.mealplanner.service;

import com.deyan.mealplanner.dto.MealPlanDTO;
import com.deyan.mealplanner.dto.MealPlanDetailsDTO;
import com.deyan.mealplanner.dto.MealPlanSummaryDTO;
import com.deyan.mealplanner.dto.RecipeDetailsDTO;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private static final Map<String, BigDecimal> UNIT_TO_GRAMS = Map.ofEntries(
            Map.entry("g", BigDecimal.ONE),
            Map.entry("gram", BigDecimal.ONE),
            Map.entry("grams", BigDecimal.ONE),
            Map.entry("tbsp", new BigDecimal("15")),
            Map.entry("tablespoon", new BigDecimal("15")),
            Map.entry("tablespoons", new BigDecimal("15")),
            Map.entry("tsp", new BigDecimal("5")),
            Map.entry("teaspoon", new BigDecimal("5")),
            Map.entry("teaspoons", new BigDecimal("5")),
            Map.entry("cup", new BigDecimal("240")),
            Map.entry("cups", new BigDecimal("240")),
            Map.entry("clove", new BigDecimal("3")),
            Map.entry("pinch", new BigDecimal("0.3")),
            Map.entry("medium", new BigDecimal("100")),
            Map.entry("serving", new BigDecimal("150"))
    );
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
        int targetCalories = (targetKcal != null) ? targetKcal : apiPlan.nutrients().calories().intValue();
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
                targetCalories,      protein, carb,      fats,    // targets
                actualKcal,      actualProtein, actualCarb,      actualFat     // actuals
        );
        long kcalDelta  = Math.abs(actualKcal    - targetCalories);
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
                .set(RECIPE.URL, r.sourceUrl() != null ? r.sourceUrl() : "")
                .onConflict(RECIPE.ID).doUpdate()
                .set(RECIPE.NAME, r.title())        // update title if it changed
                .execute();

            external.fetchNutritionWidget(r.id()).ifPresent(widget -> {
                BigDecimal kcal = parse(widget.calories());
                BigDecimal protein = parse(widget.protein());
                BigDecimal fat = parse(widget.fat());
                BigDecimal carbs = parse(widget.carbs());

                db.update(RECIPE)
                        .set(RECIPE.CALORIES, kcal)
                        .set(RECIPE.PROTEIN, protein)
                        .set(RECIPE.FAT, fat)
                        .set(RECIPE.CARBOHYDRATES, carbs)
                        .where(RECIPE.ID.eq(r.id()))
                        .execute();

        });
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
                planId
        );

        // 1. Fetch all relevant ingredient quantities for this meal plan
        var ingredients = db.select(
                        RECIPE_INGREDIENT.INGREDIENT_ID,
                        RECIPE_INGREDIENT.QUANTITY_G,
                        RECIPE_INGREDIENT.UNIT
                )
                .from(MEAL_PLAN_RECIPE)
                .join(RECIPE_INGREDIENT)
                .on(MEAL_PLAN_RECIPE.RECIPE_ID.eq(RECIPE_INGREDIENT.RECIPE_ID))
                .where(MEAL_PLAN_RECIPE.MEAL_PLAN_ID.eq(planId))
                .fetch();

        // 2. Merge by ingredient_id
        Map<Long, String> quantities = new LinkedHashMap<>();

        for (var record : ingredients) {
            Long ingId = record.get(RECIPE_INGREDIENT.INGREDIENT_ID);
            BigDecimal rawAmount = record.get(RECIPE_INGREDIENT.QUANTITY_G);
            String unit = record.get(RECIPE_INGREDIENT.UNIT);
            String unitNormalized = unit != null ? unit.toLowerCase().trim() : "";
            BigDecimal safeAmount = rawAmount != null ? rawAmount : BigDecimal.ZERO;

            String quantityText;

            if (UNIT_TO_GRAMS.containsKey(unitNormalized)) {
                // Convert to grams
                BigDecimal grams = safeAmount.multiply(UNIT_TO_GRAMS.get(unitNormalized));
                quantityText = grams.stripTrailingZeros().toPlainString() + " g";
            } else {
                // No conversion, just keep the original unit if available
                quantityText = safeAmount.stripTrailingZeros().toPlainString();
                if (!unitNormalized.isEmpty()) {
                    quantityText += " " + unitNormalized;
                }
            }

            quantities.put(ingId, quantityText);
        }

        // 3. Insert one row per ingredient
        for (var entry : quantities.entrySet()) {
            db.insertInto(SHOPPING_LIST_ITEM)
                    .set(SHOPPING_LIST_ITEM.SHOPPING_LIST_ID, listId)
                    .set(SHOPPING_LIST_ITEM.INGREDIENT_ID, entry.getKey())
                    .set(SHOPPING_LIST_ITEM.QUANTITY, entry.getValue())
                    .execute();
        }
    }
    public MealPlanDetailsDTO getPlanById(long userId, long planId) {
        var plan = db.selectFrom(MEAL_PLAN)
                .where(MEAL_PLAN.ID.eq(planId).and(MEAL_PLAN.USER_ID.eq(userId)))
                .fetchOne();
        if (plan == null) throw new IllegalArgumentException("Plan not found");

        var mealRecords = db.select(
                        MEAL_PLAN_RECIPE.DAY_INDEX,
                        MEAL_PLAN_RECIPE.MEAL_SLOT
                )
                .select(RECIPE.fields()) // separate call
                .from(MEAL_PLAN_RECIPE)
                .join(RECIPE).on(RECIPE.ID.eq(MEAL_PLAN_RECIPE.RECIPE_ID))
                .where(MEAL_PLAN_RECIPE.MEAL_PLAN_ID.eq(planId))
                .fetch();

        var meals = mealRecords.stream().map(r -> {
            var recipeId = r.get(RECIPE.ID);
            var ingredients = db.select(
                            INGREDIENT.ID,
                            INGREDIENT.NAME,
                            RECIPE_INGREDIENT.QUANTITY_G,
                            RECIPE_INGREDIENT.UNIT
                    ).from(RECIPE_INGREDIENT)
                    .join(INGREDIENT).on(INGREDIENT.ID.eq(RECIPE_INGREDIENT.INGREDIENT_ID))
                    .where(RECIPE_INGREDIENT.RECIPE_ID.eq(recipeId))
                    .fetch()
                    .stream()
                    .map(ir -> new RecipeDetailsDTO.ExtendedIngredient(
                            ir.get(INGREDIENT.ID),
                            ir.get(INGREDIENT.NAME),
                            ir.get(RECIPE_INGREDIENT.QUANTITY_G),
                            ir.get(RECIPE_INGREDIENT.UNIT)
                    )).toList();


            var recipe = new MealPlanDetailsDTO.RecipeDTO(
                    recipeId,
                    r.get(RECIPE.NAME),
                    r.get(RECIPE.PREP_TIME),
                    r.get(RECIPE.SERVINGS),
                    r.get(RECIPE.URL),
                    ingredients
            );

            return new MealPlanDetailsDTO.MealSlotDTO(
                    "Day " + r.get(MEAL_PLAN_RECIPE.DAY_INDEX),
                    r.get(MEAL_PLAN_RECIPE.MEAL_SLOT),
                    recipe
            );
        }).toList();

//        var shoppingList = db.select(
//                        INGREDIENT.ID,
//                        INGREDIENT.NAME,
//                        SHOPPING_LIST_ITEM.QUANTITY
//                )
//                .from(SHOPPING_LIST)
//                .join(SHOPPING_LIST_ITEM).on(SHOPPING_LIST.ID.eq(SHOPPING_LIST_ITEM.SHOPPING_LIST_ID))
//                .join(INGREDIENT).on(INGREDIENT.ID.eq(SHOPPING_LIST_ITEM.INGREDIENT_ID))
//                .where(SHOPPING_LIST.MEAL_PLAN_ID.eq(planId))
//                .fetch()
//                .map(r -> new MealPlanDetailsDTO.ShoppingListItemDTO(
//                        r.get(INGREDIENT.ID),
//                        r.get(INGREDIENT.NAME),
//                        r.get(SHOPPING_LIST_ITEM.QUANTITY)
//                ));
        var rawItems = db.select(
                        INGREDIENT.ID,
                        INGREDIENT.NAME,
                        SHOPPING_LIST_ITEM.QUANTITY
                )
                .from(SHOPPING_LIST_ITEM)
                .join(SHOPPING_LIST).on(SHOPPING_LIST.ID.eq(SHOPPING_LIST_ITEM.SHOPPING_LIST_ID))
                .join(INGREDIENT).on(INGREDIENT.ID.eq(SHOPPING_LIST_ITEM.INGREDIENT_ID))
                .where(SHOPPING_LIST.MEAL_PLAN_ID.eq(planId))
                .fetch()
                .map(r -> new MealPlanDetailsDTO.ShoppingListItemDTO(
                        r.get(INGREDIENT.ID),
                        r.get(INGREDIENT.NAME),
                        r.get(SHOPPING_LIST_ITEM.QUANTITY)
                ));

// 2. Deduplicate by name + unit (case-insensitive)
            Map<String, BigDecimal> merged = new LinkedHashMap<>();

        for (var item : rawItems) {
            String name = item.name().toLowerCase().trim();
            String quantityText = item.quantityText();

            // Try to split into numeric value and unit
            String numberPart = quantityText.replaceAll("[^\\d.]", "");
            String unitPart = quantityText.replaceAll("[\\d.\\s]", "");

            BigDecimal quantity;
            try {
                quantity = new BigDecimal(numberPart);
            } catch (Exception e) {
                quantity = BigDecimal.ZERO;
            }

            String key = name + "_" + unitPart;
            merged.merge(key, quantity, BigDecimal::add);
        }

// 3. Convert merged map back to ShoppingListItemDTOs
        List<MealPlanDetailsDTO.ShoppingListItemDTO> shoppingList = merged.entrySet().stream()
                .map(entry -> {
                    String[] parts = entry.getKey().split("_");
                    String name = parts[0];
                    String unit = parts.length > 1 ? parts[1] : "";
                    String quantityText = entry.getValue().toPlainString() + " " + unit;
                    return new MealPlanDetailsDTO.ShoppingListItemDTO(null, name, quantityText);
                }).toList();

        return new MealPlanDetailsDTO(
                plan.getId(),
                BigDecimal.valueOf(plan.getTargetKcal()), BigDecimal.valueOf(plan.getTargetProteinG()), BigDecimal.valueOf(plan.getTargetCarbG()), BigDecimal.valueOf(plan.getTargetFatG()),
                BigDecimal.valueOf(plan.getActualKcal()), BigDecimal.valueOf(plan.getActualProteinG()), BigDecimal.valueOf(plan.getActualCarbG()), BigDecimal.valueOf(plan.getActualFatG()),
                meals,
                shoppingList
        );
    }
    public List<MealPlanSummaryDTO> getUserPlans(long userId) {
        return db.select(
                        MEAL_PLAN.ID,
                        MEAL_PLAN.TARGET_KCAL,
                        MEAL_PLAN.ACTUAL_KCAL,
                        MEAL_PLAN.CREATED_AT
                )
                .from(MEAL_PLAN)
                .where(MEAL_PLAN.USER_ID.eq(userId))
                .orderBy(MEAL_PLAN.CREATED_AT.desc())
                .fetch()
                .map(r -> new MealPlanSummaryDTO(
                        r.get(MEAL_PLAN.ID),
                        BigDecimal.valueOf(r.get(MEAL_PLAN.TARGET_KCAL)),
                        BigDecimal.valueOf(r.get(MEAL_PLAN.ACTUAL_KCAL)),
                        r.get(MEAL_PLAN.CREATED_AT).toString()
                ));
    }
    public void deletePlan(long userId, long planId) {
        var valid = db.fetchExists(
                db.selectFrom(MEAL_PLAN)
                        .where(MEAL_PLAN.ID.eq(planId).and(MEAL_PLAN.USER_ID.eq(userId)))
        );
        if (!valid) throw new IllegalArgumentException("Plan not found or unauthorized");

        db.deleteFrom(SHOPPING_LIST_ITEM)
                .where(SHOPPING_LIST_ITEM.SHOPPING_LIST_ID.in(
                        db.select(SHOPPING_LIST.ID)
                                .from(SHOPPING_LIST)
                                .where(SHOPPING_LIST.MEAL_PLAN_ID.eq(planId))
                )).execute();

        db.deleteFrom(SHOPPING_LIST)
                .where(SHOPPING_LIST.MEAL_PLAN_ID.eq(planId))
                .execute();

        db.deleteFrom(MEAL_PLAN_RECIPE)
                .where(MEAL_PLAN_RECIPE.MEAL_PLAN_ID.eq(planId))
                .execute();

        db.deleteFrom(MEAL_PLAN)
                .where(MEAL_PLAN.ID.eq(planId))
                .execute();
    }
    private BigDecimal parse(String input) {
        try {
            return new BigDecimal(input.replaceAll("[^\\d.]", ""));
        } catch (Exception e) {
            return null;
        }
    }
}
