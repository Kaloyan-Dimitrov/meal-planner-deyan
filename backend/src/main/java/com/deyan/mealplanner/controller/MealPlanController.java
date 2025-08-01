package com.deyan.mealplanner.controller;

import com.deyan.mealplanner.dto.MealPlanDetailsDTO;
import com.deyan.mealplanner.dto.MealPlanSummaryDTO;
import com.deyan.mealplanner.service.MealPlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users/{userId}/meal-plans")
public class MealPlanController {

    private final MealPlanService mealPlanService;

    public MealPlanController(MealPlanService mealPlanService) {
        this.mealPlanService = mealPlanService;
    }

    /**
     * Creates a new meal plan for the specified user based on target macros and duration.
     *
     * @param userId The ID of the user.
     * @param r The meal plan request containing macros and duration.
     * @return A DTO containing the newly created meal plan's ID.
     */
    @PostMapping
    public CreatedDTO createMealPlan(@PathVariable Long userId, @RequestBody CreateReq r) {
        long id = mealPlanService.createPlan(
                userId,
                r.targetKcal(), r.proteinG(), r.carbG(), r.fatG(),
                r.days() != null ? r.days() : null
        );
        return new CreatedDTO(id);
    }

    /**
     * Regenerates the meals for an existing meal plan.
     *
     * @param userId The ID of the user.
     * @param planId The ID of the meal plan to regenerate.
     * @return A DTO containing the updated meal plan details.
     */
    @PostMapping("/{planId}/regenerate")
    public MealPlanDetailsDTO regenerateMealPlan(@PathVariable Long userId, @PathVariable Long planId) {
        return mealPlanService.regenerate(userId, planId);
    }

    /**
     * Retrieves a specific meal plan by its ID.
     *
     * @param userId The ID of the user.
     * @param planId The ID of the meal plan.
     * @return A DTO containing the meal plan's details.
     */
    @GetMapping("/{planId}")
    public MealPlanDetailsDTO getMealPlanById(@PathVariable Long userId, @PathVariable Long planId) {
        return mealPlanService.getPlanById(userId, planId);
    }

    /**
     * Retrieves the most recently created meal plan for a user.
     *
     * @param userId The ID of the user.
     * @return The latest meal plan details.
     */
    @GetMapping("/latest")
    public ResponseEntity<MealPlanDetailsDTO> getLatestMealPlan(@PathVariable Long userId) {
        MealPlanDetailsDTO plan = mealPlanService.getLatestPlanForUser(userId);
        return ResponseEntity.ok(plan);
    }

    /**
     * Retrieves a summary list of all meal plans for the given user.
     *
     * @param userId The ID of the user.
     * @return A list of meal plan summaries.
     */
    @GetMapping
    public List<MealPlanSummaryDTO> getUserMealPlans(@PathVariable Long userId) {
        return mealPlanService.getUserPlans(userId);
    }

    /**
     * Deletes a specific meal plan.
     *
     * @param userId The ID of the user.
     * @param planId The ID of the meal plan to delete.
     */
    @DeleteMapping("/{planId}")
    public void deleteMealPlan(@PathVariable Long userId, @PathVariable Long planId) {
        mealPlanService.deletePlan(userId, planId);
    }

    /**
     * Request body for creating a meal plan.
     *
     * @param targetKcal The target calories.
     * @param days The number of days for the plan.
     * @param proteinG Target grams of protein.
     * @param carbG Target grams of carbohydrates.
     * @param fatG Target grams of fat.
     */
    public record CreateReq(Integer targetKcal,
                            Integer days,
                            Integer proteinG,
                            Integer carbG,
                            Integer fatG) { }

    /**
     * Response wrapper containing the ID of a newly created meal plan.
     *
     * @param id The meal plan ID.
     */
    public record CreatedDTO(Long id) { }
}
