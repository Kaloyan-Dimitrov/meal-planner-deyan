package com.deyan.mealplanner.controller;

import com.deyan.mealplanner.service.MealPlanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
@Slf4j
@RestController
@RequestMapping("api/users/{userId}/meal-plans")
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

    public record CreateReq(Integer targetKcal,
                            Integer days,
                            Integer proteinG,
                            Integer carbG,
                            Integer fatG) { }

    public record CreatedDTO(Long id){}
}
