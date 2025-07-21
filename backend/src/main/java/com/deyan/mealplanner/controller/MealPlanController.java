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
    @PostMapping
    public CreatedDTO createMealPlan(@PathVariable Long userId, @RequestBody CreateReq r){
        log.debug("CONTROLLER  ⇢  req.targetKcal = {}", r.targetKcal);
        long id = mealPlanService.createPlan(
                userId,
                r.targetKcal(), r.proteinG(), r.carbG(), r.fatG(),r.days()!=null ? r.days() : null);
        return new CreatedDTO(id);
    }
    @PostMapping("/{planId}/regenerate")
    public MealPlanDetailsDTO regenerateMealPlan(@PathVariable Long userId, @PathVariable Long planId) {
        return mealPlanService.regenerate(userId, planId);
    }
    @GetMapping("/{planId}")
    public MealPlanDetailsDTO getMealPlanById(@PathVariable Long userId, @PathVariable Long planId) {
        return mealPlanService.getPlanById(userId, planId);
    }
    @GetMapping("/latest")
    public ResponseEntity<MealPlanDetailsDTO> getLatestMealPlan(@PathVariable Long userId) {
        MealPlanDetailsDTO plan = mealPlanService.getLatestPlanForUser(userId);
        return ResponseEntity.ok(plan);
    }
    @GetMapping
    public List<MealPlanSummaryDTO> getUserMealPlans(@PathVariable Long userId) {
        return mealPlanService.getUserPlans(userId);
    }
    @DeleteMapping("/{planId}")
    public void deleteMealPlan(@PathVariable Long userId, @PathVariable Long planId) {
        mealPlanService.deletePlan(userId, planId);
    }


    public record CreateReq(Integer targetKcal,
                            Integer days,
                            Integer proteinG,
                            Integer carbG,
                            Integer fatG) { }

    public record CreatedDTO(Long id){}
}
